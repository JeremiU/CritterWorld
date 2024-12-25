package gui;

import controller.ControllerFactory;
import controller.ControllerImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simulation.Hex;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static model.Constants.DEFAULT_CRITTER_LOAD;

public class DisplayController extends Application {

    private final ControllerImpl controller = ControllerFactory.getConsoleController();
    private static Stage currentStage;
    private static Scene currentScene;
    private static Group currentGroup;

    // All the hexagonal tiles
    private final ArrayList<Hexagon> hexagons = new ArrayList<>();

    // Used to scale and move the hexagons
    public static Scale scale = new Scale(0.1, 0.1);
    public static Translate translate = new Translate(0, 0);

    // The currently highlighted Hex that the user selected
    private static Hexagon highlightedHex;

    @FXML
    private TextField playRate, critterNumber;

    // Used to run the simulation at certain time intervals
    Timeline timer;

    @FXML
    private StackPane mainStackPane;

    @FXML
    private Button timerStart, loadCritter, singleStep, pauseTimer, onPrintClick;

    @FXML
    private MenuButton loadWorldMenu;

    @FXML
    private TextFlow helpLabels;

    @Override
    public void start(Stage stage) throws Exception {
        currentStage = stage;

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("design.fxml")));

        currentScene = new Scene(root);
        currentStage.setScene(currentScene);
        currentStage.show();
        currentStage.setMinWidth(600);
        currentStage.setMinHeight(600);
        currentStage.setTitle("CS 2112 CritterWorld");

        currentGroup = (Group) currentStage.getScene().lookup("#mainGroup");
        mainStackPane = (StackPane) currentStage.getScene().lookup("#mainStackPane");
        currentGroup = (Group) mainStackPane.lookup("#mainGroup");
        loadWorldMenu = (MenuButton) currentStage.getScene().lookup("#loadWorldMenu");
        timerStart = (Button) currentStage.getScene().lookup("#timerStart");
        loadCritter = (Button) currentStage.getScene().lookup("#loadCritter");
        singleStep = (Button) currentStage.getScene().lookup("#singleStep");
        pauseTimer = (Button) currentStage.getScene().lookup("#pauseTimer");
        onPrintClick = (Button) currentStage.getScene().lookup("#onPrintClick");
        helpLabels = (TextFlow) currentStage.getScene().lookup("#helpLabels");
        playRate = (TextField) currentStage.getScene().lookup("#playRate");
        critterNumber = (TextField) currentStage.getScene().lookup("#critterNumber");

        Text stepsText = new Text("Steps: 0");
        stepsText.setId("stepsNumber");

        Text critterText = new Text("Critters: 0");
        critterText.setId("crittersNumber");

        Text zoomText = new Text("ZOOM - mousewheel");
        Text panText = new Text("PAN - WASD/arrow keys");
        Text clickText = new Text("CLICK - on Hex to see info.");

        helpLabels.getChildren().addAll(stepsText, new Text("\n"), critterText, new Text("\n"), zoomText, new Text("\n"), panText, new Text("\n"), clickText);

        adjustMenuButtonWidth(timerStart, timerStart.getText(), timerStart.getFont());
        adjustMenuButtonWidth(loadCritter, loadCritter.getText(), loadCritter.getFont());
        adjustMenuButtonWidth(onPrintClick, onPrintClick.getText(), onPrintClick.getFont());
        adjustMenuButtonWidth(singleStep, singleStep.getText(), singleStep.getFont());
        adjustMenuButtonWidth(pauseTimer, pauseTimer.getText(), pauseTimer.getFont());
        adjustMenuButtonWidth(loadWorldMenu, loadWorldMenu.getText(), loadWorldMenu.getFont());
        adjustMenuButtonWidth(playRate, playRate.getPromptText(), playRate.getFont());
        adjustMenuButtonWidth(critterNumber, critterNumber.getPromptText(), critterNumber.getFont());
        initAll();
    }

    private void adjustMenuButtonWidth(Region label, String menuText, Font font) {
        Text text = new Text(menuText + "\n");
        text.setFont(font);
        double textWidth = text.getLayoutBounds().getWidth(); // Use the actual width from the text layout
        double totalPadding = 20;
        double buttonWidth = textWidth + totalPadding;

        final double MARGIN = 7.5;
        HBox.setMargin(label, new Insets(MARGIN));
        if (menuText.equalsIgnoreCase("load world"))
            HBox.setMargin(label, new Insets(MARGIN, MARGIN*2, MARGIN, MARGIN)); //allows for drop-down arrow

        double arrowWidth = (label instanceof MenuButton) ? 20.0 : 0;
        label.setPrefWidth(buttonWidth + arrowWidth);
    }

    private void adjustGroupScale() {
        if (mainStackPane != null && currentGroup != null) {
            double scaleX = mainStackPane.getWidth() / 680.0;
            double scaleY = mainStackPane.getHeight() / 500.0;
            double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio

            scale = Math.max(scale, 0.1); // Prevent shrinking too small
            currentGroup.setScaleX(scale);
            currentGroup.setScaleY(scale);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    private void onPrintClick() {
    }

    @FXML
    private void singleStep() {
        controller.advanceTime(1);
        updateDashboard();
        updateInfo();
    }

    private void updateInfo() {
        String text = highlightedHex == null ? "Information Panel" : highlightedHex.getHex().printInfo();
        Label console = (Label) currentStage.getScene().lookup("#displayConsole");
        console.setText(text);
    }

    @FXML
    private void newWorld() {
        hexagons.clear();
        controller.newWorld();
        currentStage.setTitle("CS 2112 CritterWorld: Playing " + controller.getReadOnlyWorld().getWorldName());
        initAll();
        updateInfo();
    }

    @FXML
    private void loadCritterFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile == null) return;

        hexagons.clear();
        currentGroup.getChildren().clear();

        TextField critterNumber = (TextField) currentStage.getScene().lookup("#critterNumber");
        int critterLoad = DEFAULT_CRITTER_LOAD;

        if (!critterNumber.getText().isEmpty() && critterNumber.getText().matches("\\d+"))
            critterLoad = Integer.parseInt(critterNumber.getText());

        controller.loadCritters(selectedFile.getAbsolutePath(), critterLoad);
        initAll();
        updateInfo();
    }

    @FXML
    private void chooseWorld() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile == null) return; //TODO: Check world file format
        hexagons.clear();

        String path = selectedFile.getAbsolutePath();

        controller.loadWorld(path, true, false);
        currentStage.setTitle("CS 2112 CritterWorld: Playing " + controller.getReadOnlyWorld().getWorldName());
        initAll();
    }

    private void initGrid() {
        currentGroup.getChildren().clear();
        Hex[][] hexes = controller.currentWorld.getAllHexes();

        ArrayList<Hex> specialRow = new ArrayList<>();
        ArrayList<Hex> row = new ArrayList<>();
        ArrayList<ArrayList<Hex>> rows = new ArrayList<>();

        // Splitting the hexes into pairs of rows
        for (int i = hexes.length - 1; i >= 0; i--) {
            // Special Row if it's the first row in a world with an odd number of rows
            if (hexes.length % 2 == 1 && i == hexes.length-1) {
                for (int j = 0; j < hexes[i].length; j++) {
                    if (j % 2 == 1) continue;
                    specialRow.add(hexes[i][j]);
                }
            }

            if (i % 2 == 0) continue;
            for (int j = 0; j < hexes[i].length; j++) {
                if (j % 2 == 1) {
                    row.add(hexes[i-1][j-1]);
                    row.add(hexes[i][j]);
                }
            }

            rows.add(new ArrayList<>(row));
            row.clear();
        }

        // Temporary initial "root" hexagon (deleted after grid is initialized)
        boolean firstHex = true;
        int adjustment = (controller.currentWorld.getWidth() % 2 == 0) ? 0 : 1;

        Hexagon originHex = new Hexagon(100, 100, 20);
        hexagons.add(originHex);

        if (controller.currentWorld.getWidth() % 2 == 1) {
            ArrayList<Hex> finalColumn = new ArrayList<>();
            for (int i = 0; i < hexes.length; i++)
                for (int j = 0; j < hexes[i].length; j++)
                    if (j == hexes[i].length - 1 && Hex.isValidHexCoordinate(i, j))
                        finalColumn.add(0, hexes[i][j]);

            for (int i = 0; i < rows.size(); i++) rows.get(i).add(finalColumn.get(i));
        }

        // Drawing the hexagonal tiles
        boolean upFirst = true;
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < rows.get(i).size(); j++) {
                Hexagon newHexagon = rows.get(i).get(j).getHexagon();
                if (j == 0 && i != 0) {
                    if (controller.currentWorld.getWidth() % 2 == 1) {
                        hexagons.get(hexagons.size() - (controller.currentWorld.getWidth())).attachBottom(newHexagon);
                        upFirst = false;
                    } else {
                        hexagons.get(hexagons.size()+adjustment - (controller.currentWorld.getWidth())).attachBottom(newHexagon);
                    }
                } else {
                    if (upFirst) hexagons.get(hexagons.size() - 1).attachBottomRight(newHexagon);
                    else hexagons.get(hexagons.size() - 1).attachTopRight(newHexagon);
                    upFirst = !upFirst;
                }
                hexagons.add(newHexagon);
            }
            upFirst = !upFirst;
            if (firstHex) {
                hexagons.remove(originHex);
                firstHex = false;
            }
        }

        // for all children in Group, if hex is rightmost -> add element of finalColumn
        // Adding the special row to the top of the Hexagon matrix
        int nextSpecialHex = 0;
        if (controller.currentWorld.getHeight() % 2 == 1) {
            for (int i = 0; i < controller.currentWorld.getWidth(); i++) {
                if (i % 2 == 0) {
                    Hexagon newHexagon = specialRow.get(nextSpecialHex).getHexagon();
                    nextSpecialHex++;

                    hexagons.get(i).attachTop(newHexagon);
                    hexagons.add(newHexagon);
                }
            }
        }

        for (int i = 0; i < hexagons.size(); i++) {
            Polygon polygon = getPolygon(i);
            polygon.getTransforms().add(scale); // Scaling

            if (hexagons.get(i).hasArrow()) {
                hexagons.get(i).initArrow();
                Polygon arrow = hexagons.get(i).getArrow();
                arrow.getTransforms().add(scale);
                Text text = hexagons.get(i).getText();
                text.getTransforms().add(scale);
            }

            currentGroup.getChildren().add(polygon);

            if (hexagons.get(i).hasArrow()) {
                hexagons.get(i).setArrowDirection(); // Rotating
                currentGroup.getChildren().add(hexagons.get(i).getArrow());
                currentGroup.getChildren().add(hexagons.get(i).getText());
            }
            currentGroup.getTransforms().add(translate); // Transforming
        }
    }

    private Polygon getPolygon(int i) {
        Polygon polygon = hexagons.get(i).getPolygon();
        polygon.setOnMouseClicked(mouseEvent -> {
            Label console = (Label) currentStage.getScene().lookup("#displayConsole");
            console.setText(hexagons.get(i).getHex().printInfo());

            if (highlightedHex != null) highlightedHex.deHighlight();
            hexagons.get(i).highlight();
            highlightedHex = hexagons.get(i);
        });
        return polygon;
    }

    public void addListeners() {
        //resize listeners
        mainStackPane.widthProperty().addListener((obs, oldVal, newVal) -> adjustGroupScale());
        mainStackPane.heightProperty().addListener((obs, oldVal, newVal) -> adjustGroupScale());

        //only non-zero integers
        critterNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || Integer.parseInt(newValue) <= 0) critterNumber.setText(oldValue);
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
                case W, UP -> translate.setY(flatten(Math.max(-0.04, translate.getY() - 0.01)));
                case A, LEFT -> translate.setX(flatten(Math.max(-0.15, translate.getX() - 0.01)));
                case S, DOWN -> translate.setY(flatten(Math.min(0.1, translate.getY() + 0.01)));
                case D, RIGHT -> translate.setX(flatten(Math.min(0.13, translate.getX() + 0.01)));
                case EQUALS, PLUS, ADD -> {
                    scale.setX(Math.min(0.18, flatten(scale.getX() + 0.01)));
                    scale.setY(Math.min(0.18, flatten(scale.getY() + 0.01)));
                }
                case MINUS, SUBTRACT -> {
                    scale.setX(Math.max(0.1, flatten(scale.getX() - 0.01)));
                    scale.setY(Math.max(0.1, flatten(Math.max(-0.07, scale.getY() - 0.01))));
                }
            }
            event.consume();
        });
    }

    public void updateDashboard() {
        int stepNum = controller.getReadOnlyWorld() != null ? controller.getReadOnlyWorld().getSteps() : 0;
        int critNum = controller.getReadOnlyWorld() != null ? controller.currentWorld.critterNumber() : 0;

        Text steps = (Text) currentStage.getScene().lookup("#stepsNumber");
        steps.setText("Steps: " + stepNum);

        Text critters = (Text) currentStage.getScene().lookup("#crittersNumber");
        critters.setText("Critters: " + critNum);
    }

    private void initAll() {
        updateDashboard();
        addListeners();
        if (controller.getReadOnlyWorld() != null) initGrid();
    }

    @FXML
    private void timerStart() {
        Text text = (Text) currentStage.getScene().lookup("#stepsNumber");
        if (!text.getText().equalsIgnoreCase("Steps: 0")) return;

        timer = new Timeline(new KeyFrame(Duration.seconds(0), event -> singleStep()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    @FXML
    private void pauseTimer() {
        if (timer != null) timer.pause();
    }

    private double flatten(double d) {
        return ((int)(d * 100)) / 100.0;
    }
}