package meditrack.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single scheduled duty assignment in the unit's roster.
 */
public class DutySlot {

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean crossesMidnight;
    private final DutyType dutyType;
    private final String personnelName;

    /**
     * Constructs a DutySlot for a specific personnel member.
     *
     * @param date          The calendar date the duty starts on. Must not be null.
     * @param startTime     The start of the duty window. Must not be null.
     * @param endTime       The end of the duty window. If earlier than startTime, it implies crossing midnight. Must not be null.
     * @param dutyType      The specific type of duty assigned. Must not be null.
     * @param personnelName The name of the assigned personnel. Must not be null or blank.
     */
    public DutySlot(LocalDate date, LocalTime startTime, LocalTime endTime,
                    DutyType dutyType, String personnelName) {
        Objects.requireNonNull(date, "date must not be null.");
        Objects.requireNonNull(startTime, "startTime must not be null.");
        Objects.requireNonNull(endTime, "endTime must not be null.");
        Objects.requireNonNull(dutyType, "dutyType must not be null.");
        if (personnelName == null || personnelName.isBlank()) {
            throw new IllegalArgumentException("Personnel name must not be blank.");
        }
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.crossesMidnight = endTime.isBefore(startTime);
        this.dutyType = dutyType;
        this.personnelName = personnelName.trim();
    }

    /** Retrieves the calendar date of the duty assignment. */
    public LocalDate getDate() {
        return date;
    }

    /** Retrieves the starting time of the duty window. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** Retrieves the ending time of the duty window. */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Checks if the duty slot extends past midnight into the next calendar day.
     *
     * @return {@code true} if endTime is earlier than startTime.
     */
    public boolean crossesMidnight() {
        return crossesMidnight;
    }

    /** Retrieves the assigned duty type. */
    public DutyType getDutyType() {
        return dutyType;
    }

    /** Retrieves the name of the assigned personnel. */
    public String getPersonnelName() {
        return personnelName;
    }

    /**
     * Formats the duty time range for UI display.
     * @return A formatted time range string.
     */
    public String getTimeSlotDisplay() {
        String base = startTime.format(DISPLAY_FMT) + " - " + endTime.format(DISPLAY_FMT);
        return crossesMidnight ? base + " (+1)" : base;
    }

    /**
     * Returns a string representation of the duty slot for debugging.
     */
    @Override
    public String toString() {
        return date + " | " + getTimeSlotDisplay() + " | " + dutyType + " | " + personnelName;
    }

    /**
     * Evaluates equality based on exact matches of all fields (case-insensitive for name).
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DutySlot other)) {
            return false;
        }
        return date.equals(other.date)
                && startTime.equals(other.startTime)
                && endTime.equals(other.endTime)
                && dutyType == other.dutyType
                && personnelName.equalsIgnoreCase(other.personnelName);
    }

    /**
     * Generates a hash code for the duty slot.
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime, dutyType, personnelName.toLowerCase());
    }
}