package tqs.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import tqs.data.Slot;

public class BookingRequestDTO {

    @NotBlank(message = "Municipality is mandatory")
    private String municipality;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Date is mandatory")
    private LocalDate collectionDate;

    @NotNull(message = "Time slot is mandatory")
    private Slot timeSlot;

    private String contactInfo;
    private String address;

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
