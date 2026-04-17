package meditrack.logic.commands.personnel;

import java.util.List;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Role;

/**
 * Removes a personnel member from the roster using their displayed 1-based index.
 * Permitted for Medical Officers and Platoon Commanders.
 */
public class RemovePersonnelCommand extends Command {

    public static final String COMMAND_WORD = "remove_personnel";

    public static final String MESSAGE_SUCCESS = "Removed personnel: %s";
    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Removes a personnel member by their list index.\n"
                    + "Parameters: INDEX (must be a positive integer)\n"
                    + "Example: " + COMMAND_WORD + " 2";

    private final int oneBasedIndex;

    /**
     * Constructs a command to remove a personnel member.
     *
     * @param oneBasedIndex The 1-based row number displayed in the UI.
     */
    public RemovePersonnelCommand(int oneBasedIndex) {
        this.oneBasedIndex = oneBasedIndex;
    }

    /**
     * Executes the command to permanently delete the personnel from the system.
     *
     * @param model The application model interface.
     * @return A CommandResult containing the name of the removed personnel.
     * @throws CommandException If the index provided is out of bounds.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Personnel removed = model.deletePersonnel(oneBasedIndex);
        return new CommandResult(String.format(MESSAGE_SUCCESS, removed.getName()));
    }

    /**
     * Retrieves the list of roles authorized to execute this command.
     *
     * @return Medical Officers and Platoon Commanders.
     */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER, Role.PLATOON_COMMANDER);
    }

    /**
     * Retrieves the target 1-based index.
     *
     * @return The 1-based index integer.
     */
    public int getOneBasedIndex() {
        return oneBasedIndex;
    }
}
