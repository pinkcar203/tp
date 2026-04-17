package meditrack.logic.commands;

import java.util.List;
import java.util.Objects;

import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Supply;
import meditrack.model.exceptions.InvalidIndexException;

/**
 * Removes a supply row by its index in the table (same numbering as the UI).
 * Field medic and logistics officer roles.
 */
public class DeleteSupplyCommand extends Command {

    public static final String MESSAGE_SUCCESS = "Deleted supply: %s";
    public static final String MESSAGE_INVALID_INDEX = "The supply index provided is out of bounds.";

    private final Index targetIndex;

    /**
     * @param targetIndex which row to delete (wrapped 0/1-based logic lives in {@link Index}).
     */
    public DeleteSupplyCommand(Index targetIndex) {
        this.targetIndex = Objects.requireNonNull(targetIndex);
    }

    /**
     * Calls {@link Model#deleteSupply(Index)}; bad index becomes a user-facing {@link CommandException}.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        try {
            Supply deleted = model.deleteSupply(targetIndex);
            return new CommandResult(String.format(MESSAGE_SUCCESS, deleted.getName()));
        } catch (InvalidIndexException e) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
    }

    /** Field medic or logistics officer. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER);
    }
}
