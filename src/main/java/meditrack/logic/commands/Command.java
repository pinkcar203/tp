package meditrack.logic.commands;

import java.util.List;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;
import meditrack.model.Role;

/**
 * Abstract base class for all commands in the application.
 * Each concrete command must implement execute() and getRequiredRole().
 */
public abstract class Command {

    /**
     * Runs the command against the model and returns a result message.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns a list of roles allowed to run this command, or an empty list/null if anyone can run it.
     */
    public abstract List<Role> getRequiredRoles();
}