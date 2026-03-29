package meditrack.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One person on the roster. Same name (case-insensitive) = duplicate.
 */
public class Personnel {

    private final String name;
    private Status status;
    private BloodGroup bloodGroup;
    private String allergies;
    private LocalDateTime lastModified;

    /**
     * Constructs a Personnel record with no blood group or allergies recorded.
     *
     * @param name   display name
     * @param status initial medical readiness status
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

    /** Full constructor. Blood group can be null if unknown. */
    public Personnel(String name, Status status, BloodGroup bloodGroup, String allergies) {
        this(name, status);
        this.bloodGroup = bloodGroup;
        this.allergies = (allergies == null) ? "" : allergies.trim();
        this.lastModified = LocalDateTime.now();
    }

    /** Returns the personnel name. */
    public String getName() {
        return name;
    }

    /** Returns the current status. */
    public Status getStatus() {
        return status;
    }

    /** Returns the blood group, or null if not recorded. */
    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    /** Returns known allergies, or an empty string if none recorded. */
    public String getAllergies() {
        return allergies;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /** Updates the medical readiness status of this personnel member. */
    public void setStatus(Status status) {
        Objects.requireNonNull(status, "Status must not be null.");
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }

    /** Sets the blood group; null means not recorded. */
    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
        this.lastModified = LocalDateTime.now();
    }

    /** Sets the known allergies description. Null is treated as empty. */
    public void setAllergies(String allergies) {
        this.allergies = (allergies == null) ? "" : allergies.trim();
        this.lastModified = LocalDateTime.now();
    }

    /** Returns true if this personnel member is deployable (status is FIT). */
    public boolean isDeployable() {
        return status == Status.FIT;
    }

    /**
     * Sets the last modified timestamp directly.
     * Primarily used by the Storage layer when loading saved data.
     * * @param lastModified The historical timestamp to restore.
     */
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns true if obj is a personnel with the same name (case-insensitive).
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

    /** Hash from lowercased name. */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    /** Name and status for display. */
    @Override
    public String toString() {
        return name + " [" + status + "]";
    }
}