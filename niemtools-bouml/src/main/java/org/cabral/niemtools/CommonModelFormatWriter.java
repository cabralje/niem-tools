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

		// export subset and extension namespaces
		for (UmlItem item : items)
			if (item.kind() == anItemKind.aClassView) {
//				if (NiemModel.isInfrastructurePrefix(NamespaceModel.getPrefix(item)))
//					continue;
				String prefix = NamespaceModel.getPrefix(item);
				if (prefix.equals(NiemModel.STRUCTURES_PREFIX) || (prefix.equals(NiemModel.LOCAL_PREFIX)))
					continue;
				cmfNamespaces.add(exportCMFNamespace((UmlClassView)item));
				for (UmlItem item2 : item.children())
					// export subset and extension classes
					if (item2.kind() == anItemKind.aClass) {
						Log.trace("exportCMFModel: exporting class " + NamespaceModel.getName(item2));
						cmfClasses.add(exportCMFClass((UmlClass) item2));
					// export subset and extension elements
					} else if (item2.kind() == anItemKind.aClassInstance) {
						Log.trace("exportCMFModel: exporting property " + NamespaceModel.getName(item2));
						cmfProperties.add(exportCMFProperty((UmlClassInstance) item2));
					}
				}
		// export enumerations
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
                        fw.write(">");
                        
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
		if (tagIds.contains(id))
			return tagRef("Class",id);

		tagIds.add(id);
		String typeName = NamespaceModel.getName(type);
		Log.debug("exportCMFClass: exporting class " + typeName);

		UmlClass baseType = NiemModel.getBaseType(type);
		if (baseType == null)
			baseType = NiemUmlClass.getSubsetModel().getObjectType();

		String prefix = NamespaceModel.getPrefix(type);
            //UmlAttribute augmentationPoint = null;

		// abstract class
		if (baseType != null && 
		(baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType()))
			return tagStart("Class", id, true) + ">" 
				+ exportCMFComponent(type) 
				+ tag("AbstractIndicator","true")
				+ "</Class>";

		// data type class
		if (prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(NiemModel.PROXY_PREFIX))
			return exportCMFDatatype(type);

		int properties = 0;
		String childrenCMF = "";
		for (UmlItem item : type.children())
			if (item.kind() == anItemKind.anAttribute) {
				UmlAttribute attribute = (UmlAttribute) item;
				NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
				UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));
				if (element != null) {
					String elementName = NamespaceModel.getPrefixedName(element);
					if (elementName.contains(XmlWriter.AUGMENTATION_POINT_NAME))
						continue;
					if (NamespaceModel.isAttribute(elementName))
						continue;
					properties++;
					String multiplicity = attribute.multiplicity();
					childrenCMF += "<ChildPropertyAssociation>"
								+ tagRef("ObjectProperty",elementName)
								+ exportCMFMultiplicity(multiplicity)
								+ "</ChildPropertyAssociation>";
				}
			}

		// restriction type
		if (properties == 0)
			return exportCMFRestrictionType(type);
		
		// complex type
		Log.debug("exportCMFClass: exported complex class " + typeName);
		return tagStart("Class", id, true) + ">" 
			+ exportCMFComponent(type) 
			+ tagRef("SubClassOf", NamespaceModel.getPrefixedName(baseType))
			+ childrenCMF 
			+ "</Class>";
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
		dataTypeIds.add(id);
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
		dataTypeIds.add(id);
		String typeName = NamespaceModel.getName(type);

		String restrictionCMF = tagStart("Restriction", id, true) + ">"
				+ exportCMFComponent(type);
		String codeList = type.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		if (codeList != null && codeList.contains(NiemModel.CODELIST_DELIMITER)) {
			// add codeList
			UmlClass baseType = NiemModel.getBaseType(type);
			restrictionCMF += tagRef("RestrictionBase", NamespaceModel.getPrefixedName(baseType));
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
	// TODO exportCMFNamespace: augmentation multiplicity
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
			if (item.kind() == anItemKind.aClassInstance) {
				//UmlClassInstance instance = (UmlAttribute) item;
				NiemModel model2 = NiemUmlClass.getModel(NiemModel.getURI(item));
				UmlClassInstance element = model2.getElementByURI(NiemModel.getURI(item));
				if (element != null) {
					String head = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
					if (head != null) {
						Log.debug("exportCMFNamespace: augmenting " + NamespaceModel.getName(element) + " with " + head);
						String multiplicity = "0,unbounded";
						String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
						
						UmlClassInstance headElement = NiemUmlClass.getModel(uri).getElementByURI(uri);
						String headElementName = NamespaceModel.getPrefixedName(headElement);
						String headElementClassName = headElementName.replaceAll(XmlWriter.AUGMENTATION_POINT_NAME,"Type");
						augmentationCmf += "<AugmentationRecord>"
							+ tagRef("Class",headElementClassName)
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
		+ tag("NIEMVersionName", NiemUmlClass.getNiemVersion())
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
		if (tagIds.contains(id))
			return tagRef("ObjectProperty",id);

		tagIds.add(id);
		String elementName = NamespaceModel.getName(element);
		if (elementName.contains(XmlWriter.AUGMENTATION_POINT_NAME))
			return "";
		Log.debug("exportCMFProperty: exporting property " + elementName);


		UmlClass baseType = NiemModel.getBaseType(element);
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
			String propertyCmf = tagStart("DataProperty", id, true) + ">"
				+ exportCMFComponent(element);
			if (NamespaceModel.isAttribute(elementName))
				propertyCmf += tag("AbstractIndicator","true");
			propertyCmf += tagRef("Datatype",baseTypeName) + "</DataProperty>";
			return propertyCmf;
		}
		
		// object property
		return tagStart("ObjectProperty", id, true) + ">"
			+ exportCMFComponent(element) + tagRef("Class", NamespaceModel.getPrefixedName(baseType))
			+ tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(baseType))
			+ "</ObjectProperty>";

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
