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

class SupplyCommandsTest {

    private ModelManager model;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
    }

    @Test
    void addSupplyCommand_execute_success() throws CommandException {
        AddSupplyCommand cmd = new AddSupplyCommand("Bandages", 100, LocalDate.of(2027, 6, 1));
        CommandResult result = cmd.execute(model);

        assertEquals(1, model.getFilteredSupplyList().size());
        assertTrue(result.getFeedbackToUser().contains("Bandages"));
    }

    @Test
    void addSupplyCommand_duplicateName_throwsCommandException() {
        model.addSupply(new Supply("Bandages", 50, LocalDate.of(2027, 6, 1)));

        AddSupplyCommand cmd = new AddSupplyCommand("bandages", 100, LocalDate.of(2028, 1, 1));
        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    @Test
    void addSupplyCommand_requiredRole_isFieldMedic() {
        AddSupplyCommand cmd = new AddSupplyCommand("Test", 1, LocalDate.of(2027, 1, 1));
        assertEquals(Role.FIELD_MEDIC, cmd.getRequiredRoles());
    }

    @Test
    void editSupplyCommand_execute_success() throws CommandException {
        model.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        Supply edited = new Supply("Bandages XL", 200, LocalDate.of(2028, 1, 1));
        EditSupplyCommand cmd = new EditSupplyCommand(Index.fromOneBased(1), edited);
        CommandResult result = cmd.execute(model);

        assertEquals("Bandages XL", model.getFilteredSupplyList().get(0).getName());
        assertTrue(result.getFeedbackToUser().contains("Bandages XL"));
    }

    @Test
    void editSupplyCommand_invalidIndex_throwsCommandException() {
        EditSupplyCommand cmd = new EditSupplyCommand(
                Index.fromOneBased(1),
                new Supply("Test", 10, LocalDate.of(2027, 1, 1)));

        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    @Test
    void editSupplyCommand_requiredRole_isFieldMedic() {
        EditSupplyCommand cmd = new EditSupplyCommand(
                Index.fromOneBased(1),
                new Supply("Test", 10, LocalDate.of(2027, 1, 1)));
        assertEquals(List.of(Role.FIELD_MEDIC), cmd.getRequiredRoles());
    }

    // Delete Supply Command Tests

    @Test
    void deleteSupplyCommand_execute_success() throws CommandException {
        model.addSupply(new Supply("Bandages", 100, LocalDate.of(2027, 6, 1)));

        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        CommandResult result = cmd.execute(model);

        assertTrue(model.getFilteredSupplyList().isEmpty());
        assertTrue(result.getFeedbackToUser().contains("Bandages"));
    }

    @Test
    void deleteSupplyCommand_invalidIndex_throwsCommandException() {
        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        assertThrows(CommandException.class, () -> cmd.execute(model));
    }

    @Test
    void deleteSupplyCommand_requiredRole_isFieldMedic() {
        DeleteSupplyCommand cmd = new DeleteSupplyCommand(Index.fromOneBased(1));
        assertEquals(List.of(Role.FIELD_MEDIC), cmd.getRequiredRoles());
    }

    @Test
    void generateResupplyReport_allClear_returnsAllClearMessage() throws CommandException {
        model.addSupply(new Supply("Healthy", 100, LocalDate.now().plusDays(365)));

        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(
                Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                Constants.EXPIRY_THRESHOLD_DAYS);
        CommandResult result = cmd.execute(model);

        assertTrue(result.getFeedbackToUser().contains("adequately stocked"));
    }

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

    @Test
    void generateResupplyReport_requiredRole_isLogisticsOfficer() {
        GenerateResupplyReportCommand cmd = new GenerateResupplyReportCommand(20, 30);
        assertEquals(List.of(Role.LOGISTICS_OFFICER), cmd.getRequiredRoles());
    }
}
