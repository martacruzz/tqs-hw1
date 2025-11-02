package tqs.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import tqs.data.*;

public class BookingResponseDTO {

    private String token;
    private String municipality;
    private String description;
    private LocalDate date;
    private Slot slot;
    private Status status;
    private String contactInfo;
    private String address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<StatusHistoryDTO> history;

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

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
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

    public List<StatusHistoryDTO> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistoryDTO> history) {
        this.history = history;
    }
}
