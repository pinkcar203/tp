package meditrack.logic.commands.exceptions;

/**
 * Represents an error that occurs during the execution of a command.
 */
public class CommandException extends Exception {

    /**
     * Constructs a new CommandException with the specified detail message.
     *
     * @param message The specific detail message explaining the cause of the exception.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new CommandException with the specified detail message and cause.
     *
     * @param message The specific detail message explaining the cause of the exception.
     * @param cause The underlying cause of the exception.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}