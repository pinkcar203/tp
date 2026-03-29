package meditrack.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import meditrack.model.Role;
import meditrack.model.Session;
import meditrack.security.PasswordManager;
import meditrack.storage.JsonMediTrackStorage;
import meditrack.storage.JsonSerializableMediTrack;

/**
 * Represents the Login UI screen.
 * Handles user authentication and role selection before granting access to the
 * main application.
 * Uses the "Field Ops Command" tactical design system.
 */
public class LoginScreen extends BorderPane {

    private static final String BG = "#0a0a0a";
    private static final String SURFACE = "#121410";
    private static final String OLIVE = "#556b2f";
    private static final String OLIVE_LIGHT = "#8aa65c";
    private static final String TEXT_DIM = "#8f9284";
    private static final String TEXT_MUTED = "#45483c";
    private static final String BORDER = "#2a2d24";

    private final Runnable onLoginSuccess;
    private final JsonMediTrackStorage storageEngine;

    /**
     * Constructs the Login screen.
     *
     * @param onLoginSuccess A callback function to execute once authentication is
     *                       successful.
     */
    public LoginScreen(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.storageEngine = new JsonMediTrackStorage();
        initializeUI();
    }

    private void initializeUI() {
        setStyle("-fx-background-color: " + BG + ";");
        setTop(buildTitleBar());
        setCenter(buildMainContent());
        setBottom(buildStatusBar());
    }

    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 12, 0, 12));
        bar.setPrefHeight(36);
        bar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 0 0 1 0;");

        Rectangle icon = new Rectangle(12, 12);
        icon.setFill(Color.web(OLIVE));
        icon.setStroke(Color.web(OLIVE_LIGHT));
        icon.setStrokeWidth(1);

        Label title = new Label("  MEDITRACK TERMINAL");
        title.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");

        bar.getChildren().addAll(icon, title);
        return bar;
    }

    private StackPane buildMainContent() {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: " + BG + ";");

        Canvas gridCanvas = new Canvas();
        gridCanvas.widthProperty().bind(stack.widthProperty());
        gridCanvas.heightProperty().bind(stack.heightProperty());
        gridCanvas.widthProperty().addListener((o, ov, nv) -> drawDotGrid(gridCanvas));
        gridCanvas.heightProperty().addListener((o, ov, nv) -> drawDotGrid(gridCanvas));

        // Decorative nested border frames
        Rectangle outerFrame = new Rectangle();
        outerFrame.setFill(Color.TRANSPARENT);
        outerFrame.setStroke(Color.web(OLIVE, 0.18));
        outerFrame.setStrokeWidth(1);
        outerFrame.widthProperty().bind(stack.widthProperty().multiply(0.48));
        outerFrame.setHeight(450);

        Rectangle innerFrame = new Rectangle();
        innerFrame.setFill(Color.TRANSPARENT);
        innerFrame.setStroke(Color.web(OLIVE, 0.08));
        innerFrame.setStrokeWidth(1);
        innerFrame.widthProperty().bind(stack.widthProperty().multiply(0.52));
        innerFrame.setHeight(490);

        VBox form = buildLoginForm();

        stack.getChildren().addAll(gridCanvas, innerFrame, outerFrame, form);
        return stack;
    }

    private void drawDotGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web(OLIVE, 0.07));
        int spacing = 28;
        for (int x = spacing; x < canvas.getWidth(); x += spacing) {
            for (int y = spacing; y < canvas.getHeight(); y += spacing) {
                gc.fillOval(x - 1.5, y - 1.5, 3, 3);
            }
        }
    }

    private VBox buildLoginForm() {
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(340);
        form.setPadding(new Insets(48, 40, 48, 40));

        // Heading
        Label heading = new Label("MEDITRACK LOGIN");
        heading.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;");

        // Divider with subtitle
        HBox divRow = new HBox(8);
        divRow.setAlignment(Pos.CENTER);
        Region l1 = new Region();
        l1.setPrefWidth(36);
        l1.setPrefHeight(1);
        l1.setStyle("-fx-background-color: " + OLIVE + ";");
        Label divText = new Label("SECURE ACCESS");
        divText.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        Region l2 = new Region();
        l2.setPrefWidth(36);
        l2.setPrefHeight(1);
        l2.setStyle("-fx-background-color: " + OLIVE + ";");
        divRow.getChildren().addAll(l1, divText, l2);

        // Password field
        VBox passwordSection = buildFieldSection("MASTER ACCESS KEY");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("••••••••••••");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(inputStyle());
        passwordField.focusedProperty()
                .addListener((o, ov, focused) -> passwordField.setStyle(focused ? inputFocusStyle() : inputStyle()));
        passwordSection.getChildren().add(passwordField);

        
        final String DD_BG = "#1E201C";
        final String DD_BORDER = "#45483C";
        final String DD_TEXT = "#C8C6C6";
        final String DD_SEL_BG = "#B6D088";
        final String DD_SEL_FG = "#233600";

        VBox roleSection = buildFieldSection("OPERATIONAL ROLE");
        ComboBox<Role> roleDropdown = new ComboBox<>();
        roleDropdown.getItems().addAll(Role.values());
        roleDropdown.setPromptText("Select Role");
        roleDropdown.setMaxWidth(Double.MAX_VALUE);

        // Outer box style olive border when closed
        roleDropdown.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);"
                        + "-fx-border-color: " + OLIVE + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 0; -fx-background-radius: 0;"
                        + "-fx-padding: 0;"
                        + "-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");

        // Button cell text shown in the closed box after selection
        roleDropdown.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Select Role");
                    setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-background-color: transparent;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-background-color: transparent;"
                            + " -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");
                }
            }
        });

        // Styles each item in the open popup list
        roleDropdown.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: " + DD_BG + ";");
                    return;
                }
                setText(item.toString());
                // Base unselected style
                String base = "-fx-background-color: " + DD_BG + ";"
                        + "-fx-text-fill: " + DD_TEXT + ";"
                        + "-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;"
                        + "-fx-border-color: transparent; -fx-border-width: 0 0 0 2;"
                        + "-fx-padding: 6 12 6 10;";
                setStyle(base);

                
                setOnMouseEntered(e -> setStyle(
                        "-fx-background-color: " + DD_BORDER + ";"
                                + "-fx-text-fill: " + DD_TEXT + ";"
                                + "-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;"
                                + "-fx-border-color: " + DD_SEL_BG + "; -fx-border-width: 0 0 0 2;"
                                + "-fx-padding: 6 12 6 10;"));
                setOnMouseExited(e -> {
                    if (!isSelected())
                        setStyle(base);
                });

                
                if (isSelected()) {
                    setStyle("-fx-background-color: " + DD_SEL_BG + ";"
                            + "-fx-text-fill: " + DD_SEL_FG + ";"
                            + "-fx-font-weight: bold;"
                            + "-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;"
                            + "-fx-padding: 6 12 6 12;");
                }
            }
        });

        // Popup list background
        roleDropdown.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                javafx.scene.Node popup = roleDropdown.lookup(".list-view");
                if (popup != null) {
                    popup.setStyle("-fx-background-color: " + DD_BG + ";"
                            + "-fx-border-color: " + DD_BORDER + ";"
                            + "-fx-border-width: 1;"
                            + "-fx-background-radius: 0; -fx-border-radius: 0;");
                }
            }
        });

        roleSection.getChildren().add(roleDropdown);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e07070; -fx-font-size: 11px;"
                + " -fx-font-family: 'Consolas', monospace;");

        // Login button
        Button loginBtn = new Button("INITIALISE CONNECTION");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(buttonStyle());
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(buttonHoverStyle()));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(buttonStyle()));
        loginBtn.setOnAction(e -> handleLogin(passwordField.getText(), roleDropdown.getValue(), errorLabel));

        // Network status indicator
        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_RIGHT);
        Circle dot = new Circle(3.5, Color.web(OLIVE_LIGHT));
        FadeTransition pulse = new FadeTransition(Duration.seconds(1.2), dot);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.3);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        Label netLabel = new Label("NETWORK SECURE");
        netLabel.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        statusRow.getChildren().addAll(dot, netLabel);

        form.getChildren().addAll(heading, divRow, passwordSection, roleSection,
                errorLabel, loginBtn, statusRow);
        return form;
    }

    private VBox buildFieldSection(String labelText) {
        VBox section = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + " -fx-font-family: 'Consolas', monospace;");
        section.getChildren().add(lbl);
        return section;
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 12, 0, 12));
        bar.setPrefHeight(28);
        bar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1 0 0 0;");

        HBox left = new HBox(16);
        left.setAlignment(Pos.CENTER);
        left.getChildren().addAll(
                makeStatusLabel("● LOCAL DATA SYNC: OK"),
                makeStatusLabel("● ENCRYPTION: BCRYPT"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label clock = new Label();
        clock.setStyle("-fx-text-fill: " + OLIVE_LIGHT + "; -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        clock.setText(LocalDateTime.now().format(fmt));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> clock.setText(LocalDateTime.now().format(fmt))));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        bar.getChildren().addAll(left, spacer, clock);
        return bar;
    }

    private Label makeStatusLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 9px;"
                + " -fx-font-family: 'Consolas', monospace;");
        return lbl;
    }

    private String inputStyle() {
        return "-fx-background-color: #0d0f0b; -fx-text-fill: " + OLIVE_LIGHT + ";"
                + " -fx-border-color: " + BORDER + "; -fx-border-width: 1;"
                + " -fx-padding: 10 12 10 12; -fx-font-size: 13px;"
                + " -fx-background-radius: 0; -fx-border-radius: 0;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;"
                + " -fx-prompt-text-fill: " + TEXT_MUTED + ";";
    }

    private String inputFocusStyle() {
        return "-fx-background-color: #0d0f0b; -fx-text-fill: " + OLIVE_LIGHT + ";"
                + " -fx-border-color: " + OLIVE + "; -fx-border-width: 0 0 2 0;"
                + " -fx-padding: 10 12 10 12; -fx-font-size: 13px;"
                + " -fx-background-radius: 0; -fx-border-radius: 0;"
                + " -fx-font-family: 'Consolas', 'Courier New', monospace;"
                + " -fx-prompt-text-fill: " + TEXT_MUTED + ";";
    }

    private String buttonStyle() {
        return "-fx-background-color: " + OLIVE + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 13 24 13 24;"
                + " -fx-background-radius: 0; -fx-cursor: hand;"
                + " -fx-font-family: 'Consolas', monospace;";
    }

    private String buttonHoverStyle() {
        return "-fx-background-color: " + OLIVE_LIGHT + "; -fx-text-fill: white;"
                + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 13 24 13 24;"
                + " -fx-background-radius: 0; -fx-cursor: hand;"
                + " -fx-font-family: 'Consolas', monospace;";
    }

    /**
     * Handles the authentication process when the login button is clicked.
     *
     * @param plainTextPassword The password entered by the user.
     * @param selectedRole      The role selected from the dropdown.
     * @param errorLabel        The label used to display authentication errors.
     */
    private void handleLogin(String plainTextPassword, Role selectedRole, Label errorLabel) {
        if (selectedRole == null) {
            errorLabel.setText("! ROLE SELECTION REQUIRED.");
            return;
        }

        Optional<JsonSerializableMediTrack> dataOpt = storageEngine.readData();
        if (dataOpt.isEmpty()) {
            errorLabel.setText("! DATABASE MISSING. RESTART REQUIRED.");
            return;
        }

        String storedHash = dataOpt.get().passwordHash;

        if (PasswordManager.checkPassword(plainTextPassword, storedHash)) {
            Session.getInstance().setRole(selectedRole);
            onLoginSuccess.run();
        } else {
            errorLabel.setText("! AUTH FAILED: INVALID ACCESS KEY.");
        }
    }
}
