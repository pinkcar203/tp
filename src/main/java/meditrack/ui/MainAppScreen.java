package meditrack.ui;

import javafx.scene.layout.*;
import meditrack.logic.Logic;
import meditrack.logic.LogicManager;
import meditrack.model.MediTrack;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.storage.StorageManager;
import meditrack.ui.screen.DutyRosterScreen;
import meditrack.ui.screen.ExpiringSoonScreen;
import meditrack.ui.screen.FitPersonnelScreen;
import meditrack.ui.screen.MedicalAttentionScreen;
import meditrack.ui.screen.InventoryScreen;
import meditrack.ui.screen.PersonnelScreen;
import meditrack.ui.screen.ResupplyReportScreen;
import meditrack.ui.screen.SupplyLevelsScreen;
import meditrack.ui.sidebar.Sidebar;
import meditrack.ui.sidebar.Sidebar.Screen;

/**
 * Root application layout wiring the {@link Sidebar} and the content area together.
 *
 * <p>Role-based home screen:
 * <ul>
 *   <li>FIELD_MEDIC — Inventory</li>
 *   <li>MEDICAL_OFFICER — Personnel</li>
 *   <li>LOGISTICS_OFFICER — Supply Levels</li>
 * </ul>
 */
public class MainAppScreen extends HBox {

    private final ModelManager model;
    private final StorageManager storage;
    private final Logic logic;
    private final StackPane contentArea = new StackPane();

    private PersonnelScreen personnelScreen;
    private FitPersonnelScreen fitPersonnelScreen;
    private DutyRosterScreen dutyRosterScreen;
    private MedicalAttentionScreen medicalAttentionScreen;
    private InventoryScreen inventoryScreen;
    private ExpiringSoonScreen expiringSoonScreen;
    private SupplyLevelsScreen supplyLevelsScreen;
    private ResupplyReportScreen resupplyReportScreen;

    /**
     * @param mediTrack data loaded at startup
     * @param storage for persistence
     * @param logoutCallback run when user logs out (returns to login)
     */
    public MainAppScreen(MediTrack mediTrack, StorageManager storage, Runnable logoutCallback) {
        this.model = new ModelManager(mediTrack);
        this.storage = storage;
        this.logic = new LogicManager(model, storage);
        setFillHeight(true);

        Sidebar sidebar = new Sidebar(this::showScreen, () -> {
            Session.getInstance().clear();
            logoutCallback.run();
        }, this::handleExport);

        HBox.setHgrow(contentArea, Priority.ALWAYS);
        contentArea.setStyle("-fx-background-color: #f0f2f5; -fx-padding: 16;");

        getChildren().addAll(sidebar, contentArea);

        Role currentRole = Session.getInstance().getRole();
        if (currentRole == Role.FIELD_MEDIC || currentRole == Role.LOGISTICS_OFFICER) {
            showScreen(Screen.INVENTORY);
        } else {
            showScreen(Screen.PERSONNEL);
        }
    }

    /**
     * Switches the content area to the given screen.
     * Screens are lazily instantiated and refreshed on each switch.
     */
    public void showScreen(Screen screen) {
        contentArea.getChildren().clear();
        switch (screen) {
            case PERSONNEL:
                if (personnelScreen == null) {
                    personnelScreen = new PersonnelScreen(model, storage);
                }
                personnelScreen.refresh();
                contentArea.getChildren().add(personnelScreen);
                break;
            case FIT_PERSONNEL:
                if (fitPersonnelScreen == null) {
                    fitPersonnelScreen = new FitPersonnelScreen(model);
                }
                fitPersonnelScreen.refresh();
                contentArea.getChildren().add(fitPersonnelScreen);
                break;
            case DUTY_ROSTER:
                if (dutyRosterScreen == null) {
                    dutyRosterScreen = new DutyRosterScreen(model);
                }
                contentArea.getChildren().add(dutyRosterScreen);
                break;
            case MEDICAL_ATTENTION:
                if (medicalAttentionScreen == null) {
                    medicalAttentionScreen = new MedicalAttentionScreen(model);
                }
                medicalAttentionScreen.refresh();
                contentArea.getChildren().add(medicalAttentionScreen);
                break;
            case INVENTORY:
                if (inventoryScreen == null) {
                    inventoryScreen = new InventoryScreen(model, logic);
                }
                contentArea.getChildren().add(inventoryScreen);
                break;
            case EXPIRING_SOON:
                if (expiringSoonScreen == null) {
                    expiringSoonScreen = new ExpiringSoonScreen(model);
                }
                contentArea.getChildren().add(expiringSoonScreen);
                break;
            case SUPPLY_LEVELS:
                if (supplyLevelsScreen == null) {
                    supplyLevelsScreen = new SupplyLevelsScreen(model);
                }
                contentArea.getChildren().add(supplyLevelsScreen);
                break;
            case RESUPPLY_REPORT:
                if (resupplyReportScreen == null) {
                    resupplyReportScreen = new ResupplyReportScreen(model, logic);
                }
                contentArea.getChildren().add(resupplyReportScreen);
                break;
        }
    }

    /**
     * Handles the CSV export process and shows a popup alert with the result.
     */
    private void handleExport() {
        try {
            java.nio.file.Path savedPath = meditrack.storage.CsvExportUtility.exportData(model.getMediTrack());

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Data successfully exported to:\n" + savedPath.toAbsolutePath());
            alert.showAndWait();

        } catch (java.io.IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not export data: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
