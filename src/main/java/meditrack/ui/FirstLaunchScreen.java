package meditrack.ui;

import java.io.IOException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import meditrack.security.PasswordManager;
import meditrack.storage.JsonMediTrackStorage;
import meditrack.storage.JsonSerializableMediTrack;

/**
 * Represents the UI screen displayed when the application is launched for the very first time.
 * Handles the creation and secure storage of the master application password.
 */
public class FirstLaunchScreen extends VBox {

    private final Runnable onSetupComplete;
    private final JsonMediTrackStorage storageEngine;

    /**
     * Constructs the First Launch setup screen.
     *
     * @param onSetupComplete A callback function to execute once the password is successfully saved (usually transitions to Login).
     */
    public FirstLaunchScreen(Runnable onSetupComplete) {
        this.onSetupComplete = onSetupComplete;
        this.storageEngine = new JsonMediTrackStorage();
        initializeUI();
    }

    /**
     * Initializes and arranges the JavaFX components for this screen.
     */
    private void initializeUI() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(15);
        this.setPadding(new Insets(40));

        Label titleLabel = new Label("Welcome to MediTrack\nPlease set a master password:");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-alignment: center;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new master password");
        passwordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button confirmButton = new Button("Set Password");
        confirmButton.setOnAction(e -> handlePasswordSetup(passwordField.getText(), errorLabel));

        this.getChildren().addAll(titleLabel, passwordField, confirmButton, errorLabel);
    }

    /**
     * Handles the logic when the user clicks the Confirm button.
     * Hashes the password and initializes the data.json file.
     *
     * @param plainTextPassword The password entered by the user.
     * @param errorLabel The label used to display validation errors.
     */
    private void handlePasswordSetup(String plainTextPassword, Label errorLabel) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        try {
            // Hash the password
            String hash = PasswordManager.hashPassword(plainTextPassword);

            // Create the initial empty database with the new password hash
            JsonSerializableMediTrack initialData = new JsonSerializableMediTrack(hash, null, null);
            storageEngine.saveData(initialData);

            // Trigger the callback to switch to the Login screen
            onSetupComplete.run();
        } catch (IOException ex) {
            errorLabel.setText("Failed to save data: " + ex.getMessage());
        }
    }
}