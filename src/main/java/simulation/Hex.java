package simulation;

import gui.Hexagon;
import javafx.scene.paint.Color;
import main.Util;

import java.util.Arrays;

import static model.Constants.DirectionConstants.DIR_AMOUNT;
import static model.Constants.ROCK_VALUE;
import static simulation.Hex.HexType.CRITTER;

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
        this(coordinate.column(), coordinate.row(), CRITTER);
        this.critter = critter;
        this.critter.setLocation(coordinate);
    }

    public Hex(int column, int row, HexType type, int foodValue) {
        this(column, row, type);
        this.foodValue = foodValue;
    }

    public void tryTickCritter() {
        if (critter != null) this.critter.tick();
    }

    public void setCritter(Critter critter) {
        this.critter = critter;
        type = CRITTER;
    }

    public void setCritter(Critter critter, Color critterColor) {
        this.critter = critter;
        critter.setColor(critterColor);
        type = CRITTER;
    }

    public Critter getCritter() {
        return this.critter;
    }

    public static boolean isValidHexCoordinate(int column, int row) {
        return (column + row) % 2 == 0 && column >= 0 && row >= 0;
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
        if (type != CRITTER) this.critter = null;
    }

    public void becomeFood() { //on Critter death
        this.type = HexType.FOOD;
        critter = null;
    }

    public int getFoodValue() {
        return foodValue;
    }

    public void setFoodValue(int foodValue) {
        this.foodValue = foodValue;
        if (foodValue == 0) type = HexType.EMPTY;
        if (foodValue > 0) type = HexType.FOOD;
    }

    public enum HexType {EMPTY, ROCK, FOOD, CRITTER, INVALID}

    /**
     * Prints information about the hex to the console
     */
    public String printInfo() {
        return getHexagon().getHex().getCoordinate() + ": " + switch (type) {
            case INVALID -> "this hex should not exist";
            case EMPTY -> "empty hex";
            case FOOD -> foodValue + " Food present";
            case ROCK -> "a big, heavy rock";
            case CRITTER ->
                "CRITTER FOUND!\nSPECIES: " + critter.getSpecies() + "\nMEM: " +
                        Arrays.toString(critter.getMemory()) + "\nRULESET: \n" + critter.getProgram() + "\nLAST RULE RUN: \n" + critter.getLastRuleString();
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
        var v = new Hexagon(30, getColor(), this);
        v.setArrow(type == CRITTER);
        return v;
    }

    public Color getColor() {
        return switch (type) {
            case EMPTY -> Color.WHITE;
            case ROCK -> Color.DARKGRAY;
            case FOOD -> Color.DARKMAGENTA;
            case CRITTER -> this.critter.getColor();
            case INVALID -> Color.RED;
        };
    }
}