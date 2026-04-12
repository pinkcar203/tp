package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import meditrack.model.DutySlot;
import meditrack.model.DutyType;
import meditrack.model.MediTrack;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Supply;

/**
 * Tests for the CsvExportUtility.
 * Verifies that the correct data is exported based on the user's Role-Based Access Control (RBAC) permissions.
 */
public class CsvExportUtilityTest {

    @TempDir
    public Path tempExportDir;

    private MediTrack testData;

    @BeforeEach
    public void setUp() {
        testData = new MediTrack();

        // Add dummy personnel (One FIT, one needing MEDICAL ATTENTION)
        testData.addPersonnelRecord(new Personnel("Alice", Status.FIT));
        testData.addPersonnelRecord(new Personnel("Bob", Status.CASUALTY));

        // Add dummy supply (One healthy, one LOW / EXPIRING)
        testData.addSupplyRecord(new Supply("Bandages", 100, LocalDate.now().plusDays(100)));
        testData.addSupplyRecord(new Supply("Morphine", 5, LocalDate.now().plusDays(5)));

        // Add dummy duty slot
        testData.addDutySlotRecord(new DutySlot(LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, DutyType.GUARD_DUTY, "Alice"));
    }

    @Test
    public void exportData_medicalOfficer_exportsOnlyPersonnel() throws IOException {
        Path exportedFile = CsvExportUtility.exportData(testData, Role.MEDICAL_OFFICER, tempExportDir);
        String fileContent = Files.readString(exportedFile);

        // Medical Officers should see Personnel, and their medical attention flags
        assertTrue(fileContent.contains("=== PERSONNEL ROSTER ==="));
        assertTrue(fileContent.contains("⚠ MEDICAL ATTENTION"));

        // Medical Officers should NOT see Duties or Supplies
        assertFalse(fileContent.contains("=== DUTY ROSTER ==="));
        assertFalse(fileContent.contains("=== SUPPLY INVENTORY ==="));
    }

    @Test
    public void exportData_logisticsOfficer_exportsOnlySupplies() throws IOException {
        Path exportedFile = CsvExportUtility.exportData(testData, Role.LOGISTICS_OFFICER, tempExportDir);
        String fileContent = Files.readString(exportedFile);

        // Logistics Officers should see Supplies, and their low/expiring flags
        assertTrue(fileContent.contains("=== SUPPLY INVENTORY ==="));
        assertTrue(fileContent.contains("⚠ LOW / EXPIRING"));

        // Logistics Officers should NOT see medical personnel or duties
        assertFalse(fileContent.contains("=== PERSONNEL ROSTER ==="));
        assertFalse(fileContent.contains("=== DUTY ROSTER ==="));
    }

    @Test
    public void exportData_fieldMedic_exportsPersonnelAndSupplies() throws IOException {
        Path exportedFile = CsvExportUtility.exportData(testData, Role.FIELD_MEDIC, tempExportDir);
        String fileContent = Files.readString(exportedFile);

        // Field Medics need access to both Personnel and Supplies
        assertTrue(fileContent.contains("=== PERSONNEL ROSTER ==="));
        assertTrue(fileContent.contains("=== SUPPLY INVENTORY ==="));

        // Field Medics do NOT manage the Duty Roster
        assertFalse(fileContent.contains("=== DUTY ROSTER ==="));
    }

    @Test
    public void exportData_platoonCommander_exportsPersonnelAndDuties() throws IOException {
        Path exportedFile = CsvExportUtility.exportData(testData, Role.PLATOON_COMMANDER, tempExportDir);
        String fileContent = Files.readString(exportedFile);

        // Platoon Commanders manage Personnel and Duties
        assertTrue(fileContent.contains("=== PERSONNEL ROSTER ==="));
        assertTrue(fileContent.contains("=== DUTY ROSTER ==="));

        // Platoon Commanders do NOT manage Supplies
        assertFalse(fileContent.contains("=== SUPPLY INVENTORY ==="));
    }
}