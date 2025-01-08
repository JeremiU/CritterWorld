package gui;

import controller.ControllerImpl;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import main.Util;
import simulation.Coordinate;
import simulation.Hex;

import java.util.ArrayList;

import static model.Constants.DirectionConstants.*;
import static simulation.Hex.HexType.*;

public class Grid {

    private Hexagon highlightedHex;

    private final Group group;
    private final ControllerImpl controller;
    private final Scale scale;
    private final Translate translate;
    private boolean initialized = false;

    private final ArrayList<Hexagon> hexagons = new ArrayList<>();

    public Grid(Group group, ControllerImpl controller, Scale scale, Translate translate) {
        this.group = group;
        this.controller = controller;
        this.scale = scale;
        this.translate = translate;
    }

    public void init() {
        initialized = true;
        clear();
        Hex[][] hexes = controller.currentWorld.getAllHexes();
        ArrayList<ArrayList<Hex>> rows = new ArrayList<>();
        int width = controller.currentWorld.getWidth();

        // Generate rows from the hexes array
        for (int i = hexes.length - 1; i >= 0; i--) {
            if (i % 2 == 0) continue;
            ArrayList<Hex> row = new ArrayList<>();
            for (int j = 1; j < hexes[i].length; j += 2) {
                row.add(hexes[i - 1][j - 1]);
                row.add(hexes[i][j]);
            }
            rows.add(row);
        }

        // Create and attach hexagons row by row
        boolean upFirst = true;
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < rows.get(i).size(); j++) {
                Hexagon newHexagon = rows.get(i).get(j).getHexagon();

                if (!hexagons.isEmpty()) {
                    boolean ji = j == 0 && i != 0;
                    int indx = hexagons.size() + (ji ? -width : -1);
                    int dir = ji ? BOTTOM : (upFirst ? TOP_RIGHT : BOTTOM_RIGHT);

                    hexagons.get(indx).attachDirection(newHexagon, dir);
                    upFirst = ji == upFirst;
                }
                hexagons.add(newHexagon);
            }
            upFirst = !upFirst;
        }
        addTransformations();
    }

    private void addTransformations() {
        for (int i = 0; i < hexagons.size(); i++) {
            Polygon polygon = getPolygon(i);
            polygon.getTransforms().add(scale); // Scaling

            group.getChildren().add(polygon);

            if (hexagons.get(i).hasArrow()) {
                hexagons.get(i).initArrow();
                Polygon arrow = hexagons.get(i).getArrow();
                arrow.getTransforms().add(scale);
                Text text = hexagons.get(i).getText();
                text.getTransforms().add(scale);

                hexagons.get(i).setArrowDirection(); // Rotating
                group.getChildren().add(hexagons.get(i).getArrow());
                group.getChildren().add(hexagons.get(i).getText());
            }
            group.getTransforms().add(translate); // Transforming
        }
    }

    private final ArrayList<Coordinate> memoized = new ArrayList<>();

    private Polygon getPolygon(int i) {
        Polygon polygon = hexagons.get(i).getPolygon();
        if (!memoized.contains(hexagons.get(i).getHex().getCoordinate())) {
            System.out.println("HI! " + Util.randomInt(100));
            polygon.setFill(hexagons.get(i).getHex().getColor());
            polygon.setStroke(Color.GRAY);
            polygon.setStrokeWidth(4.0);

            polygon.setOnMouseClicked(mouseEvent -> {
                Label console = (Label) group.getScene().lookup("#infoPanel");
                console.setText(hexagons.get(i).getHex().printInfo());

                if (highlightedHex != null) highlightedHex.deHighlight();
                hexagons.get(i).highlight();
                highlightedHex = hexagons.get(i);
            });
            memoized.add(hexagons.get(i).getHex().getCoordinate());
        }
        return polygon;
    }

    public void clear() {
        group.getChildren().clear();
        hexagons.clear();
    }

    public Hexagon getHighlightedHex() {
        return highlightedHex;
    }

    public void updateHexagon(Coordinate c) {
        Hexagon v = null;
        for (Hexagon h : hexagons)
            if (h.getHex().getCoordinate().equals(c)) v = h;
        if (v == null) return;

        Node k = null;
        for (Node n : group.getChildren())
            if (n.equals(v.getPolygon())) k = n;
        if (k == null) return;

        group.getChildren().remove(k);
        memoized.remove(c);

        group.getChildren().add(v.getPolygon());
        System.out.println(v.getHex().getColor());

        v.getPolygon().setFill(v.getHex().getColor());
        v.getPolygon().setStroke(Color.GRAY);
        v.getPolygon().setStrokeWidth(4.0);

        v.setArrow(v.getHex().getType() != CRITTER);
        if (v.getText() != null && v.getHex().getType() != CRITTER) v.getText().setText("");
        if (v.getHex().getType() == CRITTER) {
            v.initArrow();
            Polygon arrow = v.getArrow();
            arrow.getTransforms().add(scale);
            Text text = v.getText();
            text.getTransforms().add(scale);

            v.setArrowDirection(); // Rotating
            group.getChildren().add(v.getArrow());
            group.getChildren().add(v.getText());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}