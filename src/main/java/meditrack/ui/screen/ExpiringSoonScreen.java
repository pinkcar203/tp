package meditrack.ui.screen;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import meditrack.commons.core.Constants;
import meditrack.model.Model;
import meditrack.model.Supply;

/**
 * Displays supplies that are nearing their expiration date.
 * Designed primarily for Field Medics and Logistics personnel.
 */
public class ExpiringSoonScreen extends VBox {

    private static final String BG              = "#121410";
    private static final String SURFACE_LOW     = "#1a1c18";
    private static final String SURFACE_HIGH    = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT  = "#383a35";
    private static final String PRIMARY         = "#b6d088";
    private static final String OUTLINE         = "#8f9284";
    private static final String OUTLINE_VAR     = "#45483c";
    private static final String ON_SURFACE      = "#e3e3dc";
    private static final String SECONDARY       = "#c8c6c6";
    private static final String WARNING         = "#fbbc00";
    private static final String ERROR           = "#ffb4ab";
    private static final int    PAGE_SIZE       = 15;

    private final Model model;
    private final TableView<Supply> table = new TableView<>();
    private final ObservableList<Supply> tableItems = FXCollections.observableArrayList();
    private final ObservableList<Supply> pageItems  = FXCollections.observableArrayList();
    private int currentPage = 0;

    private Label countLabel;
    private Label expiredLabel;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    /**
     * Constructs the Expiring Soon screen.
     *
     * @param model The application model providing live supply data.
     */
    public ExpiringSoonScreen(Model model) {
        this.model = model;
        buildUi();
        refresh();
    }

    /** Assembles the layout components for this screen. */
    @SuppressWarnings("unchecked")
    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        tableItems.addListener((javafx.collections.ListChangeListener<Supply>) c -> {
            currentPage = 0;
            updatePage();
        });

        VBox tableSection = buildTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        getChildren().addAll(buildHeader(), tableSection, buildFooter());
    }

    /** Builds the top title and threshold indicator. */
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("EXPIRING SOON");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        Label subtitle = new Label("Supplies expiring within "
                + Constants.EXPIRY_THRESHOLD_DAYS + " days — requires immediate attention.");
        subtitle.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        titleArea.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("⚠  THRESHOLD: " + Constants.EXPIRY_THRESHOLD_DAYS + " DAYS");
        badge.setPadding(new Insets(6, 14, 6, 14));
        badge.setStyle("-fx-background-color: rgba(251,188,0,0.1); -fx-text-fill: " + WARNING + ";"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-border-color: rgba(251,188,0,0.3); -fx-border-width: 1;");

        header.getChildren().addAll(titleArea, spacer, badge);
        return header;
    }

    /** Configures the table layout and sets up column definitions. */
    @SuppressWarnings("unchecked")
    private VBox buildTableSection() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(50);
        table.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: transparent;"
                + " -fx-table-cell-border-color: rgba(69,72,60,0.2);"
                + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        table.setPlaceholder(emptyPlaceholder());

        table.skinProperty().addListener((obs, old, skin) -> {
            if (skin != null) Platform.runLater(() -> {
                javafx.scene.Node hdrBg = table.lookup(".column-header-background");
                if (hdrBg != null) hdrBg.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                table.lookupAll(".column-header").forEach(n -> n.setStyle(
                        "-fx-background-color: transparent; -fx-border-color: transparent transparent "
                                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;"));
                table.lookupAll(".column-header .label").forEach(n -> n.setStyle(
                        "-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;"));
                javafx.scene.Node filler = table.lookup(".filler");
                if (filler != null) filler.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
            });
        });

        table.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(Supply item, boolean empty) {
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

        TableColumn<Supply, Number> idxCol = new TableColumn<>("#");
        idxCol.setMinWidth(50); idxCol.setMaxWidth(50);
        idxCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(0));
        idxCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }
                int globalIdx = currentPage * PAGE_SIZE + idx + 1;
                String color = isExpired(getTableView().getItems().get(idx)) ? ERROR : WARNING;
                setText(String.format("%03d", globalIdx));
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
            }
        });

        TableColumn<Supply, String> nameCol = new TableColumn<>("SUPPLY NAME");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            private final Region dot = new Region();
            private final Label lbl = new Label();
            private final HBox box  = new HBox(10, dot, lbl);
            { dot.setMinSize(8, 8); dot.setMaxSize(8, 8); box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                String color = isExpired(getTableView().getItems().get(idx)) ? ERROR : WARNING;
                dot.setStyle("-fx-background-color: " + color + ";");
                lbl.setText(v.toUpperCase());
                lbl.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 12px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;");
                setGraphic(box);
                setStyle("-fx-background-color: transparent;");
            }
        });

        TableColumn<Supply, Integer> qtyCol = new TableColumn<>("QUANTITY");
        qtyCol.setMinWidth(100); qtyCol.setMaxWidth(100);
        qtyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        qtyCol.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setPadding(new Insets(3, 10, 3, 10)); badge.setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                boolean expired = isExpired(getTableView().getItems().get(idx));
                badge.setText(String.format("%03d", v));
                if (expired) {
                    badge.setStyle("-fx-background-color: rgba(255,180,171,0.15); -fx-text-fill: " + ERROR + ";"
                            + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else {
                    badge.setStyle("-fx-background-color: rgba(251,188,0,0.1); -fx-text-fill: " + WARNING + ";"
                            + " -fx-border-color: rgba(251,188,0,0.2); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                }
                setGraphic(badge);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER;");
            }
        });

        TableColumn<Supply, LocalDate> expiryCol = new TableColumn<>("EXPIRY DATE");
        expiryCol.setMinWidth(160); expiryCol.setMaxWidth(200);
        expiryCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getExpiryDate()));
        expiryCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }
                if (isExpired(getTableView().getItems().get(idx))) {
                    setText("EXPIRED");
                    setStyle("-fx-text-fill: " + ERROR + "; -fx-font-weight: bold; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                } else {
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), v);
                    setText(v.toString().replace("-", ".") + "  [" + daysLeft + "d]");
                    setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
            }
        });

        TableColumn<Supply, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setMinWidth(120); statusCol.setMaxWidth(140);
        statusCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(isExpired(c.getValue()) ? "EXPIRED" : "EXPIRING"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final Label tag = new Label();
            { tag.setPadding(new Insets(3, 10, 3, 10)); }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                boolean expired = "EXPIRED".equals(v);
                tag.setText(v);
                tag.setStyle((expired
                        ? "-fx-text-fill: " + ERROR + "; -fx-border-color: rgba(255,180,171,0.3);"
                        + " -fx-background-color: rgba(255,180,171,0.1);"
                        : "-fx-text-fill: " + WARNING + "; -fx-border-color: rgba(251,188,0,0.3);"
                        + " -fx-background-color: rgba(251,188,0,0.08);")
                        + " -fx-border-width: 1; -fx-font-size: 9px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;");
                setGraphic(tag);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER_LEFT;");
            }
        });

        table.getColumns().addAll(idxCol, nameCol, qtyCol, expiryCol, statusCol);

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    /** Builds the footer containing total counts and pagination navigation. */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44); footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        countLabel = new Label("EXPIRING: 0");
        countLabel.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        expiredLabel = new Label();
        expiredLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        prevBtn = pageNavBtn("← PREV");
        prevBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });

        pageLabel = new Label("PAGE 1 / 1");
        pageLabel.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        pageLabel.setPadding(new Insets(0, 6, 0, 6));

        nextBtn = pageNavBtn("NEXT →");
        nextBtn.setOnAction(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) tableItems.size() / PAGE_SIZE));
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePage();
            }
        });

        footer.getChildren().addAll(countLabel, expiredLabel, spacer, prevBtn, pageLabel, nextBtn);
        return footer;
    }

    /** Recalculates the active items to display based on the current page. */
    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = tableItems.size();
        int to   = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? tableItems.subList(from, to) : List.of());
        updatePaginationControls();
    }

    /** Enables or disables pagination buttons. */
    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) tableItems.size() / PAGE_SIZE));
        if (pageLabel != null) pageLabel.setText("PAGE " + (currentPage + 1) + " / " + totalPages);
        if (prevBtn   != null) prevBtn.setDisable(currentPage == 0);
        if (nextBtn   != null) nextBtn.setDisable(currentPage >= totalPages - 1);
    }

    /** Creates a styled button for pagination navigation. */
    private Button pageNavBtn(String text) {
        String base  = "-fx-background-color: " + SURFACE_HIGH + "; -fx-text-fill: " + SECONDARY + ";"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0; -fx-pref-height: 28; -fx-padding: 0 12 0 12;";
        String hover = "-fx-background-color: rgba(85,107,47,0.6); -fx-text-fill: white;"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0; -fx-pref-height: 28; -fx-padding: 0 12 0 12;";
        Button btn = new Button(text);
        btn.setStyle(base);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }

    /**
     * Refreshes the screen's data directly from the abstract model.
     * Guaranteed to use the live system clock and catch ALREADY EXPIRED items.
     */
    public void refresh() {
        LocalDate thresholdDate = LocalDate.now().plusDays(Constants.EXPIRY_THRESHOLD_DAYS);

        // Filter the main list manually to bypass backend exclusions of expired items
        List<Supply> expiring = model.getFilteredSupplyList().stream()
                .filter(s -> !s.getExpiryDate().isAfter(thresholdDate))
                .sorted(Comparator.comparing((Supply s) -> s.getName().toLowerCase())
                        .thenComparing(Supply::getExpiryDate))
                .toList();

        tableItems.setAll(expiring);
        updateFooterStats(expiring);
    }

    /** Recalculates footer statistics. */
    private void updateFooterStats(List<Supply> items) {
        long expired  = items.stream().filter(this::isExpired).count();
        long upcoming = items.size() - expired;
        if (countLabel   != null) countLabel.setText("EXPIRING SOON: " + upcoming);
        if (expiredLabel != null) expiredLabel.setText("ALREADY EXPIRED: " + expired);
    }

    /** Checks if a supply has already passed its expiration date. */
    private boolean isExpired(Supply s) {
        return s.getExpiryDate().isBefore(LocalDate.now());
    }

    /** Returns a generic empty state placeholder label. */
    private Label emptyPlaceholder() {
        Label lbl = new Label("ALL SUPPLIES WITHIN TOLERANCE");
        lbl.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }
}