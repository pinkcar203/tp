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
 * Overwrites an existing supply at a list index with new name/qty/expiry from the modal.
 */
public class EditSupplyCommand extends Command {

    public static final String MESSAGE_SUCCESS = "Supply at index %d updated to: %s (Qty: %d, Expiry: %s)";
    public static final String MESSAGE_INVALID_INDEX = "The supply index provided is out of bounds.";

    private final Index targetIndex;
    private final Supply editedSupply;

    /**
     * @param targetIndex  row to change (Index type keeps 0-based vs 1-based straight)
     * @param editedSupply   new field values to save
     */
    public EditSupplyCommand(Index targetIndex, Supply editedSupply) {
        this.targetIndex = Objects.requireNonNull(targetIndex);
        this.editedSupply = Objects.requireNonNull(editedSupply);
    }

    /**
     * Updates the model; out-of-range index is turned into a normal {@link CommandException} message.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        try {
            model.editSupply(targetIndex, editedSupply);
        } catch (InvalidIndexException e) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                targetIndex.getOneBased(),
                editedSupply.getName(),
                editedSupply.getQuantity(),
                editedSupply.getExpiryDate()));
    }

    /** Field medic or logistics officer. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER);
    }
}
