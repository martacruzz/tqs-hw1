package tqs.boundary;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tqs.data.Status;
import tqs.dto.BookingResponseDTO;
import tqs.services.BookingService;

@RestController
@RequestMapping("/api/staff/bookings")
public class StaffBookingController {

    private final BookingService service;

    @Autowired
    public StaffBookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = service.getBookingsByDateRange(
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(14));
        return ResponseEntity.ok(bookings);
    }

    @GetMapping(params = "municipality")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByMunicipalityAndDate(
            @RequestParam("municipality") String municipality,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookingResponseDTO> bookings = service.getBookingsByMunicipalityByDate(municipality, date);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping(params = "status")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByStatus(
            @RequestParam("status") Status status) {
        List<BookingResponseDTO> bookings = service.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{token}/update")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable("token") String token,
            @RequestParam("newStatus") Status newStatus) {
        BookingResponseDTO updated = service.updateBookingStatus(token, newStatus);
        return ResponseEntity.ok(updated);
    }
}