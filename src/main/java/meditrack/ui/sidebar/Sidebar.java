package meditrack.ui.sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import meditrack.model.Role;
import meditrack.model.Session;
import java.util.function.Consumer;

/**
 * Sidebar navigation component 
 */
public class Sidebar extends VBox {

    /** Identifies which screen to display in the content area. */
    public enum Screen {
        DASHBOARD,
        PERSONNEL,
        MEDICAL_ATTENTION,
        DUTY_ROSTER,
        INVENTORY,
        EXPIRING_SOON,
        SUPPLY_LEVELS,
        RESUPPLY_REPORT
    }

    private static final String BG          = "#121410";
    private static final String OLIVE       = "#556b2f";
    private static final String OLIVE_LIGHT = "#8aa65c";
    private static final String ACTIVE_BG   = "#292b26";
    private static final String BORDER      = "#2a2d24";
    private static final String TEXT_DIM    = "#8f9284";
    private static final String TEXT_MUTED  = "#45483c";

    private final Consumer<Screen> navigationHandler;
    private final Runnable logoutHandler;
    private final Runnable exportHandler;
    private Button activeButton;

    public Sidebar(Consumer<Screen> navigationHandler, Runnable logoutHandler, Runnable exportHandler) {
        this.navigationHandler = navigationHandler;
        this.logoutHandler = logoutHandler;
        this.exportHandler = exportHandler;
        buildUi();
    }

    private void buildUi() {
        setPrefWidth(200);
        setMinWidth(180);
        setSpacing(2);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: " + BG + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 0 1 0 0;");

        // Brand header
        VBox header = new VBox(4);
        header.setPadding(new Insets(14, 12, 12, 12));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 0 0 1 0;");

        Label brand = new Label("MEDITRACK");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace; -fx-letter-spacing: 0.15em;");

        Role role = Session.getInstance().getRole();
        String roleText = switch (role) {
            case MEDICAL_OFFICER   -> "MEDICAL OFFICER";
            case PLATOON_COMMANDER -> "PLATOON COMMANDER";
            case FIELD_MEDIC       -> "FIELD MEDIC";
            case LOGISTICS_OFFICER -> "LOGISTICS OFFICER";
        };
        Label roleBadge = new Label(roleText);
        roleBadge.setMaxWidth(Double.MAX_VALUE);
        roleBadge.setPadding(new Insets(3, 6, 3, 6));
        roleBadge.setStyle("-fx-background-color: " + OLIVE + "; -fx-text-fill: white;"
                + " -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        header.getChildren().addAll(brand, roleBadge);
        getChildren().add(header);

        // Nav section label
        Label navLabel = new Label("NAVIGATION");
        navLabel.setPadding(new Insets(12, 12, 4, 12));
        navLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 8px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        getChildren().add(navLabel);

        // Dashboard (all roles)
        Button dashBtn = navButton("DASHBOARD", Screen.DASHBOARD);
        getChildren().add(dashBtn);

        // Role-specific nav
        if (role == Role.FIELD_MEDIC) {
            getChildren().add(navButton("INVENTORY", Screen.INVENTORY));
            getChildren().add(navButton("EXPIRING SOON", Screen.EXPIRING_SOON));
            getChildren().add(navButton("PERSONNEL", Screen.PERSONNEL));
        } else if (role == Role.MEDICAL_OFFICER) {
            getChildren().add(navButton("PERSONNEL", Screen.PERSONNEL));
            getChildren().add(navButton("MEDICAL ATTENTION", Screen.MEDICAL_ATTENTION));
        } else if (role == Role.PLATOON_COMMANDER) {
            getChildren().add(navButton("PERSONNEL", Screen.PERSONNEL));
            getChildren().add(navButton("DUTY ROSTER", Screen.DUTY_ROSTER));
        } else if (role == Role.LOGISTICS_OFFICER) {
            getChildren().add(navButton("SUPPLY LEVELS", Screen.SUPPLY_LEVELS));
            getChildren().add(navButton("RESUPPLY REPORT", Screen.RESUPPLY_REPORT));
        }

        // Utilities section
        Label utilLabel = new Label("UTILITIES");
        utilLabel.setPadding(new Insets(16, 12, 4, 12));
        utilLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 8px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        getChildren().add(utilLabel);

        Button exportBtn = new Button("EXPORT CSV");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setAlignment(Pos.CENTER_LEFT);
        exportBtn.setPadding(new Insets(8, 12, 8, 12));
        exportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + OLIVE_LIGHT + ";"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;");
        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle(
                "-fx-background-color: " + ACTIVE_BG + "; -fx-text-fill: " + OLIVE_LIGHT + ";"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;"));
        exportBtn.setOnMouseExited(e -> exportBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + OLIVE_LIGHT + ";"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;"));
        exportBtn.setOnAction(e -> exportHandler.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bottom border separator
        Region bottomBorder = new Region();
        bottomBorder.setPrefHeight(1);
        bottomBorder.setMaxWidth(Double.MAX_VALUE);
        bottomBorder.setStyle("-fx-background-color: " + BORDER + ";");

        Button logoutBtn = new Button("LOGOUT");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setPadding(new Insets(10, 12, 10, 12));
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e07070;"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
                "-fx-background-color: #2a1515; -fx-text-fill: #e07070;"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e07070;"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-border-color: transparent; -fx-background-radius: 0;"));
        logoutBtn.setOnAction(e -> logoutHandler.run());

        getChildren().addAll(exportBtn, spacer, bottomBorder, logoutBtn);

        // Default Dashboard active
        activateButton(dashBtn);
    }

    private Button navButton(String label, Screen target) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 12, 8, 12));
        btn.setStyle(inactiveStyle());
        btn.setOnMouseEntered(e -> { if (btn != activeButton) btn.setStyle(hoverStyle()); });
        btn.setOnMouseExited(e -> { if (btn != activeButton) btn.setStyle(inactiveStyle()); });
        btn.setOnAction(e -> {
            activateButton(btn);
            navigationHandler.accept(target);
        });
        return btn;
    }

    private void activateButton(Button btn) {
        if (activeButton != null) {
            activeButton.setStyle(inactiveStyle());
        }
        btn.setStyle(activeStyle());
        activeButton = btn;
    }

    private String inactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + TEXT_DIM + ";"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: transparent; -fx-border-width: 0 0 0 2;";
    }

    private String hoverStyle() {
        return "-fx-background-color: " + ACTIVE_BG + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: transparent; -fx-border-width: 0 0 0 2;";
    }

    private String activeStyle() {
        return "-fx-background-color: " + ACTIVE_BG + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: " + OLIVE_LIGHT + "; -fx-border-width: 0 0 0 2;";
    }
}
