package meditrack.ui.modal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import meditrack.model.DutySlot;
import meditrack.model.DutyType;

/** Add-duty-slot modal matching the app design system. */
public class AddSlotModal {

    private static final String SURFACE_LOW  = "#1a1c18";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String PRIMARY      = "#b6d088";
    private static final String PRIMARY_CONT = "#556b2f";
    private static final String ON_PRIMARY   = "#233600";
    private static final String OUTLINE      = "#8f9284";
    private static final String ON_SURFACE   = "#e3e3dc";
    private static final String SECONDARY    = "#c8c6c6";
    private static final String ERROR        = "#ffb4ab";

    /**
     * Shows the add-slot modal and calls onSuccess with the new DutySlot if confirmed.
     *
     * @param selectedDate date for the new slot
     * @param fitNames     names of FIT personnel available for assignment
     * @param owner        parent window for centering
     * @param onSuccess    callback receiving the created DutySlot
     */
    public static void show(LocalDate selectedDate, List<String> fitNames,
                            Window owner, Consumer<DutySlot> onSuccess) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)");

        TextField startField = styledField("HH:MM (E.G. 08:00)");
        TextField endField   = styledField("HH:MM (E.G. 14:00)");

        ComboBox<DutyType> typeCombo = buildDutyTypeCombo();
        ComboBox<String> nameCombo = buildNameCombo(fitNames);

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        // Title bar
        HBox titleBar = buildTitleBar(stage, "ADD DUTY SLOT");

        // Form body
        VBox body = new VBox(24);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        Label dateDisplay = new Label("DATE:  " + selectedDate.format(dateFmt).toUpperCase());
        dateDisplay.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        VBox startSection = fieldSection("START TIME", startField);
        VBox endSection   = fieldSection("END TIME", endField);
        HBox timeRow = new HBox(24, startSection, endSection);
        HBox.setHgrow(startSection, Priority.ALWAYS);
        HBox.setHgrow(endSection, Priority.ALWAYS);

        Label typeHdr = fieldHeader("DUTY TYPE");
        VBox typeSection = new VBox(8, typeHdr, typeCombo);

        Label nameHdr = fieldHeader("ASSIGNED PERSONNEL");
        VBox nameSection = new VBox(8, nameHdr, nameCombo);

        HBox assignRow = new HBox(24, typeSection, nameSection);
        HBox.setHgrow(typeSection, Priority.ALWAYS);
        HBox.setHgrow(nameSection, Priority.ALWAYS);

        HBox infoBar = buildInfoBar("Verify time and assignment before confirmation. "
                + "Slot will be added to the duty roster.");

        body.getChildren().addAll(dateDisplay, timeRow, assignRow, infoBar, errorLabel);

        // Footer
        HBox footer = buildFooter(stage, "CONFIRM SLOT  \u2192", () -> {
            errorLabel.setText("");

            LocalTime start;
            try {
                start = LocalTime.parse(startField.getText().trim());
            } catch (DateTimeParseException e) {
                errorLabel.setText("! Invalid start time \u2014 use HH:MM format.");
                return;
            }
            LocalTime end;
            try {
                end = LocalTime.parse(endField.getText().trim());
            } catch (DateTimeParseException e) {
                errorLabel.setText("! Invalid end time \u2014 use HH:MM format.");
                return;
            }
            if (typeCombo.getValue() == null) {
                errorLabel.setText("! Please select a duty type.");
                return;
            }
            if (nameCombo.getValue() == null || nameCombo.getValue().isBlank()) {
                errorLabel.setText("! Please select a personnel member.");
                return;
            }

            DutySlot slot = new DutySlot(selectedDate, start, end,
                    typeCombo.getValue(), nameCombo.getValue());
            stage.close();
            onSuccess.accept(slot);
        });

        VBox root = new VBox(0, titleBar, body, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        Scene scene = new Scene(root, 560, 480);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }

    // Helpers

    private static HBox buildTitleBar(Stage stage, String title) {
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 14));
        titleBar.setPrefHeight(40);
        titleBar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: rgba(69,72,60,0.2);"
                + " -fx-border-width: 0 0 1 0;");

        Region iconBox = new Region();
        iconBox.setMinSize(16, 16);
        iconBox.setMaxSize(16, 16);
        iconBox.setStyle("-fx-background-color: " + PRIMARY + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
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
        return titleBar;
    }

    private static HBox buildFooter(Stage stage, String confirmText, Runnable onConfirm) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 20, 14, 20));
        footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: rgba(69,72,60,0.1);"
                + " -fx-border-width: 1 0 0 0;");

        Button confirmBtn = new Button(confirmText);
        confirmBtn.setPrefHeight(44);
        confirmBtn.setPadding(new Insets(0, 24, 0, 24));
        String confirmBase = "-fx-background-color: linear-gradient(to bottom, " + PRIMARY + ", " + PRIMARY_CONT + ");"
                + " -fx-text-fill: " + ON_PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace; -fx-cursor: hand; -fx-background-radius: 0;";
        String confirmHover = "-fx-background-color: " + PRIMARY + "; -fx-text-fill: " + ON_PRIMARY + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        confirmBtn.setStyle(confirmBase);
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(confirmHover));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(confirmBase));
        confirmBtn.setOnAction(e -> onConfirm.run());

        footer.getChildren().addAll(EditSupplyModal.cancelButton(stage), confirmBtn);
        return footer;
    }

    private static HBox buildInfoBar(String text) {
        HBox infoBar = new HBox(10);
        infoBar.setAlignment(Pos.CENTER_LEFT);
        infoBar.setPadding(new Insets(12, 16, 12, 14));
        infoBar.setStyle("-fx-background-color: rgba(85,107,47,0.08);"
                + " -fx-border-color: " + PRIMARY_CONT + "; -fx-border-width: 0 0 0 2;");
        Label infoLbl = new Label(text);
        infoLbl.setWrapText(true);
        infoLbl.setStyle("-fx-text-fill: rgba(227,227,220,0.55); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        infoBar.getChildren().add(infoLbl);
        return infoBar;
    }

    private static Label fieldHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private static VBox fieldSection(String label, TextField field) {
        return EditSupplyModal.fieldSection(label, field);
    }

    private static TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        String base    = EditSupplyModal.fieldStyle(false, false);
        String focused = EditSupplyModal.fieldStyle(true, false);
        field.setStyle(base);
        field.focusedProperty().addListener((obs, was, isFocused) ->
                field.setStyle(isFocused ? focused : base));
        return field;
    }

    private static ComboBox<DutyType> buildDutyTypeCombo() {
        ComboBox<DutyType> combo = new ComboBox<>(FXCollections.observableArrayList(DutyType.values()));
        combo.setPromptText("SELECT DUTY TYPE...");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: #1e201c; -fx-border-color: #45483c;"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<DutyType>() {
            @Override protected void updateItem(DutyType dt, boolean empty) {
                super.updateItem(dt, empty);
                if (dt == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: #1e201c;");
                    return;
                }
                setText(dt.toString());
                String base = "-fx-background-color: #1e201c; -fx-text-fill: " + ON_SURFACE + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 6 10 6 10;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle(base.replace("#1e201c;", "#292b26;")));
                setOnMouseExited(e -> setStyle(base));
            }
        });
        combo.setButtonCell(new ListCell<DutyType>() {
            @Override protected void updateItem(DutyType dt, boolean empty) {
                super.updateItem(dt, empty);
                if (dt == null || empty) {
                    setText("SELECT DUTY TYPE...");
                    setStyle("-fx-background-color: transparent; -fx-text-fill: " + OUTLINE + ";"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
                    return;
                }
                setText(dt.toString());
                setStyle("-fx-background-color: transparent; -fx-text-fill: " + ON_SURFACE + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
        return combo;
    }

    private static ComboBox<String> buildNameCombo(List<String> names) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(names));
        combo.setPromptText("SELECT PERSONNEL...");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: #1e201c; -fx-border-color: #45483c;"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (name == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: #1e201c;");
                    return;
                }
                setText(name.toUpperCase());
                String base = "-fx-background-color: #1e201c; -fx-text-fill: " + ON_SURFACE + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 6 10 6 10;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle(base.replace("#1e201c;", "#292b26;")));
                setOnMouseExited(e -> setStyle(base));
            }
        });
        combo.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (name == null || empty) {
                    setText("SELECT PERSONNEL...");
                    setStyle("-fx-background-color: transparent; -fx-text-fill: " + OUTLINE + ";"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
                    return;
                }
                setText(name.toUpperCase());
                setStyle("-fx-background-color: transparent; -fx-text-fill: " + ON_SURFACE + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
        return combo;
    }
}
