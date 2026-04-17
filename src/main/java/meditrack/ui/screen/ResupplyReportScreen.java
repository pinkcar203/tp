package meditrack.ui.screen;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import meditrack.commons.core.Constants;
import meditrack.logic.Logic;
import meditrack.logic.commands.GenerateResupplyReportCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.parser.CommandType;
import meditrack.logic.parser.Parser;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.Model;
import meditrack.model.Supply;

/**
 * Resupply Report screen generates and displays supply items flagged for restock.
 */
public class ResupplyReportScreen extends VBox {

    private static final String BG              = "#121410";
    private static final String SURFACE_LOW     = "#1a1c18";
    private static final String SURFACE_HIGH    = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT  = "#383a35";
    private static final String WARNING         = "#fbbc00";
    private static final String ERROR           = "#ffb4ab";
    private static final String PRIMARY         = "#b6d088";
    private static final String OUTLINE         = "#8f9284";
    private static final String OUTLINE_VAR     = "#45483c";
    private static final String ON_SURFACE      = "#e3e3dc";
    private static final String SECONDARY       = "#c8c6c6";

    private final Model model;
    private final Logic logic;

    private final TableView<ReportRow> reportTable = new TableView<>();
    private Label feedbackLabel;
    private Label flaggedCountLabel;

    /**
     * Constructs the Resupply Report screen.
     * Decoupled to accept Model instead of ModelManager.
     *
     * @param model For validation and flagged-item queries.
     * @param logic Runs the generate report command.
     */
    public ResupplyReportScreen(Model model, Logic logic) {
        this.model = model;
        this.logic = logic;
        buildUi();

        // Use an InvalidationListener. It guarantees a trigger on ANY backend list modification!
        this.model.getFilteredSupplyList().addListener((javafx.beans.Observable obs) -> {
            javafx.application.Platform.runLater(() -> populateReportTable());
        });

        // Force a data refresh every single time you click the sidebar to view this screen!
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> populateReportTable());
            }
        });
    }

    /** Assembles the layout components for this screen. */
    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        VBox tableSection = buildTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        getChildren().addAll(buildHeader(), tableSection, buildFooter());

        handleGenerateReport();
    }

    /** Builds the top title bar and feedback label. */
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("RESUPPLY REPORT");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        feedbackLabel = new Label("Auto-generated stock report.");
        feedbackLabel.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        titleArea.getChildren().addAll(title, feedbackLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleArea, spacer);
        return header;
    }

    /** Configures the TableView, its columns, and custom cell rendering. */
    @SuppressWarnings("unchecked")
    private VBox buildTableSection() {
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        reportTable.setFixedCellSize(50);
        reportTable.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: transparent;"
                + " -fx-table-cell-border-color: rgba(69,72,60,0.2);"
                + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        reportTable.setPlaceholder(buildEmptyPlaceholder());

        reportTable.skinProperty().addListener((obs, old, skin) -> {
            if (skin != null) {
                Platform.runLater(() -> {
                    javafx.scene.Node hdrBg = reportTable.lookup(".column-header-background");
                    if (hdrBg != null) hdrBg.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                    reportTable.lookupAll(".column-header").forEach(n -> n.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: transparent transparent "
                                    + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;"));
                    reportTable.lookupAll(".column-header .label").forEach(n -> n.setStyle(
                            "-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                    + " -fx-font-family: 'Consolas', monospace;"));
                });
            }
        });

        reportTable.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(ReportRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(null); setOnMouseExited(null);
                    return;
                }
                String bg = isExpired(item) ? "rgba(147,0,10,0.15)" : "rgba(251,188,0,0.04)";
                setStyle("-fx-background-color: " + bg + ";");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";"));
                setOnMouseExited(e -> setStyle("-fx-background-color: " + bg + ";"));
            }
        });

        reportTable.getColumns().addAll(
                buildIndexColumn(),
                buildNameColumn(),
                buildQuantityColumn(),
                buildExpiryColumn(),
                buildReasonColumn());

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        section.getChildren().add(reportTable);
        return section;
    }

    /** Creates the numerical index column. */
    private TableColumn<ReportRow, Number> buildIndexColumn() {
        TableColumn<ReportRow, Number> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(0));
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }

                int globalIdx = idx + 1;
                ReportRow row = getTableView().getItems().get(idx);
                boolean isError = row.getReason().contains("CRITICAL") || row.getReason().contains("EXPIRED");

                setText(String.format("%03d", globalIdx));
                setStyle("-fx-text-fill: " + (isError ? ERROR : WARNING) + "; -fx-font-weight: bold;"
                        + " -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace;"
                        + " -fx-background-color: transparent;");
            }
        });
        return col;
    }

    /** Creates the supply name column. */
    private TableColumn<ReportRow, String> buildNameColumn() {
        TableColumn<ReportRow, String> col = new TableColumn<>("SUPPLY NAME");
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Region dot = new Region();
            private final Label lbl = new Label();
            private final HBox box = new HBox(10, dot, lbl);
            { dot.setMinSize(8, 8); dot.setMaxSize(8, 8); box.setAlignment(Pos.CENTER_LEFT); }

            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }

                ReportRow row = getTableView().getItems().get(idx);
                boolean isError = row.getReason().contains("CRITICAL") || row.getReason().contains("EXPIRED");
                String color = isError ? ERROR : WARNING;

                dot.setStyle("-fx-background-color: " + color + ";");
                lbl.setText(v.toUpperCase());
                lbl.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 12px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;");
                setGraphic(box);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return col;
    }

    /** Creates the numerical quantity column. */
    private TableColumn<ReportRow, Integer> buildQuantityColumn() {
        TableColumn<ReportRow, Integer> col = new TableColumn<>("QUANTITY");
        col.setMinWidth(100);
        col.setMaxWidth(120);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setPadding(new Insets(3, 10, 3, 10)); badge.setAlignment(Pos.CENTER); }

            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }

                boolean isExpired = getTableView().getItems().get(idx).getExpiryDate().isBefore(LocalDate.now());
                boolean isCritical = v < 10 || isExpired;
                boolean isLow = v < 50;

                badge.setText(String.format("%03d", v));

                if (isCritical) {
                    badge.setStyle("-fx-background-color: rgba(255,180,171,0.15); -fx-text-fill: " + ERROR + ";"
                            + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else if (isLow) {
                    badge.setStyle("-fx-background-color: rgba(251,188,0,0.1); -fx-text-fill: " + WARNING + ";"
                            + " -fx-border-color: rgba(251,188,0,0.2); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else {
                    badge.setStyle("-fx-background-color: " + SURFACE_HIGHEST + "; -fx-text-fill: " + ON_SURFACE + ";"
                            + " -fx-border-color: rgba(69,72,60,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                }
                setGraphic(badge);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER;");
            }
        });
        return col;
    }

    /** Creates the supply expiry date column. */
    private TableColumn<ReportRow, LocalDate> buildExpiryColumn() {
        TableColumn<ReportRow, LocalDate> col = new TableColumn<>("EXPIRY DATE");
        col.setMinWidth(150);
        col.setMaxWidth(170);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getExpiryDate()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }

                LocalDate today = LocalDate.now();

                // Check if actually expired
                if (v.isBefore(today)) {
                    setText("EXPIRED");
                    setStyle("-fx-text-fill: " + ERROR + "; -fx-font-weight: bold; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
                // Check if expiring within the configured threshold
                else if (v.isBefore(today.plusDays(Constants.EXPIRY_THRESHOLD_DAYS))) {
                    setText(v.toString().replace("-", ".") + " [!]");
                    setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
                // Normal date
                else {
                    setText(v.toString().replace("-", "."));
                    setStyle("-fx-text-fill: #c8c6c6; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
            }
        });
        return col;
    }

    /** Creates the reason flagged column. */
    private TableColumn<ReportRow, String> buildReasonColumn() {
        TableColumn<ReportRow, String> col = new TableColumn<>("REASON FLAGGED");
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getReason()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setPadding(new Insets(3, 10, 3, 10)); }

            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }

                boolean isError = v.contains("CRITICAL") || v.contains("EXPIRED");
                String color = isError ? ERROR : WARNING;

                badge.setText(v.toUpperCase());
                badge.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;"
                        + " -fx-border-color: " + color + "; -fx-border-width: 1;"
                        + " -fx-background-color: transparent;");
                setGraphic(badge);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER_LEFT;");
            }
        });
        return col;
    }

    /** Builds the footer containing total flagged counts. */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44);
        footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        flaggedCountLabel = new Label("—");
        flaggedCountLabel.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        footer.getChildren().add(flaggedCountLabel);
        return footer;
    }

    /**
     * Executes the report generation logic.
     * Evaluates live data natively against the assigned Model interface.
     */
    private void handleGenerateReport() {
        Parser parser = new Parser((meditrack.model.ModelManager) model);
        try {
            parser.validate(CommandType.GENERATE_RESUPPLY_REPORT, Map.of());
        } catch (ParseException e) {
            setFeedback(e.getMessage(), true);
            return;
        }

        try {
            logic.executeCommand(new GenerateResupplyReportCommand(
                    Constants.LOW_STOCK_THRESHOLD_QUANTITY,
                    Constants.EXPIRY_THRESHOLD_DAYS));
        } catch (CommandException e) {
            setFeedback(e.getMessage(), true);
            return;
        }

        // Trigger the visual table build
        populateReportTable();
    }

    /** Populates the visual table natively without executing commands. */
    private void populateReportTable() {
        List<ReportRow> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Supply s : model.getFilteredSupplyList()) {
            boolean isAlreadyExpired = s.getExpiryDate().isBefore(today);
            boolean isExpiringSoon = !isAlreadyExpired && s.getExpiryDate().isBefore(today.plusDays(Constants.EXPIRY_THRESHOLD_DAYS));
            boolean isCritical = s.getQuantity() < Constants.CRITICAL_STOCK_THRESHOLD_QUANTITY;
            boolean isLowStock = !isCritical && s.getQuantity() < Constants.LOW_STOCK_THRESHOLD_QUANTITY;

            String reason = null;
            if (isCritical) {
                reason = "CRITICAL";
            } else if (isLowStock) {
                reason = "LOW STOCK";
            }

            if (isAlreadyExpired) {
                reason = (reason == null) ? "EXPIRED" : reason + " & EXPIRED";
            } else if (isExpiringSoon) {
                reason = (reason == null) ? "EXPIRING SOON" : reason + " & EXPIRING";
            }

            if (reason != null) {
                rows.add(new ReportRow(s.getName(), s.getQuantity(), s.getExpiryDate(), reason));
            }
        }

        if (rows.isEmpty()) {
            setFeedback("All Clear: No supplies flagged.", false);
            reportTable.setItems(FXCollections.observableArrayList());
            updateFlaggedCount(0);
            return;
        }

        rows.sort(Comparator.comparing((ReportRow r) -> r.getName().toLowerCase())
                .thenComparing(ReportRow::getExpiryDate));

        reportTable.setItems(FXCollections.observableArrayList(rows));
        setFeedback(rows.size() + " item" + (rows.size() == 1 ? "" : "s") + " flagged for resupply.", true);
        updateFlaggedCount(rows.size());

        reportTable.refresh();
    }

    /** Visual feedback indicator for UI actions. */
    private void setFeedback(String message, boolean isWarning) {
        if (feedbackLabel == null) return;
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: " + (isWarning ? WARNING : PRIMARY) + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
    }

    /** Helper to update the total count of flagged items in the footer. */
    private void updateFlaggedCount(int count) {
        if (flaggedCountLabel == null) return;
        if (count == 0) {
            flaggedCountLabel.setText("ALL CLEAR — NO FLAGS");
            flaggedCountLabel.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                    + " -fx-font-family: 'Consolas', monospace;");
        } else {
            flaggedCountLabel.setText("FLAGGED: " + count + " ITEM" + (count == 1 ? "" : "S"));
            flaggedCountLabel.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                    + " -fx-font-family: 'Consolas', monospace;");
        }
    }

    /** Checks if a specific report row is expired based on the live system clock. */
    private boolean isExpired(ReportRow row) {
        return row.getExpiryDate() != null && row.getExpiryDate().isBefore(LocalDate.now());
    }

    /** Generates the placeholder text for when no issues are found. */
    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("PRESS GENERATE REPORT TO VIEW FLAGGED ITEMS");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    /** Lightweight container class representing a single row in the generated report. */
    public static class ReportRow {
        private final String name;
        private final int quantity;
        private final LocalDate expiryDate;
        private final String reason;

        /**
         * Constructs a report row.
         *
         * @param name       The supply name.
         * @param quantity   The current quantity.
         * @param expiryDate The expiry date.
         * @param reason     The justification for why the item was flagged.
         */
        public ReportRow(String name, int quantity, LocalDate expiryDate, String reason) {
            this.name = name;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.reason = reason;
        }

        /** Retrieves the supply name. */
        public String getName() { return name; }

        /** Retrieves the item quantity. */
        public int getQuantity() { return quantity; }

        /** Retrieves the expiration date. */
        public LocalDate getExpiryDate() { return expiryDate; }

        /** Retrieves the flagging reason. */
        public String getReason() { return reason; }
    }
}