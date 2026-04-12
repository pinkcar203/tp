package meditrack.model;

/**
 * Manages the active user session within the application.
 */
public class Session {

    private Role currentRole;

    /**
     * Constructs a new Session instance.
     * Handled natively by the ModelManager to ensure only one session exists per application lifecycle.
     */
    public Session() {
    }

    /**
     * Assigns the active operational role to the current session upon successful login.
     *
     * @param role The Role to assign to the current user.
     */
    public void setRole(Role role) {
        this.currentRole = role;
    }

    /**
     * Retrieves the role associated with the current session.
     *
     * @return The active Role, or null if no user is currently logged in.
     */
    public Role getRole() {
        return currentRole;
    }

    /**
     * Clears the current session data, effectively logging the user out and revoking permissions.
     */
    public void clear() {
        this.currentRole = null;
    }
}