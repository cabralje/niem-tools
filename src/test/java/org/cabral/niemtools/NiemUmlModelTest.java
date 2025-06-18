package org.cabral.niemtools;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlItem;

public class NiemUmlModelTest {
    @Test
    public void testGetMaxOccurs() {
        assertEquals("1", NiemUmlModel.getMaxOccurs(""));
        assertEquals("2", NiemUmlModel.getMaxOccurs("1,2"));
        assertEquals("unbounded", NiemUmlModel.getMaxOccurs("0,unbounded"));
        assertEquals("5", NiemUmlModel.getMaxOccurs("5"));
    }

    @Test
    public void testGetMinOccurs() {
        assertEquals("1", NiemUmlModel.getMinOccurs(""));
        assertEquals("0", NiemUmlModel.getMinOccurs("0,2"));
        assertEquals("3", NiemUmlModel.getMinOccurs("3"));
    }

    @Test
    public void testGetNiemMapAndGetNiemProperty() {
        String[][] map = NiemUmlModel.getNiemMap();
        assertNotNull(map);
        assertTrue(map.length > 0);
        String prop = NiemUmlModel.getNiemProperty(5);
        assertTrue(prop.contains(":"));
    }

    //@Test
    //public void testGetNiemVersion() {
    //    assertEquals("6.0", NiemUmlModel.getNiemVersion());
    //}

    @Test
    public void testIsNiemUml() {
        assertFalse(NiemUmlModel.isNiemUml(null));
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn(null);
        assertFalse(NiemUmlModel.isNiemUml(item));
        Mockito.when(item.stereotype()).thenReturn("niem-profile:niem");
        assertTrue(NiemUmlModel.isNiemUml(item));
    }

    @Test
    public void testIsNiemTypeAndIsNiemElement() {
        // These will return false for null, empty, or unknown types
        //assertFalse(NiemUmlModel.isNiem(null));
        assertFalse(NiemUmlModel.isNiem(""));
        assertFalse(NiemUmlModel.isNiemElement(null));
        assertFalse(NiemUmlModel.isNiemElement(""));
    }
    /* 
    @Test
    public void testIsNiemElementInType() {
        // Will return false for non-NIEM types/elements
        assertFalse(NiemUmlModel.isNiemElementInType("foo", "bar"));
    }

    @Test
    public void testIsNiem() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        Mockito.when(item.name()).thenReturn("foo");
        assertFalse(NiemUmlModel.isNiem(item));
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClassInstance);
        assertFalse(NiemUmlModel.isNiem(item));
    }
    */

    @Test
    public void testGetModelByUriAndItem() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        UmlItem parent = Mockito.mock(UmlItem.class);
        UmlItem grandparent = Mockito.mock(UmlItem.class);
        Mockito.when(item.parent()).thenReturn(parent);
        Mockito.when(parent.parent()).thenReturn(grandparent);
        Mockito.when(grandparent.name()).thenReturn("NIEMReference");
        assertNotNull(NiemUmlModel.getModel(item));
    }

    @Test
    public void testHideReferenceModelAndHideItem() {
        // Should not throw
        NiemUmlModel.hideReferenceModel();
    }

    /*
    @Test
    public void testGetProperty() {
        // Will return null unless UmlPackage.getProject() is properly mocked
        assertNull(NiemUmlModel.getProperty("nonexistent"));
    }
    */
    
    @Test
    public void testGetReferenceModelAndSubsetModelAndExtensionModel() {
        assertNotNull(NiemUmlModel.getReferenceModel());
        assertNotNull(NiemUmlModel.getSubsetModel());
        assertNotNull(NiemUmlModel.getExtensionModel());
    }
}
