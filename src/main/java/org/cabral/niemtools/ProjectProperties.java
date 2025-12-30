/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2025 James E. Cabral Jr., jim@cabral.org, http://github.com/cabralje
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * ProjectProperties is a specialized {@link Properties} class that manages configuration
 * properties for a NIEM tools project. It provides a set of predefined property keys
 * for import, export, and message specification settings, and synchronizes these properties
 * with a BOUML UML project.
 * <p>
 * The class supports loading properties from the associated UML project, storing properties
 * back to the project, and initializing with a set of default values.
 * </p>
 *
 * <h2>Property Categories:</h2>
 * <ul>
 *   <li><b>Import Properties:</b> Control how code descriptions, facets, and domains are imported.</li>
 *   <li><b>Export Properties:</b> Configure export options such as URIs, schema locations, and output formats (CMF, HTML, XSD, JSON, WSDL, OpenAPI).</li>
 *   <li><b>Message Specification Properties:</b> Define metadata for the IEPD (Information Exchange Package Documentation), such as name, version, organization, and contact information.</li>
 *   <li><b>Import/Export Paths:</b> Specify directories and filenames for importing reference models and exporting generated artifacts.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <ul>
 *   <li>Use {@link #getDefaults()} to obtain a Properties object with default values.</li>
 *   <li>Call {@link #load()} to populate properties from the UML project.</li>
 *   <li>Use {@link #store()} to persist properties back to the UML project.</li>
 *   <li>Setting a property via {@link #setProperty(String, String)} will also update the UML project.</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * The {@link #setProperty(String, String)} method is synchronized to ensure thread safety
 * when updating properties.
 * </p>
 *
 * @author James Cabral
 * @version 1.0
 */
package org.cabral.niemtools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import fr.bouml.UmlPackage;


public class ProjectProperties extends Properties {

    // import properties
    static final String IMPORT_NIEM_VERSION = "ImportNIEMVersion";
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
    static final String EXPORT_CMF_TO_XSD_MODEL = "ExportCMFtoXSDModel";
    static final String EXPORT_CMFTOOL_TO_XSD_MODEL = "ExportCMFTooltoXSDModel";
    static final String EXPORT_HTML = "ExportHTML";
    static final String EXPORT_XSD = "ExportXSD";
    static final String EXPORT_CMF_TO_XSD = "ExportCMFtoXSD";
    static final String EXPORT_CMFTOOL_TO_XSD = "ExportCMFTooltoXSD";
    static final String EXPORT_JSON = "ExportJSON";
    static final String EXPORT_CMF_TO_JSON = "ExportCMFtoJSON";
    static final String EXPORT_CMFTOOL_TO_JSON = "ExportCMFTooltoJSON";
    static final String EXPORT_WSDL = "ExportWSDL";
    static final String EXPORT_OPENAPI = "ExportOpenAPI";
    static final String EXPORT_ATTRIBUTION = "ExportAttribution";
    static final String EXPORT_CT_URI = "ExportConformanceTargetURI";
    static final String EXPORT_DATE = "ExportDate";     

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
    static final String EXPORT_HTML_DIR = "html dir";
    //static final String EXPORT_CMF_DIR = "ExportCMFDir";
    static final String EXPORT_XSD_MODEL_DIR = "ExportXSDModelDir";
    static final String EXPORT_CMF_FILE = "ExportCMFFile";
    static final String EXPORT_XSD_DIR = "ExportXSDDir";
    static final String EXPORT_XML_DIR = "ExportXMLDir";
    //static final String EXPORT_JSON_SCHEMA_DIR = "ExportJSONSchemaDir";
    static final String EXPORT_JSON_SCHEMA_FILE = "ExportJSONSchemaFile";
    static final String EXPORT_JSON_DIR = "ExportJSONDir"; 
    static final String EXPORT_WSDL_DIR = "ExportWSDLDir";
    static final String EXPORT_OPENAPI_DIR = "ExportOpenAPIDir";
    static final String EXPORT_MAPPING_FILE = "niem-mapping";
    static final String EXPORT_WANTLIST_FILE = "ExportWantlistFile";
    static final String EXPORT_CODELISTS_DIR = "ExportCodeListsDir";

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
        defaults.setProperty(IMPORT_NIEM_VERSION, "6.0");
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
        defaults.setProperty(EXPORT_CMF_VERSION, "1.0");
        defaults.setProperty(EXPORT_CMF,"true");
        defaults.setProperty(EXPORT_CMF_TO_XSD_MODEL,"true");
        defaults.setProperty(EXPORT_CMFTOOL_TO_XSD_MODEL,"cmftool.bat m2x -d -c -o");
        defaults.setProperty(EXPORT_HTML,"true");
        defaults.setProperty(EXPORT_XSD,"true");
        defaults.setProperty(EXPORT_CMF_TO_XSD,"true");
        defaults.setProperty(EXPORT_CMFTOOL_TO_XSD,"cmftool.bat m2xmsg -d -c -o");
        defaults.setProperty(EXPORT_JSON,"true");
        defaults.setProperty(EXPORT_CMF_TO_JSON,"true");
        defaults.setProperty(EXPORT_CMFTOOL_TO_JSON,"cmftool.bat m2jmsg -o");
        defaults.setProperty(EXPORT_WSDL,"true");
        defaults.setProperty(EXPORT_OPENAPI,"true");
        defaults.setProperty(EXPORT_ATTRIBUTION, "<!-- Generated by BOUML niemtools plug_out (https://github.com/cabralje/niem-tools) -->\n");
        defaults.setProperty(EXPORT_CT_URI, "https://docs.oasis-open.org/niemopen/ns/specification/conformanceTargets/6.0/");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        defaults.setProperty(EXPORT_DATE, dateFormat.format(new Date())); // default to today

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
        defaults.setProperty(EXPORT_HTML_DIR,"model\\html");
        //defaults.setProperty(EXPORT_CMF_DIR,"model\\cmf");
        defaults.setProperty(EXPORT_XSD_MODEL_DIR,"model\\xsd");
        defaults.setProperty(EXPORT_CMF_FILE,"model\\cmf\\model");
        defaults.setProperty(EXPORT_XSD_DIR,"xml\\schema");
        defaults.setProperty(EXPORT_XML_DIR,"xml\\examples");
        //defaults.setProperty(EXPORT_JSON_SCHEMA_DIR,"json\\schema");
        defaults.setProperty(EXPORT_JSON_SCHEMA_FILE,"json\\schema\\model");
        defaults.setProperty(EXPORT_JSON_DIR,"json\\examples");
        defaults.setProperty(EXPORT_WSDL_DIR,"xml\\wsdl");
        defaults.setProperty(EXPORT_OPENAPI_DIR,"json");
        defaults.setProperty(EXPORT_MAPPING_FILE,"model\\mapping\\niem-mapping.csv");
        defaults.setProperty(EXPORT_WANTLIST_FILE,"model\\mapping\\wantlist");
        defaults.setProperty(EXPORT_CODELISTS_DIR,"codelists");

        return defaults;
    }
    /**
     * Loads all properties from the BOUML project into this Properties object.
     */
    public void load() {
        @SuppressWarnings("unchecked")
        Hashtable<String, String> projectProperties = 
             (project != null && project.properties() != null)
            ? project.properties()
            : new Hashtable<>();
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