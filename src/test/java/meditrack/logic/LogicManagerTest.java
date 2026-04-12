package meditrack.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.commons.core.Index;
import meditrack.logic.commands.AddSupplyCommand;
import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.DeleteSupplyCommand;
import meditrack.logic.commands.EditSupplyCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.model.Model;
import meditrack.model.ModelManager;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Supply;
import meditrack.storage.Storage;
import meditrack.storage.StorageStub;


class LogicManagerTest {

    private ModelManager model;
    private Storage validStorage;
    private LogicManager logicManager;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        validStorage = new StorageStub();
        logicManager = new LogicManager(model, validStorage);
    }

    // POSITIVE TESTS 

    @Test
    void test_executeCommand_authorizedRoleAddSupply_returnsSuccessResult() throws CommandException {
        // Arrange
        model.setRole(Role.FIELD_MEDIC);
        LocalDate validExpiry = LocalDate.now().plusDays(60);
        String validName = "Bandages";
        int validQuantity = 100;
        AddSupplyCommand command = new AddSupplyCommand(validName, validQuantity, validExpiry);

        // Act
        CommandResult result = logicManager.executeCommand(command);

        // Assert
        assertTrue(result.getFeedbackToUser().contains(validName));
        assertEquals(1, model.getFilteredSupplyList().size());
    }

    @Test
    void test_executeCommand_authorizedRoleLogisticsOfficer_returnsSuccessResult() throws CommandException {
        // Arrange
        model.setRole(Role.LOGISTICS_OFFICER);
        LocalDate validExpiry = LocalDate.now().plusDays(60);
        AddSupplyCommand command = new AddSupplyCommand("Morphine", 50, validExpiry);

        // Act
        CommandResult result = logicManager.executeCommand(command);

        // Assert
        assertTrue(result.getFeedbackToUser().contains("Morphine"));
    }

    @Test
    void test_executeCommand_authorizedRoleDeleteSupply_returnsSuccessResult() throws CommandException {
        // Arrange
        model.setRole(Role.FIELD_MEDIC);
        model.addSupply(new Supply("Aspirin", 20, LocalDate.now().plusDays(30)));
        DeleteSupplyCommand command = new DeleteSupplyCommand(Index.fromOneBased(1));

        // Act
        CommandResult result = logicManager.executeCommand(command);

        // Assert
        assertTrue(result.getFeedbackToUser().contains("Aspirin"));
        assertTrue(model.getFilteredSupplyList().isEmpty());
    }

    @Test
    void test_executeCommand_authorizedRoleEditSupply_returnsSuccessResult() throws CommandException {
        // Arrange
        model.setRole(Role.FIELD_MEDIC);
        model.addSupply(new Supply("OldName", 10, LocalDate.now().plusDays(30)));
        Supply editedSupply = new Supply("NewName", 200, LocalDate.now().plusDays(90));
        EditSupplyCommand command = new EditSupplyCommand(Index.fromOneBased(1), editedSupply);

        // Act
        CommandResult result = logicManager.executeCommand(command);

        // Assert
        assertTrue(result.getFeedbackToUser().contains("NewName"));
    }

    @Test
    void test_executeCommand_commandWithEmptyRequiredRoles_executesWithoutRbacCheck()
            throws CommandException {
        // Arrange: command with empty roles list (unrestricted)
        Command unrestrictedCommand = new Command() {
            @Override
            public CommandResult execute(Model m) {
                return new CommandResult("Unrestricted command executed.");
            }

            @Override
            public List<Role> getRequiredRoles() {
                return List.of();
            }
        };

        // Act
        CommandResult result = logicManager.executeCommand(unrestrictedCommand);

        // Assert
        assertEquals("Unrestricted command executed.", result.getFeedbackToUser());
    }

    @Test
    void test_executeCommand_commandWithNullRequiredRoles_executesWithoutRbacCheck()
            throws CommandException {
        // Arrange
        Command nullRolesCommand = new Command() {
            @Override
            public CommandResult execute(Model m) {
                return new CommandResult("Null roles command executed.");
            }

            @Override
            public List<Role> getRequiredRoles() {
                return null;
            }
        };

        // Act
        CommandResult result = logicManager.executeCommand(nullRolesCommand);

        // Assert
        assertEquals("Null roles command executed.", result.getFeedbackToUser());
    }

    @Test
    void test_executeCommand_autoSavesAfterExecution_storageReceivesData() throws CommandException {
        // Arrange: use a spy-like storage that tracks save calls
        boolean[] saveCalled = {false};
        Storage spyStorage = new Storage() {
            @Override
            public Optional<ReadOnlyMediTrack> readMediTrackData() {
                return Optional.empty();
            }

            @Override
            public void saveMediTrackData(ReadOnlyMediTrack data) {
                saveCalled[0] = true;
            }
        };
        LogicManager spyLogicManager = new LogicManager(model, spyStorage);
        model.setRole(Role.FIELD_MEDIC);
        AddSupplyCommand command = new AddSupplyCommand("Gauze", 10, LocalDate.now().plusDays(30));

        // Act
        spyLogicManager.executeCommand(command);

        // Assert
        assertTrue(saveCalled[0]);
    }

    // NEGATIVE TESTS 

    @Test
    void test_executeCommand_unauthorizedRoleNull_throwsCommandException() {
        // Arrange: session role is null (not logged in)
        AddSupplyCommand command = new AddSupplyCommand("Bandages", 100, LocalDate.now().plusDays(60));

        // Act & Assert
        CommandException ex = assertThrows(CommandException.class, () ->
                logicManager.executeCommand(command));
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void test_executeCommand_unauthorizedRoleMismatch_throwsCommandException() {
        // Arrange: platoon commander cannot add supplies
        model.setRole(Role.PLATOON_COMMANDER);
        AddSupplyCommand command = new AddSupplyCommand("Bandages", 100, LocalDate.now().plusDays(60));

        // Act & Assert
        CommandException ex = assertThrows(CommandException.class, () ->
                logicManager.executeCommand(command));
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void test_executeCommand_storageThrowsIoException_throwsCommandException() {
        // Arrange: storage that always fails on save
        Storage failingStorage = new Storage() {
            @Override
            public Optional<ReadOnlyMediTrack> readMediTrackData() {
                return Optional.empty();
            }

            @Override
            public void saveMediTrackData(ReadOnlyMediTrack data) throws IOException {
                throw new IOException("Disk full");
            }
        };
        LogicManager failLogicManager = new LogicManager(model, failingStorage);
        model.setRole(Role.FIELD_MEDIC);
        AddSupplyCommand command = new AddSupplyCommand("Bandages", 10, LocalDate.now().plusDays(30));

        // Act & Assert
        CommandException ex = assertThrows(CommandException.class, () ->
                failLogicManager.executeCommand(command));
        assertTrue(ex.getMessage().contains("Could not save data"));
    }

    @Test
    void test_executeCommand_medicalOfficerOnSupplyCommand_throwsCommandException() {
        // Arrange: Medical Officer is not authorized for supply commands
        model.setRole(Role.MEDICAL_OFFICER);
        AddSupplyCommand command = new AddSupplyCommand("Bandages", 100, LocalDate.now().plusDays(60));

        // Act & Assert
        assertThrows(CommandException.class, () -> logicManager.executeCommand(command));
    }
}
