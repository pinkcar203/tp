package meditrack.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import meditrack.logic.Logic;
import meditrack.model.Model;
import meditrack.model.Role;
import meditrack.storage.CsvExportUtility;
import meditrack.ui.screen.DashboardScreen;
import meditrack.ui.screen.DutyRosterScreen;
import meditrack.ui.screen.ExpiringSoonScreen;
import meditrack.ui.screen.InventoryScreen;
import meditrack.ui.screen.LowSupplyScreen;
import meditrack.ui.screen.MedicalAttentionScreen;
import meditrack.ui.screen.PersonnelScreen;
import meditrack.ui.screen.ResupplyReportScreen;
import meditrack.ui.screen.SupplyLevelsScreen;
import meditrack.ui.sidebar.Sidebar;
import meditrack.ui.sidebar.Sidebar.Screen;

/**
 * The root application layout that wires the Sidebar navigation menu and the main content area together.
 * This class ensures that all UI components interact exclusively with the Logic engine and Model interfaces,
 * strictly decoupling the visual layer from the underlying Storage persistence layer.
 */
public class MainAppScreen extends HBox {

    private final Model model;
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

    private VBox devPanel;
    private boolean isDevMode = false;
    private Screen currentScreen;

    /**
     * Constructs the main application view layout.
     * Initializes the sidebar, configures the content area, and sets up developer keybindings.
     *
     * @param model          The abstract application data model to bind UI components to.
     * @param logic          The command execution engine for handling user actions.
     * @param logoutCallback The callback function executed when the user initiates a sign-out.
     */
    public MainAppScreen(Model model, Logic logic, Runnable logoutCallback) {
        this.model = model;
        this.logic = logic;
        setFillHeight(true);

        Sidebar sidebar = new Sidebar(model, this::showScreen, () -> {
            model.getSession().clear();
            logoutCallback.run();
        }, this::handleExport);

        HBox.setHgrow(contentArea, Priority.ALWAYS);
        contentArea.setStyle("-fx-background-color: #0d0f0b;");

        getChildren().addAll(sidebar, contentArea);
        showScreen(Screen.DASHBOARD);

        setupDevModeAccelerator();
    }

    /**
     * Registers the global keyboard shortcut (CTRL + SHIFT + D) utilized to toggle
     * the visibility of the developer mode panel.
     */
    private void setupDevModeAccelerator() {
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                        () -> {
                            isDevMode = !isDevMode;
                            showScreen(currentScreen);
                        }
                );
            }
        });
    }

    /**
     * A UI helper method designed to clear the current stack pane and load a new layout region.
     *
     * @param screenContent The JavaFX Region containing the newly constructed view to display.
     */
    private void switchContent(Region screenContent) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(screenContent);
    }

    /**
     * Orchestrates the lazy-loading, refreshing, and transition between different
     * application screens based on user interactions with the sidebar navigation.
     *
     * @param screen The target Screen enum representing the requested view.
     */
    public void showScreen(Screen screen) {
        this.currentScreen = screen;

        switch (screen) {
            case DASHBOARD:
                if (dashboardScreen == null) dashboardScreen = new DashboardScreen(model);
                dashboardScreen.refresh();
                switchContent(dashboardScreen);
                break;
            case PERSONNEL:
                if (personnelScreen == null) personnelScreen = new PersonnelScreen(model, logic);
                personnelScreen.refresh();
                switchContent(personnelScreen);
                break;
            case DUTY_ROSTER:
                if (dutyRosterScreen == null) dutyRosterScreen = new DutyRosterScreen(model, logic);
                dutyRosterScreen.refresh();
                switchContent(dutyRosterScreen);
                break;
            case MEDICAL_ATTENTION:
                if (medicalAttentionScreen == null) medicalAttentionScreen = new MedicalAttentionScreen(model);
                medicalAttentionScreen.refresh();
                switchContent(medicalAttentionScreen);
                break;
            case INVENTORY:
                if (inventoryScreen == null) inventoryScreen = new InventoryScreen(model, logic);
                inventoryScreen.refresh(); // <-- New!
                switchContent(inventoryScreen);
                break;
            case LOW_SUPPLY:
                if (lowSupplyScreen == null) lowSupplyScreen = new LowSupplyScreen(model);
                lowSupplyScreen.refresh();
                switchContent(lowSupplyScreen);
                break;
            case EXPIRING_SOON:
                if (expiringSoonScreen == null) expiringSoonScreen = new ExpiringSoonScreen(model);
                expiringSoonScreen.refresh();
                switchContent(expiringSoonScreen);
                break;
            case SUPPLY_LEVELS:
                if (supplyLevelsScreen == null) supplyLevelsScreen = new SupplyLevelsScreen(model, logic);
                supplyLevelsScreen.refresh();
                switchContent(supplyLevelsScreen);
                break;
            case RESUPPLY_REPORT:
                if (resupplyReportScreen == null) resupplyReportScreen = new ResupplyReportScreen(model, logic);
                switchContent(resupplyReportScreen);
                break;
        }

        if (isDevMode) {
            if (devPanel == null) devPanel = buildDevPanel();
            contentArea.getChildren().add(devPanel);
            StackPane.setAlignment(devPanel, Pos.TOP_RIGHT);
            StackPane.setMargin(devPanel, new Insets(20));
        }
    }

    /**
     * Executes the data export process, delegates the CSV generation to the utility class,
     * and provides immediate UI feedback via JavaFX modal Alerts.
     */
    private void handleExport() {
        try {
            Role currentRole = model.getSession().getRole();
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

    /**
     * Constructs the graphical developer tools overlay panel utilized for debugging
     * and time-travel simulations.
     *
     * @return A styled VBox containing the developer control buttons.
     */
    private VBox buildDevPanel() {
        VBox panel = new VBox(10);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.95); -fx-border-color: #00ff00;"
                + " -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,255,0,0.5), 10, 0, 0, 0);");

        Label title = new Label("⚠ DEV MODE ACTIVE");
        title.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        Button fastForwardBtn = new Button("⏩ TIME TRAVEL (DAYS)");
        fastForwardBtn.setStyle("-fx-background-color: #004400; -fx-text-fill: #00ff00; -fx-font-family: 'Consolas', monospace; -fx-cursor: hand; -fx-border-color: #00ff00;");
        fastForwardBtn.setOnAction(e -> handleTimeTravel());

        panel.getChildren().addAll(title, fastForwardBtn);
        return panel;
    }

    /**
     * Prompts the user via a text dialog to inject a specific time offset into the system clock.
     * Fast-forwards the application state to thoroughly test expiration logic and automatic status updates.
     */
    private void handleTimeTravel() {
        TextInputDialog dialog = new TextInputDialog("3");
        dialog.setTitle("Dev Tools: Time Travel");
        dialog.setHeaderText("Fast Forward Time");
        dialog.setContentText("Enter number of days to skip:");

        dialog.getDialogPane().setStyle("-fx-background-color: #1e201c; -fx-border-color: #00ff00; -fx-border-width: 1;");
        dialog.getEditor().setStyle("-fx-background-color: #292b26; -fx-text-fill: #00ff00; -fx-font-family: 'Consolas', monospace;");
        dialog.getDialogPane().lookupAll(".label").forEach(n -> n.setStyle("-fx-text-fill: #00ff00; -fx-font-family: 'Consolas', monospace;"));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(daysStr -> {
            try {
                int days = Integer.parseInt(daysStr.trim());
                Clock futureClock = Clock.offset(model.getClock(), Duration.ofDays(days));
                model.setClock(futureClock);
                model.cleanExpiredStatuses();

                showScreen(currentScreen);
                System.out.println("DEV: Clock shifted " + days + " days forward.");
            } catch (NumberFormatException ex) {
                System.out.println("DEV: Invalid integer inputted.");
            }
        });
    }
}