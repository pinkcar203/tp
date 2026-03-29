package meditrack.logic.commands;

import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Supply;

/**
 * Adds a new supply item to the inventory.
 * Field Medic and Logistics Officer roles can run this command.
 */
public class AddSupplyCommand extends Command {

    public static final String MESSAGE_SUCCESS = "New supply added: %s (Qty: %d, Expiry: %s)";

    private final String name;
    private final int quantity;
    private final LocalDate expiryDate;

    /**
     * @param name supply name
     * @param quantity quantity to add
     * @param expiryDate expiry date
     */
    public AddSupplyCommand(String name, int quantity, LocalDate expiryDate) {
        this.name = Objects.requireNonNull(name);
        this.quantity = quantity;
        this.expiryDate = Objects.requireNonNull(expiryDate);
    }

    /** Adds the supply to the model. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Supply supply = new Supply(name, quantity, expiryDate);
        model.addSupply(supply);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, quantity, expiryDate));
    }

    /** Field medic only. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER);
    }
}
