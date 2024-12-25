import ast.*;
import ast.Cmd;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;
import simulation.Coordinate;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains tests for the Critter parser.
 */
public class ParserTest {

    public static final String[] critter_programs = {"files/one-rule.txt", "files/one-rule-with-syntactic-sugar.txt", "files/example-critter-simple-1.txt", "files/example-critter-simple-2.txt", "files/one-rule-binaryoperation.txt", "files/draw_critter.txt", "files/mutated_critter_1.txt", "files/mutated_critter_2.txt", "files/mutated_critter_3.txt", "files/mutated_critter_4.txt", "files/mutated_critter_5.txt", "files/mutated_critter_6.txt", "files/unmutated_critter.txt", "files/example-rules.txt"};

    /**
     * Checks that a valid critter program is not {@code null} when parsed.
     */
    @Test
    public void testProgramIsNotNone() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[0]);
        InputStream is = new FileInputStream(critter_programs[0]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testSize() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[0]);
        InputStream is = new FileInputStream(critter_programs[0]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);
            System.out.println("Size: " + prog.size());
            assertEquals(10, prog.size());
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testNodeAt() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[0]);
        InputStream is = new FileInputStream(critter_programs[0]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);

            NodeCategory[] list = new NodeCategory[]{NodeCategory.PROGRAM, NodeCategory.RULE, NodeCategory.BINARY_CONDITION, NodeCategory.ACTION, NodeCategory.RELATION, NodeCategory.RELATION, NodeCategory.SENSOR, NodeCategory.NUMBER, NodeCategory.MEM, NodeCategory.NUMBER}; //correct program node category order

            for (int i = 0; i < 9; i++)
                assertEquals(list[i], prog.nodeAt(i).getCategory());
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks
     */
    @Test
    public void testPrintAST() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[13]);
        InputStream is = new FileInputStream(critter_programs[13]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        StringBuilder bldr = new StringBuilder();
        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);
            Queue<Node> parents = new LinkedList<>();
            parents.add(prog);
            while (!parents.isEmpty()) {
                //print this node and add the next level children
                Node parent = parents.remove();
                //go up the tree until the root to get the level
                int level = 0;
                Maybe<Node> mn = ((AbstractNode) parent).getParent();
                Node gp = null;
                try {
                    gp = mn.get();
                } catch (NoMaybeValue ignored) {
                }

                while (gp != null) {
                    level++;
                    mn = ((AbstractNode) gp).getParent();
                    gp = null;
                    try {
                        gp = mn.get();
                    } catch (NoMaybeValue ignored) {
                    }
                }
                //indent based on the level of the child node
                if (level > 0) {
                    bldr.append("--".repeat(level));
                    bldr.append(">");
                }

                bldr.append(parent.getCategory().toString()).append("\n");
                List<Node> children = parent.getChildren();
                if (children != null)
                    parents.addAll(children);
            }
            System.out.println(bldr);
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testPrettyPrint() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[13]);
        InputStream is = new FileInputStream(critter_programs[13]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        StringBuilder bldr = new StringBuilder();

        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);
            bldr = prog.prettyPrint(bldr);
            System.out.println(bldr.toString());
            assertTrue(bldr.toString().length() > 0);
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testPrettyPrintAll() throws FileNotFoundException {
        for (String critter_program : critter_programs) {
            InputStream is = new FileInputStream(critter_program);
            Reader r = new BufferedReader(new InputStreamReader(is));
            Parser parser = ParserFactory.getParser();
            StringBuilder bldr = new StringBuilder();

            try {
                Program prog = parser.parse(r);
                assertNotNull(prog);
                bldr = prog.prettyPrint(bldr);
                System.out.println(bldr.toString());
                assertTrue(bldr.toString().length() > 0);
            } catch (SyntaxError e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void testClone() throws FileNotFoundException {
        System.out.println("Critter program: " + critter_programs[0]);
        InputStream is = new FileInputStream(critter_programs[0]);
        Reader r = new BufferedReader(new InputStreamReader(is));
        Parser parser = ParserFactory.getParser();
        StringBuilder bldr = new StringBuilder();
        try {
            Program prog = parser.parse(r);
            assertNotNull(prog);
            //get a node of type Rule and clone it
            Maybe<Node> mn = prog.findNodeOfType(NodeCategory.RULE);
            assertTrue(mn.isPresent());
            try {
                Rule rul = (Rule) mn.get();
                bldr = rul.prettyPrint(bldr);
                String beforeClone = bldr.toString();
                //clone this rul
                Rule clone = (Rule) rul.clone();
                bldr = new StringBuilder();
                clone.prettyPrint(bldr);
                String afterClone = bldr.toString();
                assertEquals(beforeClone, afterClone);
            } catch (NoMaybeValue ex) {
                fail("testClone failed");
            }

            //get a node of type Command and clone it
            Maybe<Node> mcd = prog.findNodeOfType(NodeCategory.ACTION);
            assertTrue(mcd.isPresent());
            try {
                Cmd ac = (Cmd) mcd.get();
                bldr = new StringBuilder();
                bldr = ac.prettyPrint(bldr);
                String beforeClone = bldr.toString();
                //clone this ActionCommand
                Cmd clone = (Cmd) ac.clone();
                bldr = new StringBuilder();
                clone.prettyPrint(bldr);
                String afterClone = bldr.toString();
                assertEquals(beforeClone, afterClone);
            } catch (NoMaybeValue ex) {
                fail("testClone failed");
            }

        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
            fail("testClone failed");
        }
    }
    @Test
    public void testMisc() throws FileNotFoundException {
        int width = 10;
        int height = 7;
        for (int i = 0; i < 100; i++) {
//            System.out.println(generateValidCoordinate());
            System.out.println(getRandomEvenNumber(0, 7));
        }
    }
    private int getRandomOddNumber(int min, int max) {
        max--;
        if (max % 2 == 0) --max;
        if (min % 2 == 0) ++min;
        return min + 2 * (int) (Math.random() * ((max - min) / 2 + 1));
    }
    private int getRandomEvenNumber(int min, int max) {
        Random rand = new Random();
        return min + rand.nextInt((max+1 - min) / 2) * 2;
    }
    private Coordinate generateValidCoordinate() {
        Random random = new Random();
        if (random.nextBoolean()) {
            return new Coordinate(getRandomOddNumber(0, 10), getRandomOddNumber(0, 7));
        } else {
            return new Coordinate(getRandomEvenNumber(0, 10), getRandomEvenNumber(0, 7));
        }
    }

}