package meditrack;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import meditrack.model.MediTrack;
import meditrack.model.ReadOnlyMediTrack;
import meditrack.storage.StorageManager;
import meditrack.ui.FirstLaunchScreen;
import meditrack.ui.LoginScreen;
import meditrack.ui.MainAppScreen;
import java.util.Optional;
/**
 * The main entry point for the MediTrack JavaFX application.
 * Manages the primary stage and controls the flow between setup, login, and the main app.
 */
public class Main extends Application {

    private Stage primaryStage;
    private final StorageManager storageManager = new StorageManager();

    // Loaded once from disk; shared across the lifetime of the session
    private MediTrack mediTrack;

    /**
     * The main entry point for all JavaFX applications.
     *
     * @param primaryStage The primary window for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("MediTrack");
        primaryStage.setWidth(900);
        primaryStage.setHeight(620);

        // Load existing data (if any) at startup
        Optional<ReadOnlyMediTrack> loaded = storageManager.readMediTrackData();
        mediTrack = loaded.isPresent()
                ? (MediTrack) loaded.get()
                : new MediTrack();

        if (storageManager.isFirstLaunch()) {
            showFirstLaunchScreen();
        } else {
            showLoginScreen();
        }

        primaryStage.show();
    }

    /**
     * Displays the First Launch Setup screen.
     */
    private void showFirstLaunchScreen() {
        FirstLaunchScreen setupScreen = new FirstLaunchScreen(this::showLoginScreen);
        primaryStage.setScene(new Scene(setupScreen));
    }

    /**
     * Displays the Login screen.
     */
    private void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(this::showMainAppScreen);
        primaryStage.setScene(new Scene(loginScreen));
    }

    /**
     * Displays the Main Application screen after a successful login.
     * with the full {@link MainAppScreen} (sidebar + personnel screens).
     */
    private void showMainAppScreen() {
        MainAppScreen mainApp = new MainAppScreen(mediTrack, storageManager, this::showLoginScreen);
        primaryStage.setScene(new Scene(mainApp, 900, 620));
    }

    /**
     * The standard Java main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}