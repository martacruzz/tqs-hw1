package tqs.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import tqs.data.Status;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void whenReceivedStatus_thenCanTransitionToAssignedOrCancelled() {
        Status status = Status.RECEIVED;

        assertTrue(status.canTransition(Status.ASSIGNED));
        assertTrue(status.canTransition(Status.CANCELLED));
        assertFalse(status.canTransition(Status.IN_PROGRESS));
        assertFalse(status.canTransition(Status.COMPLETED));
        assertFalse(status.canTransition(Status.RECEIVED));
    }

    @Test
    void whenAssignedStatus_thenCanTransitionToInProgressOrCancelled() {
        Status status = Status.ASSIGNED;

        assertTrue(status.canTransition(Status.IN_PROGRESS));
        assertTrue(status.canTransition(Status.CANCELLED));
        assertFalse(status.canTransition(Status.ASSIGNED));
        assertFalse(status.canTransition(Status.COMPLETED));
        assertFalse(status.canTransition(Status.RECEIVED));
    }

    @Test
    void whenInProgressStatus_thenCanTransitionToCompletedOrCancelled() {
        Status status = Status.IN_PROGRESS;

        assertTrue(status.canTransition(Status.COMPLETED));
        assertTrue(status.canTransition(Status.CANCELLED));
        assertFalse(status.canTransition(Status.IN_PROGRESS));
        assertFalse(status.canTransition(Status.ASSIGNED));
        assertFalse(status.canTransition(Status.RECEIVED));
    }

    @Test
    void whenCompletedStatus_thenCannotTransitionToAnyStatus() {
        Status status = Status.COMPLETED;

        assertFalse(status.canTransition(Status.RECEIVED));
        assertFalse(status.canTransition(Status.ASSIGNED));
        assertFalse(status.canTransition(Status.IN_PROGRESS));
        assertFalse(status.canTransition(Status.COMPLETED));
        assertFalse(status.canTransition(Status.CANCELLED));
    }

    @Test
    void whenCancelledStatus_thenCannotTransitionToAnyStatus() {
        Status status = Status.CANCELLED;

        assertFalse(status.canTransition(Status.RECEIVED));
        assertFalse(status.canTransition(Status.ASSIGNED));
        assertFalse(status.canTransition(Status.IN_PROGRESS));
        assertFalse(status.canTransition(Status.COMPLETED));
        assertFalse(status.canTransition(Status.CANCELLED));
    }

    // run with parameters -- goes through with all status
    @ParameterizedTest
    @EnumSource(Status.class)
    void testTerminalStates(Status status) {
        if (status == Status.COMPLETED || status == Status.CANCELLED) {
            // terminal states should not transition to any other state
            for (Status newStatus : Status.values()) {
                assertFalse(status.canTransition(newStatus),
                        String.format("Terminal state %s should not transition to %s", status, newStatus));
            }
        }
    }
}