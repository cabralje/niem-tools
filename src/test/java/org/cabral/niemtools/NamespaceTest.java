package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlClassView;

public class NamespaceTest {
    @Test
    public void testConstructorAndGetSchemaURI() {
        Namespace ns = new Namespace("http://example.com/schema");
        assertEquals("http://example.com/schema", ns.getSchemaURI());
    }

    @Test
    public void testNsClassViewSetAndGet() {
        Namespace ns = new Namespace("uri");
        UmlClassView mockView = Mockito.mock(UmlClassView.class);
        ns.setNsClassView(mockView);
        assertSame(mockView, ns.getNsClassView());
    }

    @Test
    public void testReferenceClassViewSetAndGet() {
        Namespace ns = new Namespace("uri");
        UmlClassView mockView = Mockito.mock(UmlClassView.class);
        ns.setReferenceClassView(mockView);
        assertSame(mockView, ns.getReferenceClassView());
    }

    @Test
    public void testFilepathSetAndGet() {
        Namespace ns = new Namespace("uri");
        assertNull(ns.getFilepath());
        ns.setFilepath("/tmp/file");
        assertEquals("/tmp/file", ns.getFilepath());
    }

    @Test
    public void testNullsAndDefaults() {
        Namespace ns = new Namespace(null);
        assertNull(ns.getSchemaURI());
        assertNull(ns.getNsClassView());
        assertNull(ns.getReferenceClassView());
        assertNull(ns.getFilepath());
    }
}
