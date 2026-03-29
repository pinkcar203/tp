package meditrack.model;

/**
 * Blood group classification.
 * UNKNOWN is used when the blood group has not been recorded.
 */
public enum BloodGroup {
    A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG, UNKNOWN;

    /** Returns the human-readable label */
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
     * Parses from a display label.
     *
     * @param s the string to parse
     * @return the matching BloodGroup
     */
    public static BloodGroup fromString(String s) {
        if (s == null || s.isBlank()) {
            return UNKNOWN;
        }
        for (BloodGroup bg : values()) {
            if (bg.display().equalsIgnoreCase(s.trim()) || bg.name().equalsIgnoreCase(s.trim())) {
                return bg;
            }
        }
        return UNKNOWN;
    }
}
