package gui;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import simulation.Hex;

import java.util.Arrays;

public class Hexagon {

    private final Point2D[] vertices = new Point2D[6];
    private Polygon polygon;

    // These only apply if the Hexagon has a critter on it
    private boolean hasArrow = false;
    private Polygon arrow = null;
    private Text text = null;

    private final int sideLength;
    private final int height;

    private Hex hex;

    private Color color = Color.DARKORANGE;

    // x and y represent the UPPER LEFT vertex of the Hexagon
    public Hexagon(int x, int y, int sideLength) {
        this.sideLength = sideLength;
        this.height = (int) (Math.sqrt(3)/2 * sideLength);
        moveCorner(x, y);
    }

    public Hexagon(int sideLength, Color color, Hex hex) {
        this(0,0, sideLength);
        this.color = color;
        this.hex = hex;
        polygon.setFill(color);
    }

    // Moves upper left corner to a new position
    public void moveCorner(int x, int y) {
        vertices[0] = new Point2D(x, y); // The position of the UPPER-LEFT CORNER
        vertices[1] = new Point2D(x + sideLength, y);
        vertices[2] = new Point2D(x + 1.5f * sideLength, y + height);
        vertices[3] = new Point2D(x + sideLength, y + 2 * height);
        vertices[4] = new Point2D(x, y + 2 * height);
        vertices[5] = new Point2D(x - 0.5f * sideLength, y + height);

        double[] points = new double[vertices.length*2];
        for (int i = 0; i < vertices.length * 2; i++) {
                points[i] = i % 2 == 0 ? vertices[i/2].getX() : vertices[i/2].getY();
        }
        this.polygon = new Polygon(points);
        polygon.setFill(color);
    }

    public void initArrow() {
        hasArrow = true;
        arrow = new Polygon(
                vertices[0].getX(), vertices[0].getY(),
                vertices[1].getX(), vertices[1].getY(),
                getCenter().getX(), getCenter().getY()
        );
        arrow.setFill(Color.WHITE);

        //TODO: fix
        // Setting the text that represents the Critter's size
        text = new Text(getCenter().getX()-4, getCenter().getY() - (double)height/2, String.valueOf(hex.getCritter().getSize()));
    }

    public void setArrowDirection() {
        int angle = hex.getCritter().getDirection() * 60 % 360;
        Rotate rotate = new Rotate(angle, getCenter().getX(), getCenter().getY());
        arrow.getTransforms().addAll(rotate);
    }

    public Polygon getArrow() {
        return arrow;
    }

    public Point2D getCorner(int n) {
        return vertices[n];
    }

    public boolean hasArrow() {
        return hasArrow;
    }

    public void giveArrow() {
        hasArrow = true;
    }

    public Text getText() {
        return text;
    }

    public void attachTopRight(Hexagon hexagon) {
        hexagon.moveCorner((int)(vertices[1].getX() + 0.5f * sideLength)+1, (int)(this.getCorner(0).getY() - this.height));
    }

    public void attachBottomRight(Hexagon hexagon) {
        hexagon.moveCorner((int)(vertices[1].getX() + 0.5f * sideLength)+1, (int)(this.getCorner(0).getY() + this.height));
    }

    public void attachBottom(Hexagon hexagon) {
        hexagon.moveCorner((int)(vertices[0].getX()), (int)(vertices[4].getY())+1);
    }

    public void attachTop(Hexagon hexagon) {
        hexagon.moveCorner((int)(vertices[0].getX()), (int)(vertices[0].getY()-2*height-1));
    }

    public Point2D getCenter() {
        return new Point2D(vertices[0].getX() + (double)sideLength/2, vertices[0].getY()+height);
    }

    public Polygon getPolygon() {
        return this.polygon;
    }

//    @Override
//    public Color getColor() {
//        return this.color;
//    }

    public void setColor(Color color) {
        polygon.setFill(color);
    }
    // getCenter
    // draw from center (Constructor)

    public Hex getHex() {
        return this.hex;
    }

    public void highlight() {
        polygon.setOpacity(0.5);
    }

    public void deHighlight() {
        polygon.setOpacity(1);
    }

    @Override
    public String toString() {
        return "Hexagon{" +
                "vertices=" + Arrays.toString(vertices) +
                ", sideLength=" + sideLength +
                ", height=" + height +
                '}';
    }
}
