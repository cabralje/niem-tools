package org.cabral.niemtools;

import java.util.HashMap;

import fr.bouml.UmlCom;

public class Log {

    // Debugging options
    //private static final Boolean DEBUG = true;
    private static final Boolean DEBUG = false;

    //private static final Boolean PROFILE = true;
    private static final Boolean PROFILE = false;

    private static final HashMap<String, Long> timer = new HashMap<>();

    /**
     * outputs debugging information
     */
    /**
     * @param output
     */
    static void debug(String output) {
        if (DEBUG) {
            Log.trace(output);
        }
    }

    /**
     * @param s
     */
    public static void trace(String s) {
        UmlCom.trace(s);
    }

    /**
     * @param s
     */
    public static void start(String s) {
        if (PROFILE) {
            trace("ELAPSED TIME (" + s + "): starting timer");
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
}
