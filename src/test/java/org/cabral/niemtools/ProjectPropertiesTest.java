package org.cabral.niemtools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlPackage;

public class ProjectPropertiesTest {

    @Test
    public void testDefaultsNotNull() {
        assertNotNull(ProjectProperties.getDefaults());
    }

    // --- Default values verification ---

    @Test
    public void testDefaultNiemVersion() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("6.0-ps02", defaults.getProperty(ProjectProperties.IMPORT_NIEM_VERSION));
    }

    @Test
    public void testDefaultCmfVersion() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("1.0", defaults.getProperty(ProjectProperties.EXPORT_CMF_VERSION));
    }

    @Test
    public void testDefaultExportURI() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("http://local", defaults.getProperty(ProjectProperties.EXPORT_URI));
    }

    @Test
    public void testDefaultBooleanProperties() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("true", defaults.getProperty(ProjectProperties.IMPORT_CODE_DESCRIPTIONS));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_CMF));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_XSD_MODEL));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_HTML));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_XSD));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_JSON));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_CODELISTS));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_WSDL));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_OPENAPI));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_WANTLIST));
        assertEquals("true", defaults.getProperty(ProjectProperties.EXPORT_SORT_EXTENSION));
        assertEquals("false", defaults.getProperty(ProjectProperties.LOG_DEBUG));
        assertEquals("false", defaults.getProperty(ProjectProperties.LOG_PROFILE));
    }

    @Test
    public void testDefaultMaxFacets() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("100", defaults.getProperty(ProjectProperties.IMPORT_MAX_FACETS));
    }

    @Test
    public void testDefaultIEPDProperties() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals("NIEM Message Specification", defaults.getProperty(ProjectProperties.IEPD_NAME));
        assertEquals("1.0", defaults.getProperty(ProjectProperties.IEPD_VERSION));
        assertEquals("Draft", defaults.getProperty(ProjectProperties.IEPD_STATUS));
        assertEquals("Organization Name", defaults.getProperty(ProjectProperties.IEPD_ORGANIZATION));
        assertEquals("Contact Name", defaults.getProperty(ProjectProperties.IEPD_CONTACT));
        assertEquals("email@example.com", defaults.getProperty(ProjectProperties.IEPD_EMAIL));
    }

    @Test
    public void testDefaultExportDateFormat() {
        Properties defaults = ProjectProperties.getDefaults();
        String date = defaults.getProperty(ProjectProperties.EXPORT_DATE);
        assertNotNull(date);
        // Should match yyyy-MM-dd format
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testDefaultExportProjectDir() {
        Properties defaults = ProjectProperties.getDefaults();
        assertEquals(System.getProperty("user.home"), defaults.getProperty(ProjectProperties.EXPORT_PROJECT_DIR));
    }

    @Test
    public void testDefaultPathProperties() {
        Properties defaults = ProjectProperties.getDefaults();
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_HTML_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_XSD_MODEL_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_CMF_FILE));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_XSD_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_XML_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_FILE));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_JSON_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_WSDL_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_OPENAPI_DIR));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_WANTLIST_FILE));
        assertNotNull(defaults.getProperty(ProjectProperties.EXPORT_CODELISTS_DIR));
    }

    // --- setProperty / getProperty roundtrip with null project ---

    @Test
    public void testSetAndGetPropertyNullProject() {
        ProjectProperties props = new ProjectProperties(null, ProjectProperties.getDefaults());
        props.setProperty("testKey", "testValue");
        assertEquals("testValue", props.getProperty("testKey"));
    }

    @Test
    public void testSetPropertyOverridesDefault() {
        ProjectProperties props = new ProjectProperties(null, ProjectProperties.getDefaults());
        assertEquals("6.0-ps02", props.getProperty(ProjectProperties.IMPORT_NIEM_VERSION));
        props.setProperty(ProjectProperties.IMPORT_NIEM_VERSION, "5.0");
        assertEquals("5.0", props.getProperty(ProjectProperties.IMPORT_NIEM_VERSION));
    }

    @Test
    public void testSetPropertyMultipleTimes() {
        ProjectProperties props = new ProjectProperties(null, ProjectProperties.getDefaults());
        props.setProperty("key", "value1");
        assertEquals("value1", props.getProperty("key"));
        props.setProperty("key", "value2");
        assertEquals("value2", props.getProperty("key"));
    }

    @Test
    public void testGetPropertyWithDefault() {
        ProjectProperties props = new ProjectProperties(null, null);
        assertEquals("fallback", props.getProperty("nonexistent", "fallback"));
    }

    @Test
    public void testGetPropertyReturnsNullForMissing() {
        ProjectProperties props = new ProjectProperties(null, null);
        assertNull(props.getProperty("nonexistent"));
    }

    // --- Constructor with null defaults ---

    @Test
    public void testConstructorWithNullDefaults() {
        ProjectProperties props = new ProjectProperties(null, null);
        assertNotNull(props);
    }

    // --- store() with null project ---

    @Test
    public void testStoreWithNullProjectDoesNotThrow() {
        ProjectProperties props = new ProjectProperties(null, ProjectProperties.getDefaults());
        try {
            props.store();
        } catch (Exception e) {
            fail("store() with null project should not throw: " + e.getMessage());
        }
    }

    // --- load() with null project ---

    @Test
    public void testLoadWithNullProjectDoesNotThrow() {
        ProjectProperties props = new ProjectProperties(null, null);
        try {
            props.load();
        } catch (Exception e) {
            fail("load() with null project should not throw: " + e.getMessage());
        }
    }

    // --- Property key constants are non-null ---

    @Test
    public void testPropertyKeyConstantsAreNotNull() {
        assertNotNull(ProjectProperties.IMPORT_NIEM_VERSION);
        assertNotNull(ProjectProperties.IMPORT_CODE_DESCRIPTIONS);
        assertNotNull(ProjectProperties.IMPORT_MAX_FACETS);
        assertNotNull(ProjectProperties.EXPORT_URI);
        assertNotNull(ProjectProperties.EXPORT_CMF);
        assertNotNull(ProjectProperties.EXPORT_HTML);
        assertNotNull(ProjectProperties.EXPORT_XSD);
        assertNotNull(ProjectProperties.EXPORT_JSON);
        assertNotNull(ProjectProperties.IEPD_NAME);
        assertNotNull(ProjectProperties.IEPD_VERSION);
        assertNotNull(ProjectProperties.LOG_DEBUG);
        assertNotNull(ProjectProperties.LOG_PROFILE);
    }
}
