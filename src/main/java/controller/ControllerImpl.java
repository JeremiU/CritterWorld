package controller;

import cms.util.maybe.Maybe;
import javafx.stage.FileChooser;
import model.ReadOnlyCritter;
import model.ReadOnlyWorld;
import simulation.Critter;
import simulation.World;
import simulation.loaders.CritterFactory;
import simulation.loaders.WorldFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * This class is responsible for managing the console inputs / outputs.
 */
public class ControllerImpl implements Controller {

    public World currentWorld;
    private Scanner scanner = new Scanner(System.in);

//    public static void main(String[] args) {
//        //get input from command line and pass to readLine
//
//        Scanner scanner = new Scanner(System.in);
//
//        boolean runAgain = true;
//        while (runAgain) {
//            String line = scanner.nextLine();
//            runAgain = readLine(line);
//        }
//    }
    /**
     * Processes a single console command provided by the user.
     *
     * @return whether we should continue handling commands.
     */
/*    public boolean readLine() {
        String line = scanner.nextLine();

        // Reading the file line by line
        line = line.trim();

        String[] words = line.trim().split(" ");

        //load src\test\resources\files\world_loader_test.txt
        //critters src\test\resources\files\critter_loader_test.txt 1

        switch (words[0]) {
            case "new":
                currentWorld = WorldFactory.createRandomWorld();
                return true;
            case "load":
                currentWorld = WorldFactory.fromFile(words[1]);
                return true;
            case "critters":
                if (currentWorld == null) {
                    System.err.println("Main world is not initialized.");
                    break;
                }
                Critter critter = CritterFactory.fromFile(currentWorld, words[1]);
                for (int i = 0; i < Integer.parseInt(words[2]); i++) {
                    currentWorld.insertCritter(critter);
                }
                return true;
            case "step":
                if (currentWorld == null) {
                    System.err.println("Main world is not initialized.");
                    break;
                }
                currentWorld.step(Integer.parseInt(words[1]));
                return true;
            case "info":
                if (currentWorld == null) {
                    System.err.println("Main world is not initialized.");
                    break;
                }
                worldInfo();
                return true;
            case "hex":
                if (currentWorld == null) {
                    System.err.println("Main world is not initialized.");
                    break;
                }
//                currentWorld.hexAt(Integer.parseInt(words[1]), Integer.parseInt(words[2])).printInfo();
                hexInfo(Integer.parseInt(words[1]), Integer.parseInt(words[2]));
                return true;
            default:
                System.err.println("Invalid command. Console exiting.");
                return false;

        }
        return true;
    }*/

    // Supplementary methods that are used for testing

    /** Prints current time step, number of critters, and world map of the simulation. */
    private void worldInfo() {
        currentWorld.print();
        System.out.println("Time-steps: " + currentWorld.getSteps());
        System.out.println("Live Critters: " + currentWorld.critterNumber());
    }

    /**
     * Prints description of the contents of hex (c,r).
     *
     * @param c column of hex
     * @param r row of hex
     */
    private void hexInfo(int c, int r) {
        currentWorld.hexAt(c, r).printInfo();
    }



    /**
     * @return the readonly world.
     */
    @Override
    public ReadOnlyWorld getReadOnlyWorld() {
        return currentWorld;
    }

    /**
     * Starts new random world simulation.
     */
    @Override
    public void newWorld() {
        currentWorld = WorldFactory.createRandomWorld();
    }

    /**
     * Starts new simulation with world specified in filename.
     *
     * @param filename             name of the world file.
     * @param enableManna          if enableManna is false, then the world should not drop any manna.
     *                             This is important for deterministic unit testing.
     * @param enableForcedMutation if enableForcedMutation is true, then a critter's program
     *                             will mutate every time it finishes its action.
     * @return whether the world is successfully loaded.
     */
    @Override
    public boolean loadWorld(String filename, boolean enableManna, boolean enableForcedMutation) {
        currentWorld = WorldFactory.fromFile(filename);
        //need to take care of "enableManna" and "enableForcedMutation"
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
        if (n < 0) {
            return false;
        }

        for (int i = 0; i < n; i++) {
            Critter cr = CritterFactory.fromFile(currentWorld, filename);
            currentWorld.insertCritter(cr);
        }

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
        if (n < 0) return false;

        currentWorld.step(n);
        return true;
    }

    @Override
    public void printWorld(PrintStream out) {
        out.println(currentWorld);
    }










}
