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
 * The {@code NiemModel} class provides methods and constants for managing and manipulating
 * NIEM (National Information Exchange Model) reference, subset, and extension models
 * within a UML modeling environment. It supports importing NIEM schemas, caching model
 * elements and types, exporting schemas to XML and JSON, and handling NIEM-specific
 * naming, design rules, and conformance targets.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Importing NIEM elements, types, and attribute groups from XML Schema documents.</li>
 *   <li>Caching and managing UML representations of NIEM elements, types, and their relationships.</li>
 *   <li>Exporting NIEM models to XML Schema (XSD) and JSON Schema formats.</li>
 *   <li>Supporting NIEM naming conventions, namespace management, and code list handling.</li>
 *   <li>Providing utility methods for filtering and validating NIEM model names and values.</li>
 *   <li>Managing UML stereotypes, descriptions, and custom properties for NIEM artifacts.</li>
 * </ul>
 * <p>
 * This class interacts with UML modeling classes (such as {@code UmlClass}, {@code UmlClassInstance},
 * {@code UmlAttribute}, and {@code UmlRelation}) and relies on supporting classes for namespace
 * resolution, logging, and schema writing.
 * <p>
 * <b>Note:</b> This class is intended for internal use within NIEM UML modeling tools and assumes
 * the presence of supporting infrastructure for UML and XML processing.
 *
 * @author James Cabral
 * @version 1.0
 * @see NamespaceModel
 * @see NiemUmlModel
 * @see UmlClass
 * @see UmlClassInstance
 * @see UmlAttribute
 * @see UmlRelation
 */
package org.cabral.niemtools;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlClassView;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;
import fr.bouml.UmlRelation;
import fr.bouml.UmlTypeSpec;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;


class NiemModel {

    // NIEM namespaces
    static final String NC_PREFIX = "nc";
    static final String STRUCTURES_PREFIX = "structures";
    static final String STRUCTURES_URI = "https://docs.oasis-open.org/niemopen/ns/model/structures/6.0/";
    static final String LOCAL_PREFIX = "local";
    static final String LOCAL_URI = "local";
    static final String XSD_PREFIX = "xs";
    static final String XSD_URI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    static final String PROXY_PREFIX = "niem-xs";
    //static final String PROXY_URI = "https://docs.oasis-open.org/niemopen/ns/model/adapters/niem-xs/6.0/";
    static final String APPINFO_PREFIX = "appinfo";
    static final String APPINFO_URI = "https://docs.oasis-open.org/niemopen/ns/model/appinfo/6.0/";
    static final String CT_PREFIX = "ct";
    static final String CT_URI = "https://docs.oasis-open.org/niemopen/ns/specification/conformanceTargets/6.0/";
    //static final String TERM_PREFIX = "term";
    //static final String TERM_URI = "http://release.niem.gov/niem/localTerminology/3.0/";

    // NIEM Naming and Design Rules
    static final String NDR_URI = "https://docs.oasis-open.org/niemopen/ns/specification/NDR/6.0/";
    static final String NILLABLE_DEFAULT = "false";

    // NIEM conformance targets
    static final String CT_REFERENCE = "#SubsetSchemaDocument";
    static final String CT_EXTENSION = "#ExtensionSchemaDocument";

    // NIEM message specification defaults
    static final String MPD_URI = "http://reference.niem.gov/niem/specification/model-package-description/3.0/";
    static final String MPD_NC_URI = "http://release.niem.gov/niem/niem-core/3.0/";
    static final String MPD_STRUCTURES_URI = "http://release.niem.gov/niem/structures/3.0/";
    static final String MPD_CATALOG_LOCATION = "https://tools.niem.gov/IEPD/mpd-catalog-3.0.xsd";
    static final String MPD_CATALOG_URI = "http://reference.niem.gov/niem/resource/mpd/catalog/3.0/";
    // private static final String MPD_CATALOG_LOCATION = "../../mpd-toolkit-3.0/mpd-catalog-3.0.xsd";

    // NIEM infrastructure type and element names
    static final String ABSTRACT_TYPE_NAME = "abstract";
    static final String ANY_ELEMENT_NAME = "any";
    //static final String ASSOCIATION_TYPE_NAME = "AssociationType";
    private static final String AUGMENTATION_TYPE_NAME = "AugmentationType";
    static final String AUGMENTATION_POINT_NAME = "AugmentationPoint";
    static final String AUGMENTATION_NAME = "Augmentation";
    static final String ABSTRACT_NAME = "Abstract";
    static final String REPRESENTATION_NAME = "Representation"; 
    static final String SIMPLE_TYPE_NAME = "SimpleType";
    private static final String OBJECT_TYPE_NAME = "ObjectType";
    private static final String SIMPLE_OBJECT_ATTRIBUTE_GROUP = "@SimpleObjectAttributeGroup";
    private static final String[] XML_TYPE_NAMES = {"anyURI", "base64Binary", "boolean", "byte", "date", "dateTime",
        "decimal", "double", "duration", "ENTITIES", "ENTITY", "float", "gDay", "gMonth", "gMonthDay", "gYear",
        "gYearMonth", "hexBinary", "ID", "IDREF", "IDREFS", "int", "integer", "language", "long", "Name", "NCName",
        "negativeInteger", "NMTOKEN", "NMTOKENS", "nonNegativeInteger", "nonPositiveInteger", "normalizedString",
        "NOTATION", "positiveInteger", "QName", "short", "string", "time", "token", "unsignedByte", "unsignedInt",
        "unsignedLong", "unsignedShort"};

    // delimiters
    static final String CODELIST_DEFINITION_DELIMITER = "=";
    static final String CODELIST_DELIMITER = ";";
    private static final String HASH_DELIMITER = ",";

    // UML model caches
    static Map<String, List<UmlClassInstance>> Substitutions = new ConcurrentHashMap<>();
    private UmlClass abstractType = null;
    private UmlClass augmentationType = null;
    private final Map<String, UmlClassInstance> elements = new ConcurrentHashMap<>();
    private final Map<String, List<UmlClassInstance>> elementsInType = new ConcurrentHashMap<>();
    private UmlPackage modelPackage = null;
    private UmlClass objectType = null;
    private UmlClass simpleObjectAttributeGroup = null;
    private final Map<String, UmlClass> types = new ConcurrentHashMap<>();

    // XPath expressions
    static final XPath xPath = XPathFactory.newInstance().newXPath();
    static XPathExpression namespaceXPath = null;
    static XPathExpression schemaDocumentationXPath = null;
    static XPathExpression localTermsXPath = null;
    private static XPathExpression nodeDocumentationXPath = null;
    private static XPathExpression enumerationXPath = null;
    private static XPathExpression facetXPath = null;
    private static XPathExpression elementXPath = null;
    private static XPathExpression attributeNameXPath = null;
    private static XPathExpression attributeRefXPath = null;
    private static XPathExpression restrictionBaseXPath = null;
    private static XPathExpression enumerationValueXPath = null;
    private static XPathExpression listXPath = null;
    private static XPathExpression attributeGroupNameXPath = null;
    private static XPathExpression attributeGroupRefXPath = null;
    private static XPathExpression complexTypeXPath = null;
    private static XPathExpression baseTypeXPath = null;
    private static XPathExpression elementRefXPath = null;
    private static XPathExpression typeNameXPath = null;
    
    public NiemModel() {
        super();
    }

    /**
     * recompile XPaths
     * @param Document
     * 
     */
    public static void recompileXPaths(Document doc) {
        try {
            if (xPath != null) {
                xPath.setNamespaceContext(new NamespaceResolver(doc, true));
                namespaceXPath = xPath.compile("xs:schema/@targetNamespace");
                schemaDocumentationXPath = xPath.compile("xs:schema/xs:annotation[1]/xs:documentation[1]");
                localTermsXPath = xPath.compile("xs:schema/xs:annotation[1]/xs:appinfo[1]/*[local-name()='LocalTerm']");
                nodeDocumentationXPath = xPath.compile("xs:annotation[1]/xs:documentation[1]");
                enumerationXPath = xPath.compile("xs:restriction[1]/xs:enumeration");
                facetXPath = xPath.compile("xs:restriction[1]/*[not(self::xs:enumeration)]");
                elementXPath = xPath.compile("xs:element[@name]");
                attributeNameXPath = xPath.compile("xs:attribute[@name]");
                attributeRefXPath = xPath.compile(".//xs:attribute[@ref]");
                restrictionBaseXPath = xPath.compile(".//xs:simpleType/xs:restriction[1][@base]");
                enumerationValueXPath = xPath.compile("xs:enumeration[@value]");
                listXPath = xPath.compile(".//xs:simpleType/xs:list/@itemType");
                attributeGroupNameXPath = xPath.compile("xs:attributeGroup[@name]");
                attributeGroupRefXPath = xPath.compile(".//xs:attributeGroup[@ref]");
                complexTypeXPath = xPath.compile("xs:complexType[@name]");
                baseTypeXPath = xPath.compile("xs:simpleContent[1]/xs:extension[1][@base] | xs:complexContent[1]/xs:extension[1][@base]");
                elementRefXPath = xPath.compile(".//xs:sequence[1]/xs:element[@ref]");
                typeNameXPath = xPath.compile("xs:complexType|xs:simpleType[@name]");
            }
        } catch (XPathExpressionException e) {
            Log.trace("Failed to compile documentation XPath expression: " + e.getMessage());
        }
    }

    /**
     * @param item
     * @return base type related to a type or element as a UmlClass
     */
    @SuppressWarnings("unused")
    static UmlClass getBaseType(UmlItem item) {
        if (item == null)
            return null;
        UmlClass baseType = null;
        UmlClassInstance classInstance;
        switch (item.kind().value()) {
            case anItemKind._anAttribute:
                UmlAttribute attribute = (UmlAttribute) item;
                if (attribute.stereotype().equals(NiemUmlModel.FACET_STEREOTYPE)) {
                    Log.trace("getBaseType: facets do not have base types " + item.name());
                    return null;
                }
                baseType = attribute.type().type;
                break;
            case anItemKind._aClassInstance:
                classInstance = (UmlClassInstance) item;
                baseType = classInstance.type();
                break;
            case anItemKind._aClass:
                if (isAbstract(NamespaceModel.getName(item))) {
                    return null;
                }
                UmlClass type = (UmlClass) item;
                if (type.children() != null)
                    for (UmlItem item2 : type.children())
                        if (item2.kind() == anItemKind.aRelation) {
                            UmlRelation r = (UmlRelation) item2;
                            if (r.relationKind() == aRelationKind.aGeneralisation) {
                                baseType = r.roleType();
                                break;
                            }
                        }
                if (baseType == null) {
                    String prefix = NamespaceModel.getPrefix(item);
                    //if (prefix.equals(STRUCTURES_PREFIX) || prefix.equals(XSD_PREFIX))
                    //    return null;
                    if (NiemModel.isAugmentationType(NamespaceModel.getName(item)))
                        baseType = NiemUmlModel.getSubsetModel().getAugmentationType();
                }
            default:
                break;
        }
        // if (baseType == null)
        // trace("getBaseType: error - no base type for " + item.name());
        // else
        // trace("getBaseType: base type found " + baseType.name());
        return baseType;
    }

    /**
     * @param schemaURI
     * @param itemName
     * @return URI of a item in schemaURI with name itemName as a String
     */
    static String getURI(String schemaURI, String itemName) {
        // itemName.replaceAll("[^-._:A-Za-z0-9]", "");
        return schemaURI + HASH_DELIMITER + NamespaceModel.getName(itemName).replaceAll(HASH_DELIMITER, "");
    }

    /**
     * @param item
     * @return URI of an item as a String
     */
    static String getURI(UmlItem item) {
        if (item.kind() == anItemKind.anAttribute) {
            UmlAttribute attribute = (UmlAttribute) item;
            if (attribute.stereotype().equals(NiemUmlModel.FACET_STEREOTYPE)) {
                    Log.trace("getURI: facets do not have URIs" + item.name());
                    return null;
            }
        }
        return item.propertyValue(NiemUmlModel.URI_PROPERTY);
    }

    /**
     * @param elementSchemaURI
     * @param elementName
     * @param baseType
     * @param description
     * @param notes
     * @return an element added to reference or extension model as a
     * UmlClassInstance
     */
    UmlClassInstance addElement(String elementSchemaURI, String elementName, UmlClass baseType, String description, String notes) {

        if (elementName.isEmpty() || elementName.equals("??")) {
            return null;
        }

        // get element if it exists
        UmlClassInstance element = getElement(elementSchemaURI, elementName);

        // if not, create element
        if (element == null) {

            // default to abstract type if base type not defined
            if (baseType == null)
                baseType = getAbstractType();

            // create element
            UmlClassView nsClassView = NamespaceModel.getNamespaceClassView(this, NamespaceModel.getPrefix(elementName), elementSchemaURI);
            if (nsClassView == null) {
                Log.debug("addElement: error - no prefix or schema for element " + elementName);
                return null;
            }
            try {
                String elementName2 = filterUMLElement(NamespaceModel.getName(elementName));
                Log.debug("addElement: adding element " + elementName2 + " to schema " + elementSchemaURI);
                element = UmlClassInstance.create(nsClassView, elementName2, baseType);
            } catch (RuntimeException e) {
                Log.debug("addElement: error adding element " + elementName + " of type " + baseType.name());
                return null;
            }
            if (element == null) {
                Log.debug("addElement: error adding element " + elementName + " of type " + baseType.name());
                return null;
            }
            element.set_Description(description);
            String uri = getURI(elementSchemaURI, elementName);
            element.set_PropertyValue(NiemUmlModel.URI_PROPERTY, uri);
            elements.put(uri, element);
            Log.debug("addElement: added " + elementName);
        }

        // add notes
        setNotes(element, notes);
        return element;
    }

    /**
     * @param type
     * @param element
     * @param multiplicity
     * @return an element in type added to reference model or extension as a
     * UmlAttribute
     */
    @SuppressWarnings("unchecked")
    UmlAttribute addElementInType(UmlClass type, UmlClassInstance element, String multiplicity) {

        // return null if element or type do not exist
        if (type == null || element == null)
            return null;

        // get element in type if it exists
        // trace("addElementInTypes: inserting element " + element.name() + " to type "
        // + type.name());
        String elementInTypeName = NamespaceModel.getPrefixedName(element);
        String typeName = NamespaceModel.getPrefixedName(type);
        UmlAttribute elementInType = getElementInType(type, elementInTypeName, multiplicity);

        // create element in type
        if (elementInType == null)
			try {
            Log.debug("addElementInType: adding " + elementInTypeName + " to type " + typeName);
            elementInType = UmlAttribute.create(type, elementInTypeName);
        } catch (RuntimeException re) {
            Log.debug("addElementInType: error - creating element " + elementInTypeName + " in type " + type.name());
            return null;
        }

        // copy stereotype, description, multiplicity and type
        elementInType.set_Stereotype(element.stereotype());
        elementInType.set_Description(element.description());
        if (!multiplicity.isEmpty())
            elementInType.set_Multiplicity(multiplicity);
        //elementInType.set_Type(element.type());

        // copy properties
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>(element.properties());
        if (properties.keySet() != null)
            for (String key : properties.keySet())
                elementInType.set_PropertyValue(key, properties.get(key));

        // copy element properties
        //elementInType.set_PropertyValue(URI_PROPERTY, getURI(element));
        //if (element.description() != null)
        //	elementInType.set_Description(element.description());
        //if (multiplicity != null)
        //	elementInType.set_Multiplicity(multiplicity);
        // relate element in type to element
        relateElementInType(elementInType, element);

        // insert element in type list
        NiemModel model = NiemUmlModel.getModel(type);
        if (model == null) {
            Log.debug("addElementInTypes: error - no model for type " + typeName);
            return null;
        }
        List<UmlClassInstance> elementInTypeList = model.getElementsInType(getURI(type));
        if (!elementInTypeList.contains(element)) {
            elementInTypeList.add(element);
            Log.debug("addElementInTypes: inserted " + elementInTypeName + " to " + typeName);
        }
        return elementInType;
    }

    /**
     * @param typeSchemaURI
     * @param typeName
     * @param description
     * @param notes
     * @return a type added to the reference or extension models as a UmlClass
     */
    UmlClass addType(String typeSchemaURI, String typeName, String description,
            String notes) {

        UmlClass type = getType(typeSchemaURI, typeName);
        if (type == null) {
            // create type
            UmlClassView nsClassView = NamespaceModel.getNamespaceClassView(this, NamespaceModel.getPrefix(typeName), typeSchemaURI);
            if (nsClassView == null) {
                Log.debug("addType: error - no prefix or schema for type " + typeName);
                return null;
            }
            try {
                String typeName2 = filterUMLType(NamespaceModel.getName(typeName));
                Log.debug("addType: adding " + typeName2 + " to schema " + typeSchemaURI);
                type = UmlClass.create(nsClassView, typeName2);
            } catch (RuntimeException re) {
                Log.debug("addType: error adding type " + typeName);
                return null;
            }
            if (type == null) {
                Log.debug("addType: error adding type " + typeName);
                return null;
            }
            String uri = getURI(typeSchemaURI, typeName);
            type.set_PropertyValue(NiemUmlModel.URI_PROPERTY, uri);
            types.put(uri, type);
            Log.debug("addType: added " + typeName);
        }
        setDescription(type, description);
        setNotes(type, notes);
        return type;
    }

    /**
     * caches elements, types and elements in types of NIEM reference, subset or
     * extension model
     */
    void cacheModel() {

        if (modelPackage == null) {
            return;
        }

        Log.debug("cacheModel: caching model " + modelPackage.name());
        // Cache namespaces, types and elements
        String schemaURI;

        Log.debug("cacheModel: caching namespaces, types and elements");
        if (modelPackage.children() != null)
            for (UmlItem classView : modelPackage.children()) {
                if ((classView == null) || (classView.kind() != anItemKind.aClassView)) {
                    continue;
                }
                schemaURI = getURI(classView);
                String prefix = classView.propertyValue(NiemUmlModel.PREFIX_PROPERTY);
                NamespaceModel.addPrefix(schemaURI, prefix);
                Namespace ns = NamespaceModel.getNamespace(schemaURI);
                if ((ns == null) && (schemaURI != null)) {
                    // create namespace
                    ns = NamespaceModel.addNamespace(schemaURI);
                }
                if (ns != null) {
                    if (this == NiemUmlModel.getReferenceModel())
                        ns.setReferenceClassView((UmlClassView) classView);
                    else
                        ns.setNsClassView((UmlClassView) classView);

                    if (this == NiemUmlModel.getReferenceModel() || this == NiemUmlModel.getExtensionModel())
                        ns.setFilepath(classView.propertyValue(NiemUmlModel.FILE_PATH_PROPERTY));
                }

                // cache types and elements
                if (classView.children() != null)
                    for (UmlItem item : classView.children()) {
                        String uri = getURI(item);
                        if (uri != null) {
                            switch (item.kind().value()) {
                                case anItemKind._aClass -> {
                                    if (!types.containsKey(uri))
                                        types.put(uri, (UmlClass) item);
                                }
                                case anItemKind._aClassInstance -> {
                                    UmlClassInstance element = (UmlClassInstance) item;
                                    if (!elements.containsKey(uri))
                                        elements.put(uri, element);
                                    String headElement = element.propertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY);
                                    try {
                                        if (headElement != null) {
                                            List<UmlClassInstance> enlist = (Substitutions.get(headElement));
                                            if (enlist == null)
                                                enlist = new ArrayList<>();
                                            if (!enlist.contains(element))
                                                enlist.add(element);
                                            Substitutions.put(headElement, enlist);
                                        }
                                    } catch (Exception e) {
                                        Log.trace("cacheModel: error caching sustitutions " + e.toString());
                                    }
                                }
                                default -> {
                                }
                            }
                        }
                    }
            }

        // Cache elements in types
        Log.debug("cacheModel: caching elements in types");
        if (types != null && types.values() != null)
            for (UmlClass type : types.values()) {
                String typeURI = getURI(type);
                List<UmlClassInstance> list = (elementsInType.get(typeURI));
                if (list == null) {
                    list = new ArrayList<>();
                    elementsInType.put(typeURI, list);
                }
                if (!NiemUmlModel.isEnumeration(type))
                    if (type != null && type.children() != null)
                        for (UmlItem attribute : type.children()) {
                            if (attribute.kind() == anItemKind.anAttribute && !NiemUmlModel.isFacet(attribute)) {
                                // trace("cacheModel: caching " + getURI(attribute));
                                String attributeURI = getURI(attribute);
                                if (attributeURI == null) {
                                    Log.trace("cacheModel: error - no URI for " + attribute.name());
                                    continue;
                                }
                                UmlClassInstance element = elements.get(attributeURI);
                                if (!list.contains(element))
                                    list.add(element);
                            }
                        }
            }

        Log.debug("cacheModel: store caches and add simple and abstract types");
        if (this == NiemUmlModel.getReferenceModel()) {
            // add local namespace and abstract type
            //NamespaceModel.getNamespaceClassView(this, LOCAL_PREFIX, LOCAL_URI);
            abstractType = addType(LOCAL_URI,
                    NamespaceModel.getPrefixedName(LOCAL_PREFIX, ABSTRACT_TYPE_NAME), null, null);
            // add XML namespace, simple types and any element
            //NamespaceModel.getNamespaceClassView(this, XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
            for (String typeName : XML_TYPE_NAMES)
                addType(XSD_URI, NamespaceModel.getPrefixedName(XSD_PREFIX, typeName), null, null);
            addElement(XSD_URI, NamespaceModel.getPrefixedName(XSD_PREFIX, ANY_ELEMENT_NAME), null, null, null);
        } else if (this == NiemUmlModel.getSubsetModel()) {
            abstractType = copyType(NamespaceModel.getPrefixedName(LOCAL_PREFIX, ABSTRACT_TYPE_NAME));
            augmentationType = copyType(NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, AUGMENTATION_TYPE_NAME));
            objectType = copyType(NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, OBJECT_TYPE_NAME));
            simpleObjectAttributeGroup = copyType(
                    NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, SIMPLE_OBJECT_ATTRIBUTE_GROUP));
            //NamespaceModel.getNamespaceClassView(null, XSD_PREFIX, XSD_URI);
            copyElement(NamespaceModel.getPrefixedName(XSD_PREFIX, ANY_ELEMENT_NAME));
            copyType("xs:NCName"); // JSON-LD @id is type xs:NCName
        } else if (this == NiemUmlModel.getExtensionModel()) {
            abstractType = NiemUmlModel.getSubsetModel().getAbstractType();
            augmentationType = NiemUmlModel.getSubsetModel().getAugmentationType();
            simpleObjectAttributeGroup = NiemUmlModel.getSubsetModel().getSimpleObjectAttributeGroup();
        }
        Log.debug("cacheModels: model " + modelPackage.name() + " cached");
    }

    /**
     * @param elementName
     * @return an element copied from reference model to subset as a
     * UmlClassInstance
     */
    @SuppressWarnings("unchecked")
    UmlClassInstance copyElement(String elementName) {

        // return subset element if it exists
        String schemaURI = NamespaceModel.getSchemaURI(elementName);
        UmlClassInstance element = getElement(schemaURI, elementName);
        if (element != null)
            return element;

        // if element doesn't exist in reference model, return error
        UmlClassInstance sourceElement = NiemUmlModel.getReferenceModel().getElement(schemaURI, elementName);
        if (sourceElement == null) {
            Log.debug("copyElement: error - element " + elementName + " not in reference model");
            return null;
        }

        // copy base type if not already in subset
        UmlClass sourceBaseType = getBaseType(sourceElement);
        String baseTypeName = NamespaceModel.getPrefixedName(sourceBaseType);
        UmlClass baseType = getType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName);
        if (baseType == null)
            baseType = copyType(baseTypeName);

        // if namespace doesn't exist, create it
        UmlClassView nsClassView = NamespaceModel.getNamespaceClassView(this, NamespaceModel.getPrefix(elementName), schemaURI);
        if (nsClassView == null) {
            Log.debug("copyElement: error - no prefix or schema for element " + elementName);
            return null;
        }

        // create element
        try {
            element = UmlClassInstance.create(nsClassView, filterUMLElement(NamespaceModel.getName(elementName)), baseType);
        } catch (RuntimeException e) {
            Log.debug("copyElement: error copying element " + elementName + " to subset");
            return null;
        }

        // copy stereotype, description and type
        element.set_Stereotype(sourceElement.stereotype());
        element.set_Description(sourceElement.description());
        element.set_Type(baseType);

        // copy properties
        String uri = getURI(schemaURI, elementName);
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>(sourceElement.properties());
        if (properties.keySet() != null)
            for (String key : properties.keySet())
                element.set_PropertyValue(key, key.equals(NiemUmlModel.URI_PROPERTY) ? uri : properties.get(key));
        //String uri = getURI(schemaURI, elementName);
        //element.set_PropertyValue(URI_PROPERTY, uri);
        elements.put(uri, element);

        // copy substitution group head element if it exists
        String substitutionGroup = sourceElement.propertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY);
        if (substitutionGroup != null && !substitutionGroup.isEmpty())
          copyElement(substitutionGroup);

        Log.debug("copyElement: element " + elementName + " copied to subset");
        return element;
    }

    /**
     * @param type
     * @param elementInTypeName
     * @param multiplicity
     * @return an element in type as a UmlAttribute
     */
    UmlAttribute getElementInType(UmlClass type, String elementName) {

        //boolean childFound = false;
        if (type != null && type.children() != null)
            for (UmlItem item : type.children()) {
                if (item.name().equals(elementName)) {
                    return (UmlAttribute) item;
                    //childFound = true;
                    //break;
                }
            }

        // if not found in type, check base type
        //if (!childFound) {
        //    UmlClass baseType = NiemModel.getBaseType(type);
        //    if (baseType != null)
        //        childFound = isElementInType(baseType, elementName);
       // }

        return null;
    }

    /**
     * @param type
     * @param element
     * @param multiplicity
     * @return an element in type copied from reference model to subset as a
     * UmlAttribute
     */
    @SuppressWarnings("unchecked")
    UmlAttribute copyElementInType(UmlClass type, UmlClassInstance element, String multiplicity) {
        String elementInTypeName = NamespaceModel.getPrefixedName(element);
        String typeName = NamespaceModel.getPrefixedName(type);
        UmlAttribute attribute = getElementInType(type, elementInTypeName, multiplicity);
        UmlAttribute referenceAttribute = null;
        if (attribute == null)

            // if reference model, ensure element exists in the type
            if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName))) {
                UmlClass referenceType = NiemUmlModel.getReferenceModel().getType(NamespaceModel.getSchemaURI(typeName), typeName);

                referenceAttribute = getElementInType(referenceType, elementInTypeName);
                if (referenceAttribute == null) {
                    Log.debug("copyElementInType: error - element " + elementInTypeName + " not in type " + typeName);
                    return null;
                }
            }
			try {
                attribute = UmlAttribute.create(type, elementInTypeName);
            } catch (RuntimeException re) {
                Log.debug("copyElementInType: error - attribute already exists " + element + " " + re.toString());
            }
        if (attribute == null)
            Log.debug("copyElementInType: error copying element " + elementInTypeName + " in " + typeName);
        else {
            // copy stereotype, type, description and multiplicity
            attribute.set_Stereotype(element.stereotype());
            attribute.set_Description(element.description());
            //attribute.set_Type(element.type());
            attribute.set_Multiplicity(multiplicity);
            relateElementInType(attribute, element);

            // copy properties
             ConcurrentHashMap<String, String> properties = new  ConcurrentHashMap<>(element.properties());
             if (properties.keySet() != null)
                for (String key : properties.keySet())
                    attribute.set_PropertyValue(key, properties.get(key));
            if (referenceAttribute != null)
                attribute.set_PropertyValue(NiemUmlModel.SEQUENCE_ID_PROPERTY, referenceAttribute.propertyValue(NiemUmlModel.SEQUENCE_ID_PROPERTY));
        }
        Log.debug("addElementInTypes: inserted " + elementInTypeName + " to " + typeName);
        return attribute;
    }

    /**
     * @param typeName
     * @return a type copied from the reference model to subset as a UmlClass
     */
    @SuppressWarnings("unchecked")
    UmlClass copyType(String typeName) {

        if ((typeName == null) || (typeName.isEmpty()))
            return null;

        // Check prefix
        if (!NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName))) {
            Log.debug("copyType: error - type " + typeName + " doesn't have a NIEM prefix");
            return null;
        }

        // Check namespace
        String schemaURI = NamespaceModel.getSchemaURI(typeName);
        if (schemaURI == null) {
            Log.debug("copyType: error - namespace for type " + typeName + " not in reference model");
            return null;
        }

        // return subset type if it exists
        UmlClass type = getType(schemaURI, typeName);
        if (type != null)
            return type;

        // find reference type
        UmlClass sourceType = NiemUmlModel.getReferenceModel().getType(schemaURI, typeName);
        if (sourceType == null) {
            Log.debug("copyType: error - type " + typeName + " not in reference model");
            return null;
        }

        // if subset namespace doesn't exist, create it
        UmlClassView nsClassView = NamespaceModel.getNamespaceClassView(this, NamespaceModel.getPrefix(typeName), schemaURI);
        if (nsClassView == null) {
            Log.debug("copyType: error - no prefix or schema for type " + typeName);
            return null;
        }

        // create type
        // trace("copyType: copying type " + typeName + " to subset schema " +
        // nsClassView.name());
        try {
            type = UmlClass.create(nsClassView, filterUMLType(NamespaceModel.getName(typeName)));
        } catch (RuntimeException e) {
            Log.debug("copyType: error - type already exists " + typeName + " " + e.toString());
            return null;
        }

        // copy type stereotype and description
        type.set_Description(sourceType.description());
        type.set_Stereotype(sourceType.stereotype());
        UmlTypeSpec baseTypeSpec = sourceType.baseType();
        if (baseTypeSpec != null)
            type.set_BaseType(baseTypeSpec);

        // copy type properties
        ConcurrentHashMap<String, String> properties = new  ConcurrentHashMap<>(sourceType.properties());
        if (properties.keySet() != null)
            for (String key : properties.keySet())
                type.set_PropertyValue(key, properties.get(key));
        //type.set_PropertyValue(URI_PROPERTY, getURI(sourceType));

        //String codeList = NiemUmlClass.getCodeList(sourceType);
        //if (codeList != null)
        //NiemUmlClass.setCodeList(type, codeList);
        types.put(getURI(type), type);

        // copy and relate to base type
        UmlClass sourceBaseType = getBaseType(sourceType);
        if (sourceBaseType != null) {
            String baseTypeName = NamespaceModel.getPrefixedName(sourceBaseType);
            UmlClass baseType = copyType(baseTypeName);
            if (baseType != null)
                relateBaseType(type, baseType);
        }

        // copy elements and attributes in type
        if (sourceType.children() != null)
            for (UmlItem item : sourceType.children()) {
                if (NamespaceModel.isAttribute(item)) {
                    UmlClassInstance sourceElement = NiemUmlModel.getReferenceModel().getReferencedElement(item);
                    if (sourceElement == null) {
                        Log.trace("copyType: error - no element for uri " + getURI(item) + " in reference model");
                        continue;
                    }
                    String attributeName = NamespaceModel.getPrefixedName(sourceElement);
                    UmlClassInstance element = copyElement(attributeName);
                    if (element == null)
                        Log.trace("copyType: error - no attribute " + attributeName);
                    else
                        copyElementInType(type, element, ((UmlAttribute) item).multiplicity());
                }
            }

        // copy enumerations in type
        if (NiemUmlModel.isEnumeration(sourceType)) {
            type.set_Stereotype(NiemUmlModel.ENUM_STEREOTYPE);
            if (sourceType.children() != null)
                for (UmlItem sourceItem : sourceType.children()) {
                    if (sourceItem.kind() != anItemKind.anAttribute)
                        continue;
                    UmlAttribute sourceAttribute = (UmlAttribute) sourceItem;
                    UmlAttribute attribute = UmlAttribute.create(type, sourceAttribute.name());
                    if (attribute == null) {
                        Log.trace("copyType: error - attribute " + sourceAttribute.name() + " already exists in type " + typeName);
                        continue;
                    }
                    // copy stereotype, description, value and type
                    attribute.set_Stereotype(sourceAttribute.stereotype());
                    attribute.set_DefaultValue(sourceAttribute.defaultValue());
                    attribute.set_Description(sourceAttribute.description());
                    attribute.set_Type(sourceAttribute.type());

                    // copy properties
                    Map<String, String> attributeProperties = sourceAttribute.properties();
                    if (attributeProperties != null)
                        for (String key : attributeProperties.keySet())
                            attribute.set_PropertyValue(key, attributeProperties.get(key));
                }
        }

        // copy and relate to attribute groups
        UmlClass attributeGroupType = getAttributeGroup(sourceType);
        if (attributeGroupType != null) {
            String attributeGroupName = NamespaceModel.getPrefixedName(attributeGroupType);
            UmlClass attributeGroup = copyType(attributeGroupName);
            if (attributeGroup == null)
                Log.trace("copyType: error - no attribute group " + attributeGroupName);
            else
                relateAttributeGroup(type, attributeGroupType);
        }
        Log.debug("copyType: type copied " + typeName);
        return type;
    }

    /**
     * exports NIEM extension and exchange schema
     *
     * @param xmlDir
     * @param jsonDir
     * @return JSON definitions as a Set
     */
    TreeSet<String> exportSchemas(ProjectProperties properties) {

        String xmlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                        properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);
        String jsonFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                         properties.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_FILE);
        String jsonDir = Paths.get(jsonFile).getParent().toString();

        String exportXML = properties.getProperty(ProjectProperties.EXPORT_XSD);
        String exportJSON = properties.getProperty(ProjectProperties.EXPORT_JSON);
        String exportCmftoXSD = properties.getProperty(ProjectProperties.EXPORT_CMF_TO_XSD);
        String exportCmftoJSON = properties.getProperty(ProjectProperties.EXPORT_CMF_TO_JSON);

        XmlWriter xmlWriter = new XmlWriter(xmlDir);
        JsonWriter jsonWriter = new JsonWriter(jsonDir);
        TreeSet<String> openapiDefinitions = new TreeSet<>();

        // export each schema
        if (modelPackage != null && modelPackage.children() != null)
          for (UmlItem thisPackage : modelPackage.children()) {
            if (thisPackage.kind() != anItemKind.aClassView)
                continue;
            UmlClassView classView = (UmlClassView) thisPackage;
            // classView.sort();
            String prefix = classView.propertyValue(NiemUmlModel.PREFIX_PROPERTY);
            if (prefix == null || prefix.isEmpty()) {
                Log.trace("exportSchemas: prefix for " + thisPackage.name() + " is not set");
                continue;
            }
            if (NamespaceModel.isExternalPrefix(prefix))
                continue;
            String nsSchemaURI = getURI(classView);
            Log.debug("exportSchemas: exporting schema " + prefix);

            // build list of referenced namespaces
            TreeSet<String> schemaNamespaces = new TreeSet<>();
            schemaNamespaces.add(XSD_PREFIX);

            // add structures namespaces (for base types)
            schemaNamespaces.add(STRUCTURES_PREFIX);

            TreeSet<String> xmlTypes = new TreeSet<>();
            TreeSet<String> jsonDefinitions = new TreeSet<>();
            TreeSet<String> xmlElements = new TreeSet<>();
            TreeSet<String> jsonProperties = new TreeSet<>();
            TreeSet<String> jsonRequired = new TreeSet<>();

            Log.debug("exportSchemas: exporting types and elements");
            String xmlType;
            String jsonType;
            if (classView.children() != null)
                for (UmlItem item : classView.children()) {
                    // add types and attribute groups
                    if (item.kind() == anItemKind.aClass) {
                        UmlClass type = (UmlClass) item;

                        // add referenced namespaces
                        try {
                            schemaNamespaces.add(NamespaceModel.getPrefix(type));
                            UmlClass baseType = getBaseType(type);
                            if (baseType != null)
                                schemaNamespaces.add(NamespaceModel.getPrefix(baseType));
                            if (type.children() != null && !NiemUmlModel.isEnumeration(type))
                                for (UmlItem item2 : type.children()) {
                                    if (item2.kind() == anItemKind.anAttribute) {
                                        NiemModel model2 = NiemUmlModel.getModel(getURI(item2));
                                        UmlClassInstance element = model2.getReferencedElement(item2);
                                        if (element != null)
                                            schemaNamespaces.add(NamespaceModel.getPrefix(element));
                                    }
                                }
                        } catch (RuntimeException re) {
                            Log.trace("exportSchemas: error adding namespace " + type.name() + " " + re.toString());
                            continue;
                        }

                        // get type schema
                        try {
                            xmlType = xmlWriter.exportXmlTypeSchema(type);
                            if (xmlType != null)
                                xmlTypes.add(xmlType);
                            if (exportJSON.equals("true") && exportCmftoJSON.equals("false")) {
                                jsonType = (prefix.equals(NiemModel.XSD_PREFIX)) ? jsonWriter.exportJsonPrimitiveSchemafromXML(type) : 
                                jsonWriter.exportJsonTypeSchema(this, type, prefix);
                            if (jsonType != null)
                                jsonDefinitions.add(jsonType);
                            }
                        } catch (RuntimeException re) {
                            Log.trace("exportSchemas: error exporting type " + type.name() + " " + re.toString());
                            continue;
                        }
                    }
                    // add elements and attributes
                    String xmlElement;
                    String jsonElement = null;
                    if (item.kind() == anItemKind.aClassInstance) {
                        UmlClassInstance element = (UmlClassInstance) item;
                        UmlClass baseType = getBaseType(element);

                        // add referenced namespaces
                        try {
                            schemaNamespaces.add(NamespaceModel.getPrefix(element));
                            if (baseType != null)
                                schemaNamespaces.add(NamespaceModel.getPrefix(baseType));
                            String headElement = element.propertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY);
                            if (headElement != null)
                                schemaNamespaces.add(NamespaceModel.getPrefix(headElement));
                        } catch (RuntimeException re) {
                            Log.trace("exportSchemas: error adding namespace " + element.name() + " " + re.toString());
                            continue;
                        }

                        // get element schema
                        try {
                            Log.debug("exportSchemas: exporting element " + element.name());
                            if (NamespaceModel.isAttribute(element.name()))
                                xmlElement = xmlWriter.exportXmlAttributeSchema(element);
                            else
                                xmlElement = xmlWriter.exportXmlElementSchema(element);
                            if (xmlElement != null)
                                xmlElements.add(xmlElement);
                            if (baseType != null && !NiemModel.isAbstract(NamespaceModel.getName(baseType))) {
                                schemaNamespaces.add(NamespaceModel.getPrefix(element));
                                if (exportJSON.equals("true") && exportCmftoJSON.equals("false")) {
                                    jsonElement = jsonWriter.exportJsonElementSchema(element, prefix);
                                    if (jsonElement != null)
                                        jsonDefinitions.add(jsonElement);
                                }
                                String messageElement = element.propertyValue(NiemUmlModel.MESSAGE_ELEMENT_PROPERTY);
                                if (messageElement != null)
                                    Log.debug("exportSchemas: messageElement " + messageElement);
                                if (exportJSON.equals("true") && exportCmftoJSON.equals("false")
                                    && messageElement != null && !messageElement.isEmpty())
                                        jsonProperties.add(jsonElement);
                            }
                        } catch (RuntimeException re) {
                            Log.trace("exportSchemas: error exporting element " + element.name() + " " + re.toString());
                        }
                    }
                }

            // export XSDs
            if (exportXML.equals("true") && exportCmftoXSD.equals("false")) {
                // Open XSD file for each extension schema and write header
                Log.debug("exportSchemas: schema " + xmlDir + "/" + prefix + XmlWriter.XSD_FILE_TYPE);
                Namespace ns = NamespaceModel.getNamespace(nsSchemaURI);
                if (ns.getFilepath() == null) {
                    Log.trace("exportSchemas: error - no path for schema " + nsSchemaURI);
                    continue;
                }
                Log.trace("Generating XSD for extension schema " + prefix);
                String filename = Paths.get(xmlDir, ns.getFilepath()).toString();
                Log.debug("exportSchemas: referenced namespaces in " + filename + ": " + schemaNamespaces.toString());
                xmlWriter.exportXmlSchema(filename, nsSchemaURI, xmlTypes, xmlElements, schemaNamespaces);
            }

            // export JSON file
            if (exportJSON.equals("true") && exportCmftoJSON.equals("false")) {
                jsonWriter.exportJsonSchema(prefix, nsSchemaURI, schemaNamespaces, jsonDefinitions,
                        jsonProperties, jsonRequired);
            openapiDefinitions.addAll(jsonProperties);
            openapiDefinitions.addAll(jsonDefinitions);
            }
        }
        
        return openapiDefinitions;
    }

    /**
     * filter non-ASCII characters
     *
     * @param string
     * @return filtered String
     */
    private String filterASCII(String string) {
        return string.replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * filter illegal characters in enumerations
     *
     * @param string
     * @return filtered String
     */
    String filterEnum(String string) {
        return string.replaceAll(CODELIST_DELIMITER + "|" + CODELIST_DEFINITION_DELIMITER, "");
    }

    /**
     * filter illegal characters in enumeration definitions
     *
     * @param string
     * @return filtered String
     */
    private String filterEnumDefinition(String string) {
        return string.replaceAll(CODELIST_DELIMITER + "|" + CODELIST_DEFINITION_DELIMITER, "");
    }

    /**
     * filter illegal characters in UML elements
     *
     * @param string
     * @return filtered String
     */
    static protected String filterUMLAttribute(String string) {
        return string.replaceAll("[^A-Za-z0-9_@#$`~,.<?;:'\"\\\\]", "");
    }

    /**
     * filter illegal characters in UML elements
     *
     * @param string
     * @return filtered String
     */
    private String filterUMLElement(String string) {
        return string.replaceAll("[^A-Za-z0-9_@#$-`~,.<?;:'\"\\\\]", "");
    }

    /**
     * filter illegal characters in UML types
     *
     * @param string
     * @return filtered String
     */
    private String filterUMLType(String string) {
        return string.replaceAll("[^A-Za-z0-9_@#$`~,.<?;:'\"\\\\]", "");
    }

    /**
     * @return abstract type as a UmlClass
     */
    UmlClass getAbstractType() {
        return abstractType;
    }

    /**
     * @param type
     * @return attribute group related to a type as a UmlClass
     */
    private UmlClass getAttributeGroup(UmlClass type) {
        if (type != null && type.children() != null)
            for (UmlItem item : type.children()) {
                if (item.kind() == anItemKind.aRelation) {
                    UmlRelation r = (UmlRelation) item;
                    if (r.relationKind() == aRelationKind.aDirectionalAggregation) {
                        return r.roleType();
                    }
                }
            }
        // trace("getAttributeGroup: error - attribute group not found for " +
        // type.name());
        return null;
    }

    /**
     * @return augmentation type as a UmlClass
     */
    UmlClass getAugmentationType() {
        return augmentationType;
    }

    /**
     * @param filename
     * @param doc
     * @return the default schema URI for the current file as a String
     */
    private String getDefaultSchemaURI(Document doc) {
        String defaultSchemaURI = doc.lookupNamespaceURI(null);
        if (defaultSchemaURI == null) {
            // trace("getDefaultSchemaURI: no default namespace found in " + filename);
            defaultSchemaURI = LOCAL_PREFIX;
        }
        return defaultSchemaURI;
    }

    /**
     * @param schemaURI
     * @param elementName
     * @return an element in schema schemaURI with name elementName as a
     * UmlClassInstance
     */
    UmlClassInstance getElement(String schemaURI, String elementName) {
        return getElementByURI(getURI(schemaURI, elementName));
    }

    /**
     * @param elementUri
     * @return an element in model with uri elementUri as a String
     */
    UmlClassInstance getElementByURI(String elementUri) {
        if (elementUri == null || elementUri.isEmpty()) {
            Log.trace("getElementByURI: error - no URI for element");
            return null;
        }
        return elements.get(elementUri);
    }

    /**
     * @param item
     * @return a referenced element in model as a UmlClassInstance
     */
    @SuppressWarnings("unused")
    UmlClassInstance getReferencedElement(UmlItem item) {
        anItemKind kind = item.kind();
        //if (item.kind() != anItemKind.anAttribute)
        if (item.kind() != anItemKind.anAttribute && item.kind() != anItemKind.aClassInstance)
            return null;
        String itemURI = getURI(item);
        if (itemURI == null) {
            Log.trace("getReferencedElement: error - no URI for " + item.name());
            return null;
        }
        return getElementByURI(itemURI);
    }

    /**
     * get an element in type and check the multiplicity
     *
     * @param type
     * @param elementInTypeName
     * @param multiplicity
     * @return an element in type as a UmlAttribute
     */
    private UmlAttribute getElementInType(UmlClass type, String elementInTypeName, String multiplicity) {
        if (type != null && type.children() != null)
            for (UmlItem item : type.children()) {
                if (item.kind() == anItemKind.anAttribute && item.name().equals(elementInTypeName)) {
                    String previousMultiplicity = ((UmlAttribute) item).multiplicity();
                    if (!previousMultiplicity.isEmpty() && !multiplicity.isEmpty() && !previousMultiplicity.equals(multiplicity))
                        Log.debug("getElementInType:  warning - " + NamespaceModel.getPrefixedName(type) + "/" + elementInTypeName
                                + " has conflicting multiplicities " + previousMultiplicity + " and " + multiplicity);
                    return (UmlAttribute) item;
                }
            }
        return null;
    }

    /**
     * @param typeURI
     * @return a list of elements in a type with uri typeURI as a List
     */
    List<UmlClassInstance> getElementsInType(String typeURI) {
        List<UmlClassInstance> elementInTypeList = elementsInType.get(typeURI);
        if (elementInTypeList == null) {
            elementInTypeList = new ArrayList<>();
            elementsInType.put(typeURI, elementInTypeList);
        }
        return elementInTypeList;
    }

    /**
     * @return model as a UmlPackage
     */
    UmlPackage getModelPackage() {
        return modelPackage;
    }

    /**
     * @return object type as a UmlClass
     */
    UmlClass getObjectType() {
        return objectType;
    }

    /**
     * @return simple object attribute group as a UmlClass
     */
    UmlClass getSimpleObjectAttributeGroup() {
        return simpleObjectAttributeGroup;
    }

    /**
     * @return number of elements in the model as an int
     */
    int getSize() {
        return elements.size();
    }

    /**
     * @param schemaURI
     * @param typeName
     * @return type in model with schema schemaURI and name tagname as a String
     */
    UmlClass getType(String schemaURI, String typeName) {
        // return cached type
        String uri = getURI(schemaURI, typeName);
        return types.get(uri);
    }

    /**
     * @param uri
     * @return an element in model with uri elementUri as a UmlClass
     */
    UmlClass getTypeByURI(String uri) {
        if (uri == null || uri.isEmpty()) {
            Log.trace("getTypeByURI: error - no URI for type");
            return null;
        }
        return types.get(uri);
    }

    private String importCodeList(NodeList elist) {
        return importCodeList(null, elist);
    }

    /**
     * @param elist
     * @return code values and descriptions from enumerations in schema as a
     * String
     */
    private String importCodeList(UmlClass type, NodeList elist) {
        if (type != null) {
            Log.debug("importCodeList: adding code list " + type.name());
        }
        String codeList = "";
        int anonymousEnums = 0;
        int length = elist.getLength();
        if (length == 0)
            return codeList;
        String truncated = "false";
        //int me = maxEnums;
        String maxEnumsString = NiemUmlModel.getProperty(ProjectProperties.IMPORT_MAX_FACETS);
        if (maxEnumsString != null && !maxEnumsString.isEmpty()) {
            try {
                Integer maxEnums = Integer.valueOf(maxEnumsString);
                if (length > maxEnums) {
                    if (type != null)
                        Log.trace("importCodeList: truncated code list " + type.name() + " from " + length + " to " + maxEnums);
                    length = maxEnums;
                    truncated = "true";
                }
            } catch (NumberFormatException e) {
                Log.debug("importCodeList: error - cannot parse maxEnums " + maxEnumsString + " " + e.toString());
            }
        }
        if (type != null)
            type.set_PropertyValue(NiemUmlModel.TRUNCATED_PROPERTY, truncated);
        for (int j = 0; j < length; j++) {
            Element enumElement = (Element) elist.item(j);
            //String value = filterEnum(filterASCII(enumElement.getAttribute("value")));
            String value = filterASCII(enumElement.getAttribute("value"));
            String codeDescription = null;
            String importCodeDescriptions = NiemUmlModel.getProperty(ProjectProperties.IMPORT_CODE_DESCRIPTIONS);
            if (importCodeDescriptions != null && !importCodeDescriptions.equals("false"))
				try {
                if (nodeDocumentationXPath != null) {
//						codeDescription = filterEnumDefinition(filterASCII(xe.evaluate(enumElement)));
                    codeDescription = filterASCII(nodeDocumentationXPath.evaluate(enumElement));
                } else
                    Log.trace("importCodeList: XPathExpression 'xe' is null, skipping code description evaluation.");
            } catch (XPathExpressionException e) {
                Log.trace("getCodeList: error - cannot import code descriptions " + e.toString());
            }
            if (codeDescription != null && !codeDescription.isEmpty())
                codeList += filterEnum(value) + CODELIST_DEFINITION_DELIMITER + filterEnumDefinition(codeDescription) + CODELIST_DELIMITER + " ";
            else
                codeList += filterEnum(value) + CODELIST_DELIMITER + " ";

            // add enumerations to type
            if (type != null) {
                Log.debug("importCodeList: adding " + value + " to type " + type.name());
                UmlAttribute attribute;
                try {
                    attribute = UmlAttribute.create(type, filterUMLAttribute(value));
                } catch (RuntimeException re) {
                    attribute = UmlAttribute.create(type, filterUMLAttribute("Enum" + anonymousEnums++));
                    //Log.trace("importCodeList: error - cannot add attribute " + value + " in type " + type.name());
                    //continue;
                }
                if (attribute != null) {
                    attribute.set_DefaultValue(value);
                    attribute.set_Description(codeDescription);
                }
            }
        }
        return codeList;
    }

    /**
     * import NIEM reference model elements
     *
     * @param db
     * @param filename
     * @return namespace of imported elements as a Namespace
     */
    Namespace importElements(Document doc, String filename) {
        // trace("importElements: importing elements from schema " + filename);
        String filename2 = "\n" + filename + "\n";
        Namespace ns = null;
        Node root = null;
        if (doc != null) {
            root = doc.getDocumentElement();
            ns = importNamespaces(doc);
        } else
            filename2 = "";
        //recompileXPaths();

        // get target and default prefixes
        if (ns == null) {
            Log.trace("importElements: error - namespace is null");
            return null;
        }
        UmlClassView targetClassView = ns.getReferenceClassView();
        if (targetClassView == null) {
            Log.trace("importElements: error - target classview is null");
            return ns;
        }
        String targetPrefix = NamespaceModel.getPrefix(targetClassView);
        String defaultSchemaURI = getDefaultSchemaURI(doc);
        Namespace defaultNamespace = NamespaceModel.getNamespace(defaultSchemaURI);
        if (defaultNamespace == null)
            defaultNamespace = ns;
        UmlClassView defaultClassView = defaultNamespace.getReferenceClassView();
        String defaultPrefix = NamespaceModel.getPrefix(defaultClassView);

        // import elements
        // trace("importElement: importing elements");
        NodeList elementList = null;
        try {
            if (doc != null)
                elementList = (NodeList) elementXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace("importElements: error - cannot import element " + e.toString());
        }

        if (elementList != null) {
            for (int elementIndex = 0; elementIndex < elementList.getLength(); elementIndex++) {
                Element elementElement = (Element) elementList.item(elementIndex);
                String elementName = elementElement.getAttribute("name");
                if (NamespaceModel.getPrefix(elementName) == null)
                    elementName = NamespaceModel.getPrefixedName(targetPrefix, elementName);
                String abstractAttribute = elementElement.getAttribute("abstract");
                String baseTypeSchemaURI = null;
                String baseTypeName = elementElement.getAttribute("type");
                UmlClass baseType;
                if (abstractAttribute.equals("true") || (baseTypeName.isEmpty()))
                    baseType = getAbstractType();
                else {
                    if (NamespaceModel.getPrefix(baseTypeName) == null)
                        baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
                    String baseTypePrefix = NamespaceModel.getPrefix(baseTypeName);
                    if (baseTypePrefix != null && doc != null)
                        baseTypeSchemaURI = doc.lookupNamespaceURI(baseTypePrefix);
                    else
                        Log.trace("Namespace prefix for baseTypeName is null: " + baseTypeName);
                    if (baseTypeSchemaURI == null)
                        baseTypeSchemaURI = NamespaceModel.getSchemaURI(baseTypeName);
                    if (baseTypeName != null && baseTypeSchemaURI == null && NamespaceModel.getPrefix(baseTypeName) == null) {
                        baseTypeSchemaURI = XSD_URI;
                        baseTypeName = NamespaceModel.getPrefixedName(XSD_PREFIX, baseTypeName);
                    }
                    baseType = getType(baseTypeSchemaURI, baseTypeName);
                }
                UmlClassInstance newElement = null;
                try {
                    if (baseType == null && baseTypeName != null && !baseTypeName.isEmpty())
                        Log.trace("importElements: error - base type " + baseTypeName + " not in model");
                    else if (nodeDocumentationXPath != null)
                        newElement = addElement(ns.getSchemaURI(), elementName, baseType, nodeDocumentationXPath.evaluate(elementElement), null);
                    else
                        Log.trace("addElement: error - XPathExpression 'xe' is null, skipping element evaluation.");
                } catch (XPathExpressionException e) {
                    Log.trace("importElements: error - cannot import element " + e.toString());
                }
                if (newElement != null) {
                    // substitution groups
                    String substitutionGroup = elementElement.getAttribute("substitutionGroup");
                    if (substitutionGroup != null && !substitutionGroup.isEmpty())
                        newElement.set_PropertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY, substitutionGroup);

                    // nillable
                    String nillable = elementElement.getAttribute("nillable");
                    if (nillable != null)
                        newElement.set_PropertyValue(NiemUmlModel.NILLABLE_PROPERTY, nillable);
                }
            }
        }

        // import attributes
        // trace("importElement: importing attributes");
        NodeList attributeList = null;
        try {
            if (doc != null)
                attributeList = (NodeList) attributeNameXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace("importElements: error - cannot import attributes " + e.toString());
        }
        if (attributeList != null) {
            for (int attributeIndex = 0; attributeIndex < attributeList.getLength(); attributeIndex++) {
                Element attribute = (Element) attributeList.item(attributeIndex).cloneNode(true);
                String attributeName = attribute.getAttribute("name");
                String attributePrefix = NamespaceModel.getPrefix(attributeName);
                if (attributePrefix == null)
                    attributePrefix = targetPrefix;
                attributeName = NamespaceModel.getPrefixedAttributeName(attributePrefix, NamespaceModel.getName(attributeName));
                // trace("importElements: adding attribute " + elementName);
                String codeList = "";
                String baseTypeSchemaURI = null;
                String baseTypeName = attribute.getAttribute("type");
                if (baseTypeName.isEmpty())
					try {
                    Element restriction = (Element) restrictionBaseXPath.evaluate(attribute, XPathConstants.NODE);
                    if (restriction != null) {
                        Node restrictionNode = restriction.cloneNode(true);
                        baseTypeName = restriction.getAttribute("base");
                        NodeList eList = (NodeList) enumerationValueXPath.evaluate(restrictionNode, XPathConstants.NODESET);
                        codeList = importCodeList(eList);
                    } else
                        baseTypeName = listXPath.evaluate(attribute);
                } catch (XPathExpressionException e) {
                    Log.trace(filename + "\nimportElements: error importing base types for attribute "
                            + attributeName + " " + e.toString());
                }
                if (baseTypeName == null || baseTypeName.isEmpty())
                    baseTypeName = null;
                else {
                    if (NamespaceModel.getPrefix(baseTypeName) == null)
                        baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
                    if (doc != null)
                        baseTypeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName));
                    if (baseTypeSchemaURI == null)
                        baseTypeSchemaURI = NamespaceModel.getSchemaURI(baseTypeName);
                    if (baseTypeName != null && baseTypeSchemaURI == null && NamespaceModel.getPrefix(baseTypeName) == null) {
                        baseTypeSchemaURI = XSD_URI;
                        baseTypeName = NamespaceModel.getPrefixedName(XSD_PREFIX, baseTypeName);
                    }
                }
                UmlClassInstance element = null;
                try {
                    UmlClass baseType2 = getType(baseTypeSchemaURI, baseTypeName);
                    if (baseType2 == null && baseTypeName != null && !baseTypeName.isEmpty())
                        Log.trace("importElements: error - base type " + baseTypeName + " not in model");
                    else if (nodeDocumentationXPath != null)
                        element = addElement(ns.getSchemaURI(), attributeName, baseType2, nodeDocumentationXPath.evaluate(attribute), null);
                    else
                        Log.trace("addElement: error - XPathExpression 'xe' is null, skipping element evaluation.");
                } catch (XPathExpressionException e) {
                    Log.trace(filename2 + "importElements: error - cannot add attribute " + attributeName + " of type "
                            + baseTypeName + " " + e.toString());
                    filename2 = "";
                }
                if (element != null) {
                    if (!codeList.isEmpty())
                        NiemUmlModel.setCodeList(element, codeList);
                }
            }
        }
        return ns;
    }

    /**
     * import NIEM reference model elements in types
     *
     * @param db
     * @param filename
     * @return namespace of imported elements in types as a Namespace
     */
    Namespace importElementsInTypes(Document doc, String filename) {
        // trace("importElementsInTypes: importing elements in types from schema " + filename);
        // String filename2 = "\n" + filename + "\n";
        Namespace ns = null;
        Node root = null;
        if (doc != null) {
            root = doc.getDocumentElement();
            ns = importNamespaces(doc);
        }
        //recompileXPaths();

        // get target and default prefixes
        if (ns == null) {
            Log.trace("importElementsInTypes: error - namespace not found");
            return ns;
        }
        UmlClassView targetClassView = ns.getReferenceClassView();
        if (targetClassView == null) {
            Log.trace("importElementsInTypes: error - target classview is null");
            return ns;
        }
        String targetPrefix = NamespaceModel.getPrefix(targetClassView);
        String defaultSchemaURI = getDefaultSchemaURI(doc);
        Namespace defaultNamespace = NamespaceModel.getNamespace(defaultSchemaURI);
        if (defaultNamespace == null)
            defaultNamespace = ns;
        UmlClassView defaultClassView = defaultNamespace.getReferenceClassView();
        String defaultPrefix = NamespaceModel.getPrefix(defaultClassView);

        // import attributes in attribute groups
        // Log.trace("importElementsInTypes: import attributes in attribute groups");
        NodeList attributeGroupList = null;
        try {
            if (root != null)    
                attributeGroupList = (NodeList) attributeGroupNameXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
        }
        if (attributeGroupList != null) {
            for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupList.getLength(); attributeGroupIndex++) {
                Element attributeGroupElement = (Element) attributeGroupList.item(attributeGroupIndex).cloneNode(true);
                String attributeGroupName = attributeGroupElement.getAttribute("name");
                if (NamespaceModel.getPrefix(attributeGroupName) == null)
                    attributeGroupName = NamespaceModel.getPrefixedAttributeName(targetPrefix, attributeGroupName);
                UmlClass attributeGroup = getType(ns.getSchemaURI(), attributeGroupName);
                if (attributeGroup == null) {
                    Log.trace("importElementsInType: error - attribute group " + attributeGroupName
                            + " not in reference model");
                    continue;
                }
                NodeList attributeList = null;
                try {
                    attributeList = (NodeList) attributeRefXPath.evaluate(attributeGroupElement, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
                }
                if (attributeList != null) {
                    for (int attributeIndex = 0; attributeIndex < attributeList.getLength(); attributeIndex++) {
                        Element attributeElement = (Element) attributeList.item(attributeIndex);
                        String attributeName = attributeElement.getAttribute("ref");
                        String attributePrefix = NamespaceModel.getPrefix(attributeName);
                        if (attributePrefix == null)
                            attributePrefix = defaultPrefix;
                        attributeName = NamespaceModel.getPrefixedAttributeName(attributePrefix, NamespaceModel.getName(attributeName));
                        String multiplicity = (attributeElement.getAttribute("use")).equals("required") ? "1,1" : "0,1";
                        String attributeSchemaURI = null;
                        if (doc != null)
                            attributeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName));
                        if (attributeSchemaURI == null)
                            attributeSchemaURI = NamespaceModel.getSchemaURI(attributeName);
                        if (attributeSchemaURI == null) {
                            Log.trace("importElementInType: error - prefix for attribute " + attributeName + " not in model");
                            continue;
                        }
                        UmlClassInstance element = getElement(attributeSchemaURI, NamespaceModel.getName(attributeName));
                        if (element == null) {
                            Log.trace("importElementsInType: error - attribute " + attributeName + " not in model");
                            continue;
                        }
                        UmlAttribute attribute = addElementInType(attributeGroup, element, multiplicity);
                        if (attribute == null)
                            Log.trace("importElementsInTypes: error adding attribute " + attributeName + " to group " + attributeGroupName);
                        else {
                            attribute.set_PropertyValue(NiemUmlModel.SEQUENCE_ID_PROPERTY, String.valueOf(attributeIndex));
                            Log.debug("importElementsInTypes: imported attribute " + attributeName + " in group " + attributeGroupName);
                        }
                    }
                }
            }
        }

        // import base types for simple types (codes)
        // trace("importElementsInTypes: import base types for simple types (codes)");
        NodeList simpleTypeNodeList = null;
        try {
            simpleTypeNodeList = (NodeList) restrictionBaseXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace(filename + "\nimportElementsInTypes: error importing base types for simple types " + e.toString());
        }
        if (simpleTypeNodeList != null) {
            for (int elementIndex = 0; elementIndex < simpleTypeNodeList.getLength(); elementIndex++) {
                Element restrictionElement = (Element) simpleTypeNodeList.item(elementIndex);
                Element typeElement = (Element) restrictionElement.getParentNode();
                String typeName = typeElement.getAttribute("name");
                if (NamespaceModel.getPrefix(typeName) == null)
                    typeName = NamespaceModel.getPrefixedName(targetPrefix, typeName);
                String baseTypeName = restrictionElement.getAttribute("base");
                if (NamespaceModel.getPrefix(baseTypeName) == null)
                    baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
                UmlClass type = getType(ns.getSchemaURI(), typeName);
                if (type == null)
                    continue;
                UmlClass baseType = null;
                if (doc != null)
                    baseType = getType(doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName)), baseTypeName);
                if (baseType == null)
                    continue;
                relateBaseType(type, baseType);
            }
        }

        // import base types and elements for complex types
        // trace("importElementsInTypes: import base types and elements for complex
        // types");
        NodeList complexTypeNodeList = null;
        try {
            complexTypeNodeList = (NodeList) complexTypeXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace(filename + "\nimportElementsInTypes: error parsing types" + e.toString());
        }
        if (complexTypeNodeList != null) {
            for (int elementIndex = 0; elementIndex < complexTypeNodeList.getLength(); elementIndex++) {
                int sequenceID = 0;
                Element typeElement = (Element) complexTypeNodeList.item(elementIndex).cloneNode(true);
                if (typeElement == null)
                    continue;
                String typeName = typeElement.getAttribute("name");
                if (NamespaceModel.getPrefix(typeName) == null)
                    typeName = NamespaceModel.getPrefixedName(targetPrefix, typeName);
                
                UmlClass type = getType(ns.getSchemaURI(), typeName);
                if (type == null)
                    continue;

                // import base types for complex types
                // trace("importElementsInTypes: import base types for complex types");
                NodeList baseTypeList = null;
                try {
                    baseTypeList = (NodeList) baseTypeXPath.evaluate(typeElement, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    Log.trace(filename + "\nimportElementsInTypes: error importing base types for complex types"
                            + e.toString());
                }
                if (baseTypeList != null) {
                    for (int baseTypeIndex = 0; baseTypeIndex < baseTypeList.getLength(); baseTypeIndex++) {
                        Element baseTypeElement = (Element) baseTypeList.item(baseTypeIndex);
                        String baseTypeName = baseTypeElement.getAttribute("base");
                        if (NamespaceModel.getPrefix(baseTypeName) == null)
                            baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
                        UmlClass baseType = null;
                        if (doc != null)
                            baseType = getType(doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName)), baseTypeName);
                        if (baseType == null)
                            continue;
                        relateBaseType(type, baseType);
                    }
                }

                // import attribute groups in type
                // trace("importElementsInTypes: import attributes groups in types");
                NodeList attributeGroupInTypeNodeList = null;
                try {
                    attributeGroupInTypeNodeList = (NodeList) attributeGroupRefXPath.evaluate(typeElement, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    Log.trace(filename + "\nimportElementsInTypes:error importing attributeGroup in complex type" + typeName + " "
                            + e.toString());
                }
                if (attributeGroupInTypeNodeList != null) {
                    for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupInTypeNodeList
                            .getLength(); attributeGroupIndex++) {
                        Element attributeGroupElement = (Element) attributeGroupInTypeNodeList.item(attributeGroupIndex);
                        String attributeGroupName = attributeGroupElement.getAttribute("ref");
                        if (NamespaceModel.getPrefix(attributeGroupName) == null)
                            attributeGroupName = NamespaceModel.getPrefixedName(defaultPrefix, attributeGroupName);
                        String prefix = NamespaceModel.getPrefix(attributeGroupName);
                        String schemaURI = null;
                        if (doc != null)
                            schemaURI = (prefix == null) ? ns.getSchemaURI() : doc.lookupNamespaceURI(prefix);
                        UmlClass attributeGroupType = getType(schemaURI, NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeGroupName), NamespaceModel.getName(attributeGroupName)));
                        if (attributeGroupType == null)
                            continue;
                        relateAttributeGroup(type, attributeGroupType);
                    }
                }

                // import attributes in type
                // trace("importElementsInTypes: import attributes in types");
                NodeList attributeInTypeNodeList = null;
                try {
                    attributeInTypeNodeList = (NodeList) attributeRefXPath.evaluate(typeElement, XPathConstants.NODESET);
                } catch (XPathExpressionException re) {
                    Log.trace(filename + "\nimportElementsInTypes: error importing attributes in type " + typeName + " " + re.toString());
                }
                if (attributeInTypeNodeList != null) {
                    for (int attributeIndex = 0; attributeIndex < attributeInTypeNodeList.getLength(); attributeIndex++) {
                        Element attributeElement = (Element) attributeInTypeNodeList.item(attributeIndex);
                        String attributeName = attributeElement.getAttribute("ref");
                        attributeName = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), NamespaceModel.getName(attributeName));
                        if (NamespaceModel.getPrefix(attributeName) == null)
                            attributeName = NamespaceModel.getPrefixedAttributeName(defaultPrefix, NamespaceModel.getName(attributeName));

                        Log.debug("importElementsInTypes: importing attribute " + attributeName + " in type " + typeName);
                        String multiplicity = (attributeElement.getAttribute("use").equals("required")) ? "1,1" : "0,1";
                        String attributeSchemaURI = null;
                        if (doc != null)
                            attributeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName));
                        //String attributeName2 = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), attributeName);
                        Log.debug("importElementsInTypes: attribute SchemaURI " + attributeSchemaURI + " attributeName " + attributeName);
                        UmlClassInstance element = getElement(attributeSchemaURI, attributeName);
                        //UmlClassInstance element = getElement(doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName)),
                        //		NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), attributeName));
                        if (element == null) {
                            Log.trace("importElementsInTypes: error - attribute " + attributeName + " not in model");
                            continue;
                        }
                        
                        UmlAttribute attributeInType = addElementInType(type, element, multiplicity);
                        if (attributeInType == null)
                            Log.trace("importElementsInTypes: error adding attribute " + attributeName + " to type" + typeName);
                        else {
                            attributeInType.set_PropertyValue(NiemUmlModel.SEQUENCE_ID_PROPERTY, String.valueOf(sequenceID++));
                            Log.debug("importElementsInTypes: imported attribute " + attributeName + " in type " + typeName);
                        }
                    }
                }

                // import elements in type
                // trace("importElementsInTypes: import elements in types");
                NodeList elementInTypeNodeList = null;
                try {
                    elementInTypeNodeList = (NodeList) elementRefXPath.evaluate(typeElement, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    Log.trace(filename + "\nimportElementsInTypes: error importing elements in type " + e.toString());
                }
                String elementName = null;
                try {
                    if (elementInTypeNodeList != null) {
                        for (int elementInTypeIndex = 0; elementInTypeIndex < elementInTypeNodeList
                                .getLength(); elementInTypeIndex++) {
                            Element elementElement = (Element) elementInTypeNodeList.item(elementInTypeIndex);
                            elementName = elementElement.getAttribute("ref");
                            // trace("importElementsInTypes: adding element " + elementName + " to type " +
                            // typeName);
                            if (NamespaceModel.getPrefix(elementName) == null)
                                elementName = NamespaceModel.getPrefixedName(defaultPrefix, elementName);
                            String minOccurs = elementElement.getAttribute("minOccurs");
                            if (minOccurs.isEmpty())
                                minOccurs = "1";
                            String maxOccurs = elementElement.getAttribute("maxOccurs");
                            if (maxOccurs.isEmpty())
                                maxOccurs = "1";
                            String multiplicity = minOccurs + "," + maxOccurs;
                            String elementSchemaURI = null;
                            if (doc != null)
                                elementSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(elementName));
                            if (elementSchemaURI == null)
                                elementSchemaURI = NamespaceModel.getSchemaURI(elementName);
                            if (elementSchemaURI == null) {
                                Log.trace("importElementsInType: error - prefix for element " + elementName + " not in model");
                                continue;
                            }
                            UmlClassInstance element = getElement(elementSchemaURI, elementName);
                            if (element == null) {
                                Log.trace("importElementsInType: error - element " + getURI(elementSchemaURI, elementName)
                                        + " not in reference model");
                                continue;
                            }
                            UmlAttribute attribute = addElementInType(type, element, multiplicity);
                            if (attribute == null)
                                Log.trace("importElementsInTypes: error adding element " + elementName + " to type" + typeName);
                            else {
                                attribute.set_PropertyValue(NiemUmlModel.SEQUENCE_ID_PROPERTY, String.valueOf(sequenceID++));
                                Log.debug("importElementsInTypes: imported element " + elementName + " in type " + typeName);
                            }
                        }
                    }
                } catch (RuntimeException re) {
                    Log.trace(filename + "\nimportElementsInTypes: error importing element " + elementName + " in type "
                            + typeName + " " + re.toString());
                }
            }
        }
        return ns;
    }

    /**
     * @param type
     * @param elist
     * @return facets in schema as a String
     */
    private String importFacets(UmlClass type, NodeList elist) {
        //if (type != null)
        //	Log.debug("importCodeList: adding facets to " + type.name());
        String facets = "";

        int length = elist.getLength();
        if (length == 0)
            return facets;

        String truncated = "false";
        String maxEnumsString = NiemUmlModel.getProperty(ProjectProperties.IMPORT_MAX_FACETS);
        if (maxEnumsString != null && !maxEnumsString.isEmpty()) {
            try {
                Integer maxEnums = Integer.valueOf(maxEnumsString);
                if (length > maxEnums) {
                    Log.trace("importFacets: truncated facet restrictions on " + type.name() + " from " + length + " to " + maxEnums);
                    length = maxEnums;
                    truncated = "true";
                }
            } catch (NumberFormatException e) {
                Log.trace("importFacets: error - cannot parse max facets value " + maxEnumsString  + " " + e.toString());
            }
        }
        if (type != null)
            type.set_PropertyValue(NiemUmlModel.TRUNCATED_PROPERTY, truncated);
        
        for (int j = 0; j < length; j++) {
            Element facetElement = (Element) elist.item(j);
            //String value = filterEnum(filterASCII(enumElement.getAttribute("value")));
            String facetPrefix = NamespaceModel.getPrefix(facetElement.getTagName());
            if (!facetPrefix.equals(XSD_PREFIX)) {
                continue;
            }
            String facet = filterASCII(facetElement.getLocalName());
            String value = filterASCII(facetElement.getAttribute("value"));
            String facetDescription = null;
            String importCodeDescriptions = NiemUmlModel.getProperty(ProjectProperties.IMPORT_CODE_DESCRIPTIONS);
            if (importCodeDescriptions != null && !importCodeDescriptions.equals("false"))
				try {
                if (nodeDocumentationXPath != null) {
//						facetDescription = filterEnumDefinition(filterASCII(xe.evaluate(enumElement)));
                    facetDescription = filterASCII(nodeDocumentationXPath.evaluate(facetElement));
                } else
                    Log.trace("importCodeList: XPathExpression 'xe' is null, skipping facet description evaluation.");
            } catch (XPathExpressionException e) {
                Log.trace("getCodeList: error - cannot import facet descriptions " + e.toString());
            }
            //if (facetDescription != null && !facetDescription.isEmpty()) {
            //	codeList += filterEnum(value) + CODELIST_DEFINITION_DELIMITER + filterEnumDefinition(codeDescription) + CODELIST_DELIMITER + " ";
            //} else
            // codeList += filterEnum(value) + CODELIST_DELIMITER + " ";
            //if (!facet.equals("enumeration")) {
            facets += filterEnum(facet) + CODELIST_DEFINITION_DELIMITER + filterEnumDefinition(value) + CODELIST_DELIMITER + " ";

            //}
            // add facets to type
            if (type != null) {
                Log.debug("importCodeList: adding facet " + facet + " with value " + value + " to type " + type.name());
                UmlAttribute attribute;
                try {
                    attribute = UmlAttribute.create(type, filterUMLAttribute(facet));
                } catch (RuntimeException re) {
                    Log.trace("importCodeList: error - cannot add facet " + facet + " in type " + type.name());
                    continue;
                }
                if (attribute != null) {
                    attribute.set_Stereotype(NiemUmlModel.FACET_STEREOTYPE);
                    attribute.set_DefaultValue(value);
                    attribute.set_Description(facetDescription);
                }
            }
        }
        return facets;
    }

        /**
     * @param elist
     * @return local terms and literals code values in a schema as a String
     */
    protected String importLocalTerms(NodeList elist, String namespaceURI) {
        if (namespaceURI != null) {
            Log.debug("importLocalTerms: adding local terms to namespace " + namespaceURI);
        }
        String termList = "";
        int length = elist.getLength();
        if (length == 0)
            return termList;
        // String truncated = "false";
        String maxEnumsString = NiemUmlModel.getProperty(ProjectProperties.IMPORT_MAX_FACETS);
        if (maxEnumsString != null && !maxEnumsString.isEmpty()) {
            try {
                Integer maxEnums = Integer.valueOf(maxEnumsString);
                if (length > maxEnums) {
                    if (namespaceURI != null)
                        Log.trace("importLocalTerms: truncated local terms in namespace " + namespaceURI + " from " + length + " to " + maxEnums);
                    length = maxEnums;
                    // truncated = "true";
                }
            } catch (NumberFormatException e) {
                Log.debug("importCodeList: error - cannot parse maxEnums " + maxEnumsString + " " + e.toString());
            }
        }
        for (int j = 0; j < length; j++) {
            Element enumElement = (Element) elist.item(j);
            String term = filterASCII(enumElement.getAttribute("term"));
            String literal = filterASCII(enumElement.getAttribute("literal"));
            if (literal != null && !literal.isEmpty())
                termList += filterEnum(term) + CODELIST_DEFINITION_DELIMITER + filterEnumDefinition(literal) + CODELIST_DELIMITER + " ";
            else
                termList += filterEnum(term) + CODELIST_DELIMITER + " ";
        }

        return termList;
    }

        /**
     * import namespaces and return target namespace
     *
     * @param doc
     * @return target namespace
     */
    Namespace importNamespaces(Document doc) {

        // get target namespace
        Namespace ns = null;
        String targetSchemaURI = null;
        //Node doc2 = doc.cloneNode(true);
        try {
            //NiemModel.xPath.setNamespaceContext(new NamespaceResolver(doc, true));
            targetSchemaURI = NiemModel.namespaceXPath.evaluate(doc);
            if (targetSchemaURI == null)
                targetSchemaURI = NiemModel.LOCAL_URI;
            Log.debug("importNamespaces: target schema URI " + targetSchemaURI);

            // create namespaces and classviews
            NamedNodeMap nslist = doc.getDocumentElement().getAttributes();
            for (int nsIndex = 0; nsIndex < nslist.getLength(); nsIndex++) {
                Node attributeNode = nslist.item(nsIndex);
                String attributeNodeName = attributeNode.getNodeName();
                String schemaURI = attributeNode.getNodeValue();
                // parser filters "xml" namespace definition so it is hardcoded
                if (schemaURI.equals(XMLConstants.XML_NS_URI))
                    attributeNodeName = NamespaceModel.getPrefixedName(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XML_NS_PREFIX);
                Log.debug("importNamespaces: processing attribute " + attributeNodeName);
                if (attributeNodeName.startsWith(XMLConstants.XMLNS_ATTRIBUTE) && !attributeNodeName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {

                    String prefix = attributeNodeName.substring(6);
                    UmlClassView classView = NamespaceModel.getNamespaceClassView(NiemUmlModel.getReferenceModel(), prefix, schemaURI);

                    // get target namespace description
                    if (schemaURI.equals(targetSchemaURI) && classView != null) {
                        classView.set_Description(NiemModel.schemaDocumentationXPath.evaluate(doc));

                        // get local terms
                        NodeList elist = (NodeList) localTermsXPath.evaluate(doc, XPathConstants.NODESET);
                        String localTerms = importLocalTerms(elist, targetSchemaURI);
                        if (!localTerms.isEmpty())
                            classView.set_PropertyValue(NiemUmlModel.LOCALTERM_PROPERTY, localTerms);
                    }
                }
            }
            ns = NamespaceModel.getNamespace(targetSchemaURI);
            if (ns == null) {
                UmlClassView classView = NamespaceModel.getNamespaceClassView(NiemUmlModel.getReferenceModel(), targetSchemaURI, targetSchemaURI);
                ns = NamespaceModel.getNamespace(targetSchemaURI);
                if (classView != null) {
                    classView.set_Description(NiemModel.schemaDocumentationXPath.evaluate(doc));
                    NodeList elist = (NodeList) localTermsXPath.evaluate(doc, XPathConstants.NODESET);
                    String localTerms = importLocalTerms(elist, targetSchemaURI);
                    if (!localTerms.isEmpty())
                        classView.set_PropertyValue(NiemUmlModel.LOCALTERM_PROPERTY, localTerms);
                }
            }
        } catch (RuntimeException | XPathExpressionException e) {
            Log.trace("importNamespaces: error - could not create namespace for schema " + targetSchemaURI);
        }

        return ns;
    }

    /**
     * import NIEM reference model types
     *
     * @param db
     * @param filename
     * @return namespace of imported types as a Namespace
     */
    Namespace importTypes(Document doc, String filename) {
        Log.debug("importTypes: importing types from schema " + filename);
        String filename2 = "\n" + filename + "\n";
        Namespace ns = null;
        Node root = null;

        if (doc != null) {
            root = doc.getDocumentElement();
            ns = importNamespaces(doc);
        } else
            filename2 = "";
        //recompileXPaths();

        // get target and default prefixes
        if (ns == null) {
            Log.trace("importTypes: error - could not import target namespace in " + filename);
            return null;
        }
        UmlClassView classView = ns.getReferenceClassView();
        if (classView == null) {
            Log.trace("importTypes: error - classview does not exist");
            return ns;
        }
        String targetPrefix = NamespaceModel.getPrefix(classView);

        // import types
        NodeList typeList = null;
        try {
            if (root != null)
                typeList = (NodeList) typeNameXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace(filename2 + "importTypes: error - cannot parse types " + e.toString());
        }
        if (typeList != null) {
            for (int typeIndex = 0; typeIndex < typeList.getLength(); typeIndex++) {
                Element typeElement = (Element) typeList.item(typeIndex);
                String nodeType = typeElement.getNodeName();
                String typeName = typeElement.getAttribute("name");
                if (NamespaceModel.getPrefix(typeName) == null)
                    typeName = NamespaceModel.getPrefixedName(targetPrefix, typeName);
                UmlClass type = null;
                try {
                    if (nodeDocumentationXPath != null)
                        type = addType(ns.getSchemaURI(), typeName, nodeDocumentationXPath.evaluate(typeElement), null);
                } catch (XPathExpressionException e) {
                    Log.trace(filename2 + "importTypes: error - cannot add type " + typeName + " to schema " + ns.getSchemaURI() + " "
                            + e.toString());
                }
                if (type == null) {
                    Log.trace(filename2 + "importTypes: error - cannot add type " + typeName + " to schema " + ns.getSchemaURI());
                    continue;
                }

                // import restrictions
                if ("xs:simpleType".equals(nodeType)) {
                    // import enumerated values for simple types (codes)
                    NodeList elist = null;
                    try {
                        if (enumerationXPath != null)
                            elist = (NodeList) enumerationXPath.evaluate(typeElement, XPathConstants.NODESET);
                    } catch (XPathExpressionException e) {
                        Log.trace(filename2 + "importTypes: error - cannot import enumerations " + e.toString());
                    }
                    String codeList = importCodeList(type, elist);
                    if (!codeList.isEmpty())
                        type.set_Stereotype(NiemUmlModel.ENUM_STEREOTYPE);

                    // import facets
                    NodeList elist2 = null;
                    try {
                        if (facetXPath != null)
                            elist2 = (NodeList) facetXPath.evaluate(typeElement, XPathConstants.NODESET);
                    } catch (XPathExpressionException e) {
                        Log.trace(filename2 + "importTypes: error - cannot import facets " + e.toString());
                    }
                    String  facets = importFacets(type, elist2);
                    if (!facets.isEmpty())
                        type.set_Stereotype(NiemUmlModel.ENUM_STEREOTYPE);
                }
            }
        }

        // import attribute groups
        NodeList attributeGroupList = null;
        try {
            if (root != null)
                attributeGroupList = (NodeList) attributeGroupNameXPath.evaluate(root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            Log.trace(filename2 + "importTypes: error - cannot parse attribute groups " + e.toString());
        }
        if (attributeGroupList != null) {
            for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupList.getLength(); attributeGroupIndex++) {
                Element attributeGroupElement = (Element) attributeGroupList.item(attributeGroupIndex);
                String attributeGroupName = attributeGroupElement.getAttribute("name");
                String attributeGroupPrefix = NamespaceModel.getPrefix(attributeGroupName);
                if (attributeGroupPrefix == null)
                    attributeGroupPrefix = targetPrefix;
                attributeGroupName = NamespaceModel.getPrefixedAttributeName(attributeGroupPrefix, NamespaceModel.getName(attributeGroupName));
                UmlClass attributeGroup = null;
                try {
                    if (nodeDocumentationXPath != null)
                        attributeGroup = addType(ns.getSchemaURI(), attributeGroupName, nodeDocumentationXPath.evaluate(attributeGroupElement), null);
                } catch (XPathExpressionException e) {
                    Log.trace(filename2 + "importTypes: error - cannot add attribute group " + attributeGroupName + " "
                            + e.toString());
                    filename2 = "";
                }
                if (attributeGroup == null)
                    Log.trace(filename2 + "importTypes: error - cannot add attribute group " + attributeGroupName + " to schema " + ns.getSchemaURI());
            }
        }
        return ns;
    }

    /**
     * identify if a type is the abstract type
     *
     * @param typeName
     * @return true if type is abstract
     */
    static boolean isAbstract(String typeName) {
        return typeName.equals(NiemModel.ABSTRACT_TYPE_NAME);
    }

    /**
     * identify if an element is an augmentation
     *
     * @param elementName
     * @return true if element is an augmentation
     */
    static boolean isAugmentation(String elementName) {
        return elementName.endsWith(NiemModel.AUGMENTATION_POINT_NAME) || elementName.endsWith(NiemModel.AUGMENTATION_NAME);
    }
    /**
     * identify if a type is an augmentation
     *
     * @param typeName
     * @return true if type is an augmentation
     */
    static boolean isAugmentationType(String typeName) {
        return typeName.endsWith(NiemModel.AUGMENTATION_TYPE_NAME);
    }

    /**
     * create a relationship between a type and an attribute group
     *
     * @param type
     * @param attributeGroupType
     */
    void relateAttributeGroup(UmlClass type, UmlClass attributeGroupType) {
        if (type != null && attributeGroupType != null)
			try {
            UmlRelation.create(aRelationKind.aDirectionalAggregation, type, attributeGroupType);
            //Log.trace("relateAttributeGroup: " + type.name() + " related to attribute group "+ attributeGroupType.name());
        } catch (RuntimeException re) {
            // trace("createSubsetAndExtension: " + typeName + " already related to
            // attribute group " + baseTypeName);
        }
    }

    /**
     * create a relationship between a type and a base type
     *
     * @param type
     * @param baseType
     */
    void relateBaseType(UmlClass type, UmlClass baseType) {
        if (type != null && baseType != null)
			try {
            UmlRelation.create(aRelationKind.aGeneralisation, type, baseType);
        } catch (RuntimeException re) {
            // trace("copyType: error - type " + type.name() + " already related to base
            // type " + sourceBaseTypeName);
        }
    }

    /**
     * relate element in type to element
     *
     * @param elementInType
     * @param element
     */
    private void relateElementInType(UmlAttribute elementInType, UmlClassInstance element) {
        UmlTypeSpec elementType = new UmlTypeSpec();
        elementType.type = getBaseType(element);
        if (elementType.type != null)
			try {
            elementInType.set_Type(elementType);
        } catch (RuntimeException re) {
            Log.trace("relateElementInType: error relating element in type to " + NamespaceModel.getPrefixedName(element) + " "
                    + re.toString());
        }
    }

    /**
     * add a description to type or element
     *
     * @param item
     * @param description
     */
    private void setDescription(UmlItem item, String description) {
        String currentDescription = item.description();
        if ((currentDescription.isEmpty()) && (description != null) && (!description.isEmpty()))
            item.set_Description(description);
    }

    /**
     * @param modelPackage
     */
    void setModelPackage(UmlPackage modelPackage) {
        this.modelPackage = modelPackage;
    }

    /**
     * add NIEM mapping notes to a type or element
     *
     * @param item
     * @param notes
     */
    private void setNotes(UmlItem item, String notes) {
        if ((item != null) && (notes != null) && (!notes.isEmpty())) {
            String currentNotes = item.propertyValue(NiemUmlModel.NOTES_PROPERTY);
            if (currentNotes == null) {
                currentNotes = notes;
            } else if (!currentNotes.contains(notes))
                currentNotes = currentNotes + "; " + notes;
            if (!currentNotes.isEmpty())
                item.set_PropertyValue(NiemUmlModel.NOTES_PROPERTY, currentNotes);
            }
        }
}
