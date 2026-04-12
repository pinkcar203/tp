package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 *Tests for the DutyType enum, ensuring proper display formatting and value resolution.
 */
public class DutyTypeTest {

    @Test
    public void toString_formatsCorrectlyForUI() {
        assertEquals("Guard Duty", DutyType.GUARD_DUTY.toString());
        assertEquals("Medical Cover", DutyType.MEDICAL_COVER.toString());
        assertEquals("Patrol", DutyType.PATROL.toString());
        assertEquals("Standby", DutyType.STANDBY.toString());
        assertEquals("Sentry", DutyType.SENTRY.toString());
    }

}