package meditrack.logic.parser.exceptions;

/**
 * Represents an error that occurs during the validation of user inputs.
 */
public class ParseException extends Exception {

    /**
     * Constructs a new ParseException with the specified detail message.
     *
     * @param message The specific detail message explaining why the input validation failed.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new ParseException with the specified detail message and cause.
     *
     * @param message The specific detail message explaining why the input validation failed.
     * @param cause The underlying cause of the exception.
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}