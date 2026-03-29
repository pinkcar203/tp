package meditrack.ui.modal;

import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import meditrack.commons.core.Index;
import meditrack.logic.Logic;
import meditrack.logic.commands.DeleteSupplyCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.parser.CommandType;
import meditrack.logic.parser.Parser;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.ModelManager;
import meditrack.model.Supply;

/** Delete-supply confirmation modal. */
public class DeleteSupplyModal {

    private static final String SURFACE_LOW  = "#1a1c18";
    private static final String SURFACE      = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String OUTLINE      = "#8f9284";
    private static final String OUTLINE_VAR  = "#45483c";
    private static final String ON_SURFACE   = "#e3e3dc";
    private static final String SECONDARY    = "#c8c6c6";
    private static final String ERROR        = "#ffb4ab";
    private static final String ERROR_DARK   = "#93000a";
    private static final String WARNING      = "#fbbc00";

    public static void show(ModelManager model, Logic logic, Supply supply,
                            int oneBasedIndex, Window owner) {

        // Validate before showing the dialog
        Parser parser = new Parser(model);
        try {
            parser.validate(CommandType.DELETE_SUPPLY,
                    Map.of("index", String.valueOf(oneBasedIndex)));
        } catch (ParseException e) {
            showError(owner, e.getMessage());
            return;
        }

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        // Title bar
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 14));
        titleBar.setPrefHeight(40);
        titleBar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: "
                + "rgba(69,72,60,0.2); -fx-border-width: 0 0 1 0;");

        Region iconBox = new Region();
        iconBox.setMinSize(16, 16); iconBox.setMaxSize(16, 16);
        iconBox.setStyle("-fx-background-color: " + ERROR + ";");

        Label titleLbl = new Label("DELETE SUPPLY RECORD  //  #" + String.format("%03d", oneBasedIndex));
        titleLbl.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = EditSupplyModal.windowCloseBtn(stage);
        titleBar.getChildren().addAll(iconBox, titleLbl, spacer, closeBtn);

        final double[] drag = {0, 0};
        titleBar.setOnMousePressed(e -> { drag[0] = stage.getX() - e.getScreenX(); drag[1] = stage.getY() - e.getScreenY(); });
        titleBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + drag[0]); stage.setY(e.getScreenY() + drag[1]); });

        // Body
        VBox body = new VBox(20);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        // Target record display
        VBox recordCard = new VBox(8);
        recordCard.setPadding(new Insets(16));
        recordCard.setStyle("-fx-background-color: " + SURFACE + ";"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 3;");

        Label recordIndex = new Label(String.format("#%03d", oneBasedIndex));
        recordIndex.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        Label recordName = new Label(supply.getName().toUpperCase());
        recordName.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 16px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        HBox metaRow = new HBox(20);
        Label qtyLbl = new Label("QTY: " + supply.getQuantity());
        Label expLbl = new Label("EXP: " + supply.getExpiryDate().toString().replace("-", "."));
        qtyLbl.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        expLbl.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        metaRow.getChildren().addAll(qtyLbl, expLbl);

        recordCard.getChildren().addAll(recordIndex, recordName, metaRow);

        // Warning bar
        HBox warnBar = new HBox(10);
        warnBar.setAlignment(Pos.CENTER_LEFT);
        warnBar.setPadding(new Insets(12, 16, 12, 14));
        warnBar.setStyle("-fx-background-color: rgba(147,0,10,0.12);"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 2;");

        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 13px;");
        Label warnText = new Label("This action is irreversible. The record will be permanently removed from the system.");
        warnText.setWrapText(true);
        warnText.setStyle("-fx-text-fill: rgba(255,180,171,0.75); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        warnBar.getChildren().addAll(warningIcon, warnText);

        // Error feedback label
        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");

        body.getChildren().addAll(recordCard, warnBar, errorLabel);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 20, 14, 20));
        footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: "
                + "rgba(69,72,60,0.1); -fx-border-width: 1 0 0 0;");

        Button cancelBtn = EditSupplyModal.cancelButton(stage);

        Button deleteBtn = new Button("CONFIRM DELETE  →");
        deleteBtn.setPrefHeight(44);
        deleteBtn.setPadding(new Insets(0, 24, 0, 24));
        String delBase = "-fx-background-color: " + ERROR_DARK + "; -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        String delHover = "-fx-background-color: " + ERROR + "; -fx-text-fill: #1a0000;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;";
        deleteBtn.setStyle(delBase);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(delHover));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(delBase));

        deleteBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                logic.executeCommand(new DeleteSupplyCommand(Index.fromOneBased(oneBasedIndex)));
                stage.close();
            } catch (CommandException ex) {
                errorLabel.setText("! " + ex.getMessage());
            }
        });

        footer.getChildren().addAll(cancelBtn, deleteBtn);

        // Assemble
        VBox root = new VBox(0, titleBar, body, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        Scene scene = new Scene(root, 480, 360);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }

    // Inline error stage (replaces Alert)

    private static void showError(Window owner, String message) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 14));
        titleBar.setPrefHeight(40);
        titleBar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: "
                + "rgba(69,72,60,0.2); -fx-border-width: 0 0 1 0;");
        Label errTitle = new Label("OPERATION BLOCKED");
        errTitle.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleBar.getChildren().addAll(errTitle, sp, EditSupplyModal.windowCloseBtn(stage));

        final double[] drag = {0, 0};
        titleBar.setOnMousePressed(e -> { drag[0] = stage.getX() - e.getScreenX(); drag[1] = stage.getY() - e.getScreenY(); });
        titleBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + drag[0]); stage.setY(e.getScreenY() + drag[1]); });

        Label msg = new Label("! " + message);
        msg.setWrapText(true);
        msg.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 12px;"
                + " -fx-font-family: 'Consolas', monospace;");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle("-fx-background-color: rgba(41,43,38,0.5);");
        Button okBtn = new Button("ACKNOWLEDGE");
        okBtn.setPrefHeight(38); okBtn.setPadding(new Insets(0, 20, 0, 20));
        okBtn.setStyle("-fx-background-color: " + ERROR_DARK + "; -fx-text-fill: " + ERROR + ";"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                + " -fx-cursor: hand; -fx-background-radius: 0;");
        okBtn.setOnAction(e -> stage.close());
        footer.getChildren().add(okBtn);

        VBox body = new VBox(0, titleBar,
                new VBox(msg) {{ setPadding(new Insets(24, 36, 24, 36)); setStyle("-fx-background-color: " + SURFACE_LOW + ";"); }},
                footer);
        body.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2); -fx-border-width: 1;");

        stage.setScene(new Scene(body, 400, 180));
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }
}
