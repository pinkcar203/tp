package meditrack.logic.commands;

import java.util.Objects;
import java.util.List;
import meditrack.commons.core.Index;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Supply;
import meditrack.model.exceptions.InvalidIndexException;

/**
 * Deletes a supply from the inventory by its displayed index.
 * Only the Field Medic role can run this command.
 */
public class DeleteSupplyCommand extends Command {

    public static final String MESSAGE_SUCCESS = "Deleted supply: %s";
    public static final String MESSAGE_INVALID_INDEX = "The supply index provided is out of bounds.";

    private final Index targetIndex;

    /** @param targetIndex index of the row to delete */
    public DeleteSupplyCommand(Index targetIndex) {
        this.targetIndex = Objects.requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        try {
            Supply deleted = model.deleteSupply(targetIndex);
            return new CommandResult(String.format(MESSAGE_SUCCESS, deleted.getName()));
        } catch (InvalidIndexException e) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
    }

    /** Field medic only. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER);
    }
}
