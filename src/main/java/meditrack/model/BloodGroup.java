package meditrack.model;

/**
 * Represents the ABO and Rh classification of a personnel member's blood group.
 * Includes an UNKNOWN fallback for incomplete medical records.
 */
public enum BloodGroup {
    A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG, UNKNOWN;

    /**
     * Returns the standardized human-readable label for the blood group (e.g., "A+").
     *
     * @return The formatted blood group string.
     */
    public String display() {
        return switch (this) {
            case A_POS -> "A+";
            case A_NEG -> "A-";
            case B_POS -> "B+";
            case B_NEG -> "B-";
            case AB_POS -> "AB+";
            case AB_NEG -> "AB-";
            case O_POS -> "O+";
            case O_NEG -> "O-";
            case UNKNOWN -> "UNKNOWN";
        };
    }

    /**
     * Parses a string representation into the corresponding BloodGroup enum.
     * @param s The string to parse.
     * @return The matching BloodGroup, or UNKNOWN if the input is null, blank, or unrecognized.
     */
    public static BloodGroup fromString(String s) {
        if (s == null || s.isBlank()) {
            return UNKNOWN;
        }

        String trimmedInput = s.trim();
        for (BloodGroup bg : values()) {
            if (bg.display().equalsIgnoreCase(trimmedInput) || bg.name().equalsIgnoreCase(trimmedInput)) {
                return bg;
            }
        }
        return UNKNOWN;
    }
}
