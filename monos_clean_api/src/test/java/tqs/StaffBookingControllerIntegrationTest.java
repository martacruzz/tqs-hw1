package tqs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.data.Status;
import tqs.dto.BookingResponseDTO;
import tqs.services.BookingService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StaffBookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private List<BookingResponseDTO> mockBookings;

    @BeforeEach
    void setUp() {
        BookingResponseDTO b1 = new BookingResponseDTO();
        b1.setToken("TOKEN1234567890ABCDE");
        b1.setMunicipality("LISBOA");
        b1.setStatus(Status.RECEIVED);
        b1.setDate(LocalDate.now());

        BookingResponseDTO b2 = new BookingResponseDTO();
        b2.setToken("TOKEN1234567890ABCDF");
        b2.setMunicipality("PORTO");
        b2.setStatus(Status.ASSIGNED);
        b2.setDate(LocalDate.now());

        mockBookings = Arrays.asList(b1, b2);
    }

    @Test
    void shouldGetAllBookings() throws Exception {
        when(bookingService.getBookingsByDateRange(any(), any())).thenReturn(mockBookings);

        mockMvc.perform(get("/api/staff/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].municipality").value("LISBOA"));
    }

    @Test
    void shouldGetBookingsByMunicipalityAndDate() throws Exception {
        when(bookingService.getBookingsByMunicipalityByDate(eq("LISBOA"), any(LocalDate.class)))
                .thenReturn(Arrays.asList(mockBookings.get(0)));

        mockMvc.perform(get("/api/staff/bookings")
                .param("municipality", "LISBOA")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].municipality").value("LISBOA"));
    }

    @Test
    void shouldGetBookingsByStatus() throws Exception {
        when(bookingService.getBookingsByStatus(Status.RECEIVED))
                .thenReturn(Arrays.asList(mockBookings.get(0)));

        mockMvc.perform(get("/api/staff/bookings")
                .param("status", "RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    @Test
    void shouldUpdateBookingStatus() throws Exception {
        BookingResponseDTO updated = new BookingResponseDTO();
        updated.setToken("TOKEN1234567890ABCDE");
        updated.setStatus(Status.IN_PROGRESS);
        when(bookingService.updateBookingStatus(eq("TOKEN1234567890ABCDE"), any(Status.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/staff/bookings/TOKEN1234567890ABCDE/update")
                .param("newStatus", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidStatus() throws Exception {
        when(bookingService.updateBookingStatus(anyString(), any(Status.class)))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        mockMvc.perform(patch("/api/staff/bookings/TOKEN1234567890ABCDE/update")
                .param("newStatus", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}