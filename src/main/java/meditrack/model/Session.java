package meditrack.model;

/**
 * Holds who is logged in (role only).
 */
public class Session {
    private static Session instance;
    private Role currentRole;

    private Session() {
    }

    /**
     * Lazy singleton — call this instead of new Session().
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /** Called after a successful login. */
    public void setRole(Role role) {
        this.currentRole = role;
    }

    /** Null if nobody is logged in yet. */
    public Role getRole() {
        return currentRole;
    }

    /** Logout / reset session. */
    public void clear() {
        this.currentRole = null;
    }
}
