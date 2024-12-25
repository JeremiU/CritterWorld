import org.junit.jupiter.api.Test;
import controller.ControllerImpl;
import simulation.Critter;
import simulation.Interpreter;
import simulation.World;
import simulation.loaders.CritterFactory;
import simulation.loaders.WorldFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InterpreterTests {

    private static final String[] critter_definitions = {"files/critter_loader_test_1.txt",
            "files/critter_loader_mate_test_1.txt",
            "files/critter_loader_mate_test_2.txt",};

    private static final String[] world_definitions = {"files/world_loader_test.txt"};

//    private World currentWorld;

    protected void testAll() {
        testBud();
        testMate();
        testSimulation();
    }

    @Test
    public void testBud() {

        System.out.println("Critter definiton file: "+critter_definitions[0]);



        //create a world and insert this Critter into the world at a random location
        World currentWorld = new World();
        Critter cr = CritterFactory.fromFile(currentWorld, critter_definitions[0]);

        currentWorld.insertCritterAtLocation(cr, 2, 2);
        System.out.println("Row: "+cr.getRow());
        System.out.println("Col: "+cr.getColumn());
        System.out.println(currentWorld);
        Interpreter ip = new Interpreter(cr);
        ip.run();
        System.out.println(currentWorld);
    }

    @Test
    public void testMate() {
        //create a world and insert this Critter into the world at a random location
        World currentWorld = new World();

        System.out.println("Critter definition file: "+critter_definitions[1]);
        Critter cr1 = CritterFactory.fromFile(currentWorld, critter_definitions[1]);

        System.out.println("Critter definition file: "+critter_definitions[2]);
        Critter cr2 = CritterFactory.fromFile(currentWorld, critter_definitions[2]);


        currentWorld.insertCritterAtLocation(cr1, 2, 4);
        currentWorld.insertCritterAtLocation(cr2, 3, 3);

        cr1.mate();
    }

    @Test
    public void testSimulation() {
        System.out.println("World definition file: "+world_definitions[0]);
        World w = WorldFactory.fromFile(world_definitions[0]);
        assertNotNull(w);
        //advance this world by one time steps
        w.step(1);
    }

}