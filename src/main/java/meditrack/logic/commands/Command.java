package meditrack.logic.commands;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 * All specific user actions (e.g., AddSupplyCommand, UpdateStatusCommand) must extend this class.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model The {@code Model} which the command should operate on.
     * @return A {@code CommandResult} object containing the feedback message of the operation result.
     * @throws CommandException If an error occurs during command execution or if role-based constraints are violated.
     */
    public abstract CommandResult execute(Model model) throws CommandException;
}