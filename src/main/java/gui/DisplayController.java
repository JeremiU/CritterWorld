package gui;

import console.Logger;
import controller.ControllerFactory;
import controller.ControllerImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simulation.Coordinate;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static model.Constants.*;

public class DisplayController extends Application {

    private final ControllerImpl controller = ControllerFactory.getConsoleController();
    private static Stage stage;
    private static Scene currentScene;
    private static Group currentGroup;

    private final AtomicBoolean critterLock = new AtomicBoolean(false);
    private final AtomicBoolean timerLock = new AtomicBoolean(false);

    private boolean isRunning = false;

    public static Scale scale = new Scale(0.1, 0.1);
    public static Translate translate = new Translate(0, 0);

    private Grid grid;

    @FXML
    private CheckBox manna, forcedMutation;

    @FXML
    private Slider playRate, critterNumber;

    Timeline animatorTimeLine;

    @FXML
    private StackPane mainStackPane;

    @FXML
    private Button timerButton, loadCritter, singleStep, onPrintClick;

    @FXML
    private MenuButton loadWorldMenu;

    @FXML
    private Text stepsText, crittersText;

    @Override
    public void start(Stage stage) throws Exception {
        DisplayController.stage = stage;

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("design.fxml")));

        currentScene = new Scene(root);

        String[] iconSizes = {"16x16", "32x32", "48x48", "128x128", "256x256"};
        for (String size : iconSizes) {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(String.format("/icon_%s.png", size)))));
        }

        stage.setScene(currentScene);
        stage.show();
        stage.setMinWidth(600);
        stage.setMinHeight(600);
        stage.setTitle("CS 2112 CritterWorld");

        currentGroup = (Group) currentScene.lookup("#mainGroup");
        mainStackPane = (StackPane) currentScene.lookup("#mainStackPane");
        loadWorldMenu = (MenuButton) currentScene.lookup("#loadWorldMenu");
        timerButton = (Button) currentScene.lookup("#timerButton");
        loadCritter = (Button) currentScene.lookup("#loadCritter");
        singleStep = (Button) currentScene.lookup("#singleStep");
        onPrintClick = (Button) currentScene.lookup("#onPrintClick");
        playRate = (Slider) currentScene.lookup("#playRate");
        critterNumber = (Slider) currentScene.lookup("#critterNumber");
        stepsText = (Text) currentScene.lookup("#stepsText");
        crittersText = (Text) currentScene.lookup("#crittersText");

        grid = new Grid(currentGroup, controller, scale, translate);

        adjustMenuButtonWidth(timerButton, timerButton.getText(), timerButton.getFont());
        adjustMenuButtonWidth(loadCritter, loadCritter.getText(), loadCritter.getFont());
        adjustMenuButtonWidth(onPrintClick, onPrintClick.getText(), onPrintClick.getFont());
        adjustMenuButtonWidth(singleStep, singleStep.getText(), singleStep.getFont());
        adjustMenuButtonWidth(loadWorldMenu, loadWorldMenu.getText(), loadWorldMenu.getFont());
        initAll();
    }

    @FXML
    private void onPrintClick() {
        if (isNull(controller.getReadOnlyWorld(), "No World Loaded!")) return;
        controller.printWorld(System.out);
    }

    @FXML
    private void step() {
        if (isNull(controller.getReadOnlyWorld(), "No World Loaded!")) return;
        controller.advanceTime(1);
        updateDashboard();
        updateInfo();
    }

    @FXML
    private void newWorld() {
        startWorld(null);
    }

    @FXML
    private void chooseWorld() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (isNull(selectedFile, "No World File selected!")) return; //TODO: Check world file format
        startWorld(selectedFile.getAbsolutePath());
    }

    @FXML
    private void loadCritterFile() {
        FileChooser fileChooser = new FileChooser();
        if (isNull(controller.getReadOnlyWorld(), "No World Loaded!")) return; //TODO: Check world file format
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (isNull(selectedFile, "No Critter File selected!")) return;

        int critterLoad = (int) critterNumber.getValue();

        controller.loadCritters(selectedFile.getAbsolutePath(), critterLoad);
        for (Coordinate c : controller.currentWorld.getInsertedCordinates()) {
            controller.currentWorld.updateHex(c);
        }
        controller.currentWorld.clearInsertedCordinates();
    }

    @FXML
    private void timerButton() {
        if (isNull(controller.getReadOnlyWorld(), "No World Loaded!")) return;
        double interval = playRate.getValue();

        if (isRunning) {
            animatorTimeLine.pause();
        } else {
            animatorTimeLine = new Timeline(new KeyFrame(Duration.seconds(interval), event -> step()));
            animatorTimeLine.setCycleCount(Timeline.INDEFINITE);
            animatorTimeLine.play();
        }
        timerButton.setText(isRunning ? "▶" : "⏸");
        isRunning = !isRunning;
    }

    private void adjustMenuButtonWidth(Region label, String menuText, Font font) {
        Text text = new Text(menuText + "\n");
        text.setFont(font);
        double textWidth = text.getLayoutBounds().getWidth(); // Use the actual width from the text layout
        double totalPadding = 20;
        double MARGIN = 7.5;

        HBox.setMargin(label, new Insets(MARGIN));
        if (menuText.equalsIgnoreCase("load world"))
            HBox.setMargin(label, new Insets(MARGIN, MARGIN*2, MARGIN, MARGIN)); //allows for drop-down arrow

        double arrowWidth = (label instanceof MenuButton) ? 20.0 : 0;
        label.setPrefWidth(textWidth + totalPadding + arrowWidth);
    }

    private void adjustGroupScale() {
        if (mainStackPane == null || currentGroup == null) return;
        double scaleX = mainStackPane.getWidth() / 600.0;
        double scaleY = mainStackPane.getHeight() / 600.0;
        double scale = Math.max(Math.min(scaleX, scaleY), 0.1); // Maintain aspect ratio

        currentGroup.setScaleX(scale);
        currentGroup.setScaleY(scale);
    }

    private void updateInfo() {
        String text = grid.getHighlightedHex() == null ? "Information Panel" : grid.getHighlightedHex().getHex().printInfo();
        updateInfo(text, false);
    }

    public static void updateInfo(String text, boolean isErr) {
        Label console = (Label) stage.getScene().lookup("#infoPanel");
        if (isErr) console.setStyle(String.format(PANEL_TEMPLATE, "darkred", "white"));
        else console.setStyle(String.format(PANEL_TEMPLATE, "teal", "white"));
        console.setText(text);
    }

    private void startWorld(String filePath) {
        if (grid == null) grid = new Grid(currentGroup, controller, scale, translate);
        grid.clear();
        if (filePath == null) controller.newWorld();
        else controller.loadWorld(filePath, true, false);

        controller.currentWorld.setGrid(grid);
        controller.currentWorld.setDisplayController(this);

        stage.setTitle("CS 2112 CritterWorld: Playing " + controller.getReadOnlyWorld().getWorldName());
        updateInfo("Loaded " + controller.getReadOnlyWorld().getWorldName() + "!", false);
        initAll();
    }

    public void addListeners() {
        //resize listeners
        mainStackPane.widthProperty().addListener((obs, oldVal, newVal) -> adjustGroupScale());
        mainStackPane.heightProperty().addListener((obs, oldVal, newVal) -> adjustGroupScale());

        snapTicks(critterNumber, critterLock);
        snapTicks(playRate, timerLock);

        critterNumber.valueProperty().addListener((obs, oldVal, newVal) -> {
            int cnt = (int) critterNumber.getValue();
            if (cnt == 1) loadCritter.setText("Load in a Critter");
            else loadCritter.setText("Load in " + cnt + " Critters");
        });

        if (controller.getReadOnlyWorld() == null) return;

        currentGroup.setOnScroll(event -> {
            scale.setPivotX(MouseInfo.getPointerInfo().getLocation().getX());
            scale.setPivotY(MouseInfo.getPointerInfo().getLocation().getY());

            if (event.getDeltaY() > 0) {
                scale.setX(flatten(scale.getX() + 0.01));
                scale.setY(flatten(scale.getY() + 0.01));
            } else if (scale.getX() >= 0.2 && scale.getY() >= 0.2) {
                scale.setX(flatten(scale.getX() - 0.01));
                scale.setY(flatten(scale.getY() - 0.01));
            }
            event.consume();
        });
        currentScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W, UP -> translate.setY(flatten(Math.min(0.1, translate.getY() + 0.01)));
                case A, LEFT -> translate.setX(flatten(Math.max(-0.15, translate.getX() - 0.01)));
                case S, DOWN -> translate.setY(flatten(Math.max(-0.04, translate.getY() - 0.01)));
                case D, RIGHT -> translate.setX(flatten(Math.min(0.13, translate.getX() + 0.01)));
                case EQUALS, PLUS, ADD -> {
                    scale.setX(Math.min(0.3, flatten(scale.getX() + 0.01)));
                    scale.setY(Math.min(0.3, flatten(scale.getY() + 0.01)));
                }
                case MINUS, SUBTRACT -> {
                    scale.setX(Math.max(0.1, flatten(scale.getX() - 0.01)));
                    scale.setY(Math.max(0.1, flatten(Math.max(-0.07, scale.getY() - 0.01))));
                }
            }
            event.consume();
        });

    }

    private void snapTicks(Slider slider, AtomicBoolean lock) {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (lock.get()) return;
            lock.set(true);
            double tickUnit = slider.getMajorTickUnit(),
                    snappedValue = Math.round(newValue.doubleValue() / tickUnit) * tickUnit + 1;
            slider.setValue(snappedValue);
            lock.set(false);
        });
    }

    public void updateDashboard() {
        stepsText.setText("Steps: " + (controller.getReadOnlyWorld() != null ? controller.getReadOnlyWorld().getSteps() : 0));
        crittersText.setText("Critters: " + (controller.getReadOnlyWorld() != null ? controller.currentWorld.getNumberOfAliveCritters() : 0));
    }

    private void initAll() {
        updateDashboard();
        addListeners();
        if (controller.getReadOnlyWorld() != null && !grid.isInitialized()) grid.init();
    }

    private double flatten(double d) {
        return ((int)(d * 100)) / 100.0;
    }

    private boolean isNull(Object o, String err) {
        if (o == null) {
            Logger.error(err, "DisplayController:check", Logger.FLAG_DISPLAY_CONTROLLER);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Logger.updateFlags(Logger.FLAG_ALL);
        launch(args);
    }

    @FXML
    private void updateManna() {
        if (controller.getReadOnlyWorld() != null) controller.currentWorld.setEnableManna(manna.isSelected());
    }

    @FXML
    public void updateMutation() {
        if (controller.getReadOnlyWorld() != null) controller.currentWorld.setEnableManna(forcedMutation.isSelected());
    }
}