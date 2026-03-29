package meditrack.ui.modal;

import java.io.IOException;
import java.util.function.Consumer;

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

import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.commands.personnel.RemovePersonnelCommand;
import meditrack.model.ModelManager;
import meditrack.model.Personnel;
import meditrack.storage.StorageManager;

/** Remove-personnel confirmation modal. */
public class RemovePersonnelModal {

    private static final String SURFACE_LOW  = "#1a1c18";
    private static final String SURFACE      = "#1e201c";
    private static final String SURFACE_HIGH = "#292b26";
    private static final String OUTLINE_VAR  = "#45483c";
    private static final String ON_SURFACE   = "#e3e3dc";
    private static final String SECONDARY    = "#c8c6c6";
    private static final String ERROR        = "#ffb4ab";
    private static final String ERROR_DARK   = "#93000a";

    public static void show(ModelManager model, StorageManager storage, Personnel personnel,
                            int oneBasedIndex, Window owner,
                            Consumer<String> onSuccess, Consumer<String> onError) {
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

        Label titleLbl = new Label("REMOVE PERSONNEL RECORD  //  #" + String.format("%03d", oneBasedIndex));
        titleLbl.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(iconBox, titleLbl, spacer, EditSupplyModal.windowCloseBtn(stage));

        final double[] drag = {0, 0};
        titleBar.setOnMousePressed(e -> { drag[0] = stage.getX() - e.getScreenX(); drag[1] = stage.getY() - e.getScreenY(); });
        titleBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() + drag[0]); stage.setY(e.getScreenY() + drag[1]); });

        // Body
        VBox body = new VBox(20);
        body.setPadding(new Insets(32, 36, 28, 36));
        body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

        VBox recordCard = new VBox(8);
        recordCard.setPadding(new Insets(16));
        recordCard.setStyle("-fx-background-color: " + SURFACE + ";"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 3;");

        Label recordIndex = new Label(String.format("#%03d", oneBasedIndex));
        recordIndex.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        Label recordName = new Label(personnel.getName().toUpperCase());
        recordName.setStyle("-fx-text-fill: " + ON_SURFACE + "; -fx-font-size: 16px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        Label statusLbl = new Label("STATUS: " + personnel.getStatus().toString().replace("_", " "));
        statusLbl.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 10px;"
                + " -fx-font-family: 'Consolas', monospace;");
        recordCard.getChildren().addAll(recordIndex, recordName, statusLbl);

        HBox warnBar = new HBox(10);
        warnBar.setAlignment(Pos.CENTER_LEFT);
        warnBar.setPadding(new Insets(12, 16, 12, 14));
        warnBar.setStyle("-fx-background-color: rgba(147,0,10,0.12);"
                + " -fx-border-color: " + ERROR + "; -fx-border-width: 0 0 0 2;");
        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 13px;");
        Label warnText = new Label("This action is irreversible. The personnel record will be permanently removed from the roster.");
        warnText.setWrapText(true);
        warnText.setStyle("-fx-text-fill: rgba(255,180,171,0.75); -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        warnBar.getChildren().addAll(warningIcon, warnText);

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

        Button deleteBtn = new Button("CONFIRM REMOVE  →");
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
                new RemovePersonnelCommand(oneBasedIndex).execute(model);
                storage.saveMediTrackData(model.getMediTrack());
                stage.close();
                onSuccess.accept("Removed: " + personnel.getName());
            } catch (CommandException | IOException ex) {
                errorLabel.setText("! " + ex.getMessage());
            }
        });

        footer.getChildren().addAll(EditSupplyModal.cancelButton(stage), deleteBtn);

        // Assemble
        VBox root = new VBox(0, titleBar, body, footer);
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                + " -fx-border-width: 1;");

        Scene scene = new Scene(root, 480, 360);
        stage.setScene(scene);
        stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
        stage.showAndWait();
    }
}
