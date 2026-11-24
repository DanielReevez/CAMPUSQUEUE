package com.campusqueue.app.model;

public enum TicketStatus {
    WAITING("Waiting"),
    CALLED("Called"),
    IN_SERVICE("In Service"),
    COMPLETED("Completed"),
    RETURN_REQUIRED("Return Required"),
    CANCELLED("Cancelled"),
    NO_SHOW("No-show");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(TicketStatus target) {
        if (target == null) return false;
        if (this == target) return true;

        switch (this) {
            case WAITING:
                return target == CALLED || target == CANCELLED;
            case CALLED:
                return target == IN_SERVICE || target == NO_SHOW || target == CANCELLED;
            case IN_SERVICE:
                return target == COMPLETED || target == RETURN_REQUIRED;
            case COMPLETED:
            case RETURN_REQUIRED:
            case CANCELLED:
            case NO_SHOW:
                return false; // Terminal states
            default:
                return false;
        }
    }

    public static TicketStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) return WAITING;
        for (TicketStatus ts : values()) {
            if (ts.name().equalsIgnoreCase(status.trim()) || ts.displayName.equalsIgnoreCase(status.trim())) {
                return ts;
            }
        }
        return WAITING;
    }
}
