package meditrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single personnel member on the roster.
 * Personnel uniqueness is determined strictly by their name (case-insensitive).
 */
public class Personnel {

    private final String name;
    private Status status;
    private BloodGroup bloodGroup;
    private String allergies;
    private LocalDateTime lastModified;
    private LocalDate statusExpiryDate;

    /**
     * Constructs a Personnel record with no blood group or allergies recorded.
     *
     * @param name   The display name of the personnel. Must not be null or blank.
     * @param status The initial medical readiness status. Must not be null.
     */
    public Personnel(String name, Status status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Personnel name must not be blank.");
        }
        Objects.requireNonNull(status, "Status must not be null.");
        this.name = name.trim();
        this.status = status;
        this.bloodGroup = null;
        this.allergies = "";
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Constructs a comprehensive Personnel record.
     *
     * @param name       The display name of the personnel. Must not be null or blank.
     * @param status     The initial medical readiness status. Must not be null.
     * @param bloodGroup The blood group of the personnel, or null if unknown.
     * @param allergies  Known allergies, or null/empty if none.
     */
    public Personnel(String name, Status status, BloodGroup bloodGroup, String allergies) {
        this(name, status);
        this.bloodGroup = bloodGroup;
        this.allergies = (allergies == null) ? "" : allergies.trim();
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Constructs a comprehensive Personnel record used for editing or duplicating an existing entry.
     * This constructor retains historical tracking data such as the expiry date and modification timestamp.
     *
     * @param name             The display name of the personnel. Must not be null or blank.
     * @param status           The current medical readiness status. Must not be null.
     * @param bloodGroup       The blood group of the personnel, or null if unknown.
     * @param allergies        Known allergies, or null/empty if none.
     * @param statusExpiryDate The date the current medical status expires, or null if indefinite.
     * @param lastModified     The timestamp of when this record was last modified.
     */
    public Personnel(String name, Status status, BloodGroup bloodGroup, String allergies,
                     LocalDate statusExpiryDate, LocalDateTime lastModified) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Personnel name must not be blank.");
        }
        Objects.requireNonNull(status, "Status must not be null.");
        this.name = name.trim();
        this.status = status;
        this.bloodGroup = bloodGroup;
        this.allergies = (allergies == null) ? "" : allergies.trim();
        this.statusExpiryDate = statusExpiryDate;
        this.lastModified = lastModified;
    }

    /**
     * Retrieves the personnel's name.
     *
     * @return The trimmed name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the current medical readiness status.
     *
     * @return The active Status enum.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Retrieves the personnel's blood group.
     *
     * @return The BloodGroup enum, or null if not recorded.
     */
    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    /**
     * Retrieves the personnel's known allergies.
     *
     * @return A string detailing allergies, or an empty string if none.
     */
    public String getAllergies() {
        return allergies;
    }

    /**
     * Retrieves the timestamp of the last modification to this profile.
     *
     * @return The LocalDateTime of the last update.
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Retrieves the expiration date for the current medical status.
     *
     * @return The LocalDate when the status expires, or null if indefinite.
     */
    public LocalDate getStatusExpiryDate() {
        return statusExpiryDate;
    }

    /**
     * Sets the expiration date for the current medical status and updates the modified timestamp.
     *
     * @param statusExpiryDate The date the status should expire.
     */
    public void setStatusExpiryDate(LocalDate statusExpiryDate) {
        this.statusExpiryDate = statusExpiryDate;
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Updates the medical readiness status and updates the modified timestamp.
     *
     * @param status The new Status enum to apply.
     */
    public void setStatus(Status status) {
        Objects.requireNonNull(status, "Status must not be null.");
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Sets the blood group and updates the modified timestamp.
     *
     * @param bloodGroup The BloodGroup to assign, or null if unknown.
     */
    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Sets the known allergies and updates the modified timestamp.
     *
     * @param allergies The allergy description. Null is safely converted to an empty string.
     */
    public void setAllergies(String allergies) {
        this.allergies = (allergies == null) ? "" : allergies.trim();
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Checks if the personnel is ready for deployment.
     *
     * @return {@code true} if the status is FIT, {@code false} otherwise.
     */
    public boolean isDeployable() {
        return status == Status.FIT;
    }

    /**
     * Sets the last modified timestamp directly.
     * Primarily used by the Storage layer when loading historical saved data.
     *
     * @param lastModified The historical timestamp to restore.
     */
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Checks equality based strictly on a case-insensitive name match.
     *
     * @param obj The object to compare against.
     * @return {@code true} if both objects are Personnel with matching names.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Personnel)) {
            return false;
        }
        Personnel other = (Personnel) obj;
        return this.name.equalsIgnoreCase(other.name);
    }

    /**
     * Generates a hash code based on the lowercased name.
     *
     * @return The integer hash code.
     */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    /**
     * Returns a string representation of the personnel for display.
     *
     * @return A formatted string showing name and status.
     */
    @Override
    public String toString() {
        return name + " [" + status + "]";
    }
}