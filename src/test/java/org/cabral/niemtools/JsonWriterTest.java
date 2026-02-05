package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public class JsonWriterTest {

    // --- Constructor test ---

    @Test
    public void testConstructor() {
        JsonWriter writer = new JsonWriter("/tmp/test");
        assertNotNull(writer);
    }

    @Test
    public void testConstructorWithNull() {
        JsonWriter writer = new JsonWriter(null);
        assertNotNull(writer);
    }

    // --- getJsonFilename tests ---

    @Test
    public void testGetJsonFilenameBasic() {
        String result = JsonWriter.getJsonFilename("model");
        assertEquals("model.schema.json", result);
    }

    @Test
    public void testGetJsonFilenameWithPath() {
        String result = JsonWriter.getJsonFilename("json/schema/model");
        assertEquals("json/schema/model.schema.json", result);
    }

    @Test
    public void testGetJsonFilenameEmpty() {
        String result = JsonWriter.getJsonFilename("");
        assertEquals(".schema.json", result);
    }

    // --- filterQuotes (private) tests via reflection ---

    @Test
    public void testFilterQuotesRemovesDoubleQuotes() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("filterQuotes", String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "He said \"hello\"");
            assertEquals("He said hello", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testFilterQuotesRemovesNewlines() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("filterQuotes", String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "line1\nline2\rline3");
            assertEquals("line1line2line3", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testFilterQuotesRemovesBackslashes() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("filterQuotes", String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "path\\to\\file");
            assertEquals("pathtofile", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testFilterQuotesEmptyString() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("filterQuotes", String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "");
            assertEquals("", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testFilterQuotesPlainString() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("filterQuotes", String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "A simple description");
            assertEquals("A simple description", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- convertMultiplicity (private) tests via reflection ---

    @Test
    public void testConvertMultiplicityBasic() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("convertMultiplicity", String.class);
            m.setAccessible(true);
            assertEquals("1,2", m.invoke(writer, "1..2"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testConvertMultiplicityUnbounded() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("convertMultiplicity", String.class);
            m.setAccessible(true);
            assertEquals("0,unbounded", m.invoke(writer, "0..*"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testConvertMultiplicitySingle() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("convertMultiplicity", String.class);
            m.setAccessible(true);
            assertEquals("1", m.invoke(writer, "1"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testConvertMultiplicityEmpty() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("convertMultiplicity", String.class);
            m.setAccessible(true);
            assertEquals("", m.invoke(writer, ""));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testConvertMultiplicityZeroToOne() {
        JsonWriter writer = new JsonWriter("/tmp");
        try {
            Method m = JsonWriter.class.getDeclaredMethod("convertMultiplicity", String.class);
            m.setAccessible(true);
            assertEquals("0,1", m.invoke(writer, "0..1"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
