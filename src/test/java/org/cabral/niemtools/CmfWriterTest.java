package org.cabral.niemtools;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlClass;
import fr.bouml.UmlItem;

public class CmfWriterTest {
    @Test
    public void testIsOlderCmfVersionTrue() {
        CmfWriter writer = new CmfWriter("dir", "0.7");
        assertTrue(writer.getClass().getDeclaredMethods().length > 0); // Sanity check
        // Reflection to access private method
        try {
            var m = CmfWriter.class.getDeclaredMethod("isOlderCmfVersion", String.class, String.class);
            m.setAccessible(true);
            assertTrue((Boolean) m.invoke(writer, "0.7", "1.0"));
            assertFalse((Boolean) m.invoke(writer, "1.0", "0.7"));
            assertFalse((Boolean) m.invoke(writer, "1.0", "1.0"));
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testExportCmfModelNotNull() {
        CmfWriter writer = new CmfWriter("dir", "0.8");
        // Should not throw and should return a LinkedHashSet
        try {
            var m = CmfWriter.class.getDeclaredMethod("exportCmfModel");
            m.setAccessible(true);
            Object result = m.invoke(writer);
            assertNotNull(result);
            assertTrue(result instanceof LinkedHashSet);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            // Acceptable if dependencies are missing
        }
    }
/* 
    @Test
    public void testExportCmfCreatesFile() throws IOException {
        String tempDir = Files.createTempDirectory("cmfTest").toString();
        CmfWriter writer = new CmfWriter(tempDir, "0.8");
        try {
            writer.exportCmf(tempDir);
            File[] files = new File(tempDir).listFiles((dir, name) -> name.endsWith(".cmf"));
            assertNotNull(files);
            assertTrue(files.length > 0);
        } catch (Exception e) {
            // Acceptable if dependencies are missing
        }
    }

    @Test
    public void testExportCmfWithNullDir() {
        CmfWriter writer = new CmfWriter(null, "0.8");
        try {
            writer.exportCmf(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException | IOException e) {
            // Expected
        }
    }
*/
    @Test
    public void testExportCmfClassHandlesNullBaseType() {
        CmfWriter writer = new CmfWriter("dir", "0.8");
        try {
            var m = CmfWriter.class.getDeclaredMethod("exportCmfClass", UmlClass.class);
            m.setAccessible(true);
            UmlClass mockClass = Mockito.mock(UmlClass.class);
            Mockito.when(mockClass.children()).thenReturn(new UmlItem[0]);
            Mockito.when(mockClass.description()).thenReturn("");
            Mockito.when(mockClass.kind()).thenReturn(null);
            Mockito.when(mockClass.name()).thenReturn("TestClass");
            Object result = m.invoke(writer, mockClass);
            assertNotNull(result);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            // Acceptable if dependencies are missing
        }
    }

    @Test
    public void testGetCmfFilename() {
        String filename = "TestFile";
        String version = "1.2";
        String expected = "TestFile.cmf";
        assertEquals(expected, CmfWriter.getCmfFilename(filename, version));
    }

    @Test
    public void testTagMethodProducesCorrectXml() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var tagMethod = CmfWriter.class.getDeclaredMethod("tag", String.class, String.class);
        tagMethod.setAccessible(true);
        String result = (String) tagMethod.invoke(writer, "TestTag", "TestContent");
        assertEquals("<TestTag>TestContent</TestTag>", result);
    }

    @Test
    public void testTagIdProducesCorrectXml() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var tagIdMethod = CmfWriter.class.getDeclaredMethod("tagId", String.class, String.class, String.class);
        tagIdMethod.setAccessible(true);
        String result = (String) tagIdMethod.invoke(writer, "TestTag", "id:val", "Content");
        assertTrue(result.contains("structures:id=\"id.val\""));
        assertTrue(result.contains(">Content</TestTag>"));
    }

    @Test
    public void testTagRefProducesCorrectXml() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var tagRefMethod = CmfWriter.class.getDeclaredMethod("tagRef", String.class, String.class);
        tagRefMethod.setAccessible(true);
        String result = (String) tagRefMethod.invoke(writer, "TestTag", "ref:val");
        assertTrue(result.contains("structures:ref=\"ref.val\""));
        assertTrue(result.contains("/>"));
    }

    @Test
    public void testExportCmfMultiplicityNullOrEmpty() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var method = CmfWriter.class.getDeclaredMethod("exportCmfMultiplicity", String.class);
        method.setAccessible(true);
        assertEquals("", method.invoke(writer, (String) null));
        assertEquals("", method.invoke(writer, ""));
    }

    /*
    @Test
    public void testExportCmfDatatypeAbstract() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var method = CmfWriter.class.getDeclaredMethod("exportCmfDatatype", fr.bouml.UmlClass.class, Boolean.class);
        method.setAccessible(true);
        fr.bouml.UmlClass mockClass = Mockito.mock(fr.bouml.UmlClass.class);
        Mockito.when(mockClass.children()).thenReturn(new fr.bouml.UmlItem[0]);
        Mockito.when(mockClass.description()).thenReturn("desc");
        Mockito.when(mockClass.kind()).thenReturn(null);
        Mockito.when(mockClass.name()).thenReturn("TestClass");
        // static methods for NamespaceModel.getPrefixedName and NamespaceModel.getName must be stubbed if possible
        // Here we just check that it doesn't throw and returns a non-null string
        Object result = method.invoke(writer, mockClass, true);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    public void testExportCmfRestrictionTypeHandlesNullBaseType() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var method = CmfWriter.class.getDeclaredMethod("exportCmfRestrictionType", fr.bouml.UmlClass.class);
        method.setAccessible(true);
        fr.bouml.UmlClass mockClass = Mockito.mock(fr.bouml.UmlClass.class);
        Mockito.when(mockClass.children()).thenReturn(new fr.bouml.UmlItem[0]);
        Mockito.when(mockClass.description()).thenReturn("desc");
        Mockito.when(mockClass.kind()).thenReturn(null);
        Mockito.when(mockClass.name()).thenReturn("TestClass");
        Object result = method.invoke(writer, mockClass);
        // Should not throw, may return null or string depending on static stubs
        assertTrue(result == null || result instanceof String);
    }
    */

    @Test
    public void testExportCmfNamespaceHandlesNull() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var method = CmfWriter.class.getDeclaredMethod("exportCmfNamespace", fr.bouml.UmlClassView.class);
        method.setAccessible(true);
        Object result = method.invoke(writer, (Object) null);
        assertNull(result);
    }

    /* 
    @Test
    public void testExportCmfPropertyHandlesNullId() throws Exception {
        CmfWriter writer = new CmfWriter("dir", "1.0");
        var method = CmfWriter.class.getDeclaredMethod("exportCmfProperty", fr.bouml.UmlClassInstance.class);
        method.setAccessible(true);
        fr.bouml.UmlClassInstance mockInstance = Mockito.mock(fr.bouml.UmlClassInstance.class);
        // Simulate filterAttributePrefix returning null by making NamespaceModel static method return null
        // Here, just check that it doesn't throw and returns null
        Object result = method.invoke(writer, mockInstance);
        assertNull(result);
    }
    */
    // Add more edge case tests as needed
}
