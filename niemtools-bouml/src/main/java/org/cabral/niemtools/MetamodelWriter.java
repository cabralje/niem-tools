package com.infotrack.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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

//import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class MetamodelWriter {

	private static final String METAMODEL_URI = "http://reference.niem.gov/specification/metamodel/5.0alpha1";
	private static final String METAMODEL_PREFIX = "mm";
	private static final String METAMODEL_FILE_TYPE = ".xml";

	// limit enumerations 
	// private static final int MAX_ENUMS = 10000;
	private static final int MAX_ENUMS = 20;

	@SuppressWarnings("unused")
	private String directory;
	private Set<String> tagIds = new HashSet<String>();
	private List<UmlClassInstance> substitutionElements = new ArrayList<UmlClassInstance>();

	/**
	 * @param initialDirectory
	 */
	public MetamodelWriter(String initialDirectory) {
		super();
		directory = initialDirectory;
	}

	/**
	 * @param metamodelDir
	 * @param messages
	 */
	void exportMetamodel(String metamodelDir, Map<String, UmlClassInstance> messages) throws IOException {

		Log.start("exportMetamodel");
		UmlCom.message("Generating NIEM meta models");
		Log.trace("Generating NIEM meta models");

		// iterate on each message
		for (Map.Entry<String, UmlClassInstance> entry : messages.entrySet()) {
			String messageName = NamespaceModel.getName(entry.getKey());
			try {
				// reset tagIds
				tagIds.clear();

				// open file
				String path1 = messageName + METAMODEL_FILE_TYPE;
				Path p1 = Paths.get(metamodelDir, path1);
				File file = p1.toFile();
				File parentFile = file.getParentFile();
				if (parentFile != null)
					parentFile.mkdirs();
				FileWriter fw = new FileWriter(file);

				// write header with namespace definitions
				fw.write(XmlWriter.XML_HEADER);
				fw.write("<Model ");
				XmlWriter.writeXmlNs(fw,"",METAMODEL_URI);
				XmlWriter.writeXmlNs(fw,METAMODEL_PREFIX,METAMODEL_URI);
				XmlWriter.writeXmlNs(fw,XmlWriter.XSI_PREFIX,XmlWriter.XSI_URI);
				XmlWriter.writeXmlNs(fw,NiemModel.STRUCTURES_PREFIX,NiemModel.STRUCTURES_URI);
				fw.write(">");

				// message metamodel
				fw.write(exportMetamodelProperty(entry.getValue()));

				// substitution element metamodels
				int e = 0;
				while (e < substitutionElements.size())
					fw.write(exportMetamodelProperty(substitutionElements.get(e++)));

				// close file
				fw.write("</Model>");
				fw.close();	

			} catch (Exception e) {
				Log.trace("exportMetamodel - error exporting message" + messageName + " " + e.toString());
			}
		}
		Log.stop("exportMetamodel");
	}

	/**
	 * @param type
	 * @param value
	 * @return Metamodel any data type definition
	 */
	String exportMetamodelAnyDatatype(UmlClass type, String value) {

		String typeName = NamespaceModel.getName(type);
		String tagName = "";
		switch (typeName) {
		case "nonNegativeInteger":
			tagName = "NonNegativeValue";
			break;
		case "positiveInteger":
			tagName = "PositiveValue";
			break;
		default:
			tagName = "StringValue";
		}
		return tag(tagName,value);
	}

	/**
	 * @param type
	 * @return Metamodel class definition
	 */
	String exportMetamodelClass(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		if (tagIds.contains(id))
			return tagRef("Class",id);

		tagIds.add(id);
		String typeName = NamespaceModel.getName(type);
		Log.debug("exportMetamodelClass: exporting class " + typeName);
		String classMetamodel = tagStart("Class", id, true) + ">"
				+ exportMetamodelComponent(type);
		UmlClass baseType = NiemModel.getBaseType(type);
		if (baseType == null)
			baseType = NiemUmlClass.getSubsetModel().getObjectType();

		String prefix = NamespaceModel.getPrefix(type);
		String baseTypePrefix = NamespaceModel.getPrefix(baseType);
		String baseTypeCodeList = baseType.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		UmlAttribute augmentationPoint = null;
		if (baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType()) {
			// abstract
			classMetamodel += tag("AbstractIndicator","true");
		} else if (!prefix.equals(NiemModel.STRUCTURES_PREFIX) && !prefix.equals(NiemModel.XSD_PREFIX)) {
			String extensionMetamodel = "";
			// if (NiemUmlClass.getModel(NiemModel.getURI(type)) == NiemUmlClass.getExtensionModel()) {
			if (baseTypeCodeList != null || (baseTypePrefix != null && baseTypePrefix.equals(NiemModel.XSD_PREFIX))) {
				// simple type or enumeration - HasValue
				extensionMetamodel = exportMetamodelClass(NiemUmlClass.getSubsetModel().getObjectType());
				extensionMetamodel += tag("HasValue", exportMetamodelDatatype(baseType));
				classMetamodel += tag("ExtensionOf",extensionMetamodel);
				classMetamodel += tag("ContentStyleCode","HasValue");
			} else {
				// complex type
				int sequenceID = 1;
				extensionMetamodel = exportMetamodelClass(baseType);

				for (UmlItem item : type.children())
					if (item.kind() == anItemKind.anAttribute) {
						UmlAttribute attribute = (UmlAttribute) item;
						NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
						UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));

						if (element != null) {
							String multiplicity = attribute.multiplicity();
							String elementMetamodel = "";

							// attribute - HasDataProperty
							if (NiemUmlClass.isAttribute(element)) {
								elementMetamodel = exportMetamodelComponent(element);
								if (baseType != null) 
									elementMetamodel += exportMetamodelDatatype(baseType);
								elementMetamodel += tag("DataProperty", elementMetamodel);
								elementMetamodel = "<HasDataProperty "
										+ exportMetamodelMultiplicity(multiplicity)
										+ ">" + elementMetamodel + "</HasDataProperty>";
								classMetamodel += elementMetamodel;
							} else {
								if (NamespaceModel.getPrefixedName(element).endsWith(XmlWriter.AUGMENTATION_POINT_NAME))
									// augmentation point
									augmentationPoint = attribute;
								else {
									// element - HasObjectProperty
									elementMetamodel = "<HasObjectProperty " + METAMODEL_PREFIX + ":sequenceID=\"" + sequenceID++ + "\" " 
											+ exportMetamodelMultiplicity(multiplicity)
											+ ">" + exportMetamodelProperty(element) + "</HasObjectProperty>";
								}
							}
							extensionMetamodel += elementMetamodel;
						}
					}
				classMetamodel += tag("ExtensionOf",extensionMetamodel);
				classMetamodel += tag("ContentStyleCode","HasObjectProperty");
			}

			// add augmentations
			if (augmentationPoint != null) {
				NiemModel model = NiemUmlClass.getModel(NiemModel.getURI(augmentationPoint));
				UmlClassInstance element = model.getElementByURI(NiemModel.getURI(augmentationPoint));
				classMetamodel += "<ClassAugmentationPoint "
						+ exportMetamodelMultiplicity(augmentationPoint.multiplicity())
						+ ">" + exportMetamodelProperty(element) + "</ClassAugmentationPoint>";
			}
		}

		classMetamodel += "</Class>\n";
		return classMetamodel;
	}

	/**
	 * @param baseType
	 * @param codeList
	 * @return Metamodel component definition
	 */
	String exportMetamodelComponent(UmlItem item) {

		String componentMetamodel = 
				tag("Name", NamespaceModel.getName(item))
				+ exportMetamodelNamespace(NamespaceModel.getPrefix(item));
		String description = item.description();
		if (description != null && !description.equals(""))
			componentMetamodel += tag("DefinitionText",description);

		return componentMetamodel;
	}

	/**
	 * @param type
	 * @return Metamodel data type definition
	 */
	String exportMetamodelDatatype(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		if (tagIds.contains(id))
			return tagRef("Datatype",id);

		tagIds.add(id);
		String datatypeMetamodel = tagStart("Datatype", id, true) + ">"
				+ exportMetamodelComponent(type);
		String codeList = type.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		if (codeList != null && codeList.contains(NiemModel.CODELIST_DELIMITER)) {
			// add codeList
			UmlClass baseType = NiemModel.getBaseType(type);
			String restrictionMetamodel = (baseType == null) ? tag("Datatype","") : exportMetamodelDatatype(baseType);
			int codeValues = 0;
			for (String code : codeList.split(NiemModel.CODELIST_DELIMITER)) {
				if (code.equals(""))
					continue;
				String[] codeParams = code.replace("&", "&amp;").split(NiemModel.CODELIST_DEFINITION_DELIMITER);
				String codeValue = codeParams[0].trim();
				if (codeValue.equals(""))
					continue;
				String codeDescription = (codeParams.length > 1 && !codeParams[1].equals("")) ? codeParams[1].trim()
						: "";
				String enumeration = exportMetamodelAnyDatatype(type, codeValue);
				if (codeDescription != null && !codeDescription.equals(""))
					enumeration += tag("DefinitionText", codeDescription);
				enumeration = tag("Enumeration", enumeration);
				restrictionMetamodel += enumeration;
				if (++codeValues > MAX_ENUMS) {
					Log.trace("exportMetamodelDatatype: warning - truncated enumerations in class " + id );
					break;
				}
			}					
			datatypeMetamodel += tag("RestrictionOf", restrictionMetamodel);
		}

		datatypeMetamodel += "</Datatype>";
		return datatypeMetamodel;
	}

	/**
	 * @param prefix
	 * @return Metamodel multiplicity definition
	 */
	String exportMetamodelMultiplicity(String multiplicity) {
		return METAMODEL_PREFIX + ":minOccursQuantity=\"" +  NiemUmlClass.getMinOccurs(multiplicity) + "\" "
				+ METAMODEL_PREFIX + ":maxOccursQuantity=\"" + NiemUmlClass.getMaxOccurs(multiplicity) + "\""; 
	}

	/**
	 * @param prefix
	 * @return Metamodel namespace definition
	 */
	String exportMetamodelNamespace(String prefix) {
		if (tagIds.contains(prefix))
			return tagRef("Namespace",prefix);
		tagIds.add(prefix);
		return tagStart("Namespace", prefix, true) + ">"
		+ tag("NamespaceURI",NamespaceModel.getSchemaURIForPrefix(prefix)) 
		+ tag("NamespacePrefixName",prefix)
		+ "</Namespace>";
	}

	/**
	 * @param element
	 * @return Metamodel property definition
	 */
	String exportMetamodelProperty(UmlClassInstance element) {		
		String id = NamespaceModel.getPrefixedName(element);
		if (tagIds.contains(id))
			return tagRef("ObjectProperty",id);

		tagIds.add(id);
		String elementName = NamespaceModel.getName(element);
		Log.debug("exportMetamodelProperty: exporting property " + elementName);
		String elementMetamodel = tagStart("ObjectProperty", id, true) + ">"
				+ exportMetamodelComponent(element);

		//	if (NiemUmlClass.getModel(NiemModel.getURI(element)) == NiemUmlClass.getExtensionModel()) {
		// add substitution groups
		String head = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
		if (head != null) { // substitutionGroups
			String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
			UmlClassInstance headElement = NiemUmlClass.getModel(uri).getElementByURI(uri);
			elementMetamodel += tag("SubPropertyOf", exportMetamodelProperty(headElement));
		}
		UmlClass baseType = NiemModel.getBaseType(element);
		if (baseType == null || baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType())
			// abstract
			elementMetamodel += tag("AbstractIndicator","true");
		else
			// class
			elementMetamodel += exportMetamodelClass(baseType);

		// add substitution elements
		List<UmlClassInstance> enlist = (NiemModel.Substitutions.get(id));
		if (enlist != null)
			for (UmlClassInstance element2 : enlist)
				substitutionElements.add(element2);
		//		}
		elementMetamodel += "</ObjectProperty>\n";
		return elementMetamodel;

	}

	/**
	 * @param tag
	 * @param content
	 * @return XML tagged content
	 */
	String tag(String tag, String content) {
		return "<" + tag + ">" + content + "</" + tag + ">";
	}

	/**
	 * @param tag
	 * @param firstOccurence
	 * @param content
	 * @return XML tagged content with ID
	 */
	String tagId(String tag, String tagId, boolean firstOccurence, String content) {
		String t = tagStart(tag, tagId, firstOccurence);
		if (!firstOccurence || content.isEmpty())
			return t + "/>";
		else
			return t + ">" + content + "</" + tag + ">";
	}

	/**
	 * @param tag
	 * @param content
	 * @return XML reference tag
	 */
	String tagRef(String tag, String tagId) {
		return tagId(tag, tagId, false, "");
	}

	/**
	 * @param tag
	 * @param firstOccurence
	 * @return XML start tag
	 */
	String tagStart(String tag, String tagId, boolean firstOccurence) {
		return "<" + tag + " " + NiemModel.STRUCTURES_PREFIX + ":" + (firstOccurence ? "id" : "ref") + "=\"" + tagId.replace(":", ".") + "\"" + (firstOccurence ? "" : " " + XmlWriter.XSI_PREFIX + ":nil = \"true\"");
	}
}
