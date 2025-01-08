package simulation.loaders;

import ast.ProgramImpl;
import console.Logger;
import exceptions.SyntaxError;
import org.eclipse.jetty.util.log.Log;
import parse.Parser;
import parse.ParserFactory;
import simulation.Critter;
import simulation.World;

import java.io.*;

import static model.Constants.*;

public class CritterLoader extends AbstractLoader {
    private final World currentWorld;

    // Default value from species name is "unnamed"
    private String species = "unnamed";
    private int memsize = MIN_MEMORY, defense = 1, offense = 1, size = INITIAL_SIZE, energy = INITIAL_ENERGY, posture = INITIAL_POSTURE;

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

    @Override
    public void readLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//")) return;
        if (line.contains("//")) line = line.substring(0, line.indexOf("//"));
        String[] words = line.trim().split(" ");

        if (programStarted) {
            streamBuffer.append(line).append("\n");
        } else {
            switch (words[0].toLowerCase()) {
                case "species:" -> species = words[1];
                case "memsize:" -> memsize = parseOrDefault(words[1], MIN_MEMORY, line);
                case "defense:" -> defense = parseOrDefault(words[1], 1, line);
                case "offense:" -> offense = Integer.parseInt(words[1]);
                case "size:" -> size = parseOrDefault(words[1], INITIAL_SIZE, line);
                case "energy:" -> energy = parseOrDefault(words[1], INITIAL_ENERGY, line);
                case "posture:" -> {
                    posture = parseOrDefault(words[1], INITIAL_POSTURE, line);
                    programStarted = true;
                }
                default -> programStarted = true;
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
        } catch (SyntaxError e) {
            Logger.error("Syntax Error " + e.getMessage(), "CritterLoader:afterRead", Logger.FLAG_CRITTER_LOADER);
        }
    }

    public Critter createCritter() {
        Logger.info("New critter loaded " + this.species, "CritterLoader:createCritter", Logger.FLAG_CRITTER_LOADER);
        return new Critter(currentWorld, species, memsize, defense, offense, size, energy, posture, program);
    }

    public Critter getCritter() {
        readFile();
        return createCritter();
    }

    private int parseOrDefault(String toParse, int defaultValue, String line) {
        int i = defaultValue;
        boolean defVal = false;
        try {
            i = Integer.parseInt(toParse);
            if (i < 0) defVal = true;
        } catch (NumberFormatException e) {
            defVal = true;
        }
        if (defVal)
            Logger.error("Invalid line: '" + line + "'\n" + "default value assigned. (CritterLoader)"
                    , "CritterLoader:parseOrDefault", Logger.FLAG_CRITTER_LOADER);
        return defVal ? defaultValue : i;
    }
}