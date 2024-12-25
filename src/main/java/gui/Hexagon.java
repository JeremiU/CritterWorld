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

    private Point2D[] vertices = new Point2D[6];
    private Polygon polygon;

    // These only apply if the Hexagon has a critter on it
    private boolean hasArrow = false;
    private Polygon arrow = null;
    private Text text = null;

    private int sideLength;
    private int height;

    private Hex hex;

    private Color color = Color.DARKORANGE;

    private boolean highlighted = false;


//    private Hex.HexType type = Hex.HexType.INVALID;

    // x and y represent the UPPER LEFT vertex of the Hexagon
    public Hexagon(int x, int y, int sideLength) {
        this.sideLength = sideLength;
        this.height = (int) (Math.sqrt(3)/2 * sideLength);

        moveCorner(x, y);
    }

    // Constructor without position
    public Hexagon(int sideLength, Color color, Hex hex) {
        this.sideLength = sideLength;
        this.height = (int) (Math.sqrt(3)/2 * sideLength);
        this.color = color;
        this.hex = hex;

        moveCorner(0, 0);
    }

    // Moves upper left corner to a new position
    public void moveCorner(int x, int y) {
        vertices[0] = new Point2D(x, y); // The position of the UPPER-LEFT CORNER
        vertices[1] = new Point2D(x + sideLength, y);
        vertices[2] = new Point2D(x + 1.5f * sideLength, y + height);
        vertices[3] = new Point2D(x + sideLength, y + 2 * height);
        vertices[4] = new Point2D(x, y + 2 * height);
        vertices[5] = new Point2D(x - 0.5f * sideLength, y + height);

        // Used for the construction of the Polygon
        double[] points = new double[] {
                vertices[0].getX(), vertices[0].getY(),
                vertices[1].getX(), vertices[1].getY(),
                vertices[2].getX(), vertices[2].getY(),
                vertices[3].getX(), vertices[3].getY(),
                vertices[4].getX(), vertices[4].getY(),
                vertices[5].getX(), vertices[5].getY(),
        };
        this.polygon = new Polygon(points);
        polygon.setFill(color);
    }

/*    public void moveCenter(int x, int y) {
        x += sideLength/2;
        y += height;
        moveCorner(x, y);
    }*/

/*    public void move(int x, int y) {
        int newX = (int) (vertices[0].getX() + x);
        int newY = (int) (vertices[0].getY() + y);
        moveCorner(newX, newY);
    }*/

    public void initArrow() {
        hasArrow = true;
        arrow = new Polygon(
                vertices[0].getX(), vertices[0].getY(),
                vertices[1].getX(), vertices[1].getY(),
                getCenter().getX(), getCenter().getY()
                );
        arrow.setFill(Color.WHITE);

        // Setting the text that represents the Critter's size
        text = new Text(getCenter().getX()-4, getCenter().getY() - (double)height/2, String.valueOf(hex.getCritter().getSize()));

        // Turning the arrow based on the Critter's direction
/*        Rotate rotate = new Rotate(60, getCenter().getX(), getCenter().getY());
        arrow.getTransforms().addAll(rotate);*/
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

//    public void setType(Hex.HexType type) {
//        this.type = type;
//    }

    public void draw(GraphicsContext g) {

        g.setFill(Color.WHITESMOKE);
//        g.setStroke(Color.BLACK);

        // Drawing the lines that represent a Hexagon
        Point2D prevVertex = vertices[5];
        for (Point2D vertex : vertices) {
            g.strokeLine(prevVertex.getX(), prevVertex.getY(), vertex.getX(), vertex.getY());
            prevVertex = vertex;
        }

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
