package controller;

import model.ReadOnlyWorld;
import simulation.World;
import simulation.loaders.CritterFactory;
import simulation.loaders.WorldFactory;

import java.io.PrintStream;

/**
 * This class is responsible for managing the console inputs / outputs.
 */
public class ControllerImpl implements Controller {

    public World currentWorld;

    @Override
    public ReadOnlyWorld getReadOnlyWorld() {
        return currentWorld;
    }

    @Override
    public void newWorld() {
        currentWorld = WorldFactory.createRandomWorld();
    }

    @Override
    public boolean loadWorld(String filename, boolean enableManna, boolean enableForcedMutation) {
        currentWorld = WorldFactory.fromFile(filename);
        //TODO need to take care of "enableManna" and "enableForcedMutation"
        return true;
    }

    /**
     * Loads critter definition from filename and randomly places n critters with that definition
     * into the world.
     *
     * @param filename name of the critter spec file.
     * @param n        number of critter to add.
     * @return whether all critters are successfully loaded.
     */
    @Override
    public boolean loadCritters(String filename, int n) {
        if (n < 0) return false;
        if (getReadOnlyWorld() == null) return false;

        for (int i = 0; i < n; i++)
            currentWorld.insertCritter(CritterFactory.fromFile(currentWorld, filename));
        return true;
    }

    /**
     * Advances the world by n time steps.
     *
     * @param n number of steps.
     * @return false if the world has not been initialized or n is negative, true otherwise.
     */
    @Override
    public boolean advanceTime(int n) {
        if (n < 0 || getReadOnlyWorld() == null) return false;
        currentWorld.step(n);
        return true;
    }

    @Override
    public void printWorld(PrintStream out) {
        out.println(currentWorld);
    }
}