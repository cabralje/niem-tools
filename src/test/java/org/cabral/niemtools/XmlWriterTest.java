package org.cabral.niemtools;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class XmlWriterTest {
    @Test
    public void testXmlNs() {
        String ns = XmlWriter.xmlNs("prefix", "uri");
        assertTrue(ns.contains("prefix"));
        assertTrue(ns.contains("uri"));
    }

    @Test
    public void testXmlHeader() {
        assertNotNull(XmlWriter.XML_HEADER);
    }
    // Add more tests for XmlWriter's static methods if available
}
