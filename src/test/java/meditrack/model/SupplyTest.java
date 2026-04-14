package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/*
 * Equivalence Partitions:
 *
 * constructor(name, quantity, expiryDate) 
 * Parameter: name
 *   Valid:   non-null string
 *   Invalid: null
 *
 * Parameter: expiryDate
 *   Valid:   non-null LocalDate
 *   Invalid: null
 *
 * equals(Object) 
 * Parameter: other
 *   Valid:   same instance (reflexive), same name different case, same name same case
 *   Invalid: different name, null, non-Supply object
 *
 * toString() 
 *   Returns formatted string with name, quantity, expiryDate
 */

public class SupplyTest {

    @Test
    public void constructor_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Supply(null, 10, LocalDate.of(2027, 1, 1)));
    }

    @Test
    public void constructor_nullExpiryDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new Supply("Bandages", 10, null));
    }

    @Test
    public void getters_returnCorrectValues() {
        LocalDate expiry = LocalDate.of(2028, 5, 15);
        Supply supply = new Supply("IV Drip", 25, expiry);

        assertEquals("IV Drip", supply.getName());
        assertEquals(25, supply.getQuantity());
        assertEquals(expiry, supply.getExpiryDate());
    }

    @Test
    public void equals_sameNameDifferentCase_returnsTrue() {
        // Because equality is strictly tied to name, identical names are treated as equal
        // regardless of differing quantities or dates.
        Supply s1 = new Supply("bandages", 10, LocalDate.of(2027, 1, 1));
        Supply s2 = new Supply("BANDAGES", 50, LocalDate.of(2028, 1, 1));

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void equals_differentName_returnsFalse() {
        Supply s1 = new Supply("Bandages", 10, LocalDate.of(2027, 1, 1));
        Supply s2 = new Supply("Morphine", 10, LocalDate.of(2027, 1, 1));

        assertNotEquals(s1, s2);
    }

    @Test
    void test_equals_sameInstance_returnsTrue() {
        // Arrange
        Supply supply = new Supply("Bandages", 100, LocalDate.of(2027, 1, 1));

        // Act & Assert
        assertEquals(supply, supply);
    }

    @Test
    void test_equals_nullObject_returnsFalse() {
        // Arrange
        Supply supply = new Supply("Bandages", 100, LocalDate.of(2027, 1, 1));

        // Act & Assert
        assertNotEquals(null, supply);
    }

    @Test
    void test_equals_nonSupplyObject_returnsFalse() {
        // Arrange
        Supply supply = new Supply("Bandages", 100, LocalDate.of(2027, 1, 1));
        String notSupply = "Bandages";

        // Act & Assert
        assertNotEquals(supply, notSupply);
    }

    @Test
    void test_toString_validSupply_containsAllFields() {
        // Arrange
        LocalDate expiry = LocalDate.of(2028, 6, 15);
        Supply supply = new Supply("Morphine", 25, expiry);

        // Act
        String result = supply.toString();

        // Assert
        assertTrue(result.contains("Morphine"));
        assertTrue(result.contains("25"));
        assertTrue(result.contains("2028-06-15"));
    }

    @Test
    void test_hashCode_sameNameDifferentCase_sameHashCode() {
        // Arrange
        Supply s1 = new Supply("aspirin", 10, LocalDate.of(2027, 1, 1));
        Supply s2 = new Supply("ASPIRIN", 50, LocalDate.of(2028, 6, 1));

        // Act & Assert
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
