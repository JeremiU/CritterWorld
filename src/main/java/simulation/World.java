package simulation;

import cms.util.maybe.Maybe;
import main.Util;
import model.ReadOnlyCritter;
import model.ReadOnlyWorld;

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

    private int steps = 0;
    private int critterNumber;

    private final String worldName;

    private boolean enableManna = true, enableForcedMutation;

    public World(String worldName, int width, int height, List<Critter> critters, List<Hex> rocks, List<Hex> foods) {
        this.worldName = worldName;
        this.width = width;
        this.height = height;
        hexes = new Hex[height][width];
        critterNumber = critters.size();

        for (int i = 0; i < hexes.length; i++) {
            for (int j = 0; j < hexes[i].length; j++) {
                hexes[i][j] = new Hex(i, j, Hex.HexType.EMPTY);
            }
        }

        for (Hex rock : rocks) hexes[rock.getRow()][rock.getColumn()] = rock;
        for (Hex food : foods) hexes[food.getRow()][food.getColumn()] = food;
        for (Critter critter : critters) hexes[critter.getRow()][critter.getColumn()].setCritter(critter);
    }

    public World(String worldName, int width, int height, List<Hex> rocks, List<Hex> foods) {
        this(worldName, width, height, new ArrayList<>(), rocks, foods);
    }

    public World() {
        this("New World", WIDTH, HEIGHT, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        // Placing rocks
        for (int i = 0; i < 5; i++) {
            Coordinate coordinate = generateValidCoordinate();
            int rockColumn = coordinate.column();
            int rockRow = coordinate.row();

            while (hexes[rockRow][rockColumn].getType() == Hex.HexType.ROCK) {
                coordinate = generateValidCoordinate();
                rockColumn = coordinate.column();
                rockRow = coordinate.row();
            }

            hexes[rockRow][rockColumn] = new Hex(rockColumn, rockRow, Hex.HexType.ROCK);
        }
    }

    public void insertCritter(Critter critter) {
        Coordinate coordinate = generateValidCoordinate();

        while (hexAt(coordinate).getType() == Hex.HexType.EMPTY) {
            coordinate = generateValidCoordinate();
        }
        insertCritterAtLocation(critter, coordinate.column(), coordinate.row());
    }

    //precondition: critter, col, row = valid
    public void insertCritterAtLocation(Critter critter, int col, int row) {
        if (hexes[row][col].getType() == Hex.HexType.CRITTER) return;

        critter.setLocation(col, row);
        hexes[row][col].setCritter(critter);
    }

    public void setHex(Coordinate coordinate, Hex hex) {
        hexes[coordinate.row()][coordinate.column()] = hex;
    }

    public Hex hexAt(int column, int row) {
        return hexes[row][column];
    }

    public Hex hexAt(Coordinate coordinate) {
        if (coordinate.row() > hexes.length || coordinate.column() > hexes[coordinate.row()].length) return new Hex(-1,-1, Hex.HexType.INVALID);
        return hexes[coordinate.row()][coordinate.column()];
    }

    public void step(int n) {
        steps += n;
        for (int k = 0; k < n; k++)
            for (Hex[] hex : hexes)
                for (Hex value : hex) value.tryTickCritter();
    }

    public int getSteps() {
        return steps;
    }

    public int critterNumber() {
        return critterNumber;
    }

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
            for (int j = 0; j < hexes[i].length; j++) {
                builder.append(" ");

                if (Hex.isValidHexCoordinate(i, j)) builder.append(hexes[i][j].toString());
                else builder.append(" ");

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
        return 0; //TODO FIX
    }

    @Override
    public Maybe<ReadOnlyCritter> getReadOnlyCritter(int c, int r) {
        Hex h = this.hexAt(c, r);
        if (h != null && h.getType() == Hex.HexType.CRITTER) return Maybe.from(h.getCritter());
        return Maybe.none();
    }

    @Override
    public int getTerrainInfo(int c, int r) {
        return switch (this.hexAt(c, r).getType()) {
            case CRITTER:
                yield (this.hexAt(c, r).getCritter()).getDirection() + 1;
            case ROCK, INVALID:
                yield -1;
            case EMPTY:
                yield 0;
            case FOOD:
                yield -(this.hexAt(c, r).getFoodValue() + 1);
        };
    }

    public static int getRandomOddNumber(int min, int max) {
        if (min % 2 == 0) min++;  // Ensure min is odd
        if (max % 2 == 0) max--;  // Ensure max is odd

        return min + 2 * (int) (Math.random() * (((max - min) >> 1) + 1));
    }

    public static int getRandomEvenNumber(int min, int max) {
        Random rand = new Random();
        return min + rand.nextInt((max+1 - min) / 2) * 2;
    }

    private Coordinate generateValidCoordinate() {
        return new Random().nextBoolean()
          ? new Coordinate(getRandomOddNumber(0, width-1), getRandomOddNumber(0, height-1))
          : new Coordinate(getRandomEvenNumber(0, width-1), getRandomEvenNumber(0, height-1));
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

    public String getWorldName() {
        return worldName;
    }

    public void addFood() {
        if (!this.enableManna) return;
        if (Util.randomInt(this.getNumberOfAliveCritters()) != 0) return; // 1/n
        int hexCount = (int) Math.floor((MANNA_COUNT * getHeight() * getWidth()) / 1000.0);
        while (hexCount > 0) {
            Coordinate c = generateValidCoordinate();
            Hex cHex = this.hexAt(c);
            while (cHex.getType() != Hex.HexType.FOOD && cHex.getType() != Hex.HexType.EMPTY) {
                c = generateValidCoordinate();
                cHex = this.hexAt(c);
            }
            cHex.setFoodValue(this.hexAt(c).getFoodValue() + MANNA_AMOUNT);
            hexCount--;
        }
    }
}