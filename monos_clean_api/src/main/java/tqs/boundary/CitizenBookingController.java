package tqs.boundary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tqs.dto.BookingRequestDTO;
import tqs.dto.BookingResponseDTO;
import tqs.services.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class CitizenBookingController {

    private final BookingService service;

    @Autowired
    public CitizenBookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = service.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{token}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable("token") String token) {
        System.out.println("🔍 Received token: " + token);
        BookingResponseDTO response = service.getBookingByToken(token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable("token") String token) {
        service.cancelBookingByToken(token);
        return ResponseEntity.noContent().build();
    }

}
