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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlOperation;
import fr.bouml.UmlParameter;
import fr.bouml.UmlRelation;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class JsonWriter {

	public JsonWriter(String initialDirectory) {
		super();
		directory = initialDirectory;
	}

	String directory;
	
	static final String[] HTTP_METHODS = { "get", "put", "post", "update" };
	static final String HTTP_METHODS_PROPERTY = NiemUmlClass.WEBSERVICE_STEREOTYPE_TYPE + NiemUmlClass.STEREOTYPE_DELIMITER
	+ "HTTPMethods";
	static final String JSON_SCHEMA_FILE_TYPE = ".schema.json";

	// JSON
	static final String JSON_SCHEMA_URI = "http://json-schema.org/draft-04/schema#";

	private static final String OPENAPI_FILE_TYPE = ".openapi.json";

	// FIXME swagger-parser error: Unable to load RELATIVE ref:
	// ./niem/nc.schema.json
	/** return JSON Pointer to a type with name typeName */
	static String exportJsonPointer(String tagName, String localPrefix) {
		if (tagName == null)
			return "";
		String prefix = NamespaceModel.getPrefix(tagName);
		if (prefix == null)
			return "";
		if (NamespaceModel.isExternalPrefix(prefix))
			return "";
		else if (localPrefix != null && prefix.equals(localPrefix))
			return "#/definitions/" + tagName;
		if (NamespaceModel.isNiemPrefix(prefix) && !NamespaceModel.isNiemPrefix(localPrefix))
			return "./" + NiemUmlClass.NIEM_DIR + "/" + prefix + JSON_SCHEMA_FILE_TYPE + "#/definitions/" + tagName;
		else if (!NamespaceModel.isNiemPrefix(prefix) && NamespaceModel.isNiemPrefix(localPrefix))
			return "../" + prefix + JSON_SCHEMA_FILE_TYPE + "#/definitions/" + tagName;
		else
			return "./" + prefix + JSON_SCHEMA_FILE_TYPE + "#/definitions/" + tagName;
	}

	/** convert multiplicity from UML representation to XML representation */
	String convertMultiplicity(String mult) {
		mult = mult.replaceAll("\\.\\.", ",").replaceAll("\\*", "unbounded");
		return mult;
	}

	/**
	 * return JSON property description of an element with name elementName and
	 * multiplicity
	 */
	String exportJsonElementInTypeSchema(String elementName, String multiplicity, String localPrefix,
			boolean isAttribute) {
		String elementName2 = null;
		String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
		String maxOccurs = NiemUmlClass.getMaxOccurs(multiplicity);
		if (isAttribute) {
			if (elementName.equals(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, "@id"))
					|| elementName.equals(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, "@ref"))) {
				elementName = "xs:NCName";
				elementName2 = "@id";
			} else {
				elementName = NiemUmlClass.filterAttributePrefix(elementName);
				elementName2 = elementName;
			}
		} else
			elementName2 = elementName;
		String elementSchema = "";
		elementSchema += "\"" + elementName2 + "\": {\n";
		if (maxOccurs.equals("1"))
			elementSchema += "\"$ref\": \"" + exportJsonPointer(elementName, localPrefix) + "\"\n";
		else {
			elementSchema += "\"oneOf\": [";
			if (minOccurs.equals("0") || minOccurs.equals("1")) {
				elementSchema += "{\n" + "\"$ref\": \"" + exportJsonPointer(elementName, localPrefix) + "\"\n" + "},\n";
			}
			elementSchema += "{\n" + "\"items\": {\n" + "\"$ref\": \"" + exportJsonPointer(elementName, localPrefix)
					+ "\"\n" + "},\n" + "\n\"minItems\": " + minOccurs + ",\n";
			if (!maxOccurs.equals("unbounded"))
				elementSchema += "\n\"maxItems\": " + maxOccurs + ",\n";
			elementSchema += "\"type\": \"array\"\n" + "}\n" + "]\n";
		}
		elementSchema += "}";
		return elementSchema;
	}

	/** return JSON schema element definition */
	String exportJsonElementSchema(UmlClassInstance element, String prefix) {
		String elementName = NiemUmlClass.filterAttributePrefix(NamespaceModel.getPrefixedName(element));
		TreeSet<String> jsonDefinition = new TreeSet<String>();
		String description = element.description();
		if (description != null && description.equals(""))
			jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
		UmlClass baseType = NiemModel.getBaseType(element);
	
		// if derived from XSD primitive, use the primitive as base type
		UmlClass baseType2 = baseType;
		UmlClass nextBaseType = NiemModel.getBaseType(baseType);
		while (nextBaseType != null) {
			baseType2 = nextBaseType;
			nextBaseType = NiemModel.getBaseType(baseType2);
		}
		if (NamespaceModel.getPrefix(baseType2).equals(NiemUmlClass.XSD_PREFIX))
			baseType = baseType2;
		jsonDefinition.add("\"$ref\": \"" + exportJsonPointer(NamespaceModel.getPrefixedName(baseType), prefix) + "\"");
		String elementSchema = "\"" + elementName + "\": {\n" + String.join(",", jsonDefinition) + "\n}\n";
		return elementSchema;
	}

	/** return JSON type definition corresponding to an XML Schema primitive type */
	String exportJsonPrimitiveSchema(UmlClass type) {
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

	/** return JSON schema type definition 
	 * @param subsetModel TODO*/
	String exportJsonTypeSchema(NiemModel model, UmlClass type, String prefix) {
		// add properties
		// type.sortChildren();
		TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
		TreeSet<String> jsonElementsInType = new TreeSet<String>();
		String anyElement = NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, NiemUmlClass.ANY_ELEMENT_NAME);
		Boolean anyJSON = false;
		UmlClass type2 = type, baseType2 = null;
		while (type2 != null) {
			for (UmlItem item4 : type2.children()) {
				if (item4.kind() == anItemKind.anAttribute) {
					UmlAttribute attribute = (UmlAttribute) item4;
					NiemModel model2 = (NiemUmlClass.SubsetModel.elements.containsKey(NiemUmlClass.getURI(attribute))) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;
					UmlClassInstance element = model2.getElementByURI(NiemUmlClass.getURI(attribute));
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
						if ((elementBaseType != model.abstractType)) {
							String jsonElementInType = exportJsonElementInTypeSchema(elementName, multiplicity2, prefix,
									elementIsAttribute);
							if (jsonElementInType != null)
								jsonElementsInType.add(jsonElementInType);
						}
						List<UmlClassInstance> enlist = (NiemModel.Substitutions.get(elementName));
						// add substitution elements
						for (UmlClassInstance element2 : enlist) {
							String jsonElementInType = exportJsonElementInTypeSchema(NamespaceModel.getPrefixedName(element2),
									multiplicity2, prefix, NiemUmlClass.isAttribute(element2));
							if (jsonElementInType != null)
								jsonElementsInType.add(jsonElementInType);
						}
					} else if ((elementBaseType != model.abstractType)) {
						String jsonElementInType = exportJsonElementInTypeSchema(elementName, multiplicity, prefix,
								elementIsAttribute);
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
							NiemModel model2 = (NiemUmlClass.SubsetModel.elements.containsKey(NiemUmlClass.getURI(attribute))) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;
							UmlClassInstance element = model2.getElementByURI(NiemUmlClass.getURI(attribute));
							if (element == null || !NiemUmlClass.isAttribute(element))
								continue;
							String elementName = NamespaceModel.getPrefixedName(element);
							UmlClass elementBaseType = NiemModel.getBaseType(element);
							String multiplicity = attribute.multiplicity();
							String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
							if (elementBaseType != model.abstractType) {
								String jsonElementInType = exportJsonElementInTypeSchema(elementName, multiplicity,
										prefix, NiemUmlClass.isAttribute(element));
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
			for (String code : codeList.split(NiemUmlClass.CODELIST_DELIMITER)) {
				if (code.equals(""))
					continue;
				String[] codeParams = code.replace("&", "&amp;").split(NiemUmlClass.CODELIST_DEFINITION_DELIMITER);
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
		if (jsonRequiredElementsInType != null)
			jsonDefinition.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
		String typeSchema = "\"" + NamespaceModel.getPrefixedName(type) + "\": {\n" + String.join(",", jsonDefinition) + "\n}\n";
		return typeSchema;
	}

	// FIXME Generating duplicate "get", "/GetCase", "/GetCaseList" and
	// "/GetDocument" in CourtRecordMDE.openapi.json
	// FIXME Generating duplicate "get" and "/GetCourtSchedule" in
	// CourtSchedulingMDE.openapi.json
	/** exports OpenAPI/Swagger 2.0 service definition */
	void exportOpenApi(String openapiDir, Map<String, UmlClass> ports, Set<String> messageNamespaces) throws IOException {
	
		// export JSON-LD namespace definitions
		TreeSet<String> jsonNamespaces = new TreeSet<String>();
		for (String nsPrefix : messageNamespaces)
			if (!nsPrefix.equals(NiemUmlClass.LOCAL_PREFIX))
				jsonNamespaces.add("\n" + getJsonPair(nsPrefix, NamespaceModel.Prefixes.get(nsPrefix) + "#"));
		jsonNamespaces.add("\n" + getJsonPair(NiemUmlClass.CODELIST_APPINFO_PREFIX, NiemUmlClass.CODELIST_APPINFO_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(NiemUmlClass.CT_PREFIX, NiemUmlClass.CT_URI + "#"));
		jsonNamespaces.add("\n" + getJsonPair(NiemUmlClass.TERM_PREFIX, NiemUmlClass.TERM_URI + "#"));
	
		// generate OpenAPI definitions
		Log.trace("Generating OpenAPIs");
		for (UmlClass port : ports.values()) {
			String portName = port.name();
			// write OpenAPI paths
			TreeSet<String> openapiPaths = new TreeSet<String>();
	
			// for each path
			TreeSet<String> jsonDefinitions = new TreeSet<String>();
			TreeSet<String> jsonProperties = new TreeSet<String>();
	
			// get relative path
			Path openapiPath = Paths.get(openapiDir, portName + OPENAPI_FILE_TYPE);
			Path jsonPath = Paths.get(directory);
			String relativePath = "./" + openapiPath.getParent().relativize(jsonPath).toString().replaceAll("\\\\", "/")
					+ "/";
			Log.debug("exportOpenAPI: relative path to json: " + relativePath);
	
			for (UmlItem item : port.children()) {
				if (item.kind() == anItemKind.anOperation) {
					TreeSet<String> openapiOperations = new TreeSet<String>();
					LinkedHashSet<String> openapiPathParameters = new LinkedHashSet<String>();
					LinkedHashSet<String> openapiBodyParameters = new LinkedHashSet<String>();
					LinkedHashSet<String> openapiResponses = new LinkedHashSet<String>();
					UmlOperation operation = (UmlOperation) item;
					String operationName = operation.name();
					String httpMethod = operation.propertyValue(JsonWriter.HTTP_METHODS_PROPERTY).toLowerCase();
	
					Log.trace("exportOpenAPI: generating document/literal input wrapper for " + portName + "/"
							+ operationName);
					UmlClass outputType = null, inputType = null;
					UmlParameter[] params = operation.params();
					if (params != null) {
						TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
						TreeSet<String> jsonElementsInType = new TreeSet<String>();
						String elementName = operationName + "Request";
						String inputTypeName = elementName + "Type";
						for (UmlParameter param : params) {
							String paramName = param.name;
							if (!paramName.equals("") && !paramName.equals("body")) {
								// URL parameters
								String paramType = param.type.toString();
								switch (paramType) {
								case "string":
								case "int":
									break;
								default:
									paramType = "string";
								}
								String mult = param.multiplicity;
								mult = convertMultiplicity(mult);
								// String minOccurs = getMinOccurs(mult);
								String maxOccurs = NiemUmlClass.getMaxOccurs(mult);
								String required = (maxOccurs.equals("0")) ? "false" : "true";
								openapiPathParameters.add("           {\n" + "            \"name\": \"" + paramName
										+ "\",\n" + "            \"in\": \"path\",\n" +
										// " \"description\": \"" + param.type.toString() + "\",\n" +
										"            \"required\": " + required + ",\n" + "            \"type\": \""
										+ paramType + "\"\n" +
										// " \"format\": \"int64\"\n" +
										"          }");
								continue;
							}
							// body parameters
							try {
								inputType = param.type.type;
							} catch (Exception e) {
								Log.trace("exportOpenAPI: error - no input message for " + operationName);
							}
							if (inputType == null || !inputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE_TYPE))
								continue;
							String inputMessage = inputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_XPATH);
							if (inputMessage == null || inputMessage.equals(""))
								continue;
							Log.debug("exportOpenAPI: input Message: " + inputMessage + " from operation " + operationName);
							messageNamespaces.add(NamespaceModel.getPrefix(inputMessage));
							String mult = param.multiplicity;
	
							mult = convertMultiplicity(mult);
							// String maxOccurs = getMaxOccurs(mult);
							if (!NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(inputMessage)))
								jsonElementsInType.add(exportOpenApiElementInTypeSchema(relativePath, inputMessage,
										mult, null, false));
							if (Integer.parseInt(NiemUmlClass.getMinOccurs(mult)) > 0)
								jsonRequiredElementsInType.add("\"" + inputMessage + "\"");
							// for each input parameter
							openapiBodyParameters.add("{\n" + "            \"name\": \"" + elementName + "\",\n"
									+ "            \"in\": \"body\",\n" + "            \"description\": \""
									+ operationName + " request\",\n" + "            \"required\": true,\n"
									+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/"
									+ elementName + "\"\n" + "            }\r\n" + "          }");
						}
						// export type wrapper
						TreeSet<String> jsonDefinition = new TreeSet<String>();
						// String description = "";
						// if (description != null && !description.equals(""))
						// jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
						jsonDefinition.add("\"type\": \"object\"");
						if (jsonElementsInType != null)
							jsonDefinition.add("\"properties\": {\n" + String.join(",", jsonElementsInType) + "\n}");
						if (jsonRequiredElementsInType != null)
							jsonDefinition
									.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
						String typeSchema = "\"" + inputTypeName + "\": {\n" + String.join(",", jsonDefinition)
								+ "\n}\n";
	
						// export element wrapper
						String elementSchema = "\"" + elementName + "\": {\n" + "\"$ref\": \"#/definitions/"
								+ inputTypeName + "\"" + "\n}\n";
						jsonProperties.add(elementSchema);
						jsonDefinitions.add(typeSchema);
					}
					Log.debug("exportOpenAPI: generating document/literal output wrappers for " + operationName);
					try {
						outputType = operation.returnType().type;
					} catch (Exception e) {
						Log.trace("exportOpenAPI: error - no output message for " + operationName + " " + e.toString());
					}
					if (outputType == null || !outputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE_TYPE))
						continue;
					String outputMessage = outputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_XPATH);
					if (outputMessage == null || outputMessage.equals(""))
						continue;
					Log.debug("exportOpenAPI: output Message: " + outputMessage + " from operation " + operationName);
					ArrayList<String> outputs = new ArrayList<String>();
					if (outputMessage != null)
						outputs.add(outputMessage);
					if (NiemUmlClass.ERROR_RESPONSE != null)
						outputs.add(NiemUmlClass.ERROR_RESPONSE);
					for (String message : outputs) {
						TreeSet<String> jsonRequiredElementsInType = new TreeSet<String>();
						TreeSet<String> jsonElementsInType = new TreeSet<String>();
						String elementName = (message.equals(NiemUmlClass.ERROR_RESPONSE) ? "Error" : operationName) + "Response";
						String outputTypeName = elementName + "Type";
						String mult = "1";
						if (!NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(message))) {
							jsonElementsInType
									.add(exportOpenApiElementInTypeSchema(relativePath, message, mult, null, false));
							jsonRequiredElementsInType.add("\"" + message + "\"");
						}
	
						// export type wrapper
						TreeSet<String> jsonDefinition = new TreeSet<String>();
						// String description = "";
						// if (description != null && !description.equals(""))
						// jsonDefinition.add("\"description\": \"" + filterQuotes(description) + "\"");
						jsonDefinition.add("\"type\": \"object\"");
						if (jsonElementsInType != null)
							jsonDefinition.add("\"properties\": {\n" + String.join(",", jsonElementsInType) + "\n}");
						if (jsonRequiredElementsInType != null)
							jsonDefinition
									.add("\"required\" : [" + String.join(", ", jsonRequiredElementsInType) + "]");
						String typeSchema = "\"" + outputTypeName + "\": {\n" + String.join(",", jsonDefinition)
								+ "\n}\n";
						jsonDefinitions.add(typeSchema);
	
						// export element wrapper
						String elementSchema = "\"" + elementName + "\": {\n" + "\"$ref\": \"#/definitions/"
								+ outputTypeName + "\"" + "\n}\n";
						jsonProperties.add(elementSchema);
	
						// add successful response
						openapiResponses.add("\n" + "          \"200\": {\n" + "            \"description\": \""
								+ operationName + " response\",\n" + "            \"schema\": {\n"
								+ "                \"$ref\": \"#/definitions/" + operationName + "Response" + "\"\n"
								+ "            }\n" + "          }");
						// add error response
						if (NiemUmlClass.ERROR_RESPONSE != null)
							openapiResponses.add("\n" + "          \"default\": {\n"
									+ "            \"description\": \"unexpected error\",\n"
									+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/"
									+ NiemUmlClass.ERROR_RESPONSE + "\"\n" + "            }\n" + "          }\n");
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
	
					openapiPaths.add("\n" + "    \"/" + operationName + "\": {" + String.join(",", openapiOperations)
							+ "\n      }");
	
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
								+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_VERSION_PROPERTY) + "\",\n" + "    \"title\": \"" + portName
								+ "\",\n" + "    \"description\": \"" + port.description() + "\",\n"
								+ "    \"termsOfService\": \"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_TERMS_URL_PROPERTY) + "\",\n"
								+ "    \"contact\": {\n" + "      \"name\": \""
								+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_ORGANIZATION_PROPERTY) + "\",\n" + "      \"email\": \""
								+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_EMAIL_PROPERTY) + "\",\n" + "      \"url\": \""
								+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_CONTACT_PROPERTY) + "\"\n" + "    },\n" + "    \"license\": {\n"
								+ "      \"name\": \"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_LICENSE_URL_PROPERTY) + "\",\n"
								+ "      \"url\": \"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_LICENSE_URL_PROPERTY) + "\"\n" + "    }\n"
								+ "  },\n" + "  \"host\": \"host.example.com\",\n" + "  \"basePath\": \"/api\",\n"
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
		}
	}

	/**
	 * return OpenAPI property description of an element with name elementName and
	 * multiplicity
	 */
	String exportOpenApiElementInTypeSchema(String relativePath, String elementName, String multiplicity,
			String localPrefix, boolean isAttribute) {
		String elementName2 = null;
		String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
		String maxOccurs = NiemUmlClass.getMaxOccurs(multiplicity);
		if (isAttribute) {
			if (elementName.equals(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, "@id"))
					|| elementName.equals(NamespaceModel.getPrefixedName(NiemUmlClass.STRUCTURES_PREFIX, "@ref"))) {
				elementName = "xs:NCName";
				elementName2 = "@id";
			} else {
				elementName = NiemUmlClass.filterAttributePrefix(elementName);
				elementName2 = elementName;
			}
		} else
			elementName2 = elementName;
		String elementSchema = "";
		elementSchema += "\"" + elementName2 + "\": {\n";
		if (maxOccurs.equals("1"))
			elementSchema += "\"$ref\": \"" + relativePath + exportJsonPointer(elementName, localPrefix) + "\"\n";
		else {
			elementSchema += "\"items\": {\n" + "\"$ref\": \"" + relativePath
					+ exportJsonPointer(elementName, localPrefix) + "\"\n" + "},\n" + "\n\"minItems\": " + minOccurs
					+ ",\n";
			if (!maxOccurs.equals("unbounded"))
				elementSchema += "\n\"maxItems\": " + maxOccurs + ",\n";
			elementSchema += "\"type\": \"array\"\n";
		}
		elementSchema += "}\n";
		return elementSchema;
	}

	/** filter illegal characters in XML strings */
	String filterQuotes(String string) {
		return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("\r|\n", "");
	}
	/** output a JSON name value pair */
	String getJsonPair(String name, String value) {
		return "\"" + name + "\" : \"" + value + "\"\n";
	}

}
