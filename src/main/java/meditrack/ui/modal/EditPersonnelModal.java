package meditrack.ui.modal;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import meditrack.logic.Logic;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.EditPersonnelCommand;
import meditrack.model.BloodGroup;
import meditrack.model.Model;
import meditrack.model.Personnel;

/**
 * MO edits blood group + allergies for one roster row ({@code index} is 1-based for {@link EditPersonnelCommand}).
 */
public class EditPersonnelModal {
    public static void show(Model model, Logic logic, Personnel personnel, int index, Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("EDIT MEDICAL DETAILS");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1a1c18;");

        Label title = new Label("UPDATE: " + personnel.getName().toUpperCase());
        title.setStyle("-fx-text-fill: #e3e3dc; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        // Styled Blood Group Label
        Label bgLabel = new Label("BLOOD GROUP:");
        bgLabel.setStyle("-fx-text-fill: #8f9284; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        ComboBox<BloodGroup> bgBox = new ComboBox<>(FXCollections.observableArrayList(BloodGroup.values()));
        bgBox.setValue(personnel.getBloodGroup());
        bgBox.setStyle("-fx-background-color: #292b26; -fx-border-color: #45483c; -fx-border-width: 1; -fx-font-family: 'Consolas', monospace;");

        // Force the selected text inside the ComboBox to be bright and readable
        bgBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BloodGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #e3e3dc; -fx-font-family: 'Consolas', monospace;");
                }
            }
        });

        // Styled Allergies Label
        Label algLabel = new Label("ALLERGIES:");
        algLabel.setStyle("-fx-text-fill: #8f9284; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        TextField allergiesField = new TextField(personnel.getAllergies());
        allergiesField.setPromptText("Enter allergies (or NONE)");
        allergiesField.setStyle("-fx-background-color: #292b26; -fx-text-fill: #e3e3dc; -fx-border-color: #45483c; -fx-border-width: 1; -fx-font-family: 'Consolas', monospace;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ffb4ab; -fx-font-size: 11px;");

        Button saveBtn = new Button("SAVE");
        saveBtn.setStyle("-fx-background-color: #b6d088; -fx-text-fill: #233600; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                String newAllergies = allergiesField.getText().isBlank() ? "NONE" : allergiesField.getText().trim().toUpperCase();
                EditPersonnelCommand cmd = new EditPersonnelCommand(index, bgBox.getValue(), newAllergies);
                logic.executeCommand(cmd);
                stage.close();
            } catch (CommandException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        HBox btnBox = new HBox(10, saveBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        // Replaced inline labels with our newly styled labels
        layout.getChildren().addAll(title, bgLabel, bgBox, algLabel, allergiesField, errorLabel, btnBox);

        Scene scene = new Scene(layout, 350, 310);
        stage.setScene(scene);
        stage.showAndWait();
    }
}