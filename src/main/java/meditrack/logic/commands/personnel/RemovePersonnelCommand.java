package meditrack.logic.commands.personnel;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;
import java.util.List;

/** Removes a roster entry by 1-based index. */
public class RemovePersonnelCommand extends Command {

    public static final String COMMAND_WORD = "remove_personnel";

    public static final String MESSAGE_SUCCESS = "Removed personnel: %s";
    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Removes a personnel member by their list index.\n"
                    + "Parameters: INDEX (must be a positive integer)\n"
                    + "Example: " + COMMAND_WORD + " 2";

    private final int oneBasedIndex;

    /** @param oneBasedIndex row number */
    public RemovePersonnelCommand(int oneBasedIndex) {
        this.oneBasedIndex = oneBasedIndex;
    }

    /** Removes the person at the stored index. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        ModelManager manager = (ModelManager) model;
        Personnel removed = manager.deletePersonnel(oneBasedIndex);
        return new CommandResult(String.format(MESSAGE_SUCCESS, removed.getName()));
    }

    /** Medical officer only. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER);
    }

    /** Returns the 1-based index for this command. */
    public int getOneBasedIndex() {
        return oneBasedIndex;
    }
}