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
//import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// DOM
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

public class NiemUmlClass {

	static final String STRUCTURES_PREFIX = "structures";
	private static final String NIEM_VERSION_DEFAULT = "4.0";
	// NIEM subset schema generation tool (SSGT)
	private static final String WANTLIST_URI = "http://niem.gov/niem/wantlist/2.2";
	// Niem-tools UML modeling
	private static final String NIEM_PACKAGE = "NIEM";
	private static final String NIEM_REFERENCE_PACKAGE = "NIEMReference";
	private static final String NIEM_SUBSET_PACKAGE = "NIEMSubset";
	private static final String NIEM_EXTENSION_PACKAGE = "NIEMExtension";
	static final String MAPPING_SPREADSHEET_TITLE = "NIEM Mapping";
	private static final String REFERENCE_PREFIX = "^";
	static final String NOTES_PROPERTY = "Notes";
	static final String NILLABLE_PROPERTY = "isNillable";
	static final String PREFIX_PROPERTY = "prefix";
	static final String SUBSTITUTION_PROPERTY = "substitutesFor";
	static final String CODELIST_PROPERTY = "codeList";
	static final String FILE_PATH_PROPERTY = "path";
	static final String MESSAGE_ELEMENT_PROPERTY = "messageElement";
	// File locations
	static final String NIEM_DIR = "niem";
	// Niem stereotype configuration
	static final String NIEM_STEREOTYPE = "niem-profile:niem";
	static final String STEREOTYPE_DELIMITER = ":";
	// private static final String NIEM_STEREOTYPE_LABEL = "niem:niem";
	// NIEM mapping spreadsheet column headings, NIEM profile profile stereotype
	static final String[][] NIEM_STEREOTYPE_MAP = { { "Model Class", "", }, // 0
			{ "Model Attribute", "", }, // 1
			{ "Model Type", "", }, // 2
			{ "Model Multiplicity", "", }, // 3
			{ "Model Definition", "", }, // 4
			{ "NIEM XPath", "XPath" }, // 5
			{ "NIEM Type", "Type" }, // 6
			{ "NIEM Property, " + REFERENCE_PREFIX + "Reference, (Representation)", "Property" }, // 7
			{ "NIEM Base Type", "BaseType" }, // 8
			{ "NIEM Multiplicity", "Multiplicity" }, // 9
			{ "Old XPath", "OldXPath" }, // 10
			{ "Old Multiplicity", "OldMultiplicity" }, // 11
			{ "NIEM Mapping Notes", "Notes" }, // 12
			{ "Code List Code=Definition;", "CodeList" } }; // 13

	// private static final String NIEM_STEREOTYPE_CLASS = NIEM_STEREOTYPE_TYPE +
	// STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[0][1];
	// private static final String NIEM_STEREOTYPE_ATTRIBUTE = NIEM_STEREOTYPE_TYPE
	// + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[1][1];
	// private static final String NIEM_STEREOTYPE_ATTRIBUTETYPE = NIEM_STEREOTYPE_TYPE
	// + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[2][1];
	// private static final String NIEM_STEREOTYPE_MODEL_MULTIPLICITY =
	// NIEM_STEREOTYPE_TYPE + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[3][1];
	// private static final String NIEM_STEREOTYPE_DEFINITION = NIEM_STEREOTYPE_TYPE
	// + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[4][1];
	static final String NIEM_STEREOTYPE_XPATH = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[5][1];
	private static final String NIEM_STEREOTYPE_TYPENAME = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[6][1];
	static final String NIEM_STEREOTYPE_PROPERTY = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[7][1];
	private static final String NIEM_STEREOTYPE_BASE_TYPE = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[8][1];
	private static final String NIEM_STEREOTYPE_MULTIPLICITY = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[9][1];
	// private static final String NIEM_STEREOTYPE_OLD_XPATH = NIEM_STEREOTYPE_TYPE
	// + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[10][1];
	// private static final String NIEM_STEREOTYPE_OLD_MULTIPLICITY =
	// NIEM_STEREOTYPE_TYPE + STEREOTYPE_DELIMITER + NIEM_STEREOTYPE_MAP[11][1];
	private static final String NIEM_STEREOTYPE_NOTES = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[12][1];
	private static final String NIEM_STEREOTYPE_CODE_LIST = NIEM_STEREOTYPE + STEREOTYPE_DELIMITER
			+ NIEM_STEREOTYPE_MAP[13][1];

	private static int importPass;
	//static String importPath;

	private static NiemModel ReferenceModel = new NiemModel();
	private static NiemModel SubsetModel = new NiemModel();
	private static NiemModel ExtensionModel = new NiemModel();

	/** filter whitespace */
	// private static String filterToken(String string) {
	// return string.replaceAll("\\s", "");
	// }

	/** filter illegal characters in XML names */
	// private static String filterNameToken(String string) {
	// return string.replaceAll("[^-._:A-Za-z0-9]", "");
	// }

	/**
	 * @return the NIEM Extension Model as a NiemModel
	 */
	static NiemModel getExtensionModel() {
		return ExtensionModel;
	}

	/**
	 * @param multiplicity
	 * @return XML maxOccurs from multiplicity as a String
	 */
	static String getMaxOccurs(String multiplicity) {
		String maxOccurs = null;
		if (multiplicity.equals(""))
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
		String minOccurs = null;
		if (multiplicity.equals(""))
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
		UmlPackage modelPackage = null;
		anItemKind kind = item.kind();
		if (kind == anItemKind.aClass || kind == anItemKind.aClassInstance)
			modelPackage = (UmlPackage) (item.parent().parent());
		else if (kind == anItemKind.aClassView)
			modelPackage = (UmlPackage) (item.parent());
		if (modelPackage == ReferenceModel.getModelPackage())
			return ReferenceModel;
		else if (modelPackage == SubsetModel.getModelPackage())
			return SubsetModel;
		else if (modelPackage == ExtensionModel.getModelPackage())
			return ExtensionModel;
		Log.trace("getPrefix - error - no prefix for " + item.name());
	
		return null;
	}
	/**
	 * @param p
	 * @return the NIEM stereotype associated with a column in the NIEM mapping spreadsheet as a String
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

	/** hide item from documentation
	 * @param item
	 */
	private static void hideItem(UmlItem item) {
		item.known = false;
		for (UmlItem child : item.children())
			hideItem(child);
	}

	/** hide reference model from documentation
	 * 
	 */
	public static void hideReferenceModel() {
		hideItem(ReferenceModel.getModelPackage());
	}

	/**
	 * @param item
	 * @return true if the UML item is an attribute or attribute group
	 */
	static boolean isAttribute(UmlItem item) {
		return NamespaceModel.getName(item).startsWith(NamespaceModel.ATTRIBUTE_PREFIX);
	}

	/**
	 * @param elementName
	 * @return true if an element exists in reference model 
	 */
	static Boolean isNiemElement(String elementName) {
		if ((elementName == null) || elementName.equals("") || elementName.equals("??")
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
		if ((typeName == null) || typeName.equals("") || typeName.equals("??") || NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(typeName)))
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
	
	/** initialize NiemTools project
	 * 
	 */
	public NiemUmlClass() {
		super();

		// set IEPD configuration defaults
		ConfigurationDialog.init();
	}

	/** caches namespaces and prefixes for external schemas
	 * @param referenceOnly
	 */
	public void cacheModels(boolean referenceOnly) {
		Log.start("cacheModels");
		UmlCom.message("Caching models ...");
		Log.trace("Caching models");
		NamespaceModel.cacheExternalSchemas();
		ReferenceModel.cacheModel();
		if (!referenceOnly) {
			SubsetModel.cacheModel();
			ExtensionModel.cacheModel();
		}
		Log.debug("Done caching models");
		Log.stop("cacheModels");
	}

	/** creates Platform Independent Model (NIEM)
	 * 
	 */
	public void createNIEM() {
		UmlCom.message("Resetting NIEM models");
		Log.trace("Resetting NIEM models");
		UmlPackage pimPackage = null;
		// Find or create NIEM packages
		pimPackage = getPackage(UmlPackage.getProject(), NIEM_PACKAGE, true);
		SubsetModel.setModelPackage(getPackage(pimPackage, NIEM_SUBSET_PACKAGE, true));
		ExtensionModel.setModelPackage(getPackage(pimPackage, NIEM_EXTENSION_PACKAGE, true));
		ReferenceModel.setModelPackage(getPackage(pimPackage, NIEM_REFERENCE_PACKAGE, true));
		ReferenceModel.getModelPackage().set_Stereotype("framework");
	}

	/** creates NIEM subset and extension models
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createSubsetAndExtension() {

		Log.start("createSubsetAndExtension");
		UmlCom.message("Generating NIEM subset and extension models");
		Log.trace("Generating NIEM subset and extension models");

		Iterator<UmlItem> it = (UmlItem.all.iterator());
		Log.start("createSubsetAndExtension - add types");
		// add types to subset and extension
		Log.debug("createSubsetAndExtension: copy subset types and create extension types");
		it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (!item.stereotype().equals(NIEM_STEREOTYPE))
				continue;
			Log.debug("creatSubsetAndExtension: " + item.name());
			String typeName = item.propertyValue(NIEM_STEREOTYPE_TYPENAME).trim();
			String elementName = item.propertyValue(NIEM_STEREOTYPE_PROPERTY).trim();
			String notes = item.propertyValue(NIEM_STEREOTYPE_NOTES).trim();
			String baseTypeName = item.propertyValue(NIEM_STEREOTYPE_BASE_TYPE).trim();
			if (baseTypeName.equals(NiemModel.ABSTRACT_TYPE_NAME))
				continue;
			Log.debug("createSubsetAndExtension: adding type " + typeName + " and base type " + baseTypeName);
			String description = null;
			if (elementName.equals(""))
				if (typeName.endsWith(NiemModel.AUGMENTATION_TYPE_NAME))
					description = "An augmentation type";
				else
					description = item.description().trim();

			// add base type
			if (!baseTypeName.equals(""))
				if (isNiemType(baseTypeName))
					SubsetModel.copyType(baseTypeName);
				else
					ExtensionModel.addType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName, null, null);

			// add type
			if (!typeName.equals(""))
				if (isNiemType(typeName))
					SubsetModel.copyType(typeName);
				else
					ExtensionModel.addType(NamespaceModel.getSchemaURI(typeName), typeName, description, notes);
		}
		Log.stop("createSubsetAndExtension - add types");
		Log.start("createSubsetAndExtension - add base types");
		// relate extension types to base types and attribute groups
		Log.debug("createSubsetAndExtension: copy subset base types and create extension base types");
		it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (!item.stereotype().equals(NIEM_STEREOTYPE))
				continue;
			String typeName = item.propertyValue(NIEM_STEREOTYPE_TYPENAME).trim();
			String elementName = item.propertyValue(NIEM_STEREOTYPE_PROPERTY).trim();
			String baseTypeName = item.propertyValue(NIEM_STEREOTYPE_BASE_TYPE).trim();
			if (typeName.equals("") || isNiemType(typeName))
				continue;
			NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? SubsetModel : ExtensionModel;
			UmlClass type = model.getType(NamespaceModel.getSchemaURI(typeName), typeName);
			if (type == null)
				continue;
			UmlClass baseType = null;
			if (baseTypeName.equals("") || typeName.endsWith(NiemModel.AUGMENTATION_TYPE_NAME))
				baseType = SubsetModel.getAugmentationType();
			else {
				if (!elementName.equals(""))
					continue;
				if (baseTypeName.equals("")) {
					Log.trace("createSubsetAndExtension: base type not defined for type " + typeName
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
		Log.start("createSubsetAndExtension - add elements");
		// Copy subset elements and create extension elements
		Log.debug("createSubsetAndExtension: copy subset elements and create extension elements");
		it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (!item.stereotype().equals(NIEM_STEREOTYPE))
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
				if (elementName.equals(""))
					continue;
				if (elementName.contains("Augmentation") && description.equals(""))
					description = "An augmentation";
				Boolean isNillable = elementName.startsWith(REFERENCE_PREFIX);
				if (isNillable)
					elementName = elementName.substring(1);
				// trace("createSubsetAndExtension: adding element " + elementName + " in type "
				// + typeName);
				if (substitution && !representation && headElement == null)
					headElement = elementName;
				String baseTypeName2 = baseTypeName;
				if (baseTypeName.equals(NiemModel.ABSTRACT_TYPE_NAME) || (substitution && !representation))
					baseTypeName2 = NamespaceModel.getPrefixedName(NiemModel.LOCAL_PREFIX, NiemModel.ABSTRACT_TYPE_NAME);

				// copy NIEM element or add extension element
				NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName2)) ? SubsetModel : ExtensionModel;
				UmlClass baseType = model.getType(NamespaceModel.getSchemaURI(baseTypeName2), baseTypeName2);
				if (baseType == null && !baseTypeName.equals(""))
					Log.trace("createSubsetAndExtension: error - base type " + baseTypeName2 + " not in model with URI "+ NamespaceModel.getSchemaURI(baseTypeName2));
				UmlClassInstance element = (isNiemElement(elementName)) ? SubsetModel.copyElement(elementName)
						: ExtensionModel.addElement(NamespaceModel.getSchemaURI(elementName), elementName, baseType, description, mappingNotes);
				if (element == null)
					continue;

				// copy element in type
				if ((!substitution || !representation) && !typeName.equals("")) {
					NiemModel model2 = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? SubsetModel : ExtensionModel;
					UmlClass type = model2.getType(NamespaceModel.getSchemaURI(typeName), typeName);
					if (type != null)
						if (isNiemType(typeName))
							SubsetModel.copyElementInType(type, element, multiplicity);
						else
							ExtensionModel.addElementInType(type, element, multiplicity);
				}

				if (isNillable)
					element.set_PropertyValue(NILLABLE_PROPERTY, "true");
				if (headElement != null && substitution && representation)
					element.set_PropertyValue(SUBSTITUTION_PROPERTY, headElement);
				if (codeList != null && !codeList.equals("") && (!substitution || !representation))
					element.set_PropertyValue(CODELIST_PROPERTY, codeList);
			}
		}
		Log.stop("createSubsetAndExtension - add elements");
		
		// Sorting
		Log.debug("createSubsetAndExtension: sorting namespaces");
		SubsetModel.getModelPackage().sort();
		ExtensionModel.getModelPackage().sort();
		Log.stop("createSubsetAndExtension");
	}

	/** deletes NIEM mappings
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void deleteMapping() {
		Log.trace("Deleting NIEM Mapping");
		Iterator<UmlItem> it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (item.stereotype().equals(NIEM_STEREOTYPE) && item.kind() != anItemKind.aClassInstance)
				for (int property = 4; property < NIEM_STEREOTYPE_MAP.length; property++)
					item.set_PropertyValue(getNiemProperty(property), "");
		}
	}

	/** deletes NIEM models
	 * @param deleteReferenceModel
	 */
	public void deleteNIEM(boolean deleteReferenceModel) {

		// Find NIEM package
		UmlPackage pimPackage = getPackage(UmlPackage.getProject(), NIEM_PACKAGE, true);
		UmlPackage modelPackage = null;
		
		// delete reference model
		if (deleteReferenceModel) {
			Log.debug("deleteSubset: deleting reference model");
			modelPackage = getPackage(pimPackage, NIEM_REFERENCE_PACKAGE, false);
			ReferenceModel.setModelPackage(modelPackage);
			if (modelPackage != null) {
				modelPackage.deleteIt();
				modelPackage.unload(true, true);
			}
		}

		// delete subset and extension models
		Log.debug("deleteSubset: deleting subset and extension model");
		modelPackage = getPackage(pimPackage, NIEM_SUBSET_PACKAGE, false);
		SubsetModel.setModelPackage(modelPackage);
		if (modelPackage != null) {
			modelPackage.deleteIt();
			modelPackage.unload(true, true);
		}
		
		modelPackage = getPackage(pimPackage, NIEM_EXTENSION_PACKAGE, false);
		ExtensionModel.setModelPackage(modelPackage);
		if (modelPackage != null) {
			modelPackage.deleteIt();
			modelPackage.unload(true, true);
		}
	}

	/** exports a NIEM mapping spreadsheet in CSV format
	 * roundtripping is supported with importCsv()
	 * @param directory
	 * @param filename
	 */
	public void exportCsv(String directory, String filename) {
		Log.start("exportCsv");
		UmlCom.message("Generating NIEM Mapping CSV ...");
		Log.trace("Generating NIEM Mapping CSV");
		NamespaceModel.cacheExternalSchemas();

		UmlItem.directory = directory;
		new CsvWriter().exportCsv(directory, filename);
		Log.stop("exportCsv");
	}

	/** exports a NIEM mapping spreadsheet in HTML format
	 * @param directory
	 * @param filename
	 */
	public void exportHtml(String directory, String filename) {
		Log.start("exportHtml");

		HtmlWriter htmlWriter = new HtmlWriter();

		UmlCom.message ("Generating NIEM Mapping HTML ...");
		Log.trace("Generating NIEM Mapping HTML");
		NamespaceModel.cacheExternalSchemas();
		// cache NIEM namespaces, elements and types
		// cacheModel(referencePackage);
		htmlWriter.exportHtml(directory, filename);
		Log.stop("exportHtml");
	}
	
	/** exports a NIEM IEPD including extension and exchange schema
	 * @param xmlDir
	 * @param wsdlDir
	 * @param jsonDir
	 * @param openapiDir
	 */
	@SuppressWarnings("unchecked")
	public void exportIEPD(String xmlDir, String wsdlDir, String jsonDir, String openapiDir) {

		Log.start("exportIEPD");
		
		XmlWriter xmlWriter = new XmlWriter(xmlDir);
		JsonWriter jsonWriter = new JsonWriter(jsonDir);
		TreeSet<String> jsonDefinitions = new TreeSet<String>();
		TreeSet<String> jsonDefinitions2 = new TreeSet<String>();
		/*
		 * cacheExternalSchemas(); cacheModel(referencePackage);
		 * cacheModel(subsetPackage); cacheModel(extensionPackage);
		 */

		// export code lists for extension elements

		if (xmlDir != null) {
			xmlWriter.exportCodeLists(ExtensionModel);
			xmlWriter.exportCodeLists(SubsetModel);
		}

		try {
			if (xmlDir != null) {
				// export catalog file
				xmlWriter.exportXmlCatalog();
			}
		} catch (Exception e) {
			Log.trace("exportIEPD: error creating XML catalog file " + e.toString());
		}

		// cache list of ports and message elements
		Log.debug("exportIEPD: cache ports and message elements");
		Map<String, UmlClass> ports = new TreeMap<String, UmlClass>();
		Set<String> messages = new TreeSet<String>();
		Set<String> messageNamespaces = new TreeSet<String>();
		messageNamespaces.add(NiemModel.XSD_PREFIX);
		Iterator<UmlItem> it = (UmlClass.classes.iterator());
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (!item.stereotype().equals("interface") && !item.stereotype().equals("niem-profile:interface"))
				continue;
			UmlClass port = (UmlClass) item;
			String portName = port.name();
			ports.put(portName, port);
			Log.debug("exportIEPD: port: " + port.name());
			for (UmlItem item2 : port.children()) {
				if (item2.kind() != anItemKind.anOperation)
					continue;
				UmlOperation operation = (UmlOperation) item2;
				String operationName = operation.name();
				Log.debug("exportIEPD: operation: " + operationName);
				// operations.put(operationName, operation);
				UmlClass outputType = null, inputType = null;
				UmlParameter[] params = operation.params();
				if (params != null)
					for (UmlParameter param : params) {
						// ignore RESTful path, query, header or cookie parameters
						if (!param.name.equals("") && !param.name.equals("body"))
							continue;
						Log.debug("exportIEPD: param " + param.name);
						try {
							UmlTypeSpec inputType2 = param.type;
							if (inputType2 != null)
								inputType = inputType2.type;
							// String mult = param.multiplicity;
						} catch (Exception e) {
							Log.trace("exportIEPD: error - no input message for " + operationName);
						}
						if (inputType == null || !inputType.stereotype().equals(NIEM_STEREOTYPE))
							continue;
						String inputMessage = inputType.propertyValue(NIEM_STEREOTYPE_XPATH);
						if (inputMessage == null || inputMessage.equals(""))
							continue;
						Log.debug("exportIEPD: input Message: " + inputMessage + " from operation " + operationName);
						messageNamespaces.add(NamespaceModel.getPrefix(inputMessage));
						NiemModel model = getModel(NiemModel.getURI(NamespaceModel.getSchemaURI(inputMessage), inputMessage));
						UmlClassInstance element = model.getElementByURI(NiemModel.getURI(NamespaceModel.getSchemaURI(inputMessage), inputMessage));
						if (element != null)
							element.set_PropertyValue(MESSAGE_ELEMENT_PROPERTY, operationName);
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
					Log.trace("exportIEPD: error - no output message for " + operationName + " " + e.toString());
				}
				if (outputType != null && outputType.stereotype().equals(NIEM_STEREOTYPE))
					outputMessage = outputType.propertyValue(NIEM_STEREOTYPE_XPATH);
				if (outputMessage == null || outputMessage.equals(""))
					continue;
				Log.debug("exportIEPD: output Message: " + outputMessage + " from operation " + operationName);
				String outputPrefix = NamespaceModel.getPrefix(outputMessage);
				if (NamespaceModel.isNiemPrefix(outputPrefix)) {
					messageNamespaces.add(outputPrefix);
					NiemModel model = getModel(NiemModel.getURI(NamespaceModel.getSchemaURI(outputMessage), outputMessage));
					UmlClassInstance element = model.getElementByURI(NiemModel.getURI(NamespaceModel.getSchemaURI(outputMessage), outputMessage));
					if (element != null)
						element.set_PropertyValue(MESSAGE_ELEMENT_PROPERTY, operationName);
				}
			}
		}

		if (jsonDir != null)
			jsonDefinitions.addAll(SubsetModel.exportSchemas(null, jsonDir));
		jsonDefinitions.addAll(ExtensionModel.exportSchemas(xmlDir, jsonDir));
		
		// swagger code generation tools do not support relative references, rename them to local references
		Iterator<String> it2 = jsonDefinitions.iterator();
		while (it2.hasNext()) {
			String definition = it2.next();
			String definition2 = definition.replaceAll("(\"\\$ref\": \")(.*)#/(.*\")","$1#/$3");
			Log.debug("exportIEPD: definition " + definition2);
			if (definition2 != null && jsonDefinitions2 != null)
				jsonDefinitions2.add(definition2);
		}
			
		if (xmlDir != null)
			try {
				xmlWriter.exportMpdCatalog(messages);
				if (wsdlDir != null)
					xmlWriter.exportWsdl(wsdlDir, ports, messageNamespaces);
			} catch (Exception e) {
				Log.trace("exportIEPD: error exporting MPD catalog or WSDL " + e.toString());
			}

		if (jsonDir != null)
			try {
				if (openapiDir != null)
					jsonWriter.exportOpenApi(openapiDir, ports, messageNamespaces, jsonDefinitions2);
			} catch (Exception e) {
				Log.trace("exportIEPD: error exporting OpenAPI files " + e.toString());
			}
		Log.stop("exportIEPD");
	}

	/** exports a NIEM wantlist for import into Subset Schema Generator Tool (SSGT)
	 * @param dir
	 * @param filename
	 */
	public void exportWantlist(String dir, String filename) {

		Log.start("exportWantlist");
		UmlCom.message("Generating NIEM Wantlist ...");
		Log.trace("Generating NIEM Wantlist");
		XmlWriter xmlWriter = new XmlWriter(dir);

		// createSubset();
		NamespaceModel.cacheExternalSchemas();

		UmlItem.directory = dir;
		try {
			// Export schema
			Log.debug("exportWantlist: create header");
			File file = Paths.get(dir, filename).toFile();
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			FileWriter fw = new FileWriter(file);
			fw.write(XmlWriter.XML_HEADER);
			fw.write(XmlWriter.XML_ATTRIBUTION);
			fw.write("<w:WantList w:release=\"" + getNiemVersion()
			+ "\" w:product=\"NIEM\" w:nillableDefault=\"true\" ");
			for (UmlItem item : SubsetModel.getModelPackage().children())
				if (item.kind() == anItemKind.aClassView) {
					String prefix = item.propertyValue(PREFIX_PROPERTY);
					String schemaURI = NamespaceModel.getSchemaURIForPrefix(prefix);
					if (!prefix.equals(NiemModel.LOCAL_PREFIX) && (!prefix.equals(NiemModel.XSD_PREFIX)))
						xmlWriter.writeXmlNs(fw, prefix, schemaURI);
				}
			xmlWriter.writeXmlNs(fw, "w", WANTLIST_URI);
			fw.write(">");

			// export elements
			for (UmlItem item : SubsetModel.getModelPackage().children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView classView = (UmlClassView) item;
					String prefix = classView.propertyValue(PREFIX_PROPERTY);
					String anyElement = NamespaceModel.getPrefixedName(NiemModel.XSD_PREFIX, NiemModel.ANY_ELEMENT_NAME);
					if (prefix.equals(NiemModel.LOCAL_PREFIX) || prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(STRUCTURES_PREFIX))
						continue;
					for (UmlItem item2 : classView.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance element = (UmlClassInstance) item2;
							String elementName = NamespaceModel.getPrefixedName(element);
							if (elementName.equals(anyElement))
								continue;
							if (isAttribute(element)) {
								elementName = NamespaceModel.getPrefixedAttributeName(NamespaceModel.getPrefix(elementName), elementName);
								Log.debug("exportWantlist: export attribute " + elementName);
								// fw.write("<w:Attribute w:name=\"" + elementName + "\"/>\n");
								continue;
							}
							Log.debug("exportWantlist: export element " + elementName);
							String isNillable = element.propertyValue(NILLABLE_PROPERTY);
							if (isNillable == null)
								isNillable = "false";
							fw.write("<w:Element w:name=\"" + elementName + "\" w:isReference=\"false\" w:nillable=\""
									+ isNillable + "\"/>\n");
						}
				}

			// export types
			for (UmlItem item : SubsetModel.getModelPackage().children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView classView = (UmlClassView) item;
					String prefix = classView.propertyValue(PREFIX_PROPERTY);
					if (prefix.equals(NiemModel.LOCAL_PREFIX) || prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(STRUCTURES_PREFIX))
						continue;
					for (UmlItem item2 : classView.children())
						if (item2.kind() == anItemKind.aClass) {
							UmlClass type = (UmlClass) item2;
							String typeName = NamespaceModel.getPrefixedName(type);
							Log.debug("exportWantlist: export type " + typeName);

							// do not export structures:AugmentationType
							// if (type == SubsetModel.augmentationType)
							// continue;

							// attribute groups are not supported in wantlists
							if (isAttribute(type))
								continue;

							fw.write("<w:Type w:name=\"" + typeName + "\" w:isRequested=\"true\">\n");

							for (UmlItem item3 : type.children())
								if (item3.kind() == anItemKind.anAttribute) {
									UmlAttribute attribute = (UmlAttribute) item3;
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

									if (isAttribute(attribute)) {
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

							// export enumerations
							
							String codeList = type.propertyValue(CODELIST_PROPERTY); 
							if (codeList != null) { 
								// trace("exportWantlist: exporting enumerations for " + getPrefixedName(type));
								if (codeList.trim().contains(NiemModel.CODELIST_DELIMITER)) {
									String[] codes = codeList.split(NiemModel.CODELIST_DELIMITER);
									for (String code : codes) {
										String[] pairs = code.split(NiemModel.CODELIST_DEFINITION_DELIMITER); String
										value = pairs[0].trim();
										if (!value.equals(""))
											fw.write("<w:Facet w:facet=\"enumeration\" w:value=\"" + ReferenceModel.filterEnum(value) + "\"/>"); 
									}
								}
							}
							fw.write("</w:Type>");
						}
				}

			fw.write("</w:WantList>");
			fw.close();

		} catch (IOException e) {
			Log.trace("exportWantlist: IO exception: " + e.toString());
		}
		Log.stop("exportWantlist");
	}

	/**
	 * @return NIEM version as a String
	 */
	private String getNiemVersion() {
		String niemVersion = NIEM_VERSION_DEFAULT;

		String schemaURI = NamespaceModel.getSchemaURIForPrefix("nc");
		// UmlCom.trace("NIEM URI: " + schemaURI);
		Matcher mat = Pattern.compile(".*niem-core/(.*)/").matcher(schemaURI);
		if (mat.find())
			niemVersion = mat.group(1);
		Log.trace("NIEM version: " + niemVersion);
		return niemVersion;
	}

	/** get child package with name packageName in parentPackage; if it doesn't exist and create is true, create it
	 * @param parentPackage
	 * @param packageName
	 * @param create
	 * @return return child package as a UmlPackage
	 */
	private UmlPackage getPackage(UmlPackage parentPackage, String packageName, boolean create) {
		for (UmlItem item : parentPackage.children()) {
			if (item.name().equals(packageName))
				if ((item.kind() == anItemKind.aPackage))
					return (UmlPackage) item;
		}
		if (create) {
			Log.debug("getPackage: Creating " + packageName);
			return UmlPackage.create(parentPackage, packageName);
		}
		return null;
	}

	/** import NIEM mapping spreadsheet in CSV format
	 * @param filename
	 */
	public void importCsv(String filename) {

		Log.trace("Importing NIEM Mapping");
		NamespaceModel.cacheExternalSchemas();
		new CsvReader().importCsv(filename);

	}

	/** import NIEM reference model into HashMaps to support validation of NIEM elements and types
	 * @param dir
	 * @param includeEnums
	 * @throws IOException
	 */
	public void importSchemaDir(String dir, Boolean includeEnums) throws IOException {

		UmlCom.message("Importing NIEM schema");

		// Configure DOM
		Path path = FileSystems.getDefault().getPath(dir);
		String importPath = path.toString();

		int passes = (includeEnums) ? 4 : 3;

		// Walk directory to import in passes (0: types, 1: elements, 2:
		// elements in types, 3: enumerations
		for (importPass = 0; importPass < passes; importPass++) {
			switch (importPass) {
			case 0:
				Log.trace("\nImporting types");
				break;
			case 1:
				Log.trace("\nImporting elements");
				break;
			case 2:
				Log.trace("\nImporting elements and attributes in types");
				break;
			}
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					// Configure DOM
					DocumentBuilder db = null;
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					docBuilderFactory.setNamespaceAware(true);
					try {
						db = docBuilderFactory.newDocumentBuilder();
					} catch (ParserConfigurationException e) {
						Log.trace("importSchemaDir: error configuring parser " + e.toString());
					}

					String filename = file.toString();
					String filepath1 = filename.replaceFirst(java.util.regex.Matcher.quoteReplacement(importPath), "");
					String filepath = filepath1.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), "/");
					if (filename.endsWith(XmlWriter.XSD_FILE_TYPE)) {
						Log.trace("Importing " + filepath);
						switch (importPass) {
						case 0:
							Namespace ns = ReferenceModel.importTypes(db, filename);
							if (ns != null) {
								UmlClassView classView = ns.getReferenceClassView();
								if (classView != null)
									classView.set_PropertyValue(FILE_PATH_PROPERTY, NIEM_DIR + filepath);
							}
							break;
						case 1:
							ReferenceModel.importElements(db, filename);
							break;
						case 2:
							ReferenceModel.importElementsInTypes(db, filename);
							break;
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		// Sorting
		Log.trace("Sorting namespaces");
		ReferenceModel.getModelPackage().sort();

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
	
	public void addStereotype(UmlItem item) {		
		if (item.kind() == anItemKind.aClass || 
			item.kind() == anItemKind.aClassInstance ||
			item.kind() == anItemKind.anAttribute) {
				item.set_Stereotype(NIEM_STEREOTYPE);
				item.applyStereotype();
		}
		else if (item.kind() == anItemKind.aRelation) {
			UmlRelation r = (UmlRelation)item;
			if (r.relationKind() != aRelationKind.aGeneralisation) {
				item.set_Stereotype(NIEM_STEREOTYPE);
				item.applyStereotype();
			}
		}
		UmlItem[] ch = item.children();
		for(UmlItem c : ch)
			addStereotype(c);
	}
	
	public void removeStereotype(UmlItem item) {
		if (item.kind() == anItemKind.aClass || 
				item.kind() == anItemKind.aClassInstance ||
				item.kind() == anItemKind.anAttribute) {
					item.set_Stereotype(null);
					item.applyStereotype();
				}
			else if (item.kind() == anItemKind.aRelation) {
				UmlRelation r = (UmlRelation)item;
				if (r.relationKind() != aRelationKind.aGeneralisation) {
					item.set_Stereotype(null);
					item.applyStereotype();
				}
			}
			UmlItem[] ch = item.children();
			for(UmlItem c : ch)
				removeStereotype(c);
	}	
}
