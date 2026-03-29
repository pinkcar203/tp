package meditrack.ui.screen;

import java.time.LocalDate;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import meditrack.model.ModelManager;
import meditrack.model.Supply;

/**
 * Supply Levels screen full stock overview for Logistics Officers.
 */
public class SupplyLevelsScreen extends VBox {
    private static final String BG = "#121410";
    private static final String SURFACE_LOW = "#1a1c18";
    private static final String SURFACE = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT = "#383a35";
    private static final String PRIMARY = "#b6d088";
    private static final String PRIMARY_CONT = "#556b2f";
    private static final String OUTLINE = "#8f9284";
    private static final String OUTLINE_VAR = "#45483c";
    private static final String ON_SURFACE = "#e3e3dc";
    private static final String SECONDARY = "#c8c6c6";
    private static final String WARNING = "#fbbc00";
    private static final String ERROR = "#ffb4ab";
    private static final int PAGE_SIZE = 15;

    private final ModelManager model;
    private final TableView<Supply> table = new TableView<>();

    private FilteredList<Supply> filtered;
    private final ObservableList<Supply> pageItems = FXCollections.observableArrayList();
    private int currentPage = 0;

    private Label totalLabel;
    private Label lowStockLabel;
    private Label criticalLabel;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    /**
     * @param model full supply list data source
     */
    public SupplyLevelsScreen(ModelManager model) {
        this.model = model;
        buildUi();
    }

    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        filtered = new FilteredList<>(model.getFilteredSupplyList(), p -> true);
        filtered.addListener((javafx.collections.ListChangeListener<Supply>) c -> {
            currentPage = 0;
            updatePage();
        });

        VBox tableSection = buildTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        getChildren().addAll(buildHeader(), tableSection, buildFooter());

        model.getFilteredSupplyList().addListener(
                (javafx.collections.ListChangeListener<Supply>) c -> updateFooterStats());
        updateFooterStats();
        updatePage();
    }

    // Header

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("SUPPLY LEVELS");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        Label subtitle = new Label("Full stock overview. View-only.");
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

        header.getChildren().addAll(titleArea, spacer, searchBar);
        return header;
    }

    // Table

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
            if (skin != null) {
                Platform.runLater(() -> {
                    javafx.scene.Node hdrBg = table.lookup(".column-header-background");
                    if (hdrBg != null) {
                        hdrBg.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                    }
                    table.lookupAll(".column-header").forEach(n -> n.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: transparent transparent "
                                    + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;"));
                    table.lookupAll(".column-header .label").forEach(n -> n.setStyle(
                            "-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                    + " -fx-font-family: 'Consolas', monospace;"));
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) {
                        filler.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                    }
                });
            }
        });

        table.setRowFactory(tv -> new javafx.scene.control.TableRow<Supply>() {
            @Override
            protected void updateItem(Supply item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    return;
                }
                String st = state(item);
                String bg = "ERROR".equals(st) ? "rgba(147,0,10,0.15)"
                        : "WARNING".equals(st) ? "rgba(251,188,0,0.04)" : SURFACE_LOW;
                setStyle("-fx-background-color: " + bg + ";");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";"));
                setOnMouseExited(e -> setStyle("-fx-background-color: " + bg + ";"));
            }
        });

        TableColumn<Supply, Number> idxCol = buildIndexColumn();
        TableColumn<Supply, String> nameCol = buildNameColumn();
        TableColumn<Supply, Integer> qtyCol = buildQuantityColumn();
        TableColumn<Supply, LocalDate> expiryCol = buildExpiryColumn();

        table.getColumns().addAll(idxCol, nameCol, qtyCol, expiryCol);

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    private TableColumn<Supply, Number> buildIndexColumn() {
        TableColumn<Supply, Number> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setCellValueFactory(c -> {
            int pageIdx = c.getTableView().getItems().indexOf(c.getValue());
            return new ReadOnlyObjectWrapper<>(currentPage * PAGE_SIZE + pageIdx + 1);
        });
        col.setCellFactory(c -> new TableCell<Supply, Number>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) {
                    setText(null);
                    return;
                }
                String color = stateColor(state(getTableView().getItems().get(idx)));
                setText(String.format("%03d", v.intValue()));
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<Supply, String> buildNameColumn() {
        TableColumn<Supply, String> col = new TableColumn<>("SUPPLY NAME");
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        col.setCellFactory(c -> new TableCell<Supply, String>() {
            private final Region dot = new Region();
            private final Label lbl = new Label();
            private final HBox box = new HBox(10, dot, lbl);
            {
                dot.setMinSize(8, 8);
                dot.setMaxSize(8, 8);
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                String color = stateColor(state(getTableView().getItems().get(idx)));
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

    private TableColumn<Supply, Integer> buildQuantityColumn() {
        TableColumn<Supply, Integer> col = new TableColumn<>("QUANTITY");
        col.setMinWidth(100);
        col.setMaxWidth(120);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        col.setCellFactory(c -> new TableCell<Supply, Integer>() {
            private final Label badge = new Label();
            {
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                String st = state(getTableView().getItems().get(idx));
                badge.setText(String.format("%03d", v));
                if ("ERROR".equals(st)) {
                    badge.setStyle("-fx-background-color: rgba(255,180,171,0.15); -fx-text-fill: " + ERROR + ";"
                            + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;"
                            + " -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Consolas', monospace;");
                } else if ("WARNING".equals(st)) {
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

    private TableColumn<Supply, LocalDate> buildExpiryColumn() {
        TableColumn<Supply, LocalDate> col = new TableColumn<>("EXPIRY DATE");
        col.setMinWidth(150);
        col.setMaxWidth(170);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getExpiryDate()));
        col.setCellFactory(c -> new TableCell<Supply, LocalDate>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) {
                    setText(null);
                    return;
                }
                String st = state(getTableView().getItems().get(idx));
                if ("ERROR".equals(st) && v.isBefore(LocalDate.now())) {
                    setText("EXPIRED");
                    setStyle("-fx-text-fill: " + ERROR + "; -fx-font-weight: bold; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                } else if ("WARNING".equals(st)) {
                    setText(v.toString().replace("-", ".") + " [!]");
                    setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                } else {
                    setText(v.toString().replace("-", "."));
                    setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 11px;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
                }
            }
        });
        return col;
    }

    // Footer

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

    // Pagination

    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = filtered.size();
        int to = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? filtered.subList(from, to) : List.of());
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (pageLabel != null)
            pageLabel.setText("PAGE " + (currentPage + 1) + " / " + totalPages);
        if (prevBtn != null)
            prevBtn.setDisable(currentPage == 0);
        if (nextBtn != null)
            nextBtn.setDisable(currentPage >= totalPages - 1);
    }

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

    // Helpers

    private Label statLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private void updateFooterStats() {
        int total = model.getFilteredSupplyList().size();
        int lowStock = model.getLowStockSupplies(10).size();
        int critical = model.getLowStockSupplies(5).size();
        if (totalLabel != null)
            totalLabel.setText("TOTAL ITEMS: " + total);
        if (lowStockLabel != null)
            lowStockLabel.setText("LOW STOCK: " + lowStock);
        if (criticalLabel != null)
            criticalLabel.setText("CRITICAL: " + critical);
    }

    private String state(Supply s) {
        LocalDate today = LocalDate.now();
        if (s.getExpiryDate().isBefore(today) || s.getQuantity() == 0) {
            return "ERROR";
        }
        if (s.getQuantity() < 10 || s.getExpiryDate().isBefore(today.plusDays(30))) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private String stateColor(String state) {
        return switch (state) {
            case "ERROR" -> ERROR;
            case "WARNING" -> WARNING;
            default -> PRIMARY;
        };
    }

    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("NO SUPPLY RECORDS");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }
}
