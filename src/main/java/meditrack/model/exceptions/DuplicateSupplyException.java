package meditrack.model.exceptions;

/**
 * Thrown when trying to add a supply whose name already exists.
 */
public class DuplicateSupplyException extends RuntimeException {
    /** Creates an exception with a default message. */
    public DuplicateSupplyException() {
        super("A supply with this name already exists in the inventory.");
    }
}
