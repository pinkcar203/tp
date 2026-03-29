package meditrack.ui.screen;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Status;

/**
 * Medical Officer screen showing all personnel requiring medical attention
 * (MC, LIGHT_DUTY, CASUALTY, PENDING).
 */
public class MedicalAttentionScreen extends VBox {

    private static final String BG = "#121410";
    private static final String SURFACE_LOW = "#1a1c18";
    private static final String SURFACE = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT = "#383a35";
    private static final String PRIMARY = "#b6d088";
    private static final String OUTLINE = "#8f9284";
    private static final String OUTLINE_VAR = "#45483c";
    private static final String ON_SURFACE = "#e3e3dc";
    private static final String SECONDARY = "#c8c6c6";
    private static final String WARNING = "#fbbc00";
    private static final String ERROR = "#ffb4ab";
    private static final int PAGE_SIZE = 15;

    private final ModelManager model;
    private final ObservableList<Personnel> tableData = FXCollections.observableArrayList();
    private final FilteredList<Personnel> filteredData = new FilteredList<>(tableData, p -> true);
    private final SortedList<Personnel> sortedData = new SortedList<>(filteredData,
            Comparator.comparingInt((Personnel p) -> getStatusPriority(p.getStatus()))
                    .thenComparing((p1, p2) -> p2.getLastModified().compareTo(p1.getLastModified())));
    private final ObservableList<Personnel> pageItems = FXCollections.observableArrayList();
    private int currentPage = 0;

    private final TableView<Personnel> table = new TableView<>();
    private Label totalLabel;
    private Label mcLabel;
    private Label ldLabel;
    private Label casLabel;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    /**
     * Constructs the Medical Attention monitoring screen.
     *
     * @param model the data model managing personnel records
     */
    public MedicalAttentionScreen(ModelManager model) {
        this.model = model;
        buildUi();
        refresh();
    }

    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        filteredData.addListener((javafx.collections.ListChangeListener<Personnel>) c -> {
            currentPage = 0;
            updatePage();
        });

        VBox tableSection = buildTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        getChildren().addAll(buildHeader(), tableSection, buildFooter());
    }

    // Header

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("MEDICAL ATTENTION");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        Label subtitle = new Label("Personnel on MC, Light Duty, Casualty, or Pending — requires triage.");
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
            filteredData.setPredicate(p -> q == null || q.isBlank()
                    || p.getName().toLowerCase().contains(q.toLowerCase()));
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
            if (skin != null)
                Platform.runLater(() -> {
                    javafx.scene.Node hdrBg = table.lookup(".column-header-background");
                    if (hdrBg != null)
                        hdrBg.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                    table.lookupAll(".column-header").forEach(n -> n.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: transparent transparent "
                                    + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;"));
                    table.lookupAll(".column-header .label").forEach(n -> n.setStyle(
                            "-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                    + " -fx-font-family: 'Consolas', monospace;"));
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null)
                        filler.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                });
        });

        table.setRowFactory(tv -> new TableRow<Personnel>() {
            @Override
            protected void updateItem(Personnel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    return;
                }
                String bg = rowBackground(item.getStatus());
                setStyle("-fx-background-color: " + bg + ";");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";"));
                setOnMouseExited(e -> setStyle("-fx-background-color: " + bg + ";"));
            }
        });

        table.getColumns().addAll(buildIndexColumn(), buildNameColumn(), buildStatusColumn());

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    private TableColumn<Personnel, String> buildIndexColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setCellValueFactory(cd -> {
            int pageIdx = cd.getTableView().getItems().indexOf(cd.getValue());
            return new SimpleStringProperty(String.valueOf(currentPage * PAGE_SIZE + pageIdx + 1));
        });
        col.setCellFactory(c -> new TableCell<Personnel, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
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
                String color = statusColor(getTableView().getItems().get(idx).getStatus());
                setText(String.format("%03d", Integer.parseInt(v)));
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<Personnel, String> buildNameColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("NAME");
        col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        col.setCellFactory(c -> new TableCell<Personnel, String>() {
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
                String color = statusColor(getTableView().getItems().get(idx).getStatus());
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

    private TableColumn<Personnel, String> buildStatusColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("STATUS");
        col.setMinWidth(160);
        col.setMaxWidth(200);
        col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().name()));
        col.setCellFactory(c -> new TableCell<Personnel, String>() {
            private final Label badge = new Label();
            {
                badge.setPadding(new Insets(3, 10, 3, 10));
            }

            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                Status status;
                try {
                    status = Status.valueOf(v);
                } catch (IllegalArgumentException e) {
                    status = Status.PENDING;
                }
                String color = statusColor(status);
                String rgba40 = hexToRgba(color, 0.4);
                String rgba08 = hexToRgba(color, 0.08);
                badge.setText(v.replace("_", " "));
                badge.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;"
                        + " -fx-border-color: " + rgba40 + "; -fx-border-width: 1;"
                        + " -fx-background-color: " + rgba08 + ";");
                setGraphic(badge);
                setStyle("-fx-background-color: transparent;");
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

        totalLabel = statLabel("TOTAL: 0", SECONDARY);
        mcLabel = statLabel("MC: 0", WARNING);
        ldLabel = statLabel("LIGHT DUTY: 0", WARNING);
        casLabel = statLabel("CASUALTY: 0", ERROR);

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
            int totalPages = Math.max(1, (int) Math.ceil((double) filteredData.size() / PAGE_SIZE));
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePage();
            }
        });

        footer.getChildren().addAll(totalLabel, mcLabel, ldLabel, casLabel, spacer, prevBtn, pageLabel, nextBtn);
        return footer;
    }

    // Pagination

    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = sortedData.size();
        int to = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? sortedData.subList(from, to) : List.of());
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) sortedData.size() / PAGE_SIZE));
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
        String hover = "-fx-background-color: rgba(85,107,47,0.6); -fx-text-fill: white;"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0; -fx-pref-height: 28; -fx-padding: 0 12 0 12;";
        Button btn = new Button(text);
        btn.setStyle(base);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }

    // Refresh

    /**
     * Reloads the table with the latest personnel requiring medical attention.
     */
    public void refresh() {
        List<Personnel> all = model.getFilteredPersonnelList(null);
        List<Personnel> medicalCases = all.stream()
                .filter(p -> p.getStatus() == Status.PENDING
                        || p.getStatus() == Status.CASUALTY
                        || p.getStatus() == Status.MC
                        || p.getStatus() == Status.LIGHT_DUTY)
                .collect(Collectors.toList());
        tableData.setAll(medicalCases);
        updateFooterStats(medicalCases);
    }

    private void updateFooterStats(List<Personnel> items) {
        long mc = items.stream().filter(p -> p.getStatus() == Status.MC).count();
        long ld = items.stream().filter(p -> p.getStatus() == Status.LIGHT_DUTY).count();
        long cas = items.stream().filter(p -> p.getStatus() == Status.CASUALTY).count();
        if (totalLabel != null)
            totalLabel.setText("TOTAL: " + items.size());
        if (mcLabel != null)
            mcLabel.setText("MC: " + mc);
        if (ldLabel != null)
            ldLabel.setText("LIGHT DUTY: " + ld);
        if (casLabel != null)
            casLabel.setText("CASUALTY: " + cas);
    }

    // Helpers

    private String rowBackground(Status status) {
        return switch (status) {
            case CASUALTY -> "rgba(147,0,10,0.15)";
            case MC, LIGHT_DUTY -> "rgba(251,188,0,0.04)";
            default -> SURFACE_LOW;
        };
    }

    private String statusColor(Status status) {
        return switch (status) {
            case FIT -> PRIMARY;
            case LIGHT_DUTY -> WARNING;
            case MC -> WARNING;
            case CASUALTY -> ERROR;
            case PENDING -> OUTLINE;
        };
    }

    private int getStatusPriority(Status status) {
        return switch (status) {
            case CASUALTY -> 1;
            case PENDING -> 2;
            case LIGHT_DUTY -> 3;
            case MC -> 4;
            default -> 5;
        };
    }

    private Label statLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private static String hexToRgba(String hex, double alpha) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }

    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("ALL PERSONNEL ARE FIT — NO MEDICAL ATTENTION REQUIRED");
        lbl.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }
}
