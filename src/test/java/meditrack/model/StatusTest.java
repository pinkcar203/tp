package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Status enum, verifying deployment logic and string parsing.
 */
public class StatusTest {

    @Test
    public void isDeployable_fitStatus_returnsTrue() {
        assertTrue(Status.FIT.isDeployable());
    }

    @Test
    public void isDeployable_nonFitStatuses_returnsFalse() {
        assertFalse(Status.PENDING.isDeployable());
        assertFalse(Status.LIGHT_DUTY.isDeployable());
        assertFalse(Status.MC.isDeployable());
        assertFalse(Status.CASUALTY.isDeployable());
    }

    @Test
    public void fromString_validInputs_parsesCorrectly() {
        assertEquals(Status.FIT, Status.fromString("FIT"));
        assertEquals(Status.FIT, Status.fromString(" fit ")); // whitespace testing
        assertEquals(Status.LIGHT_DUTY, Status.fromString("LIGHT_DUTY"));
        assertEquals(Status.LIGHT_DUTY, Status.fromString("light duty")); // space replacement testing
        assertEquals(Status.MC, Status.fromString("Mc")); // case-insensitive testing
        assertEquals(Status.CASUALTY, Status.fromString("CASUALTY"));
        assertEquals(Status.PENDING, Status.fromString("pending"));
    }

    @Test
    public void fromString_nullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Status.fromString(null));
    }

    @Test
    public void fromString_invalidInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Status.fromString("SUPER_FIT"));
        assertThrows(IllegalArgumentException.class, () -> Status.fromString(""));
    }

    @Test
    public void toString_formatsCorrectly() {
        assertEquals("FIT", Status.FIT.toString());
        assertEquals("LIGHT DUTY", Status.LIGHT_DUTY.toString()); // Verifies underscore removal
    }
}