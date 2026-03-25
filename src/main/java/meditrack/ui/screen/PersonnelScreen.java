package meditrack.ui.screen;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import meditrack.logic.commands.CommandResult;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.UpdateStatusCommand;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.model.Status;
import meditrack.storage.StorageManager;
import meditrack.ui.modal.AddPersonnelModal;
import meditrack.ui.modal.RemovePersonnelModal;
import java.io.IOException;
import java.util.List;

/**
 * Represents the Personnel Management UI screen.
 * <p>
 * Display and interaction are dynamically adjusted based on the current user's role:
 * <ul>
 * <li><b>Medical Officer:</b> Full access to edit the table, add/remove personnel, and update statuses inline.</li>
 * <li><b>Other Roles:</b> Read-only access to view the roster.</li>
 * </ul>
 */
public class PersonnelScreen extends VBox {

    private final ModelManager model;
    private final StorageManager storage;
    private final boolean readOnly;

    private final ObservableList<Personnel> tableData = FXCollections.observableArrayList();
    private final TableView<Personnel> table = new TableView<>(tableData);
    private final Label statusLabel = new Label();

    /**
     * Constructs the PersonnelScreen.
     *
     * @param model   The data model managing personnel records.
     * @param storage The storage manager used to persist data changes after edits.
     */
    public PersonnelScreen(ModelManager model, StorageManager storage) {
        this.model = model;
        this.storage = storage;
        this.readOnly = Session.getInstance().getRole() != Role.MEDICAL_OFFICER;
        buildUi();
        refresh();
    }

    /**
     * Initializes and arranges all JavaFX components for this screen.
     * Applies the layout fix to ensure the Add button stacks vertically beneath the title.
     */
    private void buildUi() {
        setSpacing(12);
        setPadding(new Insets(20));

        // Header
        Label title = new Label(readOnly ? "Personnel (Read-Only)" : "Personnel Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        getChildren().add(title);

        if (!readOnly) {
            Button addBtn = new Button("+ Add Personnel");
            addBtn.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 5;");
            addBtn.setOnAction(e -> openAddModal());
            getChildren().add(addBtn);
        }

        // Table
        buildTable();

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_RIGHT);
        if (!readOnly) {
            Button removeBtn = new Button("Remove Selected");
            removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 5;");
            removeBtn.setOnAction(e -> openRemoveModal());
            footer.getChildren().add(removeBtn);
        }

        statusLabel.setStyle("-fx-font-size: 12px;");

        getChildren().addAll(table, footer, statusLabel);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    /**
     * Constructs the table columns, cell factories, and data binding.
     * Injects an interactive ComboBox into the Status column for authorized roles.
     */
    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Personnel, String> indexCol = new TableColumn<>("#");
        indexCol.setPrefWidth(45);
        indexCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(tableData.indexOf(cd.getValue()) + 1)));

        // Name column
        TableColumn<Personnel, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(220);
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));

        // Status column
        TableColumn<Personnel, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(160);

        if (readOnly) {
            statusCol.setCellValueFactory(cd ->
                    new SimpleStringProperty(cd.getValue().getStatus().toString()));
        } else {
            // Inline ComboBox for MEDICAL_OFFICER
            statusCol.setCellFactory(col -> new TableCell<>() {
                private final ComboBox<Status> combo =
                        new ComboBox<>(FXCollections.observableArrayList(Status.values()));
                {
                    combo.setOnAction(e -> {
                        Personnel p = getTableRow().getItem();
                        if (p == null || combo.getValue() == null) {
                            return;
                        }
                        int idx = tableData.indexOf(p) + 1;
                        try {
                            new UpdateStatusCommand(idx, combo.getValue()).execute(model);
                            setFeedback("Status updated for " + p.getName() + ".", false);
                            saveData();
                            refresh();
                        } catch (CommandException ex) {
                            setFeedback("Error: " + ex.getMessage(), true);
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        combo.setValue(getTableRow().getItem().getStatus());
                        setGraphic(combo);
                    }
                }
            });
        }

        table.getColumns().addAll(indexCol, nameCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No personnel found. Use '+ Add Personnel' to get started."));
    }

    /**
     * Opens the modal dialogue to add a new personnel record.
     * Executes the resulting command and triggers a UI refresh and data save upon success.
     */
    private void openAddModal() {
        AddPersonnelModal modal = new AddPersonnelModal();
        modal.showAndWait().ifPresent(cmd -> {
            try {
                CommandResult result = cmd.execute(model);
                saveData();
                setFeedback(result.getFeedbackToUser(), false);
                refresh();
            } catch (CommandException ex) {
                setFeedback("Error: " + ex.getMessage(), true);
            }
        });
    }

    /**
     * Opens the modal dialogue to confirm the removal of a selected personnel record.
     * Executes the resulting command and triggers a UI refresh and data save upon success.
     */
    private void openRemoveModal() {
        Personnel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setFeedback("Please select a row to remove.", true);
            return;
        }
        int idx = tableData.indexOf(selected) + 1;
        RemovePersonnelModal modal = new RemovePersonnelModal(selected.getName(), idx);
        modal.showAndWait().ifPresent(cmd -> {
            try {
                CommandResult result = cmd.execute(model);
                saveData();
                setFeedback(result.getFeedbackToUser(), false);
                refresh();
            } catch (CommandException ex) {
                setFeedback("Error: " + ex.getMessage(), true);
            }
        });
    }

    /**
     * Persists the current state of the data model to the local JSON file.
     * Called automatically after successful add, remove, or edit actions.
     */
    private void saveData() {
        try {
            storage.saveMediTrackData(model.getMediTrack());
        } catch (IOException ex) {
            setFeedback("Warning: could not save — " + ex.getMessage(), true);
        }
    }

    /**
     * Reloads the table view with the latest filtered personnel list from the model.
     */
    public void refresh() {
        List<Personnel> all = model.getFilteredPersonnelList(null);
        tableData.setAll(all);
    }

    /**
     * Updates the status label at the bottom of the screen with operational feedback.
     *
     * @param message The text to display.
     * @param isError True if the message represents an error (displays in red), false for success (green).
     */
    private void setFeedback(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                + (isError ? "#dc3545" : "#198754") + ";");
    }
}