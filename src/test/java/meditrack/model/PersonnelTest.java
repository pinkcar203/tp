package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/*
 * Equivalence Partitions:
 *
 * constructor(name, status) 
 * Parameter: name
 *   Valid:   non-blank string ("John"), string with leading/trailing spaces ("  Jane  ")
 *   Invalid: null, blank ("   ")
 *
 * Parameter: status
 *   Valid:   any Status enum value
 *   Invalid: null
 *
 * constructor(name, status, bloodGroup, allergies) [4 params => pairwise] 
 * Parameter: bloodGroup
 *   Valid:   any BloodGroup enum, null
 *
 * Parameter: allergies
 *   Valid:   non-null string, null (converted to ""), string with spaces ("  Dust  ")
 *
 * setBloodGroup(bloodGroup) 
 * Parameter: bloodGroup
 *   Valid:   any BloodGroup enum, null
 *
 * setAllergies(allergies) 
 * Parameter: allergies
 *   Valid:   non-null string, null (converted to "")
 *
 * setStatus(status) 
 * Parameter: status
 *   Valid:   any Status enum
 *   Invalid: null
 *
 * setStatusExpiryDate(date) 
 * Parameter: date
 *   Valid:   future date, null
 *
 * setLastModified(datetime) 
 * Parameter: datetime
 *   Valid:   any LocalDateTime
 *
 * isDeployable() 
 *   FIT => true, all others => false
 *
 * equals/hashCode 
 *   Same name (case-insensitive) => equal
 *   Different name => not equal
 *   Non-Personnel object => not equal
 */

// Verification tests
public class PersonnelTest {

    @Test
    public void constructor_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Personnel(null, Status.FIT));
    }

    @Test
    public void constructor_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Personnel("   ", Status.FIT));
    }

    @Test
    public void constructor_nullStatus_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Personnel("John", null));
    }

    @Test
    public void constructor_validInputs_createsProperlyTrimmedPersonnel() {
        Personnel person = new Personnel("  Jane Doe  ", Status.CASUALTY, BloodGroup.O_POS, " Dust ");
        assertEquals("Jane Doe", person.getName());
        assertEquals(Status.CASUALTY, person.getStatus());
        assertEquals(BloodGroup.O_POS, person.getBloodGroup());
        assertEquals("Dust", person.getAllergies());
        assertNotNull(person.getLastModified());
    }

    @Test
    public void setStatus_updatesStatusAndLastModified() throws InterruptedException {
        Personnel person = new Personnel("John", Status.FIT);
        LocalDateTime initialTime = person.getLastModified();

        // Small sleep to ensure the timestamp actually ticks forward
        Thread.sleep(10);

        person.setStatus(Status.MC);
        assertEquals(Status.MC, person.getStatus());
        assertTrue(person.getLastModified().isAfter(initialTime));
    }

    @Test
    public void isDeployable_statusFit_returnsTrue() {
        Personnel fitPerson = new Personnel("John", Status.FIT);
        assertTrue(fitPerson.isDeployable());
    }

    @Test
    public void isDeployable_statusNotFit_returnsFalse() {
        Personnel mcPerson = new Personnel("John", Status.MC);
        assertFalse(mcPerson.isDeployable());
    }

    @Test
    public void equals_sameNameDifferentCase_returnsTrue() {
        Personnel p1 = new Personnel("john doe", Status.FIT);
        Personnel p2 = new Personnel("JOHN DOE", Status.CASUALTY);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equals_differentName_returnsFalse() {
        Personnel p1 = new Personnel("John", Status.FIT);
        Personnel p2 = new Personnel("Jane", Status.FIT);
        assertNotEquals(p1, p2);
    }

    // --- setBloodGroup ---

    @Test
    void test_setBloodGroup_validBloodGroup_updatesAndModifiesTimestamp() throws InterruptedException {
        // Arrange
        Personnel person = new Personnel("Alice", Status.FIT);
        LocalDateTime initialTime = person.getLastModified();
        Thread.sleep(10);

        // Act
        person.setBloodGroup(BloodGroup.B_NEG);

        // Assert
        assertEquals(BloodGroup.B_NEG, person.getBloodGroup());
        assertTrue(person.getLastModified().isAfter(initialTime));
    }

    @Test
    void test_setBloodGroup_nullBloodGroup_setsToNull() {
        // Arrange
        Personnel person = new Personnel("Bob", Status.FIT, BloodGroup.A_POS, "None");

        // Act
        person.setBloodGroup(null);

        // Assert
        assertNull(person.getBloodGroup());
    }

    // --- setAllergies ---

    @Test
    void test_setAllergies_validString_updatesAndTrims() throws InterruptedException {
        // Arrange
        Personnel person = new Personnel("Charlie", Status.FIT);
        LocalDateTime initialTime = person.getLastModified();
        Thread.sleep(10);

        // Act
        person.setAllergies("  Pollen  ");

        // Assert
        assertEquals("Pollen", person.getAllergies());
        assertTrue(person.getLastModified().isAfter(initialTime));
    }

    @Test
    void test_setAllergies_nullValue_setsEmptyString() {
        // Arrange
        Personnel person = new Personnel("Dave", Status.FIT, BloodGroup.O_POS, "Latex");

        // Act
        person.setAllergies(null);

        // Assert
        assertEquals("", person.getAllergies());
    }

    // --- setStatusExpiryDate ---

    @Test
    void test_setStatusExpiryDate_validDate_updatesAndModifiesTimestamp() throws InterruptedException {
        // Arrange
        Personnel person = new Personnel("Eve", Status.MC);
        LocalDateTime initialTime = person.getLastModified();
        Thread.sleep(10);
        LocalDate futureDate = LocalDate.now().plusDays(7);

        // Act
        person.setStatusExpiryDate(futureDate);

        // Assert
        assertEquals(futureDate, person.getStatusExpiryDate());
        assertTrue(person.getLastModified().isAfter(initialTime));
    }

    @Test
    void test_setStatusExpiryDate_null_clearsExpiryDate() {
        // Arrange
        Personnel person = new Personnel("Frank", Status.MC);
        person.setStatusExpiryDate(LocalDate.now().plusDays(3));

        // Act
        person.setStatusExpiryDate(null);

        // Assert
        assertNull(person.getStatusExpiryDate());
    }

    // --- setLastModified ---

    @Test
    void test_setLastModified_historicalTimestamp_setsCorrectly() {
        // Arrange
        Personnel person = new Personnel("Grace", Status.FIT);
        LocalDateTime historicalTime = LocalDateTime.of(2020, 1, 1, 10, 0);

        // Act
        person.setLastModified(historicalTime);

        // Assert
        assertEquals(historicalTime, person.getLastModified());
    }

    // --- setStatus ---

    @Test
    void test_setStatus_nullStatus_throwsNullPointerException() {
        // Arrange
        Personnel person = new Personnel("Hank", Status.FIT);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> person.setStatus(null));
    }

    // --- isDeployable across all statuses ---

    @Test
    void test_isDeployable_lightDuty_returnsFalse() {
        // Arrange
        Personnel person = new Personnel("Ian", Status.LIGHT_DUTY);

        // Act & Assert
        assertFalse(person.isDeployable());
    }

    @Test
    void test_isDeployable_pending_returnsFalse() {
        // Arrange
        Personnel person = new Personnel("Jack", Status.PENDING);

        // Act & Assert
        assertFalse(person.isDeployable());
    }

    @Test
    void test_isDeployable_casualty_returnsFalse() {
        // Arrange
        Personnel person = new Personnel("Karen", Status.CASUALTY);

        // Act & Assert
        assertFalse(person.isDeployable());
    }

    // --- equals edge cases ---

    @Test
    void test_equals_sameInstance_returnsTrue() {
        // Arrange
        Personnel person = new Personnel("Leo", Status.FIT);

        // Act & Assert
        assertEquals(person, person);
    }

    @Test
    void test_equals_nonPersonnelObject_returnsFalse() {
        // Arrange
        Personnel person = new Personnel("Mia", Status.FIT);
        String notPersonnel = "Mia";

        // Act & Assert
        assertNotEquals(person, notPersonnel);
    }

    @Test
    void test_equals_nullObject_returnsFalse() {
        // Arrange
        Personnel person = new Personnel("Nate", Status.FIT);

        // Act & Assert
        assertNotEquals(null, person);
    }

    // --- toString ---

    @Test
    void test_toString_validPersonnel_containsNameAndStatus() {
        // Arrange
        Personnel person = new Personnel("Oscar", Status.MC);

        // Act
        String result = person.toString();

        // Assert
        assertTrue(result.contains("Oscar"));
        assertTrue(result.contains("MC"));
    }

    // --- constructor with 4 args: null allergies ---

    @Test
    void test_constructor_fourArgWithNullAllergies_defaultsToEmpty() {
        // Arrange & Act
        Personnel person = new Personnel("Pete", Status.FIT, BloodGroup.AB_POS, null);

        // Assert
        assertEquals("", person.getAllergies());
        assertNotNull(person.getLastModified());
    }
}
