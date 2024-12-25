package simulation;

import main.Util;

import static model.Constants.DirectionConstants.*;

/**
 * Represents a coordinate in the world.
 * This class is used in {@link Hex} to represent its location within the
 * world matrix.
 */
public class Coordinate {

    private final int column, row;

    /**
     * Create new Coordinate from column & row
     * <p>
     * Invariant: column >= 0, row >= 0
     */
    public Coordinate(int column, int row) {
        assert (column >= 0 && row >= 0);
        this.column = column;
        this.row = row;
    }

    /**
     * Get the Coordinate's column (x position)
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the Coordinate's row (y position)
     */
    public int getRow() {
        return row;
    }

    /**
     * Get coordinate that the direction is pointing to
     */
    public Coordinate getCoordinateAt(int direction) {
        if (Util.properMod(direction, DIR_AMOUNT) == TOP) return getTop();
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

    // Relative position getters
    private Coordinate getTop() {
        return new Coordinate(column, row + 2);
    }

    private Coordinate getTopRight() {
        return new Coordinate(column + 1, row + 1);
    }

    private Coordinate getBottomRight() {
        return new Coordinate(column + 1, row - 1);
    }

    private Coordinate getBottom() {
        return new Coordinate(column, row - 2);
    }

    private Coordinate getBottomLeft() {
        return new Coordinate(column - 1, row - 1);
    }

    private Coordinate getTopLeft() {
        return new Coordinate(column - 1, row + 1);
    }
}