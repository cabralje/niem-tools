package com.mtgmc.niemtools;

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
import java.io.FileWriter;
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
	
	static XPath xPath = XPathFactory.newInstance().newXPath();
	static Map<String, List<UmlClassInstance>> Substitutions = new HashMap<String, List<UmlClassInstance>>();
	static final String AUGMENTATION_TYPE_NAME = "AugmentationType";
	static final String SIMPLE_OBJECT_ATTRIBUTE_GROUP = "@SimpleObjectAttributeGroup";
	static final String OBJECT_TYPE_NAME = "ObjectType";
	static final String[] XML_TYPE_NAMES = { "anyURI", "base64Binary", "boolean", "byte", "date", "dateTime",
	"decimal", "double", "duration", "ENTITIES", "ENTITY", "float", "gDay", "gMonth", "gMonthDay", "gYear",
	"gYearMonth", "hexBinary", "ID", "IDREF", "IDREFS", "int", "integer", "language", "long", "Name", "NCName",
	"negativeInteger", "NMTOKEN", "NMTOKENS", "nonNegativeInteger", "nonPositiveInteger", "normalizedString",
	"NOTATION", "positiveInteger", "QName", "short", "string", "time", "token", "unsignedByte", "unsignedInt",
	"unsignedLong", "unsignedShort" };
	static Boolean _IMPORT_CODE_DESCRIPTIONS = true;
	// private static final Boolean _IMPORT_CODE_DESCRIPTIONS = false;
	
	/** returns an element in type added to reference model or extension */
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
				elementInType = UmlAttribute.create(type, elementInTypeName);
			} catch (RuntimeException re) {
				// trace("addElementInType: error - element " + elementInTypeName + " already
				// exists in type " + type.name());
				return null;
			}
	
		// copy element properties
		elementInType.set_PropertyValue(NiemUmlClass.URI_PROPERTY, NiemModel.getURI(element));
		if (element.description() != null)
			elementInType.set_Description(element.description());
		if (multiplicity != null)
			elementInType.set_Multiplicity(multiplicity);
	
		// relate element in type to element
		relateElementInType(elementInType, element);
	
		// insert element in type list
		
		List<UmlClassInstance> elementInTypeList = getModel(type).getElementsInType( NiemModel.getURI(type));
		if (!elementInTypeList.contains(element)) {
			elementInTypeList.add(element);
			Log.debug("addElementInTypes: inserted " + elementInTypeName + " to " + typeName);
		}
		return elementInType;
	}
	/** returns an element copied from reference model to subset */
	UmlClassInstance copyElement(String elementName) {
	
		// return subset element if it exists
		String schemaURI = NamespaceModel.getSchemaURI(elementName);
		UmlClassInstance element = getElement(schemaURI, elementName);
		if (element != null)
			return element;
	
		// if element doesn't exist in reference model, return error
		UmlClassInstance sourceElement = NiemUmlClass.ReferenceModel.getElement(schemaURI, elementName);
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
		UmlClassView nsClassView = NamespaceModel.getNamespace(this, NamespaceModel.getPrefix(elementName), schemaURI);
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
		String uri = NiemModel.getURI(schemaURI, elementName);
		element.set_PropertyValue(NiemUmlClass.URI_PROPERTY, uri);
		elements.put(uri, element);
		Log.debug("copyElement: element " + elementName + " copied to subset");
		return element;
	}
	/** returns an element in type copied from reference model to subset */
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
			attribute.set_PropertyValue(NiemUmlClass.URI_PROPERTY, NiemModel.getURI(element));
			relateElementInType(attribute, element);
			attribute.set_Multiplicity(multiplicity);
		}
		Log.debug("addElementInTypes: inserted " + elementInTypeName + " to " + typeName);
		return attribute;
	}
	/** returns a type copied from the reference model to subset */
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
		UmlClass sourceType = NiemUmlClass.ReferenceModel.getType(schemaURI, typeName);
		if (sourceType == null) {
			Log.trace("copyType: error - type " + typeName + " not in reference model");
			return null;
		}
	
		// if subset namespace doesn't exist, create it
		UmlClassView nsClassView = NamespaceModel.getNamespace(this, NamespaceModel.getPrefix(typeName), schemaURI);
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
		type.set_PropertyValue(NiemUmlClass.URI_PROPERTY, NiemModel.getURI(sourceType));
		String codeList = sourceType.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		if (codeList != null)
			type.set_PropertyValue(NiemUmlClass.CODELIST_PROPERTY, codeList);
		types.put(NiemModel.getURI(type), type);
	
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
				UmlClassInstance sourceElement = NiemUmlClass.ReferenceModel.getElementByURI(NiemModel.getURI(item));
				if (sourceElement == null) {
					Log.trace("copyType: error - no element for uri " + NiemModel.getURI(item) + " in reference model");
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
	/** filter non-ASCII characters */
	String filterASCII(String string) {
		return string.replaceAll("[^\\p{ASCII}]", "");
	}
	/** filter illegal characters in enumerations */
	String filterEnum(String string) {
		return string.replaceAll(NiemUmlClass.CODELIST_DELIMITER + NiemUmlClass.CODELIST_DEFINITION_DELIMITER, "");
	}
	/** filter illegal characters in UML types */
	String filterUMLType(String string) {
		return string.replaceAll("[^A-Za-z0-9_@#$`~,.<?;:'\"\\\\]", "");
	}
	/** return attribute group related to a type */
	UmlClass getAttributeGroup(UmlClass type) {
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
	
	/** return base type related to a type or element */
	static UmlClass getBaseType(UmlItem item) {
		UmlClass baseType = null;
		switch (item.kind().value()) {
		case anItemKind._aClassInstance:
			UmlClassInstance classInstance = (UmlClassInstance) item;
			baseType = classInstance.type();
			break;
		case anItemKind._aClass:
			if (item == NiemUmlClass.SubsetModel.abstractType || item == NiemUmlClass.ReferenceModel.abstractType)
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
				if (prefix.equals(NiemUmlClass.STRUCTURES_PREFIX) || prefix.equals(NiemUmlClass.XSD_PREFIX))
					return null;
				if (NamespaceModel.getName(item).endsWith(AUGMENTATION_TYPE_NAME))
					baseType = NiemUmlClass.SubsetModel.augmentationType;
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
	
	/** return the default schema URI for the current file */
	String getDefaultSchemaURI(String filename, Document doc) {
		String defaultSchemaURI = doc.lookupNamespaceURI(null);
		if (defaultSchemaURI == null) {
			// trace("getDefaultSchemaURI: no default namespace found in " + filename);
			defaultSchemaURI = NiemUmlClass.LOCAL_PREFIX;
		}
		return defaultSchemaURI;
	}
	
	/** returns an element in type and checks the multiplicity */
	UmlAttribute getElementInType(UmlClass type, String elementInTypeName, String multiplicity) {
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
	
	/** return model for Umlitem item */
	static NiemModel getModel(UmlItem item) {
		UmlPackage modelPackage = null;
		anItemKind kind = item.kind();
		if (kind == anItemKind.aClass || kind == anItemKind.aClassInstance)
			modelPackage = (UmlPackage) (item.parent().parent());
		else if (kind == anItemKind.aClassView)
			modelPackage = (UmlPackage) (item.parent());
		if (modelPackage == NiemUmlClass.ReferenceModel.modelPackage)
			return NiemUmlClass.ReferenceModel;
		else if (modelPackage == NiemUmlClass.SubsetModel.modelPackage)
			return NiemUmlClass.SubsetModel;
		else if (modelPackage == NiemUmlClass.ExtensionModel.modelPackage)
			return NiemUmlClass.ExtensionModel;
		Log.trace("getPrefix - error - no prefix for " + item.name());
	
		return null;
	}

	/** return code values and descriptions from enumerations in schema */
	private String importCodeList(NodeList elist) {
		String codeList = "";
		XPathExpression xe = null;
		if (_IMPORT_CODE_DESCRIPTIONS)
			try {
				xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
			} catch (XPathExpressionException e) {
				Log.trace("getCodeList: exception " + e.toString());
			}
	
		for (int j = 0; j < elist.getLength(); j++) {
			Element enumElement = (Element) elist.item(j);
			String value = filterEnum(filterASCII(enumElement.getAttribute("value")));
			String codeDescription = null;
			if (_IMPORT_CODE_DESCRIPTIONS)
				try {
					codeDescription = filterEnum(filterASCII(xe.evaluate(enumElement)));
				} catch (Exception e) {
					Log.trace("getCodeList: error - cannot import code descriptions " + e.toString());
				}
			if (codeDescription != null && !codeDescription.equals("")) {
				codeList += value + NiemUmlClass.CODELIST_DEFINITION_DELIMITER + codeDescription + NiemUmlClass.CODELIST_DELIMITER + " ";
			} else
				codeList += value + NiemUmlClass.CODELIST_DELIMITER + " ";
		}
		return codeList;
	}
	/** import NIEM reference model elements into HashMaps and return namespace */
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
	
			// compile XPath queries
			xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
		} catch (Exception e) {
			Log.trace(filename2 + "importElements: error " + e.toString());
			filename2 = "";
		}
		// get target and default prefixes
		String targetPrefix = NamespaceModel.getPrefix(ns.referenceClassView);
		String defaultSchemaURI = getDefaultSchemaURI(filename, doc);
		String defaultPrefix = NamespaceModel.getPrefix(NamespaceModel.Namespaces.get(defaultSchemaURI).referenceClassView);
	
		// import elements
		// trace("importElement: importing elements");
		NodeList elementList = null;
		try {
			elementList = (NodeList) xPath.evaluate("xs:element[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (Exception e) {
			Log.trace("importElements: error - cannot import element " + e.toString());
		}
	
		for (int elementIndex = 0; elementIndex < elementList.getLength(); elementIndex++) {
			Element elementElement = (Element) elementList.item(elementIndex);
			String elementName = elementElement.getAttribute("name");
			if (NamespaceModel.getPrefix(elementName) == null)
				elementName = NamespaceModel.getPrefixedName(targetPrefix, elementName);
			String abstractAttribute = elementElement.getAttribute("abstract");
			String baseTypeSchemaURI = null;
			String baseTypeName = null;
			if (!abstractAttribute.equals("true")) {
				baseTypeName = elementElement.getAttribute("type");
				if (baseTypeName.equals(""))
					baseTypeName = null;
				else if (NamespaceModel.getPrefix(baseTypeName) == null)
					baseTypeName = NamespaceModel.getPrefixedName(defaultPrefix, baseTypeName);
				baseTypeSchemaURI = doc.lookupNamespaceURI(NamespaceModel.getPrefix(baseTypeName));
				if (baseTypeSchemaURI == null)
					baseTypeSchemaURI = NamespaceModel.getSchemaURI(baseTypeName);
				if (baseTypeName != null && baseTypeSchemaURI == null && NamespaceModel.getPrefix(baseTypeName) == null) {
					baseTypeSchemaURI = NiemUmlClass.XSD_URI;
					baseTypeName = NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, baseTypeName);
				}
			}
			UmlClass baseType = getType(baseTypeSchemaURI, baseTypeName);
			if (baseType == null && !baseTypeName.equals(""))
				Log.trace("importElements: error - base type " + baseTypeName + " not in model with URI "+ baseTypeSchemaURI);
			try {
				addElement(ns.schemaURI, elementName, baseType, xe.evaluate(elementElement), null);
			} catch (Exception e) {
				Log.trace("importElements: error - cannot import element " + e.toString());
			}
		}
	
		// import attributes
		// trace("importElement: importing attributes");
		NodeList attributeList = null;
		try {
			attributeList = (NodeList) xPath.evaluate("xs:attribute[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (Exception e) {
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
					}
				} catch (Exception e) {
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
					baseTypeSchemaURI = NiemUmlClass.XSD_URI;
					baseTypeName = NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, baseTypeName);
				}
			}
			UmlClassInstance element = null;
			try {
				UmlClass baseType2 = getType(baseTypeSchemaURI, baseTypeName);
				if (baseType2 == null && !baseTypeName.equals(""))
					Log.trace("importElements: error - base type " + baseTypeName + " not in model with URI "+ baseTypeSchemaURI);
				element = addElement(ns.schemaURI, attributeName, baseType2, xe.evaluate(attribute), null);
			} catch (Exception e) {
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
	/**
	 * import NIEM reference model elements in Types into HashMaps and return
	 * namespace
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
		} catch (Exception e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
		}
	
		// get target and default prefixes
		String targetPrefix = NamespaceModel.getPrefix(ns.referenceClassView);
		String defaultSchemaURI = getDefaultSchemaURI(filename, doc);
		String defaultPrefix = NamespaceModel.getPrefix(NamespaceModel.Namespaces.get(defaultSchemaURI).referenceClassView);
	
		// import attributes in attribute groups
		// trace("importElementsInTypes: import attributes in attribute groups");
		Node root = null;
		NodeList attributeGroupList = null;
		try {
			root = doc.getDocumentElement();
			attributeGroupList = (NodeList) xPath.evaluate("xs:attributeGroup[@name]", root, XPathConstants.NODESET);
		} catch (Exception e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing the schema " + e.toString());
		}
	
		for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupList.getLength(); attributeGroupIndex++) {
			Element attributeGroupElement = (Element) attributeGroupList.item(attributeGroupIndex);
			String attributeGroupName = attributeGroupElement.getAttribute("name");
			if (NamespaceModel.getPrefix(attributeGroupName) == null)
				attributeGroupName = NamespaceModel.getPrefixedAttributeName(targetPrefix, attributeGroupName);
			UmlClass attributeGroup = getType(ns.schemaURI, attributeGroupName);
			if (attributeGroup == null) {
				Log.trace("importElementsInType: error - attribute group " + attributeGroupName
						+ " not in reference model");
				continue;
			}
			NodeList attributeList = null;
			try {
				attributeList = (NodeList) xPath.evaluate(".//xs:attribute[@ref]", attributeGroupElement,
						XPathConstants.NODESET);
			} catch (Exception e) {
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
					Log.trace("addElementInType: error - prefix for attribute " + attributeName + " not in model");
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
		} catch (Exception e) {
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
			UmlClass type = getType(ns.schemaURI, typeName);
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
		} catch (Exception e) {
			Log.trace(filename + "\nimportElementsInTypes: error parsing types" + e.toString());
		}
		for (int elementIndex = 0; elementIndex < complexTypeNodeList.getLength(); elementIndex++) {
			Element typeElement = (Element) complexTypeNodeList.item(elementIndex);
			if (typeElement == null)
				continue;
			String typeName = typeElement.getAttribute("name");
			if (NamespaceModel.getPrefix(typeName) == null)
				typeName = NamespaceModel.getPrefixedName(targetPrefix, typeName);
			UmlClass type = getType(ns.schemaURI, typeName);
			if (type == null)
				continue;
	
			// import base types for complex types
			// trace("importElementsInTypes: import base types for complex types");
			NodeList baseTypeList = null;
			try {
				baseTypeList = (NodeList) xPath.evaluate(
						"xs:simpleContent[1]/xs:extension[1][@base] | xs:complexContent[1]/xs:extension[1][@base]",
						typeElement, XPathConstants.NODESET);
			} catch (Exception e) {
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
			} catch (Exception e) {
				Log.trace(filename + "\nimportElementsInTypes:error importing attributeGroup in complex types"
						+ e.toString());
			}
			for (int attributeGroupIndex = 0; attributeGroupIndex < attributeGroupInTypeNodeList
					.getLength(); attributeGroupIndex++) {
				Element attributeGroupElement = (Element) attributeGroupInTypeNodeList.item(attributeGroupIndex);
				String attributeGroupName = attributeGroupElement.getAttribute("ref");
				if (NamespaceModel.getPrefix(attributeGroupName) == null)
					attributeGroupName = NamespaceModel.getPrefixedName(defaultPrefix, attributeGroupName);
				String prefix = NamespaceModel.getPrefix(attributeGroupName);
				String schemaURI = (prefix == null) ? ns.schemaURI : doc.lookupNamespaceURI(prefix);
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
			} catch (Exception re) {
				Log.trace(filename + "\nimportElementsInTypes: error importing attributes in type " + re.toString());
			}
			for (int attributeIndex = 0; attributeIndex < attributeInTypeNodeList.getLength(); attributeIndex++) {
				Element attributeElement = (Element) attributeInTypeNodeList.item(attributeIndex);
				String attributeName = attributeElement.getAttribute("ref");
				if (NamespaceModel.getPrefix(attributeName) == null)
					attributeName = NamespaceModel.getPrefixedName(defaultPrefix, attributeName);
				String multiplicity = (attributeElement.getAttribute("use").equals("required")) ? "1,1" : "0,1";
				UmlClassInstance element = getElement(doc.lookupNamespaceURI(NamespaceModel.getPrefix(attributeName)),
						NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(attributeName), attributeName));
				addElementInType(type, element, multiplicity);
				// trace("importElementsInTypes: imported attribute :" + attributeName + " in
				// group " + typeName);
			}
	
			// import elements in type
			// trace("importElementsInTypes: import elements in types");
			NodeList elementInTypeNodeList = null;
			try {
				elementInTypeNodeList = (NodeList) xPath.evaluate(".//xs:sequence[1]/xs:element[@ref]", typeElement,
						XPathConstants.NODESET);
			} catch (Exception e) {
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
						Log.trace("importElementsInType: error - element " + NiemModel.getURI(elementSchemaURI, elementName)
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
	
	/** import NIEM reference model types into HashMaps and return namespace */
	Namespace importTypes(DocumentBuilder db, String filename) {
		// trace("importTypes: importing types from schema " + filename);
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
	
			// compile XPath queries
			xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
			xe1 = xPath.compile("xs:restriction[1]/xs:enumeration");
		} catch (Exception re) {
			Log.trace(filename2 + "importTypes: error " + re.toString());
			filename2 = "";
		}
		// get target and default prefixes
		String targetPrefix = NamespaceModel.getPrefix(ns.referenceClassView);
	
		// import types
		NodeList typeList = null;
		try {
			typeList = (NodeList) xPath.evaluate("xs:complexType|xs:simpleType[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
		} catch (Exception e) {
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
				type = addType(ns.schemaURI, typeName, xe.evaluate(typeElement), null);
			} catch (Exception e) {
				Log.trace(filename2 + "importTypes: cannot add type " + typeName + " to schema " + ns.schemaURI + " "
						+ e.toString());
			}
			if (type == null)
				continue;
			if (nodeType == "xs:simpleType") {
				type.set_Stereotype("enum_pattern");
				// import enumerated values for simple types (codes)
				NodeList elist = null;
				try {
					elist = (NodeList) xe1.evaluate(typeElement, XPathConstants.NODESET);
				} catch (Exception e) {
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
			try {
				addType(ns.schemaURI, attributeGroupName, xe.evaluate(attributeGroupElement), null);
			} catch (Exception e) {
				Log.trace(filename2 + "importTypes: error - cannot add attribute group " + attributeGroupName + " "
						+ e.toString());
				filename2 = "";
			}
		}
		return ns;
	}

	/** create a relationship between a type and an attribute group */
	void relateAttributeGroup(UmlClass type, UmlClass attributeGroupType) {
		if (type != null && attributeGroupType != null)
			try {
				UmlRelation.create(aRelationKind.aDirectionalAggregation, type, attributeGroupType);
			} catch (Exception re) {
				// trace("createSubsetAndExtension: " + typeName + " already related to
				// attribute group " + baseTypeName);
			}
	}

	/** create a relationship between a type and a base type */
	void relateBaseType(UmlClass type, UmlClass baseType) {
		if (type != null && baseType != null)
			try {
				UmlRelation.create(aRelationKind.aGeneralisation, type, baseType);
			} catch (Exception re) {
				// trace("copyType: error - type " + type.name() + " already related to base
				// type " + sourceBaseTypeName);
			}
	}

	/** relate element in type to element */
	void relateElementInType(UmlAttribute elementInType, UmlClassInstance element) {
		UmlTypeSpec elementType = new UmlTypeSpec();
		elementType.type = getBaseType(element);
		if (elementType.type != null)
			try {
				elementInType.set_Type(elementType);
			} catch (Exception re) {
				Log.trace("relateElementInType: error relating element in type to " + NamespaceModel.getPrefixedName(element) + " "
						+ re.toString());
			}
	}

	/** add a description to type or element */
	void setDescription(UmlItem item, String description) {
		String currentDescription = item.description();
		if ((currentDescription.equals("")) && (description != null) && (!description.equals("")))
			item.set_Description(description);
	}

	/** add NIEM mapping notes to a type or element */
	void setNotes(UmlItem item, String notes) {
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

	UmlClass abstractType = null;

	UmlClass augmentationType = null;

	Map<String, UmlClassInstance> elements = new HashMap<String, UmlClassInstance>();

	Map<String, List<UmlClassInstance>> elementsInType = new HashMap<String, List<UmlClassInstance>>();

	UmlPackage modelPackage = null;

	UmlClass objectType = null;

	UmlClass simpleObjectAttributeGroup = null;

	Map<String, UmlClass> types = new HashMap<String, UmlClass>();
	static final String HASH_DELIMITER = ",";
	static final String XSI_PREFIX = "xsi";
	static final String XSI_URI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	// XML
	static final String XML_URI = XMLConstants.XML_NS_URI;
	static final String XML_PREFIX = XMLConstants.XML_NS_PREFIX;

	/** returns an element added to reference or extension model */
	UmlClassInstance addElement(String elementSchemaURI, String elementName, UmlClass baseType, String description, String notes) {
	
		if (elementName.equals("") || elementName.equals("??"))
			return null;
	
		// get element if it exists
		UmlClassInstance element = getElement(elementSchemaURI, elementName);
	
		// if not, create element
		if (element == null) {
	
			// default to abstract type if base type not defined
			if (baseType == null)
				baseType = abstractType;
			// create element
			UmlClassView nsClassView = NamespaceModel.getNamespace(this, NamespaceModel.getPrefix(elementName), elementSchemaURI);
			if (nsClassView == null) {
				Log.trace("addElement: error - no prefix or schema for element " + elementName);
				return null;
			}
			try {
				element = UmlClassInstance.create(nsClassView, filterUMLElement(NamespaceModel.getName(elementName)), baseType);
			} catch (RuntimeException e) {
				Log.trace("addElement: error adding element " + elementName + " of type " + baseType.name());
				return null;
			}
			element.set_Description(description);
			String uri = NiemModel.getURI(elementSchemaURI, elementName);
			element.set_PropertyValue(NiemUmlClass.URI_PROPERTY, uri);
			elements.put(uri, element);
			Log.debug("addElement: added " + elementName);
		}
	
		// add notes
		setNotes(element, notes);
		return element;
	}

	/** returns a type added to the reference or extension models */
	UmlClass addType(String typeSchemaURI, String typeName, String description,
			String notes) {
		// trace("addType: adding " + typeName + " to schema " + typeSchemaURI);
		UmlClass type = getType(typeSchemaURI, typeName);
		if (type == null) {
			// create type
			UmlClassView nsClassView = NamespaceModel.getNamespace(this, NamespaceModel.getPrefix(typeName), typeSchemaURI);
			if (nsClassView == null) {
				Log.trace("addType: error - no prefix or schema for type " + typeName);
				return null;
			}
			try {
				type = UmlClass.create(nsClassView, filterUMLType(NamespaceModel.getName(typeName)));
			} catch (RuntimeException re) {
				Log.trace("addType: error adding type " + typeName);
				return null;
			}
			String uri = NiemModel.getURI(typeSchemaURI, typeName);
			type.set_PropertyValue(NiemUmlClass.URI_PROPERTY, uri);
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
			schemaURI = NiemModel.getURI(classView);
			String prefix = classView.propertyValue(NiemUmlClass.PREFIX_PROPERTY);
			if (!NamespaceModel.Prefixes.containsKey(prefix))
				NamespaceModel.Prefixes.put(prefix, schemaURI);
			Namespace ns = NamespaceModel.Namespaces.get(schemaURI);
			if ((ns == null) && (schemaURI != null)) {
				// create namespace
				ns = new Namespace(schemaURI);
				NamespaceModel.Namespaces.put(schemaURI, ns);
			}
			if (this == NiemUmlClass.ReferenceModel)
				ns.referenceClassView = (UmlClassView) classView;
			else
				ns.nsClassView = (UmlClassView) classView;
	
			if (this == NiemUmlClass.ReferenceModel || this == NiemUmlClass.ExtensionModel)
				ns.filepath = classView.propertyValue(NiemUmlClass.FILE_PATH_PROPERTY);
	
			// cache types and elements
			for (UmlItem item : classView.children()) {
				String uri = NiemModel.getURI(item);
				if (uri != null) {
					switch (item.kind().value()) {
					case anItemKind._aClass:
						if (!types.containsKey(uri))
							types.put(uri, (UmlClass) item);
						break;
					case anItemKind._aClassInstance:
						UmlClassInstance element = (UmlClassInstance) item;
						if (!elements.containsKey(uri))
							elements.put(uri, element);
						String headElement = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
						if (headElement != null) {
							List<UmlClassInstance> enlist = (Substitutions.get(headElement));
							if (enlist == null) {
								enlist = new ArrayList<UmlClassInstance>();
								Substitutions.put(headElement, enlist);
							}
							if (!enlist.contains(element))
								enlist.add(element);
						}
						break;
					default:
						break;
					}
				}
			}
		}
	
		// Cache elements in types
		Log.debug("cacheModel: caching elements in types");
		for (UmlClass type : types.values()) {
			String typeURI = NiemModel.getURI(type);
			List<UmlClassInstance> list = (elementsInType.get(typeURI));
			if (list == null) {
				list = new ArrayList<UmlClassInstance>();
				elementsInType.put(typeURI, list);
			}
			for (UmlItem attribute : type.children())
				if (attribute.kind() == anItemKind.anAttribute) {
					// trace("cacheModel: caching " + getURI(attribute));
					UmlClassInstance element = elements.get(NiemModel.getURI(attribute));
					if (!list.contains(element))
						list.add(element);
				}
		}
	
		Log.debug("cacheModel: store caches and add simple and abstract types");
		if (this == NiemUmlClass.ReferenceModel) {
			// add local namespace and abstract type
			NamespaceModel.getNamespace(null, NiemUmlClass.LOCAL_PREFIX, NiemUmlClass.LOCAL_URI);
			abstractType = addType(NiemUmlClass.LOCAL_URI,
					NamespaceModel.getPrefixedName(NiemUmlClass.LOCAL_PREFIX, NiemUmlClass.ABSTRACT_TYPE_NAME), null, null);
	
			// add XML namespace, simple types and any element
			NamespaceModel.getNamespace(null, NiemModel.XML_PREFIX, NiemModel.XML_URI);
			for (String typeName : XML_TYPE_NAMES)
				addType(NiemUmlClass.XSD_URI, NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, typeName), null, null);
			addElement(NiemUmlClass.XSD_URI, NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, NiemUmlClass.ANY_ELEMENT_NAME), null, null, null);
		} else if (this == NiemUmlClass.SubsetModel) {
			abstractType = copyType(NamespaceModel.getPrefixedName(NiemUmlClass.LOCAL_PREFIX, NiemUmlClass.ABSTRACT_TYPE_NAME));
			augmentationType = copyType(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, AUGMENTATION_TYPE_NAME));
			objectType = copyType(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, OBJECT_TYPE_NAME));
			simpleObjectAttributeGroup = copyType(
					NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, SIMPLE_OBJECT_ATTRIBUTE_GROUP));
			NamespaceModel.getNamespace(null, NiemUmlClass.XSD_PREFIX, NiemUmlClass.XSD_URI);
			copyElement(NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, NiemUmlClass.ANY_ELEMENT_NAME));
			copyType("xs:NCName"); // JSON-LD @id is type xs:NCName
		} else if (this == NiemUmlClass.ExtensionModel) {
			abstractType = NiemUmlClass.SubsetModel.abstractType;
			augmentationType = NiemUmlClass.SubsetModel.augmentationType;
			objectType = NiemUmlClass.SubsetModel.objectType;
			simpleObjectAttributeGroup = NiemUmlClass.SubsetModel.simpleObjectAttributeGroup;
		}
		Log.debug("cacheModels: model " + modelPackage.name() + " cached");
	}

	/** return an element in schema schemaURI with name elementName */
	UmlClassInstance getElement(String schemaURI, String elementName) {
		return getElementByURI(NiemModel.getURI(schemaURI, elementName));
	}

	/** return an element in model with uri elementUri */
	UmlClassInstance getElementByURI(String elementUri) {
		UmlClassInstance element = null;
		element = elements.get(elementUri);
		return element;
	}

	/** return an element in type in model with uri typeURI */
	List<UmlClassInstance> getElementsInType( String typeURI) {
		List<UmlClassInstance> elementInTypeList = elementsInType.get(typeURI);
		if (elementInTypeList == null) {
			elementInTypeList = new ArrayList<UmlClassInstance>();
			elementsInType.put(typeURI, elementInTypeList);
		}
		return elementInTypeList;
	}

	/** return type in model with schema schemaURI and name tagname */
	UmlClass getType(String schemaURI, String typeName) {
		// return cached type
		String uri = NiemModel.getURI(schemaURI, typeName);
		return types.get(uri);
	}
	/** filter illegal characters in UML elements */
	String filterUMLElement(String string) {
		return string.replaceAll("[^A-Za-z0-9_@#$-`~,.<?;:'\"\\\\]", "");
	}
	/** exports NIEM extension and exchange schema 
	 * @param xmlWriter TODO
	 * @param xmlDir TODO
	 * @param jsonDir TODO*/
	void exportSchemas(String xmlDir, String jsonDir) {
	
		XmlWriter xmlWriter = new XmlWriter(xmlDir);
		JsonWriter jsonWriter = new JsonWriter(jsonDir);
		boolean exportXML = (xmlDir != null);
		boolean exportJSON = (jsonDir != null);
	
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
			String nsSchemaURI = NiemModel.getURI(classView);
			Log.debug("exportSchemas: exporting schema " + prefix);
	
			// build list of referenced namespaces
			TreeSet<String> schemaNamespaces = new TreeSet<String>();
			schemaNamespaces.add(NiemUmlClass.XSD_PREFIX);
	
			TreeSet<String> xmlTypes = new TreeSet<String>();
			TreeSet<String> jsonDefinitions = new TreeSet<String>();
			TreeSet<String> xmlElements = new TreeSet<String>();
			TreeSet<String> jsonProperties = new TreeSet<String>();
			TreeSet<String> jsonRequired = new TreeSet<String>();
	
			Log.debug("exportSchemas: exporting types and elements");
			String xmlType = null;
			String jsonType = null;
			for (UmlItem item : classView.children()) {
				// add types and attribute groups
				if (item.kind() == anItemKind.aClass) {
					UmlClass type = (UmlClass) item;
	
					// add referenced namespaces
					schemaNamespaces.add(NamespaceModel.getPrefix(type));
					UmlClass baseType = NiemModel.getBaseType(type);
					if (baseType != null)
						schemaNamespaces.add(NamespaceModel.getPrefix(baseType));
					for (UmlItem item2 : type.children())
						if (item2.kind() == anItemKind.anAttribute) {
							NiemModel model2 = (NiemUmlClass.SubsetModel.elements.containsKey(NiemModel.getURI(item2))) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;
							UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item2));
							if (element != null)
								schemaNamespaces.add(NamespaceModel.getPrefix(element));
						}
	
					// get type schema
					xmlType = xmlWriter.exportXmlTypeSchema(type);
					if (xmlType != null)
						xmlTypes.add(xmlType);
					jsonType = (prefix.equals(NiemUmlClass.XSD_PREFIX)) ? jsonWriter.exportJsonPrimitiveSchema(type)
							: jsonWriter.exportJsonTypeSchema(this, type, prefix);
					if (jsonType != null)
						jsonDefinitions.add(jsonType);
				}
				// add elements and attributes
				String xmlElement = null;
				String jsonElement = null;
				if (item.kind() == anItemKind.aClassInstance) {
					UmlClassInstance element = (UmlClassInstance) item;
					UmlClass baseType = NiemModel.getBaseType(element);
	
					// add referenced namespaces
					schemaNamespaces.add(NamespaceModel.getPrefix(element));
					if (baseType != null)
						schemaNamespaces.add(NamespaceModel.getPrefix(baseType));
					String headElement = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
					if (headElement != null)
						schemaNamespaces.add(NamespaceModel.getPrefix(headElement));
	
					// get element schema
					xmlElement = xmlWriter.exportXmlElementSchema(element);
					if (xmlElement != null)
						xmlElements.add(xmlElement);
					if (baseType != NiemUmlClass.SubsetModel.abstractType) {
						schemaNamespaces.add(NamespaceModel.getPrefix(element));
						jsonElement = jsonWriter.exportJsonElementSchema(element, prefix);
						if (jsonElement != null)
							jsonDefinitions.add(jsonElement);
						String messageElement = element.propertyValue(NiemUmlClass.MESSAGE_ELEMENT_PROPERTY);
						if (messageElement != null && !messageElement.equals("")) {
							jsonProperties.add(jsonElement);
							jsonRequired.add("\"" + NamespaceModel.getPrefixedName(element) + "\"");
						}
					}
				}
			}
	
			// export XML file
			if (exportXML) {
				// Open XSD file for each extension schema and write header
				Log.debug("exportSchemas: schema " + xmlDir + "/" + prefix + NiemUmlClass.XSD_FILE_TYPE);
				Namespace ns = NamespaceModel.Namespaces.get(nsSchemaURI);
				if (ns.filepath == null) {
					Log.trace("exportSchemas: error - no path for schema " + nsSchemaURI);
					continue;
				}
				String filename = Paths.get(xmlDir, ns.filepath).toString();
				Log.debug("exportSchemas: referenced namespaces in " + filename + ": " + schemaNamespaces.toString());
				xmlWriter.exportXmlSchema(filename, nsSchemaURI, xmlTypes, xmlElements, schemaNamespaces);
			}
	
			// export JSON file
			if (exportJSON) {
				// export JSON-LD namespace definitions
				TreeSet<String> jsonNamespaces = new TreeSet<String>();
				for (String nsPrefix : schemaNamespaces)
					if (!nsPrefix.equals(NiemUmlClass.LOCAL_PREFIX))
						jsonNamespaces.add("\n" + jsonWriter.getJsonPair(nsPrefix, NamespaceModel.Prefixes.get(nsPrefix) + "#"));
				jsonNamespaces.add("\n" + jsonWriter.getJsonPair(NiemUmlClass.CODELIST_APPINFO_PREFIX, NiemUmlClass.CODELIST_APPINFO_URI + "#"));
				jsonNamespaces.add("\n" + jsonWriter.getJsonPair(NiemUmlClass.CT_PREFIX, NiemUmlClass.CT_URI + "#"));
				jsonNamespaces.add("\n" + jsonWriter.getJsonPair(NiemUmlClass.TERM_PREFIX, NiemUmlClass.TERM_URI + "#"));
	
				try {
					// Open JSON schema file for each extension schema and write header
					File file = Paths.get(jsonDir, prefix + JsonWriter.JSON_SCHEMA_FILE_TYPE).toFile();
					File parentFile = file.getParentFile();
					if (parentFile != null)
						parentFile.mkdirs();
					Log.debug("exportSchemas: schema " + file.toString());
					FileWriter json = new FileWriter(file);
					json.write("{\n" + jsonWriter.getJsonPair("$id", nsSchemaURI) + ",\n" + jsonWriter.getJsonPair("$schema", JsonWriter.JSON_SCHEMA_URI)
							+ ",\n" + jsonWriter.getJsonPair("type", "object") + ",\n" + "\"additionalProperties\" : false" + ",\n"
							+ "\"@context\" : {\n" + String.join(",\n", jsonNamespaces) + "},\n"
							+ "\"definitions\": {\n" + String.join(",\n", jsonDefinitions) + "\n}" + ",\n"
							+ "\"properties\" : {\n" + String.join(",\n", jsonProperties) + "\n}" + ",\n"
							+ "\"required\" : [\n" + String.join(",\n", jsonRequired) + "]" + "\n" + "}");
					json.close();
				} catch (Exception e1) {
					Log.trace("exportSchemas: error exporting JSON file " + e1.toString());
				}
			}
		}
	}
	/** return URI of a item in schemaURI with name itemName */
	static String getURI(String schemaURI, String itemName) {
		itemName.replaceAll("[^-._:A-Za-z0-9]", "");
		return schemaURI + NiemModel.HASH_DELIMITER + NamespaceModel.getName(itemName).replaceAll(NiemModel.HASH_DELIMITER, "");
	}
	/** return URI of an item */
	static String getURI(UmlItem item) {
		return item.propertyValue(NiemUmlClass.URI_PROPERTY);
	}
}
