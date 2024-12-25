package gui;

import controller.ControllerFactory;
import controller.ControllerImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simulation.Hex;
import simulation.World;

import java.awt.*;
import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DisplayController extends Application {

    private final ControllerImpl controller = ControllerFactory.getConsoleController();
    private static Stage currentStage;
    private static Scene currentScene;
    private static Group currentGroup;

    // All the custom Game objects that can be drawn to a GraphicsContext
    private final ArrayList<GameDrawable> drawables = new ArrayList<>();

    // All the hexagonal tiles
    private final ArrayList<Hexagon> hexagons = new ArrayList<>();

    // Used to scale and move the hexagons
    public static Scale scale = new Scale(0.1, 0.1);
    public static Translate translate = new Translate(0, 0);

    // The currently highlighted Hex that the user selected
    private static Hexagon highlightedHex;

    // Used to run the simulation at certain time intervals
    Timeline timer;


    @Override
    public void start(Stage stage) throws Exception {
        //load src\test\resources\files\world_loader_test.txt
        currentStage = stage;

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("design.fxml")));
//
        currentScene = new Scene(root);
        stage.setScene(currentScene);
        stage.show();

        currentGroup = (Group) currentStage.getScene().lookup("#mainGroup");

        initAll();
        drawGroup(stage);

//        Scene scene = new Scene(root, 300, 275);
//
//        stage.setTitle("FXML Welcome");
//        stage.setScene(scene);
//        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Supplementary methods used in GUI Controller

    @FXML
    private void displayWorld() {
//        clearCanvas((Canvas) currentStage.getScene().lookup("#canvas"));
        drawGroup(currentStage);
    }

    @FXML
    private void singleStep() {
        controller.advanceTime(1);
        updateDashboard();
    }

    @FXML
    private void newWorld() {
        hexagons.clear();
        controller.newWorld();
        initAll();
//        clearCanvas((Canvas) currentStage.getScene().lookup("#canvas"));
        drawGroup(currentStage);
    }

    @FXML
    private void loadCritterFile() {
        hexagons.clear();
        currentGroup.getChildren().clear();

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        // Reading the number of critters the user wants to add to the world
        TextField critterNumber = (TextField) currentStage.getScene().lookup("#critterNumber");
        if (!critterNumber.getText().equals("") && critterNumber.getText().matches("\\d+")) {

            int critterNum = Integer.parseInt(critterNumber.getText());

            // Ensuring nothing happens if the user cancels the file selection menu
            if (selectedFile != null) {
//                controller.loadWorld(selectedFile.getAbsolutePath(), true, false);
                System.out.println("current world: " + controller.currentWorld);
                controller.loadCritters(selectedFile.getAbsolutePath(), critterNum); //TODO: Critter Number SELECTION (user)
                initAll();
            }
        }
    }

    private void drawGroup(Stage currentStage) {
        // Testing the new Hexagon "Polygon" representation
//        Polygon hexagon = new Polygon();
//        hexagon.getPoints().addAll(200.0, 50.0,
//                400.0, 50.0,
//                450.0, 150.0,
//                400.0, 250.0,
//                200.0, 250.0,
//                150.0, 150.0);
//        currentGroup.getChildren().add(hexagon);
    }

    @FXML
    private void chooseFile() {
        hexagons.clear();
        controller.currentWorld = null;
        currentGroup.getChildren().clear();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        // Ensuring nothing happens if the user cancels the file selection menu
        if (selectedFile != null) {
            controller.loadWorld(selectedFile.getAbsolutePath(), true, false);
            initAll();
        }
    }

    private void initGrid() {
        System.out.println(controller.currentWorld);

        currentGroup.getChildren().clear();
        Hex[][] hexes = controller.currentWorld.getAllHexes();

        ArrayList<Hex> specialRow = new ArrayList<>();

        ArrayList<Hex> row = new ArrayList<>();

        ArrayList<ArrayList<Hex>> rows = new ArrayList<>();

        int normalized = 0;

        // Splitting the hexes into pairs of rows
        for (int i = hexes.length - 1; i >= 0; i--) {
//            System.out.println("viewed: " + i);

            // Special Row if it's the first row in a world with an odd number of rows
            if (hexes.length % 2 == 1 && i == hexes.length-1) {
//                specialRow.addAll(Arrays.asList(hexes[i]));
                for (int j = 0; j < hexes[i].length; j++) {
                    if (j % 2 == 1) continue;
                    specialRow.add(hexes[i][j]);
                }
            }

            if (i % 2 == 0) continue;
            for (int j = 0; j < hexes[i].length; j++) {
//                System.out.println("viewed: " + hexes[i][j].getColumn());
                if (j % 2 == 1) {

                    row.add(hexes[i-1][j-1]);
                    row.add(hexes[i][j]);

                    //TODO: THis part has the error in it!!! FIx plx ;)
//                    System.out.println("j: " + j);
//                    System.out.println("hexes.length-2: " + (hexes.length-2));
/*                    if (j+1 == hexes[i-1].length-1) {
                        hexes[i - 1][j + 1].setType(Hex.HexType.INVALID);
                        row.add(hexes[i - 1][j + 1]);
                        normalized = 1;
                    }*/



                }
            }

            // Adding all the rows to the "rows" list
            //TODO: stilt? check if this works (adding dummy hex to end of shorter row
//            if (controller.currentWorld.getWidth() % 2 == 1) {
//                row.add(new Hex(0, 0, Hex.HexType.INVALID, 0));
//            }

            rows.add(new ArrayList<>(row));
            row.clear();
        }

        //TODO: deleteME (displaying Hexes)
/*        System.out.println("hexes as they are stored");
        for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[i].length; j++) {
                System.out.print(hexes[i][j]);
            }
            System.out.println();
        }*/

        // Displaying the contents of "rows"
/*        for (int j = 0; j < rows.size(); j++) {
            for (int k = 0; k < rows.get(j).size(); k++) {
                System.out.print(rows.get(j).get(k));
            }
            System.out.println("");
        }*/

        // Temporary initial "root" hexagon (deleted after grid is initialized)
        boolean firstHex = true;
        int adjustment = (controller.currentWorld.getWidth() % 2 == 0) ? 0 : 1;

        Hexagon originHex = new Hexagon(100, 100, 20);
        hexagons.add(originHex);


        //TODO: add the last column of the world
        if (controller.currentWorld.getWidth() % 2 == 1) {
            ArrayList<Hex> finalColumn = new ArrayList<>();
            for (int i = 0; i < hexes.length; i++) {
                for (int j = 0; j < hexes[i].length; j++) {
                    if (j == hexes[i].length - 1 && Hex.isValidHexCoordinate(i, j)) {
                        finalColumn.add(0, hexes[i][j]);
                    }
                }
            }
            int next = 0;
            for (ArrayList<Hex> line : rows) {
                line.add(finalColumn.get(next));
                next++;
            }
        }



        // Drawing the hexagonal tiles
        boolean upFirst = true;
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < rows.get(i).size(); j++) {

                Hexagon newHexagon = rows.get(i).get(j).getHexagon();
                if (j == 0 && i != 0) {
                    // Of the width is ODD and ...? TODO: check!
                    if (controller.currentWorld.getWidth() % 2 == 1) {
                        hexagons.get(hexagons.size() - (controller.currentWorld.getWidth())).attachBottom(newHexagon);
                        upFirst = false;
                    } else {
                        // Adding hexagon to the first column
                        hexagons.get(hexagons.size()+adjustment - (controller.currentWorld.getWidth())).attachBottom(newHexagon);
                    }
                } else {
                    if (upFirst) {
                        hexagons.get(hexagons.size() - 1).attachBottomRight(newHexagon);
                    } else {
                        hexagons.get(hexagons.size() - 1).attachTopRight(newHexagon);
                    }
                    upFirst = !upFirst;
                }
                hexagons.add(newHexagon);
            }
            upFirst = !upFirst;
            if (firstHex) {
                hexagons.remove(originHex);
                firstHex = false;
//                System.out.println("deleted anchor hex");
            }
        }


        // for all children in Group, if hex is rightmost -> add element of finalColumn

        // Removing the anchor hex
//        hexagons.remove(0);

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



        // Adding on Click Listeners for all the Hexagons
        for (int i = 0; i < hexagons.size(); i++) {
            // Adding onClickListeners
            Polygon polygon = hexagons.get(i).getPolygon();
            int finalI = i;
            polygon.setOnMouseClicked(mouseEvent -> {

                // Showing the hex info on the displayConsole
                Label console = (Label) currentStage.getScene().lookup("#displayConsole");
                console.setText(hexagons.get(finalI).getHex().printInfo());

                // Highlighting the Hex the user clicks on
                if (highlightedHex != null)
                    highlightedHex.deHighlight();
                hexagons.get(finalI).highlight();
                highlightedHex = hexagons.get(finalI);

            });

            // Scaling the Polygons
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

    public void addListeners() {
        currentScene.setOnScroll(event -> {
            scale.setPivotX(MouseInfo.getPointerInfo().getLocation().getX());
            scale.setPivotY(MouseInfo.getPointerInfo().getLocation().getY());
            if (event.getDeltaY() > 0) {
                scale.setX(scale.getX() + 0.01);
                scale.setY(scale.getY() + 0.01);
            } else if (scale.getX() >= 0.2 && scale.getY() >= 0.2){
                scale.setX(scale.getX() - 0.01);
                scale.setY(scale.getY() - 0.01);
            }
            event.consume();
        });
        currentScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    translate.setY(translate.getY() - 0.01);
                    break;
                case A:
                    translate.setX(translate.getX() - 0.01);
                    break;
                case S:
                    translate.setY(translate.getY() + 0.01);
                    break;
                case D:
                    translate.setX(translate.getX() + 0.01);
                    break;

                case BACK_SPACE:
                    System.out.println("Scale: " + scale);
                    System.out.println("Translate: " + translate);
            }
            event.consume();
        });
    }


    public void updateDashboard() {
        Label steps = (Label) currentStage.getScene().lookup("#steps_number");
        steps.setText("Steps: " + controller.currentWorld.getSteps());

        Label critters = (Label) currentStage.getScene().lookup("#critters_number");
        critters.setText("Critters: " + controller.currentWorld.critterNumber());

    }



    private void initAll() {
        if (controller.currentWorld != null) {
            addListeners();
            initGrid();
            updateDashboard();
        }
    }

    private void drawAll(GraphicsContext g) {
        drawBorder(g);
        for (GameDrawable drawable : drawables) {
            g.setStroke(drawable.getColor());
            drawable.draw(g);
        }
    }

    private void drawBorder(GraphicsContext g) {
        final double canvasWidth = g.getCanvas().getWidth();
        final double canvasHeight = g.getCanvas().getHeight();

        g.setStroke(Color.BLACK);
        g.setLineWidth(1);
        g.strokeRect(0, 0, canvasWidth - 1, canvasHeight - 1);

    }

    @FXML
    private void timerStart() {
        TextField rate = (TextField) currentStage.getScene().lookup("#playRate");
        System.out.println(Double.parseDouble(rate.getText()));

        if (!rate.getText().equals("")) {
            timer = new Timeline(
                    new KeyFrame(Duration.seconds(Double.parseDouble(rate.getText())),
                            event -> {
                                singleStep();
                            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
        }
    }

    @FXML
    private void pauseTimer() {
        timer.pause();
    } //playRate






}
