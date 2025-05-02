package org.cabral.niemtools;

/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2017 James E. Cabral Jr., MTG Management Consultants LLC, jcabral@mtgmc.com, http://github.com/cabralje
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	private static final Boolean IMPORT_CODE_DESCRIPTIONS = true;
	// private static final Boolean _IMPORT_CODE_DESCRIPTIONS = false;

	static final String ABSTRACT_TYPE_NAME = "abstract";
	static final String ANY_ELEMENT_NAME = "any";
	static final String AUGMENTATION_TYPE_NAME = "AugmentationType";
	//static final String AUGMENTATION_POINT_NAME = "AugmentationPoint";
	static final String CODELIST_DEFINITION_DELIMITER = "=";
	static final String CODELIST_DELIMITER = ";";
	private static final String HASH_DELIMITER = ",";
	static final String LOCAL_PREFIX = "local";
	static final String LOCAL_URI = "local";
	private static final String OBJECT_TYPE_NAME = "ObjectType";
	private static final String SIMPLE_OBJECT_ATTRIBUTE_GROUP = "@SimpleObjectAttributeGroup";
	static Map<String, List<UmlClassInstance>> Substitutions = new HashMap<>();
	static final String URI_PROPERTY = "URI";
	static final String XML_PREFIX = XMLConstants.XML_NS_PREFIX;
	static final String XML_URI = XMLConstants.XML_NS_URI;
	private static final String[] XML_TYPE_NAMES = { "anyURI", "base64Binary", "boolean", "byte", "date", "dateTime",
			"decimal", "double", "duration", "ENTITIES", "ENTITY", "float", "gDay", "gMonth", "gMonthDay", "gYear",
			"gYearMonth", "hexBinary", "ID", "IDREF", "IDREFS", "int", "integer", "language", "long", "Name", "NCName",
			"negativeInteger", "NMTOKEN", "NMTOKENS", "nonNegativeInteger", "nonPositiveInteger", "normalizedString",
			"NOTATION", "positiveInteger", "QName", "short", "string", "time", "token", "unsignedByte", "unsignedInt",
			"unsignedLong", "unsignedShort" };

	private static final XPath xPath = XPathFactory.newInstance().newXPath();
	static final String XSD_PREFIX = "xs";
	static final String XSD_URI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	static final String PROXY_PREFIX = "niem-xs";
	//static final String PROXY_URI = "https://docs.oasis-open.org/niemopen/ns/model/adapters/niem-xs/6.0/";
	static final String STRUCTURES_PREFIX = "structures";
	static final String STRUCTURES_URI = "https://docs.oasis-open.org/niemopen/ns/model/structures/6.0/";
	private UmlClass abstractType = null;
	private UmlClass augmentationType = null;
	private final Map<String, UmlClassInstance> elements = new HashMap<>();
	private final Map<String, List<UmlClassInstance>> elementsInType = new HashMap<>();
	private UmlPackage modelPackage = null;
	private UmlClass objectType = null;
	private UmlClass simpleObjectAttributeGroup = null;
	private final Map<String, UmlClass> types = new HashMap<>();
	
	/**
	 * @param item
	 * @return base type related to a type or element as a UmlClass
	 */
	static UmlClass getBaseType(UmlItem item) {
		UmlClass baseType = null;
		switch (item.kind().value()) {
		case anItemKind._aClassInstance:
			UmlClassInstance classInstance = (UmlClassInstance) item;
			baseType = classInstance.type();
			break;
		case anItemKind._aClass:
			if (item == NiemUmlClass.getSubsetModel().getAbstractType() || item == NiemUmlClass.getReferenceModel().getAbstractType())
				return null;
			UmlClass type = (UmlClass) item;
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
				if (prefix.equals(STRUCTURES_PREFIX) || prefix.equals(XSD_PREFIX))
					return null;
				if (NamespaceModel.getName(item).endsWith(AUGMENTATION_TYPE_NAME))
					baseType = NiemUmlClass.getSubsetModel().getAugmentationType();
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
		itemName = itemName.replaceAll("[^-._:A-Za-z0-9]", "");
		return schemaURI + HASH_DELIMITER + NamespaceModel.getName(itemName).replaceAll(HASH_DELIMITER, "");
	}
	/**
	 * @param item
	 * @return URI of an item as a String
	 */
	static String getURI(UmlItem item) {
		return item.propertyValue(URI_PROPERTY);
	}

	/**
	 * @param elementSchemaURI
	 * @param elementName
	 * @param baseType
	 * @param description
	 * @param notes
	 * @return an element added to reference or extension model as a UmlClassInstance
	 */
	UmlClassInstance addElement(String elementSchemaURI, String elementName, UmlClass baseType, String description, String notes) {

		if (elementName.equals("") || elementName.equals("??"))
			return null;

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
				Log.trace("addElement: error - no prefix or schema for element " + elementName);
				return null;
			}
			try {
				String elementName2 = filterUMLElement(NamespaceModel.getName(elementName));
				Log.debug("addElement: adding element " + elementName2 + " to schema " + elementSchemaURI);
				element = UmlClassInstance.create(nsClassView, elementName2, baseType);
			} catch (RuntimeException e) {
				Log.trace("addElement: error adding element " + elementName + " of type " + baseType.name());
				return null;
			}
			if (element == null) {
				Log.trace("addElement: error adding element " + elementName + " of type " + baseType.name());
				return element;
			}
			element.set_Description(description);
			String uri = getURI(elementSchemaURI, elementName);
			element.set_PropertyValue(URI_PROPERTY, uri);
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
	 * @return an element in type added to reference model or extension as a UmlAttribute
	 */
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
				// trace("addElementInType: error - element " + elementInTypeName + " already
				// exists in type " + type.name());
				return null;
			}

		// copy element properties
		elementInType.set_PropertyValue(URI_PROPERTY, getURI(element));
		if (element.description() != null)
			elementInType.set_Description(element.description());
		if (multiplicity != null)
			elementInType.set_Multiplicity(multiplicity);

		// relate element in type to element
		relateElementInType(elementInType, element);

		// insert element in type list

		List<UmlClassInstance> elementInTypeList = NiemUmlClass.getModel(type).getElementsInType( getURI(type));
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
				Log.trace("addType: error - no prefix or schema for type " + typeName);
				return null;
			}
			try {
				String typeName2 = filterUMLType(NamespaceModel.getName(typeName));
				Log.debug("addType: adding " + typeName2 + " to schema " + typeSchemaURI);
				type = UmlClass.create(nsClassView, typeName2);
			} catch (RuntimeException re) {
				Log.trace("addType: error adding type " + typeName);
				return null;
			}
			if (type == null) {
				Log.trace("addType: error adding type " + typeName);
				return null;
			}
			String uri = getURI(typeSchemaURI, typeName);
			type.set_PropertyValue(URI_PROPERTY, uri);
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

		if (modelPackage == null)
			return;

		Log.debug("cacheModel: caching model " + modelPackage.name());
		// Cache namespaces, types and elements
		String schemaURI;

		Log.debug("cacheModel: caching namespaces, types and elements");
		for (UmlItem classView : modelPackage.children()) {
			if (classView.kind() != anItemKind.aClassView)
				continue;
			schemaURI = getURI(classView);
			String prefix = classView.propertyValue(NiemUmlClass.PREFIX_PROPERTY);
			NamespaceModel.addPrefix(schemaURI, prefix);
			Namespace ns = NamespaceModel.getNamespace(schemaURI);
			if ((ns == null) && (schemaURI != null)) {
				// create namespace
				ns = NamespaceModel.addNamespace(schemaURI);
			}
			if (this == NiemUmlClass.getReferenceModel())
				ns.setReferenceClassView((UmlClassView) classView);
			else
				ns.setNsClassView((UmlClassView) classView);

			if (this == NiemUmlClass.getReferenceModel() || this == NiemUmlClass.getExtensionModel())
				ns.setFilepath(classView.propertyValue(NiemUmlClass.FILE_PATH_PROPERTY));

			// cache types and elements
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
                                            String headElement = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
                                            try {
                                                if (headElement != null) {
                                                    List<UmlClassInstance> enlist = (Substitutions.get(headElement));
                                                    if (enlist == null)
                                                        enlist = new ArrayList<>();
                                                    if (!enlist.contains(element))
                                                        enlist.add(element);
                                                    Substitutions.put(headElement, enlist);
                                                }
                                            }
                                            catch (Exception e) {
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
		for (UmlClass type : types.values()) {
			String typeURI = getURI(type);
			List<UmlClassInstance> list = (elementsInType.get(typeURI));
			if (list == null) {
				list = new ArrayList<>();
				elementsInType.put(typeURI, list);
			}
			for (UmlItem attribute : type.children())
				if (attribute.kind() == anItemKind.anAttribute) {
					// trace("cacheModel: caching " + getURI(attribute));
					UmlClassInstance element = elements.get(getURI(attribute));
					if (!list.contains(element))
						list.add(element);
				}
		}

		Log.debug("cacheModel: store caches and add simple and abstract types");
		if (this == NiemUmlClass.getReferenceModel()) {
			// add local namespace and abstract type
			NamespaceModel.getNamespaceClassView(this, LOCAL_PREFIX, LOCAL_URI);
			abstractType = addType(LOCAL_URI,
					NamespaceModel.getPrefixedName(LOCAL_PREFIX, ABSTRACT_TYPE_NAME), null, null);
			// add XML namespace, simple types and any element
			NamespaceModel.getNamespaceClassView(this, XML_PREFIX, XML_URI);
			for (String typeName : XML_TYPE_NAMES)
				addType(XSD_URI, NamespaceModel.getPrefixedName(XSD_PREFIX, typeName), null, null);
			addElement(XSD_URI, NamespaceModel.getPrefixedName(XSD_PREFIX, ANY_ELEMENT_NAME), null, null, null);
		} else if (this == NiemUmlClass.getSubsetModel()) {
			abstractType = copyType(NamespaceModel.getPrefixedName(LOCAL_PREFIX, ABSTRACT_TYPE_NAME));
			augmentationType = copyType(NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, AUGMENTATION_TYPE_NAME));
			objectType = copyType(NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, OBJECT_TYPE_NAME));
			simpleObjectAttributeGroup = copyType(
					NamespaceModel.getPrefixedName(STRUCTURES_PREFIX, SIMPLE_OBJECT_ATTRIBUTE_GROUP));
			NamespaceModel.getNamespaceClassView(null, XSD_PREFIX, XSD_URI);
			copyElement(NamespaceModel.getPrefixedName(XSD_PREFIX, ANY_ELEMENT_NAME));
			copyType("xs:NCName"); // JSON-LD @id is type xs:NCName
		} else if (this == NiemUmlClass.getExtensionModel()) {
			abstractType = NiemUmlClass.getSubsetModel().getAbstractType();
			augmentationType = NiemUmlClass.getSubsetModel().getAugmentationType();
			simpleObjectAttributeGroup = NiemUmlClass.getSubsetModel().getSimpleObjectAttributeGroup();
		}
		Log.debug("cacheModels: model " + modelPackage.name() + " cached");
	}

	/**
	 * @param elementName
	 * @return an element copied from reference model to subset as a UmlClassInstance
	 */
	UmlClassInstance copyElement(String elementName) {

		// return subset element if it exists
		String schemaURI = NamespaceModel.getSchemaURI(elementName);
		UmlClassInstance element = getElement(schemaURI, elementName);
		if (element != null)
			return element;

		// if element doesn't exist in reference model, return error
		UmlClassInstance sourceElement = NiemUmlClass.getReferenceModel().getElement(schemaURI, elementName);
		if (sourceElement == null) {
			Log.trace("copyElement: error - element " + elementName + " not in reference model");
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
			Log.trace("addElement: error - no prefix or schema for element " + elementName);
			return null;
		}

		// create element
		try {
			element = UmlClassInstance.create(nsClassView, filterUMLElement(NamespaceModel.getName(elementName)), baseType);
		} catch (RuntimeException e) {
			Log.trace("copyElement: error copying element " + elementName + " to subset");
			return null;
		}
		element.set_Description(sourceElement.description());
		String uri = getURI(schemaURI, elementName);
		element.set_PropertyValue(URI_PROPERTY, uri);
		elements.put(uri, element);
		Log.debug("copyElement: element " + elementName + " copied to subset");
		return element;
	}
	/**
	 * @param type
	 * @param element
	 * @param multiplicity
	 * @return an element in type copied from reference model to subset as a UmlAttribute
	 */
	UmlAttribute copyElementInType(UmlClass type, UmlClassInstance element, String multiplicity) {
		String elementInTypeName = NamespaceModel.getPrefixedName(element);
		String typeName = NamespaceModel.getPrefixedName(type);
		UmlAttribute attribute = getElementInType(type, elementInTypeName, multiplicity);
		if (attribute == null)
			try {
				attribute = UmlAttribute.create(type, elementInTypeName);
			} catch (RuntimeException re) {
				Log.debug("copyElementInType: error - attribute already exists " + element + " " + re.toString());
			}
		if (attribute == null)
			Log.trace("copyElementInType: error copying element " + elementInTypeName + " in " + typeName);
		else {
			attribute.set_Description(element.description());
			attribute.set_PropertyValue(URI_PROPERTY, getURI(element));
			relateElementInType(attribute, element);
			attribute.set_Multiplicity(multiplicity);
		}
		Log.debug("addElementInTypes: inserted " + elementInTypeName + " to " + typeName);
		return attribute;
	}
	/**
	 * @param typeName
	 * @return a type copied from the reference model to subset as a UmlClass
	 */
	UmlClass copyType(String typeName) {

		if ((typeName == null) || (typeName.equals("")))
			return null;
		String schemaURI = NamespaceModel.getSchemaURI(typeName);
		if (schemaURI == null) {
			Log.trace("copyType: error - schema for type " + typeName + " not in reference model");
			return null;
		}

		// return subset type if it exists
		UmlClass type = getType(schemaURI, typeName);
		if (type != null)
			return type;

		// find reference type
		UmlClass sourceType = NiemUmlClass.getReferenceModel().getType(schemaURI, typeName);
		if (sourceType == null) {
			Log.trace("copyType: error - type " + typeName + " not in reference model");
			return null;
		}

		// if subset namespace doesn't exist, create it
		UmlClassView nsClassView = NamespaceModel.getNamespaceClassView(this, NamespaceModel.getPrefix(typeName), schemaURI);
		if (nsClassView == null) {
			Log.trace("copyType: error - no prefix or schema for type " + typeName);
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

		// copy type properties
		if (sourceType.description() != null)
			type.set_Description(sourceType.description());
		type.set_PropertyValue(URI_PROPERTY, getURI(sourceType));
		String codeList = sourceType.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		if (codeList != null)
			type.set_PropertyValue(NiemUmlClass.CODELIST_PROPERTY, codeList);
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
		for (UmlItem item : sourceType.children())
			if (NiemUmlClass.isAttribute(item)) {
				UmlClassInstance sourceElement = NiemUmlClass.getReferenceModel().getElementByURI(getURI(item));
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
	/** exports NIEM extension and exchange schema 
	 * @param xmlDir
	 * @param jsonDir
	 * @return JSON definitions as a Set
	 */
	TreeSet<String> exportSchemas(String xmlDir, String jsonDir) {
	
		XmlWriter xmlWriter = new XmlWriter(xmlDir);
		JsonWriter jsonWriter = new JsonWriter(jsonDir);
		boolean exportXML = (xmlDir != null);
		boolean exportJSON = (jsonDir != null);
		TreeSet<String> openapiDefinitions = new TreeSet<>();
		
		// export each schema
		for (UmlItem thisPackage : modelPackage.children()) {
			if (thisPackage.kind() != anItemKind.aClassView)
				continue;
			UmlClassView classView = (UmlClassView) thisPackage;
			// classView.sort();
			String prefix = classView.propertyValue(NiemUmlClass.PREFIX_PROPERTY);
			if (prefix == null || prefix.equals("")) {
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
						for (UmlItem item2 : type.children())
							if (item2.kind() == anItemKind.anAttribute) {
								NiemModel model2 = NiemUmlClass.getModel(getURI(item2));
								UmlClassInstance element = model2.getElementByURI(getURI(item2));
								if (element != null)
									schemaNamespaces.add(NamespaceModel.getPrefix(element));
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
						if (exportJSON) {
							jsonType = (prefix.equals(NiemModel.XSD_PREFIX)) ? jsonWriter.exportJsonPrimitiveSchemafromXML(type)
									: jsonWriter.exportJsonTypeSchema(this, type, prefix);
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
						String headElement = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
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
						if (xmlElement != null) {
							xmlElements.add(xmlElement);
						}
						if (baseType != NiemUmlClass.getSubsetModel().getAbstractType()) {
							schemaNamespaces.add(NamespaceModel.getPrefix(element));
							if (exportJSON) {
								jsonElement = jsonWriter.exportJsonElementSchema(element, prefix);
								if (jsonElement != null) {
									jsonDefinitions.add(jsonElement);
								}
							}
							String messageElement = element.propertyValue(NiemUmlClass.MESSAGE_ELEMENT_PROPERTY);
							if (messageElement != null)
								Log.debug("exportSchemas: messageElement " + messageElement);
							if (exportJSON && messageElement != null && !messageElement.equals("")) {
								jsonProperties.add(jsonElement);
								//jsonRequired.add("\"" + NamespaceModel.getPrefixedName(element) + "\"");
							}
						}
					} catch (RuntimeException re) {
						Log.trace("exportSchemas: error exporting element " + element.name() + " " + re.toString());
					}
				}
			}
	
			// export XML file
			if (exportXML) {
				// Open XSD file for each extension schema and write header
				Log.debug("exportSchemas: schema " + xmlDir + "/" + prefix + XmlWriter.XSD_FILE_TYPE);
				Namespace ns = NamespaceModel.getNamespace(nsSchemaURI);
				if (ns.getFilepath() == null) {
					Log.trace("exportSchemas: error - no path for schema " + nsSchemaURI);
					continue;
				}
				String filename = Paths.get(xmlDir, ns.getFilepath()).toString();
				Log.debug("exportSchemas: referenced namespaces in " + filename + ": " + schemaNamespaces.toString());
				xmlWriter.exportXmlSchema(filename, nsSchemaURI, xmlTypes, xmlElements, schemaNamespaces);
			}
	
			// export JSON file
			if (exportJSON) {
				jsonWriter.exportJsonSchema(prefix, nsSchemaURI, schemaNamespaces, jsonDefinitions,
						jsonProperties, jsonRequired);
			}
			
			openapiDefinitions.addAll(jsonProperties);
			openapiDefinitions.addAll(jsonDefinitions);
		}
		
		return openapiDefinitions;
	}
	
	/** filter non-ASCII characters
	 * @param string
	 * @return filtered String
	 */
	private String filterASCII(String string) {
		return string.replaceAll("[^\\p{ASCII}]", "");
	}
	/** filter illegal characters in enumerations 
	 * @param string
	 * @return filtered String
	 */
	String filterEnum(String string) {
		return string.replaceAll(CODELIST_DELIMITER + "|" + CODELIST_DEFINITION_DELIMITER, "");
	}
	/** filter illegal characters in enumeration definitions 
	 * @param string
	 * @return filtered String
	 */
	private String filterEnumDefinition(String string) {
		return string.replaceAll(CODELIST_DELIMITER + "|" + CODELIST_DEFINITION_DELIMITER, "");
	}
	/** filter illegal characters in UML elements
	 * @param string
	 * @return filtered String
	 */
	private String filterUMLElement(String string) {
		return string.replaceAll("[^A-Za-z0-9_@#$-`~,.<?;:'\"\\\\]", "");
	}

	/** filter illegal characters in UML types
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
		for (UmlItem item : type.children())
			if (item.kind() == anItemKind.aRelation) {
				UmlRelation r = (UmlRelation) item;
				if (r.relationKind() == aRelationKind.aDirectionalAggregation)
					return r.roleType();
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
	 * @return an element in schema schemaURI with name elementName as a UmlClassInstance
	 */
	UmlClassInstance getElement(String schemaURI, String elementName) {
		return getElementByURI(getURI(schemaURI, elementName));
	}
	/**
	 * @param elementUri
	 * @return an element in model with uri elementUri as a String
	 */
	UmlClassInstance getElementByURI(String elementUri) {
		return elements.get(elementUri);
	}

	/** get an element in type and check the multiplicity
	 * @param type
	 * @param elementInTypeName
	 * @param multiplicity
	 * @return an element in type as a UmlAttribute
	 */
	private UmlAttribute getElementInType(UmlClass type, String elementInTypeName, String multiplicity) {
		for (UmlItem item : type.children())
			if (item.kind() == anItemKind.anAttribute && item.name().equals(elementInTypeName)) {
				String previousMultiplicity = ((UmlAttribute) item).multiplicity();
				if (!previousMultiplicity.equals(multiplicity))
					Log.trace("getElementInType:  error - " + NamespaceModel.getPrefixedName(type) + "/" + elementInTypeName
							+ " has conflicting multiplicities " + previousMultiplicity + " and " + multiplicity);
				return (UmlAttribute) item;
			}
		return null;
	}

	/**
	 * @param typeURI
	 * @return a list of elements in a type with uri typeURI as a List
	 */
	List<UmlClassInstance> getElementsInType( String typeURI) {
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
		return types.get(uri);
	}

	/**
	 * @param elist
	 * @return code values and descriptions from enumerations in schema as a String
	 */
	private String importCodeList(NodeList elist) {
		String codeList = "";
		XPathExpression xe = null;
		if (IMPORT_CODE_DESCRIPTIONS)
			try {
				xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
			} catch (XPathExpressionException e) {
				Log.trace("getCodeList: exception " + e.toString());
			}

		for (int j = 0; j < elist.getLength(); j++) {
			Element enumElement = (Element) elist.item(j);
			String value = filterEnum(filterASCII(enumElement.getAttribute("value")));
			String codeDescription = null;
			if (IMPORT_CODE_DESCRIPTIONS)
				try {
					codeDescription = filterEnumDefinition(filterASCII(xe.evaluate(enumElement)));
				} catch (XPathExpressionException e) {
					Log.trace("getCodeList: error - cannot import code descriptions " + e.toString());
				}
			if (codeDescription != null && !codeDescription.equals("")) {
				codeList += value + CODELIST_DEFINITION_DELIMITER + codeDescription + CODELIST_DELIMITER + " ";
			} else
				codeList += value + CODELIST_DELIMITER + " ";
		}
		return codeList;
	}

	/** import NIEM reference model elements
	 * @param db
	 * @param filename
	 * @return namespace of imported elements as a Namespace
	 */
	Namespace importElements(DocumentBuilder db, String filename) {
		// trace("importElements: importing elements from schema " + filename);
		String filename2 = "\n" + filename + "\n";
		Document doc = null;
		Namespace ns = null;
		XPathExpression xe = null;
		try {
			// parse the schema
			doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = NamespaceModel.importNamespaces(doc);
			if (ns == null)
				return ns;
			
			// compile XPath queries
			xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
		} catch (IOException | XPathExpressionException | SAXException e) {
			Log.trace(filename2 + "importElements: error " + e.toString());
			filename2 = "";
		}
		// get target and default prefixes
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
			elementList = (NodeList) xPath.evaluate("xs:element[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace("importElements: error - cannot import element " + e.toString());
		}

		for (int elementIndex = 0; elementIndex < elementList.getLength(); elementIndex++) {
			Element elementElement = (Element) elementList.item(elementIndex);
			String elementName = elementElement.getAttribute("name");
			if (NamespaceModel.getPrefix(elementName) == null)
				elementName = NamespaceModel.getPrefixedName(targetPrefix, elementName);
			String abstractAttribute = elementElement.getAttribute("abstract");
			String baseTypeSchemaURI = null;
			String baseTypeName = elementElement.getAttribute("type");
			UmlClass baseType;
			if (abstractAttribute.equals("true") || (baseTypeName.equals(""))) {
				baseType = getAbstractType();
			} else {
				if (NamespaceModel.getPrefix(baseTypeName) == null)
					baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
				baseTypeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName));
				if (baseTypeSchemaURI == null)
					baseTypeSchemaURI = NamespaceModel.getSchemaURI(baseTypeName);
				if (baseTypeName != null && baseTypeSchemaURI == null && NamespaceModel.getPrefix(baseTypeName) == null) {
					baseTypeSchemaURI = XSD_URI;
					baseTypeName = NamespaceModel.getPrefixedName(XSD_PREFIX, baseTypeName);
				}
				baseType = getType(baseTypeSchemaURI, baseTypeName);
			}
			try {
				if (baseType == null && !baseTypeName.equals(""))
					Log.trace("importElements: error - base type " + baseTypeName + " not in model with URI "+ baseTypeSchemaURI);
				else
					addElement(ns.getSchemaURI(), elementName, baseType, xe.evaluate(elementElement), null);
			} catch (XPathExpressionException e) {
				Log.trace("importElements: error - cannot import element " + e.toString());
			}
		}

		// import attributes
		// trace("importElement: importing attributes");
		NodeList attributeList = null;
		try {
			attributeList = (NodeList) xPath.evaluate("xs:attribute[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace("importElements: error - cannot import attributes " + e.toString());
		}
		for (int attributeIndex = 0; attributeIndex < attributeList.getLength(); attributeIndex++) {
			Element attribute = (Element) attributeList.item(attributeIndex);
			String attributeName = attribute.getAttribute("name");
			String attributePrefix = NamespaceModel.getPrefix(attributeName);
			if (attributePrefix == null)
				attributePrefix = targetPrefix;
			attributeName = NamespaceModel.getPrefixedAttributeName(attributePrefix, NamespaceModel.getName(attributeName));
			// trace("importElements: adding attribute " + elementName);
			String codeList = "";
			String baseTypeSchemaURI = null;
			String baseTypeName = attribute.getAttribute("type");
			if (baseTypeName.equals(""))
				try {
					Element restriction = (Element) xPath.evaluate(".//xs:simpleType/xs:restriction[1][@base]",
							attribute, XPathConstants.NODE);
					if (restriction != null) {
						baseTypeName = restriction.getAttribute("base");
						NodeList eList = (NodeList) xPath.evaluate("xs:enumeration[@value]", restriction,
								XPathConstants.NODESET);
						codeList = importCodeList(eList);
					} else {
						baseTypeName = xPath.evaluate(".//xs:simpleType/xs:list/@itemType", attribute);
					}
				} catch (XPathExpressionException e) {
					Log.trace(filename + "\nimportElements: error importing base types for attribute "
							+ attributeName + " " + e.toString());
				}
			if (baseTypeName == null || baseTypeName.equals(""))
				baseTypeName = null;
			else {
				if (NamespaceModel.getPrefix(baseTypeName) == null)
					baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
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
				if (baseType2 == null && !baseTypeName.equals(""))
					Log.trace("importElements: error - base type " + baseTypeName + " not in model with URI "+ baseTypeSchemaURI);
				else
					element = addElement(ns.getSchemaURI(), attributeName, baseType2, xe.evaluate(attribute), null);
			} catch (XPathExpressionException e) {
				Log.trace(filename2 + "importElements: error - cannot add attribute " + attributeName + " of type "
						+ baseTypeName + " " + e.toString());
				filename2 = "";
			}
			if (element != null)
				if (!codeList.equals(""))
					element.set_PropertyValue(NiemUmlClass.CODELIST_PROPERTY, codeList);
		}
		return ns;
	}

	/** import NIEM reference model elements in types
	 * @param db
	 * @param filename
	 * @return namespace of imported elements in types as a Namespace
	 */
	Namespace importElementsInTypes(DocumentBuilder db, String filename) {
		// trace("importElementsInTypes: importing elements in types from schema " +
		// filename);
		// String filename2 = "\n" + filename + "\n";
		Document doc = null;
		Namespace ns = null;
		try {
			// parse the schema
			doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = NamespaceModel.importNamespaces(doc);
			if (ns == null)
				return ns;
		} catch (IOException | SAXException e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
		}

		// get target and default prefixes
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
		// trace("importElementsInTypes: import attributes in attribute groups");
		Node root = null;
		NodeList attributeGroupList = null;
		try {
			root = doc.getDocumentElement();
			attributeGroupList = (NodeList) xPath.evaluate("xs:attributeGroup[@name]", root, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
		}

		for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupList.getLength(); attributeGroupIndex++) {
			Element attributeGroupElement = (Element) attributeGroupList.item(attributeGroupIndex);
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
				attributeList = (NodeList) xPath.evaluate(".//xs:attribute[@ref]", attributeGroupElement,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
			}
			for (int attributeIndex = 0; attributeIndex < attributeList.getLength(); attributeIndex++) {
				Element attributeElement = (Element) attributeList.item(attributeIndex);
				String attributeName = attributeElement.getAttribute("ref");
				String attributePrefix = NamespaceModel.getPrefix(attributeName);
				if (attributePrefix == null)
					attributePrefix = defaultPrefix;
				attributeName = NamespaceModel.getPrefixedAttributeName(attributePrefix, NamespaceModel.getName(attributeName));
				String multiplicity = (attributeElement.getAttribute("use")).equals("required") ? "1,1" : "0,1";
				String attributeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName));
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
				addElementInType(attributeGroup, element, multiplicity);
			}
		}

		// import base types for simple types (codes)
		// trace("importElementsInTypes: import base types for simple types (codes)");
		NodeList simpleTypeNodeList = null;
		try {
			simpleTypeNodeList = (NodeList) xPath.evaluate("xs:simpleType[@name]/xs:restriction[1][@base]", root,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace(filename + "\nimportElementsInTypes: error importing base types for simple types " + e.toString());
		}
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
			UmlClass baseType = getType(doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName)), baseTypeName);
			if (baseType == null)
				continue;
			relateBaseType(type, baseType);
		}

		// import base types and elements for complex types
		// trace("importElementsInTypes: import base types and elements for complex
		// types");
		NodeList complexTypeNodeList = null;
		try {
			complexTypeNodeList = (NodeList) xPath.evaluate("xs:complexType[@name]", root, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing types" + e.toString());
		}
		for (int elementIndex = 0; elementIndex < complexTypeNodeList.getLength(); elementIndex++) {
			Element typeElement = (Element) complexTypeNodeList.item(elementIndex);
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
				baseTypeList = (NodeList) xPath.evaluate(
						"xs:simpleContent[1]/xs:extension[1][@base] | xs:complexContent[1]/xs:extension[1][@base]",
						typeElement, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				Log.trace(filename + "\nimportElementsInTypes: error importing base types for complex types"
						+ e.toString());
			}
			for (int baseTypeIndex = 0; baseTypeIndex < baseTypeList.getLength(); baseTypeIndex++) {
				Element baseTypeElement = (Element) baseTypeList.item(baseTypeIndex);
				String baseTypeName = baseTypeElement.getAttribute("base");
				if (NamespaceModel.getPrefix(baseTypeName) == null)
					baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
				UmlClass baseType = getType(doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName)), baseTypeName);
				if (baseType == null)
					continue;
				relateBaseType(type, baseType);
			}

			// import attribute groups in type
			// trace("importElementsInTypes: import attributes groups in types");
			NodeList attributeGroupInTypeNodeList = null;
			try {
				attributeGroupInTypeNodeList = (NodeList) xPath.evaluate(".//xs:attributeGroup[@ref]", typeElement,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				Log.trace(filename + "\nimportElementsInTypes:error importing attributeGroup in complex type" + typeName + " "
						+ e.toString());
			}
			for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupInTypeNodeList
					.getLength(); attributeGroupIndex++) {
				Element attributeGroupElement = (Element) attributeGroupInTypeNodeList.item(attributeGroupIndex);
				String attributeGroupName = attributeGroupElement.getAttribute("ref");
				if (NamespaceModel.getPrefix(attributeGroupName) == null)
					attributeGroupName = NamespaceModel.getPrefixedName(defaultPrefix, attributeGroupName);
				String prefix = NamespaceModel.getPrefix(attributeGroupName);
				String schemaURI = (prefix == null) ? ns.getSchemaURI() : doc.lookupNamespaceURI(prefix);
				UmlClass attributeGroupType = getType(schemaURI, NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeGroupName), NamespaceModel.getName(attributeGroupName)));
				if (attributeGroupType == null)
					continue;
				relateAttributeGroup(type, attributeGroupType);
			}

			// import attributes in type
			// trace("importElementsInTypes: import attributes in types");
			NodeList attributeInTypeNodeList = null;
			try {
				attributeInTypeNodeList = (NodeList) xPath.evaluate(".//xs:attribute[@ref]", typeElement,
						XPathConstants.NODESET);
			} catch (XPathExpressionException re) {
				Log.trace(filename + "\nimportElementsInTypes: error importing attributes in type " + typeName + " " + re.toString());
			}
			for (int attributeIndex = 0; attributeIndex < attributeInTypeNodeList.getLength(); attributeIndex++) {
				Element attributeElement = (Element) attributeInTypeNodeList.item(attributeIndex);
				String attributeName = attributeElement.getAttribute("ref");
				attributeName = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), NamespaceModel.getName(attributeName));
				if (NamespaceModel.getPrefix(attributeName) == null)
					attributeName = NamespaceModel.getPrefixedAttributeName(defaultPrefix, NamespaceModel.getName(attributeName));
				Log.trace("importElementsInTypes: importing attribute " + attributeName + " in type " + typeName);
				String multiplicity = (attributeElement.getAttribute("use").equals("required")) ? "1,1" : "0,1";
				String attributeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName));
				//String attributeName2 = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), attributeName);
				Log.trace("importElementsInTypes: attribute SchemaURI " + attributeSchemaURI + " attributeName " + attributeName);
				UmlClassInstance element = getElement(attributeSchemaURI, attributeName);
				//UmlClassInstance element = getElement(doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName)),
				//		NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), attributeName));
				if (element == null) {
						Log.trace("importElementsInTypes: error - attribute " + attributeName + " not in model");
				} else {
					UmlAttribute attributeInType = addElementInType(type, element, multiplicity);
					if (attributeInType == null) {
						Log.trace("importElementsInTypes: error adding attribute " + attributeName + " to type");
					} else {
					Log.trace("importElementsInTypes: imported attribute " + attributeName + " in type " + typeName);
					}
				}
			}

			// import elements in type
			// trace("importElementsInTypes: import elements in types");
			NodeList elementInTypeNodeList = null;
			try {
				elementInTypeNodeList = (NodeList) xPath.evaluate(".//xs:sequence[1]/xs:element[@ref]", typeElement,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				Log.trace(filename + "\nimportElementsInTypes: error importing elements in type " + e.toString());
			}
			String elementName = null;
			try {
				for (int elementInTypeIndex = 0; elementInTypeIndex < elementInTypeNodeList
						.getLength(); elementInTypeIndex++) {
					Element elementElement = (Element) elementInTypeNodeList.item(elementInTypeIndex);
					elementName = elementElement.getAttribute("ref");
					// trace("importElementsInTypes: adding element " + elementName + " to type " +
					// typeName);
					if (NamespaceModel.getPrefix(elementName) == null)
						elementName = NamespaceModel.getPrefixedName(defaultPrefix, elementName);
					String minOccurs = elementElement.getAttribute("minOccurs");
					if (minOccurs.equals(""))
						minOccurs = "1";
					String maxOccurs = elementElement.getAttribute("maxOccurs");
					if (maxOccurs.equals(""))
						maxOccurs = "1";
					String multiplicity = minOccurs + "," + maxOccurs;
					String elementSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(elementName));
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
					addElementInType(type, element, multiplicity);
					// trace("importElementsInTypes: added element " + elementName + " in type " +
					// typeName);

				}
			} catch (Exception re) {
				Log.trace(filename + "\nimportElementsInTypes: error importing element " + elementName + " in type "
						+ typeName + " " + re.toString());
			}
		}
		return ns;
	}

	/** import NIEM reference model types
	 * @param db
	 * @param filename
	 * @return namespace of imported types as a Namespace
	 */
	Namespace importTypes(DocumentBuilder db, String filename) {
		Log.debug("importTypes: importing types from schema " + filename);
		String filename2 = "\n" + filename + "\n";
		Namespace ns = null;
		Document doc = null;
		XPathExpression xe = null;
		XPathExpression xe1 = null;
		try {
			// parse the schema
			doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = NamespaceModel.importNamespaces(doc);
			if (ns == null) {
				Log.debug("importTypes: error - could not import target namespace in " + filename);
				return ns;
			}

			// compile XPath queries
			xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
			xe1 = xPath.compile("xs:restriction[1]/xs:enumeration");
		} catch (IOException | XPathExpressionException | SAXException re) {
			Log.trace(filename2 + "importTypes: error " + re.toString());
			filename2 = "";
		}
		// get target and default prefixes
		UmlClassView classView = ns.getReferenceClassView();
		if (classView == null) {
			Log.trace("importTypes: error - classview does not exist");
			return ns;
		}
			
		String targetPrefix = NamespaceModel.getPrefix(ns.getReferenceClassView());

		// import types
		NodeList typeList = null;
		try {
			typeList = (NodeList) xPath.evaluate("xs:complexType|xs:simpleType[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace(filename2 + "importTypes: error - cannot parse types " + e.toString());
		}

		for (int typeIndex = 0; typeIndex < typeList.getLength(); typeIndex++) {
			Element typeElement = (Element) typeList.item(typeIndex);
			String nodeType = typeElement.getNodeName();
			String typeName = typeElement.getAttribute("name");
			if (NamespaceModel.getPrefix(typeName) == null)
				typeName = NamespaceModel.getPrefixedName(targetPrefix, typeName);
			UmlClass type = null;
			try {
				type = addType(ns.getSchemaURI(), typeName, xe.evaluate(typeElement), null);
			} catch (XPathExpressionException e) {
				Log.trace(filename2 + "importTypes: error - cannot add type " + typeName + " to schema " + ns.getSchemaURI() + " "
						+ e.toString());
			}
			if (type == null) {
				Log.trace(filename2 + "importTypes: error - cannot add type " + typeName + " to schema " + ns.getSchemaURI());
				continue;
			}
			if ("xs:simpleType".equals(nodeType)) {
				type.set_Stereotype("enum_pattern");
				// import enumerated values for simple types (codes)
				NodeList elist = null;
				try {
					elist = (NodeList) xe1.evaluate(typeElement, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					Log.trace(filename2 + "importTypes: error - cannot import enumerations " + e.toString());
				}
				String codeList = importCodeList(elist);
				if (!codeList.equals(""))
					type.set_PropertyValue(NiemUmlClass.CODELIST_PROPERTY, codeList);
			}
		}

		// import attribute groups
		NodeList attributeGroupList = null;
		try {
			attributeGroupList = (NodeList) xPath.evaluate("xs:attributeGroup[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			Log.trace(filename2 + "importTypes: error - cannot parse attribute groups " + e.toString());
		}
		for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupList.getLength(); attributeGroupIndex++) {
			Element attributeGroupElement = (Element) attributeGroupList.item(attributeGroupIndex);
			String attributeGroupName = attributeGroupElement.getAttribute("name");
			String attributeGroupPrefix = NamespaceModel.getPrefix(attributeGroupName);
			if (attributeGroupPrefix == null)
				attributeGroupPrefix = targetPrefix;
			attributeGroupName = NamespaceModel.getPrefixedAttributeName(attributeGroupPrefix, NamespaceModel.getName(attributeGroupName));
			UmlClass attributeGroup = null;
			try {
				attributeGroup = addType(ns.getSchemaURI(), attributeGroupName, xe.evaluate(attributeGroupElement), null);
			} catch (XPathExpressionException e) {
				Log.trace(filename2 + "importTypes: error - cannot add attribute group " + attributeGroupName + " "
						+ e.toString());
				filename2 = "";
			}
			if (attributeGroup == null) {
				Log.trace(filename2 + "importTypes: error - cannot add attribute group " + attributeGroupName + " to schema " + ns.getSchemaURI());
			}
		}
		return ns;
	}

	/** identify if a prefix is an infrastructure prefix
	 * @param prefix
	 */
	static Boolean isInfrastructurePrefix(String prefix) {
		return (prefix.equals(LOCAL_PREFIX) || prefix.equals(XSD_PREFIX) || prefix.equals(STRUCTURES_PREFIX));
	}

	/** create a relationship between a type and an attribute group
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
	/** create a relationship between a type and a base type
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
	/** relate element in type to element
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
	/** add a description to type or element
	 * @param item
	 * @param description
	 */
	private void setDescription(UmlItem item, String description) {
		String currentDescription = item.description();
		if ((currentDescription.equals("")) && (description != null) && (!description.equals("")))
			item.set_Description(description);
	}
	/**
	 * @param modelPackage
	 */
	void setModelPackage(UmlPackage modelPackage) {
		this.modelPackage = modelPackage;
	}
	/** add NIEM mapping notes to a type or element
	 * @param item
	 * @param notes
	 */
	private void setNotes(UmlItem item, String notes) {
		if ((item != null) && (notes != null) && (!notes.equals(""))) {
			String currentNotes = item.propertyValue(NiemUmlClass.NOTES_PROPERTY);
			if (currentNotes == null)
				currentNotes = notes;
			else if (!currentNotes.contains(notes))
				currentNotes = currentNotes + "; " + notes;
			if (!currentNotes.equals(""))
				item.set_PropertyValue(NiemUmlClass.NOTES_PROPERTY, currentNotes);
		}
	}
}
