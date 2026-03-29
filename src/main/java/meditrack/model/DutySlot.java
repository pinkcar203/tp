package meditrack.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents one scheduled duty slot in the duty roster.
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
     * Constructs a duty slot.
     *
     * @param date          calendar date the duty falls on
     * @param startTime     start of the duty window
     * @param endTime       end of the duty window; if earlier than startTime the slot crosses midnight
     * @param dutyType      type of duty
     * @param personnelName name of the assigned person
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

    /** Calendar date the duty falls on. */
    public LocalDate getDate() {
        return date;
    }

    /** Start of the duty window. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * End of the duty window (may be on the following day see crossesMidnight).
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Returns true when endTime is earlier than startTime,
     * meaning the slot extends past midnight into the next calendar day.
     */
    public boolean crossesMidnight() {
        return crossesMidnight;
    }

    /** Type of duty assigned. */
    public DutyType getDutyType() {
        return dutyType;
    }

    /** Name of the personnel member assigned. */
    public String getPersonnelName() {
        return personnelName;
    }

    /**
     * Formatted time range for display, e.g. "08:00 - 10:00" or
     * "22:00 - 02:00 (+1)" for overnight duties.
     */
    public String getTimeSlotDisplay() {
        String base = startTime.format(DISPLAY_FMT) + " - " + endTime.format(DISPLAY_FMT);
        return crossesMidnight ? base + " (+1)" : base;
    }

    @Override
    public String toString() {
        return date + " | " + getTimeSlotDisplay() + " | " + dutyType + " | " + personnelName;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime, dutyType, personnelName.toLowerCase());
    }
}
