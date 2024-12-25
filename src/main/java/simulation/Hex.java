package simulation;

import gui.Hexagon;
import javafx.scene.canvas.GraphicsContext;
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

    // The hexagon class is responsible for the GUI display of the hexes
    private Hexagon hexagon = new Hexagon(30, Color.BLACK, this);

    // Constructor for ROCK, FOOD, EMPTY hexes
    public Hex(Coordinate coordinate, HexType type) {
        this.coordinate = coordinate;
        this.type = type;
    }

    // Constructor without type will be EMPTY
    public Hex(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.type = HexType.EMPTY;
    }

    // Constructor for CRITTER hexes
    public Hex(Coordinate coordinate, Critter critter) {
        this.critter = critter;
        this.critter.setLocation(coordinate);
        this.coordinate = coordinate;
        this.type = HexType.CRITTER;
    }

    // Constructor for width / height initialisation with ROCK
    public Hex(int column, int row, HexType type) {
        this.coordinate = new Coordinate(column, row);
        this.type = type;
    }

    // Constructor for width / height initialisation for FOOD
    public Hex(int column, int row, HexType type, int foodValue) {
        this.coordinate = new Coordinate(column, row);
        this.foodValue = foodValue;
        this.type = type;

        if (type == HexType.ROCK) //TODO fix
            throw new IllegalArgumentException("This constructor is only to be used for FOOD, no ROCKs allowed.");
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

    // Location getters
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getColumn() {
        return coordinate.getColumn();
    }

    public int getRow() {
        return coordinate.getRow();
    }

    // Getting the type of Hex
    public HexType getType() {
        return type;
    }

    public void setType(HexType type) {
        this.type = type;
    }

    // Hex turned into food when CRITTER dies
    public void becomeFood() {
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

    //TODO: hexTop, hexBottom, hexAt()....

    public enum HexType {EMPTY, ROCK, FOOD, CRITTER, INVALID}

    /**
     * Prints information about the hex to the console
     */
    public String printInfo() {
        String info = "";
        switch (type) {
            case INVALID:
                System.out.println("This hex should not exist");
                info = "This hex should not exist";
                break;
            case EMPTY:
                System.out.println("This is an empty hex");
                info = "This is an empty hex";
                break;
            case FOOD:
                System.out.println(foodValue + " Food present.");
                info = foodValue + " Food present.";
                break;
            case ROCK:
                System.out.println("This hex has a big, heavy rock in it.");
                info = "This hex has a big, heavy rock in it.";
                break;
            case CRITTER:
                System.out.println("CRITTER FOUND!");
                System.out.println("SPECIES: " + critter.getSpecies());
                System.out.println("MEM: " + Arrays.toString(critter.getMemory()));
                System.out.println("RULESET: \n" + critter.getProgram());
                info = "CRITTER FOUND!\nSPECIES: " + critter.getSpecies() + "\nMEM: " +
                        Arrays.toString(critter.getMemory()) + "\nRULESET: \n" + critter.getProgram();
                //TODO: print last rule executed! Interpreter must be complete for this.
        }
        return info;
    }

    public int evaluate(Critter observer) {
        switch (this.getType()) {
            case EMPTY:
                return 0;
            case CRITTER: //return critter posture
                return critter.getSize() * 1000 + critter.getPosture() * 10 + dirInRelation(observer);
            case FOOD:
                return -this.getFoodValue() - 1;
            case ROCK:
                return ROCK_VALUE;
        }
        return 0;
    }

    @Override
    public String toString() {
        switch (type) {
            case EMPTY:
                return "-";
            case ROCK:
                return "#";
            case FOOD:
                return "F";
            case CRITTER:
                return "C";
            case INVALID:
                return " ";
        }
        // This means the hex is broken
        return "X";
    }

    private int dirInRelation(Critter observer) {
        return Util.properMod(DIR_AMOUNT + critter.getDirection() - observer.getDirection(), DIR_AMOUNT);
    }

    // sets the hexagon (used in GUI attachments)
    public void setHexagon(Hexagon hexagon) {
        this.hexagon = hexagon;
    }

    // gets the GUI hexagon
    public Hexagon getHexagon() {
        switch (type) {
            case EMPTY:
                return new Hexagon(30, Color.DARKGREEN, this);
            case ROCK:
                return new Hexagon(30, Color.DARKGRAY, this);
            case FOOD:
                return new Hexagon(30, Color.DARKMAGENTA, this);
            case CRITTER:
                Hexagon hexagon = new Hexagon(30, Color.GOLDENROD, this);
                hexagon.giveArrow();
                return hexagon;

            case INVALID:
                return new Hexagon(30, Color.PURPLE, this);
            default:
                return new Hexagon(30, Color.RED, this);
        }
    }


        /**
         * hexagon without position
         * hexes[][] creates attachments with Hexagon (positions the hexagons) (via rows and columns)
         * draw() run this method.
         */
}