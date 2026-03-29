package meditrack.ui.screen;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.UpdateStatusCommand;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.model.Status;
import meditrack.storage.StorageManager;
import meditrack.ui.modal.AddPersonnelModal;
import meditrack.ui.modal.RemovePersonnelModal;

/**
 * Personnel Management screen.
 */
public class PersonnelScreen extends VBox {

    private static final String BG = "#121410";
    private static final String SURFACE_LOW = "#1a1c18";
    private static final String SURFACE = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String SURFACE_HIGHEST = "#333531";
    private static final String SURFACE_BRIGHT = "#383a35";
    private static final String PRIMARY = "#b6d088";
    private static final String PRIMARY_CONT = "#556b2f";
    private static final String ON_PRIMARY = "#233600";
    private static final String OUTLINE = "#8f9284";
    private static final String OUTLINE_VAR = "#45483c";
    private static final String ON_SURFACE = "#e3e3dc";
    private static final String SECONDARY = "#c8c6c6";
    private static final String WARNING = "#fbbc00";
    private static final String ERROR = "#ffb4ab";
    private static final int PAGE_SIZE = 15;

    private final ModelManager model;
    private final StorageManager storage;
    private final boolean canAddDelete;
    private final boolean canEditStatus;

    private final ObservableList<Personnel> tableData = FXCollections.observableArrayList();
    private final FilteredList<Personnel> filteredData = new FilteredList<>(tableData, p -> true);
    private final ObservableList<Personnel> pageItems = FXCollections.observableArrayList();
    private int currentPage = 0;

    private final TableView<Personnel> table = new TableView<>();
    private Label feedbackLabel;
    private Label totalLabel;
    private Label fitLabel;
    private Label alertLabel;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    /**
     * Constructs the PersonnelScreen.
     *
     * @param model   data model managing personnel records
     * @param storage persists data changes after edits
     */
    public PersonnelScreen(ModelManager model, StorageManager storage) {
        this.model = model;
        this.storage = storage;
        Role currentRole = Session.getInstance().getRole();
        this.canAddDelete = (currentRole == Role.MEDICAL_OFFICER || currentRole == Role.PLATOON_COMMANDER);
        this.canEditStatus = (currentRole == Role.MEDICAL_OFFICER || currentRole == Role.FIELD_MEDIC);
        buildUi();
        refresh();
    }

    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        // Reset to page 0 whenever the filtered result set changes 
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
        Label title = new Label("PERSONNEL MANAGEMENT");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        String roleHint = buildRoleHint();
        Label subtitle = new Label(roleHint);
        subtitle.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        titleArea.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search bar
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

        if (canAddDelete) {
            header.getChildren().add(buildAddButton());
        }

        return header;
    }

    private String buildRoleHint() {
        Role role = Session.getInstance().getRole();
        return switch (role) {
            case FIELD_MEDIC -> "Showing FIT and CASUALTY personnel. You may flag CASUALTY status.";
            case MEDICAL_OFFICER -> "Full access.";
            case PLATOON_COMMANDER -> "Roster management.";
            case LOGISTICS_OFFICER -> "Personnel records.";
        };
    }

    private Button buildAddButton() {
        String normal = "-fx-background-color: " + PRIMARY_CONT + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        String hover = "-fx-background-color: " + PRIMARY + "; -fx-text-fill: " + ON_PRIMARY + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        Button btn = new Button("+ ADD");
        btn.setPrefHeight(42);
        btn.setPadding(new Insets(0, 20, 0, 20));
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        btn.setOnAction(e -> openAddModal());
        return btn;
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

        styleTableHeaders();
        buildRowFactory();
        buildColumns();

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    private void styleTableHeaders() {
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
    }

    private void buildRowFactory() {
        table.setRowFactory(tv -> new TableRow<Personnel>() {
            {
                selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    Personnel item = getItem();
                    if (item == null || isEmpty()) {
                        return;
                    }
                    applyRowStyle(item, isSelected);
                });
            }

            private void applyRowStyle(Personnel item, boolean selected) {
                if (selected) {
                    setStyle("-fx-background-color: rgba(85,107,47,0.45);"
                            + " -fx-border-color: " + PRIMARY + "; -fx-border-width: 0 0 0 3;");
                } else {
                    setStyle("-fx-background-color: " + rowBackground(item.getStatus()) + ";");
                }
            }

            @Override
            protected void updateItem(Personnel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    return;
                }
                applyRowStyle(item, isSelected());
                setOnMouseEntered(e -> {
                    if (!isSelected()) {
                        setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";");
                    }
                });
                setOnMouseExited(e -> applyRowStyle(item, isSelected()));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void buildColumns() {
        table.getColumns().addAll(
                buildIndexColumn(),
                buildNameColumn(),
                buildStatusColumn());

        // Blood group and allergies are only relevant in the Medical Officer view
        if (Session.getInstance().getRole() == Role.MEDICAL_OFFICER) {
            table.getColumns().addAll(buildBloodGroupColumn(), buildAllergiesColumn());
        }
    }

    private TableColumn<Personnel, String> buildIndexColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        // Display global position across all pages
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

        if (!canEditStatus) {
            col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));
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
                    badge.setText(v.toUpperCase().replace("_", " "));
                    badge.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                            + " -fx-font-family: 'Consolas', monospace;"
                            + " -fx-border-color: " + color + "; -fx-border-width: 1;"
                            + " -fx-background-color: transparent;");
                    setGraphic(badge);
                    setStyle("-fx-background-color: transparent;");
                }
            });
        } else {
            col.setCellFactory(c -> buildEditableStatusCell());
        }

        return col;
    }

    private TableColumn<Personnel, String> buildBloodGroupColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("BLOOD GROUP");
        col.setMinWidth(110);
        col.setMaxWidth(130);
        col.setCellValueFactory(cd -> {
            meditrack.model.BloodGroup bg = cd.getValue().getBloodGroup();
            return new SimpleStringProperty(bg != null ? bg.display() : "—");
        });
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
                badge.setText(v);
                boolean unknown = "—".equals(v) || "UNKNOWN".equals(v);
                badge.setStyle("-fx-text-fill: " + (unknown ? OUTLINE : ON_SURFACE) + ";"
                        + " -fx-font-size: 11px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;");
                setGraphic(badge);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<Personnel, String> buildAllergiesColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("ALLERGIES");
        col.setCellValueFactory(cd -> {
            String allergies = cd.getValue().getAllergies();
            return new SimpleStringProperty(allergies.isBlank() ? "NONE" : allergies.toUpperCase());
        });
        col.setCellFactory(c -> new TableCell<Personnel, String>() {
            private final Label lbl = new Label();

            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                boolean none = "NONE".equals(v);
                lbl.setText(v);
                lbl.setWrapText(false);
                lbl.setStyle("-fx-text-fill: " + (none ? OUTLINE : WARNING) + ";"
                        + " -fx-font-size: 10px; -fx-font-family: 'Consolas', monospace;");
                setGraphic(lbl);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return col;
    }

    @SuppressWarnings("unchecked")
    private TableCell<Personnel, String> buildEditableStatusCell() {
        ObservableList<Status> options = (Session.getInstance().getRole() == Role.FIELD_MEDIC)
                ? FXCollections.observableArrayList(Status.CASUALTY)
                : FXCollections.observableArrayList(Status.values());

        return new TableCell<Personnel, String>() {
            private final ComboBox<Status> combo = new ComboBox<>(options);
            {
                styleStatusCombo(combo);
                combo.setOnAction(e -> {
                    Personnel p = getTableRow().getItem();
                    if (p == null || combo.getValue() == null)
                        return;
                    int idx = model.getFilteredPersonnelList(null).indexOf(p) + 1;
                    try {
                        new UpdateStatusCommand(idx, combo.getValue()).execute(model);
                        setFeedback(" for " + p.getName() + ".", false);
                        saveData();
                        refresh();
                    } catch (CommandException ex) {
                        setFeedback("Error: " + ex.getMessage(), true);
                    }
                });
            }

            /**
             * Renders the status combo box for editable cells.
             *
             * @param item  unused status is read from the row item
             * @param empty true if this cell has no data
             */
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    Status current = getTableRow().getItem().getStatus();
                    combo.setValue(current);
                    refreshComboButtonCell(combo, current);
                    setGraphic(combo);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        };
    }

    // Footer

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44);
        footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        boolean isMedicalOfficer = (Session.getInstance().getRole() == Role.MEDICAL_OFFICER);

        totalLabel = statLabel("TOTAL: 0", SECONDARY);
        fitLabel = statLabel("FIT: 0", PRIMARY);
        alertLabel = statLabel("ATTENTION: 0", WARNING);

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

        feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 10px; -fx-font-family: 'Consolas', monospace;");

        if (!isMedicalOfficer) {
            footer.getChildren().addAll(totalLabel, fitLabel, alertLabel);
        }

        footer.getChildren().addAll(spacer, prevBtn, pageLabel, nextBtn);

        if (canAddDelete) {
            if (!isMedicalOfficer) {
                footer.getChildren().add(feedbackLabel);
            }
            footer.getChildren().add(buildRemoveButton());
        } else if (!isMedicalOfficer) {
            footer.getChildren().add(feedbackLabel);
        }

        return footer;
    }

    private Button buildRemoveButton() {
        String base = "-fx-background-color: " + SURFACE_HIGH + "; -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;";
        String hover = "-fx-background-color: rgba(147,0,10,0.4); -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;";
        Button btn = new Button("REMOVE");
        btn.setPrefHeight(30);
        btn.setPadding(new Insets(0, 14, 0, 14));
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        btn.setOnAction(e -> openRemoveModal());
        return btn;
    }

    // Pagination

    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = filteredData.size();
        int to = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? filteredData.subList(from, to) : List.of());
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredData.size() / PAGE_SIZE));
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

    // Actions

    /**
     * Opens the add-personnel modal and executes the resulting command.
     */
    private void openAddModal() {
        AddPersonnelModal.show(model, storage, getScene().getWindow(),
                msg -> {
                    setFeedback(msg, false);
                    refresh();
                },
                msg -> setFeedback(msg, true));
    }

    /**
     * Opens the remove-personnel modal for the currently selected row.
     */
    private void openRemoveModal() {
        Personnel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setFeedback("Select a row to remove.", true);
            return;
        }
        // Look up position in the full model list to get the correct 1-based index
        int idx = model.getFilteredPersonnelList(null).indexOf(selected) + 1;
        RemovePersonnelModal.show(model, storage, selected, idx, getScene().getWindow(),
                msg -> {
                    setFeedback(msg, false);
                    refresh();
                },
                msg -> setFeedback(msg, true));
    }

    /**
     * Persists the current model state to disk.
     */
    private void saveData() {
        try {
            storage.saveMediTrackData(model.getMediTrack());
        } catch (IOException ex) {
            setFeedback("Warning: could not save — " + ex.getMessage(), true);
        }
    }

    // Refresh

    /**
     * Reloads the table and footer stats from the model.
     */
    public void refresh() {
        Role currentRole = Session.getInstance().getRole();
        List<Personnel> all = model.getFilteredPersonnelList(null);

        List<Personnel> visible = (currentRole == Role.FIELD_MEDIC)
                ? all.stream()
                        .filter(p -> p.getStatus() == Status.FIT || p.getStatus() == Status.CASUALTY)
                        .collect(Collectors.toList())
                : all;

        tableData.setAll(visible);
        // filteredData listener resets page and calls updatePage() automatically
        updateFooterStats(visible);
    }

    private void updateFooterStats(List<Personnel> visible) {
        long fit = visible.stream().filter(p -> p.getStatus() == Status.FIT).count();
        long alert = visible.stream()
                .filter(p -> p.getStatus() == Status.MC
                        || p.getStatus() == Status.LIGHT_DUTY
                        || p.getStatus() == Status.CASUALTY)
                .count();
        if (totalLabel != null)
            totalLabel.setText("TOTAL: " + visible.size());
        if (fitLabel != null)
            fitLabel.setText("FIT: " + fit);
        if (alertLabel != null)
            alertLabel.setText("ATTENTION: " + alert);
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

    private void styleStatusCombo(ComboBox<Status> combo) {
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + OUTLINE_VAR + ";"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<Status>() {
            @Override
            protected void updateItem(Status s, boolean empty) {
                super.updateItem(s, empty);
                if (s == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: " + SURFACE + ";");
                    return;
                }
                setText(s.toString().replace("_", " "));
                String color = statusColor(s);
                String base = "-fx-background-color: " + SURFACE + "; -fx-text-fill: " + color + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 6 10 6 10;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle(base.replace(SURFACE + ";", SURFACE_HIGH + ";")));
                setOnMouseExited(e -> {
                    if (!isSelected())
                        setStyle(base);
                });
            }
        });
    }

    private void refreshComboButtonCell(ComboBox<Status> combo, Status current) {
        combo.setButtonCell(combo.getCellFactory().call(null));
        combo.setValue(current);
    }

    private Label statLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("NO PERSONNEL ON RECORD");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    /**
     * Updates the feedback label with a success or error message.
     *
     * @param message the text to display
     * @param isError true for error styling, false for success
     */
    private void setFeedback(String message, boolean isError) {
        if (feedbackLabel == null)
            return;
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-font-size: 10px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-text-fill: " + (isError ? ERROR : PRIMARY) + ";");
    }
}
