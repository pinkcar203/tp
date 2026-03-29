package meditrack.ui.modal;

import java.time.LocalDate;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

import meditrack.commons.core.Index;
import meditrack.logic.Logic;
import meditrack.logic.commands.EditSupplyCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.parser.CommandType;
import meditrack.logic.parser.Parser;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.ModelManager;
import meditrack.model.Supply;

/** Edit-supply modal. */
public class EditSupplyModal {

        private static final String SURFACE_LOW = "#1a1c18";
        private static final String SURFACE = "#1e201c";
        private static final String SURFACE_HIGH = "#292b26";
        private static final String PRIMARY = "#b6d088";
        private static final String PRIMARY_CONT = "#556b2f";
        private static final String ON_PRIMARY = "#233600";
        private static final String OUTLINE = "#8f9284";
        private static final String OUTLINE_VAR = "#45483c";
        private static final String ON_SURFACE = "#e3e3dc";
        private static final String SECONDARY = "#c8c6c6";
        private static final String WARNING = "#fbbc00";
        private static final String ERROR = "#ffb4ab";

        public static void show(ModelManager model, Logic logic, Supply current,
                        int oneBasedIndex, Window owner) {
                Stage stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(owner);

                // Fields (pre-filled)
                TextField nameField = styledField(current.getName(), false);
                TextField qtyField = styledField(String.valueOf(current.getQuantity()), true);
                TextField expiryField = styledField(current.getExpiryDate().toString(), false);
                Label errorLabel = new Label();
                errorLabel.setWrapText(true);
                errorLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;"
                                + " -fx-font-family: 'Consolas', monospace;");

                // Title bar
                HBox titleBar = new HBox(10);
                titleBar.setAlignment(Pos.CENTER_LEFT);
                titleBar.setPadding(new Insets(0, 8, 0, 14));
                titleBar.setPrefHeight(40);
                titleBar.setStyle("-fx-background-color: " + SURFACE_HIGH + "; -fx-border-color: "
                                + "rgba(69,72,60,0.2); -fx-border-width: 0 0 1 0;");

                Region iconBox = new Region();
                iconBox.setMinSize(16, 16);
                iconBox.setMaxSize(16, 16);
                iconBox.setStyle("-fx-background-color: " + WARNING + ";");

                Label titleLbl = new Label("EDIT SUPPLY RECORD  //  #" + String.format("%03d", oneBasedIndex));
                titleLbl.setStyle("-fx-text-fill: " + WARNING + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button closeBtn = windowCloseBtn(stage);
                titleBar.getChildren().addAll(iconBox, titleLbl, spacer, closeBtn);

                final double[] drag = { 0, 0 };
                titleBar.setOnMousePressed(e -> {
                        drag[0] = stage.getX() - e.getScreenX();
                        drag[1] = stage.getY() - e.getScreenY();
                });
                titleBar.setOnMouseDragged(e -> {
                        stage.setX(e.getScreenX() + drag[0]);
                        stage.setY(e.getScreenY() + drag[1]);
                });

                // Form body
                VBox body = new VBox(28);
                body.setPadding(new Insets(32, 36, 28, 36));
                body.setStyle("-fx-background-color: " + SURFACE_LOW + ";");

                VBox nameSection = fieldSection("NOMENCLATURE", nameField);

                VBox qtySection = fieldSection("QUANTITY", qtyField);
                VBox expirySection = fieldSection("EXPIRY DATE", expiryField);
                Label expiryHint = new Label("FORMAT: YYYY-MM-DD");
                expiryHint.setStyle("-fx-text-fill: rgba(143,146,132,0.5); -fx-font-size: 9px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;");
                expirySection.getChildren().add(expiryHint);

                HBox twoCol = new HBox(24, qtySection, expirySection);
                HBox.setHgrow(qtySection, Priority.ALWAYS);
                HBox.setHgrow(expirySection, Priority.ALWAYS);

                // Info bar — amber tint to signal modification
                HBox infoBar = new HBox(10);
                infoBar.setAlignment(Pos.CENTER_LEFT);
                infoBar.setPadding(new Insets(12, 16, 12, 14));
                infoBar.setStyle("-fx-background-color: rgba(251,188,0,0.06);"
                                + " -fx-border-color: " + WARNING + "; -fx-border-width: 0 0 0 2;");
                Label infoLbl = new Label("Modifying record #" + String.format("%03d", oneBasedIndex)
                                + " — changes will overwrite existing entry.");
                infoLbl.setWrapText(true);
                infoLbl.setStyle("-fx-text-fill: rgba(227,227,220,0.55); -fx-font-size: 9px;"
                                + " -fx-font-family: 'Consolas', monospace;");
                infoBar.getChildren().add(infoLbl);

                body.getChildren().addAll(nameSection, twoCol, infoBar, errorLabel);

                // Footer
                HBox footer = new HBox(12);
                footer.setAlignment(Pos.CENTER_RIGHT);
                footer.setPadding(new Insets(14, 20, 14, 20));
                footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: "
                                + "rgba(69,72,60,0.1); -fx-border-width: 1 0 0 0;");

                Button cancelBtn = cancelButton(stage);

                Button confirmBtn = new Button("APPLY CHANGES  →");
                confirmBtn.setPrefHeight(44);
                confirmBtn.setPadding(new Insets(0, 24, 0, 24));
                String confirmBase = "-fx-background-color: linear-gradient(to bottom, " + WARNING + ", #b07800);"
                                + " -fx-text-fill: #1a1000; -fx-font-size: 11px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace; -fx-cursor: hand; -fx-background-radius: 0;";
                String confirmHover = "-fx-background-color: " + WARNING + "; -fx-text-fill: #1a1000;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                                + " -fx-cursor: hand; -fx-background-radius: 0;";
                confirmBtn.setStyle(confirmBase);
                confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(confirmHover));
                confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(confirmBase));

                confirmBtn.setOnAction(e -> {
                        errorLabel.setText("");
                        String name = nameField.getText().trim();
                        String qty = qtyField.getText().trim();
                        String expiry = expiryField.getText().trim();

                        Parser parser = new Parser(model);
                        try {
                                parser.validate(CommandType.EDIT_SUPPLY, Map.of(
                                                "name", name, "qty", qty, "expiry", expiry,
                                                "index", String.valueOf(oneBasedIndex)));
                        } catch (ParseException ex) {
                                errorLabel.setText("! " + ex.getMessage());
                                return;
                        }
                        try {
                                Supply edited = new Supply(name, Integer.parseInt(qty), LocalDate.parse(expiry));
                                logic.executeCommand(new EditSupplyCommand(Index.fromOneBased(oneBasedIndex), edited));
                                stage.close();
                        } catch (CommandException ex) {
                                errorLabel.setText("! " + ex.getMessage());
                        }
                });

                footer.getChildren().addAll(cancelBtn, confirmBtn);

                // Assemble
                VBox root = new VBox(0, titleBar, body, footer);
                root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                                + " -fx-border-width: 1;");

                Scene scene = new Scene(root, 520, 440);
                stage.setScene(scene);
                stage.setOnShown(ev -> centre(stage, owner));
                stage.showAndWait();
        }

        // Shared helpers

        static VBox fieldSection(String labelText, TextField field) {
                Label lbl = new Label(labelText);
                lbl.setStyle("-fx-text-fill: " + OUTLINE + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;");
                return new VBox(8, lbl, field);
        }

        static TextField styledField(String value, boolean large) {
                TextField field = new TextField(value);
                String base = fieldStyle(false, large);
                String focused = fieldStyle(true, large);
                field.setStyle(base);
                field.focusedProperty()
                                .addListener((obs, was, isFocused) -> field.setStyle(isFocused ? focused : base));
                return field;
        }

        static String fieldStyle(boolean focused, boolean large) {
                String border = focused ? "#556b2f" : "#45483c";
                String size = large ? "20px" : "13px";
                String weight = large ? "-fx-font-weight: bold;" : "";
                return "-fx-background-color: #1e201c; -fx-border-color: transparent transparent " + border
                                + " transparent;"
                                + " -fx-border-width: 0 0 2 0; -fx-border-radius: 0; -fx-background-radius: 0;"
                                + " -fx-text-fill: #e3e3dc; -fx-font-size: " + size + "; " + weight
                                + " -fx-font-family: 'Consolas', monospace;"
                                + " -fx-padding: 10 12 8 12; -fx-prompt-text-fill: rgba(143,146,132,0.35);";
        }

        public static Button windowCloseBtn(Stage stage) {
                Button btn = new Button("✕");
                btn.setPrefSize(36, 40);
                String base = "-fx-background-color: transparent; -fx-text-fill: rgba(143,146,132,0.6);"
                                + " -fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 0; -fx-border-color: transparent;";
                btn.setStyle(base);
                btn.setOnMouseEntered(e -> btn.setStyle(
                                "-fx-background-color: rgba(147,0,10,0.6); -fx-text-fill: white;"
                                                + " -fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 0; -fx-border-color: transparent;"));
                btn.setOnMouseExited(e -> btn.setStyle(base));
                btn.setOnAction(e -> stage.close());
                return btn;
        }

        public static Button cancelButton(Stage stage) {
                Button btn = new Button("CANCEL");
                btn.setPrefHeight(44);
                btn.setPadding(new Insets(0, 24, 0, 24));
                String base = "-fx-background-color: transparent; -fx-text-fill: #c8c6c6;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                                + " -fx-cursor: hand; -fx-background-radius: 0;"
                                + " -fx-border-color: rgba(69,72,60,0.3); -fx-border-width: 2;";
                btn.setStyle(base);
                btn.setOnMouseEntered(e -> btn.setStyle(base.replace("transparent; -fx-text-fill: #c8c6c6",
                                "#292b26; -fx-text-fill: #e3e3dc")));
                btn.setOnMouseExited(e -> btn.setStyle(base));
                btn.setOnAction(e -> stage.close());
                return btn;
        }

        public static void centre(Stage stage, Window owner) {
                if (owner != null) {
                        stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
                        stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
                }
        }
}
