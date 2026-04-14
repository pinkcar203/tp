package meditrack.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.commons.core.Index;
import meditrack.logic.commands.AddSupplyCommand;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.DeleteSupplyCommand;
import meditrack.logic.commands.EditSupplyCommand;
import meditrack.logic.commands.GenerateResupplyReportCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.logic.commands.personnel.RemovePersonnelCommand;
import meditrack.logic.commands.personnel.UpdateStatusCommand;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Supply;
import meditrack.storage.StorageStub;

class WorkflowValidationTest {

    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        logic = new LogicManager(model, new StorageStub());
    }

    /**
     * End-to-end scenario: Medical Officer adds personnel, updates status to MC with duration,
     * time passes, and the system auto-reverts expired MC back to FIT.
     */
    @Test
    void test_fullPersonnelLifecycle_addUpdateStatusTimeTravel_statusAutoReverts() throws CommandException {
        // Step 1: Medical Officer adds personnel
        model.setRole(Role.MEDICAL_OFFICER);
        AddPersonnelCommand addCmd = new AddPersonnelCommand("Soldier One", Status.FIT);
        logic.executeCommand(addCmd);
        assertEquals(1, model.getPersonnelCount());

        // Step 2: Medical Officer assigns MC with 3-day duration
        UpdateStatusCommand mcCmd = new UpdateStatusCommand(1, Status.MC, 3);
        logic.executeCommand(mcCmd);
        Personnel soldier = model.getFilteredPersonnelList(null).get(0);
        assertEquals(Status.MC, soldier.getStatus());

        // Step 3: Time travel 4 days into the future
        Clock futureClock = Clock.offset(model.getClock(), Duration.ofDays(4));
        model.setClock(futureClock);
        model.cleanExpiredStatuses();

        // Step 4: Verify auto-revert to FIT
        assertEquals(Status.FIT, soldier.getStatus());
        assertNull(soldier.getStatusExpiryDate());
    }

    /**
     * End-to-end scenario: Field Medic adds supply, edits it, then deletes it.
     */
    @Test
    void test_fullSupplyLifecycle_addEditDelete_modelReflectsAllChanges() throws CommandException {
        // Step 1: Field Medic adds supply
        model.setRole(Role.FIELD_MEDIC);
        AddSupplyCommand addCmd = new AddSupplyCommand("Bandages", 100, LocalDate.now().plusDays(60));
        logic.executeCommand(addCmd);
        assertEquals(1, model.getFilteredSupplyList().size());

        // Step 2: Edit the supply
        Supply editedSupply = new Supply("Bandages XL", 200, LocalDate.now().plusDays(120));
        EditSupplyCommand editCmd = new EditSupplyCommand(Index.fromOneBased(1), editedSupply);
        logic.executeCommand(editCmd);
        assertEquals("Bandages XL", model.getFilteredSupplyList().get(0).getName());

        // Step 3: Delete the supply
        DeleteSupplyCommand deleteCmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        logic.executeCommand(deleteCmd);
        assertTrue(model.getFilteredSupplyList().isEmpty());
    }

    /**
     * End-to-end scenario: Logistics Officer generates a resupply report
     * after Field Medic stocks items.
     */
    @Test
    void test_resupplyReportWorkflow_lowStockAndExpiring_flagsBothCorrectly() throws CommandException {
        // Step 1: Field Medic adds supplies
        model.setRole(Role.FIELD_MEDIC);
        logic.executeCommand(new AddSupplyCommand("LowItem", 5, LocalDate.now().plusDays(365)));
        logic.executeCommand(new AddSupplyCommand("ExpiringItem", 100, LocalDate.now().plusDays(10)));
        logic.executeCommand(new AddSupplyCommand("HealthyItem", 200, LocalDate.now().plusDays(365)));

        // Step 2: Logistics Officer generates report
        model.setRole(Role.LOGISTICS_OFFICER);
        GenerateResupplyReportCommand reportCmd = new GenerateResupplyReportCommand(50, 30);
        CommandResult result = logic.executeCommand(reportCmd);

        // Assert: LowItem and ExpiringItem flagged, HealthyItem not
        String report = result.getFeedbackToUser();
        assertTrue(report.contains("LowItem"));
        assertTrue(report.contains("ExpiringItem"));
    }

    /**
     * End-to-end scenario: RBAC prevents unauthorized cross-role operations.
     */
    @Test
    void test_rbacEnforcement_fieldMedicCannotAddPersonnel_throwsCommandException() {
        // Arrange: Field Medic tries to add personnel (not allowed)
        model.setRole(Role.FIELD_MEDIC);
        AddPersonnelCommand cmd = new AddPersonnelCommand("Unauthorized", Status.FIT);

        // Act & Assert
        assertThrows(CommandException.class, () -> logic.executeCommand(cmd));
    }

    @Test
    void test_rbacEnforcement_logisticsOfficerCannotRemovePersonnel_throwsCommandException()
            throws CommandException {
        // Arrange: add personnel first with authorized role, then switch
        model.setRole(Role.MEDICAL_OFFICER);
        model.addPersonnel("Target", Status.FIT);
        model.setRole(Role.LOGISTICS_OFFICER);
        RemovePersonnelCommand cmd = new RemovePersonnelCommand(1);

        // Act & Assert
        assertThrows(CommandException.class, () -> logic.executeCommand(cmd));
    }

    /**
     * End-to-end scenario: Field Medic can only mark CASUALTY, not MC.
     */
    @Test
    void test_fieldMedicRbac_canMarkCasualty_cannotMarkMc() throws CommandException {
        // Arrange
        model.setRole(Role.MEDICAL_OFFICER);
        model.addPersonnel("Injured", Status.FIT);
        model.setRole(Role.FIELD_MEDIC);

        // Act: CASUALTY should succeed
        UpdateStatusCommand casualtyCmd = new UpdateStatusCommand(1, Status.CASUALTY);
        logic.executeCommand(casualtyCmd);
        assertEquals(Status.CASUALTY, model.getFilteredPersonnelList(null).get(0).getStatus());

        // Act: MC should fail (inner RBAC in command)
        UpdateStatusCommand mcCmd = new UpdateStatusCommand(1, Status.MC);
        assertThrows(CommandException.class, () -> logic.executeCommand(mcCmd));
    }
}
