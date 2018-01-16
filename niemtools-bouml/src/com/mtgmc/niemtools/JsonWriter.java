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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;
import fr.bouml.UmlOperation;
import fr.bouml.UmlParameter;
import fr.bouml.UmlRelation;
import fr.bouml.UmlTypeSpec;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class JsonWriter {

	private static final String ERROR_RESPONSE = "cbrn:MessageStatus";
	private static final String[] HTTP_METHODS = { "get", "put", "post", "update" };
	public static final String WEBSERVICE_STEREOTYPE = "niem-profile:webservice";
	private static final String HTTP_METHODS_PROPERTY = WEBSERVICE_STEREOTYPE + NiemUmlClass.STEREOTYPE_DELIMITER
			+ "HTTPMethods";
	static final String JSON_SCHEMA_FILE_TYPE = ".schema.json";
	private static final String JSON_SCHEMA_URI = "http://json-schema.org/draft-04/schema#";
	private static final String OPENAPI_FILE_TYPE = ".openapi.json";

	public static final String INTERFACE_STEREOTYPE = "niem-profile:interface";
	public static final String INTERFACE_PATH_PROPERTY = INTERFACE_STEREOTYPE + NiemUmlClass.STEREOTYPE_DELIMITER + "Path";
	private static final String JSON_LD_ID_ELEMENT = "@id";
	private static final String JSON_LD_ID_ELEMENT_TYPE = "xs:NCName";

	private String directory;

	/**
	 * @param initialDirectory
	 */
	public JsonWriter(String initialDirectory) {
		super();
		directory = initialDirectory;
	}

	/**
	 * @param mult
	 * @return converted multiplicity from UML representation to XML representation as a String
	 */
	private String convertMultiplicity(String mult) {
		mult = mult.replaceAll("\\.\\.", ",").replaceAll("\\*", "unbounded");
		return mult;
	}

	/**
	 * @param type
	 * @param element
	 * @param multiplicity
	 * @param localPrefix
	 * @param isAttribute
	 * @return JSON property description of an element with name elementName and
	 * multiplicity
	 */
	private String exportJsonElementInTypeSchema(UmlClass type, UmlClassInstance element, String multiplicity,
			String localPrefix, boolean isAttribute) {
		String elementName = NamespaceModel.getPrefixedName(element);
		String elementName2 = null;
		String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
		String maxOccurs = NiemUmlClass.getMaxOccurs(multiplicity);
		Path typePath = getJsonPath(type);
		String elementRef = exportJsonPointer(typePath, element);
		if (isAttribute) {
			if (elementName.endsWith("@id") || elementName.endsWith("@ref") || elementName.equals("structures:id") || elementName.equals("structures:ref")) {
				elementName2 = JSON_LD_ID_ELEMENT;
				element = NiemUmlClass.getSubsetModel().getElement(NiemModel.XSD_URI, JSON_LD_ID_ELEMENT_TYPE);
				UmlClass baseType = NiemUmlClass.getSubsetModel().getType(NiemModel.XSD_URI, JSON_LD_ID_ELEMENT_TYPE);
				elementRef = exportJsonPointer(typePath, baseType);
			} else {
				elementName = NamespaceModel.filterAttributePrefix(elementName);
				elementName2 = elementName;
			}
		} else
			elementName2 = elementName;

		String elementSchema = "";
		elementSchema += "\"" + elementName2 + "\": {\n";
		if (element!= null && element.description() != null && !element.description().equals(""))
			elementSchema += "\"description\": \"" + filterQuotes(element.description()) + "\",";
		if (maxOccurs.equals("1"))
			elementSchema += "\"$ref\": \"" + elementRef + "\"\n";
		else {
			// OpenAPI 2.0 does not support "oneOf"
			//elementSchema += "\"oneOf\": [";
			//if (minOccurs.equals("0") || minOccurs.equals("1")) {
			//	elementSchema += "{\n" + "\"$ref\": \"" + elementRef + "\"\n" + "},\n";
			//}
			//elementSchema += "{\n";
			elementSchema += "\"items\": {\n" + "\"$ref\": \"" + elementRef + "\"\n" + "},\n" + "\n\"minItems\": " + minOccurs + ",\n";
			if (!maxOccurs.equals("unbounded"))
				elementSchema += "\n\"maxItems\": " + maxOccurs + ",\n";
			elementSchema += "\"type\": \"array\"\n";
			//elementSchema += "}\n" + "]\n";
		}
		elementSchema += "}";
		return elementSchema;
	}

	/**
	 * @param element
	 * @param prefix
	 * @return JSON schema definition as a String
	 */
	String exportJsonElementSchema(UmlClassInstance element, String prefix) {
		String elementName = NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element));
		TreeSet<String> jsonDefinition = new TreeSet<String>();
		if (element != null && element.description() != null && !element.description().equals(""))
			jsonDefinition.add("\"description\": \"" + filterQuotes(element.description()) + "\"");
		UmlClass baseType = NiemModel.getBaseType(element);

		// if derived from XSD primitive, use the primitive as base type
		UmlClass baseType2 = baseType;
		UmlClass nextBaseType = NiemModel.getBaseType(baseType);
		while (nextBaseType != null) {
			baseType2 = nextBaseType;
			nextBaseType = NiemModel.getBaseType(baseType2);
		}
		if (NamespaceModel.getPrefix(baseType2).equals(NiemModel.XSD_PREFIX))
			baseType = baseType2;
		Path elementPath = getJsonPath(element);
		jsonDefinition.add("\"$ref\": \"" + exportJsonPointer(elementPath, baseType) + "\"");
		String elementSchema = "\"" + elementName + "\": {\n" + String.join(",", jsonDefinition) + "\n}\n";
		return elementSchema;
	}

	/**
	 * @param sourcePath
	 * @param targetItem
	 * @return JSON Pointer to a type/element with name tagName from file sourceFileNAme to targetFileName or "" if unknown prefix or namespace
	 */
	private String exportJsonPointer(Path sourcePath, UmlItem targetItem) {
		if (targetItem == null)
			return "";
		String targetPrefix = NamespaceModel.getPrefix(targetItem);
		if (targetPrefix == null || NamespaceModel.isExternalPrefix(targetPrefix))
			return "";
		Path targetPath = getJsonPath(targetItem);
		
		// different file
		String path = "";
		if (!sourcePath.equals(targetPath)) {
			path = sourcePath.getParent().relativize(targetPath).toString().replaceAll("\\\\", "/");
			if (!path.startsWith("/") && (!path.startsWith(".")))
				path = "./" + path;
		}
		String prefixedName = NamespaceModel.getPrefixedName(targetItem);
		if (prefixedName.contains(NamespaceModel.ATTRIBUTE_PREFIX) && !prefixedName.endsWith(JSON_LD_ID_ELEMENT))
			prefixedName = NamespaceModel.filterAttributePrefix(prefixedName);
		path += "#/definitions/" + prefixedName;
		//Log.trace("exportJsonPointer: " + sourcePath.toString() + " " + targetPath.toString() + " " + path);
		return path;
	}

	/**
	 * @param type
	 * @return JSON type definition corresponding to an UML primitive type as a String
	 */
	String exportJsonPrimitiveSchemafromUml(UmlTypeSpec type) {
		String name = type.toString();
		String jsonType = "";
		switch (name) {
		case "bool":
			jsonType += "\"type\": \"boolean\"\n";
			break;

			// numeric types
		case "double":
		case "float":
			jsonType += "\"type\": \"number\"\n";
			break;
		case "int":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": –2147483648,\n";
			jsonType += "\"maximum\": 2147483647\n";
			break;
		case "long":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -9223372036854775808,\n";
			jsonType += "\"maximum\": 9223372036854775807\n";
			break;
		case "uLong":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 18446744073709551615\n";
			break;
		case "uint":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 4294967295\n";
			break;
		case "short":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -32768,\n";
			jsonType += "\"maximum\": 32767\n";
			break;
		case "ushort":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 65535\n";
			break;
		case "char":
		case "byte":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -128,\n";
			jsonType += "\"maximum\": 127\n";
			break;
		case "uchar":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 255\n";
			break;
			
		// String types
		case "string":
			jsonType += "\"type\": \"string\"\n";
			break;
		// Other types
		case "any":
		case "void":
			return null;
		default:
			Log.trace("exportJsonPrimitiveSchemafromUml: error - type not recognized " + name);
			return "";
		}
		return jsonType;
	}


	/**
	 * @param type
	 * @return JSON type definition corresponding to an XML Schema primitive type as a String
	 */
	String exportJsonPrimitiveSchemafromXML(UmlClass type) {
		String jsonType = "\"" + NamespaceModel.getPrefixedName(type) + "\": {\n";
		switch (NamespaceModel.getName(type)) {
		case "boolean":
			jsonType += "\"type\": \"boolean\"\n";
			break;

			// numeric types
		case "decimal":
		case "double":
		case "float":
			jsonType += "\"type\": \"number\"\n";
			break;
		case "int":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": –2147483648,\n";
			jsonType += "\"maximum\": 2147483647\n";
			break;
		case "integer":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0\n";
			break;
		case "long":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -9223372036854775808,\n";
			jsonType += "\"maximum\": 9223372036854775807\n";
			break;
		case "unsignedLong":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 18446744073709551615\n";
			break;
		case "unsignedInt":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 4294967295\n";
			break;
		case "short":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -32768,\n";
			jsonType += "\"maximum\": 32767\n";
			break;
		case "unsignedShort":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 65535\n";
			break;
		case "byte":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": -128,\n";
			jsonType += "\"maximum\": 127\n";
			break;
		case "unsignedByte":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0,\n";
			jsonType += "\"maximum\": 255\n";
			break;
		case "negativeInteger":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"maximum\": -1\n";
			break;
		case "nonNegativeInteger":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 0\n";
			break;
		case "nonPositiveInteger":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"maximum\": 0\n";
			break;
		case "positiveInteger":
			jsonType += "\"type\": \"number\",\n";
			jsonType += "\"multipleOf\": 1.0,\n";
			jsonType += "\"minimum\": 1\n";
			break;

			// date/time types
		case "date":
		case "dateTime":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"format\": \"date-time\"\n";
			break;
		case "time":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^([0-9]{2}):([0-9]{2}):([0-9]{2}([.][0-9]{1,6})?)([+-]([0-9]{2}):([0-9]{2}))?$\"\n";
			break;
		case "duration":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[-+]?P(([0-9]d+Y)|([0-9]+M)|([0-9]+D)|(T([0-9]+H)|([0-9]+M)|([0-9]+([.][0-9]{1,6})?S)))$\"\n";
			break;
		case "gDay":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^---[0-3][0-9]$\"\n";
			break;
		case "gMonth":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^--[0-1][0-9]$\"\n";
			break;
		case "gMonthDay":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^--[0-1][0-9]-[0-3][0-9]$\"\n";
			break;
		case "gYear":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[0-9]{4}$\"\n";
			break;
		case "gYearMonth":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[0-9]{4}-[0-1][0-9]$\"\n";
			break;

			// string types
		case "token":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^\\\\S*$\"\n";
			break;
		case "normalizedString":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^\\\\s?(\\\\S+\\\\s?)+\\\\s?$\"\n";
			break;
		case "NMTOKEN":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[-.:_A-Za-z0-9]+$\"\n";
			break;
		case "NMTOKENS":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^([-.:_A-Za-z0-9]+\\\\s)+$\"\n";
			break;
		case "NAME":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[_:A-Za-z][-.:_A-Za-z0-9]*$\"\n";
			break;
		case "language":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*$\"\n";
			break;
		case "hexBinary":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^([A-Z0-9]{2})*$\"\n";
			break;
		case "base64Binary":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[A-Za-z0-9+/=\\\\s]*$\"\n";
			break;

			// reference types
		case "anyURI":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"format\": \"uri\"\n";
			break;
		case "ID":
		case "IDREF":
		case "NCNAME":
		case "ENTITY":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[_A-Za-z][-._A-Za-z0-9]*$\"\n";
			break;
		case "IDREFS":
		case "ENTITIES":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^([_A-Za-z][-._A-Za-z0-9]*\\\\s)*$\"\n";
			break;
		case "NOTATION":
		case "QName":
			jsonType += "\"type\": \"string\",\n";
			jsonType += "\"pattern\": \"^[_A-Za-z][-._A-Za-z0-9]*:[_A-Za-z][-._A-Za-z0-9]*$\"\n";
			break;

		default:
			jsonType += "\"type\": \"string\"\n";
		}
		jsonType += "}\n";
		return jsonType;
	}

	/**
	 * @param prefix
	 * @param nsSchemaURI
	 * @param schemaNamespaces
	 * @param jsonDefinitions
	 * @param jsonProperties
	 * @param jsonRequired
	 */
	void exportJsonSchema(String prefix, String nsSchemaURI, TreeSet<String> schemaNamespaces, TreeSet<String> jsonDefinitions, TreeSet<String> jsonProperties, TreeSet<String> jsonRequired) {
		// export JSON-LD namespace definitions
		TreeSet<String> jsonNamespaces = new TreeSet<String>();
		for (String nsPrefix : schemaNamespaces)
			if (!nsPrefix.equals(NiemModel.LOCAL_PREFIX))
				jsonNamespaces.add("\n" + getJsonPair(nsPrefix, NamespaceModel.getSchemaURIForPrefix(nsPrefix) + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.CODELIST_APPINFO_PREFIX, XmlWriter.CODELIST_APPINFO_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.CT_PREFIX, XmlWriter.CT_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.TERM_PREFIX, XmlWriter.TERM_URI + "#"));

		try {
			// Open JSON schema file for each extension schema and write header
			Path path = getJsonPath(prefix);
			File parentFile = path.getParent().toFile();
			if (parentFile != null)
				parentFile.mkdirs();
			
			Log.debug("exportSchemas: schema " + path.toString());
			FileWriter json = new FileWriter(path.toFile());
			json.write("{\n" + getJsonPair("$id", nsSchemaURI) + ",\n" + getJsonPair("$schema", JsonWriter.JSON_SCHEMA_URI)
			+ ",\n" + getJsonPair("type", "object") + ",\n" + "\"additionalProperties\" : false" + ",\n"
			+ "\"@context\" : {\n" + String.join(",\n", jsonNamespaces) + "},\n"
			+ "\"definitions\": {\n" + String.join(",\n", jsonDefinitions) + "\n}" + ",\n"
			+ "\"properties\" : {\n" + String.join(",\n", jsonProperties) + "\n}" + ",\n"
			+ "\"required\" : [\n" + String.join(",\n", jsonRequired) + "]" + "\n" + "}");
			json.close();
		} catch (Exception e1) {
			Log.trace("exportSchemas: error exporting JSON file " + e1.toString());
		}
	}

	/**
	 * @param model
	 * @param type
	 * @param prefix
	 * @return JSON schema type definition as a String
	 */
	String exportJsonTypeSchema(NiemModel model, UmlClass type, String prefix) {
		// add properties
		// type.sortChildren();
		TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
		TreeSet<String> jsonElementsInType = new TreeSet<String>();
		String anyElement = NamespaceModel.getPrefixedName(NiemModel.XSD_PREFIX, NiemModel.ANY_ELEMENT_NAME);
		Boolean anyJSON = false;
		UmlClass type2 = type, baseType2 = null;
		while (type2 != null) {
			for (UmlItem item4 : type2.children()) {
				if (item4.kind() == anItemKind.anAttribute) {
					UmlAttribute attribute = (UmlAttribute) item4;
					NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(attribute));
					UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(attribute));
					if (element == null)
						continue;
					String elementName = NamespaceModel.getPrefixedName(element);
					boolean elementIsAttribute = NiemUmlClass.isAttribute(element);
					Log.debug("exportSchema: exporting element in type " + elementName);
					if (elementName.equals(anyElement)) {
						anyJSON = true;
						continue;
					}
					// if (complexContent)
					String multiplicity = attribute.multiplicity();
					UmlClass elementBaseType = NiemModel.getBaseType(element);
					// if (elementBaseType == null && elementName.endsWith(AUGMENTATION_TYPE_NAME))
					// elementBaseType = SubsetModel.augmentationType;

					if (NiemModel.Substitutions.containsKey(elementName)) {
						// relax minoccurs if substitutions
						String multiplicity2 = "0," + NiemUmlClass.getMaxOccurs(multiplicity);
						// add head element if not abstract
						if ((elementBaseType != model.getAbstractType())) {
							String jsonElementInType = exportJsonElementInTypeSchema(type, element, multiplicity2,
									prefix, elementIsAttribute);
							if (jsonElementInType != null)
								jsonElementsInType.add(jsonElementInType);
						}
						List<UmlClassInstance> enlist = (NiemModel.Substitutions.get(elementName));
						// add substitution elements
						for (UmlClassInstance element2 : enlist) {
							String jsonElementInType = exportJsonElementInTypeSchema(type,
									element2, multiplicity2, prefix, NiemUmlClass.isAttribute(element2));
							if (jsonElementInType != null)
								jsonElementsInType.add(jsonElementInType);
						}
					} else if ((elementBaseType != model.getAbstractType())) {
						String jsonElementInType = exportJsonElementInTypeSchema(type, element, multiplicity,
								prefix, elementIsAttribute);
						if (jsonElementInType != null)
							jsonElementsInType.add(jsonElementInType);
						if (Integer.parseInt(NiemUmlClass.getMinOccurs(multiplicity)) > 0)
							jsonRequiredElementsInType.add("\"" + elementName + "\"");
					}
				}
				if (item4.kind() == anItemKind.aRelation) {
					UmlRelation relation = (UmlRelation) item4;
					if (relation.relationKind() == aRelationKind.aGeneralisation) // base type
						baseType2 = relation.roleType();
					if (relation.relationKind() == aRelationKind.aDirectionalAggregation) { // attributeGroup
						UmlClass sourceBaseType = relation.roleType();
						for (UmlItem item5 : sourceBaseType.children()) {
							if (item5.kind() != anItemKind.anAttribute)
								// if (getName(item5).equals("@id") || getName(item5).equals("@ref"))
								continue;
							UmlAttribute attribute = (UmlAttribute) item5;
							NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(attribute));
							UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(attribute));
							if (element == null || !NiemUmlClass.isAttribute(element))
								continue;
							String elementName = NamespaceModel.getPrefixedName(element);
							UmlClass elementBaseType = NiemModel.getBaseType(element);
							String multiplicity = attribute.multiplicity();
							String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
							if (elementBaseType != model.getAbstractType()) {
								String jsonElementInType = exportJsonElementInTypeSchema(type, element,
										multiplicity, prefix, NiemUmlClass.isAttribute(element));
								if (jsonElementInType != null)
									jsonElementsInType.add(jsonElementInType);
								if (Integer.parseInt(minOccurs) > 0)
									jsonRequiredElementsInType.add("\"" + elementName + "\"");
							}
						}
					}
				}
			}
			type2 = baseType2;
			baseType2 = null;
		}
		// get code list
		String codeList = type.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		Set<String> enums = new HashSet<String>();
		if (codeList != null && codeList.equals("")) {
			for (String code : codeList.split(NiemModel.CODELIST_DELIMITER)) {
				if (code.equals(""))
					continue;
				String[] codeParams = code.replace("&", "&amp;").split(NiemModel.CODELIST_DEFINITION_DELIMITER);
				String codeValue = codeParams[0].trim();
				if (!codeValue.equals(""))
					enums.add("\"" + codeValue + "\"");
			}
		}
		// define JSON type
		TreeSet<String> jsonDefinition = new TreeSet<String>();
		String description = type.description();
		if (description != null && !description.equals(""))
			jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
		// UmlClass baseType = getBaseType(type);
		// if (baseType != null)
		// jsonDefinition.add("\"$ref\": \"" +
		// exportJsonTypePointer(getPrefixedName(baseType), prefix) + "\"");
		jsonDefinition.add("\"type\": \"object\"");
		jsonDefinition.add("\"additionalProperties\" : " + anyJSON);
		if (codeList != null && codeList.equals(""))
			jsonDefinition.add("\"enums\": [" + String.join(",", enums) + "]");
		if (jsonElementsInType != null)
			jsonDefinition.add("\"properties\": {\n" + String.join(",", jsonElementsInType) + "\n}");
		if (jsonRequiredElementsInType != null && jsonRequiredElementsInType.size()>0)
			jsonDefinition.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
		String typeSchema = "\"" + NamespaceModel.getPrefixedName(type) + "\": {\n" + String.join(",", jsonDefinition) + "\n}\n";
		return typeSchema;
	}

	/** exports OpenAPI/Swagger 2.0 service definition
	 * @param openapiDir
	 * @param ports
	 * @param messageNamespaces
	 * @param jsonDefinitions
	 * @throws IOException
	 */
	void exportOpenApi(String openapiDir, Map<String, UmlClass> ports, Set<String> messageNamespaces, TreeSet<String> jsonDefinitions) throws IOException {

		// export JSON-LD namespace definitions
		TreeSet<String> jsonNamespaces = new TreeSet<String>();
		for (String nsPrefix : messageNamespaces)
			if (!nsPrefix.equals(NiemModel.LOCAL_PREFIX))
				jsonNamespaces.add("\n" + getJsonPair(nsPrefix, NamespaceModel.getSchemaURIForPrefix(nsPrefix) + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.CODELIST_APPINFO_PREFIX, XmlWriter.CODELIST_APPINFO_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.CT_PREFIX, XmlWriter.CT_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(XmlWriter.TERM_PREFIX, XmlWriter.TERM_URI + "#"));

		// generate OpenAPI definitions
		Log.trace("Generating OpenAPIs");
		for (UmlClass port : ports.values()) {
			String portName = port.name();
			String portPath = port.propertyValue(INTERFACE_PATH_PROPERTY);
			// write OpenAPI paths
			TreeSet<String> openapiPaths = new TreeSet<String>();

			// for each OpenAPI path
			//TreeSet<String> jsonDefinitions = new TreeSet<String>();
			TreeSet<String> jsonProperties = new TreeSet<String>();

			// get directory path
			Path openapiPath = Paths.get(openapiDir, portName + OPENAPI_FILE_TYPE);

			for (UmlItem item : port.children()) {
				if (item.kind() == anItemKind.anOperation) {
					TreeSet<String> openapiOperations = new TreeSet<String>();
					LinkedHashSet<String> openapiPathParameters = new LinkedHashSet<String>();
					LinkedHashSet<String> openapiBodyParameters = new LinkedHashSet<String>();
					LinkedHashSet<String> openapiResponses = new LinkedHashSet<String>();
					UmlOperation operation = (UmlOperation) item;
					String operationName = operation.name();
					String operationPath = "/" + operationName;
					String httpMethod = operation.propertyValue(JsonWriter.HTTP_METHODS_PROPERTY);
					if (httpMethod == null)
						continue;
					httpMethod = httpMethod.toLowerCase();

					Log.debug("exportOpenAPI: generating document/literal input wrapper for " + portName + "/"
							+ operationName);
					UmlClass outputType = null, inputType = null;
					UmlParameter[] params = operation.params();
					if (params != null) {
						TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
						TreeSet<String> jsonElementsInType = new TreeSet<String>();
						String elementName = operationName + "Request";
						String inputTypeName = elementName + "Type";
						for (UmlParameter param : params) {
							//String paramName = param.name;
							if (!param.name.equals("") && !param.name.equals("body")) {
								String[] paramNames = param.name.split(":");
								if (paramNames.length < 2) {
									Log.trace("exportOpenAPI: error - illegal parameter " + param.name + " in " + portName);
									continue;
								}
								String paramKind = paramNames[0].trim();
								String paramName = paramNames[1].trim();
								switch (paramKind) {
								case "path":
									operationPath += "/{" + paramName + "}";
								case "query":
								case "header":
								case "cookie":
									break;
								default:
									Log.trace("exportOpenAPI: unreognized parameter type " + param.name + " in " + portName);
									continue;
								}
								
								// URL parameters
								UmlTypeSpec paramType2 = param.type;
								if (paramType2 == null)
									continue;
								//UmlClass paramType = paramType2.type;
								//if (paramType == null)
								//	continue;
								String paramSchema = exportJsonPrimitiveSchemafromUml(paramType2);
								if (paramSchema == null)
									continue;
								if (paramSchema.equals(""))
									Log.trace("exportOpenAPI - operation " + operationName + " parameter " + param.name + " has no base type "); 
								String mult = param.multiplicity;
								mult = convertMultiplicity(mult);
								// String minOccurs = getMinOccurs(mult);
								String maxOccurs = NiemUmlClass.getMaxOccurs(mult);
								String required = (maxOccurs.equals("0")) ? "false" : "true";
								openapiPathParameters.add("           {\n"
										+ "            \"name\": \"" + paramName + "\",\n" 
										+ "            \"in\": \"" + paramKind + "\",\n" 
										// + " \"description\": \"" + param.type.toString() + "\",\n"
										+ "            \"required\": " + required + ",\n" 
										+ paramSchema + "\n" +
										// " \"format\": \"int64\"\n" +
										"          }");

								continue;
							}
							// body parameters
							UmlTypeSpec inputType2 = null;
							try {
								inputType2 = param.type;
								if (inputType2 == null) {
									Log.trace("exportOpenAPI: error - no input message for " + operationName);
									continue;
								}
								inputType = inputType2.type;
							} catch (Exception e) {
								Log.trace("exportOpenAPI: error - no input message for " + operationName + " " + e.toString());
							}
							if (inputType == null) {
								String inputTypeSchema = exportJsonPrimitiveSchemafromUml(inputType2);
								if (inputTypeSchema != null) {
									if (inputTypeSchema.equals(""))
										Log.trace("exportOpenAPI - operation " + operationName + " parameter " + param.name + " has no base type "); 
									openapiBodyParameters.add("{\n" 
											+ "            \"name\": \"" + elementName + "\",\n"
											+ "            \"in\": \"body\",\n" 
											//+ "            \"description\": \"" + operationName + " request\",\n" 
											+ "            \"required\": true,\n"
											+ inputTypeSchema + "\n" 
											+ "          }");
								}
							} else {
								if (!inputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE)) {
									Log.trace("exportOpenAPI: error - no NIEM input message for " + operationName);
									continue;
								}
								String inputMessage = inputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_PROPERTY);
								if (inputMessage == null || inputMessage.equals("")) {
									Log.trace("exportOpenAPI: error - NIEM XPath not defined for input message for " + operationName);
									continue;
								}
								
								Log.debug("exportOpenAPI: input Message: " + inputMessage + " from operation " + operationName);
								messageNamespaces.add(NamespaceModel.getPrefix(inputMessage));
								String mult = param.multiplicity;
	
								mult = convertMultiplicity(mult);
								// String maxOccurs = getMaxOccurs(mult);
								String prefix = NamespaceModel.getPrefix(inputMessage);
								if (!NamespaceModel.isExternalPrefix(prefix)) {
									String schemaURI = NamespaceModel.getSchemaURI(inputMessage);
									UmlClassInstance messageElement = (NamespaceModel.isNiemPrefix(prefix)) ? 
										NiemUmlClass.getSubsetModel().getElement(schemaURI, inputMessage) : 
										NiemUmlClass.getExtensionModel().getElement(schemaURI, inputMessage);
									jsonElementsInType.add(exportOpenApiElementInTypeSchema(openapiPath, messageElement, mult, false));
								}
								if (Integer.parseInt(NiemUmlClass.getMinOccurs(mult)) > 0)
									jsonRequiredElementsInType.add("\"" + inputMessage + "\"");
								// for each input parameter
								openapiBodyParameters.add("{\n" + "            \"name\": \"" + elementName + "\",\n"
										+ "            \"in\": \"body\",\n" + "            \"description\": \""
										+ operationName + " request\",\n" + "            \"required\": true,\n"
										+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/"
										+ elementName + "\"\n" + "            }\r\n" + "          }");
							}
						}
						// export type wrapper
						TreeSet<String> jsonDefinition = new TreeSet<String>();
						// String description = "";
						// if (description != null && !description.equals(""))
						// jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
						jsonDefinition.add("\"type\": \"object\"");
						if (jsonElementsInType != null && jsonElementsInType.size()>0)
							jsonDefinition.add("\"properties\": {\n" + String.join(",", jsonElementsInType) + "\n}");
						if (jsonRequiredElementsInType != null && jsonRequiredElementsInType.size()>0)
							jsonDefinition
							.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
						String typeSchema = "\"" + inputTypeName + "\": {\n" + String.join(",", jsonDefinition)
						+ "\n}\n";

						// export element wrapper
						String elementSchema = "\"" + elementName + "\": {\n";
						elementSchema += "\"description\": \"An input message\",";
						elementSchema += "\"$ref\": \"#/definitions/" + inputTypeName + "\"" + "\n}\n";
						
						// swagger code generation tools do not support relative references, rename them to local references
						elementSchema = elementSchema.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")","$1#/$3");
						typeSchema = typeSchema.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")","$1#/$3");

						jsonProperties.add(elementSchema);
						jsonDefinitions.add(typeSchema);
					}
					Log.debug("exportOpenAPI: generating document/literal output wrappers for " + operationName);
					UmlTypeSpec outputType2 = null;
					try {
						outputType2 = operation.returnType();
						if (outputType2 == null || outputType2.equals("")) {
							Log.trace("exportOpenAPI: error - no output message for " + operationName);
							continue;
						}
						outputType = outputType2.type;
					} catch (Exception e) {
						Log.trace("exportOpenAPI: error - no output message for " + operationName + " " + e.toString());
					}
					if (outputType == null) {
						String outputTypeSchema = exportJsonPrimitiveSchemafromUml(outputType2);
						if (outputTypeSchema == null)
							openapiResponses.add("\n" 
									+ "          \"200\": {\n" 
									+ "            \"description\": \"" + operationName + " response\"\n" 
									+ "            }");
						else  {
							if (outputTypeSchema.equals(""))
								Log.trace("exportOpenAPI - operation " + operationName + " response has no base type "); 
							openapiResponses.add("\n" 
									+ "          \"200\": {\n" 
									+ "            \"description\": \"" + operationName + " response\",\n" 
									+ "\"schema\": {" + outputTypeSchema + "}\n" 
									+ "            }");
						}
					} else {
						if (!outputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE)) {
							Log.trace("exportOpenAPI: error - no NIEM output message for " + operationName);
							continue;
						}
						String outputMessage = outputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_PROPERTY);
						if (outputMessage == null || outputMessage.equals("")) {
							Log.trace("exportOpenAPI: error - NIEM XPath not defined for output message for " + operationName);
							continue;
						}
						Log.debug("exportOpenAPI: output Message: " + outputMessage + " from operation " + operationName);
						//ArrayList<String> outputs = new ArrayList<String>();
						//if (outputMessage != null)
						//	outputs.add(outputMessage);
						//if (JsonWriter.ERROR_RESPONSE != null)
						//	outputs.add(JsonWriter.ERROR_RESPONSE);
						//for (String message : outputs) {
							String message = outputMessage;
							TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
							TreeSet<String> jsonElementsInType = new TreeSet<String>();
							//String elementName = (message.equals(JsonWriter.ERROR_RESPONSE) ? "Error" : operationName) + "Response";
							String elementName = operationName + "Response";
							String outputTypeName = elementName + "Type";
							String mult = "1";
							String prefix = NamespaceModel.getPrefix(message);
							if (!NamespaceModel.isExternalPrefix(prefix)) {
								String schemaURI = NamespaceModel.getSchemaURI(message);
								UmlClassInstance messageElement = (NamespaceModel.isNiemPrefix(prefix)) ? 
									NiemUmlClass.getSubsetModel().getElement(schemaURI, message) : 
									NiemUmlClass.getExtensionModel().getElement(schemaURI, message);
								jsonElementsInType
								.add(exportOpenApiElementInTypeSchema(openapiPath, messageElement, mult, false));
								jsonRequiredElementsInType.add("\"" + message + "\"");
							}
	
							// export type wrapper
							TreeSet<String> jsonDefinition = new TreeSet<String>();
							// String description = "";
							// if (description != null && !description.equals(""))
							// jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
							jsonDefinition.add("\"type\": \"object\"");
							if (jsonElementsInType != null && jsonElementsInType.size()>0)
								jsonDefinition.add("\"properties\": {\n" + String.join(",", jsonElementsInType) + "\n}");
							if (jsonRequiredElementsInType != null && jsonRequiredElementsInType.size()>0)
								jsonDefinition
								.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
							String typeSchema = "\"" + outputTypeName + "\": {\n" + String.join(",", jsonDefinition)
							+ "\n}\n";
	
							// export element wrapper
							String elementSchema = "\"" + elementName + "\": {\n";
							elementSchema += "\"description\": \"An output message\",";
							elementSchema += "\"$ref\": \"#/definitions/" + outputTypeName + "\"" + "\n}\n";
							
							// swagger code generation tools do not support relative references, rename them to local references
							elementSchema = elementSchema.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")","$1#/$3");
							typeSchema = typeSchema.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")","$1#/$3");
	
							jsonProperties.add(elementSchema);
							jsonDefinitions.add(typeSchema);
							Log.debug("exportOpenAPI: exported element " + elementName + " and type " + outputTypeName);
							// add successful response
							openapiResponses.add("\n" + "          \"200\": {\n" + "            \"description\": \""
									+ operationName + " response\",\n" + "            \"schema\": {\n"
									+ "                \"$ref\": \"#/definitions/" + operationName + "Response" + "\"\n"
									+ "            }\n" + "          }");
							// add error response
							if (JsonWriter.ERROR_RESPONSE != null)
								openapiResponses.add("\n" + "          \"default\": {\n"
										+ "            \"description\": \"unexpected error\",\n"
										+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/"
										+ JsonWriter.ERROR_RESPONSE + "\"\n" + "            }\n" + "          }\n");
						//}
						}
					for (String method : JsonWriter.HTTP_METHODS) {
						if (httpMethod.contains(method)) {
							LinkedHashSet<String> openapiParameters = openapiPathParameters;
							switch (method) {
							case "put":
							case "post":
								openapiParameters.addAll(openapiBodyParameters);
							default:
								openapiOperations.add("\n" + "      \"" + method + "\": {\n"
										+ "        \"description\": \"" + operation.description() + "\\n\",\n"
										+ "        \"operationId\": \"" + operationName + "\",\n"
										+ "        \"parameters\": [" + "        " + String.join(",", openapiParameters)
										+ "\n" + "        ],\n" + "        \"responses\": {" + "        "
										+ String.join(",", openapiResponses) + "\n" + "        }" + "\n      }");
							}
						}
					}

					openapiPaths.add("\n" + "    \"" + operationPath + "\": {" + String.join(",", openapiOperations)
					+ "\n      }");
				}
			}
			// write OpenAPI file
			jsonDefinitions.addAll(jsonProperties);
			try {
				File file = openapiPath.toFile();
				File parentFile = file.getParentFile();
				if (parentFile != null)
					parentFile.mkdirs();
				FileWriter fw = new FileWriter(file);
				Log.debug("OpenAPI: " + portName + OPENAPI_FILE_TYPE);
				fw.write("{\n" +
						// jsonContext + ",\n" +
						"  \"swagger\": \"2.0\",\n" + "  \"info\": {\n" + "    \"version\": \""
						+ NiemUmlClass.getProperty(ConfigurationDialog.IEPD_VERSION_PROPERTY) + "\",\n" + "    \"title\": \"" + portName
						+ "\",\n" + "    \"description\": \"" + port.description() + "\",\n"
						+ "    \"termsOfService\": \"" + NiemUmlClass.getProperty(ConfigurationDialog.IEPD_TERMS_URL_PROPERTY) + "\",\n"
						+ "    \"contact\": {\n" + "      \"name\": \""
						+ NiemUmlClass.getProperty(ConfigurationDialog.IEPD_ORGANIZATION_PROPERTY) + "\",\n" + "      \"email\": \""
						+ NiemUmlClass.getProperty(ConfigurationDialog.IEPD_EMAIL_PROPERTY) + "\",\n" + "      \"url\": \""
						+ NiemUmlClass.getProperty(ConfigurationDialog.IEPD_CONTACT_PROPERTY) + "\"\n" + "    },\n" + "    \"license\": {\n"
						+ "      \"name\": \"" + NiemUmlClass.getProperty(ConfigurationDialog.IEPD_LICENSE_URL_PROPERTY) + "\",\n"
						+ "      \"url\": \"" + NiemUmlClass.getProperty(ConfigurationDialog.IEPD_LICENSE_URL_PROPERTY) + "\"\n" + "    }\n"
						+ "  },\n" + "  \"host\": \"host.example.com\",\n" + "  \"basePath\": \"" + portPath + "\",\n"
						+ "  \"schemes\": [\n" + "    \"http\"\n" + "  ],\n" + "  \"consumes\": [\n"
						+ "    \"application/json\"\n" + "  ],\n" + "  \"produces\": [\n"
						+ "    \"application/json\"\n" + "  ]," + "  \"paths\": {" + "  "
						+ String.join(",", openapiPaths) + "\n" + "      },\n" + "  \"definitions\": {\n"
						+ String.join(",\n", jsonDefinitions) + "\n}" + "}\n");
				fw.close();
			} catch (Exception e1) {
				Log.trace("exportOpenAPI: error exporting OpenAPI JSON " + e1.toString());
			}
		}
	}

	/**
	 * @param openapiPath
	 * @param element
	 * @param multiplicity
	 * @param isAttribute
	 * @return OpenAPI property description of an element with name elementName and
	 * multiplicity as a String
	 */
	private String exportOpenApiElementInTypeSchema(Path openapiPath, UmlItem element, String multiplicity,
			boolean isAttribute) {
		String elementName = NamespaceModel.getPrefixedName(element);
		Log.debug("exportOpenApiElementInTypeSchema: " + elementName);
		String elementName2 = null;
		String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
		String maxOccurs = NiemUmlClass.getMaxOccurs(multiplicity);
		String elementRef = exportJsonPointer(openapiPath, element);
		if (isAttribute) {
			if (elementName.equals("@id") || elementName.equals("@ref")) {
				elementName = JSON_LD_ID_ELEMENT_TYPE;
				elementName2 = JSON_LD_ID_ELEMENT;
				UmlClass baseType = NiemUmlClass.getSubsetModel().getType(NiemModel.XSD_URI, JSON_LD_ID_ELEMENT_TYPE);
				elementRef = exportJsonPointer(openapiPath, baseType);
			} else {
				elementName = NamespaceModel.filterAttributePrefix(elementName);
				elementName2 = elementName;
			}
		} else 
			elementName2 = elementName;
		String elementSchema = "";
		elementSchema += "\"" + elementName2 + "\": {\n";
		if (element!= null && element.description() != null && !element.description().equals(""))
			elementSchema += "\"description\": \"" + filterQuotes(element.description()) + "\",";
		if (maxOccurs.equals("1"))
			elementSchema += "\"$ref\": \"" + elementRef + "\"\n";
		else {
			elementSchema += "\"items\": {\n" + "\"$ref\": \"" + elementRef + "\"\n" + "},\n" + "\n\"minItems\": " + minOccurs + ",\n";
			if (!maxOccurs.equals("unbounded"))
				elementSchema += "\n\"maxItems\": " + maxOccurs + ",\n";
			elementSchema += "\"type\": \"array\"\n";
		}
		elementSchema += "}\n";
		return elementSchema;
	}

	/** filter illegal characters in JSON strings
	 * @param string
	 * @return filtered String
	 */
	private String filterQuotes(String string) {
		return string.replaceAll("\r|\n|\"|\\\\", "");
	}

	/**
	 * @param name
	 * @param value
	 * @return a JSON name value pair as a String
	 */
	private String getJsonPair(String name, String value) {
		return "\"" + name + "\" : \"" + value + "\"\n";
	}

	/**
	 * @param item
	 * @return JSON filename as a Path
	 */
	Path getJsonPath(UmlItem item) {
		return Paths.get(directory, (NiemUmlClass.isNiem(item)) ? NiemUmlClass.NIEM_DIR + "/" : "", NamespaceModel.getPrefix(item) + JsonWriter.JSON_SCHEMA_FILE_TYPE);
	}

	/**
	 * @param prefix
	 * @return JSON filename as a Path
	 */
	Path getJsonPath(String prefix) {
		String schemaURI = NamespaceModel.getSchemaURIForPrefix(prefix);
		boolean isNiem = NamespaceModel.getNamespace(schemaURI).getReferenceClassView() != null;
		return Paths.get(directory, (isNiem) ? NiemUmlClass.NIEM_DIR + "/" : "", prefix + JsonWriter.JSON_SCHEMA_FILE_TYPE);
	}
	
}
