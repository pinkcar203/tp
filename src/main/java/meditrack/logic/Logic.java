package meditrack.logic;

import meditrack.logic.commands.Command;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;

/**
 * API of the Logic component.
 * Defines the contract for the main execution engine of the application.
 */
public interface Logic {
    /**
     * Executes the specified command and returns the result.
     *
     * @param command The command to execute.
     * @return The result of the command execution, containing feedback for the user.
     * @throws CommandException If an error occurs during command execution (e.g., unauthorized access).
     */
    CommandResult executeCommand(Command command) throws CommandException;
}