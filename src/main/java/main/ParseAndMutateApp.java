package main;

import ast.Program;
import exceptions.SyntaxError;
import parse.ParserImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * Console app for parsing & mutating a given program
 * <p>
 * Syntax: (file) OR
 * --mutate [mutation count] (file)
 */
public class ParseAndMutateApp {

    public static void main(String[] args) {
//        args = new String[]{"--mutate", "100", "/src/test/resources/files/mutated_critter_1.txt"
//        };

        int n = 0;
        String file;
        try {
            if (args.length == 1) {
                file = args[0];
            } else if (args.length == 3 && args[0].equals("--mutate")) {
                n = Integer.parseInt(args[1]);
                if (n < 0) throw new IllegalArgumentException();
                file = args[2];
            } else {
                throw new IllegalArgumentException();
            }

            String currentDirectory = Paths.get("").toAbsolutePath().toString();
            System.out.println("Current directory: " + currentDirectory);

            ParserImpl parser = new ParserImpl();
            Program program = parser.parse(new FileReader(currentDirectory + file));

            System.out.println("-".repeat(80) + "\n");
            System.out.println(" ".repeat(28) + "Printing initial program" + " ".repeat(28) + "\n");
            System.out.println("-".repeat(80) + "\n");
            System.out.println(program);
            System.out.println("-".repeat(80));

            for (int i = 1; i < n + 1; i++) {
                System.out.println("Applying mutation " + i + "!");
                System.out.println("-".repeat(80));
                program.mutate();
                System.out.println("-".repeat(80));
                System.out.println("Mutation applied:");
                System.out.println("\tType: ");
                System.out.println("\tEtc: ");
            }
            System.out.println();

            boolean lenEven = String.valueOf(n).length() % 2 == 0;

            String exec = lenEven ? "Finished running program" : "Finished executing program";
            String execPadding = " ".repeat((80 - exec.length()) / 2);

            String mutCount = lenEven ? "Ran (" + n + ") mutations" : "Executed " + n + " mutation(s)";

            String mutPadding = " ".repeat((80 - mutCount.length()) / 2);

            System.out.println(execPadding + exec + execPadding);
            System.out.println(mutPadding + mutCount + mutPadding);
            System.out.println();
            System.out.println("-".repeat(80) + "\n");

        } catch (IllegalArgumentException e) {
            System.out.println("Usage:\n  <input_file>\n  --mutate <n> <input_file>");
        } catch (SyntaxError e) {
            System.out.println("ERROR: MISFORMULATED PROGRAM");
            System.out.println("\t" + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: FILE NOT FOUND");
            System.out.println("Usage:\n  <input_file>\n  --mutate <n> <input_file>");
        }
    }
}