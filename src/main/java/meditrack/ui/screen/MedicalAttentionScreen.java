package meditrack.ui.screen;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Status;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A dedicated dashboard for the Medical Officer to monitor casualties.
 * Automatically filters the roster to display only personnel who are currently
 * marked as MC or on LIGHT_DUTY.
 */
public class MedicalAttentionScreen extends VBox {

    private final ModelManager model;
    private final ObservableList<Personnel> tableData = FXCollections.observableArrayList();
    private final TableView<Personnel> table = new TableView<>(tableData);

    /**
     * Constructs the Medical Attention monitoring screen.
     *
     * @param model The data model managing personnel records.
     */
    public MedicalAttentionScreen(ModelManager model) {
        this.model = model;
        buildUi();
        refresh();
    }

    /**
     * Initializes and arranges all JavaFX components for this screen.
     */
    private void buildUi() {
        setSpacing(12);
        setPadding(new Insets(20));

        Label title = new Label("Medical Attention Required");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Showing personnel currently on LIGHT DUTY/MC/CASUALTY/PENDING status.");
        subtitle.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        buildTable();

        getChildren().addAll(title, subtitle, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    /**
     * Constructs the read-only table columns and data binding.
     */
    private void buildTable() {
        // Index Column
        TableColumn<Personnel, String> indexCol = new TableColumn<>("#");
        indexCol.setPrefWidth(45);
        indexCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(tableData.indexOf(cd.getValue()) + 1)));

        // Name Column
        TableColumn<Personnel, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(220);
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));

        // Status Column
        TableColumn<Personnel, String> statusCol = new TableColumn<>("Medical Status");
        statusCol.setPrefWidth(160);
        statusCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getStatus().toString()));

        table.getColumns().addAll(indexCol, nameCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("All personnel are currently FIT. No medical attention required."));
    }

    /**
     * Reloads the table view with the latest filtered list of personnel requiring triage.
     */
    public void refresh() {
        List<Personnel> all = model.getFilteredPersonnelList(null);
        List<Personnel> medicalCases = all.stream()
                .filter(p -> p.getStatus() == Status.PENDING ||
                        p.getStatus() == Status.CASUALTY ||
                        p.getStatus() == Status.MC ||
                        p.getStatus() == Status.LIGHT_DUTY)
                .collect(Collectors.toList());

        tableData.setAll(medicalCases);
    }
}