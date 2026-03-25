package meditrack.ui.modal;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.logic.parser.personnel.PersonnelParser;
import meditrack.model.Status;

/**
 * Modal dialog for adding a new personnel member.
 *
 * <p>Returns an {@link AddPersonnelCommand} via {@link #showAndWait()} when confirmed,
 * or {@link java.util.Optional#empty()} on cancel.
 *
 * <p>Inline validation prevents the dialog from closing on OK if:
 * <ul>
 *   <li>Name is blank</li>
 *   <li>No status is selected</li>
 * </ul>
 */
public class AddPersonnelModal extends Dialog<AddPersonnelCommand> {

    private final TextField nameField = new TextField();
    private final ComboBox<Status> statusCombo =
            new ComboBox<>(FXCollections.observableArrayList(Status.values()));
    private final Label validationLabel = new Label();

    /** Builds the add-personnel dialog (call {@link #showAndWait()}). */
    public AddPersonnelModal() {
        setTitle("Add Personnel");
        setHeaderText("Enter new personnel details");
        buildContent();
        wireResultConverter();
    }

    private void buildContent() {
        nameField.setPromptText("Full name");
        statusCombo.setValue(Status.PENDING); // default

        validationLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px;");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 24, 8, 24));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(validationLabel, 0, 2, 2, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(60);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setMinWidth(200);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Block close on OK if validation fails
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validate()) {
                event.consume();
            }
        });
    }

    private boolean validate() {
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            showError("Name must not be blank.");
            return false;
        }
        if (statusCombo.getValue() == null) {
            showError("Please select a status.");
            return false;
        }
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
        return true;
    }

    private void showError(String msg) {
        validationLabel.setText("⚠  " + msg);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }

    private void wireResultConverter() {
        setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            try {
                return PersonnelParser.parseAddPersonnel(
                        "n/" + nameField.getText().trim()
                                + " s/" + statusCombo.getValue().name());
            } catch (ParseException e) {
                return null; // already blocked by validate()
            }
        });
    }
}