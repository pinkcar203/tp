package meditrack.logic.commands.personnel;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.model.Session;
import java.util.List;

/** Changes someone's status. Index is 1-based like the UI table. */
public class UpdateStatusCommand extends Command {

    public static final String COMMAND_WORD = "update_status";

    public static final String MESSAGE_SUCCESS = "Updated status of %s to: %s";

    private final int oneBasedIndex;
    private final Status newStatus;

    /**
     * @param oneBasedIndex 1-based index as displayed in the UI list
     * @param newStatus     pre-validated new status
     */
    public UpdateStatusCommand(int oneBasedIndex, Status newStatus) {
        this.oneBasedIndex = oneBasedIndex;
        this.newStatus = newStatus;
    }

    /** Updates the person's status in the model. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Role currentRole = Session.getInstance().getRole();

        if (currentRole == Role.FIELD_MEDIC && newStatus != Status.CASUALTY) {
            throw new CommandException("Field Medics are only authorized to update status to CASUALTY.");
        }

        ModelManager manager = (ModelManager) model;
        String name = manager.getFilteredPersonnelList(null)
                .get(oneBasedIndex - 1).getName();
        manager.setPersonnelStatus(oneBasedIndex, newStatus);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, newStatus));
    }

    /** Medical officer and Field Medic. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER, Role.FIELD_MEDIC);
    }

    /** Returns the 1-based index for this command. */
    public int getOneBasedIndex() {
        return oneBasedIndex;
    }

    /** Returns the new status for this command. */
    public Status getNewStatus() {
        return newStatus;
    }
}