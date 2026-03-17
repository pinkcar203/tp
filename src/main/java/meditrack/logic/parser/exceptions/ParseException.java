package meditrack.logic.parser.exceptions;

/**
 * Represents an error that occurs during the validation of user inputs by the Parser.
 * This is typically thrown when user input violates format or logical constraints
 * (e.g., entering a negative quantity, a blank name, or an invalid date).
 */
public class ParseException extends Exception {

    /**
     * Constructs a new {@code ParseException} with the specified detail message.
     *
     * @param message The specific detail message explaining why the input validation failed.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ParseException} with the specified detail message and cause.
     *
     * @param message The specific detail message explaining why the input validation failed.
     * @param cause The underlying cause of the exception.
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}