package meditrack.logic.commands.exceptions;

/**
 * Represents an error that occurs during the execution of a command.
 * This is typically thrown when business rules or role-based access permissions are violated
 * (e.g., a Field Medic trying to update a medical status they are not authorized to change).
 */
public class CommandException extends Exception {

    /**
     * Constructs a new {@code CommandException} with the specified detail message.
     *
     * @param message The specific detail message explaining the cause of the exception.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CommandException} with the specified detail message and cause.
     *
     * @param message The specific detail message explaining the cause of the exception.
     * @param cause The underlying cause of the exception.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}