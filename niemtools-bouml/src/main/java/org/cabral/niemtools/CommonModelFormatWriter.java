package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlClassView;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class CommonModelFormatWriter {

	private static final String CMF_URI = "https://docs.oasis-open.org/niemopen/ns/specification/cmf/1.0/";
	private static final String CMF_PREFIX = "cmf";
	private static final String CMF_FILE_TYPE = ".cmf";
	private static final String CMF_FILE = "model";

	// limit enumerations 
	// private static final int MAX_ENUMS = 10000;
	private static final int MAX_ENUMS = 20;
    //@SuppressWarnings("unused")

	private final Set<String> tagIds = new HashSet<>();
	private final Set<String> dataTypeIds = new HashSet<>();
	private final Set<String> classIds = new HashSet<>();
//	private List<UmlClassInstance> substitutionElements = new ArrayList<UmlClassInstance>();

	/**
	 * @param initialDirectory
	 */
	public CommonModelFormatWriter(String initialDirectory) {
		super();
	}


/** exports NIEM extension and exchange schema 
	 * @param xmlDir
	 * @param jsonDir
	 * @return JSON definitions as a Set
	 */
	LinkedHashSet<String> exportCMFModel() {
	
		LinkedHashSet<String> cmfModel = new LinkedHashSet<>();
		TreeSet<String> cmfNamespaces = new TreeSet<>();
		TreeSet<String> cmfClasses = new TreeSet<>();
		TreeSet<String> cmfProperties = new TreeSet<>();

		List<UmlItem> items = new ArrayList<>();
		items.addAll(Arrays.asList(NiemUmlClass.getSubsetModel().getModelPackage().children()));
		items.addAll(Arrays.asList(NiemUmlClass.getExtensionModel().getModelPackage().children()));

		// export subset and extension namespaces and types
		for (UmlItem item : items)
			if (item != null && item.kind() == anItemKind.aClassView) {
				String prefix = NamespaceModel.getPrefix(item);
				if (prefix.equals(NiemModel.STRUCTURES_PREFIX) || (prefix.equals(NiemModel.LOCAL_PREFIX)) || (prefix.equals(NiemModel.PROXY_PREFIX)))
					continue;
				cmfNamespaces.add(exportCMFNamespace((UmlClassView)item));
				for (UmlItem item2 : item.children())
					// export subset and extension classes
					if (item2 != null && item2.kind() == anItemKind.aClass) {
						Log.trace("exportCMFModel: exporting class " + NamespaceModel.getName(item2));
						cmfClasses.add(exportCMFClass((UmlClass) item2));
					}
			}

		// export subset and extension properties
		for (UmlItem item : items)
			if (item != null && item.kind() == anItemKind.aClassView) {
				String prefix = NamespaceModel.getPrefix(item);
				if (prefix.equals(NiemModel.STRUCTURES_PREFIX) || (prefix.equals(NiemModel.LOCAL_PREFIX)) || (prefix.equals(NiemModel.PROXY_PREFIX)))
					continue;
				for (UmlItem item2 : item.children())
					// export subset and extension properties
					if (item2 != null && item2.kind() == anItemKind.aClassInstance) {
						cmfProperties.add(exportCMFProperty((UmlClassInstance) item2));
					}
				}
		cmfModel.addAll(cmfNamespaces);
		cmfModel.addAll(cmfProperties);
		cmfModel.addAll(cmfClasses);	

		return cmfModel;
	}

	/**
	 * @param CMFDir
	 * @param messages
	 */
	void exportCMF(String CMFDir) throws IOException {

		Log.start("exportCMF");
		UmlCom.message("Generating NIEM meta models");

		LinkedHashSet<String> modelCmf = exportCMFModel();

		try {
			// reset tagIds
			tagIds.clear();

			// open file
			String path1 = CMF_FILE + CMF_FILE_TYPE;
			Path p1 = Paths.get(CMFDir, path1);
			File file = p1.toFile();
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
                    // write header with namespace definitions
                    try (FileWriter fw = new FileWriter(file)) {
                        // write header with namespace definitions
                        fw.write(XmlWriter.XML_HEADER);
                        fw.write("<Model ");
                        XmlWriter.writeXmlNs(fw,"",CMF_URI);
                        XmlWriter.writeXmlNs(fw,CMF_PREFIX,CMF_URI);
                        XmlWriter.writeXmlNs(fw,XmlWriter.XSI_PREFIX,XmlWriter.XSI_URI);
                        XmlWriter.writeXmlNs(fw,NiemModel.STRUCTURES_PREFIX,NiemModel.STRUCTURES_URI);
						fw.write(" xml:lang=\"" + XmlWriter.XML_LANG + "\">");
                        
                        // message CMF
                        fw.write(" " + String.join("\n",modelCmf));
                        
                        // close file
                        fw.write("</Model>");
                    }	

		} catch (IOException e) {
			Log.trace("exportCMF - error exporting CMF model " + e.toString());
		}
		Log.stop("exportCMF");
	}

	/**
	 * @param type
	 * @return CMF class definition
	 */
	// TODO exportCMFClass: handle facets other than enumerations
	
	String exportCMFClass(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		String classCmf;

		if (tagIds.contains(id))
			return tagRef("Class",id);
		tagIds.add(id);

		String typeName = NamespaceModel.getName(type);
		Log.debug("exportCMFClass: exporting class " + typeName);

		UmlClass baseType = getBaseType(type);
		if (baseType == null)
			baseType = NiemUmlClass.getSubsetModel().getObjectType();

		String prefix = NamespaceModel.getPrefix(type);
            //UmlAttribute augmentationPoint = null;

		// abstract class
		if (baseType != null && 
		(baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType())) {
			classIds.add(id);
			Log.trace("exportCMFClass: exported abstract class " + typeName);
			return tagStart("Class", id, true) + ">" 
				+ exportCMFComponent(type) 
				+ tag("AbstractIndicator","true")
				+ "</Class>";
		}

		// datatype
		if (prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(NiemModel.PROXY_PREFIX)) {
			dataTypeIds.add(id);
			Log.trace("exportCMFClass: exported datatype " + typeName);
			return exportCMFDatatype(type);
		}

		// restriction
		if (!isClass(type)) {
			dataTypeIds.add(id);
			Log.trace("exportCMFClass: exported restriction class " + typeName);
			return exportCMFRestrictionType(type);
		}

		// complex class
		//classIds.add(id);
		String childrenCMF = "";
		for (UmlItem item : type.children())
			if (item != null &&  item.kind() == anItemKind.anAttribute) {
				UmlAttribute attribute = (UmlAttribute) item;
				NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
				UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));
				if (element != null) {
					String elementName = NamespaceModel.getPrefixedName(element);
					if (elementName.endsWith(XmlWriter.AUGMENTATION_POINT_NAME))
						continue;
					if (NamespaceModel.isAttribute(elementName))
						continue;
					String multiplicity = attribute.multiplicity();
					UmlClass elementBaseType = getBaseType(element);

					childrenCMF += "<ChildPropertyAssociation>";
					if (!isClass(elementBaseType))
						childrenCMF += tagRef("DataProperty",elementName);
					else
						childrenCMF += tagRef("ObjectProperty",elementName);
					childrenCMF += exportCMFMultiplicity(multiplicity)
								+ "</ChildPropertyAssociation>";
				}
			}


		Log.trace("exportCMFClass: exported complex class " + typeName);
		classCmf = tagStart("Class", id, true) + ">" 
			+ exportCMFComponent(type);
		String baseTypePrefix = NamespaceModel.getPrefix(baseType);
		if (!baseTypePrefix.equals(NiemModel.STRUCTURES_PREFIX))
			classCmf += tagRef("SubClassOf", NamespaceModel.getPrefixedName(baseType));
		classCmf += childrenCMF 
			+ "</Class>";

		return classCmf;
	}

	/**
	 * @param baseType
	 * @param codeList
	 * @return CMF component definition
	 */
	String exportCMFComponent(UmlItem item) {
		String name = NamespaceModel.getName(item);
		if (NamespaceModel.isAttribute(name))
			name = NamespaceModel.filterAttributePrefix(name);
		String componentCMF = 
				tag("Name", name)
				+ tagRef("Namespace", NamespaceModel.getPrefix(item));
		String description = item.description();
		if (description != null && !description.equals(""))
			componentCMF += tag("DocumentationText",description);

		return componentCMF;
	}

		/**
	 * @param type
	 * @return CMF data type definition
	 */
	String exportCMFDatatype(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		//dataTypeIds.add(id);
		String typeName = NamespaceModel.getName(type);
		Log.debug("exportCMFClass: exported data type class " + typeName);
		return tagStart("Datatype", id, true) + ">" 
				+ exportCMFComponent(type) + "</Datatype>";
	}

	/**
	 * @param type
	 * @return CMF restriction type definition
	 */
	String exportCMFRestrictionType(UmlClass type) {

 		String id = NamespaceModel.getPrefixedName(type);
		 if (id.endsWith(XmlWriter.SIMPLE_TYPE_NAME)) {
			Log.trace("exportCMFRestrictionType: skipping simple type " + id);
		 	return "";
		 }
		String typeName = NamespaceModel.getName(type);

		String restrictionCMF = tagStart("Restriction", id, true) + ">"
				+ exportCMFComponent(type);
		UmlClass baseType = getBaseType(type);
		if (baseType == null)
			Log.trace("exportCMFClass: unable to find base type for " + typeName);
		else
			restrictionCMF += tagRef("RestrictionBase", NamespaceModel.getPrefixedName(baseType));
		String codeList = NiemUmlClass.getCodeList(type);
		if (codeList == null) {
			UmlClass baseType2 = NiemModel.getBaseType(type);
			String baseTypeName = null;
			if (baseType2 != null)
				baseTypeName = NamespaceModel.getPrefixedName(baseType2);
			if (baseTypeName != null && baseTypeName.endsWith(XmlWriter.SIMPLE_TYPE_NAME))
				codeList = NiemUmlClass.getCodeList(baseType2);
		}
		if (codeList != null && codeList.contains(NiemModel.CODELIST_DELIMITER)) {
			// add codeList
			int codeValues = 0;
			for (String code : codeList.split(NiemModel.CODELIST_DELIMITER)) {
				if (code.equals(""))
					continue;
				String[] codeParams = code.replace("&", "&amp;").split(NiemModel.CODELIST_DEFINITION_DELIMITER);
				String codeValue = codeParams[0].trim();
				if (codeValue.equals(""))
					continue;
				String enumeration = tag("FacetCategoryCode", "enumeration")
					+ tag("FacetValue", codeValue);
				String codeDescription = (codeParams.length > 1 && !codeParams[1].equals("")) ? codeParams[1].trim()
						: "";
				if (codeDescription != null && !codeDescription.equals(""))
					enumeration += tag("DocumentationText", codeDescription);
				restrictionCMF += tag("Facet", enumeration);
				if (++codeValues > MAX_ENUMS) {
					Log.trace("exportCMFDatatype: warning - truncated enumerations in class " + id );
					break;
				}
			}					
		}

		restrictionCMF += "</Restriction>";
		Log.debug("exportCMFClass: exported restriction class " + typeName);
		return restrictionCMF;
	}

	/**
	 * @param prefix
	 * @return CMF multiplicity definition
	 */
	String exportCMFMultiplicity(String multiplicity) {
		return tag("MinOccursQuantity", NiemUmlClass.getMinOccurs(multiplicity)) 
				+ tag("MaxOccursQuantity", NiemUmlClass.getMaxOccurs(multiplicity)); 
	}

	/**
	 * @param prefix
	 * @return CMF namespace definition
	 */
	// TODO exportCMFNamespace: local terms
	String exportCMFNamespace(UmlClassView classview) {
		if (classview == null)
			return null;
		String namespaceCategoryCode;
		String conformanceTargetURI;
		String prefix = NamespaceModel.getPrefix(classview);

		if (!NamespaceModel.isNiemPrefix(prefix)) {
			namespaceCategoryCode = "EXTENSION";
			conformanceTargetURI = XmlWriter.CT_EXTENSION; 
		} else {
			conformanceTargetURI = XmlWriter.CT_REFERENCE; 
                        namespaceCategoryCode = switch (prefix) {
                        case XmlWriter.NC_PREFIX -> "CORE";
                        case NiemModel.LOCAL_PREFIX, NiemModel.XML_PREFIX, NiemModel.XSD_PREFIX, NiemModel.PROXY_PREFIX, NiemModel.STRUCTURES_PREFIX -> "OTHERNIEM";
                        default -> "DOMAIN";
                    };
		}

		String path = classview.propertyValue(NiemUmlClass.FILE_PATH_PROPERTY);
		if (path == null || path.equals(""))
			path = prefix + XmlWriter.XSD_FILE_TYPE;
		String description = classview.description();
		if (description == null || description.equals(""))
			description = "Namespace for " + prefix + " schema";
		Log.trace("exportCMF: adding namespace " + prefix);
		if (tagIds.contains(prefix))
			return tagRef("Namespace",prefix);
		tagIds.add(prefix);

		// augmentations
		String augmentationCmf = "";
		int augmentations = 0;
		for (UmlItem item : classview.children())
			if (item != null && item.kind() == anItemKind.aClassInstance) {
				//UmlClassInstance instance = (UmlAttribute) item;
				NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
				UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));
				if (element != null) {
					String typeName = element.propertyValue(NiemUmlClass.SUBSTITUTION_TYPE_PROPERTY);
					if (typeName != null) {
						Log.debug("exportCMFNamespace: augmenting " + typeName + " with " + NamespaceModel.getName(element));
						String multiplicity = element.propertyValue(NiemUmlClass.SUBSTITUTION_MULTIPLICITY_PROPERTY);
						if (multiplicity == null || multiplicity.equals(""))
							multiplicity = "0,unbounded";
						NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? NiemUmlClass.getSubsetModel() : NiemUmlClass.getExtensionModel();
						UmlClass type = model.getType(NamespaceModel.getSchemaURI(typeName), typeName);
						if (type == null) {
							Log.trace("exportCMFNamespace: unable to find type " + typeName);
							continue;
						}
						String typePrefix = NamespaceModel.getPrefix(type);
						if (prefix != null && typePrefix != null && prefix.equals(typePrefix))
							continue;
						augmentationCmf += "<AugmentationRecord>"
							+ tagRef("Class",typeName)
							+ tagRef("DataProperty",NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element)))
							+ exportCMFMultiplicity(multiplicity)
							+ tag("AugmentationIndex", String.valueOf(augmentations++))
							+ "</AugmentationRecord>";
					}
				}
			}

		return tagStart("Namespace", prefix, true) + ">"
		+ tag("NamespaceURI",NamespaceModel.getSchemaURIForPrefix(prefix)) 
		+ tag("NamespacePrefixText", prefix)
		+ tag("DocumentationText", description)
		+ tag("ConformanceTargetURI", XmlWriter.NDR_URI + conformanceTargetURI)
		+ tag("DocumentFilePathText", path)
		+ tag("NamespaceCategoryCode", namespaceCategoryCode)
//		+ tag("NamespaceVersionText", "ps02")
		+ tag("NIEMVersionName", "NIEM" + NiemUmlClass.getNiemVersion())
		+ tag("NamespaceLanguageName", XmlWriter.XML_LANG)
		+ augmentationCmf
		+ "</Namespace>";
	}

	/**
	 * @param element
	 * @return CMF property definition
	 */
	String exportCMFProperty(UmlClassInstance element) {		
		String id = NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element));
		String propertyCmf;

		if (tagIds.contains(id))
			return tagRef("ObjectProperty",id);

		tagIds.add(id);
		String elementName = NamespaceModel.getName(element);
		if (elementName.endsWith(XmlWriter.AUGMENTATION_POINT_NAME))
			return "";
		Log.debug("exportCMFProperty: exporting property " + elementName);

		UmlClass baseType = getBaseType(element);
		if (baseType == null || baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType()) {
			// abstract
			return tagStart("ObjectProperty", id, true) + ">"
				+ exportCMFComponent(element)
				+ tag("AbstractIndicator","true")
				+ "</ObjectProperty>";
		}
		
		String baseTypeName = NamespaceModel.getPrefixedName(baseType);
		if (dataTypeIds.contains(baseTypeName)) {
			// data property
			propertyCmf = tagStart("DataProperty", id, true) + ">"
				+ exportCMFComponent(element);
				// augmentation
				String head = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
				if (head != null) {
					String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
					UmlClassInstance headElement = NiemUmlClass.getModel(uri).getElementByURI(uri);
					String headElementName = NamespaceModel.getPrefixedName(headElement);
					if (!headElementName.contains(XmlWriter.AUGMENTATION_POINT_NAME))
						propertyCmf += tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(headElement));
				}
				propertyCmf+= tagRef("Datatype",baseTypeName); 
			if (NamespaceModel.isAttribute(elementName))
				propertyCmf += tag("AttributeIndicator","true");
			propertyCmf += "</DataProperty>";
			Log.trace("exportCMFProperty: exported data property " + elementName);
			return propertyCmf;
		} else {
			// object property
			propertyCmf = tagStart("ObjectProperty", id, true) + ">"
				+ exportCMFComponent(element) + tagRef("Class", NamespaceModel.getPrefixedName(baseType));
				// augmentation
				String head = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
				if (head != null) {
					String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
					UmlClassInstance headElement = NiemUmlClass.getModel(uri).getElementByURI(uri);
					propertyCmf += tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(headElement));
				}
			propertyCmf += "</ObjectProperty>";
		}
		Log.trace("exportCMFProperty: exported object property " + elementName);
		return propertyCmf;

	}

		/**
	 * @param item
	 * @return base type related to a type or element as a UmlClass
	 */
	static UmlClass getBaseType(UmlItem item) {

		UmlClass baseType = NiemModel.getBaseType(item);
		if (baseType == null)
		  return null;

		String baseTypeName = NamespaceModel.getPrefixedName(baseType);
		if (baseTypeName.endsWith(XmlWriter.SIMPLE_TYPE_NAME))
			return getBaseType(baseType);

		// replace PROXY_PREFIX with XSD_PREFIX
		String prefix = NamespaceModel.getPrefix(baseType);
		if (prefix.equals(NiemModel.PROXY_PREFIX)) {
			String proxyTypeName = NamespaceModel.getPrefixedName(baseType);
			baseTypeName = NamespaceModel.getPrefixedName(NiemModel.XSD_PREFIX, NamespaceModel.getName(baseType));
			NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName)) ? NiemUmlClass.getSubsetModel() : NiemUmlClass.getExtensionModel();
			baseType = model.getType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName);
			if (baseType == null) {
				Log.trace("getBaseType: unable to replace proxy type " + proxyTypeName + " with type " + baseTypeName);
				return null;
			}
		}

		return baseType;
	}

		/**
	 * @param id
	 * @return Boolean
	 */
	Boolean isClass(UmlClass type) {
		if (type == null)
			return false;
		
		String id = NamespaceModel.getPrefixedName(type);

		// check if association
		String prefix = NamespaceModel.getPrefix(type);
		if (prefix.equals(NiemModel.STRUCTURES_PREFIX)) {
			classIds.add(id);
			return true;
		}

		// check if known data type
		if (dataTypeIds.contains(id))
			return false;

		// check if known class
		if (classIds.contains(id))
			return true;

		// check if base type is a known class
		UmlClass baseType = getBaseType(type);
		if (isClass(baseType)) {
			classIds.add(id);
			return true;
		}

		// check if type is abstract
		if (type == NiemUmlClass.getSubsetModel().getAbstractType() || type == NiemUmlClass.getExtensionModel().getAbstractType()) {
			classIds.add(id);
			return true;
		}

		// check if type contains properties that are not attributes or augmentation points
		Boolean properties = false;
		for (UmlItem item : type.children())
			if (item != null && item.kind() == anItemKind.anAttribute) {
				// UmlAttribute attribute = (UmlAttribute) item;
				NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
				UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));
				if (element != null) {
					String elementName = NamespaceModel.getPrefixedName(element);
					if (elementName.endsWith(XmlWriter.AUGMENTATION_POINT_NAME))
						continue;
					if (NamespaceModel.isAttribute(elementName))
						continue;
					properties=true;
					break;
				}
			}
		return properties;
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
	 * @param tagId	
	 * @param firstOccurence
	 * @return XML start tag
	 */
	String tagStart(String tag, String tagId, Boolean firstOccurence) {
		return "<" + tag + " " + NiemModel.STRUCTURES_PREFIX + ":" + (firstOccurence ? "id" : "ref") + "=\"" + tagId.replace(":", ".") + "\"" + (firstOccurence ? "" : " " + XmlWriter.XSI_PREFIX + ":nil = \"true\"");
	}
}
