package meditrack.model;

/**
 * Represents the specific types of duties that can be assigned to personnel.
 * Used by the RosterAutoGenerator to determine shift lengths and constraints.
 */
public enum DutyType {
    GUARD_DUTY("Guard Duty"),
    MEDICAL_COVER("Medical Cover"),
    PATROL("Patrol"),
    STANDBY("Standby"),
    SENTRY("Sentry");

    private final String displayName;

    /**
     * Constructs a DutyType with a human-readable display name.
     *
     * @param displayName The formatted string to be used in UI dropdowns and labels.
     */
    DutyType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the UI-friendly display name of the duty.
     *
     * @return The formatted display name string.
     */
    @Override
    public String toString() {
        return displayName;
    }
}