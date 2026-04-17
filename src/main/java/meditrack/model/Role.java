package meditrack.model;

/**
 * Represents the operational roles available within the MediTrack system.
 * Used to enforce strict Role-Based Access Control (RBAC) across application commands.
 */
public enum Role {
    FIELD_MEDIC("Field Medic"),
    MEDICAL_OFFICER("Medical Officer"),
    PLATOON_COMMANDER("Platoon Commander"),
    LOGISTICS_OFFICER("Logistics Officer");

    private final String displayName;

    /**
     * Constructs a Role with a specific, human-readable display name.
     *
     * @param displayName The formatted string used in UI dropdowns and labels.
     */
    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Overrides the default uppercase enum string formatting.
     *
     * @return The formatted display name string.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
