package tqs.data;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingRequest booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public StatusHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public StatusHistory(BookingRequest booking, Status status) {
        this();
        this.booking = booking;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookingRequest getBooking() {
        return booking;
    }

    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BookingStatusHistory{" +
                "id=" + id +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
