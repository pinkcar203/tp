package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Role enum, verifying UI display name formatting.
 */
public class RoleTest {

    @Test
    public void toString_formatsCorrectlyForUI() {
        assertEquals("Field Medic", Role.FIELD_MEDIC.toString());
        assertEquals("Medical Officer", Role.MEDICAL_OFFICER.toString());
        assertEquals("Platoon Commander", Role.PLATOON_COMMANDER.toString());
        assertEquals("Logistics Officer", Role.LOGISTICS_OFFICER.toString());
    }
}