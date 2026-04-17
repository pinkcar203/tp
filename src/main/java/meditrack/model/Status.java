package meditrack.model;

/**
 * Medical posting / MC state for roster rules and RBAC (who can set what).
 */
public enum Status {
    /** Platoon adds first; MO has not cleared them yet. */
    PENDING,
    /** Medically cleared for full duty and deployment. */
    FIT,
    /** Requires monitoring; assigned light physical duties. */
    LIGHT_DUTY,
    /** Medical Certificate; excused from duty, requires monitoring. */
    MC,
    /** Marked unwell in the field by a Field Medic, requires MO assessment. */
    CASUALTY;

    /**
     * Determines if the current status qualifies the personnel for full deployment.
     * Only FIT personnel are eligible to appear in the active duty roster.
     *
     * @return {@code true} if the status is FIT, {@code false} otherwise.
     */
    public boolean isDeployable() {
        return this == FIT;
    }

    /**
     * Parses a string representation into the corresponding Status enum.
     * Handles case-insensitivity and converts spaces to underscores for flexibility.
     *
     * @param value The raw string from user input or JSON storage.
     * @return The matching Status enum.
     * @throws IllegalArgumentException If the value is null or does not match any valid Status.
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

    /**
     * Returns status, replacing underscores with spaces.
     *
     * @return The formatted status string.
     */
    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}
