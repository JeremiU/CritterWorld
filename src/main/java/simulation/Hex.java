package simulation;

import gui.Hexagon;
import javafx.scene.paint.Color;
import main.Util;

import java.util.Arrays;

import static model.Constants.DirectionConstants.DIR_AMOUNT;
import static model.Constants.ROCK_VALUE;

/**
 * Represents a Hex tile in the world.
 */
public class Hex {

    private final Coordinate coordinate;
    private HexType type;
    private Critter critter;

    private int foodValue = 0;

    public Hex(int column, int row, HexType type) {
        this.coordinate = new Coordinate(column, row);
        this.type = type;
    }

    public Hex(Coordinate coordinate, Critter critter) {
        this(coordinate.column(), coordinate.row(), HexType.CRITTER);
        this.critter = critter;
        this.critter.setLocation(coordinate);
    }

    public Hex(int column, int row, HexType type, int foodValue) {
        this(column, row, type);
        this.foodValue = foodValue;
    }

    // CRITTER Operations
    public void tryTickCritter() {
        if (critter != null) this.critter.tick();
    }

    public void setCritter(Critter critter) {
        this.critter = critter;
        type = HexType.CRITTER;
    }

    public Critter getCritter() {
        return this.critter;
    }

    // Are the coordinates given valid under the hexagonal system?
    public static boolean isValidHexCoordinate(int column, int row) {
        return (column + row) % 2 == 0;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getColumn() {
        return coordinate.column();
    }

    public int getRow() {
        return coordinate.row();
    }

    public HexType getType() {
        return type;
    }

    public void setType(HexType type) {
        this.type = type;
    }

    public void becomeFood() { //on Critter death
        type = HexType.FOOD;
        critter = null;
    }

    public int getFoodValue() {
        return foodValue;
    }

    public void setFoodValue(int foodValue) {
        this.foodValue = foodValue;
        if (foodValue == 0) type = HexType.EMPTY;
    }

    public enum HexType {EMPTY, ROCK, FOOD, CRITTER, INVALID}

    /**
     * Prints information about the hex to the console
     */
    public String printInfo() {
        return switch (type) {
            case INVALID ->
                "This hex should not exist";
            case EMPTY ->
                "This is an empty hex";
            case FOOD ->
                foodValue + " Food present.";
            case ROCK ->
                "This hex has a big, heavy rock in it.";
            case CRITTER ->
                "CRITTER FOUND!\nSPECIES: " + critter.getSpecies() + "\nMEM: " +
                        Arrays.toString(critter.getMemory()) + "\nRULESET: \n" + critter.getProgram() + "\nLAST RULE RUN: \n" + critter.getLastRuleString();
                //TODO: print last rule ran! Interpreter must be complete for this.
        };
    }

    public int evaluate(Critter observer) {
        return switch (this.getType()) {
            case CRITTER -> critter.getSize() * 1000 + critter.getPosture() * 10 + dirInRelation(observer);
            case FOOD -> -this.getFoodValue() - 1;
            case ROCK -> ROCK_VALUE;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return switch (type) {
            case EMPTY -> "-";
            case ROCK -> "#";
            case FOOD -> "F";
            case CRITTER -> "C";
            case INVALID -> " ";
        };
    }

    private int dirInRelation(Critter observer) {
        return Util.properMod(DIR_AMOUNT + critter.getDirection() - observer.getDirection(), DIR_AMOUNT);
    }

    public Hexagon getHexagon() {
        return switch (type) {
            case EMPTY:
                yield new Hexagon(30, Color.DARKGREEN, this);
            case ROCK:
                yield new Hexagon(30, Color.DARKGRAY, this);
            case FOOD:
                yield new Hexagon(30, Color.DARKMAGENTA, this);
            case CRITTER: {
                //TODO CUSTOM CRITTER COLOR
                Hexagon hexagon = new Hexagon(30, Color.GOLDENROD, this);
                hexagon.giveArrow();
                yield hexagon;
            }
            case INVALID:
                yield new Hexagon(30, Color.PURPLE, this);
        };
    }
}