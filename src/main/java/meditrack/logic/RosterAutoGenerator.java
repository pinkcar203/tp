package meditrack.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import meditrack.model.DutySlot;
import meditrack.model.DutyType;

/**
 * Generates a duty roster automatically from a list of FIT personnel.
 */
public class RosterAutoGenerator {

    public static final int DAY_MINUTES = 1440;
    private static final int MIN_BREAK_MINUTES = 480;   // 8 hours
    private static final int DAYTIME_START_MINUTES = 480;   // 08:00
    private static final int DAYTIME_END_MINUTES = 1200;    // 20:00

    /** Default slot duration in minutes for each duty type. */
    public static final Map<DutyType, Integer> DEFAULT_DURATIONS = Map.of(
            DutyType.GUARD_DUTY, 120,
            DutyType.SENTRY, 120,
            DutyType.PATROL, 90,
            DutyType.MEDICAL_COVER, 240,
            DutyType.STANDBY, 240);

    /** Duty types that cover the full 24-hour day window. */
    private static final Set<DutyType> ROUND_CLOCK =
            EnumSet.of(DutyType.GUARD_DUTY, DutyType.PATROL);

    /**
     * Result of a single auto-generation run.
     *
     * @param slots            all successfully generated duty slots for the date
     * @param uncoveredWindows windows no one could cover
     */
    public record GenerateResult(List<DutySlot> slots, List<String> uncoveredWindows) {
        /** Returns true if any time window went uncovered. */
        public boolean hasGaps() {
            return !uncoveredWindows.isEmpty();
        }
    }

    private RosterAutoGenerator() {}

    /**
     * Generates duty slots for a specific calendar date.
     *
     * @param personnelNames     names of all FIT personnel available for assignment
     * @param selectedTypes      duty types to schedule
     * @param date               the calendar date these slots fall on
     * @param slotDurations      slot duration in minutes per duty type
     * @param existingDutyCounts cumulative duty count per person name (for fair rotation)
     * @return result containing generated slots and any uncovered windows
     */
    public static GenerateResult generate(
            List<String> personnelNames,
            List<DutyType> selectedTypes,
            LocalDate date,
            Map<DutyType, Integer> slotDurations,
            Map<String, Integer> existingDutyCounts) {

        if (personnelNames.isEmpty() || selectedTypes.isEmpty()) {
            return new GenerateResult(List.of(), List.of());
        }

        // Mutable duty counts for fair rotation within this run
        Map<String, Integer> dutyCounts = new HashMap<>(existingDutyCounts);
        for (String name : personnelNames) {
            dutyCounts.putIfAbsent(name, 0);
        }

        // Committed intervals per person: [startMin, endMin] pairs
        Map<String, List<int[]>> personIntervals = new HashMap<>();
        for (String name : personnelNames) {
            personIntervals.put(name, new ArrayList<>());
        }

        List<DutySlot> result = new ArrayList<>();
        List<String> uncoveredWindows = new ArrayList<>();

        for (DutyType type : selectedTypes) {
            boolean isRoundClock = ROUND_CLOCK.contains(type);
            int windowStart = isRoundClock ? 0 : DAYTIME_START_MINUTES;
            int windowEnd = isRoundClock ? DAY_MINUTES : DAYTIME_END_MINUTES;
            int slotMinutes = slotDurations.getOrDefault(type,
                    DEFAULT_DURATIONS.getOrDefault(type, 120));

            int current = windowStart;
            while (current < windowEnd) {
                int slotEnd = Math.min(current + slotMinutes, windowEnd);
                String assigned = pickBestCandidate(personnelNames, dutyCounts, personIntervals, current, slotEnd);

                if (assigned != null) {
                    result.add(new DutySlot(date, minutesToTime(current), minutesToTime(slotEnd), type, assigned));
                    personIntervals.get(assigned).add(new int[]{current, slotEnd});
                    dutyCounts.merge(assigned, 1, Integer::sum);
                } else {
                    uncoveredWindows.add(type + " " + minutesToTime(current) + "–" + minutesToTime(slotEnd));
                }

                current = slotEnd;
            }
        }

        result.sort(Comparator.comparing(DutySlot::getStartTime).thenComparing(DutySlot::getDutyType));
        return new GenerateResult(result, uncoveredWindows);
    }

    /**
     * Selects the person with the lowest duty count who satisfies both the
     * no-overlap and 8-hour break constraints for the proposed time window.
     *
     * @return the chosen person's name, or null if nobody is available
     */
    private static String pickBestCandidate(
            List<String> names,
            Map<String, Integer> dutyCounts,
            Map<String, List<int[]>> personIntervals,
            int slotStart,
            int slotEnd) {

        String best = null;
        int bestCount = Integer.MAX_VALUE;

        for (String name : names) {
            int count = dutyCounts.getOrDefault(name, 0);
            if (count >= bestCount) {
                continue;
            }
            List<int[]> intervals = personIntervals.get(name);
            if (hasNoOverlap(intervals, slotStart, slotEnd)
                    && hasEightHourBreak(intervals, slotStart, slotEnd)) {
                best = name;
                bestCount = count;
            }
        }
        return best;
    }

    // Constraint helpers

    /** Returns true if newStart-newEnd does not overlap any committed interval. */
    private static boolean hasNoOverlap(List<int[]> intervals, int newStart, int newEnd) {
        for (int[] iv : intervals) {
            if (newStart < iv[1] && newEnd > iv[0]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if, after adding newStart-newEnd, the person still has at
     * least one 8-hour contiguous free block within the 24-hour day.
     */
    private static boolean hasEightHourBreak(List<int[]> existing, int newStart, int newEnd) {
        List<int[]> all = new ArrayList<>(existing);
        all.add(new int[]{newStart, newEnd});
        all.sort((a, b) -> Integer.compare(a[0], b[0]));

        if (all.get(0)[0] >= MIN_BREAK_MINUTES) {
            return true;
        }
        for (int i = 0; i < all.size() - 1; i++) {
            int gap = all.get(i + 1)[0] - all.get(i)[1];
            if (gap >= MIN_BREAK_MINUTES) {
                return true;
            }
        }
        int afterLast = DAY_MINUTES - all.get(all.size() - 1)[1];
        return afterLast >= MIN_BREAK_MINUTES;
    }

    // Time utilities

    /**
     * Converts minutes-from-midnight to LocalTime.
     * 1440 (24:00) maps to LocalTime.MIDNIGHT (00:00).
     */
    private static LocalTime minutesToTime(int minutes) {
        int clamped = minutes % DAY_MINUTES;
        return LocalTime.of(clamped / 60, clamped % 60);
    }
}
