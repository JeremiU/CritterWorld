package simulation;

import cms.util.maybe.Maybe;
import model.ReadOnlyCritter;
import model.ReadOnlyWorld;
import simulation.loaders.WorldLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static model.Constants.*;

/**
 * Represents the game world.
 */
public class World implements ReadOnlyWorld {

    private final Hex[][] hexes; // [row][column]
    private final int width;
    private final int height;

    // number of timesteps elapsed
    private int steps = 0;
    private int critterNumber = 0;

    // Constructor used in WorldLoader
    public World(String worldName, int width, int height, List<Critter> critters, List<Hex> rocks, List<Hex> foods) {
        // is it [height][width]?
        this.width = width;
        this.height = height;
        hexes = new Hex[height][width];
        critterNumber = critters.size();

        // Initially filling the world with EMPTY hexes
        for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[i].length; j++) {
                hexes[i][j] = new Hex(i, j, Hex.HexType.EMPTY);
            }
        }

        // Populating the World
        for (Hex rock : rocks) {
            hexes[rock.getRow()][rock.getColumn()] = rock;
        }

        for (Hex food : foods) {
            hexes[food.getRow()][food.getColumn()] = food;
        }

        for (Critter critter : critters) {
            hexes[critter.getRow()][critter.getColumn()].setCritter(critter);
        }
    }

    // Constructor without Critters (used in WorldLoader)
    public World(String worldName, int width, int height, List<Hex> rocks, List<Hex> foods) {
        // is it [height][width]?
        this.width = width;
        this.height = height;
        hexes = new Hex[height][width];
//        critterNumber = critters.size();

        // Initially filling the world with EMPTY hexes
        for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[i].length; j++) {
                hexes[i][j] = new Hex(i, j, Hex.HexType.EMPTY);
            }
        }

        // Populating the World
        for (Hex rock : rocks) {
            hexes[rock.getRow()][rock.getColumn()] = rock;
        }

        for (Hex food : foods) {
            hexes[food.getRow()][food.getColumn()] = food;
        }

//        for (Critter critter : critters) {
//            hexes[critter.getRow()][critter.getColumn()].setCritter(critter);
//        }
    }

    // Constructor used in Controller (creates an empty world with random rocks)
    public World() {
        // default values
        width = WIDTH;
        height = HEIGHT;
        hexes = new Hex[height][width];

        // Initially filling the world with EMPTY hexes
        for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[i].length; j++) {
                if (Hex.isValidHexCoordinate(i, j))
                    hexes[i][j] = new Hex(i, j, Hex.HexType.EMPTY);
                else
                    hexes[i][j] = new Hex(i, j, Hex.HexType.INVALID);

            }
        }

        // Placing rocks
        for (int i = 0; i < 5; i++) {
            // Generating random coordinates for the rocks to be placed at
            Coordinate coordinate = generateValidCoordinate();
            int rockColumn = coordinate.getColumn();
            int rockRow = coordinate.getRow();

            System.out.println("Generated Coordinate: " + coordinate);


            // Ensuring two Rocks aren't placed on top of each other (generating coordinates again)
            while (hexes[rockRow][rockColumn].getType() == Hex.HexType.ROCK) {
                coordinate = generateValidCoordinate();
                rockColumn = coordinate.getColumn();
                rockRow = coordinate.getRow();
            }

            hexes[rockRow][rockColumn] = new Hex(rockColumn, rockRow, Hex.HexType.ROCK);
        }
    }

    // Inserts a new critter into the world (with RANDOM POSITION)
    public void insertCritter(Critter critter) {

        // Generating random coordinates for the Critters to be placed at
        Coordinate coordinate = generateValidCoordinate();
        int critterColumn = coordinate.getColumn();
        int critterRow = coordinate.getRow();

        // Ensuring two Critters aren't placed on top of each other (generating coordinates again)
        while (hexes[critterRow][critterColumn].getType() == Hex.HexType.CRITTER) {
            coordinate = generateValidCoordinate();
            critterColumn = coordinate.getColumn();
            critterRow = coordinate.getRow();
        }

        //set the random location in the critter
        critter.setLocation(critterColumn, critterRow);
        hexes[critterRow][critterColumn].setCritter(critter);
    }

    // Inserts a new critter into the world at the specified
    //location.
    //if the critter is successfully inserted returns true, false
    //otherwise
    public boolean insertCritterAtLocation(Critter critter, int col, int row) {

        //if there is a critter already at the specified location then
        //return false
        if (hexes[row][col].getType() == Hex.HexType.CRITTER) {
            return false;
        }

        critter.setLocation(col, row);
        hexes[row][col].setCritter(critter);

        return true;
    }

    // Sets any Hex in "hexes" as a user-defined value
    public void setHex(Coordinate coordinate, Hex hex) {
        hexes[coordinate.getRow()][coordinate.getColumn()] = hex;
    }

    public void setHex(int column, int row, Hex hex) {
        hexes[row][column] = hex;
    }

    // Returns the hex at a given location
    public Hex hexAt(int column, int row) {
        return hexes[row][column];
    }

    public Hex hexAt(Coordinate coordinate) {

        return hexes[coordinate.getRow()][coordinate.getColumn()];
    }


    // Advances the world for n time steps
    public void step(int n) {
        for (int k = 0; k < n; k++) {
            steps++;
            for (Hex[] hex : hexes) {
                for (Hex value : hex) {
                    // covers all hexes
                    value.tryTickCritter();
                }
            }
        }
    }

    // Returns the number of timesteps elapsed
    public int getSteps() {
        return steps;
    }

    // Returns number of critters
    public int critterNumber() {
        return critterNumber;
    }

    // Adds critters to the world (used in constructor without critters)
    public void setCritters(List<Critter> critters) {
        critterNumber = critters.size();
        for (Critter critter : critters) {
            hexes[critter.getRow()][critter.getColumn()].setCritter(critter);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = hexes.length - 1; i >= 0; i--) {

            // Creating the hex alignment in the console

            for (int j = 0; j < hexes[i].length; j++) {
                builder.append(" ");

                //TODO: create the toString logic (printing out the world in the console)
//                builder.append(hexes[i][j].toString());
                if (Hex.isValidHexCoordinate(i, j))
//                    builder.append("(" + hexes[i][j].getCoordinate().row + "," + hexes[i][j].getCoordinate().column + ")");
                    builder.append(hexes[i][j].toString());

                else
                    builder.append(" ");
//                builder.append("(" + j + "," + i + ")");

                // Adds separating spaces in the console
                builder.append(" ");
            }

            builder.append("\n");
        }
        return builder.toString();
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public int getNumberOfAliveCritters() {
        return 0;
    }

    /**
     * @param c column id.
     * @param r row id.
     * @return the critter at the specified hex.
     */
    @Override
    public Maybe<ReadOnlyCritter> getReadOnlyCritter(int c, int r) {
        Hex h = this.hexAt(c, r);
        if (h != null && h.getType() == Hex.HexType.CRITTER) {
            return Maybe.from(h.getCritter());
        }
        return Maybe.none();
    }

    /**
     * @param c column id.
     * @param r row id.
     * @return 0 if the cell is empty. -1 if it is rock, -(X+1) if it is X food, X+1 if it contains a critter facing
     * in direction X. Treat out-of-bound or invalid hex as rock.
     */
    @Override
    public int getTerrainInfo(int c, int r) {
        //check if this is a valid hex coordinate
        //if not valid return -1
        if (!Hex.isValidHexCoordinate(c, r)) {
            return -1;
        }
        Hex h = this.hexAt(c, r);
        int result = 0;
        switch (h.getType()) {
            case EMPTY:
                break;
            case CRITTER:
                Critter cr = h.getCritter();
                result = cr.getDirection() + 1;
                break;
            case ROCK:
            case INVALID:
                result = -1;
                break;
            case FOOD:
                result = -(h.getFoodValue() + 1);
                break;
        }

        return result;
    }

    // Supplementary methods for generating valid odd / even coordinate values
    public static int getRandomOddNumber(int min, int max) {
        max--;
        if (max % 2 == 0) --max;
        if (min % 2 == 0) ++min;
        return min + 2 * (int) (Math.random() * ((max - min) / 2 + 1));
    }

    public static int getRandomEvenNumber(int min, int max) {
        Random rand = new Random();
        return min + rand.nextInt((max+1 - min) / 2) * 2;
    }

    private Coordinate generateValidCoordinate() {
        if (new Random().nextBoolean()) {
            return new Coordinate(getRandomOddNumber(0, width), getRandomOddNumber(0, height));
        } else {
            return new Coordinate(getRandomEvenNumber(0, width), getRandomEvenNumber(0, height));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Hex[][] getAllHexes() {
        return hexes;
    }

/*    public static void main(String[] args) {

        System.out.println(new File("").getAbsolutePath());
        WorldLoader loader = new WorldLoader("files/world_loader_test.txt");
        World world = loader.getWorld();
        System.out.println(world);
        world.step(1);
        System.out.println(world);
    }*/
}