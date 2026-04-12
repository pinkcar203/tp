package meditrack.logic.commands.personnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Status;

/*
 * Equivalence Partitions:
 *
 *  constructor(name, status, bloodGroup, allergies) [4 params => pairwise] 
 * Parameter: name
 *   Valid:   non-null string ("John")
 *
 * Parameter: status
 *   Valid:   FIT, MC, LIGHT_DUTY, CASUALTY, PENDING
 *
 * Parameter: bloodGroup
 *   Valid:   any BloodGroup enum, null
 *
 * Parameter: allergies
 *   Valid:   non-null string ("Peanuts"), null (converted to ""), empty ("")
 *
 *  execute(model) 
 * Parameter: session role
 *   Valid:   MEDICAL_OFFICER (all statuses), PLATOON_COMMANDER (PENDING only)
 *   Invalid: PLATOON_COMMANDER with non-PENDING status
 *           duplicate name in roster
 *
 *  getRequiredRoles() 
 *   Returns: [MEDICAL_OFFICER, PLATOON_COMMANDER]
 *
 *  getters 
 *   getName(), getStatus(), getBloodGroup(), getAllergies()
 */


public class AddPersonnelCommandTest {

    private ModelManager modelManager;

    @BeforeEach
    public void setUp() {
        modelManager = new ModelManager();
    }

    @Test
    public void execute_medicalOfficerAddsFit_success() throws CommandException {
        modelManager.getSession().setRole(Role.MEDICAL_OFFICER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("John Tan", Status.FIT);

        CommandResult result = cmd.execute(modelManager);

        assertEquals(1, modelManager.getPersonnelCount());
        assertEquals(String.format(AddPersonnelCommand.MESSAGE_SUCCESS, "John Tan", Status.FIT), result.getFeedbackToUser());
    }

    @Test
    public void execute_platoonCommanderAddsPending_success() throws CommandException {
        modelManager.getSession().setRole(Role.PLATOON_COMMANDER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Bob Lee", Status.PENDING);

        CommandResult result = cmd.execute(modelManager);

        assertEquals(1, modelManager.getPersonnelCount());
        assertEquals(Status.PENDING, modelManager.getFilteredPersonnelList(null).get(0).getStatus());
    }

    @Test
    public void execute_platoonCommanderAddsFit_throwsCommandException() {
        modelManager.getSession().setRole(Role.PLATOON_COMMANDER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Bob Lee", Status.FIT);

        CommandException ex = assertThrows(CommandException.class, () -> cmd.execute(modelManager));
        assertEquals("Platoon Commanders can only add personnel with PENDING status.", ex.getMessage());
    }

    @Test
    public void execute_duplicatePersonnel_throwsCommandException() throws CommandException {
        modelManager.getSession().setRole(Role.MEDICAL_OFFICER);
        modelManager.addPersonnel("John Tan", Status.FIT); // Add initially

        AddPersonnelCommand cmd = new AddPersonnelCommand("John Tan", Status.MC); // Attempt duplicate

        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }

    //  Getter tests 

    @Test
    void test_getName_validName_returnsCorrectName() {
        // Arrange
        String expectedName = "John";
        AddPersonnelCommand cmd = new AddPersonnelCommand(expectedName, Status.FIT);

        // Act & Assert
        assertEquals(expectedName, cmd.getName());
    }

    @Test
    void test_getStatus_fitStatus_returnsFit() {
        // Arrange
        AddPersonnelCommand cmd = new AddPersonnelCommand("Alice", Status.FIT);

        // Act & Assert
        assertEquals(Status.FIT, cmd.getStatus());
    }

    @Test
    void test_getBloodGroup_withBloodGroup_returnsCorrectBloodGroup() {
        // Arrange
        AddPersonnelCommand cmd = new AddPersonnelCommand("Bob", Status.FIT, BloodGroup.O_POS, "None");

        // Act & Assert
        assertEquals(BloodGroup.O_POS, cmd.getBloodGroup());
    }

    @Test
    void test_getBloodGroup_basicConstructor_returnsNull() {
        // Arrange
        AddPersonnelCommand cmd = new AddPersonnelCommand("Eve", Status.MC);

        // Act & Assert
        assertNull(cmd.getBloodGroup());
    }

    @Test
    void test_getAllergies_validAllergies_returnsCorrectString() {
        // Arrange
        String expectedAllergies = "Peanuts, Shellfish";
        AddPersonnelCommand cmd = new AddPersonnelCommand("Dave", Status.FIT, BloodGroup.A_NEG, expectedAllergies);

        // Act & Assert
        assertEquals(expectedAllergies, cmd.getAllergies());
    }

    @Test
    void test_getAllergies_nullAllergies_returnsEmptyString() {
        // Arrange
        AddPersonnelCommand cmd = new AddPersonnelCommand("Eve", Status.FIT, BloodGroup.A_POS, null);

        // Act & Assert
        assertEquals("", cmd.getAllergies());
    }

    @Test
    void test_getRequiredRoles_returnsMedicalOfficerAndPlatoonCommander() {
        // Arrange
        AddPersonnelCommand cmd = new AddPersonnelCommand("Test", Status.FIT);

        // Act & Assert
        assertEquals(List.of(Role.MEDICAL_OFFICER, Role.PLATOON_COMMANDER), cmd.getRequiredRoles());
    }

    //  execute with full medical profile 

    @Test
    void test_execute_medicalOfficerWithBloodGroupAndAllergies_addsSuccessfully() throws CommandException {
        // Arrange
        modelManager.getSession().setRole(Role.MEDICAL_OFFICER);
        AddPersonnelCommand cmd = new AddPersonnelCommand(
                "Full Profile", Status.LIGHT_DUTY, BloodGroup.AB_NEG, "Latex");

        // Act
        CommandResult result = cmd.execute(modelManager);

        // Assert
        assertEquals(1, modelManager.getPersonnelCount());
        assertEquals("Full Profile", modelManager.getFilteredPersonnelList(null).get(0).getName());
    }

    //  Pairwise: PLATOON_COMMANDER with different statuses 

    @Test
    void test_execute_platoonCommanderWithMcStatus_throwsCommandException() {
        // Arrange
        modelManager.getSession().setRole(Role.PLATOON_COMMANDER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Soldier", Status.MC);

        // Act & Assert
        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }

    @Test
    void test_execute_platoonCommanderWithLightDutyStatus_throwsCommandException() {
        // Arrange
        modelManager.getSession().setRole(Role.PLATOON_COMMANDER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Soldier", Status.LIGHT_DUTY);

        // Act & Assert
        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }

    @Test
    void test_execute_platoonCommanderWithCasualtyStatus_throwsCommandException() {
        // Arrange
        modelManager.getSession().setRole(Role.PLATOON_COMMANDER);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Soldier", Status.CASUALTY);

        // Act & Assert
        assertThrows(CommandException.class, () -> cmd.execute(modelManager));
    }
}
