package org.cabral.niemtools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

public class HtmlWriterTest {

    // --- Constructor test ---

    @Test
    public void testConstructor() {
        HtmlWriter writer = new HtmlWriter();
        assertNotNull(writer);
    }

    // --- getColumnHtml tests ---

    @Test
    public void testGetColumnHtml() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "value", "#fff", "#000", true);
        assertTrue(html.contains("value"));
        assertTrue(html.contains("#fff"));
        assertTrue(html.contains("#000"));
    }

    @Test
    public void testGetColumnHtmlWithWordWrap() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "test", "#fff", "#000", true);
        assertTrue(html.contains("word-wrap: break-word"));
    }

    @Test
    public void testGetColumnHtmlWithoutWordWrap() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "test", "#fff", "#000", false);
        assertFalse(html.contains("word-wrap: break-word"));
    }

    @Test
    public void testGetColumnHtmlContainsTdTags() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "cell value", "#ffffff", "#000000", false);
        assertTrue(html.startsWith("<td"));
        assertTrue(html.endsWith("</td>"));
    }

    @Test
    public void testGetColumnHtmlContainsFontTags() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "value", "#aaa", "#bbb", true);
        assertTrue(html.contains("<font color"));
        assertTrue(html.contains("</font>"));
    }

    @Test
    public void testGetColumnHtmlWithEmptyValue() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "", "#fff", "#000", true);
        assertNotNull(html);
        assertTrue(html.contains("<td"));
    }

    @Test
    public void testGetColumnHtmlWithHtmlEntitiesInValue() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "<b>bold</b>", "#fff", "#000", true);
        // The value should be passed through as-is
        assertTrue(html.contains("<b>bold</b>"));
    }

    @Test
    public void testGetColumnHtmlBgColorApplied() throws Exception {
        HtmlWriter writer = new HtmlWriter();
        Method m = HtmlWriter.class.getDeclaredMethod("getColumnHtml", String.class, String.class, String.class, Boolean.class);
        m.setAccessible(true);
        String html = (String) m.invoke(writer, "val", "#ff0000", "#00ff00", true);
        assertTrue(html.contains("bgcolor=\"#ff0000\""));
        assertTrue(html.contains("color = \"#00ff00\""));
    }
}
