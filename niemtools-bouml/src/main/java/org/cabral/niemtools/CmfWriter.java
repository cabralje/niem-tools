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

public class CmfWriter {

	private static final String CMF_URI = "https://docs.oasis-open.org/niemopen/ns/specification/cmf/";
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

	private String cmfVersion = "1.0";
	private String documentationName = "DocumentationText";
	private String objectPropertyName = "ObjectProperty";
	private String dataPropertyName = "DataProperty";
	private String childPropertyName = "ChildPropertyAssociation";
	private String subclassName = "SubClassOf";
	private String restrictionName = "Restriction";
	private String restrictionBaseName = "RestrictionBase";
	private String facetName = "Facet";
	/**
	 * @param initialDirectory
	 * @param version
	 */
	public CmfWriter(String initialDirectory, String version) {
		super();
		if (version != null) {
			cmfVersion = version;
			if (isOlderCmfVersion(cmfVersion, "1.0")) {
				documentationName = "DefinitionText";
				objectPropertyName =  "Property";
				dataPropertyName = "Property";
				childPropertyName = "HasProperty";
				subclassName = "ExtensionOfClass";
				restrictionName = "Datatype";
				restrictionBaseName = "Datatype";
				facetName = "Enumeration";
			}
		}
	}

	/** copmares CMF versions
	 * @param version1
	 * @param version2
	 * @return true is version 1 is older than version 2
	 */
	final boolean isOlderCmfVersion(String version1, String version2) {
		return (Float.parseFloat(version1) < Float.parseFloat(version2));
	}


/** exports NIEM extension and exchange schema 
	 * @param xmlDir
	 * @param jsonDir
	 * @return JSON definitions as a Set
	 */
	LinkedHashSet<String> exportCmfModel() {
	
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
				cmfNamespaces.add(exportCmfNamespace((UmlClassView)item));
				for (UmlItem item2 : item.children())
					// export subset and extension classes
					if (item2 != null && item2.kind() == anItemKind.aClass) {
						Log.trace("exportCmfModel: exporting class " + NamespaceModel.getName(item2));
						cmfClasses.add(exportCmfClass((UmlClass) item2));
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
						cmfProperties.add(exportCmfProperty((UmlClassInstance) item2));
					}
				}
		cmfModel.addAll(cmfNamespaces);
		cmfModel.addAll(cmfProperties);
		cmfModel.addAll(cmfClasses);	

		return cmfModel;
	}

	/**
	 * @param cmfDir
	 * @param messages
	 */
	void exportCmf(String cmfDir) throws IOException {

		Log.start("exportCmf");
		UmlCom.message("Generating NIEM meta models");


		String cmfUri = CMF_URI + cmfVersion + "/";
		String headerCmf = 	"<Model "
		    + XmlWriter.xmlNs("",cmfUri)
			+ XmlWriter.xmlNs(CMF_PREFIX,cmfUri)
			+ XmlWriter.xmlNs(XmlWriter.XSI_PREFIX,XmlWriter.XSI_URI)
			+ XmlWriter.xmlNs(NiemModel.STRUCTURES_PREFIX,NiemModel.STRUCTURES_URI)
			+ " " + XmlWriter.XML_LANG_PREFIX + "=\"" + XmlWriter.XML_LANG + "\">";
		LinkedHashSet<String> modelCmf = exportCmfModel();
		String fileCmf = XmlWriter.XML_HEADER 
			+ headerCmf + String.join("\n",modelCmf) + ("</Model>");

		try {
			// reset tagIds
			tagIds.clear();

			// open file
			String path1 = CMF_FILE + "-" + cmfVersion + CMF_FILE_TYPE;
			Path p1 = Paths.get(cmfDir, path1);
			File file = p1.toFile();
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			try (FileWriter fw = new FileWriter(file)) {
				// write model
				fw.write(fileCmf);
				fw.close();
			}	

		} catch (IOException e) {
			Log.trace("exportCmf - error exporting CMF model " + e.toString());
		}
		Log.stop("exportCmf");
	}


	/**
	 * @param type
	 * @return CMF class definition
	 */
	// TODO exportCmfClass: handle facets other than enumerations
	// TODO exportCmfClass: add augmentations
	
	String exportCmfClass(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		String classCmf;

		if (tagIds.contains(id))
			return tagRef("Class",id);
		tagIds.add(id);

		String typeName = NamespaceModel.getName(type);
		Log.debug("exportCmfClass: exporting class " + typeName);

		UmlClass baseType = getCmfBaseType(type);
		if (baseType == null)
			baseType = NiemUmlClass.getSubsetModel().getObjectType();

		String prefix = NamespaceModel.getPrefix(type);
            //UmlAttribute augmentationPoint = null;

		// abstract class
		if (baseType != null && 
		(baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType())) {
			classIds.add(id);
			Log.trace("exportCmfClass: exported abstract class " + typeName);
			return tagId("Class", id, 
				exportCmfComponent(type) 
				+ CmfWriter.this.tag("AbstractIndicator","true"));
		}

		// datatype
		if (prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(NiemModel.PROXY_PREFIX)) {
			dataTypeIds.add(id);
			Log.trace("exportCmfClass: exported datatype " + typeName);
			return exportCmfDatatype(type);
		}

		// restriction
		if (!isClass(type)) {
			dataTypeIds.add(id);
			Log.trace("exportCmfClass: exported restriction class " + typeName);
			return exportCmfRestrictionType(type);
		}

		// complex class
		//classIds.add(id);
		String childrenCmf = "";
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
					UmlClass elementBaseType = getCmfBaseType(element);
					String propertyCmf = tagRef((isClass(elementBaseType) ? objectPropertyName : dataPropertyName), elementName, exportCmfMultiplicity(multiplicity));
					childrenCmf += CmfWriter.this.tag(childPropertyName, propertyCmf);
				}
			}


		Log.trace("exportCmfClass: exported complex class " + typeName);
		classCmf = exportCmfComponent(type);
		String baseTypePrefix = NamespaceModel.getPrefix(baseType);
		if (!baseTypePrefix.equals(NiemModel.STRUCTURES_PREFIX))
			classCmf += tagRef(subclassName, NamespaceModel.getPrefixedName(baseType));
		classCmf += childrenCmf;
		return tagId("Class", id, classCmf);
	}

	/**
	 * @param baseType
	 * @param codeList
	 * @return CMF component definition
	 */
	String exportCmfComponent(UmlItem item) {
		String name = NamespaceModel.getName(item);
		if (NamespaceModel.isAttribute(name))
			name = NamespaceModel.filterAttributePrefix(name);
		String componentCmf = 
				CmfWriter.this.tag("Name", name)
				+ tagRef("Namespace", NamespaceModel.getPrefix(item));
		String description = item.description();
		if (description != null && !description.equals(""))
			componentCmf += CmfWriter.this.tag(documentationName,description);

		return componentCmf;
	}

		/**
	 * @param type
	 * @return CMF data type definition
	 */
	String exportCmfDatatype(UmlClass type) {

		String id = NamespaceModel.getPrefixedName(type);
		//dataTypeIds.add(id);
		String typeName = NamespaceModel.getName(type);
		Log.debug("exportCmfClass: exported data type class " + typeName);
		return tagId("Datatype", id, exportCmfComponent(type));
	}

	/**
	 * @param type
	 * @return CMF restriction type definition
	 */
	String exportCmfRestrictionType(UmlClass type) {

 		String id = NamespaceModel.getPrefixedName(type);
		 if (id.endsWith(XmlWriter.SIMPLE_TYPE_NAME)) {
			Log.trace("exportCmfRestrictionType: skipping simple type " + id);
		 	return "";
		 }
		String typeName = NamespaceModel.getName(type);

		String restrictionCmf = exportCmfComponent(type);
		UmlClass baseType = getCmfBaseType(type);
		if (baseType == null)
			Log.trace("exportCmfClass: unable to find base type for " + typeName);
		else {
			//if (isOlderCmfVersion(cmfVersion, "1.0"))
			//	restrictionCmf += tagHead("RestrictionOf");
			restrictionCmf += tagRef(restrictionBaseName, NamespaceModel.getPrefixedName(baseType));
		}
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
				String enumeration;
				if (isOlderCmfVersion(cmfVersion, "1.0"))
					enumeration = CmfWriter.this.tag("StringValue", codeValue);
				else
					enumeration = CmfWriter.this.tag("FacetCategoryCode", "enumeration")
						+ CmfWriter.this.tag("FacetValue", codeValue);
				String codeDescription = (codeParams.length > 1 && !codeParams[1].equals("")) ? codeParams[1].trim()
						: "";
				if (codeDescription != null && !codeDescription.equals(""))
					enumeration += CmfWriter.this.tag(documentationName, codeDescription);
				restrictionCmf += CmfWriter.this.tag(facetName, enumeration);
				if (++codeValues > MAX_ENUMS) {
					Log.trace("exportCmfDatatype: warning - truncated enumerations in class " + id );
					break;
				}
			}					
		}
		if (isOlderCmfVersion(cmfVersion, "1.0"))
			restrictionCmf += CmfWriter.this.tag("RestrictionOf",restrictionCmf);
		Log.debug("exportCmfClass: exported restriction class " + typeName);
		return tagId(restrictionName, id, restrictionCmf);
	}

	/**
	 * @param prefix
	 * @return CMF multiplicity definition
	 */
	String exportCmfMultiplicity(String multiplicity) {
		return CmfWriter.this.tag("MinOccursQuantity", NiemUmlClass.getMinOccurs(multiplicity)) 
				+ CmfWriter.this.tag("MaxOccursQuantity", NiemUmlClass.getMaxOccurs(multiplicity)); 
	}

	/**
	 * @param prefix
	 * @return CMF namespace definition
	 */
	// TODO exportCmfNamespace: local terms
	String exportCmfNamespace(UmlClassView classview) {
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
		Log.trace("exportCmf: adding namespace " + prefix);
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
						Log.debug("exportCmfNamespace: augmenting " + typeName + " with " + NamespaceModel.getName(element));
						String multiplicity = element.propertyValue(NiemUmlClass.SUBSTITUTION_MULTIPLICITY_PROPERTY);
						if (multiplicity == null || multiplicity.equals(""))
							multiplicity = "0,unbounded";
						NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? NiemUmlClass.getSubsetModel() : NiemUmlClass.getExtensionModel();
						UmlClass type = model.getType(NamespaceModel.getSchemaURI(typeName), typeName);
						if (type == null) {
							Log.trace("exportCmfNamespace: unable to find type " + typeName);
							continue;
						}
						String typePrefix = NamespaceModel.getPrefix(type);
						if (prefix != null && typePrefix != null && prefix.equals(typePrefix))
							continue;
						augmentationCmf += "<AugmentationRecord>"
							+ tagRef("Class",typeName)
							+ tagRef("DataProperty",NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element)))
							+ exportCmfMultiplicity(multiplicity)
							+ CmfWriter.this.tag("AugmentationIndex", String.valueOf(augmentations++))
							+ "</AugmentationRecord>";
					}
				}
			}
		String schemaCmf = "";
		String namespaceCmf = CmfWriter.this.tag("NamespaceURI",NamespaceModel.getSchemaURIForPrefix(prefix)) 
			+ CmfWriter.this.tag("NamespacePrefixText", prefix)
			+ CmfWriter.this.tag(documentationName, description);
		if (isOlderCmfVersion(cmfVersion, "1.0")) {
			namespaceCmf += CmfWriter.this.tag("NamespaceKindCode", namespaceCategoryCode);
			schemaCmf = CmfWriter.this.tag("SchemaDocument",
				CmfWriter.this.tag("NamespaceURI",NamespaceModel.getSchemaURIForPrefix(prefix)) 
				+ CmfWriter.this.tag("NamespacePrefixText", prefix)
				+ CmfWriter.this.tag("ConformanceTargetURIList", XmlWriter.NDR_URI + conformanceTargetURI)
				+ CmfWriter.this.tag("DocumentFilePathText", path)
	//			+ tag("NamespaceKindCode", namespaceCategoryCode)
				+ CmfWriter.this.tag("NIEMVersionText", NiemUmlClass.getNiemVersion())
	//			+ tag("SchemaVersionText", "ps02")
				+ CmfWriter.this.tag("SchemaLanguageName", XmlWriter.XML_LANG));
		} else
			namespaceCmf += CmfWriter.this.tag("ConformanceTargetURI", XmlWriter.NDR_URI + conformanceTargetURI)
				+ CmfWriter.this.tag("DocumentFilePathText", path)
				+ CmfWriter.this.tag("NamespaceCategoryCode", namespaceCategoryCode)
		//		+ tag("NamespaceVersionText", "ps02")
				+ CmfWriter.this.tag("NIEMVersionName", "NIEM" + NiemUmlClass.getNiemVersion())
				+ CmfWriter.this.tag("NamespaceLanguageName", XmlWriter.XML_LANG)
				+ augmentationCmf;

		return tagId("Namespace", prefix, namespaceCmf) + schemaCmf;
	}

	/**
	 * @param element
	 * @return CMF property definition
	 */
	String exportCmfProperty(UmlClassInstance element) {		
		String id = NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element));
		String propertyCmf;


		if (tagIds.contains(id))
			return tagRef(objectPropertyName,id);

		tagIds.add(id);
		String elementName = NamespaceModel.getName(element);
		if (elementName.endsWith(XmlWriter.AUGMENTATION_POINT_NAME))
			return "";
		Log.debug("exportCmfProperty: exporting property " + elementName);
			
		UmlClass baseType = getCmfBaseType(element);
		if (baseType == null || baseType == NiemUmlClass.getSubsetModel().getAbstractType() || baseType == NiemUmlClass.getExtensionModel().getAbstractType()) {
			// abstract
			return tagId(objectPropertyName, id,
				exportCmfComponent(element)
				+ CmfWriter.this.tag("AbstractIndicator","true"));
		}
		
		String baseTypeName = NamespaceModel.getPrefixedName(baseType);
		if (dataTypeIds.contains(baseTypeName)) {
			// data property
			propertyCmf = exportCmfComponent(element);
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
				propertyCmf += CmfWriter.this.tag("AttributeIndicator","true");
			Log.trace("exportCmfProperty: exported data property " + elementName);
			return tagId(dataPropertyName, id, propertyCmf);
		} else {
			// object property
			propertyCmf = exportCmfComponent(element) + tagRef("Class", NamespaceModel.getPrefixedName(baseType));
				// augmentation
				String head = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
				if (head != null) {
					String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
					UmlClassInstance headElement = NiemUmlClass.getModel(uri).getElementByURI(uri);
					propertyCmf += tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(headElement));
				}
		}
		Log.trace("exportCmfProperty: exported object property " + elementName);
		return tagId(objectPropertyName, id, propertyCmf);

	}

		/**
	 * @param item
	 * @return base type related to a type or element as a UmlClass
	 */
	static UmlClass getCmfBaseType(UmlItem item) {

		UmlClass baseType = NiemModel.getBaseType(item);
		if (baseType == null)
		  return null;

		String baseTypeName = NamespaceModel.getPrefixedName(baseType);
		if (baseTypeName.endsWith(XmlWriter.SIMPLE_TYPE_NAME))
			return getCmfBaseType(baseType);

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
		UmlClass baseType = getCmfBaseType(type);
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
	 * @param tagId
	 * @param tagRef	
	 * @param content
	 * @return XML start tag
	 */
	String tag(String tag, String id, String ref, String content) {
		String tagString = "<" + tag;
		if (id != null && !id.equals(""))
			tagString += " " + NiemModel.STRUCTURES_PREFIX + ":id=\"" + id.replace(":", ".") + "\"";
		if (ref != null && !ref.equals(""))
			tagString += " " + NiemModel.STRUCTURES_PREFIX + ":ref=\"" + ref.replace(":", ".") + "\"";
		if (content != null && !content.equals(""))
			tagString += ">" + content + "</" + tag + ">";
		else
			tagString += "/>";
		return tagString;
	}

	/**
	 * @param tag
	 * @param id
	 * @return XML tagged content with ID
	 */
	String tagId(String tag, String id) {
		return tag(tag, id, null, null);
	}

	/**
	 * @param tag
	 * @param id
	 * @param content
	 * @return XML tagged content with ID
	 */
	String tagId(String tag, String id, String content) {
		return tag(tag, id, null, content);
	}

	/**
	 * @param tag
	 * @param id
	 * @param content
	 * @return XML reference tag
	 */
	String tagRef(String tag, String id) {
		return tag(tag, null, id, null);
	}
	
	/**
	 * @param tag
	 * @param id
	 * @param content
	 * @return XML reference tag
	 */
	String tagRef(String tag, String id, String content) {
		return tag(tag, null, id, content);
	}

}