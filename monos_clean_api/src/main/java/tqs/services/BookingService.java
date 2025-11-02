package tqs.services;

import java.time.LocalDate;
import java.util.List;

import tqs.data.*;
import tqs.dto.*;

public interface BookingService {
    // citizen and staff
    public BookingResponseDTO createBooking(BookingRequestDTO request);

    public BookingResponseDTO getBookingByToken(String token);

    public void cancelBookingByToken(String token);

    // staff only
    public BookingResponseDTO updateBookingStatus(String token, Status newStatus);

    public List<BookingResponseDTO> getBookingsByMunicipalityByDate(String municipality, LocalDate date);

    public List<BookingResponseDTO> getBookingsByStatus(Status status);

    public List<BookingResponseDTO> getBookingsByDateRange(LocalDate start, LocalDate end);

    // validation
    public void validateBookingDate(LocalDate date);

    public void validateMunicipality(String municipality);

    public boolean hasCapacity(String municipality, LocalDate date, Slot slot);

    public String generateToken();
}
