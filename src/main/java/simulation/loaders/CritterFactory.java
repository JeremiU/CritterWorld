package simulation.loaders;

import simulation.Critter;
import simulation.World;

public class CritterFactory {

    private CritterFactory() {
    } // unused, private constructor.

    public static Critter fromFile(World currentWorld, String fileName) {
        CritterLoader loader = new CritterLoader(currentWorld, fileName);
        return loader.getCritter();

        //TODO: In addition to specifying a
        //critter file to load, the user should be able to specify the number of such critters to be added to the world.
        //These critters are placed at randomly chosen legal positions in the world: that is, not on top of a rock, food,
        //or another critter.
    }
}