package meditrack.model;

/**
 * Represents the roles available in the MediTrack system.
 */
public enum Role {
    FIELD_MEDIC("Field Medic"),
    MEDICAL_OFFICER("Medical Officer"),
    PLATOON_COMMANDER("Platoon Commander"),
    LOGISTICS_OFFICER("Logistics Officer");

    private final String displayName;

    /**
     * Constructs a Role with a specific display name for the UI.
     * @param displayName The formatted string to show in dropdowns and labels.
     */
    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Overrides the default enum string (e.g., "MEDICAL_OFFICER")
     * to return the clean, readable display name for JavaFX components.
     */
    @Override
    public String toString() {
        return displayName;
    }
}