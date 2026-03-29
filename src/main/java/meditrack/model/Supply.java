package meditrack.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a medical supply item in the inventory.
 */
public class Supply {

    private final String name;
    private final int quantity;
    private final LocalDate expiryDate;

    /**
     * @param name supply name (must not be null)
     * @param quantity stock count
     * @param expiryDate expiry date (must not be null)
     */
    public Supply(String name, int quantity, LocalDate expiryDate) {
        this.name = Objects.requireNonNull(name);
        this.quantity = quantity;
        this.expiryDate = Objects.requireNonNull(expiryDate);
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    // equality is based on name only (case-insensitive) for duplicate detection
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Supply)) {
            return false;
        }
        Supply otherSupply = (Supply) other;
        return name.equalsIgnoreCase(otherSupply.name);
    }

    /** Hash from lowercased name. */
    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    /** String with all fields. */
    @Override
    public String toString() {
        return String.format("Supply{name='%s', quantity=%d, expiryDate=%s}", name, quantity, expiryDate);
    }
}