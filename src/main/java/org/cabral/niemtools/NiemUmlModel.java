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
 * The NiemUmlModel class provides methods and utilities for managing NIEM (National Information Exchange Model)
 * UML models, including reference, subset, and extension models. It supports importing and exporting NIEM mapping
 * spreadsheets, generating NIEM-compliant schemas and documentation, and managing stereotypes and properties
 * associated with NIEM elements and types.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Importing NIEM reference schemas and mapping spreadsheets (CSV format).</li>
 *   <li>Exporting NIEM mapping spreadsheets (CSV/HTML), wantlists, and specification artifacts (XSD, JSON, WSDL, OpenAPI, CMF).</li>
 *   <li>Creating and deleting NIEM subset and extension models within a UML project.</li>
 *   <li>Managing stereotypes, code lists, and facets for NIEM UML items.</li>
 *   <li>Supporting validation and lookup of NIEM elements and types.</li>
 *   <li>Generating HTML documentation for UML models.</li>
 * </ul>
 * <p>
 * This class relies on the BoUML Java API and several supporting classes (e.g., NiemModel, NamespaceModel, UmlItem, UmlClass, etc.).
 * It is intended for use in tools that automate the creation, validation, and export of NIEM-conformant UML models.
 *
 * <p>
 * Usage typically involves:
 * <ol>
 *   <li>Importing NIEM reference schemas using {@link #importSchemaDir(String)}.</li>
 *   <li>Creating NIEM subset and extension models with {@link #createSubsetAndExtension()}.</li>
 *   <li>Exporting mappings and specifications using the various export methods.</li>
 *   <li>Managing stereotypes and properties for UML items as needed.</li>
 * </ol>
 *
 * <p>
 * Note: Many methods are static and operate on shared model instances for reference, subset, and extension models.
 * Project-specific properties are managed via the {@link ProjectProperties} class.
 *
 * @author James Cabral
 * @version 6.0
 */
package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
//import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlClassView;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlOperation;
import fr.bouml.UmlPackage;
import fr.bouml.UmlParameter;
import fr.bouml.UmlRelation;
import fr.bouml.UmlTypeSpec;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class NiemUmlModel {

    // NIEM defaults
    private static final String NIEM_VERSION_DEFAULT = "6.0";
    private static final String WANTLIST_URI = "http://niem.gov/niem/wantlist/2.2";
    static final String NIEM_DIR = "niem";

    // NIEM UML modeling
    private static final String NIEM_PACKAGE = "NIEM";
    private static final String NIEM_REFERENCE_PACKAGE = "NIEMReference";
    private static final String NIEM_SUBSET_PACKAGE = "NIEMSubset";
    private static final String NIEM_EXTENSION_PACKAGE = "NIEMExtension";
    static final String MAPPING_SPREADSHEET_TITLE = "NIEM Mapping";
    private static final String REFERENCE_PREFIX = "^";

    // niem stereotypes
    private static final String NIEM_STEREOTYPE = "niem-profile:niem";
    static final String ENUM_STEREOTYPE = "enum_pattern";
    static final String FACET_STEREOTYPE = "attribute";
    static final String WEBSERVICE_STEREOTYPE = "niem-profile:webservice";
    static final String INTERFACE_STEREOTYPE = "niem-profile:interface";
    static final String HTTP_METHODS_PROPERTY = WEBSERVICE_STEREOTYPE + NiemUmlModel.STEREOTYPE_DELIMITER + "HTTPMethods";
    static final String INTERFACE_PATH_PROPERTY = INTERFACE_STEREOTYPE + NiemUmlModel.STEREOTYPE_DELIMITER + "Path";

    // niem stereotype properties
    static final String URI_PROPERTY = "URI";
    static final String NOTES_PROPERTY = "Notes";
    static final String NILLABLE_PROPERTY = "isNillable";
    static final String PREFIX_PROPERTY = "prefix";
    static final String SUBSTITUTION_PROPERTY = "substitutesFor";
    static final String SUBSTITUTION_TYPE_PROPERTY = "substitutesIn";
    static final String SUBSTITUTION_MULTIPLICITY_PROPERTY = "subMultiplicity";
    static final String CODELIST_PROPERTY = "codeList";
    static final String FILE_PATH_PROPERTY = "path";
    //static final String FACETS_PROPERTY = "facets";
    static final String TRUNCATED_PROPERTY = "isTruncated";
    static final String MESSAGE_ELEMENT_PROPERTY = "messageElement";
    static final String STEREOTYPE_DELIMITER = ":";

    // NIEM mapping spreadsheet column headings, NIEM profile profile stereotype
    private static final String[][] NIEM_STEREOTYPE_MAP = {{"Model Class", "",}, // 0
    {"Model Attribute", "",}, // 1
    {"Model Type", "",}, // 2
    {"Model Multiplicity", "",}, // 3
    {"Model Definition", "",}, // 4
    {"NIEM XPath", "XPath"}, // 5
    {"NIEM Type", "Type"}, // 6
    {"NIEM Property, " + REFERENCE_PREFIX + "Reference, (Representation)", "Property"}, // 7
    {"NIEM Base Type", "BaseType"}, // 8
    {"NIEM Multiplicity", "Multiplicity"}, // 9
    {"Old XPath", "OldXPath"}, // 10
    {"Old Multiplicity", "OldMultiplicity"}, // 11
    {"NIEM Mapping Notes", "Notes"}, // 12
    {"Code List Code=Definition;", "CodeList"}}; // 13
    // private static final String NIEM_STEREOTYPE_CLASS = getNiemProperty(0);
    // private static final String NIEM_STEREOTYPE_ATTRIBUTE = getNiemProperty(1);
    // private static final String NIEM_STEREOTYPE_ATTRIBUTETYPE = getNiemProperty(2);
    // private static final String NIEM_STEREOTYPE_MODEL_MULTIPLICITY = getNiemProperty(3);
    // private static final String NIEM_STEREOTYPE_DEFINITION = getNiemProperty(4);
    static final String NIEM_STEREOTYPE_XPATH = getNiemProperty(5);
    private static final String NIEM_STEREOTYPE_TYPENAME = getNiemProperty(6);
    static final String NIEM_STEREOTYPE_PROPERTY = getNiemProperty(7);
    private static final String NIEM_STEREOTYPE_BASE_TYPE = getNiemProperty(8);
    private static final String NIEM_STEREOTYPE_MULTIPLICITY = getNiemProperty(9);
    // private static final String NIEM_STEREOTYPE_OLD_XPATH = getNiemProperty(10);
    // private static final String NIEM_STEREOTYPE_OLD_MULTIPLICITY = getNiemProperty(11);
    private static final String NIEM_STEREOTYPE_NOTES = getNiemProperty(12);
    private static final String NIEM_STEREOTYPE_CODE_LIST = getNiemProperty(13);

    // NIEM import pass
    private static int importPass;

    // NIEM models
    private static final NiemModel ReferenceModel = new NiemModel();
    private static final NiemModel SubsetModel = new NiemModel();
    private static final NiemModel ExtensionModel = new NiemModel();

    // globals
    //final UmlPackage project;
    final ProjectProperties properties;

    /**
     * @return the NIEM Extension Model as a NiemModel
     */
    static NiemModel getExtensionModel() {
        return ExtensionModel;
    }

    /**
     * @param element
     * @return codelist associated with the element
     */
    static String getCodeList(UmlItem item) {
        return item.propertyValue(CODELIST_PROPERTY);
    }

    /**
     * @param multiplicity
     * @return XML maxOccurs from multiplicity as a String
     */
    static String getMaxOccurs(String multiplicity) {
        String maxOccurs;
        if (multiplicity.isEmpty())
            maxOccurs = "1"; 
        else if (multiplicity.contains(","))
            maxOccurs = multiplicity.split(",")[1]; 
        else
            maxOccurs = multiplicity;
        try {
            if (!maxOccurs.equals("unbounded") && (Integer.parseInt(maxOccurs) < 1))
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Log.trace("getMaxOccurs: error - invalid multiplicity " + multiplicity);
        }
        return maxOccurs;
    }

    /**
     * @param multiplicity
     * @return XML minOccurs from multiplicity as a String
     */
    static String getMinOccurs(String multiplicity) {
        String minOccurs;
        if (multiplicity.isEmpty())
            minOccurs = "1"; 
        else if (multiplicity.contains(","))
            minOccurs = multiplicity.split(",")[0]; 
        else
            minOccurs = multiplicity;
        try {
            if (Integer.parseInt(minOccurs) < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Log.trace("getMinOccurs: error - invalid multiplicity " + multiplicity);
        }
        return minOccurs;
    }

    /**
     * @param uri
     * @return model for element uri as a NiemModel
     */
    static NiemModel getModel(String uri) {
        return (SubsetModel.getElementByURI(uri) != null) ? SubsetModel : ExtensionModel;
    }

    /**
     * @param item
     * @return model for Umlitem item as a NiemModel
     */
    static NiemModel getModel(UmlItem item) {
        //UmlPackage modelPackage = null;
        String modelPackageName = null;
        anItemKind kind = item.kind();
        if (kind == anItemKind.aClass || kind == anItemKind.aClassInstance)
            modelPackageName = item.parent().parent().name(); 
        else if (kind == anItemKind.aClassView)
            modelPackageName = item.parent().name();
        if (modelPackageName != null)
            switch (modelPackageName) {
                case NIEM_REFERENCE_PACKAGE -> {
                    return ReferenceModel;
            }
                case NIEM_SUBSET_PACKAGE -> {
                    return SubsetModel;
            }
                case NIEM_EXTENSION_PACKAGE -> {
                    return ExtensionModel;
            }
                default -> Log.trace("getModel - error - no model for " + item.name());
            }
        //if (modelPackage == ReferenceModel.getModelPackage())
        //    return ReferenceModel; 
        //else if (modelPackage == SubsetModel.getModelPackage())
        //    return SubsetModel; 
        //else if (modelPackage == ExtensionModel.getModelPackage())
        //    return ExtensionModel;
        //Log.trace("getModel - error - no model for " + item.name());
        return null;
    }

    /**
     * @return the columns of the NIEM mapping spreadsheet
     */
    static String[][] getNiemMap() {
        return NIEM_STEREOTYPE_MAP;
    }

    /**
     * @param p
     * @return the NIEM stereotype associated with a column in the NIEM mapping
     * spreadsheet as a String
     */
    static String getNiemProperty(int p) {
        return NIEM_STEREOTYPE + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[p][1];
    }

    /**
     * @param propertyName
     * @return return project property with name propertyName as a String
     */
    static String getProperty(String propertyName) {
        return UmlPackage.getProject().propertyValue(propertyName);
    }

    /**
     * @return the NIEM Reference Model as a NiemModel
     */
    static NiemModel getReferenceModel() {
        return ReferenceModel;
    }

    /**
     * @return the NIEM Subset Model as a NiemModel
     */
    static NiemModel getSubsetModel() {
        return SubsetModel;
    }

    /**
     * hide item from documentation
     *
     * @param item
     */
    private static void hideItem(UmlItem item) {
        item.known = false;
        for (UmlItem child : item.children())
            hideItem(child);
    }

    /**
     * hide reference model from documentation
     *
     */
    public static void hideReferenceModel() {
        UmlItem item = ReferenceModel.getModelPackage();
        if (item == null)
            return; 
        hideItem(ReferenceModel.getModelPackage());
    }

    /**
     * @param elementName
     * @return true if an element exists in reference model
     */
    static Boolean isNiemElement(String elementName) {
        if ((elementName == null) || elementName.isEmpty() || elementName.equals("??")
                || NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(elementName)))
            return false;
        String schemaURI = NamespaceModel.getSchemaURIForPrefix(NamespaceModel.getPrefix(elementName));
        if (schemaURI == null)
            return false;
        return ReferenceModel.getElementByURI(NiemModel.getURI(schemaURI, elementName)) != null;
    }

    /**
     * @param typeName
     * @param elementName
     * @return true if an element in type exists in reference model
     */
    static Boolean isNiemElementInType(String typeName, String elementName) {
        if (!isNiemType(typeName) || !isNiemElement(elementName))
            return false;
        UmlClassInstance element = ReferenceModel.getElement(NamespaceModel.getSchemaURI(elementName), elementName);
        List<UmlClassInstance> elementList = ReferenceModel.getElementsInType(NiemModel.getURI(NamespaceModel.getSchemaURI(typeName), typeName));
        for (UmlClassInstance element2 : elementList)
            if (element.equals(element2))
                return true;
        Log.trace("isNiemElementInType: error - element " + elementName + " not in type " + typeName);
        return false;
    }

    /**
     * @param typeName
     * @return true if type exists in reference model
     */
    static Boolean isNiemType(String typeName) {
        if ((typeName == null) || typeName.isEmpty() || typeName.equals("??") || NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(typeName)))
            return false;
        String schemaURI = NamespaceModel.getSchemaURIForPrefix(NamespaceModel.getPrefix(typeName));
        if (schemaURI == null)
            return false;
        return ReferenceModel.getTypeByURI(NiemModel.getURI(schemaURI, typeName)) != null;
    }

    /**
     * @param item
     * @return true if item exists in reference model
     */
    static Boolean isNiem(UmlItem item) {
        String prefixedName = NamespaceModel.getPrefixedName(item);
        if (item.kind() == anItemKind.aClass)
            return isNiemType(prefixedName); 
        else if (item.kind() == anItemKind.aClassInstance)
            return isNiemElement(prefixedName); 
        else
            return false;
    }

        /**
     * @param item
     * @return true if item has the NIEM stereotype
     */
    static Boolean isNiemUml(UmlItem item) {
        if (item == null)
            return false;
        String stereotype = item.stereotype();
        if (stereotype == null)
            return false;
        return (stereotype.equals(NIEM_STEREOTYPE));
    }

    /**
     * initialize NiemTools project
     *
     */
    public NiemUmlModel(UmlPackage project, ProjectProperties properties) {
        super();
        //this.project = project;
        this.properties = properties;
    }

    /**
     * caches namespaces and prefixes for external schemas
     *
     * @param referenceOnly
     */
    public void cacheModels(boolean referenceOnly) {

        Log.start("cacheModels");
        UmlCom.message("Caching models ...");
        Log.trace("Caching models");
        try {
            NamespaceModel.cacheExternalSchemas();
            ReferenceModel.cacheModel();
            if (!referenceOnly) {
                SubsetModel.cacheModel();
                ExtensionModel.cacheModel();
            }
        } catch (Exception e) {
            Log.trace("cacheModels: error" + e.toString());
        }
        Log.debug("Done caching models");
        Log.stop("cacheModels");
    }

    /**
     * creates Platform Independent Model (NIEM)
     *
     */
    public void createNIEM() {
        UmlCom.message("Resetting NIEM models");
        Log.trace("Resetting NIEM models");
        UmlPackage pimPackage;
        // Find or create NIEM packages
        pimPackage = getPackage(UmlPackage.getProject(), NIEM_PACKAGE, true);
        SubsetModel.setModelPackage(getPackage(pimPackage, NIEM_SUBSET_PACKAGE, true));
        ExtensionModel.setModelPackage(getPackage(pimPackage, NIEM_EXTENSION_PACKAGE, true));
        ReferenceModel.setModelPackage(getPackage(pimPackage, NIEM_REFERENCE_PACKAGE, true));
        ReferenceModel.getModelPackage().set_Stereotype("framework");
    }

    /**
     * creates NIEM subset and extension models
     *
     */
    // TODO createSubsetAndExtension: ensure all NIEM types and elements are in the reference model
    // TODO allow namespaces with a "-" in the prefix (not supported by UmlAttribute)
    //@SuppressWarnings("unchecked")
    @SuppressWarnings("unchecked")
    public void createSubsetAndExtension() {

        Log.start("createSubsetAndExtension");
        UmlCom.message("Generating NIEM subset and extension models");
        Log.trace("Generating NIEM subset and extension models");

        Log.start("createSubsetAndExtension - add types");
        
        // add types to subset and extension
        Log.debug("createSubsetAndExtension: copy subset types and create extension types");
        java.util.Vector<UmlItem> all = (java.util.Vector<UmlItem>) UmlItem.all;
        for (UmlItem item : all) {
            if (!isNiemUml(item))
                continue;

            String baseTypeName = item.propertyValue(NIEM_STEREOTYPE_BASE_TYPE).trim();
            if (NiemModel.isAbstract(baseTypeName))
                continue;

            Log.debug("createSubsetAndExtension: " + item.name());
            String typeName = item.propertyValue(NIEM_STEREOTYPE_TYPENAME).trim();
            String elementName = item.propertyValue(NIEM_STEREOTYPE_PROPERTY).trim();
            String notes = item.propertyValue(NIEM_STEREOTYPE_NOTES).trim();

            Log.debug("createSubsetAndExtension: adding type " + typeName + " and base type " + baseTypeName);
            String description = null;

            // skip elements in types
            if (elementName.isEmpty())
                if (NiemModel.isAugmentation(typeName))
                    description = "An augmentation type";
                else 
                    description = item.description().trim();

            // add base type
            if (!baseTypeName.isEmpty())
                if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName))) {
                    if (SubsetModel.copyType(baseTypeName) == null)
                        Log.trace("createSubsetAndExtension: error - base type " + baseTypeName + " not found in reference model");
                } else {
                    if (ExtensionModel.addType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName, null, null) == null)
                        Log.trace("createSubsetAndExtension: error - cannot add extension base type " + baseTypeName);
                }

            // add type
            if (!typeName.isEmpty())
                if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName))) {
                    if (SubsetModel.copyType(typeName) == null)
                        Log.trace("createSubsetAndExtension: error - type " + typeName + " not found in reference model");
                } else {
                    if (ExtensionModel.addType(NamespaceModel.getSchemaURI(typeName), typeName, null, null) == null)
                        Log.trace("createSubsetAndExtension: error - cannot add extension type " + typeName);
                }
        }
        Log.stop("createSubsetAndExtension - add types");
        Log.start("createSubsetAndExtension - add base types");

        // relate extension types to base types and attribute groups
        Log.debug("createSubsetAndExtension: copy subset base types and create extension base types");
        Iterator<UmlItem> it = UmlItem.all.iterator();
        while (it.hasNext()) {
            UmlItem item = it.next();
            if (!isNiemUml(item))
                continue;

            String typeName = item.propertyValue(NIEM_STEREOTYPE_TYPENAME).trim();
            if (typeName.isEmpty() || isNiemType(typeName))
                continue;

            String elementName = item.propertyValue(NIEM_STEREOTYPE_PROPERTY).trim();
            String baseTypeName = item.propertyValue(NIEM_STEREOTYPE_BASE_TYPE).trim();

            NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? SubsetModel : ExtensionModel;
            UmlClass type = model.getType(NamespaceModel.getSchemaURI(typeName), typeName);
            if (type == null)
                continue;

            UmlClass baseType;
            if (baseTypeName.isEmpty() && NiemModel.isAugmentation(typeName))
                baseType = SubsetModel.getAugmentationType(); 
            else {
                if (!elementName.isEmpty())
                    continue;
                if (baseTypeName.isEmpty()) {
                    Log.trace("createSubsetAndExtension: warning - base type not defined for type " + typeName
                            + "; using default base type.");
                    baseType = SubsetModel.getObjectType();
                } else {
                    NiemModel baseModel = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName)) ? SubsetModel : ExtensionModel;
                    baseType = baseModel.getType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName);
                }
                if (baseType == null) {
                    Log.trace("createSubsetAndExtension: base type " + baseTypeName + " not found.");
                    continue;
                }
            }

            // relate type to base type
            ExtensionModel.relateBaseType(type, baseType);

            // If type is based on simple type, add attribute group
            String baseTypePrefix = NamespaceModel.getPrefix(baseType);
            if (baseTypePrefix != null && baseTypePrefix.equals(NiemModel.XSD_PREFIX))
                ExtensionModel.relateAttributeGroup(type, SubsetModel.getSimpleObjectAttributeGroup());
        }
        Log.stop("createSubsetAndExtension - add base types");
        
        // Copy subset elements and create extension elements
        Log.start("createSubsetAndExtension - add elements");
        Log.debug("createSubsetAndExtension: copy subset elements and create extension elements");
        it = UmlItem.all.iterator();
        while (it.hasNext()) {
            UmlItem item = it.next();
            if (!isNiemUml(item))
                continue;

            String typeName = item.propertyValue(NIEM_STEREOTYPE_TYPENAME).trim();
            String elementList = item.propertyValue(NIEM_STEREOTYPE_PROPERTY).trim();
            String baseTypeName = item.propertyValue(NIEM_STEREOTYPE_BASE_TYPE).trim();
            String multiplicity = item.propertyValue(NIEM_STEREOTYPE_MULTIPLICITY).trim();
            String description = item.description().trim();
            String mappingNotes = item.propertyValue(NIEM_STEREOTYPE_NOTES).trim();
            String codeList = item.propertyValue(NIEM_STEREOTYPE_CODE_LIST).trim();

            boolean substitution = elementList.contains("(");
            String headElement = null;
            String[] elementNames = elementList.split(",");
            for (String elementName : elementNames) {
                boolean representation = false;
                if (elementName.contains("(")) {
                    elementName = elementName.replaceAll("\\(|\\)", "");
                    representation = true;
                }
                elementName = elementName.trim();
                if (elementName.isEmpty())
                    continue;

                if (elementName.contains("Augmentation") && description.isEmpty())
                    description = "An augmentation";

                Boolean isReference = elementName.startsWith(REFERENCE_PREFIX);
                if (isReference) 
                    elementName = elementName.substring(1);

                // trace("createSubsetAndExtension: adding element " + elementName + " in type " + typeName);
                if (substitution && !representation && headElement == null) 
                    headElement = elementName;

                String baseTypeName2 = baseTypeName;
                if (NiemModel.isAbstract(baseTypeName) || (substitution && !representation))
                    baseTypeName2 = NamespaceModel.getPrefixedName(NiemModel.LOCAL_PREFIX, NiemModel.ABSTRACT_TYPE_NAME);

                // copy NIEM element or add extension element
                NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName2)) ? SubsetModel : ExtensionModel;
                UmlClass baseType = model.getType(NamespaceModel.getSchemaURI(baseTypeName2), baseTypeName2);
                if (baseType == null && !baseTypeName.isEmpty())
                    Log.trace("createSubsetAndExtension: error - base type " + baseTypeName2 + " not in model");
                
                UmlClassInstance element = null;
                if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(elementName))) {
                    element = SubsetModel.copyElement(elementName);
                    if (element == null)
                        Log.trace("createSubsetAndExtension: error - element " + elementName + " not found in reference model");
                } else {
                    element = ExtensionModel.addElement(NamespaceModel.getSchemaURI(elementName), elementName, baseType, description, mappingNotes);
                    if (element == null)
                        Log.trace("createSubsetAndExtension: error - cannot add extension element " + elementName);
                }
                if (element == null)
                    continue;

                // copy element in type
                if ((!substitution || !representation) && !typeName.isEmpty()) {
                    UmlClass type = null;
                    if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName))) {
                        type = SubsetModel.getType(NamespaceModel.getSchemaURI(typeName), typeName);
                        if (type == null || SubsetModel.copyElementInType(type, element, multiplicity) == null)
                                Log.trace("createSubsetAndExtension: error - reference element " + elementName + " not in type " + typeName);
                    } else {
                        type = ExtensionModel.getType(NamespaceModel.getSchemaURI(typeName), typeName);
                        if (type == null || ExtensionModel.addElementInType(type, element, multiplicity)== null)
                            Log.trace("createSubsetAndExtension: error - extension element " + elementName + " not in type " + typeName);
                    }
                }

                if (isReference) 
                    element.set_PropertyValue(NILLABLE_PROPERTY, "true");
                
                if (headElement != null && substitution && representation) {
                    element.set_PropertyValue(SUBSTITUTION_PROPERTY, headElement);
                    element.set_PropertyValue(SUBSTITUTION_TYPE_PROPERTY, typeName);
                    element.set_PropertyValue(SUBSTITUTION_MULTIPLICITY_PROPERTY, multiplicity);
                }
                if (codeList != null && !codeList.isEmpty() && (!substitution || representation)) 
                    setCodeList(element, codeList);

            }
        }
        Log.stop("createSubsetAndExtension - add elements");

        // Sorting
        UmlPackage subset = SubsetModel.getModelPackage();
        if (subset != null) {
            Log.trace("createSubsetAndExtension: sorting subset model");
        	sort(subset);
        }
        UmlPackage extension = ExtensionModel.getModelPackage();
        if (extension != null) {
            Log.trace("createSubsetAndExtension: sorting extension model");
            sort(extension);
        }
        Log.stop("createSubsetAndExtension");
    }

    /**
     * deletes NIEM mappings
     *
     */
    public void deleteMapping() {
        Log.trace("Deleting NIEM Mapping");
        @SuppressWarnings("unchecked")
        java.util.Vector<UmlItem> all = (java.util.Vector<UmlItem>) UmlItem.all;
        for (UmlItem item : all) {
            if (isNiemUml(item) && item.kind() != anItemKind.aClassInstance)
                for (int property = 4; property < NIEM_STEREOTYPE_MAP.length; property++)
                    item.set_PropertyValue(getNiemProperty(property), "");
        }
    }

    /**
     * deletes NIEM models
     *
     * @param deleteReferenceModel
     */
    public void deleteNIEM(boolean deleteReferenceModel) {

        // Find NIEM package
        UmlPackage pimPackage = getPackage(UmlPackage.getProject(), NIEM_PACKAGE, true);
        UmlPackage modelPackage;

        // delete reference model
        if (deleteReferenceModel) {
            Log.debug("deleteSubset: deleting reference model");
            modelPackage = getPackage(pimPackage, NIEM_REFERENCE_PACKAGE, false);
            //ReferenceModel.setModelPackage(modelPackage);
            if (modelPackage != null) {
                modelPackage.deleteIt();
                modelPackage.unload(true, true);
            }
        }

        // delete subset and extension models
        Log.debug("deleteSubset: deleting subset and extension model");
        modelPackage = getPackage(pimPackage, NIEM_SUBSET_PACKAGE, false);
        //SubsetModel.setModelPackage(modelPackage);
        if (modelPackage != null) {
            modelPackage.deleteIt();
            modelPackage.unload(true, true);
        }

        modelPackage = getPackage(pimPackage, NIEM_EXTENSION_PACKAGE, false);
        //ExtensionModel.setModelPackage(modelPackage);
        if (modelPackage != null) {
            modelPackage.deleteIt();
            modelPackage.unload(true, true);
        }
    }

    /**
     * @param target
     * @return generates HTML documentation for the target item
     */
    public void exportHtml(UmlItem target) {
        String modelDir = properties.getProperty(ProjectProperties.EXPORT_HTML_DIR);
        String exportHtml = properties.getProperty(ProjectProperties.EXPORT_HTML);
        String startFile = "index";

        // Verify model directory exists
        Path modelPath = Paths.get(modelDir);
        if (!Files.exists(modelPath)) {
            Log.debug("exportHtml: model directory does not exist, creating " + modelDir);
            try {
                Files.createDirectories(modelPath);
            } catch (IOException e) {
                Log.trace("exportHtml: error creating model directory " + e.toString());
                return;
            }
        }
        // Generate UML Model HTML documentation
        if (exportHtml.equals("true")) {
            Log.trace("Generating HTML documentation at " + modelDir + "\\" + startFile + ".html");
            //	target.set_dir(argv.length - 1, argv);
            //String[] params = {root.propertyValue("html dir")};
            String[] params = {modelDir};
            target.set_dir(1, params);
            //target.set_dir(0,null);
            try {
                UmlItem.frame();
                UmlCom.message("Indexes ...");
                Log.start("generate_indexes");
                UmlItem.generate_indexes();
                Log.stop("generate_indexes");
                UmlItem.start_file(startFile, target.name() + "\nDocumentation", false);
                target.html(null, 0, 0);
                UmlItem.end_file();
                UmlItem.start_file("navig", null, true);
                UmlItem.end_file();
                Log.start("generate");
                UmlClass.generate();
            } catch (IOException e) {
                Log.trace("exportHtml: error generating HTML documentation " + e.toString());
            }
            Log.stop("generate");
        }
    }
    /**
     * exports a NIEM mapping spreadsheet in CSV format roundtripping is
     * supported with importCsv()
     *
     * @param directory
     * @param filename
     */
    public void exportMappingCsv() {
        Log.start("exportCsv");

        //String directory = properties.getProperty(ProjectProperties.EXPORT_MODEL_DIR);
        String filename = properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE);

        //Verify directory exists
        Path modelPath = Paths.get(filename).getParent();
        if (!Files.exists(modelPath)) {
            Log.debug("exportCsv: model directory does not exist, creating " + modelPath);
            try {
                Files.createDirectories(modelPath);
            } catch (IOException e) {
                Log.trace("exportCsv: error creating model directory " + e.toString());
                return;
            }
        }

        UmlCom.message("Generating NIEM Mapping CSV ...");
        Log.trace("Generating NIEM Mapping CSV at " + filename);
        NamespaceModel.cacheExternalSchemas();

        //UmlItem.directory = directory;
        Log.debug("exportCsv: generating CSV");
        try {
            new CsvWriter().exportCsv(filename);
        } catch (Exception e) {
            Log.trace("exportCsv: error generating CSV" + e.toString());
        }
        Log.debug("exportCsv: finished generating CSV");
        Log.stop("exportCsv");
    }

    /**
     * exports a NIEM mapping spreadsheet in HTML format
     *
     * @param directory
     * @param filename
     */
    public void exportMappingHtml() {
        Log.start("exportHtml");

        //String directory = properties.getProperty(ProjectProperties.EXPORT_MODEL_DIR);
        String filename = properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE).replace(".csv",".html");
        HtmlWriter htmlWriter = new HtmlWriter();

        // Verify directory exists
        Path modelPath = Paths.get(filename).getParent();
        if (!Files.exists(modelPath)) {
            Log.debug("exportHtml: model directory does not exist, creating " + modelPath);
            try {
                Files.createDirectories(modelPath);
            } catch (IOException e) {
                Log.trace("exportHtml: error creating model directory " + e.toString());
                return;
            }
        }
        UmlCom.message("Generating NIEM Mapping HTML ...");
        Log.trace("Generating NIEM Mapping HTML at " + filename);
        NamespaceModel.cacheExternalSchemas();
        // cache NIEM namespaces, elements and types
        // cacheModel(referencePackage);
        htmlWriter.exportMappingHtml(filename);
        Log.stop("exportHtml");
    }

    /**
     * exports a NIEM IEPD including extension and exchange schema
     *
     * @param xmlDir
     * @param wsdlDir
     * @param jsonDir
     * @param openapiDir
     */
    //@SuppressWarnings("unchecked")
    public void exportSpecification() {

        Log.start("exportSpecification");
        Log.trace("Generating NIEM message specification");
        /*
		 * cacheExternalSchemas(); cacheModel(referencePackage);
		 * cacheModel(subsetPackage); cacheModel(extensionPackage);
         */
        // export code lists for extension elements
        String exportCmf = properties.getProperty(ProjectProperties.EXPORT_CMF);
        String exportXsd = properties.getProperty(ProjectProperties.EXPORT_XSD);
        String exportJson = properties.getProperty(ProjectProperties.EXPORT_JSON);
        String exportCmfToXsd = properties.getProperty(ProjectProperties.EXPORT_CMF_TO_XSD);
        String exportCmfToJson = properties.getProperty(ProjectProperties.EXPORT_CMF_TO_JSON);
        String exportWsdl = properties.getProperty(ProjectProperties.EXPORT_WSDL);
        String exportOpenApi = properties.getProperty(ProjectProperties.EXPORT_OPENAPI);

        String xmlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                        properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);

        // Verify XML directory exists
        Path xmlPath = Paths.get(xmlDir);
        if (!Files.exists(xmlPath)) {
            Log.debug("exportSpecification: XML directory does not exist, creating " + xmlDir);
            try {
                Files.createDirectories(xmlPath);
            } catch (IOException e) {
                Log.trace("exportSpecification: error creating XML directory " + e.toString());
                return;
            }
        }
        XmlWriter xmlWriter = new XmlWriter(xmlDir);

        if (exportXsd.equals("true")){
            xmlWriter.exportCodeLists(ExtensionModel);
            xmlWriter.exportCodeLists(SubsetModel);
        }

        try {
            if (exportXsd.equals("true") && exportCmfToXsd.equals("false")) {
                // export catalog file
                xmlWriter.exportXmlCatalog();
            }
        } catch (IOException e) {
            Log.trace("exportSpecification: error creating XML catalog file " + e.toString());
        }

        // cache list of ports and message elements
        Log.debug("exportSpecification: cache ports and message elements");
        Map<String, UmlClass> ports = new TreeMap<>();
        Map<String, UmlClassInstance> messages = new TreeMap<>();
        Set<String> messageNamespaces = new TreeSet<>();
        messageNamespaces.add(NiemModel.XSD_PREFIX);
        @SuppressWarnings("unchecked")
        Iterator<UmlItem> it = (Iterator<UmlItem>) UmlClass.classes.iterator();
        while (it.hasNext()) {
            UmlItem item = it.next();
            if (!item.stereotype().equals("interface") && !item.stereotype().equals(INTERFACE_STEREOTYPE))
                continue;
            UmlClass port = (UmlClass) item;
            String portName = port.name();
            ports.put(portName, port);
            Log.debug("exportSpecification: port: " + port.name());
            for (UmlItem item2 : port.children()) {
                if (item2.kind() != anItemKind.anOperation)
                    continue;
                UmlOperation operation = (UmlOperation) item2;
                String operationName = operation.name();
                Log.debug("exportSpecification: operation: " + operationName);
                // operations.put(operationName, operation);
                UmlClass outputType = null, inputType = null;
                UmlParameter[] params = operation.params();
                if (params != null) {
                    for (UmlParameter param : params) {
                        // ignore RESTful path, query, header or cookie parameters
                        if (!param.name.isEmpty() && !param.name.equals("body"))
                            continue;
                        Log.debug("exportSpecification: param " + param.name);
                        try {
                            UmlTypeSpec inputType2 = param.type;
                            if (inputType2 != null)
                                inputType = inputType2.type;
                            // String mult = param.multiplicity;
                        } catch (Exception e) {
                            Log.trace("exportSpecification: error - no input message for " + operationName);
                        }
                        if (inputType == null || !isNiemUml(inputType))
                            continue;
                        String inputMessage = inputType.propertyValue(NIEM_STEREOTYPE_XPATH);
                        if (inputMessage == null || inputMessage.isEmpty())
                            continue;
                        Log.debug("exportSpecification: input Message: " + inputMessage + " from operation " + operationName);
                        String inputPrefix = NamespaceModel.getPrefix(inputMessage);
                        if (inputPrefix != null) {
                            messageNamespaces.add(inputPrefix);
                            NiemModel model = getModel(NiemModel.getURI(NamespaceModel.getSchemaURI(inputMessage), inputMessage));
                            UmlClassInstance element = model.getElementByURI(NiemModel.getURI(NamespaceModel.getSchemaURI(inputMessage), inputMessage));
                            if (element != null) {
                                element.set_PropertyValue(MESSAGE_ELEMENT_PROPERTY, operationName);
                                messages.put(inputMessage, element);
                                Log.debug("exportSpecification: element " + element.name() + " is input message element for operation " + operationName);
                            }
                        }
                    }
                }
                String outputMessage = null;
                try {
                    UmlTypeSpec returnType = operation.returnType();
                    if (returnType != null) {
                        outputType = returnType.type;
                        if (outputType != null)
                            outputMessage = outputType.name();
                    }
                } catch (Exception e) {
                    Log.trace("exportSpecification: error - no output message for " + operationName + " " + e.toString());
                }
                if (outputType != null && isNiemUml(outputType))
                    outputMessage = outputType.propertyValue(NIEM_STEREOTYPE_XPATH);
                if (outputMessage == null || outputMessage.isEmpty())
                    continue;
                Log.debug("exportSpecification: output Message: " + outputMessage + " from operation " + operationName);
                String outputPrefix = NamespaceModel.getPrefix(outputMessage);
                if (outputPrefix != null) {
                    //if (NamespaceModel.isNiemPrefix(outputPrefix)) {
                    messageNamespaces.add(outputPrefix);
                    NiemModel model = getModel(NiemModel.getURI(NamespaceModel.getSchemaURI(outputMessage), outputMessage));
                    UmlClassInstance element = model.getElementByURI(NiemModel.getURI(NamespaceModel.getSchemaURI(outputMessage), outputMessage));
                    if (element != null) {
                        element.set_PropertyValue(MESSAGE_ELEMENT_PROPERTY, operationName);
                        messages.put(outputMessage, element);
                        Log.debug("exportSpecification: element " + element.name() + " is output message element for operation " + operationName);
                    }
                }
            }
        }

        TreeSet<String> jsonDefinitions = new TreeSet<>();
        TreeSet<String> jsonDefinitions2 = new TreeSet<>();
        String jsonFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                         properties.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_FILE);
        String jsonDir = Paths.get(jsonFile).getParent().toString();

        // Verify JSON directory exists
        Path jsonPath = Paths.get(jsonFile);
        if (!Files.exists(jsonPath)) {
            Log.debug("exportSpecification: JSON directory does not exist, creating " + jsonFile);
            try {
                Files.createDirectories(jsonPath);
            } catch (IOException e) {
                Log.trace("exportSpecification: error creating JSON directory " + e.toString());
                return;
            }
        }
        //if (jsonDir != null) {
        //    jsonDefinitions.addAll(SubsetModel.exportSchemas(null, jsonDir));
        //}
        jsonDefinitions.addAll(ExtensionModel.exportSchemas(properties));
        // swagger code generation tools do not support relative references, rename them to local references

        for (String definition : jsonDefinitions) {
            String definition2 = definition.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")", "$1#/$3");
            //Log.debug("exportSpecification: definition " + definition2);
            if (definition2 != null)
                jsonDefinitions2.add(definition2);
        }

        if (exportXsd.equals("true")) {
            if (exportCmfToXsd.equals("false")) {
                try {

                    xmlWriter.exportMpdCatalog(messages.keySet(), properties);
                } catch (IOException e) {
                    Log.trace("exportSpecification: error exporting MPD catalog " + e.toString());
                }
            }
            if (exportWsdl.equals("true")) {
				try {
                    String wsdlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                             properties.getProperty(ProjectProperties.EXPORT_WSDL_DIR);     
                    xmlWriter.exportWSDL(wsdlDir, ports, messageNamespaces, properties);
                } catch (IOException e) {
                    Log.trace("exportSpecification: error exporting WSDL " + e.toString());
                }
            }
        }
        if (exportJson.equals("true")) {
            //if (exportCmfToJson.equals("false")) {
                try {
                    if (exportOpenApi.equals("true")) {
                        JsonWriter jsonWriter = new JsonWriter(jsonDir);
                        jsonWriter.exportOpenApi(properties, ports, messageNamespaces, jsonDefinitions2);
                    }
                } catch (IOException e) {
                    Log.trace("exportSpecification: error exporting OpenAPI files " + e.toString());
                }
            //}
        }
        //if (exportCmf.equals("true"))
        //    exportCmf();
        Log.trace("exportSpecification: done generating NIEM message specification");
    }

    public void exportCmf() {

        String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator + properties.getProperty(ProjectProperties.EXPORT_CMF_FILE);
        String cmfVersion = properties.getProperty(ProjectProperties.EXPORT_CMF_VERSION);

        // Verify directory exists
        Path cmfPath = Paths.get(cmfFile).getParent();
        if (!Files.exists(cmfPath)) {
            Log.debug("exportSpecification: cmf directory does not exist, creating " + cmfFile);
            try {
                Files.createDirectories(cmfPath);
            } catch (IOException e) {
                Log.trace("exportSpecification: error creating cmf directory " + e.toString());
                return;
            }
        }

        if (cmfFile != null && cmfVersion != null)
            try {
            CmfWriter cmfWriter = new CmfWriter(cmfPath.toString(), cmfVersion);
            cmfWriter.exportCmf(cmfFile);
        } catch (IOException e) {
            Log.trace("exportSpecification: error exporting common model format files " + e.toString());
        }
        Log.stop("exportSpecification");
    }

    /**
     * exports a NIEM wantlist for import into Subset Schema Generator Tool
     * (SSGT)
     *
     * @param dir
     * @param filename
     */
    public void exportWantlist() {

        String directory = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
        String filename = properties.getProperty(ProjectProperties.EXPORT_WANTLIST_FILE)+".xml";

        // Verify directory exists
        Path modelPath = Paths.get(directory);
        if (!Files.exists(modelPath)) {
            Log.debug("exportWantlist: model directory does not exist, creating " + directory);
            try {
                Files.createDirectories(modelPath);
            } catch (IOException e) {
                Log.trace("exportWantlist: error creating model directory " + e.toString());
                return;
            }
        }
        
        Log.start("exportWantlist");
        UmlCom.message("Generating NIEM Wantlist ...");
        Log.trace("Generating NIEM Wantlist in " + directory + "\\" + filename);
        //XmlWriter xmlWriter = new XmlWriter(dir);

        // createSubset();
        NamespaceModel.cacheExternalSchemas();

        UmlItem.directory = directory;
        try {
            // Export schema
            Log.debug("exportWantlist: create header");
            Path path = Paths.get(directory, filename);
            if (!Files.exists(path.getParent()))
                Files.createDirectories(path.getParent());
            File file = path.toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null)
                parentFile.mkdirs();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(XmlWriter.XML_HEADER);
                fw.write(XmlWriter.XML_ATTRIBUTION);
                fw.write("<w:WantList w:release=\"" + getNiemVersion()
                        + "\" w:product=\"NIEM\" w:nillableDefault=\"true\" ");
                UmlPackage modelPackage = SubsetModel.getModelPackage();
                if (modelPackage == null)
                    return;
                for (UmlItem item : modelPackage.children()) {
                    if (item.kind() == anItemKind.aClassView) {
                        String prefix = item.propertyValue(PREFIX_PROPERTY);
                        String schemaURI = NamespaceModel.getSchemaURIForPrefix(prefix);
                        if (!NamespaceModel.isInfrastructurePrefix(prefix)) {
                            XmlWriter.writeXmlNs(fw, prefix, schemaURI);
                        }
                    }
                }
                XmlWriter.writeXmlNs(fw, "w", WANTLIST_URI);
                fw.write(">");

                // export elements
                for (UmlItem item : SubsetModel.getModelPackage().children()) {
                    if (item.kind() == anItemKind.aClassView) {
                        UmlClassView classView = (UmlClassView) item;
                        String prefix = classView.propertyValue(PREFIX_PROPERTY);
                        String anyElement = NamespaceModel.getPrefixedName(NiemModel.XSD_PREFIX, NiemModel.ANY_ELEMENT_NAME);
                        if (NamespaceModel.isInfrastructurePrefix(prefix))
                            continue;
                        for (UmlItem item2 : classView.children()) {
                            if (item2.kind() == anItemKind.aClassInstance) {
                                UmlClassInstance element = (UmlClassInstance) item2;
                                String elementName = NamespaceModel.getPrefixedName(element);
                                if (elementName.equals(anyElement))
                                    continue;
                                if (NamespaceModel.isAttribute(element)) {
                                    elementName = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(elementName), elementName);
                                    Log.debug("exportWantlist: export attribute " + elementName);
                                    // fw.write("<w:Attribute w:name=\"" + elementName + "\"/>\n");
                                    continue;
                                }
                                Log.debug("exportWantlist: export element " + elementName);
                                String isNillable = element.propertyValue(NILLABLE_PROPERTY);
                                if (isNillable == null)
                                    isNillable = NiemModel.NILLABLE_DEFAULT;
                                fw.write("<w:Element w:name=\"" + elementName + "\" w:isReference=\"false\" w:nillable=\""
                                        + isNillable + "\"/>\n");
                            }
                        }
                    }
                }

                // export types
                for (UmlItem item : SubsetModel.getModelPackage().children()) {
                    if (item.kind() == anItemKind.aClassView) {
                        UmlClassView classView = (UmlClassView) item;
                        String prefix = classView.propertyValue(PREFIX_PROPERTY);
                        if (NamespaceModel.isInfrastructurePrefix(prefix))
                            continue;
                        for (UmlItem item2 : classView.children()) {
                            if (item2.kind() == anItemKind.aClass) {
                                UmlClass type = (UmlClass) item2;
                                String typeName = NamespaceModel.getPrefixedName(type);
                                Log.debug("exportWantlist: export type " + typeName);

                                // do not export structures:AugmentationType
                                // if (type == SubsetModel.augmentationType)
                                // continue;
                                // attribute groups are not supported in wantlists
                                if (NamespaceModel.isAttribute(type))
                                    continue;

                                fw.write("<w:Type w:name=\"" + typeName + "\" w:isRequested=\"true\">\n");

                                if (!isEnumeration(type)) {
                                    for (UmlItem item3 : type.children()) {
                                        if (item3.kind() == anItemKind.anAttribute) {
                                            UmlAttribute attribute = (UmlAttribute) item3;
                                            if (isFacet(attribute))
                                                continue;
                                            String elementName = attribute.name();
                                            // trace("exportWantlist: adding element " + elementName);
                                            String multiplicity = attribute.multiplicity();
                                            String minOccurs = getMinOccurs(multiplicity);
                                            String maxOccurs = getMaxOccurs(multiplicity);
                                            try {
                                                if (Integer.parseInt(minOccurs) < 0)
                                                    throw new NumberFormatException();
                                                if (!maxOccurs.equals("unbounded") && (Integer.parseInt(maxOccurs) < 1))
                                                    throw new NumberFormatException();
                                            } catch (NumberFormatException e) {
                                                Log.trace("exportWantlist: error - invalid multiplicity " + multiplicity
                                                        + " for " + typeName + "/" + elementName);
                                            }

                                            if (NamespaceModel.isAttribute(attribute)) {
                                                elementName = NamespaceModel.getPrefixedName(NamespaceModel.getPrefix(elementName),
                                                        NamespaceModel.filterAttributePrefix(NamespaceModel.getName(elementName)));
                                                Log.debug("exportWantlist: export attribute " + elementName);
                                                // fw.write("<w:AttributeInType w:name=\"" + elementName + "\" w:minOccurs=\""
                                                // + minOccurs + "\" w:maxOccurs=\"" + maxOccurs + "\"/>\n");
                                                continue;
                                            }
                                            // trace("exportWantlist: export element " + elementName + " in type " +
                                            // typeName);
                                            fw.write("\t<w:ElementInType w:name=\"" + elementName
                                                    + "\" w:isReference=\"false\" w:minOccurs=\"" + minOccurs
                                                    + "\" w:maxOccurs=\"" + maxOccurs + "\"/>\n");
                                        }
                                    }
                                } else // export enumerations
                                // if (isEnumeration(type))
                                {
                                    for (UmlItem item3 : type.children()) {
                                        if (item3.kind() != anItemKind.anAttribute)
                                            continue;
                                        UmlAttribute attribute = (UmlAttribute) item3;
                                        String value = attribute.defaultValue();
                                        //String codeList = type.propertyValue(CODELIST_PROPERTY);
                                        //if (codeList != null && codeList.contains(NiemModel.CODELIST_DELIMITER)) {
                                        // trace("exportWantlist: exporting enumerations for " + getPrefixedName(type));
                                        //if (codeList.trim().contains(NiemModel.CODELIST_DELIMITER)) {
                                        //    String[] codes = codeList.split(NiemModel.CODELIST_DELIMITER);
                                        //    for (String code : codes) {
                                        //        String[] pairs = code.split(NiemModel.CODELIST_DEFINITION_DELIMITER); String
                                        //                value = pairs[0].trim();
                                        if (!value.isEmpty()) //fw.write("<w:Facet w:facet=\"enumeration\" w:value=\"" + ReferenceModel.filterEnum(value) + "\"/>");
                                            fw.write("<w:Facet w:facet=\"enumeration\" w:value=\"" + value + "\"/>");
                                    }
                                }
                                fw.write("</w:Type>");
                            }
                        }
                    }
                }

                fw.write("</w:WantList>");
            }

        } catch (IOException e) {
            Log.trace("exportWantlist: IO exception: " + e.toString());
        }
        Log.stop("exportWantlist");
    }

    /**
     * @return NIEM version as a String
     */
    static public String getNiemVersion() {
        String niemVersion = NIEM_VERSION_DEFAULT;

        /*		String schemaURI = NamespaceModel.getSchemaURIForPrefix("nc");
		// UmlCom.trace("NIEM URI: " + schemaURI);
		Matcher mat = Pattern.compile(".*niem-core/(.*)/").matcher(schemaURI);
		if (mat.find())
			niemVersion = mat.group(1);
		Log.trace("NIEM version: " + niemVersion);*/
        return niemVersion;
    }

    /**
     * get child package with name packageName in parentPackage; if it doesn't
     * exist and create is true, create it
     *
     * @param parentPackage
     * @param packageName
     * @param create
     * @return return child package as a UmlPackage
     */
    private UmlPackage getPackage(UmlPackage parentPackage, String packageName, boolean create) {
        for (UmlItem item : parentPackage.children())
            if (item.name().equals(packageName))
                if ((item.kind() == anItemKind.aPackage))
                    return (UmlPackage) item;
        if (create) {
            Log.debug("getPackage: Creating " + packageName);
            return UmlPackage.create(parentPackage, packageName);
        }
        return null;
    }

    /**
     * import NIEM mapping spreadsheet in CSV format
     *
     * @param filename
     */
    public void importCsv(String filename) {

        Log.trace("Importing NIEM Mapping CSV from " + filename);
        NamespaceModel.cacheExternalSchemas();
        new CsvReader().importCsv(filename);

    }

    /**
     * import NIEM reference model into ConcurrentHashMaps to support validation of NIEM
     * elements and types
     *
     * @param dir
     * @param includeEnums
     * @throws IOException
     */
    // TODO importSchemaDir: import multiple pattern facets (e.g. fema:BuildingFloodZoneSimpleType)
    public void importSchemaDir(String dir) throws IOException {

        UmlCom.message("Importing NIEM schema");

        // Configure DOM
        Path path = FileSystems.getDefault().getPath(dir);
        String importPath = path.toString();

        int passes = 3;

        // Walk directory to import in passes (0: types, 1: elements, 2: elements in types
        for (importPass = 0; importPass < passes; importPass++) {
            switch (importPass) {
                case 0 ->
                    Log.trace("\nImporting types");
                case 1 ->
                    Log.trace("\nImporting elements and attributes");
                case 2 ->
                    Log.trace("\nImporting elements and attributes in types");
            }
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Configure DOM
                    DocumentBuilder db;
                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    docBuilderFactory.setNamespaceAware(true);
                    Document doc;
                    String filename = file.toString();

                    try {
                        db = docBuilderFactory.newDocumentBuilder();
                        // parse the schema
                        doc = db.parse(new File(filename));
                        if (doc == null) {
                            Log.debug("importSchemaDir: ignoring file " + filename);
                            return FileVisitResult.CONTINUE;
                        }
                        NiemModel.recompileXPaths(doc);

                    } catch (IOException | SAXException | ParserConfigurationException e) {
                        Log.debug("importSchemaDir: ignoring file " + filename);
                        return FileVisitResult.CONTINUE;
                    }


                    String filepath1 = filename.replaceFirst(java.util.regex.Matcher.quoteReplacement(importPath), "");
                    String filepath = filepath1.replaceAll(java.util.regex.Matcher.quoteReplacement(File.separator), "/");
                    
                    // check for included or excluded domains

                    if (filepath.contains("/domains/")) {
                        String filepath2 = filepath.replace("/domains/", "").replace(".xsd","");
                        String includes = properties.getProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS);
                        if (!includes.isEmpty() && (!includes.contains(filepath2))) {
                            Log.trace("importSchemaDir: skipping domain " + filepath + " - not on include list");
                                return FileVisitResult.CONTINUE;
                        }
                        String excludes = properties.getProperty(ProjectProperties.IMPORT_EXCLUDE_DOMAINS);
                        if (!excludes.isEmpty() && (excludes.contains(filepath2))) {
                            Log.trace("importSchemaDir: skipping excluded domain " + filepath2);
                                return FileVisitResult.CONTINUE;
                        }
                    }
                    
                    // check for included or excluded codes
                    if (filepath.contains("/codes/")) {
                        String filepath2 = filepath.replace("/codes/", "").replace(".xsd","");
                        String excludes = properties.getProperty(ProjectProperties.IMPORT_EXCLUDE_CODES);
                        if (!excludes.isEmpty() && (excludes.contains(filepath2))) {
                            Log.trace("importSchemaDir: skipping excluded codes " + filepath);
                                return FileVisitResult.CONTINUE;
                        }
                    }
                    if (filename.endsWith(XmlWriter.XSD_FILE_TYPE)) {
                        Log.trace("Importing " + filepath);
                        switch (importPass) {
                            case 0 -> {
                                Namespace ns = ReferenceModel.importTypes(doc, filename);
                                if (ns != null) {
                                    UmlClassView classView = ns.getReferenceClassView();
                                    if (classView != null)
                                        classView.set_PropertyValue(FILE_PATH_PROPERTY, NIEM_DIR + filepath);
                                }
                            }
                            case 1 ->
                                ReferenceModel.importElements(doc, filename);
                            case 2 ->
                                ReferenceModel.importElementsInTypes(doc, filename);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // Sorting
        //Log.trace("Sorting namespaces");
        //ReferenceModel.getModelPackage().sort(); // Error: target not allowed, must be a package, any view or a use case?
        Log.trace("Namespaces: " + NamespaceModel.getSize());
        Log.trace("Elements: " + ReferenceModel.getSize());
    }

    /**
     * @return true if NIEM reference model exists
     */
    public boolean verifyNIEM() {
        Log.debug("verifyNIEM: verifying NIEM folders");
        UmlPackage pimPackage = getPackage(UmlPackage.getProject(), NIEM_PACKAGE, false);
        if (pimPackage != null) {
            ReferenceModel.setModelPackage(getPackage(pimPackage, NIEM_REFERENCE_PACKAGE, false));
            if (ReferenceModel.getModelPackage() != null)
                return true;
        }

        Log.trace("NIEM reference model does not exist.  Import NIEM reference schemas first.");
        return false;
    }

    /**
     * add NIEM stereotype to item and all its children
     *
     * @param item
     */
    public void addStereotype(UmlItem item) {
        Log.trace("addStereotype: " + item.name());
        try {
            anItemKind kind = item.kind();
            if (kind == anItemKind.aClass
                    || kind == anItemKind.aClassInstance
                    || kind == anItemKind.anAttribute) {

                item.set_Stereotype(NIEM_STEREOTYPE);
                item.applyStereotype();
            } else if (item.kind() == anItemKind.aRelation) {
                UmlRelation r = (UmlRelation) item;
                if (r.relationKind() != aRelationKind.aGeneralisation) {
                    item.set_Stereotype(NIEM_STEREOTYPE);
                    item.applyStereotype();
                }
            }
        } catch (RuntimeException e) {
            Log.trace("addStereotype: error applying stereotype" + e.toString());
        }
        UmlItem[] ch = item.children();
        for (UmlItem c : ch)
            addStereotype(c);
    }

    /**
     * remove NIEM stereotype from item and all its children
     *
     * @param item
     */
    public void removeStereotype(UmlItem item) {
        Log.trace("removeStereotype: " + item.name());
        try {
            anItemKind kind = item.kind();
            if (kind == anItemKind.aClass
                    || kind == anItemKind.aClassInstance
                    || kind == anItemKind.anAttribute) {
                item.set_Stereotype(null);
                item.applyStereotype();
            } else if (item.kind() == anItemKind.aRelation) {
                //if (r.relationKind() != aRelationKind.aGeneralisation) {
                item.set_Stereotype(null);
                item.applyStereotype();
                //}
            }
        } catch (RuntimeException e) {
            Log.trace("removeStereotype: error removing stereotype from relation" + e.toString());
        }
        UmlItem[] ch = item.children();
        for (UmlItem c : ch)
            removeStereotype(c);
    }

    /**
     * check if item is an enumeration
     *
     * @param item
     * @return true if item is an enumeration
     */
    public static Boolean isEnumeration(UmlItem item) {
        return (switch (item.stereotype()) {
            case "enum", "enum_pattern", "enum_class", "table" ->
                true;
            default ->
                false;
        });
    }

    /**
     * check if item is an enumeration
     *
     * @param item
     * @return true if item is an enumeration
     */
    public static Boolean isFacet(UmlItem item) {
        return item.stereotype().equals(FACET_STEREOTYPE);
    }

    /**
     * @param type
     * @param codelist
     * @return set codelist associated with the type
     */
    static void setCodeList(UmlItem item, String codelist) {
        item.set_PropertyValue(CODELIST_PROPERTY, codelist);
    }

    /**
     * @param type
     * @param facets
     * @return set facets associated with the type
     */
    //static void setFacets(UmlItem item, String facets) {
    //    item.set_PropertyValue(FACETS_PROPERTY, facets);
    //}

    /**
     * sort item and all its children
     *
     * @param item
     */
    public void sort(UmlItem item) {
        item.sortChildren();
        for (UmlItem child : item.children())
            sort(child);
    }
}
