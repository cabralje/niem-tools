package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

public class CmfToolAdapterTest {

    // --- expandEnvVars tests (private, via reflection) ---

    private String invokeExpandEnvVars(String value) throws Exception {
        Method m = CmfToolAdapter.class.getDeclaredMethod("expandEnvVars", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, value);
    }

    @Test
    public void testExpandEnvVarsNull() throws Exception {
        assertNull(invokeExpandEnvVars(null));
    }

    @Test
    public void testExpandEnvVarsEmpty() throws Exception {
        assertEquals("", invokeExpandEnvVars(""));
    }

    @Test
    public void testExpandEnvVarsNoVars() throws Exception {
        assertEquals("plain text", invokeExpandEnvVars("plain text"));
    }

    @Test
    public void testExpandEnvVarsTildeHome() throws Exception {
        String home = System.getProperty("user.home");
        assertEquals(home, invokeExpandEnvVars("~"));
    }

    @Test
    public void testExpandEnvVarsTildeSlash() throws Exception {
        String home = System.getProperty("user.home");
        String result = invokeExpandEnvVars("~/path/to/file");
        assertEquals(home + "/path/to/file", result);
    }

    @Test
    public void testExpandEnvVarsTildeBackslash() throws Exception {
        String home = System.getProperty("user.home");
        String result = invokeExpandEnvVars("~\\path\\to\\file");
        assertEquals(home + "\\path\\to\\file", result);
    }

    @Test
    public void testExpandEnvVarsBracePattern() throws Exception {
        // HOME should be set in most environments
        String home = System.getenv("HOME");
        if (home != null) {
            String result = invokeExpandEnvVars("${HOME}/test");
            assertEquals(home + "/test", result);
        }
        // If HOME is not set, verify the pattern is preserved
        String result = invokeExpandEnvVars("${NONEXISTENT_VAR_12345}");
        assertEquals("${NONEXISTENT_VAR_12345}", result);
    }

    @Test
    public void testExpandEnvVarsPercentPattern() throws Exception {
        // Unknown env var should be preserved
        String result = invokeExpandEnvVars("%NONEXISTENT_VAR_12345%");
        assertEquals("%NONEXISTENT_VAR_12345%", result);
    }

    @Test
    public void testExpandEnvVarsTildeOnlyDoesNotExpandInMiddle() throws Exception {
        // Tilde in the middle should not be expanded
        String result = invokeExpandEnvVars("path/~/file");
        assertEquals("path/~/file", result);
    }

    // --- exec tests ---

    @Test
    public void testExecEchoCommand() {
        try {
            int exitCode = CmfToolAdapter.exec("echo hello");
            assertEquals(0, exitCode);
        } catch (IOException | InterruptedException e) {
            fail("exec echo should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testExecTrueCommand() {
        try {
            int exitCode = CmfToolAdapter.exec("true");
            assertEquals(0, exitCode);
        } catch (IOException | InterruptedException e) {
            fail("exec true should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testExecFalseCommand() {
        try {
            int exitCode = CmfToolAdapter.exec("false");
            assertEquals(1, exitCode);
        } catch (IOException | InterruptedException e) {
            fail("exec false should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testExecInvalidCommandThrows() {
        try {
            CmfToolAdapter.exec("nonexistent_command_xyz_12345");
            fail("Should throw IOException for nonexistent command");
        } catch (IOException e) {
            // Expected
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException");
        }
    }

    @Test
    public void testExecCommandWithQuotedArgs() {
        try {
            int exitCode = CmfToolAdapter.exec("echo \"hello world\"");
            assertEquals(0, exitCode);
        } catch (IOException | InterruptedException e) {
            fail("exec with quoted args should not throw: " + e.getMessage());
        }
    }
}
