package meditrack.ui.modal;

import java.io.IOException;
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

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.AddPersonnelCommand;
import meditrack.model.BloodGroup;
import meditrack.model.ModelManager;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.model.Status;
import meditrack.storage.StorageManager;

/** Add-personnel modal. */
public class AddPersonnelModal {

    private static final String SURFACE_LOW  = "#1a1c18";
    private static final String SURFACE      = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String PRIMARY      = "#b6d088";
    private static final String PRIMARY_CONT = "#556b2f";
    private static final String ON_PRIMARY   = "#233600";
    private static final String OUTLINE      = "#8f9284";
    private static final String OUTLINE_VAR  = "#45483c";
    private static final String ERROR        = "#ffb4ab";

    public static void show(ModelManager model, StorageManager storage, Window owner,
                            Consumer<String> onSuccess, Consumer<String> onError) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        Role currentRole = Session.getInstance().getRole();
        boolean isPc = (currentRole == Role.PLATOON_COMMANDER);
        boolean isMo = (currentRole == Role.MEDICAL_OFFICER);

        // Fields
        TextField nameField = styledField("ENTER FULL NAME...");
        ComboBox<Status> statusCombo = buildStatusCombo(isPc);

        // Medical Officer-only fields
        ComboBox<BloodGroup> bloodGroupCombo = isMo ? buildBloodGroupCombo() : null;
        TextField allergiesField = isMo ? styledField("E.G. PENICILLIN, SULFA...") : null;

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

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
        iconBox.setStyle("-fx-background-color: " + PRIMARY + ";");

        Label titleLbl = new Label("ADD PERSONNEL RECORD");
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

        // Form body
        VBox body = new VBox(24);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        VBox nameSection = EditSupplyModal.fieldSection("FULL NAME", nameField);

        Label statusHdrLbl = new Label("STATUS");
        statusHdrLbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        VBox statusSection = new VBox(8, statusHdrLbl, statusCombo);

        HBox row1 = new HBox(24, nameSection, statusSection);
        HBox.setHgrow(nameSection, Priority.ALWAYS);
        HBox.setHgrow(statusSection, Priority.ALWAYS);
        body.getChildren().add(row1);

        // Medical Officer sees blood group + allergies row
        if (isMo) {
            Label bgHdr = fieldHeader("BLOOD GROUP");
            VBox bgSection = new VBox(8, bgHdr, bloodGroupCombo);

            VBox allergiesSection = EditSupplyModal.fieldSection("ALLERGIES", allergiesField);

            HBox row2 = new HBox(24, bgSection, allergiesSection);
            HBox.setHgrow(bgSection, Priority.ALWAYS);
            HBox.setHgrow(allergiesSection, Priority.ALWAYS);
            body.getChildren().add(row2);
        }

        HBox infoBar = new HBox(10);
        infoBar.setAlignment(Pos.CENTER_LEFT);
        infoBar.setPadding(new Insets(12, 16, 12, 14));
        infoBar.setStyle("-fx-background-color: rgba(85,107,47,0.08);"
                + " -fx-border-color: " + PRIMARY_CONT + "; -fx-border-width: 0 0 0 2;");
        Label infoLbl = new Label("Verify name and status before confirmation. Personnel will be logged to the roster.");
        infoLbl.setWrapText(true);
        infoLbl.setStyle("-fx-text-fill: rgba(227,227,220,0.55); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        infoBar.getChildren().add(infoLbl);

        body.getChildren().addAll(infoBar, errorLabel);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 20, 14, 20));
        footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: rgba(69,72,60,0.1);"
                + " -fx-border-width: 1 0 0 0;");

        Button confirmBtn = new Button("CONFIRM RECORD  →");
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

        confirmBtn.setOnAction(e -> {
            errorLabel.setText("");
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                errorLabel.setText("! Name must not be blank.");
                return;
            }
            Status status = statusCombo.getValue();
            if (status == null) {
                errorLabel.setText("! Please select a status.");
                return;
            }
            BloodGroup bloodGroup = (bloodGroupCombo != null) ? bloodGroupCombo.getValue() : null;
            String allergies = (allergiesField != null) ? allergiesField.getText().trim() : "";
            try {
                new AddPersonnelCommand(name, status, bloodGroup, allergies).execute(model);
                storage.saveMediTrackData(model.getMediTrack());
                stage.close();
                onSuccess.accept("Personnel added: " + name);
            } catch (CommandException | IOException ex) {
                errorLabel.setText("! " + ex.getMessage());
            }
        });

        footer.getChildren().addAll(EditSupplyModal.cancelButton(stage), confirmBtn);

        // Assemble
        VBox root = new VBox(0, titleBar, body, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        // Taller height when Medical Officer to accommodate the extra row
        double height = isMo ? 480 : 380;
        Scene scene = new Scene(root, 560, height);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }

    // Helpers

    private static Label fieldHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
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

    private static ComboBox<BloodGroup> buildBloodGroupCombo() {
        ComboBox<BloodGroup> combo = new ComboBox<>(FXCollections.observableArrayList(BloodGroup.values()));
        combo.setValue(BloodGroup.UNKNOWN);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: #1e201c; -fx-border-color: #45483c;"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<BloodGroup>() {
            @Override protected void updateItem(BloodGroup bg, boolean empty) {
                super.updateItem(bg, empty);
                if (bg == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: #1e201c;");
                    return;
                }
                setText(bg.display());
                String base = "-fx-background-color: #1e201c; -fx-text-fill: #e3e3dc;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 6 10 6 10;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle(base.replace("#1e201c;", "#292b26;")));
                setOnMouseExited(e -> setStyle(base));
            }
        });
        combo.setButtonCell(new ListCell<BloodGroup>() {
            @Override protected void updateItem(BloodGroup bg, boolean empty) {
                super.updateItem(bg, empty);
                if (bg == null || empty) {
                    setText("SELECT BLOOD GROUP...");
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #8f9284;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
                    return;
                }
                setText(bg.display());
                setStyle("-fx-background-color: transparent; -fx-text-fill: #e3e3dc;"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
        return combo;
    }

    private static ComboBox<Status> buildStatusCombo(boolean isPc) {
        ComboBox<Status> combo = isPc
                ? new ComboBox<>(FXCollections.observableArrayList(Status.PENDING))
                : new ComboBox<>(FXCollections.observableArrayList(Status.values()));
        combo.setValue(Status.PENDING);
        if (isPc) combo.setDisable(true);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-background-color: #1e201c; -fx-border-color: #45483c;"
                + " -fx-border-width: 1; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        combo.setCellFactory(lv -> new ListCell<Status>() {
            @Override protected void updateItem(Status s, boolean empty) {
                super.updateItem(s, empty);
                if (s == null || empty) { setText(null); setStyle("-fx-background-color: #1e201c;"); return; }
                setText(s.toString().replace("_", " "));
                String color = statusColor(s);
                String base = "-fx-background-color: #1e201c; -fx-text-fill: " + color + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-padding: 6 10 6 10;";
                setStyle(base);
                setOnMouseEntered(e -> setStyle(base.replace("#1e201c;", "#292b26;")));
                setOnMouseExited(e -> setStyle(base));
            }
        });
        combo.setButtonCell(new ListCell<Status>() {
            @Override protected void updateItem(Status s, boolean empty) {
                super.updateItem(s, empty);
                if (s == null || empty) {
                    setText(isPc ? "PENDING" : "SELECT STATUS...");
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #8f9284;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
                    return;
                }
                setText(s.toString().replace("_", " "));
                setStyle("-fx-background-color: transparent; -fx-text-fill: " + statusColor(s) + ";"
                        + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
        return combo;
    }

    private static String statusColor(Status s) {
        return switch (s) {
            case FIT        -> "#b6d088";
            case LIGHT_DUTY -> "#fbbc00";
            case MC         -> "#fbbc00";
            case CASUALTY   -> "#ffb4ab";
            case PENDING    -> "#8f9284";
        };
    }
}
