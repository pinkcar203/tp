package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;



public class DutySlotTest {

    private final LocalDate testDate = LocalDate.of(2026, 4, 2);

    @Test
    public void constructor_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DutySlot(testDate, LocalTime.NOON, LocalTime.MIDNIGHT, DutyType.GUARD_DUTY, null));
    }

    @Test
    public void crossesMidnight_standardShift_returnsFalse() {
        DutySlot slot = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(12, 0), DutyType.PATROL, "Alice");
        assertFalse(slot.crossesMidnight());
        assertEquals("08:00 - 12:00", slot.getTimeSlotDisplay());
    }

    @Test
    public void crossesMidnight_overnightShift_returnsTrue() {
        DutySlot slot = new DutySlot(testDate, LocalTime.of(22, 0), LocalTime.of(2, 0), DutyType.SENTRY, "Bob");
        assertTrue(slot.crossesMidnight());
        assertEquals("22:00 - 02:00 (+1)", slot.getTimeSlotDisplay());
    }

    @Test
    public void equals_identicalFieldsDifferentCaseName_returnsTrue() {
        DutySlot s1 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0), DutyType.STANDBY, "john doe");
        DutySlot s2 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0), DutyType.STANDBY, "JOHN DOE");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void equals_differentTime_returnsFalse() {
        DutySlot s1 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0), DutyType.STANDBY, "Alice");
        DutySlot s2 = new DutySlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 0), DutyType.STANDBY, "Alice");

        assertNotEquals(s1, s2);
    }

    @Test
    void test_constructor_nullDate_throwsNullPointerException() {
        // Arrange
        LocalDate nullDate = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                new DutySlot(nullDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                        DutyType.GUARD_DUTY, "Alice"));
    }

    @Test
    void test_constructor_nullStartTime_throwsNullPointerException() {
        // Arrange
        LocalTime nullStartTime = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                new DutySlot(testDate, nullStartTime, LocalTime.of(10, 0),
                        DutyType.GUARD_DUTY, "Alice"));
    }

    @Test
    void test_constructor_nullEndTime_throwsNullPointerException() {
        // Arrange
        LocalTime nullEndTime = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                new DutySlot(testDate, LocalTime.of(8, 0), nullEndTime,
                        DutyType.GUARD_DUTY, "Alice"));
    }

    @Test
    void test_constructor_nullDutyType_throwsNullPointerException() {
        // Arrange
        DutyType nullDutyType = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                        nullDutyType, "Alice"));
    }

    @Test
    void test_constructor_blankPersonnelName_throwsIllegalArgumentException() {
        // Arrange
        String blankName = "   ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                        DutyType.GUARD_DUTY, blankName));
    }

    @Test
    void test_constructor_nameWithSpaces_trimmedCorrectly() {
        // Arrange
        String nameWithSpaces = "  Bob Smith  ";

        // Act
        DutySlot slot = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, nameWithSpaces);

        // Assert
        assertEquals("Bob Smith", slot.getPersonnelName());
    }

    @Test
    void test_equals_sameInstance_returnsTrue() {
        // Arrange
        DutySlot slot = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");

        // Act & Assert
        assertEquals(slot, slot);
    }

    @Test
    void test_equals_nullObject_returnsFalse() {
        // Arrange
        DutySlot slot = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");

        // Act & Assert
        assertNotEquals(null, slot);
    }

    @Test
    void test_equals_nonDutySlotObject_returnsFalse() {
        // Arrange
        DutySlot slot = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");
        String notSlot = "not a DutySlot";

        // Act & Assert
        assertNotEquals(slot, notSlot);
    }

    @Test
    void test_equals_differentDutyType_returnsFalse() {
        // Arrange
        DutySlot s1 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");
        DutySlot s2 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.PATROL, "Alice");

        // Act & Assert
        assertNotEquals(s1, s2);
    }

    @Test
    void test_equals_differentDate_returnsFalse() {
        // Arrange
        DutySlot s1 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");
        DutySlot s2 = new DutySlot(testDate.plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");

        // Act & Assert
        assertNotEquals(s1, s2);
    }

    @Test
    void test_equals_differentPersonnelName_returnsFalse() {
        // Arrange
        DutySlot s1 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Alice");
        DutySlot s2 = new DutySlot(testDate, LocalTime.of(8, 0), LocalTime.of(10, 0),
                DutyType.GUARD_DUTY, "Bob");

        // Act & Assert
        assertNotEquals(s1, s2);
    }

    @Test
    void test_toString_validSlot_containsAllFields() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 5, 1);
        DutySlot slot = new DutySlot(date, LocalTime.of(14, 0), LocalTime.of(16, 0),
                DutyType.MEDICAL_COVER, "Charlie");

        // Act
        String result = slot.toString();

        // Assert
        assertTrue(result.contains("2026-05-01"));
        assertTrue(result.contains("14:00"));
        assertTrue(result.contains("Charlie"));
    }

    @Test
    void test_getters_allFieldsReturnedCorrectly() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 5, 1);
        LocalTime startTime = LocalTime.of(6, 30);
        LocalTime endTime = LocalTime.of(8, 30);
        DutySlot slot = new DutySlot(date, startTime, endTime, DutyType.SENTRY, "Dave");

        // Act & Assert
        assertEquals(date, slot.getDate());
        assertEquals(startTime, slot.getStartTime());
        assertEquals(endTime, slot.getEndTime());
        assertEquals(DutyType.SENTRY, slot.getDutyType());
        assertEquals("Dave", slot.getPersonnelName());
    }
}
