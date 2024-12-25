package gui;

import javafx.geometry.Point2D;

public final class HexagonFactory {

    private HexagonFactory() {}

    public static Hexagon fromCorner(int x, int y, int sideLength) {
        return new Hexagon(x, y, sideLength);
    }

    public static Hexagon fromCorner(Point2D point, int sideLength) {
        return new Hexagon((int) point.getX(), (int) point.getY(), sideLength);
    }

    public static Hexagon fromCenter(int x, int y, int sideLength) {
        double height = Math.sqrt(3)/2 * sideLength;
        x += sideLength/2;
        y += height;
        return new Hexagon(x, y, sideLength);
    }

    public static Hexagon fromCenter(Point2D point, int sideLength) {
        double height = Math.sqrt(3)/2 * sideLength;
        int x = (int) point.getX();
        int y = (int) point.getY();
        x += sideLength/2;
        y -= height;
        return new Hexagon(x, y, sideLength);
    }
}
