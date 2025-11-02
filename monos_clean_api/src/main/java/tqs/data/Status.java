package tqs.data;

public enum Status {
    RECEIVED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransition(Status newStatus) {
        switch (this) {
            case RECEIVED:
                return newStatus == ASSIGNED || newStatus == CANCELLED;
            case ASSIGNED:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case IN_PROGRESS:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false; // terminal cases
            default:
                return false;
        }
    }
}
