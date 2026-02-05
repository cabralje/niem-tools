package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlItem;

public class NiemUmlModelTest {

    // --- getMaxOccurs tests ---

    @Test
    public void testGetMaxOccurs() {
        assertEquals("1", NiemUmlModel.getMaxOccurs(""));
        assertEquals("2", NiemUmlModel.getMaxOccurs("1,2"));
        assertEquals("unbounded", NiemUmlModel.getMaxOccurs("0,unbounded"));
        assertEquals("5", NiemUmlModel.getMaxOccurs("5"));
    }

    @Test
    public void testGetMaxOccursZeroToOne() {
        assertEquals("1", NiemUmlModel.getMaxOccurs("0,1"));
    }

    @Test
    public void testGetMaxOccursZero() {
        assertEquals("0", NiemUmlModel.getMaxOccurs("0,0"));
    }

    @Test
    public void testGetMaxOccursNull() {
        assertEquals("1", NiemUmlModel.getMaxOccurs(null));
    }

    // --- getMinOccurs tests ---

    @Test
    public void testGetMinOccurs() {
        assertEquals("1", NiemUmlModel.getMinOccurs(""));
        assertEquals("0", NiemUmlModel.getMinOccurs("0,2"));
        assertEquals("3", NiemUmlModel.getMinOccurs("3"));
    }

    @Test
    public void testGetMinOccursZeroToUnbounded() {
        assertEquals("0", NiemUmlModel.getMinOccurs("0,unbounded"));
    }

    @Test
    public void testGetMinOccursNull() {
        assertEquals("1", NiemUmlModel.getMinOccurs(null));
    }

    // --- getNiemMap / getNiemProperty ---

    @Test
    public void testGetNiemMapAndGetNiemProperty() {
        String[][] map = NiemUmlModel.getNiemMap();
        assertNotNull(map);
        assertTrue(map.length > 0);
        String prop = NiemUmlModel.getNiemProperty(5);
        assertTrue(prop.contains(":"));
    }

    @Test
    public void testGetNiemMapHas14Columns() {
        String[][] map = NiemUmlModel.getNiemMap();
        assertEquals(14, map.length);
    }

    @Test
    public void testGetNiemPropertyFirstFiveAreEmpty() {
        // Columns 0-4 are model columns with empty stereotype property names
        for (int i = 0; i < 5; i++) {
            String prop = NiemUmlModel.getNiemProperty(i);
            assertTrue("Column " + i + " should have empty property name", prop.isEmpty());
        }
    }

    @Test
    public void testGetNiemPropertyColumn5IsXPath() {
        String prop = NiemUmlModel.getNiemProperty(5);
        assertEquals("niem-profile:niem:XPath", prop);
    }

    @Test
    public void testGetNiemPropertyLastColumn() {
        String prop = NiemUmlModel.getNiemProperty(13);
        assertEquals("niem-profile:niem:CodeList", prop);
    }

    // --- isNiemUml ---

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
    public void testIsNiemUmlWithEmptyStereotype() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("");
        assertFalse(NiemUmlModel.isNiemUml(item));
    }

    @Test
    public void testIsNiemUmlWithWebserviceStereotype() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("niem-profile:webservice");
        assertFalse(NiemUmlModel.isNiemUml(item));
    }

    @Test
    public void testIsNiemUmlWithEnumStereotype() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("enum_pattern");
        assertFalse(NiemUmlModel.isNiemUml(item));
    }

    // --- isNiemType / isNiemElement ---

    @Test
    public void testIsNiemTypeAndIsNiemElement() {
        assertFalse(NiemUmlModel.isNiemType(""));
        assertFalse(NiemUmlModel.isNiemType(null));
        assertFalse(NiemUmlModel.isNiemElement(null));
        assertFalse(NiemUmlModel.isNiemElement(""));
    }

    @Test
    public void testIsNiemTypeWithRandomString() {
        assertFalse(NiemUmlModel.isNiemType("RandomNonExistentType12345"));
    }

    @Test
    public void testIsNiemElementWithRandomString() {
        assertFalse(NiemUmlModel.isNiemElement("RandomNonExistentElement12345"));
    }

    // --- getModel by item ---

    @Test
    public void testGetModelByItemReferenceModel() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        UmlItem parent = Mockito.mock(UmlItem.class);
        UmlItem grandparent = Mockito.mock(UmlItem.class);
        Mockito.when(item.parent()).thenReturn(parent);
        Mockito.when(parent.parent()).thenReturn(grandparent);
        Mockito.when(grandparent.name()).thenReturn("NIEMReference");
        NiemModel model = NiemUmlModel.getModel(item);
        assertNotNull(model);
        assertEquals(NiemUmlModel.getReferenceModel(), model);
    }

    @Test
    public void testGetModelByItemSubsetModel() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        UmlItem parent = Mockito.mock(UmlItem.class);
        UmlItem grandparent = Mockito.mock(UmlItem.class);
        Mockito.when(item.parent()).thenReturn(parent);
        Mockito.when(parent.parent()).thenReturn(grandparent);
        Mockito.when(grandparent.name()).thenReturn("NIEMSubset");
        NiemModel model = NiemUmlModel.getModel(item);
        assertNotNull(model);
        assertEquals(NiemUmlModel.getSubsetModel(), model);
    }

    @Test
    public void testGetModelByItemExtensionModel() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        UmlItem parent = Mockito.mock(UmlItem.class);
        UmlItem grandparent = Mockito.mock(UmlItem.class);
        Mockito.when(item.parent()).thenReturn(parent);
        Mockito.when(parent.parent()).thenReturn(grandparent);
        Mockito.when(grandparent.name()).thenReturn("NIEMExtension");
        NiemModel model = NiemUmlModel.getModel(item);
        assertNotNull(model);
        assertEquals(NiemUmlModel.getExtensionModel(), model);
    }

    @Test
    public void testGetModelByItemUnknownReturnsNull() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        UmlItem parent = Mockito.mock(UmlItem.class);
        UmlItem grandparent = Mockito.mock(UmlItem.class);
        Mockito.when(item.parent()).thenReturn(parent);
        Mockito.when(parent.parent()).thenReturn(grandparent);
        Mockito.when(grandparent.name()).thenReturn("SomeOtherPackage");
        NiemModel model = NiemUmlModel.getModel(item);
        assertNull(model);
    }

    // --- hideReferenceModel ---

    @Test
    public void testHideReferenceModelDoesNotThrow() {
        NiemUmlModel.hideReferenceModel();
    }

    // --- Model accessors ---

    @Test
    public void testGetReferenceModelAndSubsetModelAndExtensionModel() {
        assertNotNull(NiemUmlModel.getReferenceModel());
        assertNotNull(NiemUmlModel.getSubsetModel());
        assertNotNull(NiemUmlModel.getExtensionModel());
    }

    @Test
    public void testModelsAreDistinct() {
        assertFalse(NiemUmlModel.getReferenceModel() == NiemUmlModel.getSubsetModel());
        assertFalse(NiemUmlModel.getSubsetModel() == NiemUmlModel.getExtensionModel());
        assertFalse(NiemUmlModel.getReferenceModel() == NiemUmlModel.getExtensionModel());
    }

    // --- Stereotype and property constants ---

    @Test
    public void testStereotypeConstants() {
        assertEquals("niem-profile:niem:XPath", NiemUmlModel.NIEM_STEREOTYPE_XPATH);
        assertEquals("niem-profile:niem:Property", NiemUmlModel.NIEM_STEREOTYPE_PROPERTY);
    }

    @Test
    public void testPropertyConstants() {
        assertEquals("URI", NiemUmlModel.URI_PROPERTY);
        assertEquals("Notes", NiemUmlModel.NOTES_PROPERTY);
        assertEquals("isNillable", NiemUmlModel.NILLABLE_PROPERTY);
        assertEquals("prefix", NiemUmlModel.PREFIX_PROPERTY);
        assertEquals("localTerm", NiemUmlModel.LOCALTERM_PROPERTY);
        assertEquals("sequenceID", NiemUmlModel.SEQUENCE_ID_PROPERTY);
        assertEquals("substitutesFor", NiemUmlModel.SUBSTITUTION_PROPERTY);
        assertEquals("codeList", NiemUmlModel.CODELIST_PROPERTY);
        assertEquals("messageElement", NiemUmlModel.MESSAGE_ELEMENT_PROPERTY);
    }

    @Test
    public void testStereotypeDelimiter() {
        assertEquals(":", NiemUmlModel.STEREOTYPE_DELIMITER);
    }

    @Test
    public void testMappingSpreadsheetTitle() {
        assertEquals("NIEM Mapping", NiemUmlModel.MAPPING_SPREADSHEET_TITLE);
    }

    // --- isEnumeration / isFacet with mocked items ---

    @Test
    public void testIsEnumerationTrue() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("enum_pattern");
        assertTrue(NiemUmlModel.isEnumeration(item));
    }

    @Test
    public void testIsEnumerationFalse() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("niem-profile:niem");
        assertFalse(NiemUmlModel.isEnumeration(item));
    }

    @Test
    public void testIsFacetTrue() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("attribute");
        assertTrue(NiemUmlModel.isFacet(item));
    }

    @Test
    public void testIsFacetFalse() {
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.stereotype()).thenReturn("niem-profile:niem");
        assertFalse(NiemUmlModel.isFacet(item));
    }
}
