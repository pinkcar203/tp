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
 * An algorithmic utility class that automatically generates fair duty rosters.
 * Enforces constraints such as prohibiting overlapping shifts and ensuring
 * personnel receive adequate rest blocks (minimum 8 continuous hours).
 */
public class RosterAutoGenerator {

    public static final int DAY_MINUTES = 1440;
    private static final int MIN_BREAK_MINUTES = 480;
    private static final int DAYTIME_START_MINUTES = 480;
    private static final int DAYTIME_END_MINUTES = 1200;

    /** Fallback length if UI did not override (minutes). */
    public static final Map<DutyType, Integer> DEFAULT_DURATIONS = Map.of(
            DutyType.GUARD_DUTY, 120,
            DutyType.SENTRY, 120,
            DutyType.PATROL, 90,
            DutyType.MEDICAL_COVER, 240,
            DutyType.STANDBY, 240);

    /** These types are tiled across the full day, not just 08:00–20:00. */
    private static final Set<DutyType> ROUND_CLOCK =
            EnumSet.of(DutyType.GUARD_DUTY, DutyType.PATROL);

    /**
     * @param slots            what we managed to place
     * @param uncoveredWindows human-readable gaps when nobody was free / constraints blocked
     */
    public record GenerateResult(List<DutySlot> slots, List<String> uncoveredWindows) {
        /** Quick check for the UI warning banner. */
        public boolean hasGaps() {
            return !uncoveredWindows.isEmpty();
        }
    }

    private RosterAutoGenerator() {}

    /**
     * Generates an optimized duty roster for a specific calendar date.
     * Prioritizes candidates with the lowest cumulative duty count to ensure fair rotation.
     *
     * @param personnelNames     A list of eligible (FIT) personnel names.
     * @param selectedTypes      The list of duties that need to be scheduled for the day.
     * @param date               The calendar date being scheduled.
     * @param slotDurations      Custom duration overrides per duty type, in minutes.
     * @param existingDutyCounts Historical duty count map for fair rotation weighting.
     * @return A GenerateResult containing the populated slots and any uncovered gaps.
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

        Map<String, Integer> dutyCounts = new HashMap<>(existingDutyCounts);
        personnelNames.forEach(name -> dutyCounts.putIfAbsent(name, 0));

        Map<String, List<int[]>> personIntervals = new HashMap<>();
        personnelNames.forEach(name -> personIntervals.put(name, new ArrayList<>()));

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
     * Selects the candidate with the lowest duty count who meets all timing constraints.
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

    private static boolean hasNoOverlap(List<int[]> intervals, int newStart, int newEnd) {
        for (int[] iv : intervals) {
            if (newStart < iv[1] && newEnd > iv[0]) {
                return false;
            }
        }
        return true;
    }

    /** After adding [newStart,newEnd), there must still exist an 8h pocket somewhere in the 24h line. */
    private static boolean hasEightHourBreak(List<int[]> existing, int newStart, int newEnd) {
        List<int[]> all = new ArrayList<>(existing);
        all.add(new int[]{newStart, newEnd});
        all.sort(Comparator.comparingInt(a -> a[0]));

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

    private static LocalTime minutesToTime(int minutes) {
        int clamped = minutes % DAY_MINUTES;
        return LocalTime.of(clamped / 60, clamped % 60);
    }
}
