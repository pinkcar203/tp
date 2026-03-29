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

import meditrack.logic.Logic;
import meditrack.logic.commands.AddSupplyCommand;
import meditrack.logic.commands.exceptions.CommandException;
import meditrack.logic.parser.CommandType;
import meditrack.logic.parser.Parser;
import meditrack.logic.parser.exceptions.ParseException;
import meditrack.model.ModelManager;

/** Add-supply modal. */
public class AddSupplyModal {

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
        private static final String ERROR = "#ffb4ab";

        public static void show(ModelManager model, Logic logic, Window owner) {
                Stage stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(owner);

                // Fields
                TextField nameField = styledField("ENTER ITEM NAME...", false);
                TextField qtyField = styledField("000", true);
                TextField expiryField = styledField("YYYY-MM-DD", false);
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

                Label titleLbl = new Label("ADD SUPPLY RECORD");
                titleLbl.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                titleBar.getChildren().addAll(iconBox, titleLbl, spacer, EditSupplyModal.windowCloseBtn(stage));

                // Make title bar draggable
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

                // Nomenclature field
                VBox nameSection = fieldSection("NOMENCLATURE", nameField);

                // Quantity + Expiry side by side
                VBox qtySection = fieldSection("QUANTITY", qtyField);
                VBox expirySection = fieldSection("EXPIRY DATE", expiryField);
                Label expiryHint = new Label("FORMAT: YYYY-MM-DD");
                expiryHint.setStyle("-fx-text-fill: rgba(143,146,132,0.5); -fx-font-size: 9px; -fx-font-weight: bold;"
                                + " -fx-font-family: 'Consolas', monospace;");
                expirySection.getChildren().add(expiryHint);

                HBox twoCol = new HBox(24, qtySection, expirySection);
                HBox.setHgrow(qtySection, Priority.ALWAYS);
                HBox.setHgrow(expirySection, Priority.ALWAYS);

                // Info bar
                HBox infoBar = new HBox(10);
                infoBar.setAlignment(Pos.CENTER_LEFT);
                infoBar.setPadding(new Insets(12, 16, 12, 14));
                infoBar.setStyle("-fx-background-color: rgba(85,107,47,0.08);"
                                + " -fx-border-color: " + PRIMARY_CONT + "; -fx-border-width: 0 0 0 2;");
                Label infoLbl = new Label(
                                "Verify nomenclature and quantities before confirmation. Record will be logged.");
                infoLbl.setWrapText(true);
                infoLbl.setStyle("-fx-text-fill: rgba(227,227,220,0.55); -fx-font-size: 9px;"
                                + " -fx-font-family: 'Consolas', monospace;");
                infoBar.getChildren().add(infoLbl);

                body.getChildren().addAll(nameSection, twoCol, infoBar, errorLabel);

                // Footer
                HBox footer = new HBox(12);
                footer.setAlignment(Pos.CENTER_RIGHT);
                footer.setPadding(new Insets(14, 20, 14, 20));
                footer.setStyle("-fx-background-color: rgba(41,43,38,0.5); -fx-border-color: rgba(69,72,60,0.1);"
                                + " -fx-border-width: 1 0 0 0;");

                Button cancelBtn = new Button("CANCEL");
                cancelBtn.setPrefHeight(44);
                cancelBtn.setPadding(new Insets(0, 24, 0, 24));
                String cancelBase = "-fx-background-color: transparent; -fx-text-fill: " + SECONDARY + ";"
                                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;"
                                + " -fx-cursor: hand; -fx-background-radius: 0;"
                                + " -fx-border-color: rgba(69,72,60,0.3); -fx-border-width: 2;";
                cancelBtn.setStyle(cancelBase);
                cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(cancelBase
                                .replace("transparent; -fx-text-fill: " + SECONDARY,
                                                SURFACE_HIGH + "; -fx-text-fill: " + ON_SURFACE)));
                cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(cancelBase));
                cancelBtn.setOnAction(e -> stage.close());

                Button confirmBtn = new Button("CONFIRM RECORD  →");
                confirmBtn.setPrefHeight(44);
                confirmBtn.setPadding(new Insets(0, 24, 0, 24));
                String confirmBase = "-fx-background-color: linear-gradient(to bottom, " + PRIMARY + ", " + PRIMARY_CONT
                                + ");"
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
                        String qty = qtyField.getText().trim();
                        String expiry = expiryField.getText().trim();

                        Parser parser = new Parser(model);
                        try {
                                parser.validate(CommandType.ADD_SUPPLY,
                                                Map.of("name", name, "qty", qty, "expiry", expiry));
                        } catch (ParseException ex) {
                                errorLabel.setText("! " + ex.getMessage());
                                return;
                        }
                        try {
                                logic.executeCommand(new AddSupplyCommand(name, Integer.parseInt(qty),
                                                LocalDate.parse(expiry)));
                                stage.close();
                        } catch (CommandException ex) {
                                errorLabel.setText("! " + ex.getMessage());
                        }
                });

                footer.getChildren().addAll(cancelBtn, confirmBtn);

                // Assemble root
                VBox root = new VBox(0, titleBar, body, footer);
                root.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: rgba(143,146,132,0.2);"
                                + " -fx-border-width: 1;");

                Scene scene = new Scene(root, 520, 440);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);

                // Centre relative to owner
                stage.setOnShown(ev -> EditSupplyModal.centre(stage, owner));
                stage.showAndWait();
        }

        // Helpers

        private static VBox fieldSection(String label, TextField field) {
                return EditSupplyModal.fieldSection(label, field);
        }

        private static TextField styledField(String prompt, boolean large) {
                TextField field = new TextField();
                field.setPromptText(prompt);
                String base = EditSupplyModal.fieldStyle(false, large);
                String focused = EditSupplyModal.fieldStyle(true, large);
                field.setStyle(base);
                field.focusedProperty()
                                .addListener((obs, was, isFocused) -> field.setStyle(isFocused ? focused : base));
                return field;
        }
}
