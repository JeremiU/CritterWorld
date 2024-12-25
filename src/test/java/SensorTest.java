import model.Constants;
import org.junit.jupiter.api.Test;
import simulation.*;
import simulation.loaders.WorldFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SensorTest {

    @Test
    public void sensorTests() {

        World currentWorld = WorldFactory.createRandomWorld();

        // Creating reference critter
        Coordinate pos1 = new Coordinate(3, 3);
        Critter cr1 = createSampleCritter(currentWorld);
        currentWorld.setHex(pos1, new Hex(pos1, cr1));

        // Creating critter above it ( a friend )
        Coordinate pos2 = new Coordinate(3, 5);
        Critter cr2 = createSampleCritter(currentWorld);
        currentWorld.setHex(pos2, new Hex(pos2, cr2));

        // Creating a rock next to the reference critter
        Coordinate pos3 = new Coordinate(4, 4);
        Hex h3 = new Hex(4, 4, Hex.HexType.ROCK);
        currentWorld.setHex(pos3, h3);

        // Creating an empty hex next to the reference critter
        Coordinate pos4 = new Coordinate(4, 2);
        Hex h4 = new Hex(4, 2, Hex.HexType.EMPTY);
        currentWorld.setHex(pos4, h4);

        // Creating a food hex next to the reference critter
        Coordinate pos5 = new Coordinate(3, 1);
        Hex h5 = new Hex(3, 1, Hex.HexType.FOOD, 10);
        currentWorld.setHex(pos5, h5);

        // Critter response
        assertTrue(cr1.nearby(0) > 0);

        // Rock response
        assertEquals(-1, cr1.nearby(1));

        // Empty Hex response
        assertEquals(0, cr1.nearby(2));

        // Food Hex response
        assertTrue(cr1.nearby(3) < 0);

        // Testing "AHEAD"
        // Creating critter at (0, 0) ( a friend )
        Coordinate pos6 = new Coordinate(0, 0);
        Critter cr6 = createSampleCritter(currentWorld);
        cr1.setDirection(4);
        currentWorld.setHex(pos6, new Hex(pos6, cr6));

        currentWorld.print();


        // Testing for AHEAD critter detection
        System.out.println(cr1.ahead(3));
        System.out.println(currentWorld);
        assertTrue(cr1.ahead(-2) > 0);

        Coordinate pos7 = new Coordinate(0, 6);
        Hex h7 = new Hex(0, 6, Hex.HexType.FOOD, 10);
        cr1.setDirection(5);
        currentWorld.setHex(pos7, h7);

        // Testing for AHEAD food detection
        assertTrue(cr1.ahead(3) < 0);
        currentWorld.print();
    }

    private Critter createSampleCritter(World currentWorld) {
        return new Critter( currentWorld,
                "test1", Constants.MIN_MEMORY, 2, 3,
                4, 5, 6, null);
    }
}