package meditrack.ui.modal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import meditrack.logic.RosterAutoGenerator;
import meditrack.model.DutyType;

/** Auto-generate duty roster modal matching the app design system. */
public class AutoGenerateModal {

    private static final String SURFACE_LOW  = "#1a1c18";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String PRIMARY      = "#b6d088";
    private static final String PRIMARY_CONT = "#556b2f";
    private static final String ON_PRIMARY   = "#233600";
    private static final String OUTLINE      = "#8f9284";
    private static final String ON_SURFACE   = "#e3e3dc";
    private static final String SECONDARY    = "#c8c6c6";
    private static final String ERROR        = "#ffb4ab";

    /** Result returned to the caller on confirmation. */
    public record GenerateConfig(List<DutyType> selectedTypes, Map<DutyType, Integer> durations) { }

    /**
     * Shows the auto-generate modal.
     *
     * @param selectedDate   date to generate for
     * @param fitCount       number of FIT personnel available
     * @param owner          parent window
     * @param onSuccess      callback with the selected config
     */
    public static void show(LocalDate selectedDate, int fitCount,
                            Window owner, Consumer<GenerateConfig> onSuccess) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)");
        DutyType[] allTypes = DutyType.values();

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        // Title bar
        HBox titleBar = buildTitleBar(stage);

        // Body
        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 24, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        // Info badges
        HBox badges = new HBox(16);
        badges.getChildren().addAll(
                infoBadge("FIT PERSONNEL", String.valueOf(fitCount), PRIMARY),
                infoBadge("TARGET DATE", selectedDate.format(dateFmt).toUpperCase(), ON_SURFACE));
        body.getChildren().add(badges);

        // Duty type checkboxes
        Label typeHdr = sectionHeader("SELECT DUTY TYPES");
        VBox typeSection = new VBox(10, typeHdr);
        List<CheckBox> typeBoxes = new ArrayList<>();
        for (DutyType type : allTypes) {
            CheckBox cb = new CheckBox(type.toString());
            cb.setSelected(type == DutyType.GUARD_DUTY || type == DutyType.MEDICAL_COVER);
            cb.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 11px;"
                    + " -fx-font-family: 'Consolas', monospace;");
            typeBoxes.add(cb);
            typeSection.getChildren().add(cb);
        }
        body.getChildren().add(typeSection);

        // Separator
        Region sep1 = new Region();
        sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color: rgba(69,72,60,0.3);");
        body.getChildren().add(sep1);

        // Duration fields
        Label durHdr = sectionHeader("SLOT DURATION (MINUTES)");
        VBox durSection = new VBox(10, durHdr);
        Map<DutyType, TextField> durationFields = new LinkedHashMap<>();
        for (DutyType type : allTypes) {
            TextField tf = styledSmallField(
                    String.valueOf(RosterAutoGenerator.DEFAULT_DURATIONS.getOrDefault(type, 120)));
            durationFields.put(type, tf);
            Label lbl = new Label(type.toString());
            lbl.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                    + " -fx-font-family: 'Consolas', monospace;");
            lbl.setMinWidth(140);
            HBox row = new HBox(12, lbl, tf);
            row.setAlignment(Pos.CENTER_LEFT);
            durSection.getChildren().add(row);
        }
        body.getChildren().add(durSection);

        // Separator
        Region sep2 = new Region();
        sep2.setPrefHeight(1);
        sep2.setStyle("-fx-background-color: rgba(69,72,60,0.3);");
        body.getChildren().add(sep2);

        // Coverage note
        HBox coverageBar = new HBox(10);
        coverageBar.setAlignment(Pos.CENTER_LEFT);
        coverageBar.setPadding(new Insets(10, 14, 10, 14));
        coverageBar.setStyle("-fx-background-color: rgba(85,107,47,0.08);"
                + " -fx-border-color: " + PRIMARY_CONT + "; -fx-border-width: 0 0 0 2;");
        Label coverageLbl = new Label(
                "Guard Duty & Patrol: 00:00\u201300:00 (24h window)\n"
                + "All other types:     08:00\u201320:00 (12h window)");
        coverageLbl.setStyle("-fx-text-fill: rgba(227,227,220,0.55); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        coverageBar.getChildren().add(coverageLbl);
        body.getChildren().addAll(coverageBar, errorLabel);

        javafx.scene.control.ScrollPane scrollBody = new javafx.scene.control.ScrollPane(body);
        scrollBody.setFitToWidth(true);
        scrollBody.setStyle("-fx-background: " + SURFACE_LOW + "; -fx-background-color: " + SURFACE_LOW + ";"
                + " -fx-border-color: transparent;");
        VBox.setVgrow(scrollBody, javafx.scene.layout.Priority.ALWAYS);

        // Footer
        HBox footer = buildFooter(stage, "GENERATE ROSTER  \u2192", () -> {
            errorLabel.setText("");
            boolean anySelected = typeBoxes.stream().anyMatch(CheckBox::isSelected);
            if (!anySelected) {
                errorLabel.setText("! Select at least one duty type.");
                return;
            }

            List<DutyType> selected = new ArrayList<>();
            for (int i = 0; i < allTypes.length; i++) {
                if (typeBoxes.get(i).isSelected()) {
                    selected.add(allTypes[i]);
                }
            }

            Map<DutyType, Integer> durations = new LinkedHashMap<>();
            for (DutyType type : allTypes) {
                try {
                    int val = Integer.parseInt(durationFields.get(type).getText().trim());
                    durations.put(type, Math.max(15, val));
                } catch (NumberFormatException e) {
                    durations.put(type, RosterAutoGenerator.DEFAULT_DURATIONS.getOrDefault(type, 120));
                }
            }

            stage.close();
            onSuccess.accept(new GenerateConfig(selected, durations));
        });

        VBox root = new VBox(0, titleBar, scrollBody, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        Scene scene = new Scene(root, 480, 620);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }

    // Helpers

    private static HBox buildTitleBar(Stage stage) {
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

        Label titleLbl = new Label("AUTO-GENERATE DUTY ROSTER");
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

    private static VBox infoBadge(String label, String value, String valueColor) {
        Label hdr = new Label(label);
        hdr.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 13px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        VBox box = new VBox(4, hdr, val);
        box.setPadding(new Insets(8, 14, 8, 14));
        box.setStyle("-fx-background-color: rgba(69,72,60,0.15);");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Label sectionHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private static TextField styledSmallField(String value) {
        TextField field = new TextField(value);
        field.setPrefWidth(70);
        field.setMaxWidth(70);
        String base = "-fx-background-color: #1e201c; -fx-border-color: #45483c;"
                + " -fx-border-width: 0 0 2 0; -fx-border-radius: 0; -fx-background-radius: 0;"
                + " -fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 11px;"
                + " -fx-font-family: 'Consolas', monospace; -fx-padding: 6 8 6 8;";
        String focused = base.replace("#45483c", PRIMARY_CONT);
        field.setStyle(base);
        field.focusedProperty().addListener((obs, was, isFocused) ->
                field.setStyle(isFocused ? focused : base));
        return field;
    }
}
