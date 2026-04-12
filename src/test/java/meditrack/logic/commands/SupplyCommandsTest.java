package meditrack.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meditrack.commons.core.Constants;
import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Supply;

/**
 * Tests for supply-related commands.
 * Verifies execution logic, state changes in the model, and Role-Based Access Control (RBAC).
 */
class SupplyCommandsTest {

    private ModelManager model;

    /**
     * Initializes a fresh ModelManager before each test to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        model = new ModelManager();
    }

    // --- ADD SUPPLY COMMAND TESTS ---

    /**
     * Tests that a valid supply is successfully added to the model.
     */
    @Test
    void addSupplyCommand_execute_success() throws CommandException {
        AddSupplyCommand cmd = new AddSupplyCommand("Bandages", 100, LocalDate.of(2027, 6, 1));
        CommandResult result = cmd.execute(model);

        assertEquals(1, model.getFilteredSupplyList().size());
        assertTrue(result.getFeedbackToUser().contains("Bandages"));
    }

    /**
     * Tests that adding a supply with an identical name (batching) is successfully added.
     */
    @Test
    void addSupplyCommand_duplicateName_success() throws CommandException {
        model.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        AddSupplyCommand cmd = new AddSupplyCommand("Bandages", 50, LocalDate.of(2028, 1, 1));
        CommandResult result = cmd.execute(model);

        assertEquals(2, model.getFilteredSupplyList().size());
        assertTrue(result.getFeedbackToUser().contains("Bandages"));
    }

    /**
     * Verifies that Field Medics and Logistics Officers are authorized to add supplies.
     */
    @Test
    void addSupplyCommand_requiredRoles_areMedicAndLogistics() {
        AddSupplyCommand cmd = new AddSupplyCommand("Test", 1, LocalDate.of(2027, 1, 1));
        assertEquals(List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER), cmd.getRequiredRoles());
    }

    // --- EDIT SUPPLY COMMAND TESTS ---

    /**
     * Tests that a valid supply edit successfully updates the model.
     */
    @Test
    void editSupplyCommand_execute_success() throws CommandException {
        model.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        Supply edited = new Supply("Bandages XL", 200, LocalDate.of(2028, 1, 1));
        EditSupplyCommand cmd = new EditSupplyCommand(Index.fromOneBased(1), edited);
        CommandResult result = cmd.execute(model);

        assertEquals("Bandages XL", model.getFilteredSupplyList().get(0).getName());
        assertTrue(result.getFeedbackToUser().contains("Bandages XL"));
    }

    /**
     * Tests that editing a supply at an invalid index throws a CommandException.
     */
    @Test
    void editSupplyCommand_invalidIndex_throwsCommandException() {
        EditSupplyCommand cmd = new EditSupplyCommand(
                Index.fromOneBased(1),
                new Supply("Test", 10, LocalDate.of(2027, 1, 1)));

        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    /**
     * Verifies that Field Medics and Logistics Officers are authorized to edit supplies.
     */
    @Test
    void editSupplyCommand_requiredRoles_areMedicAndLogistics() {
        EditSupplyCommand cmd = new EditSupplyCommand(
                Index.fromOneBased(1),
                new Supply("Test", 10, LocalDate.of(2027, 1, 1)));
        assertEquals(List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER), cmd.getRequiredRoles());
    }

    // --- DELETE SUPPLY COMMAND TESTS ---

    /**
     * Tests that a valid supply deletion successfully removes the item from the model.
     */
    @Test
    void deleteSupplyCommand_execute_success() throws CommandException {
        model.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        CommandResult result = cmd.execute(model);

        assertTrue(model.getFilteredSupplyList().isEmpty());
        assertTrue(result.getFeedbackToUser().contains("Bandages"));
    }

    /**
     * Tests that attempting to delete a supply at an invalid index throws a CommandException.
     */
    @Test
    void deleteSupplyCommand_invalidIndex_throwsCommandException() {
        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    /**
     * Verifies that Field Medics and Logistics Officers are authorized to delete supplies.
     */
    @Test
    void deleteSupplyCommand_requiredRoles_areMedicAndLogistics() {
        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        assertEquals(List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER), cmd.getRequiredRoles());
    }

    // --- RESUPPLY REPORT COMMAND TESTS ---

    /**
     * Tests that generating a report with adequate inventory returns an all-clear message.
     */
    @Test
    void generateResupplyReport_allClear_returnsAllClearMessage() throws CommandException {
        model.addSupply(new Supply("Healthy", 100, LocalDate.now().plusDays(365)));

        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(
                Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                Constants.EXPIRY_THRESHOLD_DAYS);
        CommandResult result = cmd.execute(model);

        assertTrue(result.getFeedbackToUser().contains("adequately stocked"));
    }

    /**
     * Tests that supplies below the quantity threshold are correctly flagged.
     */
    @Test
    void generateResupplyReport_lowStock_flagged() throws CommandException {
        model.addSupply(new Supply("LowItem", 5, LocalDate.now().plusDays(365)));

        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(
                Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                Constants.EXPIRY_THRESHOLD_DAYS);
        CommandResult result = cmd.execute(model);

        assertTrue(result.getFeedbackToUser().contains("LowItem"));
        assertTrue(result.getFeedbackToUser().contains("Low Stock"));
    }

    /**
     * Tests that supplies approaching their expiration date are correctly flagged.
     */
    @Test
    void generateResupplyReport_expiringSoon_flagged() throws CommandException {
        model.addSupply(new Supply("ExpiringItem", 100, LocalDate.now().plusDays(10)));

        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(
                Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                Constants.EXPIRY_THRESHOLD_DAYS);
        CommandResult result = cmd.execute(model);

        assertTrue(result.getFeedbackToUser().contains("ExpiringItem"));
        assertTrue(result.getFeedbackToUser().contains("Expiring Soon"));
    }

    /**
     * Tests that supplies that are both low in stock and expiring soon are flagged as both.
     */
    @Test
    void generateResupplyReport_both_flaggedAsBoth() throws CommandException {
        model.addSupply(new Supply("BothItem", 5, LocalDate.now().plusDays(10)));

        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(
                Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                Constants.EXPIRY_THRESHOLD_DAYS);
        CommandResult result = cmd.execute(model);

        assertTrue(result.getFeedbackToUser().contains("BothItem"));
        assertTrue(result.getFeedbackToUser().contains("Both"));
    }

    /**
     * Verifies that only Logistics Officers are authorized to generate resupply reports.
     */
    @Test
    void generateResupplyReport_requiredRole_isLogisticsOfficer() {
        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(20, 30);
        assertEquals(List.of(Role.LOGISTICS_OFFICER), cmd.getRequiredRoles());
    }
}