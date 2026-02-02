package org.cabral.niemtools;

import java.util.concurrent.ConcurrentHashMap;

import fr.bouml.UmlCom;

public class Log {

    // Debugging options
    private static Boolean DEBUG = false;
    private static Boolean PROFILE = false;

    private static final ConcurrentHashMap<String, Long> timer = new ConcurrentHashMap<>();
    private static Object logArea = null;
    private static Object messageStatus = null;

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
            // Ensure UI updates happen on JavaFX Application Thread using reflection
            try {
                Class<?> platformClass = Class.forName("javafx.application.Platform");
                var runLaterMethod = platformClass.getMethod("runLater", Runnable.class);
                runLaterMethod.invoke(null, (Runnable) () -> {
                    try {
                        var appendTextMethod = logArea.getClass().getMethod("appendText", String.class);
                        appendTextMethod.invoke(logArea, s + "\n");
                    } catch (Exception e) {
                        // Silently fail if reflection fails
                    }
                });
            } catch (Exception e) {
                // JavaFX not available, fail silently
            }
        }
    }

    /**
     * Set the importStatus Label for displaying import status messages
     * @param label The Label to display messages on
     */
    public static void setMessageStatusLabel(Object label) {
        messageStatus = label;
    }

    /**
     * Set the LogArea TextArea for appending log messages
     * @param area The TextArea to append messages to
     */
    public static void setLogArea(Object area) {
        logArea = area;
    }

        /**
     * Enable or disable debug logging.
     * @param debug true to enable debug logging, false to disable
     */
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

        /**
     * Enable or disable profiling timers.
     * @param profile true to enable profiling, false to disable
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

    public static void setMessageStatus(String message) {
        if (messageStatus != null) {
            try {
                Class<?> platformClass = Class.forName("javafx.application.Platform");
                var runLaterMethod = platformClass.getMethod("runLater", Runnable.class);
                runLaterMethod.invoke(null, (Runnable) () -> {
                    try {
                        var setTextMethod = messageStatus.getClass().getMethod("setText", String.class);
                        setTextMethod.invoke(messageStatus, message);
                    } catch (Exception e) {
                        // Silently fail if reflection fails
                    }
                });
            } catch (Exception e) {
                // JavaFX not available, fail silently
            }
        }
    }
}
