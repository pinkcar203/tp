package meditrack;

/**
 * A launcher class to bypass JavaFX 11+ module path requirements.
 * This class does not extend javafx.application.Application, allowing the
 * standard Java launcher to start the application without missing component errors.
 */
public class Launcher {

    /**
     * The main entry point that acts as a proxy to the actual JavaFX Application.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}