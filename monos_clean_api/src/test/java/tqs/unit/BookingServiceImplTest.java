package tqs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.data.*;
import tqs.dto.BookingRequestDTO;
import tqs.dto.BookingResponseDTO;
import tqs.exceptions.InvalidBookingException;
import tqs.services.BookingServiceImpl;
import tqs.services.MunicipalityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRequestRepo bookingRepo;

    @Mock
    private MunicipalityService municipalityService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDTO validDTO;

    @BeforeEach
    void setUp() {
        validDTO = new BookingRequestDTO();
        validDTO.setMunicipality("LISBOA");
        validDTO.setDescription("Old sofa");
        validDTO.setCollectionDate(LocalDate.now().plusDays(1));
        validDTO.setTimeSlot(Slot.MORNING);
        validDTO.setContactInfo("user@example.com");
        validDTO.setAddress("Abc Main Str. n123");
    }

    // client
    @Test
    void shouldCreateBookingWhenAllConditionsMet() {
        when(municipalityService.isValid("LISBOA")).thenReturn(true);
        when(bookingRepo.countByMunicipalityAndCollectionDateAndTimeSlot(eq("LISBOA"), any(), any())).thenReturn(0L);
        when(bookingRepo.save(any())).thenAnswer(invocation -> {
            BookingRequest b = invocation.getArgument(0);
            b.setId(1L);
            b.setToken("TOKEN1234567890ABCDE");
            b.setCreatedAt(LocalDateTime.now());
            return b;
        });

        BookingResponseDTO result = bookingService.createBooking(validDTO);

        assertThat(result.getToken()).isNotNull().hasSize(20);
        assertThat(result.getStatus()).isEqualTo(Status.RECEIVED);
        assertThat(result.getMunicipality()).isEqualTo("LISBOA");
        verify(bookingRepo).save(any(BookingRequest.class));
    }

    @Test
    void shouldRejectBookingWhenMunicipalityInvalid() {
        when(municipalityService.isValid("LISBOA")).thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(validDTO))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Invalid municipality code: LISBOA");
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void shouldRejectBookingWhenNoCapacity() {
        when(municipalityService.isValid("LISBOA")).thenReturn(true);
        when(bookingRepo.countByMunicipalityAndCollectionDateAndTimeSlot(eq("LISBOA"), any(), any()))
                .thenReturn(15L);

        assertThatThrownBy(() -> bookingService.createBooking(validDTO))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("No capacity available for selected date and time slot for LISBOA");
        verify(bookingRepo, never()).save(any());
    }

    // cancel bookings
    @Test
    void shouldCancelBookingWhenStatusIsReceivable() {
        BookingRequest booking = new BookingRequest();
        booking.setToken("TOKEN1234567890ABCDE");
        booking.setStatus(Status.RECEIVED);
        when(bookingRepo.findByToken("TOKEN1234567890ABCDE")).thenReturn(Optional.of(booking));

        bookingService.cancelBookingByToken("TOKEN1234567890ABCDE");

        assertThat(booking.getStatus()).isEqualTo(Status.CANCELLED);
        verify(bookingRepo).save(booking);
    }

    @Test
    void shouldNotCancelBookingWhenAlreadyCompleted() {
        BookingRequest booking = new BookingRequest();
        booking.setToken("TOKEN1234567890ABCDE");
        booking.setStatus(Status.COMPLETED);
        when(bookingRepo.findByToken("TOKEN1234567890ABCDE")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBookingByToken("TOKEN1234567890ABCDE"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Cannot cancel booking in status: COMPLETED");
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void shouldThrowWhenCancelingNonExistentBooking() {
        when(bookingRepo.findByToken("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBookingByToken("UNKNOWN"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("No booking found under token: UNKNOWN");
    }

    // staff
    @Test
    void shouldUpdateBookingStatus() {
        BookingRequest booking = new BookingRequest();
        booking.setToken("TOKEN1234567890ABCDE");
        booking.setStatus(Status.RECEIVED);
        when(bookingRepo.findByToken("TOKEN1234567890ABCDE")).thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.updateBookingStatus("TOKEN1234567890ABCDE", Status.ASSIGNED);

        assertThat(result.getStatus()).isEqualTo(Status.ASSIGNED);
        assertThat(result.getHistory()).hasSize(1);
        verify(bookingRepo).save(booking);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentBooking() {
        when(bookingRepo.findByToken("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus("UNKNOWN", Status.ASSIGNED))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("No booking found under token: UNKNOWN");
    }

    @Test
    void shouldGetBookingsByMunicipalityAndDate() {
        List<BookingRequest> mockBookings = Arrays.asList(createMockBooking("LISBOA", LocalDate.now()));
        when(bookingRepo.findByMunicipalityAndCollectionDate("LISBOA", LocalDate.now()))
                .thenReturn(mockBookings);

        List<BookingResponseDTO> result = bookingService.getBookingsByMunicipalityByDate("LISBOA", LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMunicipality()).isEqualTo("LISBOA");
    }

    @Test
    void shouldGetBookingsByStatus() {
        List<BookingRequest> mockBookings = Arrays.asList(createMockBooking("PORTO", LocalDate.now()));
        when(bookingRepo.findByStatus(Status.RECEIVED)).thenReturn(mockBookings);

        List<BookingResponseDTO> result = bookingService.getBookingsByStatus(Status.RECEIVED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.RECEIVED);
    }

    // validation and utils
    @Test
    void shouldRejectBookingInPast() {
        assertThatThrownBy(() -> bookingService.validateBookingDate(LocalDate.now().minusDays(1)))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Booking date cannot be in the past");
    }

    @Test
    void shouldRejectBookingBeyond14Days() {
        assertThatThrownBy(() -> bookingService.validateBookingDate(LocalDate.now().plusDays(15)))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Can only book 2 weeks ahead");
    }

    @Test
    void shouldGenerateUniqueToken() {
        String token1 = bookingService.generateToken();
        String token2 = bookingService.generateToken();
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).matches("^[A-Z0-9]{20}$");
    }

    @Test
    void shouldGetBookingsByDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);
        List<BookingRequest> mockBookings = Arrays.asList(
                createMockBooking("LISBOA", start.plusDays(1)),
                createMockBooking("PORTO", start.plusDays(2)));

        when(bookingRepo.findByCollectionDateBetween(start, end)).thenReturn(mockBookings);

        List<BookingResponseDTO> result = bookingService.getBookingsByDateRange(start, end);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMunicipality()).isEqualTo("LISBOA");
        assertThat(result.get(1).getMunicipality()).isEqualTo("PORTO");
        verify(bookingRepo).findByCollectionDateBetween(start, end);
    }

    @Test
    void shouldGetBookingByToken() {
        BookingRequest mockBooking = createMockBooking("LISBOA", LocalDate.now());
        when(bookingRepo.findByToken("TOKEN123")).thenReturn(Optional.of(mockBooking));

        BookingResponseDTO result = bookingService.getBookingByToken("TOKEN123");

        assertThat(result).isNotNull();
        assertThat(result.getMunicipality()).isEqualTo("LISBOA");
        assertThat(result.getToken()).isEqualTo("TOKEN");
        verify(bookingRepo).findByToken("TOKEN123");
    }

    @Test
    void shouldThrowWhenGettingNonExistentBooking() {
        when(bookingRepo.findByToken("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByToken("UNKNOWN"))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("No booking found under token: UNKNOWN");
    }

    @Test
    void shouldThrowWhenUpdatingToInvalidStatus() {
        BookingRequest booking = new BookingRequest();
        booking.setToken("TOKEN123");
        booking.setStatus(Status.COMPLETED); // COMPLETED can't transition to any other status
        when(bookingRepo.findByToken("TOKEN123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus("TOKEN123", Status.ASSIGNED))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Cannot update booking status COMPLETED to status ASSIGNED");

        verify(bookingRepo, never()).save(any());
    }

    @Test
    void shouldUpdateToValidNextStatus() {
        BookingRequest booking = new BookingRequest();
        booking.setToken("TOKEN123");
        booking.setStatus(Status.RECEIVED); // RECEIVED can transition to ASSIGNED
        when(bookingRepo.findByToken("TOKEN123")).thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.updateBookingStatus("TOKEN123", Status.ASSIGNED);

        assertThat(result.getStatus()).isEqualTo(Status.ASSIGNED);
        assertThat(result.getHistory()).hasSize(1);
        verify(bookingRepo).save(booking);
    }

    // helper function
    private BookingRequest createMockBooking(String municipality, LocalDate date) {
        BookingRequest b = new BookingRequest();
        b.setId(1L);
        b.setToken("TOKEN");
        b.setMunicipality(municipality);
        b.setCollectionDate(date);
        b.setTimeSlot(Slot.MORNING);
        b.setDescription("Item");
        b.setStatus(Status.RECEIVED);
        b.setCreatedAt(LocalDateTime.now());
        b.setStatusHistory(Arrays.asList(
                new StatusHistory(b, Status.RECEIVED)));
        return b;
    }
}