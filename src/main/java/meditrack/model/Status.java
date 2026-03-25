package meditrack.model;

/**
 * Represents the medical readiness status of a Personnel member.
 *
 * <p>Valid values match the team's data schema used in
 * {@link meditrack.storage.JsonAdaptedPersonnel}.
 */
public enum Status {
    PENDING,        // Newly added by PC, awaiting MO assessment
    FIT,            // Medically cleared for duty
    LIGHT_DUTY,   // Requires MO monitoring
    MC,          // Requires MO monitoring
    CASUALTY;        // Marked unwell outfield by Field Medic, requires MO assessment

    /**
     * Returns true if this status qualifies the person for full deployment.
     * Only {@code FIT} personnel appear in the duty roster.
     */
    public boolean isDeployable() {
        return this == FIT;
    }

    /**
     * Parses a string (case-insensitive, underscores or spaces) to a Status value.
     *
     * @param value raw string from user input or JSON storage
     * @return the matching Status
     * @throws IllegalArgumentException if {@code value} does not match any Status
     */
    public static Status fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status value must not be null.");
        }
        switch (value.trim().toUpperCase().replace(' ', '_')) {
            case "FIT":
                return FIT;
            case "LIGHT_DUTY":
                return LIGHT_DUTY;
            case "MC":
                return MC;
            case "PENDING":
                return PENDING;
            case "CASUALTY":
                return CASUALTY;
            default:
                throw new IllegalArgumentException(
                        "Invalid status: \"" + value + "\". Valid values: FIT, LIGHT_DUTY, MC, CASUALTY, PENDING");
        }
    }

    /** Name with underscores replaced by spaces. */
    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}