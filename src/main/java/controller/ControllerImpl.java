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
        return true;
    }

    @Override
    public boolean loadCritters(String filename, int n) {
        if (n < 0) return false;
        if (getReadOnlyWorld() == null) return false;

        for (int i = 0; i < n; i++)
            currentWorld.insertCritter(CritterFactory.fromFile(currentWorld, filename));
        return true;
    }

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