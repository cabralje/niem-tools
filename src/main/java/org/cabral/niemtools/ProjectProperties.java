package org.cabral.niemtools;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import fr.bouml.UmlPackage;

/**
 * Properties implementation that loads and stores properties from a BOUML UmlPackage project.
 */
public class ProjectProperties extends Properties {

    // import properties
    static final String IMPORT_CODE_DESCRIPTIONS = "ImportCodeDescriptions";
    static final String IMPORT_MAX_FACETS = "ImportMaxFacets";
    static final String IMPORT_INCLUDE_DOMAINS = "ImportIncludeDomains";
    static final String IMPORT_EXCLUDE_DOMAINS = "ImportExcludeDomains";
    static final String IMPORT_EXCLUDE_CODES = "ImportExcludeCodes";

    // generation properties
    static final String EXPORT_URI = "ExportURI";
    static final String EXPORT_EXTERNAL_SCHEMAS = "ExportExternalSchemas";
    static final String EXPORT_CMF_VERSION = "ExportCMFVersion";
    static final String EXPORT_CMF = "ExportCMF";
    static final String EXPORT_HTML = "ExportHTML";
    static final String EXPORT_XSD = "ExportXSD";
    static final String EXPORT_JSON = "ExportJSON";
    static final String EXPORT_WSDL = "ExportWSDL";
    static final String EXPORT_OPENAPI = "ExportOpenAPI";     

    // message specification properties
    static final String IEPD_NAME = "IEPDName";
    static final String IEPD_VERSION = "IEPDVersion";
    static final String IEPD_STATUS = "IEPDStatus";
    static final String IEPD_ORGANIZATION = "IEPDOrganization";
    static final String IEPD_CONTACT = "IEPDContact";
    static final String IEPD_EMAIL = "IEPDEmail";
    static final String IEPD_LICENSE_URL = "IEPDLicense";
    static final String IEPD_TERMS_URL = "IEPDTermsOfService";
    static final String IEPD_CHANGE_LOG_FILE = "IEPDChangeLogFile";
    static final String IEPD_READ_ME_FILE = "IEPDReadMeFile";
    static final String IEPD_CONFORMANCE_ASSERTION_FILE = "IEPDConformanceFile";
    static final String IEPD_CATALOG_FILE = "IEPDCatalogFile";  

    // import paths
    static final String IMPORT_REFERENCE_MODEL_DIR = "ImportReferenceModelDir";

    // export paths
    static final String EXPORT_PROJECT_DIR = "ExportProjectDir";
    static final String EXPORT_MODEL_DIR = "html dir";
    static final String EXPORT_CMF_DIR = "ExportCMFDir";
    static final String EXPORT_XSD_DIR = "ExportXSDDir";
    static final String EXPORT_XML_DIR = "ExportXMLDir";
    static final String EXPORT_JSON_SCHEMA_DIR = "ExportJSONSchemaDir";
    static final String EXPORT_JSON_DIR = "ExportJSONDir"; 
    static final String EXPORT_WSDL_DIR = "ExportWSDLDir";
    static final String EXPORT_OPENAPI_DIR = "ExportOpenAPIDir";
    static final String EXPORT_MAPPING_FILE = "niem-mapping";
    static final String EXPORT_WANTLIST_FILE = "ExportWantlistFile";

    //private static final long serialVersionUID = 1L;
    private final UmlPackage project;

    /**
     * Constructor that initializes the properties with the given defaults and loads
     * the properties from the BOUML project.
     *
     * @param project  The BOUML project to load properties from.
     * @param defaults The default properties to initialize with.
     */
    public ProjectProperties(UmlPackage project, Properties defaults) {
        super(defaults);
        this.project = project;
    }

    /**
     * Initializes the properties with the default values.
     *
     * @return defaults
     */ 
    public static Properties getDefaults() {

        // import defaults
        Properties defaults = new Properties();
        defaults.setProperty(IMPORT_CODE_DESCRIPTIONS, "true");
        defaults.setProperty(IMPORT_MAX_FACETS, "300");
        defaults.setProperty(IMPORT_INCLUDE_DOMAINS, "justice, hs");
        defaults.setProperty(IMPORT_EXCLUDE_DOMAINS, "");
        defaults.setProperty(IMPORT_EXCLUDE_CODES, "");

        // export defaults
        defaults.setProperty(EXPORT_URI, "http://local");
        defaults.setProperty(EXPORT_EXTERNAL_SCHEMAS, "cac=urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonAggregateComponents-2.1.xsd,"
            + "cbc=urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonBasicComponents-2.1.xsd,"
            + "ds=http://www.w3.org/2000/09/xmldsig#=https://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd" );
        defaults.setProperty(EXPORT_CMF_VERSION, "0.8");
        defaults.setProperty(EXPORT_CMF,"true");
        defaults.setProperty(EXPORT_HTML,"true");
        defaults.setProperty(EXPORT_XSD,"true");
        defaults.setProperty(EXPORT_JSON,"true");
        defaults.setProperty(EXPORT_WSDL,"true");
        defaults.setProperty(EXPORT_OPENAPI,"true");

        // message specification defaults
        defaults.setProperty(IEPD_NAME, "NIEM Message Specification");
        defaults.setProperty(IEPD_VERSION, "1.0");
        defaults.setProperty(IEPD_STATUS, "Draft");
        defaults.setProperty(IEPD_ORGANIZATION, "Organization Name");
        defaults.setProperty(IEPD_CONTACT, "Contact Name");
        defaults.setProperty(IEPD_EMAIL, "email@example.com");
        defaults.setProperty(IEPD_LICENSE_URL, "https://opensource.org/licenses/BSD-3-Clause");
        defaults.setProperty(IEPD_TERMS_URL, "example.com/terms");
        defaults.setProperty(IEPD_CHANGE_LOG_FILE, "changelog.txt");
        defaults.setProperty(IEPD_READ_ME_FILE, "readme.txt");
        defaults.setProperty(IEPD_CONFORMANCE_ASSERTION_FILE, "conformance-assertion.pdf");
        defaults.setProperty(IEPD_CATALOG_FILE, "mpd-catalog.xml");

        // import paths
        defaults.setProperty(IMPORT_REFERENCE_MODEL_DIR,"");

        // export paths
        defaults.setProperty(EXPORT_PROJECT_DIR, System.getProperty("user.home"));
        defaults.setProperty(EXPORT_MODEL_DIR,"model");
        defaults.setProperty(EXPORT_CMF_DIR,"cmf");
        defaults.setProperty(EXPORT_XSD_DIR,"schema");
        defaults.setProperty(EXPORT_XML_DIR,"examples");
        defaults.setProperty(EXPORT_JSON_SCHEMA_DIR,"json\\schema");
        defaults.setProperty(EXPORT_JSON_DIR,"json\\examples");
        defaults.setProperty(EXPORT_WSDL_DIR,"WS-SIP");
        defaults.setProperty(EXPORT_OPENAPI_DIR,"json");
        defaults.setProperty(EXPORT_MAPPING_FILE,"niem-mapping.csv");
        defaults.setProperty(EXPORT_WANTLIST_FILE,"schema\\wantlist");

        return defaults;
    }
    /**
     * Loads all properties from the BOUML project into this Properties object.
     */
    public void load() {
        Hashtable<String,String> projectProperties = project.properties();
        for (String propertyName : projectProperties.keySet()) {
            String value = projectProperties.get(propertyName);
            if (value != null)
                this.setProperty(propertyName, value);
        }
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        project.set_PropertyValue(key, value);
        return put(key, value);
    }

    /**
     * Stores all properties from this Properties object into the BOUML project.
     */
    public void store() {
        Enumeration<?> names = this.propertyNames();
        while (names.hasMoreElements()) {
            String propertyName = (String) names.nextElement();
            String value = this.getProperty(propertyName);
            project.set_PropertyValue(propertyName, value);
        }
    }
}