package simulation.loaders;

import ast.ProgramImpl;
import exceptions.SyntaxError;
import parse.Parser;
import parse.ParserFactory;
import simulation.Critter;
import simulation.World;

import java.io.*;

public class CritterLoader extends AbstractLoader {

    // The world that the Critter will be loaded into
    private World currentWorld;

    // Default value from species name is "unnamed"
    private String species = "unnamed";

    // Default values for all mem[] elements is 1.
    private int memsize = 7, defense = 1, offense = 1, size = 1, energy = 1, posture = 1;

    // (For Program) True after "posture" is detected, meaning that the <program> has started
    private boolean programStarted = false;

    // (For Program) passed to the parser
    private final StringBuilder streamBuffer = new StringBuilder();

    // (For Program) the program object that is to be given to the generated Critter
    private ProgramImpl program;

    public CritterLoader(World currentWorld, String fileName) {
        super(fileName);
        this.currentWorld = currentWorld;
    }

    //TODO: When the world is loaded the critter file should be assumed to be in the same folder as the world
    //TODO: determine the absolute file path and pass that in as well.

    @Override
    public void readLine(String line) {
        // Reading the file line by line
        line = line.trim();

        // Ignoring whitespace and comments
        if (line.length() == 0 || line.startsWith("//")) return;
        if (line.contains("//")) line = line.substring(0, line.indexOf("//"));
        String[] words = line.trim().split(" ");

        if (programStarted) {
            streamBuffer.append(line).append("\n");
        } else {
            // Storing the properties of the critter
            switch (words[0]) {
                case "species:":
                    species = words[1];
                    break;
                case "memsize:":
                    memsize = Integer.parseInt(words[1]);
                    break;
                case "defense:":
                    defense = Integer.parseInt(words[1]);
                    break;
                case "offense:":
                    offense = Integer.parseInt(words[1]);
                    break;
                case "size:":
                    size = Integer.parseInt(words[1]);
                    break;
                case "energy:":
                    energy = Integer.parseInt(words[1]);
                    break;
                case "posture:":
                    posture = Integer.parseInt(words[1]);
                    programStarted = true;
                    break;
                default:
                    System.err.println("Invalid line: '" + line + "'\n" + "default value assigned. (CritterLoader)");
            }
        }
    }

    @Override
    public void afterRead() {
        InputStream programStream = new ByteArrayInputStream(streamBuffer.toString().getBytes());

        Reader r = new BufferedReader(new InputStreamReader(programStream));
        Parser parser = ParserFactory.getParser();
        try {
            program = (ProgramImpl) parser.parse(r);
            assert program != null; // does this work? do I need to enable assertions?
        } catch (SyntaxError e) {
            System.err.println("Error when parsing Critter from file: <invalid program>");
            System.out.println(e.getMessage());
        }
    }

    public Critter createCritter() {
        //        System.out.println("New critter loaded: " + critter.toString());
        return new Critter(currentWorld, species, memsize, defense, offense, size, energy, posture, program);
    }

    public Critter getCritter() {
        readFile();
        return createCritter();
    }
}