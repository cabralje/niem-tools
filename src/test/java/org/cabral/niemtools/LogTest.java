package org.cabral.niemtools;

import static org.junit.Assert.fail;
import org.junit.Test;

public class LogTest {
    @Test
    public void testStartAndStop() {
        try {
            Log.start("test");
            Log.stop("test");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testTrace() {
        try {
            Log.trace("trace message");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testDebug() {
        try {
            Log.debug("debug message");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testMultipleStartsAndStops() {
        try {
            Log.start("block1");
            Log.start("block2");
            Log.stop("block2");
            Log.stop("block1");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testTraceNull() {
        try {
            Log.trace(null);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testDebugNull() {
        try {
            Log.debug(null);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testStopWithoutStart() {
        try {
            Log.stop("neverStartedBlock");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testStartWithEmptyString() {
        try {
            Log.start("");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testStopWithEmptyString() {
        try {
            Log.stop("");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
