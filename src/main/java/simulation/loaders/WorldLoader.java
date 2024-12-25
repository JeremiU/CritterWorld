package simulation.loaders;

import simulation.Critter;
import simulation.Hex;
import simulation.World;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldLoader extends AbstractLoader {

    private String worldName = "unnamed";
    private int width = 10, height = 10;
    private final List<Critter> critters = new ArrayList<>();
    private final List<Hex> rocks = new ArrayList<>();
    private final List<Hex> foods = new ArrayList<>();
    private World world;
    private final String worldFileName;

    public WorldLoader(String fileName) {
        super(fileName);
        worldFileName = fileName;
    }

    @Override
    public void readLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//")) return;

        String[] words = line.trim().split(" ");

        switch (words[0].toLowerCase()) {
            case "name" ->
                worldName = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
            case "size" -> {
                width = Integer.parseInt(words[1]);
                height = Integer.parseInt(words[2]);
            }
            case "rock" ->
                rocks.add(new Hex(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Hex.HexType.ROCK));
            case "food" ->
                foods.add(new Hex(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Hex.HexType.FOOD, Integer.parseInt(words[3])));
            case "critter" -> {
                String parentPath = new File(worldFileName).getParentFile().getAbsolutePath();
                Critter critter = CritterFactory.fromFile(this.getWorld(), parentPath + File.separator + words[1]);
                critter.setLocation(Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                critter.setDirection(Integer.parseInt(words[4]));
                critters.add(critter);
            }
        }
    }

    public World getWorld() {
        return this.world;
    }

    public World createWorld() {
        this.world = new World(worldName, width, height, critters, rocks, foods);
        readFile();
        for (Critter critter : critters) critter.setWorld(this.world);
        this.world.setCritters(critters);
        return world;
    }

    @Override
    public void afterRead() {}

    @Override
    public String toString() {
        return "WorldLoader{" + "worldName='" + worldName + '\'' + ", width=" + width +
                ", height=" + height + ", critters=" + critters + ", rocks=" + rocks +
                ", foods=" + foods + '}';
    }
}