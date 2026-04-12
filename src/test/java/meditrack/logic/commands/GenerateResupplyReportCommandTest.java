package meditrack.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import meditrack.model.ModelManager;
import meditrack.model.Supply;

/*
 * Equivalence Partitions:
 *
 * ReportEntry 
 * Parameter: supply
 *   Valid:   any non-null Supply
 *
 * Parameter: reason
 *   Valid:   "Low Stock", "Expiring Soon", "Both"
 *
 * collectFlaggedEntries(model, quantityThreshold, daysThreshold) [3 params => pairwise] 
 * Parameter: quantityThreshold
 *   Valid:   positive integer, 0
 *
 * Parameter: daysThreshold
 *   Valid:   positive integer, 0
 *
 * Parameter: model supply state
 *   Valid:   empty, all adequate, some low, some expiring, some both
 */

class GenerateResupplyReportCommandTest {

    @Test
    void test_reportEntry_getSupply_returnsCorrectSupply() {
        // Arrange
        Supply supply = new Supply("Bandages", 10, LocalDate.now().plusDays(5));
        GenerateResupplyReportCommand.ReportEntry entry =
                new GenerateResupplyReportCommand.ReportEntry(supply, "Low Stock");

        // Act & Assert
        assertEquals(supply, entry.getSupply());
    }

    @Test
    void test_reportEntry_getReason_returnsCorrectReason() {
        // Arrange
        Supply supply = new Supply("Morphine", 3, LocalDate.now().plusDays(2));
        String expectedReason = "Both";
        GenerateResupplyReportCommand.ReportEntry entry =
                new GenerateResupplyReportCommand.ReportEntry(supply, expectedReason);

        // Act & Assert
        assertEquals(expectedReason, entry.getReason());
    }

    @Test
    void test_collectFlaggedEntries_emptyInventory_returnsEmptyList() {
        // Arrange
        ModelManager emptyModel = new ModelManager();
        int validQuantityThreshold = 50;
        int validDaysThreshold = 30;

        // Act
        List<GenerateResupplyReportCommand.ReportEntry> entries =
                GenerateResupplyReportCommand.collectFlaggedEntries(
                        emptyModel, validQuantityThreshold, validDaysThreshold);

        // Assert
        assertTrue(entries.isEmpty());
    }

    @Test
    void test_collectFlaggedEntries_allAdequate_returnsEmptyList() {
        // Arrange
        ModelManager model = new ModelManager();
        model.addSupply(new Supply("Healthy", 200, LocalDate.now().plusDays(365)));

        // Act
        List<GenerateResupplyReportCommand.ReportEntry> entries =
                GenerateResupplyReportCommand.collectFlaggedEntries(model, 50, 30);

        // Assert
        assertTrue(entries.isEmpty());
    }

    @Test
    void test_collectFlaggedEntries_duplicateSupplyName_onlyFlaggedOnce() {
        // Arrange
        ModelManager model = new ModelManager();
        model.addSupply(new Supply("Bandages", 5, LocalDate.now().plusDays(365)));
        model.addSupply(new Supply("Bandages", 3, LocalDate.now().plusDays(365)));

        // Act
        List<GenerateResupplyReportCommand.ReportEntry> entries =
                GenerateResupplyReportCommand.collectFlaggedEntries(model, 50, 30);

        // Assert: "Bandages" should appear only once despite two batches
        long bandageEntries = entries.stream()
                .filter(e -> e.getSupply().getName().equalsIgnoreCase("Bandages"))
                .count();
        assertEquals(1, bandageEntries);
    }
}
