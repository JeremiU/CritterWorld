package simulation.loaders;

import simulation.Critter;
import simulation.Hex;
import simulation.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldLoader extends AbstractLoader {

    private String worldName = "unnamed";
    private int width = 10, height = 10;
    private final List<Critter> critters = new ArrayList<>();
    private final List<Hex> rocks = new ArrayList<>();
    private final List<Hex> foods = new ArrayList<>();

    private String worldFileName;

    public WorldLoader(String fileName) {
        super(fileName);
        worldFileName = fileName;
    }

    @Override
    public void readLine(String line) {
        // Reading the file line by line
        line = line.trim();

        // Ignoring whitespace and comments
        if (line.length() == 0 || line.startsWith("//")) return;

        String[] words = line.trim().split(" ");

        // Storing the properties of the critter
        switch (words[0]) {
            case "name":
                worldName = words[1];
                break;
            case "size":
                width = Integer.parseInt(words[1]);
                height = Integer.parseInt(words[2]);
                break;
            case "rock":
                Hex rockHex = new Hex(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Hex.HexType.ROCK);
                rocks.add(rockHex);
                break;
            case "food":
                Hex foodHex = new Hex(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Hex.HexType.FOOD, Integer.parseInt(words[3]));
                foods.add(foodHex);
                break;
            case "critter":
                String parentPath = new File(worldFileName).getParentFile().getAbsolutePath() + "\\";
                Critter critter = CritterFactory.fromFile(null, parentPath + words[1]); //TODO: dangerous null
                critter.setLocation(Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                critter.setDirection(Integer.parseInt(words[4]));
                critters.add(critter);
                break;
            default:
                System.err.println("Invalid line: '" + line + "'\ndefault value assigned. (WorldLoader)");
        }
    }

    // Returns the created world
    public World getWorld() {
        World currentWorld = createWorld();
        for (int i = 0; i < critters.size(); i++) {
            critters.get(i).setWorld(currentWorld);
        }
        currentWorld.setCritters(critters);

        readFile();
        System.out.println(worldName);
        System.out.println(width);
        System.out.println(height);
        System.out.println(rocks);
        System.out.println(foods);
        System.out.println(critters);
        return createWorld();
    }

    public World createWorld() {
        return new World(worldName, width, height, critters, rocks, foods);
    }

    @Override
    public void afterRead() {

    }

    @Override
    public String toString() {
        return "WorldLoader{" + "worldName='" + worldName + '\'' + ", width=" + width +
                ", height=" + height + ", critters=" + critters + ", rocks=" + rocks +
                ", foods=" + foods + '}';
    }
}