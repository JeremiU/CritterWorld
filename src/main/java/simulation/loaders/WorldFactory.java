package simulation.loaders;

import simulation.World;

/**
 * A factory that gives access to instances of Worlds.
 */
public class WorldFactory {

    /**
     * Starts a new simulation with a world populated by randomly placed rocks.
     */
    public static World createRandomWorld() {
        return new World();
    }

    /**
     * Loads a world from a given file
     */
    public static World fromFile(String fileName) {
        return new WorldLoader(fileName).createWorld();
    }
}