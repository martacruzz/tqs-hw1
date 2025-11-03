package tqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tqs.data.Slot;
import tqs.data.Status;
import tqs.dto.BookingRequestDTO;
import tqs.dto.BookingResponseDTO;
import tqs.services.BookingService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CitizenBookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDTO validRequest;
    private BookingResponseDTO validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new BookingRequestDTO();
        validRequest.setMunicipality("LISBOA");
        validRequest.setDescription("Old sofa");
        validRequest.setCollectionDate(LocalDate.now().plusDays(1));
        validRequest.setTimeSlot(Slot.MORNING);
        validRequest.setContactInfo("user@example.com");
        validRequest.setAddress("Abc Main Str. n123");

        validResponse = new BookingResponseDTO();
        validResponse.setToken("TOKEN1234567890ABCDE");
        validResponse.setMunicipality("LISBOA");
        validResponse.setDescription("Old sofa");
        validResponse.setDate(LocalDate.now().plusDays(1));
        validResponse.setSlot(Slot.MORNING);
        validResponse.setStatus(Status.RECEIVED);
        validResponse.setContactInfo("user@example.com");
        validRequest.setAddress("Abc Main Str. n123");

    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        when(bookingService.createBooking(any(BookingRequestDTO.class))).thenReturn(validResponse);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("TOKEN1234567890ABCDE"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.municipality").value("LISBOA"));

        verify(bookingService).createBooking(any(BookingRequestDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenMissingRequiredFields() throws Exception {
        BookingRequestDTO invalid = new BookingRequestDTO();
        // missing all required fields

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBookingByToken() throws Exception {
        when(bookingService.getBookingByToken("TOKEN1234567890ABCDE")).thenReturn(validResponse);

        mockMvc.perform(get("/api/bookings/TOKEN1234567890ABCDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TOKEN1234567890ABCDE"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void shouldReturnNotFoundWhenBookingDoesNotExist() throws Exception {
        when(bookingService.getBookingByToken("UNKNOWN"))
                .thenThrow(new tqs.exceptions.InvalidBookingException("No booking found under token: UNKNOWN"));

        mockMvc.perform(get("/api/bookings/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCancelBookingSuccessfully() throws Exception {
        doNothing().when(bookingService).cancelBookingByToken("TOKEN1234567890ABCDE");

        mockMvc.perform(delete("/api/bookings/TOKEN1234567890ABCDE"))
                .andExpect(status().isNoContent());

        verify(bookingService).cancelBookingByToken("TOKEN1234567890ABCDE");
    }

    @Test
    void shouldReturnNotFoundWhenCancelingNonExistentBooking() throws Exception {
        doThrow(new tqs.exceptions.InvalidBookingException("No booking found under token: UNKNOWN"))
                .when(bookingService).cancelBookingByToken("UNKNOWN");

        mockMvc.perform(delete("/api/bookings/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}