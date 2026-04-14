package meditrack.ui.screen;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import meditrack.logic.Logic;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.UpdateStatusCommand;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Role;
import meditrack.model.Status;
import meditrack.ui.modal.AddPersonnelModal;
import meditrack.ui.modal.RemovePersonnelModal;
import meditrack.ui.modal.EditPersonnelModal;

/**
 * Personnel Management screen.
 * Implements strict RBAC to determine which columns and actions are available based on the session's Role.
 */
public class PersonnelScreen extends VBox {

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
    private final boolean canAddDelete;
    private final boolean canEditStatus;

    private final ObservableList<Personnel> tableData = FXCollections.observableArrayList();
    private final FilteredList<Personnel> filteredData = new FilteredList<>(tableData, p -> true);
    private final SortedList<Personnel> sortedData = new SortedList<>(filteredData,
            Comparator.comparing(p -> p.getName().toLowerCase()));
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
     * Fully decoupled to rely only on abstract interfaces.
     *
     * @param model The application data model.
     * @param logic The logic engine used for executing commands.
     */
    public PersonnelScreen(Model model, Logic logic) {
        this.model = model;
        this.logic = logic;
        Role currentRole = model.getSession().getRole();
        this.canAddDelete = (currentRole == Role.MEDICAL_OFFICER || currentRole == Role.PLATOON_COMMANDER);
        this.canEditStatus = (currentRole == Role.MEDICAL_OFFICER || currentRole == Role.FIELD_MEDIC);
        buildUi();
        refresh();
    }

    /** Assembles the core layout structure of the screen. */
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

    /** Builds the top navigation and search header. */
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
        Label subtitle = new Label(buildRoleHint());
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

        if (canAddDelete) {
            header.getChildren().add(buildAddButton());
        }

        return header;
    }

    /** Returns context-aware subtitle text based on the user's role. */
    private String buildRoleHint() {
        Role role = model.getSession().getRole();
        return switch (role) {
            case FIELD_MEDIC -> "Showing FIT and CASUALTY personnel. You may flag CASUALTY status.";
            case MEDICAL_OFFICER -> "Full access.";
            case PLATOON_COMMANDER -> "Roster management.";
            case LOGISTICS_OFFICER -> "Personnel records.";
        };
    }

    /** Generates the Add Personnel button. */
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

    /** Builds the core TableView structure. */
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

    /** Applies CSS hacks to override default JavaFX table header aesthetics. */
    private void styleTableHeaders() {
        table.skinProperty().addListener((obs, old, skin) -> {
            if (skin != null)
                Platform.runLater(() -> {
                    Node hdrBg = table.lookup(".column-header-background");
                    if (hdrBg != null) hdrBg.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                    table.lookupAll(".column-header").forEach(n -> n.setStyle(
                            "-fx-background-color: transparent; -fx-border-color: transparent transparent "
                                    + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;"));
                    table.lookupAll(".column-header .label").forEach(n -> n.setStyle(
                            "-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                    + " -fx-font-family: 'Consolas', monospace;"));
                    Node filler = table.lookup(".filler");
                    if (filler != null) filler.setStyle("-fx-background-color: " + SURFACE_HIGH + ";");
                });
        });
    }

    /** Creates custom row styling to highlight selected and CASUALTY personnel. */
    private void buildRowFactory() {
        table.setRowFactory(tv -> new TableRow<>() {
            {
                selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    Personnel item = getItem();
                    if (item != null && !isEmpty()) applyRowStyle(item, isSelected);
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
                    if (!isSelected()) setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";");
                });
                setOnMouseExited(e -> applyRowStyle(item, isSelected()));
            }
        });
    }

    /** Injects the required columns based on RBAC visibility rules. */
    private void buildColumns() {
        table.getColumns().addAll(
                buildIndexColumn(),
                buildNameColumn(),
                buildStatusColumn());

        if (model.getSession().getRole() == Role.MEDICAL_OFFICER) {
            table.getColumns().addAll(buildBloodGroupColumn(), buildAllergiesColumn(), buildActionsColumn());
        }
    }

    /** Builds the numbered index column. */
    private TableColumn<Personnel, String> buildIndexColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setCellValueFactory(cd -> {
            int pageIdx = cd.getTableView().getItems().indexOf(cd.getValue());
            return new SimpleStringProperty(String.valueOf(currentPage * PAGE_SIZE + pageIdx + 1));
        });
        col.setCellFactory(c -> new TableCell<>() {
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

    /** Builds the personnel name column. */
    private TableColumn<Personnel, String> buildNameColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("NAME");
        col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        col.setCellFactory(c -> new TableCell<>() {
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
                if (idx < 0 || idx >= getTableView().getItems().size()) return;
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

    /** Builds the personnel status column. */
    private TableColumn<Personnel, String> buildStatusColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("STATUS");
        col.setMinWidth(160);
        col.setMaxWidth(200);

        if (!canEditStatus) {
            col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));
            col.setCellFactory(c -> new TableCell<>() {
                private final Label badge = new Label();
                { badge.setPadding(new Insets(3, 10, 3, 10)); }
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

    /** Builds the blood group column. */
    private TableColumn<Personnel, String> buildBloodGroupColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("BLOOD GROUP");
        col.setMinWidth(110);
        col.setMaxWidth(130);
        col.setCellValueFactory(cd -> {
            meditrack.model.BloodGroup bg = cd.getValue().getBloodGroup();
            return new SimpleStringProperty(bg != null ? bg.display() : "—");
        });
        col.setCellFactory(c -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setPadding(new Insets(3, 10, 3, 10)); }
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

    /** Builds the allergies column. */
    private TableColumn<Personnel, String> buildAllergiesColumn() {
        TableColumn<Personnel, String> col = new TableColumn<>("ALLERGIES");
        col.setCellValueFactory(cd -> {
            String allergies = cd.getValue().getAllergies();
            return new SimpleStringProperty(allergies.isBlank() ? "NONE" : allergies.toUpperCase());
        });
        col.setCellFactory(c -> new TableCell<>() {
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

    /** Builds the interactive action buttons column for Medical Officers. */
    private TableColumn<Personnel, Void> buildActionsColumn() {
        TableColumn<Personnel, Void> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setMinWidth(70);
        actionsCol.setMaxWidth(80);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✎");
            private final HBox box = new HBox(editBtn);
            {
                String base = "-fx-background-color: #333531; -fx-text-fill: #c8c6c6; -fx-font-size: 13px; -fx-cursor: hand; -fx-pref-width: 34; -fx-pref-height: 34;"
                        + " -fx-border-color: rgba(69,72,60,0.25); -fx-border-width: 1; -fx-background-radius: 0;";
                editBtn.setStyle(base);
                box.setAlignment(Pos.CENTER);

                editBtn.setOnMouseEntered(e -> editBtn.setStyle(base.replace("-fx-text-fill: #c8c6c6", "-fx-text-fill: #b6d088")));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(base));

                editBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        Personnel p = getTableView().getItems().get(idx);
                        int modelIdx = model.getFilteredPersonnelList(null).indexOf(p) + 1;
                        EditPersonnelModal.show(model, logic, p, modelIdx, getScene().getWindow());
                        refresh();
                    }
                });
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || model.getSession().getRole() != Role.MEDICAL_OFFICER) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
                setStyle("-fx-background-color: transparent; -fx-alignment: CENTER;");
            }
        });
        return actionsCol;
    }

    /** Creates an interactive dropdown cell to execute UpdateStatusCommands natively in the table. */
    private TableCell<Personnel, String> buildEditableStatusCell() {
        ObservableList<Status> options = (model.getSession().getRole() == Role.FIELD_MEDIC)
                ? FXCollections.observableArrayList(Status.FIT, Status.CASUALTY)
                : FXCollections.observableArrayList(Status.values());

        return new TableCell<>() {
            private final ComboBox<Status> combo = new ComboBox<>(options);
            private boolean isUpdating = false;

            {
                styleStatusCombo(combo);
                combo.setButtonCell(combo.getCellFactory().call(null));

                combo.setOnAction(e -> {
                    if (isUpdating) return;
                    Personnel p = getTableRow().getItem();
                    Status newStatus = combo.getValue();
                    if (p == null || newStatus == null || p.getStatus() == newStatus) return;

                    int durationDays = 0;
                    if (newStatus == Status.MC || newStatus == Status.LIGHT_DUTY) {
                        TextInputDialog dialog = new TextInputDialog("3");
                        dialog.setTitle("Medical Status Duration");
                        dialog.setHeaderText("Set duration for " + newStatus.name().replace("_", " "));
                        dialog.setContentText("Enter number of days:");
                        dialog.getDialogPane().setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + OUTLINE_VAR + "; -fx-border-width: 1;");
                        dialog.getEditor().setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-text-fill: " + ON_SURFACE + "; -fx-font-family: 'Consolas', monospace;");
                        dialog.getDialogPane().lookupAll(".label").forEach(n -> n.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-family: 'Consolas', monospace;"));

                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            try {
                                durationDays = Integer.parseInt(result.get().trim());
                                if (durationDays <= 0) throw new NumberFormatException();
                            } catch (NumberFormatException ex) {
                                setFeedback("Invalid duration. Status update cancelled.", true);
                                revertCombo(p.getStatus());
                                return;
                            }
                        } else {
                            revertCombo(p.getStatus());
                            return;
                        }
                    }

                    int idx = model.getFilteredPersonnelList(null).indexOf(p) + 1;
                    try {
                        logic.executeCommand(new UpdateStatusCommand(idx, newStatus, durationDays));
                        setFeedback("Status updated for " + p.getName() + ".", false);
                        refresh();
                    } catch (CommandException ex) {
                        setFeedback("Error: " + ex.getMessage(), true);
                        revertCombo(p.getStatus());
                    }
                });
            }

            private void revertCombo(Status originalStatus) {
                isUpdating = true;
                combo.setValue(originalStatus);
                isUpdating = false;
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    Status current = getTableRow().getItem().getStatus();
                    isUpdating = true;
                    combo.setValue(current);
                    isUpdating = false;
                    setGraphic(combo);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        };
    }

    /** Applies specific styling to the interactive status dropdown. */
    private void styleStatusCombo(ComboBox<Status> combo) {
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + OUTLINE_VAR + ";"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<>() {
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
                    if (!isSelected()) setStyle(base);
                });
            }
        });
    }

    /** Builds the footer containing pagination and system status. */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44);
        footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        boolean isMedicalOfficer = (model.getSession().getRole() == Role.MEDICAL_OFFICER);

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

        if (!isMedicalOfficer) footer.getChildren().addAll(totalLabel, fitLabel, alertLabel);
        footer.getChildren().addAll(spacer, prevBtn, pageLabel, nextBtn);

        if (canAddDelete) {
            if (!isMedicalOfficer) footer.getChildren().add(feedbackLabel);
            footer.getChildren().add(buildRemoveButton());
        } else if (!isMedicalOfficer) {
            footer.getChildren().add(feedbackLabel);
        }

        return footer;
    }

    /** Builds the Remove Personnel button. */
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

    /** Computes the current sublist for pagination display. */
    private void updatePage() {
        int from = currentPage * PAGE_SIZE;
        int size = sortedData.size();
        int to = Math.min(from + PAGE_SIZE, size);
        pageItems.setAll(from < size ? sortedData.subList(from, to) : List.of());
        updatePaginationControls();
    }

    /** Adjusts pagination UI limits based on dataset size. */
    private void updatePaginationControls() {
        int totalPages = Math.max(1, (int) Math.ceil((double) sortedData.size() / PAGE_SIZE));
        if (pageLabel != null) pageLabel.setText("PAGE " + (currentPage + 1) + " / " + totalPages);
        if (prevBtn != null) prevBtn.setDisable(currentPage == 0);
        if (nextBtn != null) nextBtn.setDisable(currentPage >= totalPages - 1);
    }

    /** Styles a pagination button. */
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

    /** Opens the add-personnel modal overlay. */
    private void openAddModal() {
        AddPersonnelModal.show(model, logic, getScene().getWindow(),
                msg -> { setFeedback(msg, false); refresh(); },
                msg -> setFeedback(msg, true));
    }

    /** Opens the remove-personnel modal for the currently selected row. */
    private void openRemoveModal() {
        Personnel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setFeedback("Select a row to remove.", true);
            return;
        }
        int idx = model.getFilteredPersonnelList(null).indexOf(selected) + 1;
        RemovePersonnelModal.show(model, logic, selected, idx, getScene().getWindow(),
                msg -> { setFeedback(msg, false); refresh(); },
                msg -> setFeedback(msg, true));
    }

    /**
     * Reloads the table and footer statistics based on the latest abstract model state.
     */
    public void refresh() {
        Role currentRole = model.getSession().getRole();
        List<Personnel> all = model.getFilteredPersonnelList(null);

        List<Personnel> visible = (currentRole == Role.FIELD_MEDIC)
                ? all.stream()
                .filter(p -> p.getStatus() == Status.FIT || p.getStatus() == Status.CASUALTY)
                .toList()
                : all;

        tableData.setAll(visible);
        updateFooterStats(visible);
    }

    /** Refreshes footer health statistics. */
    private void updateFooterStats(List<Personnel> visible) {
        long fit = visible.stream().filter(p -> p.getStatus() == Status.FIT).count();
        long alert = visible.stream()
                .filter(p -> p.getStatus() == Status.MC
                        || p.getStatus() == Status.LIGHT_DUTY
                        || p.getStatus() == Status.CASUALTY)
                .count();
        if (totalLabel != null) totalLabel.setText("TOTAL: " + visible.size());
        if (fitLabel != null) fitLabel.setText("FIT: " + fit);
        if (alertLabel != null) alertLabel.setText("ATTENTION: " + alert);
    }

    /** Returns the CSS background color based on status urgency. */
    private String rowBackground(Status status) {
        return switch (status) {
            case CASUALTY -> "rgba(147,0,10,0.15)";
            case MC, LIGHT_DUTY -> "rgba(251,188,0,0.04)";
            default -> SURFACE_LOW;
        };
    }

    /** Returns the CSS text color based on status category. */
    private String statusColor(Status status) {
        return switch (status) {
            case FIT -> PRIMARY;
            case LIGHT_DUTY, MC -> WARNING;
            case CASUALTY -> ERROR;
            case PENDING -> OUTLINE;
        };
    }

    /** Helper to generate static stat labels. */
    private Label statLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    /** Helper to generate the empty table placeholder. */
    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("NO PERSONNEL ON RECORD");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    /** Prints feedback messages to the UI. */
    private void setFeedback(String message, boolean isError) {
        if (feedbackLabel == null) return;
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-font-size: 10px; -fx-font-family: 'Consolas', monospace;"
                + " -fx-text-fill: " + (isError ? ERROR : PRIMARY) + ";");
    }
}