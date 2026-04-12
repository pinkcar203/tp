package meditrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Session POJO class.
 * Validates that role state persists correctly without relying on global Singletons.
 */
public class SessionTest {

    private Session session;

    @BeforeEach
    public void setUp() {
        session = new Session();
    }

    @Test
    public void constructor_createsCleanSession() {
        assertNull(session.getRole(), "A newly created session should not have an active role.");
    }

    @Test
    public void setRoleAndGetRole_storesAndRetrievesCorrectly() {
        session.setRole(Role.MEDICAL_OFFICER);
        assertEquals(Role.MEDICAL_OFFICER, session.getRole());

        // Update the role
        session.setRole(Role.LOGISTICS_OFFICER);
        assertEquals(Role.LOGISTICS_OFFICER, session.getRole());
    }

    @Test
    public void clear_resetsRoleToNull() {
        session.setRole(Role.FIELD_MEDIC);

        session.clear();

        assertNull(session.getRole(), "Session role should be null after clearing.");
    }
}