package simulation;

import cms.util.maybe.Maybe;
import console.Logger;
import gui.DisplayController;
import gui.Grid;
import javafx.scene.paint.Color;
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

    private int steps = 0, stepVal, critterNumber = 0;

    private final String worldName;

    private boolean enableManna = true, enableForcedMutation;

    private Grid gridAssociated;
    private final List<Color> critterColorspace;

    private int colorIndex;

    private DisplayController displayController;

    private ArrayList<Coordinate> insertedCordinates = new ArrayList<>();

    public World(String worldName, int width, int height, List<Critter> critters, List<Hex> rocks, List<Hex> foods) {
        this.worldName = worldName;
        this.width = width+4;
        this.height = height+2;
        hexes = new Hex[this.height][this.width];
        critterColorspace = generateColors( (int) (width * height * 0.02));
        stepVal = Util.randomInt(Integer.MAX_VALUE);

        for (int i = 0; i < hexes.length; i++)
            for (int j = 0; j < hexes[i].length; j++)
                hexes[i][j] = new Hex(j, i, Hex.HexType.EMPTY);

        for (Hex rock : rocks) hexes[rock.getRow()][rock.getColumn()] = rock;
        for (Hex food : foods) hexes[food.getRow()][food.getColumn()] = food;
        for (Critter critter : critters) hexes[critter.getRow()][critter.getColumn()].setCritter(critter, getNewCritterColor());

        for (Hex h : hexes[0]) h.setType(Hex.HexType.ROCK);
        for (Hex h : hexes[1]) h.setType(Hex.HexType.ROCK);

        for (Hex[] h : hexes) {
            h[0].setType(Hex.HexType.ROCK);
            h[h.length-1].setType(Hex.HexType.ROCK);
        }

        for (Hex h : hexes[hexes.length-2]) h.setType(Hex.HexType.ROCK);
        for (Hex h : hexes[hexes.length-3]) h.setType(Hex.HexType.ROCK);
    }

    public World(String worldName, int width, int height, List<Hex> rocks, List<Hex> foods) {
        this(worldName, width, height, new ArrayList<>(), rocks, foods);
    }

    public World() {
        this("a New World", WIDTH, HEIGHT, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

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

        while (hexAt(coordinate).getType() != Hex.HexType.EMPTY) coordinate = generateValidCoordinate();

        insertCritterAtLocation(critter, coordinate.column(), coordinate.row());
        insertedCordinates.add(coordinate);
    }

    public void insertCritterAtLocation(Critter critter, int col, int row) {
        if (hexes[row][col].getType() == Hex.HexType.CRITTER) return;
        critter.setLocation(col, row);
        Color c = getNewCritterColor();
        critter.setColor(c);
        hexes[row][col].setCritter(critter);
        critterNumber++;
    }

    public void setHex(Coordinate coordinate, Hex hex) {
        hexes[coordinate.row()][coordinate.column()] = hex;
    }

    public Hex hexAt(int column, int row) {
        return hexes[row][column];
    }

    public Hex hexAt(Coordinate coordinate) {
        if (coordinate.column() < 0 || coordinate.row() < 0 || coordinate.row() >= hexes.length || coordinate.column() >= hexes[coordinate.row()].length)
            return new Hex(-1,-1, Hex.HexType.INVALID);
        return hexes[coordinate.row()][coordinate.column()];
    }

    public void step(int n) {
        steps += n;
        for (int k = 0; k < n; k++) {
            stepVal = Util.diffRandNum(stepVal, Integer.MAX_VALUE);
            for (Hex[] hex : hexes)
                for (Hex value : hex) { //stepVal ensures that critter executes once per step
                    if (value.getCritter() != null && value.getCritter().getStepVal() != stepVal) {
                        value.getCritter().setStepVal(stepVal);
                        value.tryTickCritter();
                    }
                }
        }
    }

    public int getSteps() {
        return steps;
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

    public int getNumberOfAliveCritters() {
        return critterNumber;
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
        int hexCount = (int) Math.floor((MANNA_COUNT * getHeight() * getWidth()) / 1_000.0);
        while (hexCount > 0) {
            Coordinate c = generateValidCoordinate();
            Hex cHex = this.hexAt(c);
            while (cHex.getType() != Hex.HexType.FOOD && cHex.getType() != Hex.HexType.EMPTY) {
                c = generateValidCoordinate();
                cHex = this.hexAt(c);
            }
            cHex.setFoodValue(this.hexAt(c).getFoodValue() + MANNA_AMOUNT); //doesn't actually change?
            Logger.info("Set " + cHex.getFoodValue() + " food at " + c  + "!", "World:addFood", Logger.FLAG_WORLD);
            updateHex(c);
            hexCount--;
        }
    }

    public void updateHex(Coordinate coordinate) {
        getGrid().updateHexagon(coordinate);
    }

    public Grid getGrid() {
        return gridAssociated;
    }

    public void setGrid(Grid grid) {
        this.gridAssociated = grid;
    }

    protected void decrementCritterNumber() {
        this.critterNumber--;
        if (displayController != null) displayController.updateDashboard();
    }

    public Color getNewCritterColor() {
        colorIndex = Util.diffRandNum(colorIndex, critterColorspace.size());
        return critterColorspace.remove(colorIndex);
    }

    private List<Color> generateColors(int count) {
        List<Color> colors = new ArrayList<>();
        double hueStep = 360.0 / count; // Divide the hue spectrum
        double saturation = 0.7f;     // Fixed saturation
        double brightness = 0.8f;     // Fixed brightness

        for (int i = 0; i < count; i++) colors.add(Color.hsb(i * hueStep, saturation, brightness));
        return colors;
    }

    public void setDisplayController(DisplayController controller) {
        this.displayController = controller;
    }

    public void setEnableManna(boolean enableManna) {
        this.enableManna = enableManna;
    }

    public ArrayList<Coordinate> getInsertedCordinates() {
        return insertedCordinates;
    }

    public void clearInsertedCordinates() {
        this.insertedCordinates.clear();
    }
}