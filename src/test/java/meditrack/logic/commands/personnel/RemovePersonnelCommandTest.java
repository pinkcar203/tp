package meditrack.logic.commands.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Status;

/**
 * Tests for RemovePersonnelCommand, verifying list bounds checking and execution.
 */
public class RemovePersonnelCommandTest {

    private ModelManager modelManager;

    @BeforeEach
    public void setUp() throws CommandException {
        modelManager = new ModelManager();
        modelManager.getSession().setRole(Role.MEDICAL_OFFICER);
        modelManager.addPersonnel("Alice", Status.FIT);
        modelManager.addPersonnel("Bob", Status.MC);
    }

    @Test
    public void execute_validIndex_success() throws CommandException {
        RemovePersonnelCommand cmd = new RemovePersonnelCommand(1);

        CommandResult result = cmd.execute(modelManager);

        assertEquals(1, modelManager.getPersonnelCount());
        assertEquals("Bob", modelManager.getFilteredPersonnelList(null).get(0).getName());
        assertEquals(String.format(RemovePersonnelCommand.MESSAGE_SUCCESS, "Alice"), result.getFeedbackToUser());
    }

    @Test
    public void execute_invalidIndexOutOfBounds_throwsCommandException() {
        RemovePersonnelCommand cmd = new RemovePersonnelCommand(5);

        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }

    @Test
    public void execute_invalidZeroIndex_throwsCommandException() {
        RemovePersonnelCommand cmd = new RemovePersonnelCommand(0);

        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }

    @Test
    public void getRequiredRoles_includesMedicalOfficerAndPlatoonCommander() {
        RemovePersonnelCommand cmd = new RemovePersonnelCommand(1);
        assertEquals(List.of(Role.MEDICAL_OFFICER, Role.PLATOON_COMMANDER), cmd.getRequiredRoles());
    }
}