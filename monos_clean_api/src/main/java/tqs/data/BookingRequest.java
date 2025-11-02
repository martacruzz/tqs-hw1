package tqs.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_token", columnList = "token", unique = true),
        @Index(name = "idx_municipality_date", columnList = "municipality, collection_date")
})
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique token for citizens to check status
    @Column(nullable = false, unique = true, length = 20)
    @NotNull(message = "Token is mandatory")
    private String token;

    // municipality code - from api
    @Column(nullable = false, length = 10)
    @NotNull(message = "Municipality is mandatory")
    private String municipality;

    // description of items to collect
    @Column(nullable = false, length = 500)
    @NotNull(message = "Description is mandatory")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // collection date
    @Column(name = "collection_date", nullable = false)
    @NotNull(message = "Date is mandatory")
    private LocalDate collectionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    @NotNull(message = "Time slot is mandatory")
    private Slot timeSlot;

    // current status of the booking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is mandatory")
    private Status status;

    // contact info - phone or email
    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    // address for collection
    @Column(length = 200)
    private String address;

    // creation ts
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // last update ts
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // history of status changes - the evolution of states is timestamped and
    // displayed in the web page
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    public BookingRequest() {
        this.createdAt = LocalDateTime.now();
        // TODO check if we need to add updatedAt here
        this.status = Status.RECEIVED;
    }

    public BookingRequest(String municipality, String description, LocalDate date, Slot slot, String contactInfo,
            String address) {
        this(); // sets createdAt and status
        this.municipality = municipality;
        this.description = description;
        this.collectionDate = date;
        this.timeSlot = slot;
        this.contactInfo = contactInfo;
        this.address = address;
    }

    public void addStatusHistory(Status newStatus) {
        StatusHistory bsh = new StatusHistory(this, newStatus);
        this.statusHistory.add(bsh);
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // jpa lifecycle callbacks
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String itemsDescription) {
        this.description = itemsDescription;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public Slot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Slot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<StatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    // Utility methods
    public boolean isCancellable() {
        return status == Status.RECEIVED || status == Status.ASSIGNED;
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED || status == Status.CANCELLED;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", municipality='" + municipality + '\'' +
                ", collectionDate=" + collectionDate +
                ", timeSlot=" + timeSlot +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

}
