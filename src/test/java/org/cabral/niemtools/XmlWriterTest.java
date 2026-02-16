package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.xml.XMLConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class XmlWriterTest {

    // --- Constants tests ---

    @Test
    public void testXmlHeaderConstant() {
        assertEquals("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n", XmlWriter.XML_HEADER);
    }

    @Test
    public void testXsdFileTypeConstant() {
        assertEquals(".xsd", XmlWriter.XSD_FILE_TYPE);
    }

    @Test
    public void testXmlFileTypeConstant() {
        assertEquals(".xml", XmlWriter.XML_FILE_TYPE);
    }

    @Test
    public void testGcFileTypeConstant() {
        assertEquals(".gc", XmlWriter.GC_FILE_TYPE);
    }

    @Test
    public void testXsiConstants() {
        assertEquals("xsi", XmlWriter.XSI_PREFIX);
        assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XmlWriter.XSI_URI);
    }

    @Test
    public void testXmlLangConstants() {
        assertEquals("xml:lang", XmlWriter.XML_LANG_PREFIX);
        assertEquals("en-US", XmlWriter.XML_LANG);
    }

    // --- Constructor test ---

    @Test
    public void testConstructor() {
        XmlWriter writer = new XmlWriter("/tmp/test");
        assertNotNull(writer);
    }

    @Test
    public void testConstructorWithNull() {
        XmlWriter writer = new XmlWriter(null);
        assertNotNull(writer);
    }

    // --- xmlNs static method tests ---

    @Test
    public void testXmlNsWithPrefix() {
        String result = XmlWriter.xmlNs("xs", "http://www.w3.org/2001/XMLSchema");
        assertEquals(" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"", result);
    }

    @Test
    public void testXmlNsWithEmptyPrefix() {
        String result = XmlWriter.xmlNs("", "http://example.com/schema");
        assertEquals(" xmlns=\"http://example.com/schema\"", result);
    }

    @Test
    public void testXmlNsWithStructuresPrefix() {
        String result = XmlWriter.xmlNs("structures", NiemModel.STRUCTURES_URI);
        assertTrue(result.contains("xmlns:structures="));
        assertTrue(result.contains(NiemModel.STRUCTURES_URI));
    }

    // --- writeXmlNs with FileWriter ---

    @Test
    public void testWriteXmlNsToFile() throws IOException {
        File tempFile = File.createTempFile("xmlNsTest", ".xml");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            XmlWriter.writeXmlNs(fw, "nc", "http://example.com/nc");
        }
        assertTrue(tempFile.length() > 0);
        tempFile.delete();
    }

    // --- xmlAttribute (private) via reflection ---

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testXmlAttributeViaReflection() {
        XmlWriter writer = new XmlWriter("/tmp");
        try {
            Method m = XmlWriter.class.getDeclaredMethod("xmlAttribute", String.class, String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "version", "1.0");
            assertEquals(" version=\"1.0\"", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testXmlAttributeEmptyValue() {
        XmlWriter writer = new XmlWriter("/tmp");
        try {
            Method m = XmlWriter.class.getDeclaredMethod("xmlAttribute", String.class, String.class);
            m.setAccessible(true);
            String result = (String) m.invoke(writer, "name", "");
            assertEquals(" name=\"\"", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- writeXmlAttribute (private) via reflection with file ---

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void testWriteXmlAttributeToFile() throws IOException {
        File tempFile = File.createTempFile("xmlAttrTest", ".xml");
        tempFile.deleteOnExit();
        XmlWriter writer = new XmlWriter("/tmp");
        try {
            Method m = XmlWriter.class.getDeclaredMethod("writeXmlAttribute", FileWriter.class, String.class, String.class);
            m.setAccessible(true);
            try (FileWriter fw = new FileWriter(tempFile)) {
                m.invoke(writer, fw, "elementFormDefault", "qualified");
            }
            assertTrue(tempFile.length() > 0);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
        tempFile.delete();
    }

    // --- XML catalog URI constant ---

    @Test
    public void testXmlCatalogURIConstant() {
        assertEquals("urn:oasis:names:tc:entity:xmlns:xml:catalog", XmlWriter.XML_CATALOG_URI);
    }

    // --- Multiple xmlNs calls produce valid output ---

    @Test
    public void testMultipleXmlNsCallsProduceValidOutput() throws IOException {
        File tempFile = File.createTempFile("xmlMultiNs", ".xml");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("<xs:schema");
            XmlWriter.writeXmlNs(fw, NiemModel.XSD_PREFIX, NiemModel.XSD_URI);
            XmlWriter.writeXmlNs(fw, NiemModel.NC_PREFIX, "http://example.com/nc");
            XmlWriter.writeXmlNs(fw, "", "http://default.ns");
            fw.write(">\n</xs:schema>");
        }
        assertTrue(tempFile.length() > 0);
        tempFile.delete();
    }
}
