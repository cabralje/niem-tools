package org.cabral.niemtools;

import java.util.concurrent.ConcurrentHashMap;

import fr.bouml.UmlCom;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class Log {

    // Debugging options
    private static Boolean DEBUG = false;
    private static Boolean PROFILE = false;

    private static final ConcurrentHashMap<String, Long> timer = new ConcurrentHashMap<>();
    private static TextArea logArea = null;
    private static Label importStatus = null;

    /**
     * outputs debugging information
     */
    /**
     * @param output
     */
    static void debug(String output) {
        if (DEBUG) {
            Log.trace(output);
            UmlCom.trace(output);
        }
    }

    /**
     * @param s
     */
    public static void trace(String s) {
        //UmlCom.trace(s);
        if (logArea != null) {
            // Ensure UI updates happen on JavaFX Application Thread
            Platform.runLater(() -> logArea.appendText(s + "\n"));
        }
    }

    /**
     * Set the importStatus Label for displaying import status messages
     * @param label The Label to display messages on
     */
    public static void setImportStatus(Label label) {
        importStatus = label;
    }

    /**
     * Set the LogArea TextArea for appending log messages
     * @param area The TextArea to append messages to
     */
    public static void setLogArea(TextArea area) {
        logArea = area;
    }

        /**
     * Set the LogArea TextArea for appending log messages
     * @param area The TextArea to append messages to
     */
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

        /**
     * Set the LogArea TextArea for appending log messages
     * @param area The TextArea to append messages to
     */
    public static void setProfile(boolean profile) {
        PROFILE = profile;
    }

    /**
     * @param s
     */
    public static void start(String s) {
        if (PROFILE) {
            //trace("ELAPSED TIME (" + s + "): starting timer");
            timer.put(s, System.nanoTime());
        }
    }

    /**
     * @param s
     */
    public static void stop(String s) {
        if (PROFILE) {
            if (!timer.containsKey(s)) {
                trace("ELAPSED TIME: (" + s + "): not set");
                return;
            }
            long startTime = timer.get(s);
            long stopTime = System.nanoTime();
            long elapsedTime = (stopTime - startTime) / 1000000000L;
            trace("ELAPSED TIME (" + s + "): " + elapsedTime + " sec");
            timer.remove(s);
        }
    }

    public static void setImportStatusText(String message) {
        if (importStatus != null) {
            Platform.runLater(() -> importStatus.setText(message));
        }
    }
}
