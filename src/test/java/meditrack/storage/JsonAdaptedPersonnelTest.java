package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.Personnel;
import meditrack.model.Status;


public class JsonAdaptedPersonnelTest {

    private static final String VALID_NAME = "Alice";
    private static final String VALID_STATUS = "FIT";
    private static final String VALID_BLOOD_GROUP = "A_POS";
    private static final String VALID_ALLERGIES = "Peanuts";
    private static final String VALID_LAST_MODIFIED = "2026-01-01T10:15:30";
    private static final String VALID_EXPIRY = "2026-05-01";

    @Test
    public void toModelType_validPersonnelDetails_returnsPersonnel() throws CommandException {
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(VALID_NAME, VALID_STATUS,
                VALID_BLOOD_GROUP, VALID_ALLERGIES, VALID_LAST_MODIFIED, VALID_EXPIRY);

        Personnel modelPersonnel = personnel.toModelType();

        assertEquals(VALID_NAME, modelPersonnel.getName());
        assertEquals(Status.FIT, modelPersonnel.getStatus());
        assertEquals("Peanuts", modelPersonnel.getAllergies());
    }

    @Test
    public void toModelType_nullName_throwsCommandException() {
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(null, VALID_STATUS,
                VALID_BLOOD_GROUP, VALID_ALLERGIES, VALID_LAST_MODIFIED, VALID_EXPIRY);

        assertThrows(CommandException.class, personnel::toModelType);
    }

    @Test
    public void toModelType_blankName_throwsCommandException() {
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel("  ", VALID_STATUS,
                VALID_BLOOD_GROUP, VALID_ALLERGIES, VALID_LAST_MODIFIED, VALID_EXPIRY);

        assertThrows(CommandException.class, personnel::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsCommandException() {
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(VALID_NAME, "SUPER_FIT",
                VALID_BLOOD_GROUP, VALID_ALLERGIES, VALID_LAST_MODIFIED, VALID_EXPIRY);

        assertThrows(CommandException.class, personnel::toModelType);
    }

    @Test
    public void fromModelType_validPersonnel_success() {
        Personnel p = new Personnel("Bob", Status.MC);
        assertDoesNotThrow(() -> JsonAdaptedPersonnel.fromModelType(p));
    }

    @Test
    void test_toModelType_nullStatus_throwsCommandException() {
        // Arrange
        String nullStatus = null;
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, nullStatus, null, null, null, null);

        // Act & Assert
        assertThrows(CommandException.class, personnel::toModelType);
    }

    @Test
    void test_toModelType_blankStatus_throwsCommandException() {
        // Arrange
        String blankStatus = "   ";
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, blankStatus, null, null, null, null);

        // Act & Assert
        assertThrows(CommandException.class, personnel::toModelType);
    }

    @Test
    void test_toModelType_nullBloodGroupAndAllergies_returnsPersonnelWithDefaults() throws CommandException {
        // Arrange
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, VALID_STATUS, null, null, null, null);

        // Act
        Personnel result = personnel.toModelType();

        // Assert
        assertEquals(VALID_NAME, result.getName());
        assertEquals(Status.FIT, result.getStatus());
        assertNull(result.getBloodGroup());
        assertEquals("", result.getAllergies());
    }

    @Test
    void test_toModelType_nullLastModified_doesNotSetTimestamp() throws CommandException {
        // Arrange
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, VALID_STATUS, null, null, null, null);

        // Act
        Personnel result = personnel.toModelType();

        // Assert
        assertNotNull(result.getLastModified());
    }

    @Test
    void test_toModelType_blankLastModified_doesNotSetTimestamp() throws CommandException {
        // Arrange
        String blankLastModified = "   ";
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, VALID_STATUS, null, null, blankLastModified, null);

        // Act
        Personnel result = personnel.toModelType();

        // Assert
        assertNotNull(result.getLastModified());
    }

    @Test
    void test_toModelType_blankExpiryDate_doesNotSetExpiry() throws CommandException {
        // Arrange
        String blankExpiry = "   ";
        JsonAdaptedPersonnel personnel = new JsonAdaptedPersonnel(
                VALID_NAME, VALID_STATUS, null, null, null, blankExpiry);

        // Act
        Personnel result = personnel.toModelType();

        // Assert
        assertNull(result.getStatusExpiryDate());
    }

    @Test
    void test_toModelType_allStatusTypes_parseCorrectly() throws CommandException {
        // Test each status type
        for (String statusStr : new String[]{"FIT", "MC", "LIGHT_DUTY", "CASUALTY", "PENDING"}) {
            JsonAdaptedPersonnel p = new JsonAdaptedPersonnel(VALID_NAME, statusStr, null, null, null, null);
            Personnel result = p.toModelType();
            assertEquals(Status.fromString(statusStr), result.getStatus());
        }
    }

    @Test
    void test_fromModelType_personnelWithAllFields_createsCorrectAdapter() {
        // Arrange
        Personnel p = new Personnel("Bob", Status.MC, BloodGroup.O_POS, "Latex");
        p.setStatusExpiryDate(java.time.LocalDate.of(2026, 8, 1));

        // Act
        JsonAdaptedPersonnel adapted = JsonAdaptedPersonnel.fromModelType(p);

        // Assert
        assertEquals("Bob", adapted.name);
        assertEquals("MC", adapted.status);
        assertEquals("O_POS", adapted.bloodGroup);
        assertEquals("Latex", adapted.allergies);
        assertNotNull(adapted.lastModified);
        assertEquals("2026-08-01", adapted.statusExpiryDate);
    }

    @Test
    void test_fromModelType_personnelWithNullBloodGroup_bloodGroupIsNull() {
        // Arrange
        Personnel p = new Personnel("Eve", Status.FIT);

        // Act
        JsonAdaptedPersonnel adapted = JsonAdaptedPersonnel.fromModelType(p);

        // Assert
        assertNull(adapted.bloodGroup);
        assertNull(adapted.statusExpiryDate);
    }
}
