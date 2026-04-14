package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for the BloodGroup enum, verifying display formatting and robust string parsing.
 */
public class BloodGroupTest {

    @Test
    public void display_formatsCorrectly() {
        assertEquals("A+", BloodGroup.A_POS.display());
        assertEquals("O-", BloodGroup.O_NEG.display());
        assertEquals("AB+", BloodGroup.AB_POS.display());
        assertEquals("UNKNOWN", BloodGroup.UNKNOWN.display());
    }

    @Test
    public void fromString_validDisplayNames_parsesCorrectly() {
        assertEquals(BloodGroup.A_POS, BloodGroup.fromString("A+"));
        assertEquals(BloodGroup.O_NEG, BloodGroup.fromString(" o- ")); // whitespace testing
        assertEquals(BloodGroup.AB_POS, BloodGroup.fromString("ab+")); // case-insensitive testing
    }

    @Test
    public void fromString_validEnumNames_parsesCorrectly() {
        assertEquals(BloodGroup.A_POS, BloodGroup.fromString("A_POS"));
        assertEquals(BloodGroup.O_NEG, BloodGroup.fromString("O_NEG"));
        assertEquals(BloodGroup.B_POS, BloodGroup.fromString("b_pos")); // case-insensitive testing
    }

    @Test
    public void fromString_nullOrBlankInput_returnsUnknown() {
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromString(null));
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromString(""));
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromString("   "));
    }

    @Test
    public void fromString_invalidString_returnsUnknown() {
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromString("C+"));
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromString("NotABloodType"));
    }
}