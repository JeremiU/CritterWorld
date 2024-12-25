package simulation.loaders;

import controller.ControllerImpl;
import simulation.World;

/**
 * A factory that gives access to instances of Worlds.
 */
public class WorldFactory {

    private WorldFactory() {
    } // unused, private constructor

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
        WorldLoader loader = new WorldLoader(fileName);
//        ControllerImpl.currentWorld = loader.getWorld();
//        return ControllerImpl.currentWorld;
        return loader.getWorld();
    }
}