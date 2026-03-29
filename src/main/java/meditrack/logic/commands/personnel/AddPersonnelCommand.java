package meditrack.logic.commands.personnel;

import java.util.List;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.BloodGroup;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.model.Status;

/** Adds someone to the roster. */
public class AddPersonnelCommand extends Command {

    public static final String COMMAND_WORD = "add_personnel";

    public static final String MESSAGE_SUCCESS = "Added personnel: %s [%s]";
    public static final String MESSAGE_USAGE =
            COMMAND_WORD + ": Adds a new personnel member to the roster.\n"
                    + "Parameters: n/NAME s/STATUS\n"
                    + "Example: " + COMMAND_WORD + " n/John Tan s/FIT";

    private final String name;
    private final Status status;
    private final BloodGroup bloodGroup;
    private final String allergies;

    /**
     * Creates a command with name and status only.
     *
     * @param name   personnel name
     * @param status status enum
     */
    public AddPersonnelCommand(String name, Status status) {
        this(name, status, null, "");
    }

    /**
     * Creates a command with full medical profile fields.
     *
     * @param name       personnel name
     * @param status     status enum
     * @param bloodGroup ABO+Rh classification
     * @param allergies  known allergies description
     */
    public AddPersonnelCommand(String name, Status status, BloodGroup bloodGroup, String allergies) {
        this.name       = name;
        this.status     = status;
        this.bloodGroup = bloodGroup;
        this.allergies  = (allergies == null) ? "" : allergies;
    }

    /** Adds the person to the roster. */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Role currentRole = Session.getInstance().getRole();

        if (currentRole == Role.PLATOON_COMMANDER && status != Status.PENDING) {
            throw new CommandException("Platoon Commanders can only add personnel with PENDING status.");
        }

        model.addPersonnel(name, status, bloodGroup, allergies);
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

    /** Returns the blood group, or {@code null} if not recorded. */
    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    /** Returns the allergies string. */
    public String getAllergies() {
        return allergies;
    }
}
