package console;

import gui.DisplayController;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {

    public static final int FLAG_CRITTER_ACTION = 0b1;
    public static final int FLAG_INTERPRETER = 0b10;
    public static final int FLAG_WORLD = 0b100;
    public static final int FLAG_CRITTER_LOADER = 0b1_000;
    public static final int FLAG_ABSTRACT_LOADER = 0b10_000;
    public static final int FLAG_DISPLAY_CONTROLLER = 0b100_000;
    public static final int FLAG_PARSER = 0b1_000_000;

    public static final int FLAG_ALL = 0b1_111_111;

    private static int flags = 0;

    public static void info(String message, String source, long flag) {
        if ((flags & flag) != 0)
            System.out.println(source + ": " + message);
    }

    public static void error(String message, String source, long flag) {
        if ((flags & flag) != 0)
            System.err.println(source + ": " + message);
        DisplayController.updateInfo(message, true);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> Platform.runLater(() -> DisplayController.updateInfo("Information Panel", false)), 5, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    public static void updateFlags(int flags) {
        Logger.flags = flags;
        System.out.println("Flags: " + flags);
    }
}