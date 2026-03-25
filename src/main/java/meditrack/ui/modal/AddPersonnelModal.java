package meditrack.ui.modal;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.logic.parser.personnel.PersonnelParser;
import meditrack.model.Status;
import meditrack.model.Session;
import meditrack.model.Role;

/**
 * Modal dialog for adding a new personnel member.
 *
 * <p>Returns an {@link AddPersonnelCommand} via {@link #showAndWait()} when confirmed,
 * or {@link java.util.Optional#empty()} on cancel.
 *
 * <p>Inline validation prevents the dialog from closing on OK if:
 * <ul>
 * <li>Name is blank</li>
 * <li>No status is selected</li>
 * </ul>
 */
public class AddPersonnelModal extends Dialog<AddPersonnelCommand> {

    private final TextField nameField = new TextField();
    private final ComboBox<Status> statusCombo = new ComboBox<>();
    private final Label validationLabel = new Label();

    /**
     * Builds the add-personnel dialog.
     * Call {@link #showAndWait()} to display it to the user.
     */
    public AddPersonnelModal() {
        setTitle("Add Personnel");
        setHeaderText("Enter new personnel details");
        buildContent();
        wireResultConverter();
    }

    /**
     * Initializes and arranges the UI components within the dialog pane.
     * Sets up the grid layout, input fields, and the event filter to block
     * the dialog from closing if validation fails.
     */
    private void buildContent() {
        nameField.setPromptText("Full name");

        Role currentRole = Session.getInstance().getRole();
        if (currentRole == Role.PLATOON_COMMANDER) {
            statusCombo.setItems(FXCollections.observableArrayList(Status.PENDING));
            statusCombo.setValue(Status.PENDING);
            statusCombo.setDisable(true);
        } else {
            statusCombo.setItems(FXCollections.observableArrayList(Status.values()));
            statusCombo.setValue(Status.PENDING);
        }

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

    /**
     * Validates the user's input before allowing the dialog to be submitted.
     * Checks that the name field is not blank and a status is selected.
     *
     * @return true if the inputs are valid, false otherwise.
     */
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

    /**
     * Displays an inline validation error message within the dialog.
     *
     * @param msg The error message to display to the user.
     */
    private void showError(String msg) {
        validationLabel.setText("⚠  " + msg);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }

    /**
     * Configures the result converter to translate a successful dialog submission
     * (clicking the OK button) into an {@link AddPersonnelCommand}.
     * If the user cancels or parsing fails, it returns null.
     */
    private void wireResultConverter() {
        setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }

            Status finalStatus = statusCombo.getValue();
            if (Session.getInstance().getRole() == Role.PLATOON_COMMANDER) {
                finalStatus = Status.PENDING;
            }

            try {
                return PersonnelParser.parseAddPersonnel(
                        "n/" + nameField.getText().trim()
                                + " s/" + finalStatus.name());
            } catch (ParseException e) {
                System.out.println("Parser Failed: " + e.getMessage());
                return null;
            }
        });
    }
}