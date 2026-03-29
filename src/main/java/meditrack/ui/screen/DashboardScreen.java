package meditrack.ui.screen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.model.Status;
import meditrack.model.Supply;

/**
 * Role-specific mission dashboard shown as the default landing screen.
 */
public class DashboardScreen extends VBox {

    // Design tokens
    private static final String BG = "#0d0f0b";
    private static final String SURFACE = "#1e201c";
    private static final String SURFACE_HI = "#292b26";
    private static final String OLIVE = "#556b2f";
    private static final String OLIVE_LIGHT = "#8aa65c";
    private static final String OLIVE_PALE = "#b6d088";
    private static final String TEXT = "#e3e3dc";
    private static final String TEXT_DIM = "#8f9284";
    private static final String BORDER = "#2a2d24";
    private static final String WARNING = "#fbbc00";
    private static final String ERROR = "#e07070";

    private final ModelManager model;
    private final GridPane statGrid = new GridPane();
    private final VBox activityPane = new VBox();

    /**
     * Constructs the DashboardScreen.
     *
     * @param model The application model used to read live data.
     */
    public DashboardScreen(ModelManager model) {
        this.model = model;
        setStyle("-fx-background-color: " + BG + ";");
        setSpacing(0);
        VBox.setVgrow(this, Priority.ALWAYS);
        buildUi();
        refresh();
    }

    private void buildUi() {
        getChildren().addAll(buildHeader(), buildStatRow(), buildActivityPane());
        VBox.setVgrow(activityPane, Priority.ALWAYS);
    }

    // Header bar

    private HBox buildHeader() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 0 0 1 0;");

        Role role = Session.getInstance().getRole();
        String consoleName = switch (role) {
            case FIELD_MEDIC -> "SUPPLY & PERSONNEL HUD";
            case MEDICAL_OFFICER -> "MEDICAL READINESS HUD";
            case PLATOON_COMMANDER -> "PLATOON READINESS HUD";
            case LOGISTICS_OFFICER -> "LOGISTICS OPERATIONS HUD";
        };

        VBox left = new VBox(3);
        Label title = new Label("DEPLOYMENT CONSOLE  //  " + consoleName);
        title.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 14px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        left.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ts = new Label(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        ts.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        bar.getChildren().addAll(left, spacer, ts);
        return bar;
    }

    // Stat cards

    private HBox buildStatRow() {
        HBox row = new HBox(1);
        row.setStyle("-fx-background-color: " + BORDER + ";");
        row.setPrefHeight(110);
        row.setMinHeight(110);
        row.setMaxHeight(110);

        statGrid.setHgap(1);
        statGrid.setVgap(0);
        statGrid.setPrefHeight(110);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            statGrid.getColumnConstraints().add(cc);
        }
        HBox.setHgrow(statGrid, Priority.ALWAYS);
        row.getChildren().add(statGrid);
        return row;
    }

    private VBox buildActivityPane() {
        activityPane.setStyle("-fx-background-color: " + BG + ";");
        activityPane.setPadding(new Insets(16, 20, 16, 20));
        activityPane.setSpacing(10);
        return activityPane;
    }

    // Refresh

    /**
     * Re-reads model data and redraws all stat cards and the activity pane.
     * Call this every time the dashboard is shown.
     */
    public void refresh() {
        statGrid.getChildren().clear();
        activityPane.getChildren().clear();

        Role role = Session.getInstance().getRole();
        switch (role) {
            case FIELD_MEDIC -> refreshFieldMedic();
            case MEDICAL_OFFICER -> refreshMedicalOfficer();
            case PLATOON_COMMANDER -> refreshPlatoonCommander();
            case LOGISTICS_OFFICER -> refreshLogisticsOfficer();
        }
    }

    // Role: Field Medic

    private void refreshFieldMedic() {
        List<Supply> supplies = model.getFilteredSupplyList();
        int totalSupplies = supplies.size();
        int lowStock = model.getLowStockSupplies(50).size();
        int expiringSoon = model.getExpiringSupplies(30).size();
        int totalPersonnel = model.getPersonnelList().size();

        addStatCard(0, "TOTAL SUPPLIES", String.valueOf(totalSupplies), OLIVE_PALE, null);
        addStatCard(1, "LOW STOCK (<10)", String.valueOf(lowStock), lowStock > 0 ? WARNING : OLIVE_LIGHT,
                lowStock > 0 ? WARNING : null);
        addStatCard(2, "EXPIRING SOON", String.valueOf(expiringSoon), expiringSoon > 0 ? WARNING : OLIVE_LIGHT,
                expiringSoon > 0 ? WARNING : null);
        addStatCard(3, "TOTAL PERSONNEL", String.valueOf(totalPersonnel), OLIVE_PALE, null);

        buildSupplyAlertActivity();
    }

    // Role: Medical Officer

    private void refreshMedicalOfficer() {
        int fit = count(Status.FIT);
        int mc = count(Status.MC);
        int lightDuty = count(Status.LIGHT_DUTY);
        int casualty = count(Status.CASUALTY);

        addStatCard(0, "FIT FOR DUTY", String.valueOf(fit), OLIVE_PALE, null);
        addStatCard(1, "MEDICAL ATTN", String.valueOf(mc + lightDuty), mc + lightDuty > 0 ? WARNING : OLIVE_LIGHT,
                mc + lightDuty > 0 ? WARNING : null);
        addStatCard(2, "CASUALTY", String.valueOf(casualty), casualty > 0 ? ERROR : OLIVE_LIGHT,
                casualty > 0 ? ERROR : null);
        addStatCard(3, "PENDING REVIEW", String.valueOf(count(Status.PENDING)), TEXT_DIM, null);

        buildPersonnelStatusActivity();
    }

    // Role: Platoon Commander

    private void refreshPlatoonCommander() {
        int total = model.getPersonnelList().size();
        int fit = count(Status.FIT);
        int nonDeploy = count(Status.MC) + count(Status.LIGHT_DUTY) + count(Status.CASUALTY);
        int pending = count(Status.PENDING);

        String fitPct = total == 0 ? "N/A" : (fit * 100 / total) + "%";
        addStatCard(0, "TOTAL STRENGTH", String.valueOf(total), OLIVE_PALE, null);
        addStatCard(1, "FIT / DEPLOYABLE", String.valueOf(fit) + "  (" + fitPct + ")", OLIVE_PALE, null);
        addStatCard(2, "NON-DEPLOYABLE", String.valueOf(nonDeploy), nonDeploy > 0 ? WARNING : OLIVE_LIGHT,
                nonDeploy > 0 ? WARNING : null);
        addStatCard(3, "PENDING ASSESS", String.valueOf(pending), TEXT_DIM, null);

        buildPersonnelStatusActivity();
    }

    // Role: Logistics Officer

    private void refreshLogisticsOfficer() {
        int total = model.getFilteredSupplyList().size();
        int lowStock = model.getLowStockSupplies(50).size();
        int expiringSoon = model.getExpiringSupplies(30).size();
        int critical = model.getLowStockSupplies(10).size();

        addStatCard(0, "TOTAL SUPPLIES", String.valueOf(total), OLIVE_PALE, null);
        addStatCard(1, "LOW STOCK (<50)", String.valueOf(lowStock), lowStock > 0 ? WARNING : OLIVE_LIGHT,
                lowStock > 0 ? WARNING : null);
        addStatCard(2, "EXPIRING SOON", String.valueOf(expiringSoon), expiringSoon > 0 ? WARNING : OLIVE_LIGHT,
                expiringSoon > 0 ? WARNING : null);
        addStatCard(3, "CRITICAL (<10)", String.valueOf(critical), critical > 0 ? ERROR : OLIVE_LIGHT,
                critical > 0 ? ERROR : null);

        buildSupplyAlertActivity();
    }

    // Stat card builder

    /**
     * Creates and adds a stat card to the grid at the given column.
     *
     * @param col         Grid column (0–3).
     * @param label       Uppercase label text below the value.
     * @param value       The numeric/text value to display large.
     * @param valueColor  JavaFX hex color for the value text.
     * @param accentColor Left border accent color, or null for default olive.
     */
    private void addStatCard(int col, String label, String value,
            String valueColor, String accentColor) {
        String accent = (accentColor != null) ? accentColor : OLIVE;

        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);
        card.setStyle("-fx-background-color: " + SURFACE + ";"
                + " -fx-border-color: " + accent + " " + BORDER + " " + BORDER + " " + accent + ";"
                + " -fx-border-width: 0 0 0 3;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 28px;"
                + " -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");

        // Thin accent top line
        Region topLine = new Region();
        topLine.setPrefHeight(2);
        topLine.setMaxWidth(32);
        topLine.setStyle("-fx-background-color: " + accent + ";");

        card.getChildren().addAll(topLine, valueLabel, nameLabel);
        GridPane.setHgrow(card, Priority.ALWAYS);
        statGrid.add(card, col, 0);
    }

    // Activity pane builders

    private void buildSupplyAlertActivity() {
        activityPane.getChildren().add(buildSectionHeader("SUPPLY ALERT SUMMARY", "ITEMS REQUIRING ATTENTION"));

        List<Supply> lowStock = model.getLowStockSupplies(50);
        List<Supply> expiringSoon = model.getExpiringSupplies(30);

        if (lowStock.isEmpty() && expiringSoon.isEmpty()) {
            activityPane.getChildren().add(buildEmptyState("ALL SUPPLY LEVELS NORMAL"));
            return;
        }

        VBox listBox = new VBox(1);
        listBox.setStyle("-fx-background-color: " + BORDER + ";");

        java.util.List<Supply> displayed = new java.util.ArrayList<>();

        for (Supply s : lowStock) {
            boolean isCritical = s.getQuantity() < 10;
            String tag = isCritical ? "CRITICAL" : "LOW STOCK";
            String color = isCritical ? ERROR : WARNING;

            listBox.getChildren().add(buildSupplyRow(s, color, tag));
            displayed.add(s);
        }

        for (Supply s : expiringSoon) {
            boolean alreadyShown = false;
            for (Supply d : displayed) {
                if (d == s) {
                    alreadyShown = true;
                    break;
                }
            }
            if (!alreadyShown) {
                listBox.getChildren().add(buildSupplyRow(s, WARNING, "EXPIRING"));
            }
        }
        activityPane.getChildren().add(listBox);
    }

    private void buildPersonnelStatusActivity() {
        activityPane.getChildren().add(buildSectionHeader("PERSONNEL STATUS", "LATEST 10 UPDATES"));

        List<Personnel> all = new java.util.ArrayList<>(model.getPersonnelList());
        if (all.isEmpty()) {
            activityPane.getChildren().add(buildEmptyState("NO PERSONNEL ON RECORD"));
            return;
        }

        all.sort(java.util.Comparator.comparing(Personnel::getLastModified).reversed()
                .thenComparing(Personnel::getName));

        List<Personnel> latest10 = all.stream().limit(10).toList();

        VBox listBox = new VBox(1);
        listBox.setStyle("-fx-background-color: " + BORDER + ";");

        HBox header = new HBox();
        header.setPadding(new Insets(6, 16, 6, 16));
        header.setStyle("-fx-background-color: " + SURFACE_HI + ";");
        Label hName = colHeader("NAME", 240);
        Label hStatus = colHeader("STATUS", 120);
        header.getChildren().addAll(hName, hStatus);
        listBox.getChildren().add(header);

        for (Personnel p : latest10) {
            listBox.getChildren().add(buildPersonnelRow(p));
        }
        activityPane.getChildren().add(listBox);
    }

    private HBox buildSectionHeader(String title, String badge) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 0, 8, 0));

        Region accent = new Region();
        accent.setPrefWidth(3);
        accent.setMinWidth(3);
        accent.setPrefHeight(14);
        accent.setStyle("-fx-background-color: " + OLIVE + ";");

        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeLbl = new Label(badge);
        badgeLbl.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 9px;"
                + " -fx-background-color: rgba(251,188,0,0.1); -fx-padding: 2 8 2 8;"
                + " -fx-border-color: rgba(251,188,0,0.3); -fx-border-width: 1;"
                + " -fx-font-family: 'Consolas', monospace;");

        bar.getChildren().addAll(accent, lbl, spacer, badgeLbl);
        return bar;
    }

    private HBox buildSupplyRow(Supply supply, String accentColor, String tag) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: " + SURFACE + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + SURFACE_HI + ";"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + SURFACE + ";"));

        // Status square
        Region sq = new Region();
        sq.setMinSize(8, 8);
        sq.setMaxSize(8, 8);
        sq.setStyle("-fx-background-color: " + accentColor + ";");

        Label name = new Label(supply.getName().toUpperCase());
        name.setMinWidth(220);
        name.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label qty = new Label("QTY: " + supply.getQuantity());
        qty.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        Label expiry = new Label("EXP: " + supply.getExpiryDate());
        expiry.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        Label tagLbl = new Label(tag);
        tagLbl.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 9px;"
                + " -fx-font-weight: bold; -fx-padding: 2 8 2 8;"
                + " -fx-border-color: " + accentColor + "; -fx-border-width: 1;"
                + " -fx-font-family: 'Consolas', monospace;");

        row.getChildren().addAll(sq, name, spacer, qty, expiry, tagLbl);
        return row;
    }

    private HBox buildPersonnelRow(Personnel p) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 16, 9, 16));
        row.setStyle("-fx-background-color: " + SURFACE + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + SURFACE_HI + ";"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + SURFACE + ";"));

        String statusColor = switch (p.getStatus()) {
            case FIT -> OLIVE_PALE;
            case LIGHT_DUTY -> WARNING;
            case MC -> WARNING;
            case CASUALTY -> ERROR;
            case PENDING -> TEXT_DIM;
        };

        Region sq = new Region();
        sq.setMinSize(8, 8);
        sq.setMaxSize(8, 8);
        sq.setStyle("-fx-background-color: " + statusColor + ";");

        Label name = new Label(p.getName().toUpperCase());
        name.setMinWidth(240);
        name.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;"
                + " -fx-font-family: 'Consolas', monospace; -fx-padding: 0 0 0 12;");

        Label status = new Label(p.getStatus().toString().toUpperCase());
        status.setMinWidth(120);
        status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 10px;"
                + " -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        row.getChildren().addAll(sq, name, status);
        return row;
    }

    private Label buildEmptyState(String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace; -fx-padding: 16;");
        return lbl;
    }

    private Label colHeader(String text, double minWidth) {
        Label lbl = new Label(text);
        lbl.setMinWidth(minWidth);
        lbl.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private int count(Status status) {
        return (int) model.getPersonnelList().stream()
                .filter(p -> p.getStatus() == status)
                .count();
    }
}
