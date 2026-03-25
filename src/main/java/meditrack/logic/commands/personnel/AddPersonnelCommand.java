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

/** Adds someone to the roster. Parser checks fields; model rejects duplicate names. */
public class AddPersonnelCommand extends Command {

    public static final String COMMAND_WORD = "add_personnel";

    public static final String MESSAGE_SUCCESS = "Added personnel: %s [%s]";
    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Adds a new personnel member to the roster.\n"
                    + "Parameters: n/NAME s/STATUS\n"
                    + "Example: " + COMMAND_WORD + " n/John Tan s/FIT";

    private final String name;
    private final Status status;

    /**
     * @param name personnel name (already validated when used from UI)
     * @param status status enum
     */
    public AddPersonnelCommand(String name, Status status) {
        this.name = name;
        this.status = status;
    }

    /** Adds the person to the roster. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Role currentRole = Session.getInstance().getRole();

        // Backend security check
        if (currentRole == Role.PLATOON_COMMANDER && status != Status.PENDING) {
            throw new CommandException("Platoon Commanders can only add personnel with PENDING status.");
        }

        ModelManager manager = (ModelManager) model;
        manager.addPersonnel(name, status);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, status));
    }

    /** Medical officer and Platoon Commander. */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER, Role.PLATOON_COMMANDER);
    }

    /** Returns the name passed to the constructor. */
    public String getName() {
        return name;
    }

    /** Returns the status passed to the constructor. */
    public Status getStatus() {
        return status;
    }
}