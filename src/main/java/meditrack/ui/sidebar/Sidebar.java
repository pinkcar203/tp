package meditrack.ui.sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import meditrack.model.Role;
import meditrack.model.Session;
import java.util.function.Consumer;

/**
 * Sidebar navigation component.
 *
 * <p>Navigation items are dynamically generated based on the user's {@link Role}:
 * <ul>
 *   <li><b>FIELD_MEDIC</b> — Inventory, Expiring Soon, Personnel (Read-Only)</li>
 *   <li><b>MEDICAL_OFFICER</b> — Personnel, FIT Personnel, Duty Roster</li>
 *   <li><b>LOGISTICS_OFFICER</b> — Supply Levels, Resupply Report</li>
 * </ul>
 * * <p>Also provides global utilities such as CSV data export and user logout.
 */
public class Sidebar extends VBox {

    /** Identifies which screen to display in the content area. */
    public enum Screen {
        PERSONNEL,
        FIT_PERSONNEL,
        MEDICAL_ATTENTION,
        DUTY_ROSTER,
        INVENTORY,
        EXPIRING_SOON,
        SUPPLY_LEVELS,
        RESUPPLY_REPORT
    }

    private final Consumer<Screen> navigationHandler;
    private final Runnable logoutHandler;
    private final Runnable exportHandler;
    private Button activeButton;

    /**
     * Constructs the Sidebar navigation menu.
     *
     * @param navigationHandler A callback function that receives the selected {@link Screen} to navigate to.
     * @param logoutHandler     A callback function executed when the user clicks the Logout button.
     * @param exportHandler     A callback function executed when the user clicks the Export to CSV button.
     */
    public Sidebar(Consumer<Screen> navigationHandler, Runnable logoutHandler, Runnable exportHandler) {
        this.navigationHandler = navigationHandler;
        this.logoutHandler = logoutHandler;
        this.exportHandler = exportHandler;
        buildUi();
    }

    /**
     * Initializes and arranges all JavaFX components within the sidebar.
     * Dynamically loads navigation buttons based on the active user's role.
     */
    private void buildUi() {
        setPrefWidth(200);
        setMinWidth(180);
        setSpacing(4);
        setPadding(new Insets(16, 8, 16, 8));
        setStyle("-fx-background-color: #1c2b3a;");

        Label brand = new Label("MediTrack");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 17px; "
                + "-fx-font-weight: bold; -fx-padding: 0 0 10 8;");

        Role role = Session.getInstance().getRole();
        String roleText = switch (role) {
            case MEDICAL_OFFICER  -> "Medical Officer";
            case PLATOON_COMMANDER -> "Platoon Commander";
            case FIELD_MEDIC      -> "Field Medic";
            case LOGISTICS_OFFICER -> "Logistics Officer";
        };
        Label roleBadge = new Label(roleText);
        roleBadge.setMaxWidth(Double.MAX_VALUE);
        roleBadge.setStyle("-fx-background-color: #2e4057; -fx-text-fill: #a0b8cc; "
                + "-fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");

        getChildren().addAll(brand, roleBadge, new Separator());

        Button firstBtn = null;

        if (role == Role.FIELD_MEDIC) {
            firstBtn = navButton("Inventory", Screen.INVENTORY);
            getChildren().add(firstBtn);
            getChildren().add(navButton("Personnel", Screen.PERSONNEL));
        } else if (role == Role.MEDICAL_OFFICER) {
            firstBtn = navButton("Personnel", Screen.PERSONNEL);
            getChildren().add(firstBtn);
            getChildren().add(navButton("Medical Attention", Screen.MEDICAL_ATTENTION));
        } else if (role == Role.PLATOON_COMMANDER) {
            firstBtn = navButton("Personnel", Screen.PERSONNEL);
            getChildren().add(firstBtn);
            getChildren().add(navButton("FIT Personnel", Screen.FIT_PERSONNEL));
            getChildren().add(navButton("Duty Roster", Screen.DUTY_ROSTER));
        } else if (role == Role.LOGISTICS_OFFICER) {
            firstBtn = navButton("Inventory", Screen.INVENTORY);
            getChildren().add(firstBtn);
            getChildren().add(navButton("Resupply Report", Screen.RESUPPLY_REPORT));
        }

        Button exportBtn = new Button("Export to CSV");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #198754; "
                + "-fx-font-size: 13px; -fx-padding: 9 12 9 12; -fx-cursor: hand; "
                + "-fx-background-radius: 6; -fx-alignment: CENTER_LEFT;");
        exportBtn.setOnAction(e -> exportHandler.run());

        getChildren().addAll(new Separator(), exportBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e07070; "
                + "-fx-font-size: 13px; -fx-padding: 9 12 9 12; -fx-cursor: hand; "
                + "-fx-background-radius: 6; -fx-alignment: CENTER_LEFT;");
        logoutBtn.setOnAction(e -> logoutHandler.run());

        getChildren().addAll(spacer, new Separator(), logoutBtn);

        if (firstBtn != null) {
            activateButton(firstBtn);
        }
    }

    /**
     * Creates a standardized navigation button for the sidebar.
     *
     * @param label  The text displayed on the button.
     * @param target The screen to navigate to when clicked.
     * @return A styled JavaFX {@link Button}.
     */
    private Button navButton(String label, Screen target) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(inactiveStyle());
        btn.setOnAction(e -> {
            activateButton(btn);
            navigationHandler.accept(target);
        });
        return btn;
    }

    /**
     * Updates the visual styling to reflect the currently active navigation button.
     *
     * @param btn The button that was just clicked and should become active.
     */
    private void activateButton(Button btn) {
        if (activeButton != null) {
            activeButton.setStyle(inactiveStyle());
        }
        btn.setStyle(activeStyle());
        activeButton = btn;
    }

    /**
     * Provides the CSS styling string for unselected navigation buttons.
     *
     * @return A string containing inline JavaFX CSS rules.
     */
    private String inactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #c5d5e4; "
                + "-fx-font-size: 13px; -fx-padding: 9 12 9 12; -fx-cursor: hand; "
                + "-fx-background-radius: 6; -fx-alignment: CENTER_LEFT;";
    }

    /**
     * Provides the CSS styling string for the currently selected navigation button.
     *
     * @return A string containing inline JavaFX CSS rules.
     */
    private String activeStyle() {
        return "-fx-background-color: #0d6efd; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 9 12 9 12; "
                + "-fx-cursor: hand; -fx-background-radius: 6; -fx-alignment: CENTER_LEFT;";
    }
}
