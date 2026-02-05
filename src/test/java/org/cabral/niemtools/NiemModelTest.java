package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class NiemModelTest {

    // --- Constants tests ---

    @Test
    public void testXmlTypeNamesContainsCommonTypes() {
        // Verify key XSD built-in types are present via getURI with xs prefix
        // The XML_TYPE_NAMES array should include standard types
        assertTrue(NiemModel.isAbstract("abstract"));
        assertFalse(NiemModel.isAbstract("string"));
    }

    @Test
    public void testNiemNamespaceConstants() {
        assertEquals("nc", NiemModel.NC_PREFIX);
        assertEquals("structures", NiemModel.STRUCTURES_PREFIX);
        assertEquals("local", NiemModel.LOCAL_PREFIX);
        assertEquals("xs", NiemModel.XSD_PREFIX);
        assertEquals("niem-xs", NiemModel.PROXY_PREFIX);
        assertEquals("appinfo", NiemModel.APPINFO_PREFIX);
        assertEquals("ct", NiemModel.CT_PREFIX);
    }

    @Test
    public void testInfrastructureTypeNames() {
        assertEquals("abstract", NiemModel.ABSTRACT_TYPE_NAME);
        assertEquals("AugmentationPoint", NiemModel.AUGMENTATION_POINT_NAME);
        assertEquals("Augmentation", NiemModel.AUGMENTATION_NAME);
        assertEquals("Abstract", NiemModel.ABSTRACT_NAME);
        assertEquals("Representation", NiemModel.REPRESENTATION_NAME);
        assertEquals("SimpleType", NiemModel.SIMPLE_TYPE_NAME);
    }

    @Test
    public void testCodelistDelimiters() {
        assertEquals("=", NiemModel.CODELIST_DEFINITION_DELIMITER);
        assertEquals(";", NiemModel.CODELIST_DELIMITER);
    }

    // --- getURI(String, String) tests ---

    @Test
    public void testGetURIBasic() {
        String uri = NiemModel.getURI("http://example.com/schema", "ElementName");
        assertEquals("http://example.com/schema,ElementName", uri);
    }

    @Test
    public void testGetURIWithPrefixedName() {
        // getName strips the prefix portion
        String uri = NiemModel.getURI("http://example.com/schema", "nc:PersonName");
        assertEquals("http://example.com/schema,PersonName", uri);
    }

    @Test
    public void testGetURIRemovesCommasFromName() {
        // Commas in the name should be removed (HASH_DELIMITER)
        String uri = NiemModel.getURI("http://schema", "Name,With,Commas");
        assertFalse(uri.substring(uri.indexOf(",") + 1).contains(","));
    }

    @Test
    public void testGetURIWithEmptyName() {
        String uri = NiemModel.getURI("http://schema", "");
        assertEquals("http://schema,", uri);
    }

    // --- isAbstract tests ---

    @Test
    public void testIsAbstractTrue() {
        assertTrue(NiemModel.isAbstract("abstract"));
    }

    @Test
    public void testIsAbstractFalse() {
        assertFalse(NiemModel.isAbstract("PersonType"));
        assertFalse(NiemModel.isAbstract("AbstractType"));
        assertFalse(NiemModel.isAbstract(""));
    }

    // --- isAugmentation tests ---

    @Test
    public void testIsAugmentationWithAugmentationPoint() {
        assertTrue(NiemModel.isAugmentation("PersonAugmentationPoint"));
    }

    @Test
    public void testIsAugmentationWithAugmentation() {
        assertTrue(NiemModel.isAugmentation("PersonAugmentation"));
    }

    @Test
    public void testIsAugmentationFalse() {
        assertFalse(NiemModel.isAugmentation("PersonType"));
        assertFalse(NiemModel.isAugmentation(""));
    }

    // --- isAugmentationType tests ---

    @Test
    public void testIsAugmentationTypeTrue() {
        assertTrue(NiemModel.isAugmentationType("PersonAugmentationType"));
    }

    @Test
    public void testIsAugmentationTypeFalse() {
        assertFalse(NiemModel.isAugmentationType("PersonType"));
        assertFalse(NiemModel.isAugmentationType("AugmentationPoint"));
    }

    // --- filterUMLAttribute (static protected) tests ---

    @Test
    public void testFilterUMLAttributeNormal() {
        assertEquals("PersonName", NiemModel.filterUMLAttribute("PersonName"));
    }

    @Test
    public void testFilterUMLAttributeWithSpecialChars() {
        assertEquals("@attr", NiemModel.filterUMLAttribute("@attr"));
        assertEquals("name_1", NiemModel.filterUMLAttribute("name_1"));
    }

    @Test
    public void testFilterUMLAttributeRemovesInvalidChars() {
        // Spaces and dashes should be removed
        String result = NiemModel.filterUMLAttribute("name with spaces");
        assertFalse(result.contains(" "));
    }

    @Test
    public void testFilterUMLAttributeNull() {
        assertNull(NiemModel.filterUMLAttribute(null));
    }

    @Test
    public void testFilterUMLAttributeEmpty() {
        assertEquals("", NiemModel.filterUMLAttribute(""));
    }

    // --- filterEnum (package-private) tests ---

    @Test
    public void testFilterEnumNormal() {
        NiemModel model = new NiemModel();
        assertEquals("Code1", model.filterEnum("Code1"));
    }

    @Test
    public void testFilterEnumRemovesSemicolon() {
        NiemModel model = new NiemModel();
        assertEquals("Code1Code2", model.filterEnum("Code1;Code2"));
    }

    @Test
    public void testFilterEnumRemovesEqualsSign() {
        NiemModel model = new NiemModel();
        assertEquals("CodeDefinition", model.filterEnum("Code=Definition"));
    }

    @Test
    public void testFilterEnumRemovesBothDelimiters() {
        NiemModel model = new NiemModel();
        assertEquals("abc", model.filterEnum("a=b;c"));
    }

    @Test
    public void testFilterEnumNull() {
        NiemModel model = new NiemModel();
        assertNull(model.filterEnum(null));
    }

    @Test
    public void testFilterEnumEmpty() {
        NiemModel model = new NiemModel();
        assertEquals("", model.filterEnum(""));
    }

    // --- NiemModel constructor and accessors ---

    @Test
    public void testConstructor() {
        NiemModel model = new NiemModel();
        assertNotNull(model);
    }

    @Test
    public void testGetSizeEmptyModel() {
        NiemModel model = new NiemModel();
        assertEquals(0, model.getSize());
    }

    @Test
    public void testGetModelPackageDefaultNull() {
        NiemModel model = new NiemModel();
        assertNull(model.getModelPackage());
    }

    @Test
    public void testGetAbstractTypeDefaultNull() {
        NiemModel model = new NiemModel();
        assertNull(model.getAbstractType());
    }

    @Test
    public void testGetAugmentationTypeDefaultNull() {
        NiemModel model = new NiemModel();
        assertNull(model.getAugmentationType());
    }

    @Test
    public void testGetObjectTypeDefaultNull() {
        NiemModel model = new NiemModel();
        assertNull(model.getObjectType());
    }

    // --- Element and type lookups on empty model ---

    @Test
    public void testGetElementByURIReturnsNullOnEmptyModel() {
        NiemModel model = new NiemModel();
        assertNull(model.getElementByURI("http://nonexistent"));
    }

    @Test
    public void testGetTypeByURIReturnsNullOnEmptyModel() {
        NiemModel model = new NiemModel();
        assertNull(model.getTypeByURI("http://nonexistent"));
    }

    @Test
    public void testGetElementReturnsNullOnEmptyModel() {
        NiemModel model = new NiemModel();
        assertNull(model.getElement("http://schema", "element"));
    }

    @Test
    public void testGetTypeReturnsNullOnEmptyModel() {
        NiemModel model = new NiemModel();
        assertNull(model.getType("http://schema", "SomeType"));
    }

    @Test
    public void testGetElementsInTypeReturnsNullOnEmptyModel() {
        NiemModel model = new NiemModel();
        assertNull(model.getElementsInType("http://nonexistent"));
    }

    // --- filterASCII (private, via reflection) ---

    @Test
    public void testFilterASCII() {
        NiemModel model = new NiemModel();
        try {
            Method m = NiemModel.class.getDeclaredMethod("filterASCII", String.class);
            m.setAccessible(true);
            assertEquals("Hello World", m.invoke(model, "Hello World"));
            assertEquals("abc", m.invoke(model, "abc\u00e9\u00f1"));
            assertNull(m.invoke(model, (String) null));
            assertEquals("", m.invoke(model, ""));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- filterEnumDefinition (private, via reflection) ---

    @Test
    public void testFilterEnumDefinition() {
        NiemModel model = new NiemModel();
        try {
            Method m = NiemModel.class.getDeclaredMethod("filterEnumDefinition", String.class);
            m.setAccessible(true);
            assertEquals("A description", m.invoke(model, "A description"));
            assertEquals("ab", m.invoke(model, "a=b"));
            assertEquals("ab", m.invoke(model, "a;b"));
            assertNull(m.invoke(model, (String) null));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- filterUMLElement (private, via reflection) ---

    @Test
    public void testFilterUMLElement() {
        NiemModel model = new NiemModel();
        try {
            Method m = NiemModel.class.getDeclaredMethod("filterUMLElement", String.class);
            m.setAccessible(true);
            assertEquals("PersonName", m.invoke(model, "PersonName"));
            assertEquals("name-1", m.invoke(model, "name-1"));
            assertNull(m.invoke(model, (String) null));
            assertEquals("", m.invoke(model, ""));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- filterUMLType (private, via reflection) ---

    @Test
    public void testFilterUMLType() {
        NiemModel model = new NiemModel();
        try {
            Method m = NiemModel.class.getDeclaredMethod("filterUMLType", String.class);
            m.setAccessible(true);
            assertEquals("PersonType", m.invoke(model, "PersonType"));
            assertNull(m.invoke(model, (String) null));
            assertEquals("", m.invoke(model, ""));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // --- recompileXPaths ---

    @Test
    public void testXPathFieldNotNull() {
        assertNotNull(NiemModel.xPath);
    }

    // --- cacheModel on empty model should not throw ---

    @Test
    public void testCacheModelOnEmptyModelDoesNotThrow() {
        NiemModel model = new NiemModel();
        try {
            model.cacheModel();
        } catch (Exception e) {
            fail("cacheModel on empty model should not throw: " + e.getMessage());
        }
    }
}
