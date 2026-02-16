package org.cabral.niemtools;

import java.lang.reflect.InvocationTargetException;

import javax.xml.XMLConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NamespaceResolverTest {
    /*
    @Test
    public void testConstructorAndNamespaceParsing() {
        Node root = Mockito.mock(Node.class);
        Node child = Mockito.mock(Node.class);
        NamedNodeMap attrs = Mockito.mock(NamedNodeMap.class);
        Attr attr = Mockito.mock(Attr.class);
        Mockito.when(root.getFirstChild()).thenReturn(child);
        Mockito.when(child.hasAttributes()).thenReturn(true);
        Mockito.when(child.getAttributes()).thenReturn(attrs);
        Mockito.when(attrs.getLength()).thenReturn(1);
        Mockito.when(attrs.item(0)).thenReturn(attr);
        Mockito.when(attr.getNamespaceURI()).thenReturn(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        Mockito.when(attr.getNodeName()).thenReturn(XMLConstants.XMLNS_ATTRIBUTE);
        Mockito.when(attr.getNodeValue()).thenReturn("http://test/uri");
        NamespaceResolver resolver = new NamespaceResolver(root, true);
        assertEquals("http://test/uri", resolver.getNamespaceURI(""));
    }
    */
    
    @Test
    public void testGetNamespaceURIWithKnownPrefixes() {
        Node root = Mockito.mock(Node.class);
        Node child = Mockito.mock(Node.class);
        Mockito.when(root.getFirstChild()).thenReturn(child);
        Mockito.when(child.hasAttributes()).thenReturn(false);
        NamespaceResolver resolver = new NamespaceResolver(root, true);
        assertEquals(NiemModel.XSD_URI, resolver.getNamespaceURI(null));
        assertEquals(NiemModel.XSD_URI, resolver.getNamespaceURI("xs"));
        assertEquals(NiemModel.XSD_URI, resolver.getNamespaceURI("xsd"));
        assertEquals("http://local", resolver.getNamespaceURI("local"));
    }

    @Test
    public void testGetPrefixAndPutInCache() {
        Node root = Mockito.mock(Node.class);
        Node child = Mockito.mock(Node.class);
        Mockito.when(root.getFirstChild()).thenReturn(child);
        Mockito.when(child.hasAttributes()).thenReturn(false);
        NamespaceResolver resolver = new NamespaceResolver(root, true);
        // Use reflection to call private putInCache
        try {
            var m = NamespaceResolver.class.getDeclaredMethod("putInCache", String.class, String.class);
            m.setAccessible(true);
            m.invoke(resolver, "myprefix", "myuri");
            assertEquals("myprefix", resolver.getPrefix("myuri"));
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetPrefixesReturnsNull() {
        Node root = Mockito.mock(Node.class);
        Node child = Mockito.mock(Node.class);
        Mockito.when(root.getFirstChild()).thenReturn(child);
        Mockito.when(child.hasAttributes()).thenReturn(false);
        NamespaceResolver resolver = new NamespaceResolver(root, true);
        assertNull(resolver.getPrefixes("anyuri"));
    }

    @Test
    public void testExamineNodeRecursion() {
        Node root = Mockito.mock(Node.class);
        Node child = Mockito.mock(Node.class);
        Node grandchild = Mockito.mock(Node.class);
        NodeList children = Mockito.mock(NodeList.class);
        Mockito.when(root.getFirstChild()).thenReturn(child);
        Mockito.when(child.hasAttributes()).thenReturn(false);
        Mockito.when(child.hasChildNodes()).thenReturn(true);
        Mockito.when(child.getChildNodes()).thenReturn(children);
        Mockito.when(children.getLength()).thenReturn(1);
        Mockito.when(children.item(0)).thenReturn(grandchild);
        Mockito.when(grandchild.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        Mockito.when(grandchild.hasAttributes()).thenReturn(false);
        Mockito.when(grandchild.hasChildNodes()).thenReturn(false);
        @SuppressWarnings("unused")
        NamespaceResolver resolver = new NamespaceResolver(root, false);
        // No assertion, just ensure no exceptions
    }
}
