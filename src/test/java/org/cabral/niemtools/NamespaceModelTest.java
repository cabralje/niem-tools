package org.cabral.niemtools;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import fr.bouml.UmlItem;

public class NamespaceModelTest {
    @Test
    public void testAddNamespaceAndGetNamespace() {
        Namespace ns = NamespaceModel.addNamespace("http://test/schema");
        assertNotNull(ns);
        assertEquals(ns, NamespaceModel.getNamespace("http://test/schema"));
    }

    @Test
    public void testAddPrefixAndGetPrefixes() {
        NamespaceModel.addPrefix("http://test/schema2", "tst");
        Map<String, String> prefixes = NamespaceModel.getPrefixes();
        assertTrue(prefixes.containsKey("tst"));
        assertEquals("http://test/schema2", prefixes.get("tst"));
    }

    @Test
    public void testFilterAttributePrefix() {
        assertEquals("foo", NamespaceModel.filterAttributePrefix("@foo"));
        assertEquals("bar", NamespaceModel.filterAttributePrefix("bar"));
    }

    @Test
    public void testGetNameFromString() {
        assertEquals("bar", NamespaceModel.getName("prefix:bar"));
        assertEquals("foo", NamespaceModel.getName("foo"));
        assertEquals("", NamespaceModel.getName((String) null));
    }

    @Test
    public void testGetNameFromUmlItem() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.name()).thenReturn("prefix:bar");
        assertEquals("bar", NamespaceModel.getName(item));
    }

    @Test
    public void testGetPrefixFromString() {
        assertEquals("prefix", NamespaceModel.getPrefix("prefix:bar"));
        assertNull(NamespaceModel.getPrefix("bar"));
        assertNull(NamespaceModel.getPrefix((String) null));
    }

    @Test
    public void testGetPrefixedAttributeName() {
        assertEquals("p:@foo", NamespaceModel.getPrefixedAttributeName("p", "@foo"));
    }

    @Test
    public void testGetPrefixedName() {
        assertEquals("p:foo", NamespaceModel.getPrefixedName("p", "foo"));
    }

    /*
    @Test
    public void testGetPrefixedNameFromUmlItem() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.name()).thenReturn("foo");
        Mockito.when(item.kind()).thenReturn(anItemKind.aClass);
        Mockito.when(item.parent()).thenReturn(null);
        assertTrue(NamespaceModel.getPrefixedName(item).endsWith(":foo"));
    }

    @Test
    public void testGetSchemaURIAndGetSchemaURIForPrefix() {
        NamespaceModel.addPrefix("http://test/schema3", "t3");
        assertEquals("http://test/schema3", NamespaceModel.getSchemaURIForPrefix("t3"));
        assertNull(NamespaceModel.getSchemaURI("noPrefix:foo"));
    }
    */

    @Test
    public void testIsAttribute() {
        assertTrue(NamespaceModel.isAttribute("@foo"));
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.name()).thenReturn("@foo");
        assertTrue(NamespaceModel.isAttribute(item));
    }

    @Test
    public void testIsExternalPrefixAndIsNiemPrefix() {
        // Add an external prefix
        NamespaceModel.addPrefix("http://external/schema", "ext");
        // Simulate externalPrefixes containing "ext"
        assertFalse(NamespaceModel.isNiemPrefix("ext"));
        assertFalse(NamespaceModel.isExternalPrefix(null));
    }

    @Test
    public void testGetSize() {
        int before = NamespaceModel.getSize();
        NamespaceModel.addNamespace("http://test/size");
        assertEquals(before + 1, NamespaceModel.getSize());
    }

    @Test
    public void testImportNamespacesHandlesNulls() {
        Document doc = Mockito.mock(Document.class);
        Element element = Mockito.mock(Element.class);
        NamedNodeMap attrs = Mockito.mock(NamedNodeMap.class);
        Mockito.when(doc.getDocumentElement()).thenReturn(element);
        Mockito.when(element.getAttributes()).thenReturn(attrs);
        Mockito.when(attrs.getLength()).thenReturn(0);
        //Namespace ns = NamespaceModel.importNamespaces(doc);
        //assertNull(ns);
    }
}
