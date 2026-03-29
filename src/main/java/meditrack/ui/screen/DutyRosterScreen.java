package meditrack.ui.screen;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import meditrack.logic.RosterAutoGenerator;
import meditrack.ui.modal.AddSlotModal;
import meditrack.ui.modal.AutoGenerateModal;
import meditrack.ui.modal.EditSupplyModal;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.model.DutySlot;
import meditrack.model.DutyType;
import meditrack.model.Model;
import meditrack.model.Personnel;
import meditrack.model.Status;
import meditrack.storage.StorageManager;

/**
 * Duty Roster screen — schedule FIT personnel into duty slots with date navigation.
 */
public class DutyRosterScreen extends VBox {

    // Design tokens
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

    private static final DateTimeFormatter DATE_DISPLAY_FMT =
            DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.ENGLISH);

    private final Model model;
    private final StorageManager storage;

    private LocalDate selectedDate = LocalDate.now();
    private RosterAutoGenerator.GenerateResult lastGenerateResult;

    private Label dateLabel;
    private final TableView<DutySlot> table = new TableView<>();
    private Label summaryLabel;

    /**
     * @param model   used to read personnel and manage duty slots
     * @param storage used to persist changes after each mutation
     */
    public DutyRosterScreen(Model model, StorageManager storage) {
        this.model = model;
        this.storage = storage;
        buildUi();
    }

    /** Syncs with the current model state. Call whenever this screen becomes visible. */
    public void refresh() {
        refreshTable();
    }

    // UI construction

    private void buildUi() {
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        VBox tableSection = buildTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        getChildren().addAll(
                buildHeader(),
                buildDateNavBar(),
                tableSection,
                buildSummaryPanel(),
                buildFooter());
    }

    // Header

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        VBox titleArea = new VBox(4);
        Label title = new Label("DUTY ROSTER");
        title.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 20px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");
        Label subtitle = new Label("Schedule FIT personnel into duty slots. 8h break rule enforced.");
        subtitle.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        titleArea.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addSlotBtn = buildHeaderButton("+ ADD SLOT");
        addSlotBtn.setOnAction(e -> showAddSlotDialog());

        Button autoGenBtn = buildHeaderButton("AUTO-GENERATE");
        autoGenBtn.setOnAction(e -> showAutoGenerateDialog());

        header.getChildren().addAll(titleArea, spacer, addSlotBtn, autoGenBtn);
        return header;
    }

    // Date navigation bar

    private HBox buildDateNavBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 24, 10, 24));
        bar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: transparent transparent "
                + OUTLINE_VAR + " transparent; -fx-border-width: 0 0 1 0;");

        Button prevBtn = buildNavBtn("\u2190 PREV");
        prevBtn.setOnAction(e -> navigateDate(-1));

        dateLabel = new Label(selectedDate.format(DATE_DISPLAY_FMT).toUpperCase());
        dateLabel.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 13px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace; -fx-min-width: 210; -fx-alignment: CENTER;");

        Button nextBtn = buildNavBtn("NEXT \u2192");
        nextBtn.setOnAction(e -> navigateDate(1));

        bar.getChildren().addAll(prevBtn, dateLabel, nextBtn);
        return bar;
    }

    private void navigateDate(int delta) {
        selectedDate = selectedDate.plusDays(delta);
        dateLabel.setText(selectedDate.format(DATE_DISPLAY_FMT).toUpperCase());
        lastGenerateResult = null;
        refreshTable();
    }

    // Table section

    @SuppressWarnings("unchecked")
    private VBox buildTableSection() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(50);
        table.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: transparent;"
                + " -fx-table-cell-border-color: rgba(69,72,60,0.2);"
                + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        table.setPlaceholder(buildEmptyPlaceholder());

        styleTableHeaders();
        buildRowFactory();

        table.getColumns().addAll(
                buildIndexColumn(),
                buildTimeColumn(),
                buildTypeColumn(),
                buildPersonnelColumn(),
                buildActionColumn());

        VBox section = new VBox(0);
        VBox.setVgrow(section, Priority.ALWAYS);
        section.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().add(table);
        return section;
    }

    private void styleTableHeaders() {
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
    }

    private void buildRowFactory() {
        table.setRowFactory(tv -> new TableRow<DutySlot>() {
            @Override
            protected void updateItem(DutySlot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    return;
                }
                setStyle("-fx-background-color: " + SURFACE_LOW + ";");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + SURFACE_BRIGHT + ";"));
                setOnMouseExited(e -> setStyle("-fx-background-color: " + SURFACE_LOW + ";"));
            }
        });
    }

    private TableColumn<DutySlot, String> buildIndexColumn() {
        TableColumn<DutySlot, String> col = new TableColumn<>("#");
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setSortable(false);
        col.setCellValueFactory(cd -> {
            int idx = cd.getTableView().getItems().indexOf(cd.getValue());
            return new javafx.beans.property.SimpleStringProperty(String.format("%03d", idx + 1));
        });
        col.setCellFactory(c -> new TableCell<DutySlot, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle(empty || v == null ? "" : "-fx-text-fill: " + OUTLINE + "; -fx-font-weight: bold;"
                        + " -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace;"
                        + " -fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<DutySlot, String> buildTimeColumn() {
        TableColumn<DutySlot, String> col = new TableColumn<>("TIME SLOT");
        col.setMinWidth(130);
        col.setMaxWidth(160);
        col.setSortable(false);
        col.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getTimeSlotDisplay()));
        col.setCellFactory(c -> new TableCell<DutySlot, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle(empty || v == null ? "" : "-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px;"
                        + " -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                        + " -fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<DutySlot, String> buildTypeColumn() {
        TableColumn<DutySlot, String> col = new TableColumn<>("DUTY TYPE");
        col.setMinWidth(140);
        col.setSortable(false);
        col.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getDutyType().toString()));
        col.setCellFactory(c -> new TableCell<DutySlot, String>() {
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
                badge.setText(v.replace("_", " "));
                badge.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                        + " -fx-font-family: 'Consolas', monospace;"
                        + " -fx-border-color: " + WARNING + "; -fx-border-width: 1;"
                        + " -fx-background-color: transparent;");
                setGraphic(badge);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<DutySlot, String> buildPersonnelColumn() {
        TableColumn<DutySlot, String> col = new TableColumn<>("PERSONNEL");
        col.setSortable(false);
        col.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getPersonnelName()));
        col.setCellFactory(c -> new TableCell<DutySlot, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.toUpperCase());
                setStyle(empty || v == null ? "" : "-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 12px;"
                        + " -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                        + " -fx-background-color: transparent;");
            }
        });
        return col;
    }

    private TableColumn<DutySlot, Void> buildActionColumn() {
        TableColumn<DutySlot, Void> col = new TableColumn<>("ACTIONS");
        col.setMinWidth(160);
        col.setMaxWidth(180);
        col.setSortable(false);
        col.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = buildRowBtn("EDIT", PRIMARY_CONT, "white");
            private final Button removeBtn = buildRowBtn("REMOVE", "rgba(147,0,10,0.6)", ERROR);
            private final HBox box = new HBox(8, editBtn, removeBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                editBtn.setOnAction(e -> {
                    DutySlot slot = getTableRow().getItem();
                    if (slot != null) {
                        handleEdit(slot);
                    }
                });
                removeBtn.setOnAction(e -> {
                    DutySlot slot = getTableRow().getItem();
                    if (slot != null) {
                        handleRemove(slot);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setStyle("-fx-background-color: transparent;");
            }
        });
        return col;
    }

    // Summary panel

    private HBox buildSummaryPanel() {
        HBox panel = new HBox(8);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(8, 24, 8, 24));
        panel.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + OUTLINE_VAR
                + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Label header = new Label("DAILY SUMMARY:");
        header.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        summaryLabel = new Label("\u2014");
        summaryLabel.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        summaryLabel.setWrapText(true);

        panel.getChildren().addAll(header, summaryLabel);
        return panel;
    }

    // Footer

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 24));
        footer.setPrefHeight(44);
        footer.setMinHeight(44);
        footer.setStyle("-fx-background-color: " + SURFACE_HIGHEST + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String clearBase = "-fx-background-color: " + SURFACE_HIGH + "; -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;";
        String clearHover = "-fx-background-color: rgba(147,0,10,0.4); -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: rgba(255,180,171,0.3); -fx-border-width: 1;";
        Button clearDayBtn = new Button("CLEAR DAY");
        clearDayBtn.setPrefHeight(30);
        clearDayBtn.setPadding(new Insets(0, 14, 0, 14));
        clearDayBtn.setStyle(clearBase);
        clearDayBtn.setOnMouseEntered(e -> clearDayBtn.setStyle(clearHover));
        clearDayBtn.setOnMouseExited(e -> clearDayBtn.setStyle(clearBase));
        clearDayBtn.setOnAction(e -> handleClearDay());

        footer.getChildren().addAll(spacer, clearDayBtn);
        return footer;
    }

    // Event handlers

    private void showAddSlotDialog() {
        List<String> fitNames = model.getFilteredPersonnelList(Status.FIT)
                .stream().map(Personnel::getName).toList();
        if (fitNames.isEmpty()) {
            showAlert("No FIT personnel available to assign.");
            return;
        }

        AddSlotModal.show(selectedDate, fitNames, getScene().getWindow(), slot -> {
            model.addDutySlot(slot);
            lastGenerateResult = null;
            persist();
            refreshTable();
        });
    }

    private void handleRemove(DutySlot slot) {
        int globalIdx = globalIndexOf(slot);
        if (globalIdx < 0) {
            return;
        }
        try {
            model.removeDutySlot(globalIdx);
            lastGenerateResult = null;
            persist();
            refreshTable();
        } catch (CommandException ex) {
            showAlert(ex.getMessage());
        }
    }

    private void handleEdit(DutySlot existing) {
        int globalIdx = globalIndexOf(existing);
        if (globalIdx < 0) {
            return;
        }

        List<String> fitNames = model.getFilteredPersonnelList(Status.FIT)
                .stream().map(Personnel::getName).toList();

        ComboBox<DutyType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(DutyType.values()));
        typeCombo.setValue(existing.getDutyType());

        ComboBox<String> nameCombo = new ComboBox<>(FXCollections.observableArrayList(fitNames));
        nameCombo.setValue(existing.getPersonnelName());

        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px;");
        errLabel.setVisible(false);
        errLabel.setManaged(false);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 24, 8, 24));
        grid.add(new Label("Date:"), 0, 0);
        grid.add(new Label(existing.getDate().format(DATE_DISPLAY_FMT)), 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(new Label(existing.getTimeSlotDisplay()), 1, 1);
        grid.add(new Label("Duty type:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Personnel:"), 0, 3);
        grid.add(nameCombo, 1, 3);
        grid.add(errLabel, 0, 4, 2, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(80);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setMinWidth(180);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Duty Slot");
        dialog.setHeaderText("Modify duty type or assigned personnel.");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (typeCombo.getValue() == null) {
                showDialogError(errLabel, "Please select a duty type.");
                ev.consume();
            } else if (nameCombo.getValue() == null || nameCombo.getValue().isBlank()) {
                showDialogError(errLabel, "Please select a personnel member.");
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) {
                return;
            }
            DutySlot updated = new DutySlot(
                    existing.getDate(),
                    existing.getStartTime(),
                    existing.getEndTime(),
                    typeCombo.getValue(),
                    nameCombo.getValue());
            try {
                model.replaceDutySlot(globalIdx, updated);
                lastGenerateResult = null;
                persist();
                refreshTable();
            } catch (CommandException ex) {
                showAlert(ex.getMessage());
            }
        });
    }

    private void handleClearDay() {
        long count = model.getDutySlots().stream()
                .filter(s -> s.getDate().equals(selectedDate)).count();
        if (count == 0) {
            return;
        }

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getScene().getWindow());

        // Title bar
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 14));
        titleBar.setPrefHeight(40);
        titleBar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: rgba(69,72,60,0.2);"
                + " -fx-border-width: 0 0 1 0;");

        Region iconBox = new Region();
        iconBox.setMinSize(16, 16);
        iconBox.setMaxSize(16, 16);
        iconBox.setStyle("-fx-background-color: " + ERROR + ";");

        Label titleLbl = new Label("CLEAR DAY");
        titleLbl.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(iconBox, titleLbl, spacer, EditSupplyModal.windowCloseBtn(stage));

        final double[] drag = {0, 0};
        titleBar.setOnMousePressed(e -> {
            drag[0] = stage.getX() - e.getScreenX();
            drag[1] = stage.getY() - e.getScreenY();
        });
        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() + drag[0]);
            stage.setY(e.getScreenY() + drag[1]);
        });

        // Body
        VBox body = new VBox(20);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        Label dateLbl = new Label(selectedDate.format(DATE_DISPLAY_FMT));
        dateLbl.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 16px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Label countLbl = new Label(count + " SLOT" + (count == 1 ? "" : "S") + " SCHEDULED");
        countLbl.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        VBox infoCard = new VBox(8, dateLbl, countLbl);
        infoCard.setPadding(new Insets(16));
        infoCard.setStyle("-fx-background-color: " + SURFACE + ";"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 3;");

        HBox warnBar = new HBox(10);
        warnBar.setAlignment(Pos.CENTER_LEFT);
        warnBar.setPadding(new Insets(12, 16, 12, 14));
        warnBar.setStyle("-fx-background-color: rgba(147,0,10,0.12);"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 2;");
        Label warnText = new Label("All duty slots for this date will be permanently removed.");
        warnText.setWrapText(true);
        warnText.setStyle("-fx-text-fill: rgba(255,180,171,0.75); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        warnBar.getChildren().add(warnText);

        body.getChildren().addAll(infoCard, warnBar);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 20, 14, 20));
        footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: rgba(69,72,60,0.1);"
                + " -fx-border-width: 1 0 0 0;");

        String delBase = "-fx-background-color: #93000a; -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        String delHover = "-fx-background-color: " + ERROR + "; -fx-text-fill: #1a0000;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        Button confirmBtn = new Button("CONFIRM CLEAR  \u2192");
        confirmBtn.setPrefHeight(44);
        confirmBtn.setPadding(new Insets(0, 24, 0, 24));
        confirmBtn.setStyle(delBase);
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(delHover));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(delBase));
        confirmBtn.setOnAction(e -> {
            model.clearDutySlotsForDate(selectedDate);
            lastGenerateResult = null;
            persist();
            refreshTable();
            stage.close();
        });

        footer.getChildren().addAll(EditSupplyModal.cancelButton(stage), confirmBtn);

        VBox root = new VBox(0, titleBar, body, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        Scene scene = new Scene(root, 440, 300);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, getScene().getWindow()));
        stage.showAndWait();
    }

    // Auto-generate dialog

    private void showAutoGenerateDialog() {
        List<Personnel> fitPersonnel = model.getFilteredPersonnelList(Status.FIT);
        if (fitPersonnel.isEmpty()) {
            showAlert("No FIT personnel available for auto-generation.");
            return;
        }

        AutoGenerateModal.show(selectedDate, fitPersonnel.size(),
                getScene().getWindow(), config -> {
            Map<String, Integer> existingCounts = new HashMap<>();
            for (DutySlot slot : model.getDutySlots()) {
                existingCounts.merge(slot.getPersonnelName(), 1, Integer::sum);
            }

            List<String> names = fitPersonnel.stream().map(Personnel::getName).toList();
            RosterAutoGenerator.GenerateResult result = RosterAutoGenerator.generate(
                    names, config.selectedTypes(), selectedDate,
                    config.durations(), existingCounts);

            model.clearDutySlotsForDate(selectedDate);
            for (DutySlot slot : result.slots()) {
                model.addDutySlot(slot);
            }
            lastGenerateResult = result;
            persist();
            refreshTable();
        });
    }

    // Data refresh helpers

    private void refreshTable() {
        List<DutySlot> filtered = model.getDutySlots().stream()
                .filter(s -> s.getDate().equals(selectedDate))
                .sorted(Comparator.comparing(DutySlot::getStartTime))
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtered));

        refreshSummary(filtered);
    }

    private void refreshSummary(List<DutySlot> filtered) {
        if (summaryLabel == null) {
            return;
        }
        if (filtered.isEmpty()) {
            summaryLabel.setText("\u2014");
            return;
        }
        Map<String, Integer> minutesPerPerson = new LinkedHashMap<>();
        for (DutySlot slot : filtered) {
            minutesPerPerson.merge(slot.getPersonnelName(), computeSlotDurationMinutes(slot), Integer::sum);
        }
        String summary = minutesPerPerson.entrySet().stream()
                .map(e -> e.getKey().toUpperCase() + " " + formatMinutes(e.getValue()))
                .collect(Collectors.joining("   "));
        summaryLabel.setText(summary);
    }

    // Utility helpers

    /**
     * Returns the zero-based index of the given slot in the full (unfiltered) model duty slot list.
     * Returns -1 if not found.
     */
    private int globalIndexOf(DutySlot slot) {
        List<DutySlot> all = model.getDutySlots();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).equals(slot)) {
                return i;
            }
        }
        return -1;
    }

    /** Computes the duration of a duty slot in minutes, accounting for overnight slots. */
    private int computeSlotDurationMinutes(DutySlot slot) {
        int startMin = slot.getStartTime().getHour() * 60 + slot.getStartTime().getMinute();
        int endMin = slot.getEndTime().getHour() * 60 + slot.getEndTime().getMinute();
        return slot.crossesMidnight()
                ? (RosterAutoGenerator.DAY_MINUTES - startMin) + endMin
                : endMin - startMin;
    }

    private String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return mins == 0 ? hours + "h" : hours + "h" + mins + "m";
    }

    private void persist() {
        try {
            storage.saveMediTrackData(model.getMediTrack());
        } catch (IOException e) {
            showAlert("Warning: changes could not be saved — " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showDialogError(Label errLabel, String message) {
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.setManaged(true);
    }

    // Button builders

    private Button buildHeaderButton(String text) {
        String normal = "-fx-background-color: " + PRIMARY_CONT + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        String hover = "-fx-background-color: " + PRIMARY + "; -fx-text-fill: " + ON_PRIMARY + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        Button btn = new Button(text);
        btn.setPrefHeight(42);
        btn.setPadding(new Insets(0, 20, 0, 20));
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private Button buildNavBtn(String text) {
        String normal = "-fx-background-color: " + SURFACE_BRIGHT + "; -fx-text-fill: " + ON_SURFACE + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: " + OUTLINE_VAR + "; -fx-border-width: 1;";
        String hover = "-fx-background-color: " + PRIMARY_CONT + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;"
                + " -fx-border-color: " + OUTLINE_VAR + "; -fx-border-width: 1;";
        Button btn = new Button(text);
        btn.setPrefHeight(32);
        btn.setPadding(new Insets(0, 16, 0, 16));
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private Button buildRowBtn(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setPrefHeight(26);
        btn.setPadding(new Insets(0, 10, 0, 10));
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + ";"
                + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;");
        return btn;
    }

    private Label buildEmptyPlaceholder() {
        Label lbl = new Label("NO DUTY SLOTS FOR THIS DATE");
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }
}
