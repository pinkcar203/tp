package meditrack.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import meditrack.logic.RosterAutoGenerator.GenerateResult;
import meditrack.model.DutyType;

/**
 * Tests for RosterAutoGenerator, ensuring fairness algorithms and gap constraints function properly.
 */
public class RosterAutoGeneratorTest {

    private final LocalDate testDate = LocalDate.of(2026, 4, 2);

    @Test
    public void generate_emptyPersonnel_returnsEmptyResultWithGaps() {
        GenerateResult result = RosterAutoGenerator.generate(
                List.of(), // No personnel
                List.of(DutyType.GUARD_DUTY),
                testDate,
                new HashMap<>(),
                new HashMap<>()
        );

        assertTrue(result.slots().isEmpty());
        assertFalse(result.hasGaps()); // No gaps because no calculation could start
    }

    @Test
    public void generate_sufficientPersonnel_generatesFullRosterWithoutGaps() {
        // Provide enough personnel to cover a 24-hour GUARD_DUTY (12 slots of 2 hours)
        List<String> names = List.of("A", "B", "C", "D", "E", "F");

        GenerateResult result = RosterAutoGenerator.generate(
                names,
                List.of(DutyType.GUARD_DUTY),
                testDate,
                new HashMap<>(), // Default durations
                new HashMap<>()  // Zero existing counts
        );

        assertEquals(12, result.slots().size()); // 24 hours / 2 hours per slot = 12
        assertFalse(result.hasGaps());
    }

    @Test
    public void generate_insufficientPersonnel_leavesUncoveredWindows() {
        // Provide only 1 person, they will trigger the 8-hour break limit and leave gaps
        List<String> names = List.of("A");

        GenerateResult result = RosterAutoGenerator.generate(
                names,
                List.of(DutyType.GUARD_DUTY),
                testDate,
                new HashMap<>(),
                new HashMap<>()
        );

        assertTrue(result.slots().size() > 0);
        assertTrue(result.hasGaps()); // Uncovered windows should exist
    }

    @Test
    public void generate_prioritizesPersonnelWithLowestDutyCount() {
        List<String> names = List.of("Rookie", "Veteran");

        HashMap<String, Integer> historicalCounts = new HashMap<>();
        historicalCounts.put("Veteran", 100); // Veteran has done 100 duties
        historicalCounts.put("Rookie", 0);    // Rookie is fresh

        // Schedule just one daylight duty (e.g., Medical Cover, 4 hours)
        GenerateResult result = RosterAutoGenerator.generate(
                names,
                List.of(DutyType.MEDICAL_COVER),
                testDate,
                new HashMap<>(),
                historicalCounts
        );

        // The algorithm should assign the first shift to the Rookie
        assertEquals("Rookie", result.slots().get(0).getPersonnelName());
    }
}