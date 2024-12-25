package simulation.loaders;

import simulation.Critter;
import simulation.World;

public class CritterFactory {

    private CritterFactory() {
    } // unused, private constructor.

    public static Critter fromFile(World currentWorld, String fileName) {
        CritterLoader loader = new CritterLoader(currentWorld, fileName);
        return loader.getCritter();
    }
}