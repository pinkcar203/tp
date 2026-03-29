package meditrack.ui;

import java.io.IOException;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import meditrack.logic.Logic;
import meditrack.storage.CsvExportUtility;
import meditrack.logic.LogicManager;
import meditrack.model.MediTrack;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.storage.StorageManager;
import meditrack.ui.screen.DashboardScreen;
import meditrack.ui.screen.DutyRosterScreen;
import meditrack.ui.screen.LowSupplyScreen;
import meditrack.ui.screen.ExpiringSoonScreen;
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

    private DashboardScreen dashboardScreen;
    private PersonnelScreen personnelScreen;
    private DutyRosterScreen dutyRosterScreen;
    private MedicalAttentionScreen medicalAttentionScreen;
    private InventoryScreen inventoryScreen;
    private LowSupplyScreen lowSupplyScreen;
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
        contentArea.setStyle("-fx-background-color: #0d0f0b;");

        getChildren().addAll(sidebar, contentArea);

        showScreen(Screen.DASHBOARD);
    }

    /**
     * Switches the content area to the given screen.
     * Screens are lazily instantiated and refreshed on each switch.
     */
    public void showScreen(Screen screen) {
        contentArea.getChildren().clear();
        switch (screen) {
            case DASHBOARD:
                if (dashboardScreen == null) {
                    dashboardScreen = new DashboardScreen(model);
                }
                dashboardScreen.refresh();
                contentArea.getChildren().add(dashboardScreen);
                break;
            case PERSONNEL:
                if (personnelScreen == null) {
                    personnelScreen = new PersonnelScreen(model, storage);
                }
                personnelScreen.refresh();
                contentArea.getChildren().add(personnelScreen);
                break;
            case DUTY_ROSTER:
                if (dutyRosterScreen == null) {
                    dutyRosterScreen = new DutyRosterScreen(model, storage);
                }
                dutyRosterScreen.refresh();
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
            case LOW_SUPPLY:
                if (lowSupplyScreen == null) {
                    lowSupplyScreen = new LowSupplyScreen(model);
                }
                lowSupplyScreen.refresh();
                contentArea.getChildren().add(lowSupplyScreen);
                break;
            case EXPIRING_SOON:
                if (expiringSoonScreen == null) {
                    expiringSoonScreen = new ExpiringSoonScreen(model);
                }
                expiringSoonScreen.refresh();
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
     * Handles the CSV export process, enforcing role-based access control,
     * and shows a popup alert with the result.
     */
    private void handleExport() {
        try {
            Role currentRole = Session.getInstance().getRole();

            Path savedPath = CsvExportUtility.exportData(model.getMediTrack(), currentRole);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Security Clearance: " + currentRole.toString()
                    + "\nData successfully exported to:\n" + savedPath.toAbsolutePath());
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not export data: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
