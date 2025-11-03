package tqs.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;
import tqs.data.BookingRequest;
import tqs.data.BookingRequestRepo;
import tqs.data.Slot;
import tqs.data.Status;
import tqs.dto.BookingRequestDTO;
import tqs.dto.BookingResponseDTO;
import tqs.dto.StatusHistoryDTO;
import tqs.exceptions.InvalidBookingException;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final int MAX_CAPACITY_PER_SLOT = 15;

    private BookingRequestRepo repo;
    private MunicipalityService municipalityService;

    @Autowired
    public BookingServiceImpl(BookingRequestRepo repo, MunicipalityService municipalityService) {
        this.repo = repo;
        this.municipalityService = municipalityService;
    }

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        // validate input
        validateBookingDate(request.getCollectionDate());
        validateMunicipality(request.getMunicipality());

        // check capacity
        if (!hasCapacity(request.getMunicipality(), request.getCollectionDate(), request.getTimeSlot())) {
            throw new InvalidBookingException(
                    "No capacity available for selected date and time slot for " + request.getMunicipality());
        }

        // unmarshall dto to booking object
        BookingRequest booking = new BookingRequest(request.getMunicipality(), request.getDescription(),
                request.getCollectionDate(), request.getTimeSlot(), request.getContactInfo(), request.getAddress());

        // get unique token
        String token = generateToken();
        booking.setToken(token);

        // save to repo
        repo.save(booking);
        logger.info("Booking created with token: " + token);

        return toResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO getBookingByToken(String token) {
        BookingRequest booking = repo.findByToken(token)
                .orElseThrow(() -> new InvalidBookingException("No booking found under token: " + token));
        return toResponseDTO(booking);
    }

    @Override
    public void cancelBookingByToken(String token) {
        BookingRequest booking = repo.findByToken(token)
                .orElseThrow(() -> new InvalidBookingException("No booking found under token: " + token));

        if (!booking.getStatus().canTransition(Status.CANCELLED)) {
            throw new InvalidBookingException("Cannot cancel booking in status: " + booking.getStatus());
        }

        booking.addStatusHistory(Status.CANCELLED);
        repo.save(booking);

        String safeToken = sanitizeForLog(token);
        logger.info("Booking under token {} was cancelled", safeToken);
    }

    @Override
    public BookingResponseDTO updateBookingStatus(String token, Status newStatus) {
        BookingRequest booking = repo.findByToken(token)
                .orElseThrow(() -> new InvalidBookingException("No booking found under token: " + token));

        if (!booking.getStatus().canTransition(newStatus)) {
            throw new InvalidBookingException(
                    String.format("Cannot update booking status %s to status %s", booking.getStatus().toString(),
                            newStatus.toString()));
        }

        booking.addStatusHistory(newStatus);
        repo.save(booking);
        logger.info("Booking under token {} was updated to status {}", token, newStatus);
        return toResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByMunicipalityByDate(String municipalityCode, LocalDate date) {
        return repo.findByMunicipalityAndCollectionDate(municipalityCode, date)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<BookingResponseDTO> getBookingsByStatus(Status status) {
        return repo.findByStatus(status)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<BookingResponseDTO> getBookingsByDateRange(LocalDate start, LocalDate end) {
        return repo.findByCollectionDateBetween(start, end)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public void validateBookingDate(LocalDate date) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new InvalidBookingException("Booking date cannot be in the past");
        }

        // can only book 2 weeks ahead
        if (date.isAfter(today.plusDays(14))) {
            throw new InvalidBookingException("Can only book 2 weeks ahead");
        }
    }

    @Override
    public void validateMunicipality(String municipalityCode) {
        if (!this.municipalityService.isValid(municipalityCode)) {
            throw new InvalidBookingException("Invalid municipality code: " + municipalityCode);
        }
    }

    @Override
    public boolean hasCapacity(String municipality, LocalDate date, Slot slot) {
        long count = repo.countByMunicipalityAndCollectionDateAndTimeSlot(municipality, date, slot);
        return count < MAX_CAPACITY_PER_SLOT;
    }

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    // utils
    private BookingResponseDTO toResponseDTO(BookingRequest booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setToken(booking.getToken());
        dto.setMunicipality(booking.getMunicipality());
        dto.setDescription(booking.getDescription());
        dto.setDate(booking.getCollectionDate());
        dto.setSlot(booking.getTimeSlot());
        dto.setStatus(booking.getStatus());
        dto.setContactInfo(booking.getContactInfo());
        dto.setAddress(booking.getAddress());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        List<StatusHistoryDTO> history = booking.getStatusHistory().stream()
                .map(hist -> {
                    StatusHistoryDTO historyDTO = new StatusHistoryDTO();
                    historyDTO.setStatus(hist.getStatus());
                    historyDTO.setTs(hist.getTimestamp());
                    return historyDTO;
                }).toList();

        dto.setHistory(history);
        return dto;
    }

    // this function is to fix a security logging issue pointed out by sonar
    private String sanitizeForLog(String input) {
        if (input == null) {
            return "";
        }
        // allow only uppercase letters and numbers -- normal aspect of token
        return input.matches("^[A-Z0-9]{20}$") ? input : "[INVALID_TOKEN_FORMAT]";
    }

}
