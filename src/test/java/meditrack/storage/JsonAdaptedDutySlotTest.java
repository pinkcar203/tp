package meditrack.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.DutySlot;
import meditrack.model.DutyType;


public class JsonAdaptedDutySlotTest {

    private static final String VALID_DATE = "2026-04-02";
    private static final String VALID_START = "08:00";
    private static final String VALID_END = "10:00";
    private static final String VALID_TYPE = "GUARD_DUTY";
    private static final String VALID_NAME = "John Doe";

    @Test
    public void toModelType_validDetails_returnsDutySlot() throws CommandException {
        JsonAdaptedDutySlot jsonSlot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, VALID_TYPE, VALID_NAME);
        DutySlot slot = jsonSlot.toModelType();

        assertEquals(LocalDate.parse(VALID_DATE), slot.getDate());
        assertEquals(LocalTime.parse(VALID_START), slot.getStartTime());
        assertEquals(DutyType.GUARD_DUTY, slot.getDutyType());
        assertEquals(VALID_NAME, slot.getPersonnelName());
    }

    @Test
    public void toModelType_missingDate_throwsCommandException() {
        JsonAdaptedDutySlot jsonSlot = new JsonAdaptedDutySlot(null, VALID_START, VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, jsonSlot::toModelType);
    }

    @Test
    public void toModelType_invalidTime_throwsCommandException() {
        JsonAdaptedDutySlot jsonSlot = new JsonAdaptedDutySlot(VALID_DATE, "25:00", VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, jsonSlot::toModelType);
    }

    @Test
    public void fromModelType_validSlot_success() {
        DutySlot slot = new DutySlot(LocalDate.of(2026, 1, 1), LocalTime.NOON, LocalTime.MIDNIGHT, DutyType.SENTRY, "Alice");
        assertDoesNotThrow(() -> JsonAdaptedDutySlot.fromModelType(slot));
    }

    //  NEGATIVE TESTS: one invalid field at a time 

    @Test
    void test_toModelType_blankDate_throwsCommandException() {
        // Arrange
        String blankDate = "";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(blankDate, VALID_START, VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_malformedDate_throwsCommandException() {
        // Arrange
        String malformedDate = "not-a-date";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(malformedDate, VALID_START, VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_nullStartTime_throwsCommandException() {
        // Arrange
        String nullStartTime = null;

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, nullStartTime, VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_blankStartTime_throwsCommandException() {
        // Arrange
        String blankStartTime = "   ";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, blankStartTime, VALID_END, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_nullEndTime_throwsCommandException() {
        // Arrange
        String nullEndTime = null;

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, nullEndTime, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_blankEndTime_throwsCommandException() {
        // Arrange
        String blankEndTime = "";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, blankEndTime, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_nullDutyType_throwsCommandException() {
        // Arrange
        String nullDutyType = null;

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, nullDutyType, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_blankDutyType_throwsCommandException() {
        // Arrange
        String blankDutyType = "   ";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, blankDutyType, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_invalidDutyType_throwsCommandException() {
        // Arrange
        String invalidDutyType = "INVALID_DUTY";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, invalidDutyType, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_nullPersonnelName_throwsCommandException() {
        // Arrange
        String nullName = null;

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, VALID_TYPE, nullName);
        assertThrows(CommandException.class, slot::toModelType);
    }

    @Test
    void test_toModelType_blankPersonnelName_throwsCommandException() {
        // Arrange
        String blankName = "   ";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, VALID_TYPE, blankName);
        assertThrows(CommandException.class, slot::toModelType);
    }

    // POSITIVE TESTS: all duty types 

    @Test
    void test_toModelType_patrolDutyType_returnsCorrectSlot() throws CommandException {
        // Arrange
        String patrolType = "PATROL";
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, patrolType, VALID_NAME);

        // Act
        DutySlot result = slot.toModelType();

        // Assert
        assertEquals(meditrack.model.DutyType.PATROL, result.getDutyType());
    }

    @Test
    void test_toModelType_sentryDutyType_returnsCorrectSlot() throws CommandException {
        // Arrange
        String sentryType = "SENTRY";
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, VALID_END, sentryType, VALID_NAME);

        // Act
        DutySlot result = slot.toModelType();

        // Assert
        assertEquals(meditrack.model.DutyType.SENTRY, result.getDutyType());
    }

    @Test
    void test_toModelType_medicalCoverDutyType_returnsCorrectSlot() throws CommandException {
        // Arrange
        String medicalCoverType = "MEDICAL_COVER";
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, "08:00", "12:00", medicalCoverType, VALID_NAME);

        // Act
        DutySlot result = slot.toModelType();

        // Assert
        assertEquals(meditrack.model.DutyType.MEDICAL_COVER, result.getDutyType());
    }

    @Test
    void test_toModelType_standbyDutyType_returnsCorrectSlot() throws CommandException {
        // Arrange
        String standbyType = "STANDBY";
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, "08:00", "12:00", standbyType, VALID_NAME);

        // Act
        DutySlot result = slot.toModelType();

        // Assert
        assertEquals(meditrack.model.DutyType.STANDBY, result.getDutyType());
    }

    @Test
    void test_toModelType_malformedEndTime_throwsCommandException() {
        // Arrange
        String malformedEndTime = "99:99";

        // Act & Assert
        JsonAdaptedDutySlot slot = new JsonAdaptedDutySlot(VALID_DATE, VALID_START, malformedEndTime, VALID_TYPE, VALID_NAME);
        assertThrows(CommandException.class, slot::toModelType);
    }
}
