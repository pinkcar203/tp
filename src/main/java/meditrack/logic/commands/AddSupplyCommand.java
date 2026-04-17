package meditrack.logic.commands;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Supply;

/**
 * Adds one supply row to the inventory table.
 * Field medics and logistics officers can use this (see module scope).
 */
public class AddSupplyCommand extends Command {

    public static final String MESSAGE_SUCCESS = "New supply added: %s (Qty: %d, Expiry: %s)";

    private final String name;
    private final int quantity;
    private final LocalDate expiryDate;

    /**
     * @param name       item name (e.g. bandages)
     * @param quantity   how many units
     * @param expiryDate best-before date
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

    /** Field medic or logistics officer. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.FIELD_MEDIC, Role.LOGISTICS_OFFICER);
    }
}
