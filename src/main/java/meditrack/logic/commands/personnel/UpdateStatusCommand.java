package meditrack.logic.commands.personnel;

import java.time.LocalDate;
import java.util.List;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Status;

/**
 * Updates the medical status of a specific personnel in the roster.
 * Supports attaching an optional medical leave duration (in days) which automatically calculates an expiry date.
 */
public class UpdateStatusCommand extends Command {

    public static final String COMMAND_WORD = "update_status";

    public static final String MESSAGE_SUCCESS = "Updated status of %s to: %s";
    public static final String MESSAGE_SUCCESS_WITH_DURATION = "Updated status of %s to: %s (Expires: %s)";

    private final int oneBasedIndex;
    private final Status newStatus;
    private final int durationDays;

    /**
     * Constructs an UpdateStatusCommand with no expiration duration (defaults to 0 days).
     *
     * @param oneBasedIndex The 1-based index of the personnel in the filtered roster list.
     * @param newStatus     The new medical status to assign to the personnel.
     */
    public UpdateStatusCommand(int oneBasedIndex, Status newStatus) {
        this(oneBasedIndex, newStatus, 0);
    }

    /**
     * Constructs an UpdateStatusCommand with a specified expiration duration.
     *
     * @param oneBasedIndex The 1-based index of the personnel in the filtered roster list.
     * @param newStatus     The new medical status to assign to the personnel.
     * @param durationDays  The number of days the medical status should remain active.
     */
    public UpdateStatusCommand(int oneBasedIndex, Status newStatus, int durationDays) {
        this.oneBasedIndex = oneBasedIndex;
        this.newStatus = newStatus;
        this.durationDays = durationDays;
    }

    /**
     * Executes the command to update the personnel's status and expiry date.
     * Enforces Role-Based Access Control (RBAC) to ensure Field Medics can only assign the CASUALTY status.
     *
     * @param model The application model interface.
     * @return A CommandResult containing the success message to display to the user.
     * @throws CommandException If the index is invalid or the user lacks the required authorization.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        Role currentRole = model.getSession().getRole();

        if (currentRole == Role.FIELD_MEDIC && newStatus != Status.CASUALTY) {
            throw new CommandException("Field Medics are only authorized to update status to CASUALTY.");
        }

        List<Personnel> roster = model.getFilteredPersonnelList(null);
        if (oneBasedIndex < 1 || oneBasedIndex > roster.size()) {
            throw new CommandException("The personnel index provided is invalid.");
        }

        Personnel person = roster.get(oneBasedIndex - 1);
        String name = person.getName();

        model.setPersonnelStatus(oneBasedIndex, newStatus);

        if (durationDays > 0 && (newStatus == Status.MC || newStatus == Status.LIGHT_DUTY)) {
            LocalDate expiryDate = LocalDate.now(model.getClock()).plusDays(durationDays);
            person.setStatusExpiryDate(expiryDate);
            return new CommandResult(String.format(MESSAGE_SUCCESS_WITH_DURATION, name, newStatus, expiryDate));
        }

        person.setStatusExpiryDate(null);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, newStatus));
    }

    /**
     * Retrieves the list of roles authorized to execute this command.
     *
     * @return A list containing Role.MEDICAL_OFFICER and Role.FIELD_MEDIC.
     */
    @Override
    public List<Role> getRequiredRoles() {
        return List.of(Role.MEDICAL_OFFICER, Role.FIELD_MEDIC);
    }

    /**
     * Retrieves the target 1-based index.
     *
     * @return The 1-based index of the personnel.
     */
    public int getOneBasedIndex() {
        return oneBasedIndex;
    }

    /**
     * Retrieves the target status.
     *
     * @return The new status to be applied.
     */
    public Status getNewStatus() {
        return newStatus;
    }
}
