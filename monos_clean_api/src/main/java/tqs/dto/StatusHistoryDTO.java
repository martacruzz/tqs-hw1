package tqs.dto;

import java.time.LocalDateTime;

import tqs.data.Status;

public class StatusHistoryDTO {
    private Status status;
    private LocalDateTime timestamp;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getTs() {
        return timestamp;
    }

    public void setTs(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
