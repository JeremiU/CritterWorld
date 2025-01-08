package simulation;

import main.Util;

import static model.Constants.DirectionConstants.*;

/**
 * Represents a coordinate in the world.
 * This class is used in {@link Hex} to represent its location within the
 * world matrix.
 */
public record Coordinate(int column, int row) {

    /**
     * Create new Coordinate from column & row
     * Invariant: column >= 0, row >= 0
     */
    public Coordinate {
        assert (column >= 0 && row >= 0);
    }

    @Override
    public int column() {
        return column;
    }

    @Override
    public int row() {
        return row;
    }

    /**
     * Get coordinate that the direction is pointing to
     */
    public Coordinate getCoordinateAt(int direction) {
        if (Util.properMod(direction, DIR_AMOUNT) == TOP) return getTop();
        if (Util.properMod(direction, DIR_AMOUNT) == TOP_RIGHT) return getTopRight();
        if (Util.properMod(direction, DIR_AMOUNT) == TOP_LEFT) return getTopLeft();

        if (Util.properMod(direction, DIR_AMOUNT) == BOTTOM_RIGHT) return getBottomRight();
        if (Util.properMod(direction, DIR_AMOUNT) == BOTTOM) return getBottom();
        else return getBottomLeft(); //only values of 0-5 can be returned with floor modulo arithmetic
    }

    @Override
    public String toString() {
        return "(" + column + ", " + row + ")";
    }

    public Coordinate getTop() {
        return new Coordinate(column, row + 2);
    }

    public Coordinate getTopRight() {
        return new Coordinate(column + 1, row + 1);
    }

    public Coordinate getBottomRight() {
        return new Coordinate(column + 1, row - 1);
    }

    public Coordinate getBottom() {
        return new Coordinate(column, row - 2);
    }

    public Coordinate getBottomLeft() {
        return new Coordinate(column - 1, row - 1);
    }

    public Coordinate getTopLeft() {
        return new Coordinate(column - 1, row + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coordinate coordinate)
            return coordinate.column == column && coordinate.row == row;
        return false;
    }
}