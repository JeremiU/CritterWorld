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

    /**
     * Get the Coordinate's column (x position)
     */
    @Override
    public int column() {
        return column;
    }

    /**
     * Get the Coordinate's row (y position)
     */
    @Override
    public int row() {
        return row;
    }

    /**
     * Get coordinate that the direction is pointing to
     */
    public Coordinate getCoordinateAt(int direction) {
        if (Util.properMod(direction, DIR_AMOUNT) == TOP_RIGHT) return getTopRight();
        if (Util.properMod(direction, DIR_AMOUNT) == BOTTOM_RIGHT) return getBottomRight();
        if (Util.properMod(direction, DIR_AMOUNT) == BOTTOM) return getBottom();
        if (Util.properMod(direction, DIR_AMOUNT) == BOTTOM_LEFT) return getBottomLeft();
        else return getTopLeft(); //only values of 0-5 can be returned with floor modulo arithmetic
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
}