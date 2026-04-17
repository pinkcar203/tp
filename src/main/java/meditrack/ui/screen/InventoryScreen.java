package meditrack.ui.screen;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import meditrack.commons.core.Constants;
import meditrack.logic.Logic;
import meditrack.model.Model;
import meditrack.model.Supply;
import meditrack.ui.modal.AddSupplyModal;
import meditrack.ui.modal.DeleteSupplyModal;
import meditrack.ui.modal.EditSupplyModal;

/**
 * Field medic inventory screen.
 * Displays supply tracking and facilitates additions, edits, and deletions via the Logic engine.
 */
public class InventoryScreen extends VBox {

    private static final String BG              = "#121410";
    private static final String SURFACE_LOW     = "#1a1c18";
    private static final String SURFACE         = "#1e201c";
    private static final String SURFACE_HIGH    = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT  = "#383a35";
    private static final String PRIMARY         = "#b6d088";
    private static final String PRIMARY_CONT    = "#556b2f";
    private static final String ON_PRIMARY      = "#233600";
    private static final String OUTLINE         = "#8f9284";
    private static final String OUTLINE_VAR     = "#45483c";
    private static final String ON_SURFACE      = "#e3e3dc";
    private static final String SECONDARY       = "#c8c6c6";
    private static final String WARNING         = "#fbbc00";
    private static final String ERROR           = "#ffb4ab";
    private static final int    PAGE_SIZE       = 15;

    private final Model model;
    private final Logic logic;
    private final TableView<Supply> table = new TableView<>();

    private FilteredList<Supply> filtered;
    private SortedList<Supply> sorted;
    private final ObservableList<Supply> pageItems = FXCollections.observableArrayList();
    private int currentPage = 0;

    private Label totalLabel;
    private Label lowStockLabel;
    private Label criticalLabel;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    /**
     * Constructs the primary Inventory screen.
     * Decoupled to rely exclusively on abstract interfaces.
     *
     * @param model The application model providing supply data.
     * @param logic The logic engine providing command execution context.
     */
    public InventoryScreen(Model model, Logic logic) {
        this.model = model;
        this.logic = logic;
        buildUi();
    }

    /** Assembles the core layout and binds observable list listeners. */
    @SuppressWarnings("unchecked")
    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        filtered = new FilteredList<>(model.getFilteredSupplyList(), p -> true);
        sorted = new SortedList<>(filtered, Comparator.comparing((Supply s) -> s.getName().toLowerCase())
                .thenComparing(Supply::getExpiryDate));

        // Guaranteed listener: Updates both table pagination AND footer stats synchronously
        sorted.addListener((javafx.collections.ListChangeListener<Supply>) c -> {
            Platform.runLater(() -> {
                currentPage = 0;
                updatePage();
                updateFooterStats();
            });
        });

        HBox header = buildHeader();
        VBox tableSection = buildTableSection();
        HBox footer = buildFooter();

        VBox.setVgrow(tableSection, Priority.ALWAYS);
        getChildren().addAll(header, tableSection, footer);

        updateFooterStats();
        updatePage();
    }

    /** Builds the top title bar and interactive search box. */
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("SUPPLY INVENTORY");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        Label subtitle = new Label("Real-time logistical tracking.");
        subtitle.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        titleArea.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox searchBar = new HBox(6);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(0, 12, 0, 12));
        searchBar.setPrefHeight(42);
        searchBar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 2 0;");
        Label searchIcon = new Label("⌕");
        searchIcon.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 15px;");
        TextField searchField = new TextField();
        searchField.setPromptText("FILTER BY NAME");
        searchField.setPrefWidth(170);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"
                + " -fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 11px;"
                + " -fx-font-family: 'Consolas', monospace; -fx-prompt-text-fill: " + OUTLINE + ";");
        searchField.textProperty().addListener((obs, old, q) -> {
            currentPage = 0;
            filtered.setPredicate(s -> q == null || q.isBlank()
                    || s.getName().toLowerCase().contains(q.toLowerCase()));
        });
        searchBar.getChildren().addAll(searchIcon, searchField);

        String addNormal = "-fx-background-color: " + PRIMARY_CONT + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        String addHover = "-fx-background-color: " + PRIMARY + "; -fx-text-fill: " + ON_PRIMARY + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        Button addBtn = new Button("+ ADD ");
        addBtn.setPrefHeight(42);
        addBtn.setPadding(new Insets(0, 20, 0, 20));
        addBtn.setStyle(addNormal);
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(addHover));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(addNormal));
        addBtn.setOnAction(e -> AddSupplyModal.show(model, logic, getScene().getWindow()));

        header.getChildren().addAll(titleArea, spacer, searchBar, addBtn);
        return header;
    }

    /** Configures the primary TableView and builds columns. */
    @SuppressWarnings("unchecked")
    private VBox buildTableSection() {
        table.setItems(pageItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(50);
        table.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: transparent;"
                + " -fx-table-cell-border-color: rgba(69,72,60,0.2);"
                + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        table.setPlaceholder(buildEmptyPlaceholder());

        table.skinProperty().addListener((obs, old, skin) -> {
            if (skin != null)
                Platform.runLater(() -> {
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
                String state = state(item);
                String bg = "ERROR".equals(state) ? "rgba(147,0,10,0.15)"
                        : "WARNING".equals(state) ? "rgba(251,188,0,0.04)" : SURFACE_LOW;
                setStyle("-fx-background-color: " + bg + ";");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";"));
                setOnMouseExited(e -> setStyle("-fx-background-color: " + bg + ";"));
            }
        });

        table.getColumns().addAll(
                buildIndexColumn(),
                buildNameColumn(),
                buildQuantityColumn(),
                buildExpiryColumn(),
                buildActionsColumn());

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    /** Builds the index column. */
    private TableColumn<Supply, Number> buildIndexColumn() {
        TableColumn<Supply, Number> idxCol = new TableColumn<>("#");
        idxCol.setMinWidth(50);
        idxCol.setMaxWidth(50);
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
                String color = stateColor(state(getTableView().getItems().get(idx)));
                setText(String.format("%03d", globalIdx));
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
            }
        });
        return idxCol;
    }

    /** Builds the supply name column. */
    private TableColumn<Supply, String> buildNameColumn() {
        TableColumn<Supply, String> nameCol = new TableColumn<>("SUPPLY NAME");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            private final Region dot = new Region();
            private final Label lbl = new Label();
            private final HBox box = new HBox(10, dot, lbl);
            { dot.setMinSize(8, 8); dot.setMaxSize(8, 8); box.setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                String color = stateColor(state(getTableView().getItems().get(idx)));
                dot.setStyle("-fx-background-color: " + color + ";");
                lbl.setText(v.toUpperCase());
                lbl.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 12px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;");
                setGraphic(box);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return nameCol;
    }

    /** Builds the supply quantity column. */
    private TableColumn<Supply, Integer> buildQuantityColumn() {
        TableColumn<Supply, Integer> qtyCol = new TableColumn<>("QUANTITY");
        qtyCol.setMinWidth(100);
        qtyCol.setMaxWidth(100);
        qtyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        qtyCol.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setPadding(new Insets(3, 10, 3, 10)); badge.setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setStyle(""); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                String s = state(getTableView().getItems().get(idx));
                badge.setText(String.format("%03d", v));
                if ("ERROR".equals(s)) {
                    badge.setStyle("-fx-background-color: rgba(255,180,171,0.15); -fx-text-fill: " + ERROR + ";"
                            + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else if ("WARNING".equals(s)) {
                    badge.setStyle("-fx-background-color: rgba(251,188,0,0.1); -fx-text-fill: " + WARNING + ";"
                            + " -fx-border-color: rgba(251,188,0,0.2); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else {
                    badge.setStyle("-fx-background-color: " + SURFACE_HIGHEST + "; -fx-text-fill: " + ON_SURFACE + ";"
                            + " -fx-border-color: rgba(69,72,60,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                }
                setGraphic(badge);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER_LEFT;");
            }
        });
        return qtyCol;
    }

    /** Builds the supply expiry date column. */
    private TableColumn<Supply, LocalDate> buildExpiryColumn() {
        TableColumn<Supply, LocalDate> expiryCol = new TableColumn<>("EXPIRY DATE");
        expiryCol.setMinWidth(150);
        expiryCol.setMaxWidth(170);
        expiryCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getExpiryDate()));
        expiryCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }

                LocalDate today = LocalDate.now(); // or model.getClock() if you use it

                // Check if actually expired
                if (v.isBefore(today)) {
                    setText("EXPIRED");
                    setStyle("-fx-text-fill: #ffb4ab; -fx-font-weight: bold; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
                // Check if expiring within the configured threshold
                else if (v.isBefore(today.plusDays(Constants.EXPIRY_THRESHOLD_DAYS))) {
                    setText(v.toString().replace("-", ".") + " [!]");
                    setStyle("-fx-text-fill: #fbbc00; -fx-font-size: 10px;"
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
        return expiryCol;
    }

    /** Builds the interactive action buttons column. */
    private TableColumn<Supply, Void> buildActionsColumn() {
        TableColumn<Supply, Void> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setMinWidth(100);
        actionsCol.setMaxWidth(110);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("✕");
            private final HBox box = new HBox(4, editBtn, deleteBtn);
            {
                String base = "-fx-background-color: " + SURFACE_HIGHEST + "; -fx-text-fill: " + SECONDARY + ";"
                        + " -fx-font-size: 13px; -fx-cursor: hand; -fx-pref-width: 34; -fx-pref-height: 34;"
                        + " -fx-border-color: rgba(69,72,60,0.25); -fx-border-width: 1; -fx-background-radius: 0;";
                editBtn.setStyle(base);
                deleteBtn.setStyle(base);
                box.setAlignment(Pos.CENTER_RIGHT);

                editBtn.setOnMouseEntered(e -> editBtn.setStyle(base.replace("-fx-text-fill: " + SECONDARY, "-fx-text-fill: " + PRIMARY)));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(base));

                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(base.replace("-fx-text-fill: " + SECONDARY, "-fx-text-fill: " + ERROR)));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(base));

                editBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        Supply supply = getTableView().getItems().get(idx);
                        int modelIdx = -1;
                        ObservableList<Supply> rawList = model.getFilteredSupplyList();
                        for (int i = 0; i < rawList.size(); i++) {
                            if (rawList.get(i) == supply) { modelIdx = i + 1; break; }
                        }
                        if (modelIdx != -1) EditSupplyModal.show(model, logic, supply, modelIdx, getScene().getWindow());
                    }
                });

                deleteBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        Supply supply = getTableView().getItems().get(idx);
                        int modelIdx = -1;
                        ObservableList<Supply> rawList = model.getFilteredSupplyList();
                        for (int i = 0; i < rawList.size(); i++) {
                            if (rawList.get(i) == supply) { modelIdx = i + 1; break; }
                        }
                        if (modelIdx != -1) DeleteSupplyModal.show(model, logic, supply, modelIdx, getScene().getWindow());
                    }
                });
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER_RIGHT;");
            }
        });
        return actionsCol;
    }

    /** Constructs the footer bar. */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44);
        footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        totalLabel = statLabel("TOTAL ITEMS: 0", ON_SURFACE);
        lowStockLabel = statLabel("LOW STOCK: 0", WARNING);
        criticalLabel = statLabel("CRITICAL: 0", ERROR);

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
            int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePage();
            }
        });

        footer.getChildren().addAll(totalLabel, lowStockLabel, criticalLabel, spacer, prevBtn, pageLabel, nextBtn);
        return footer;
    }

    /** Manages table pagination slices. */
    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = sorted.size();
        int to = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? sorted.subList(from, to) : List.of());
        updatePaginationControls();
    }

    /** Adjusts pagination UI state. */
    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
        if (pageLabel != null)
            pageLabel.setText("PAGE " + (currentPage + 1) + " / " + totalPages);
        if (prevBtn != null)
            prevBtn.setDisable(currentPage == 0);
        if (nextBtn != null)
            nextBtn.setDisable(currentPage >= totalPages - 1);
    }

    /** Creates a unified pagination button. */
    private Button pageNavBtn(String text) {
        String base = "-fx-background-color: " + SURFACE_HIGH + "; -fx-text-fill: " + SECONDARY + ";"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0; -fx-pref-height: 28; -fx-padding: 0 12 0 12;";
        String hover = "-fx-background-color: " + PRIMARY_CONT + "; -fx-text-fill: white;"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0; -fx-pref-height: 28; -fx-padding: 0 12 0 12;";
        Button btn = new Button(text);
        btn.setStyle(base);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }

    /** Generates consistent statistical labels. */
    private Label statLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    /** Refreshes summary labels using live data from the Model. */
    private void updateFooterStats() {
        int total = model.getFilteredSupplyList().size();
        int critical = model.getLowStockSupplies(Constants.CRITICAL_STOCK_THRESHOLD_QUANTITY).size();

        // Subtract critical items so they aren't double-counted in Low Stock
        int lowStock = model.getLowStockSupplies(Constants.LOW_STOCK_THRESHOLD_QUANTITY).size() - critical;

        if (totalLabel != null) totalLabel.setText("TOTAL ITEMS: " + total);
        if (lowStockLabel != null) lowStockLabel.setText("LOW STOCK: " + lowStock);
        if (criticalLabel != null) criticalLabel.setText("CRITICAL: " + critical);
    }

    /** Reloads the screen data safely on the UI thread. */
    public void refresh() {
        Platform.runLater(() -> {
            updateFooterStats();
            updatePage();
        });
    }

    /** Categorizes supply health based on quantity and expiration. */
    private String state(Supply s) {
        LocalDate today = LocalDate.now();
        if (s.getExpiryDate().isBefore(today) || s.getQuantity() < Constants.CRITICAL_STOCK_THRESHOLD_QUANTITY) {
            return "ERROR";
        }
        if (s.getQuantity() < Constants.LOW_STOCK_THRESHOLD_QUANTITY
                || s.getExpiryDate().isBefore(today.plusDays(Constants.EXPIRY_THRESHOLD_DAYS))) {
            return "WARNING";
        }
        return "NORMAL";
    }

    /** Converts logical health states to CSS colors. */
    private String stateColor(String state) {
        return switch (state) {
            case "ERROR"   -> ERROR;
            case "WARNING" -> WARNING;
            default        -> PRIMARY;
        };
    }

    /** Renders the empty state visual. */
    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("NO SUPPLY RECORDS");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }
}