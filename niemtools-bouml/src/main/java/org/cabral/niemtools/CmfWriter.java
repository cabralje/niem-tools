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
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class CmfWriter {

    private static final String CMF_URI = "https://docs.oasis-open.org/niemopen/ns/specification/cmf/";
    private static final String CMF_OLDURI = "http://reference.niem.gov/specification/cmf/";
    private static final String CMF_PREFIX = "cmf";
    private static final String CMF_FILE_TYPE = ".cmf";
    private static final String CMF_FILE = "model";
    //@SuppressWarnings("unused")

    private final Set<String> tagIds = new HashSet<>();
    private final Set<String> dataTypeIds = new HashSet<>();
    private final Set<String> classIds = new HashSet<>();

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
                objectPropertyName = "Property";
                dataPropertyName = "Property";
                childPropertyName = "HasProperty";
                subclassName = "ExtensionOfClass";
                restrictionName = "Datatype";
                restrictionBaseName = "Datatype";
                facetName = "Enumeration";
            }
        }
    }

    /**
     * copmares CMF versions
     *
     * @param version1
     * @param version2
     * @return true is version 1 is older than version 2
     */
    private boolean isOlderCmfVersion(String version1, String version2) {
        return (Float.parseFloat(version1) < Float.parseFloat(version2));
    }

    /**
     * exports NIEM extension and exchange schema
     *
     * @param xmlDir
     * @param jsonDir
     * @return JSON definitions as a Set
     */
    private LinkedHashSet<String> exportCmfModel() {

        LinkedHashSet<String> cmfModel = new LinkedHashSet<>();
        TreeSet<String> cmfNamespaces = new TreeSet<>();
        TreeSet<String> cmfClasses = new TreeSet<>();
        TreeSet<String> cmfProperties = new TreeSet<>();

        List<UmlItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(NiemUmlModel.getSubsetModel().getModelPackage().children()));
        items.addAll(Arrays.asList(NiemUmlModel.getExtensionModel().getModelPackage().children()));

        Log.debug("exportCmfModel: exporting " + items.size() + " namespaces and types");
        try {
            // export subset and extension namespaces and types
            for (UmlItem item : items) {
                if (item != null && item.kind() == anItemKind.aClassView) {
                    String prefix = NamespaceModel.getPrefix(item);
                    if (prefix == null) {
                        Log.trace("exportCmfModel: invalid prefix for " + NamespaceModel.getName(item));
                        continue;
                    }
                    //if (NamespaceModel.isInfrastructurePrefix(prefix))
                    if (prefix.equals(NiemModel.PROXY_PREFIX) || prefix.equals(NiemModel.LOCAL_PREFIX))
                        continue;
                    String cmfNamespace = exportCmfNamespace((UmlClassView) item);
                    if (cmfNamespace == null) {
                        Log.trace("exportCmfModel: unable to export namespace " + NamespaceModel.getName(item));
                        continue;
                    }
                    cmfNamespaces.add(cmfNamespace);
                    for (UmlItem item2 : item.children()) // export subset and extension classes
                        if (item2 != null && item2.kind() == anItemKind.aClass) {
                            Log.debug("exportCmfModel: exporting class " + NamespaceModel.getName(item2));
                            String cmfClass = null;
                            try {
                                cmfClass = exportCmfClass((UmlClass) item2);
                            } catch (Exception e) {
                                Log.trace("exportCmfModel: unable to export class " + NamespaceModel.getName(item2) + " " + e.toString());
                            }
                            if (cmfClass == null) {
                                Log.trace("exportCmfModel: unable to export class " + NamespaceModel.getName(item2));
                                continue;
                            }
                            cmfClasses.add(cmfClass);
                        }
                }
            }
        } catch (Exception e) {
            Log.trace("exportCmfModel - error exporting subset and extension namespaces and types " + e.toString());
        }

        try {
            // export subset and extension properties
            for (UmlItem item : items) {
                if (item != null && item.kind() == anItemKind.aClassView) {
                    String prefix = NamespaceModel.getPrefix(item);
                    if (NamespaceModel.isInfrastructurePrefix(prefix))
                        continue;
                    for (UmlItem item2 : item.children()) // export subset and extension properties
                        if (item2 != null && item2.kind() == anItemKind.aClassInstance) {
                            String cmfProperty = exportCmfProperty((UmlClassInstance) item2);
                            if (cmfProperty == null) {
                                Log.trace("exportCmfModel: unable to export property " + NamespaceModel.getName(item2));
                                continue;   
                            }
                            cmfProperties.add(cmfProperty);
                        }
                }
            }
        } catch (Exception e) {
            Log.trace("exportCmfModel - error exporting subset and extension properties " + e.toString());
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
    public void exportCmf(String cmfDir) throws IOException {

        Log.start("exportCmf");
        Log.trace("Generating CMF model " + cmfVersion);

        String cmfUri = CMF_URI + cmfVersion + "/";
        if (isOlderCmfVersion(cmfVersion, "1.0")) {
            cmfUri = CMF_OLDURI + cmfVersion + "/";
        }
        String headerCmf = "<Model "
                + XmlWriter.xmlNs("", cmfUri)
                + XmlWriter.xmlNs(CMF_PREFIX, cmfUri)
                + XmlWriter.xmlNs(XmlWriter.XSI_PREFIX, XmlWriter.XSI_URI)
                + XmlWriter.xmlNs(NiemModel.STRUCTURES_PREFIX, NiemModel.STRUCTURES_URI)
                + " " + XmlWriter.XML_LANG_PREFIX + "=\"" + XmlWriter.XML_LANG + "\">";
        LinkedHashSet<String> modelCmf = exportCmfModel();
        String fileCmf = XmlWriter.XML_HEADER
                + headerCmf + String.join("\n", modelCmf) + ("</Model>");

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
            Log.debug("exportCmf - error exporting CMF model " + e.toString());
        }
        Log.stop("exportCmf");
    }

    /**
     * @param type
     * @return CMF class definition
     */
    // TODO exportCmfClass: add augmentations to CMF < 1.0
    private String exportCmfClass(UmlClass type) {

        String id = NamespaceModel.getPrefixedName(type);
        String classCmf;
        String typeName;
        try {
            if (tagIds.contains(id))
                return tagRef("Class", id);
            tagIds.add(id);

            typeName = NamespaceModel.getName(type);
            Log.debug("exportCmfClass: exporting class " + typeName);

            String prefix = NamespaceModel.getPrefix(type);

            // datatype
            if (prefix.equals(NiemModel.XSD_PREFIX) || prefix.equals(NiemModel.PROXY_PREFIX)) {
                dataTypeIds.add(id);
                Log.debug("exportCmfClass: exported datatype " + typeName);
                return exportCmfDatatype(type, false);
            }
        } catch (Exception e) { 
            Log.trace("exportCmfClass: error exporting class " + id + ": " + e.toString());
            return null;
        }

        UmlClass baseType;
        try {
            baseType = getCmfBaseType(type);
        } catch (Exception e) {
            Log.trace("exportCmfClass: error getting base type for " + typeName + ": " + e.toString());
            return null;
        }
        if (baseType == null)
            baseType = NiemUmlModel.getSubsetModel().getObjectType();

        // abstract class
        try {
            if (baseType == null || NiemModel.isAbstract(NamespaceModel.getName(baseType))) {
                dataTypeIds.add(id);
                Log.debug("exportCmfClass: exported abstract class " + typeName);
                return exportCmfDatatype(type, true);
            }
        } catch (Exception e) {
            Log.trace("exportCmfClass: error checking abstract for " + typeName + ": " + e.toString());
            return null;
        }

        // restriction
        boolean isClassType = false;
        try {
            isClassType = isClass(type);
        } catch (Exception e) {
            Log.trace("exportCmfClass: error checking isClass for " + typeName + ": " + e.toString());
        }
        try {
            if (!isClassType) {
                dataTypeIds.add(id);
                Log.debug("exportCmfClass: exported restriction class " + typeName);
                return exportCmfRestrictionType(type);
            }
        } catch (Exception e) {
            Log.trace("exportCmfClass: error exporting restriction for " + typeName + ": " + e.toString());
            return null;
        }

        // complex class
        String childrenCmf = "";
        for (UmlItem item : type.children()) {
            try {
                if (item != null && item.kind() == anItemKind.anAttribute) {
                    UmlAttribute attribute = (UmlAttribute) item;
                    NiemModel model2 = NiemUmlModel.getModel(NiemModel.getURI(item));
                    UmlClassInstance element = model2.getReferencedElement(item);
                    if (element != null) {
                        String elementName = NamespaceModel.getPrefixedName(element);
                        if (elementName.endsWith(NiemModel.AUGMENTATION_POINT_NAME))
                            continue;
                        if (NamespaceModel.isAttribute(elementName))
                            continue;
                        String multiplicity = attribute.multiplicity();
                        UmlClass elementBaseType = null;
                        try {
                            elementBaseType = getCmfBaseType(element);
                        } catch (Exception e) {
                            Log.trace("exportCmfClass: error getting base type for element " + elementName + ": " + e.toString());
                        }
                        String propertyCmf = tagRef(isClass(elementBaseType) ? objectPropertyName : dataPropertyName, elementName);
                        propertyCmf += exportCmfMultiplicity(multiplicity);
                        childrenCmf += tag(childPropertyName, propertyCmf);
                    }
                }
            } catch (Exception e) {
                Log.trace("exportCmfClass: error processing child attribute: " + e.toString());
            }
        }

        try {
            Log.debug("exportCmfClass: exported complex class " + typeName);
            classCmf = exportCmfComponent(type);
            if (isOlderCmfVersion(cmfVersion, "1.0")) {
                classCmf += tag("AugmentableIndicator", "true");
            }
            String baseTypePrefix = NamespaceModel.getPrefix(baseType);
            if (!baseTypePrefix.equals(NiemModel.STRUCTURES_PREFIX)) {
                classCmf += tagRef(subclassName, NamespaceModel.getPrefixedName(baseType));
            }
            classCmf += childrenCmf;
            return tagId("Class", id, classCmf);
        } catch (Exception e) {
            Log.trace("exportCmfClass: error exporting class " + id + ": " + e.toString());
            return null;
        }
    }

    /**
     * @param baseType
     * @return CMF component definition
     */
    private String exportCmfComponent(UmlItem item) {
        String name = NamespaceModel.getName(item);
        if (NamespaceModel.isAttribute(name))
            name = NamespaceModel.filterAttributePrefix(name);
        String componentCmf
                = tag("Name", name)
                + tagRef("Namespace", NamespaceModel.getPrefix(item));
        String description = item.description();
        if (description != null && !description.equals(""))
            componentCmf += tag(documentationName, description);

        return componentCmf;
    }

    /**
     * @param type
     * @return CMF data type definition
     */
    private String exportCmfDatatype(UmlClass type, Boolean isAbstract) {

        String id = NamespaceModel.getPrefixedName(type);
        //dataTypeIds.add(id);
        String typeName = NamespaceModel.getName(type);
        Log.debug("exportCmfClass: exported data type class " + typeName);
        String dataTypeCmf = exportCmfComponent(type);
        if (isAbstract)
            dataTypeCmf += tag("AbstractIndicator", "true");
        return tagId("Datatype", id, dataTypeCmf);
    }

    /**
     * @param type
     * @return CMF restriction type definition
     */
    private String exportCmfRestrictionType(UmlClass type) {

        String typeName = null;
        UmlClass codeListType = null;
        String restrictionCmf;
        String id = NamespaceModel.getPrefixedName(type);
        try {
            if (id.endsWith(NiemModel.SIMPLE_TYPE_NAME)) {
                Log.debug("exportCmfRestrictionType: skipping simple type " + id);
                return "";
            }
            typeName = NamespaceModel.getName(type);

            restrictionCmf = exportCmfComponent(type);
            UmlClass baseType = getCmfBaseType(type);
            if (baseType == null)
                Log.trace("exportCmfClass: unable to find base type for " + typeName); 
            else 
                restrictionCmf += tagRef(restrictionBaseName, NamespaceModel.getPrefixedName(baseType));
            if (NiemUmlModel.isEnumeration(type))
                codeListType = type; 
            // check if base type is a code list
            else {
                UmlClass baseType2 = NiemModel.getBaseType(type);
                if (baseType2 != null && NiemUmlModel.isEnumeration(baseType2))
                    codeListType = baseType2;
            }
        } catch (Exception e) {
            Log.trace("exportCmfClass: error exporting restriction type " + typeName + ": " + e.toString());
            return null;
        }

        try {
            // add codeList
            if (codeListType != null) {
                Log.debug("exportCmfClass: exporting code list " + typeName);
                for (UmlItem item : codeListType.children()) {
                    if (item != null && item.kind() == anItemKind.anAttribute) {
                        UmlAttribute attribute = (UmlAttribute) item;
                        String codeValue = attribute.defaultValue();
                        String codeDescription = attribute.description();
                        String name = attribute.name();
                        if (!NiemUmlModel.isFacet(attribute))
                            name = "enumeration";
                        String enumeration;
                        if (isOlderCmfVersion(cmfVersion, "1.0"))
                            enumeration = tag("StringValue", codeValue); 
                        else
                            enumeration = tag("FacetCategoryCode", name)
                                    + tag("FacetValue", codeValue);
                        if (codeDescription != null && !codeDescription.equals(""))
                            enumeration += tag(documentationName, codeDescription);
                        restrictionCmf += tag(facetName, enumeration);
                    }
                }
            }
            if (isOlderCmfVersion(cmfVersion, "1.0"))
                restrictionCmf += tag("RestrictionOf", restrictionCmf);
            Log.debug("exportCmfClass: exported restriction class " + typeName);
            return tagId(restrictionName, id, restrictionCmf);
        }
        catch (Exception e) {
            Log.trace("exportCmfClass: error exporting restriction type " + typeName + ": " + e.toString());
            return null;
        }
    }   
    /**
     * @param prefix
     * @return CMF multiplicity definition
     */
    private String exportCmfMultiplicity(String multiplicity) {
        if (multiplicity == null || multiplicity.equals(""))
            return "";
        return tag("MinOccursQuantity", NiemUmlModel.getMinOccurs(multiplicity))
                + tag("MaxOccursQuantity", NiemUmlModel.getMaxOccurs(multiplicity));
    }

    /**
     * @param prefix
     * @return CMF namespace definition
     */
    // TODO exportCmfNamespace: local terms
    private String exportCmfNamespace(UmlClassView classview) {
        if (classview == null)
            return null;
        String namespaceCategoryCode;
        String conformanceTargetURI;
        String prefix = NamespaceModel.getPrefix(classview);

        if (!NamespaceModel.isNiemPrefix(prefix)) {
            namespaceCategoryCode = "EXTENSION";
            conformanceTargetURI = NiemModel.CT_EXTENSION;
        } else if (NamespaceModel.isInfrastructurePrefix(prefix)) {
            namespaceCategoryCode = "OTHERNIEM";
            conformanceTargetURI = NiemModel.CT_REFERENCE;
        } else {
            conformanceTargetURI = NiemModel.CT_REFERENCE;
            namespaceCategoryCode = switch (prefix) {
                case NiemModel.NC_PREFIX ->
                    "CORE";
                default ->
                    "DOMAIN";
            };
        }

        String path = classview.propertyValue(NiemUmlModel.FILE_PATH_PROPERTY);
        if (path == null || path.equals(""))
            path = prefix + XmlWriter.XSD_FILE_TYPE;
        String description = classview.description();
        if (description == null || description.equals(""))
            description = "Namespace for " + prefix + " schema";
        Log.debug("exportCmf: adding namespace " + prefix);
        if (tagIds.contains(prefix))
            return tagRef("Namespace", prefix);
        tagIds.add(prefix);

        // augmentations
        String augmentationCmf = "";
        int augmentations = 0;
        for (UmlItem item : classview.children()) {
            if (item != null && item.kind() == anItemKind.aClassInstance) {
                //UmlClassInstance instance = (UmlAttribute) item;
                NiemModel model2 = NiemUmlModel.getModel(NiemModel.getURI(item));
                UmlClassInstance element = model2.getReferencedElement(item);
                if (element != null) {
                    String typeName = element.propertyValue(NiemUmlModel.SUBSTITUTION_TYPE_PROPERTY);
                    if (typeName != null) {
                        Log.debug("exportCmfNamespace: augmenting " + typeName + " with " + NamespaceModel.getName(element));
                        String multiplicity = element.propertyValue(NiemUmlModel.SUBSTITUTION_MULTIPLICITY_PROPERTY);
                        if (multiplicity == null || multiplicity.equals(""))
                            multiplicity = "0,unbounded";
                        NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) ? NiemUmlModel.getSubsetModel() : NiemUmlModel.getExtensionModel();
                        UmlClass type = model.getType(NamespaceModel.getSchemaURI(typeName), typeName);
                        if (type == null) {
                            Log.debug("exportCmfNamespace: unable to find type " + typeName);
                            continue;
                        }
                        String typePrefix = NamespaceModel.getPrefix(type);
                        if (prefix != null && typePrefix != null && prefix.equals(typePrefix))
                            continue;
                        augmentationCmf += "<AugmentationRecord>"
                                + tagRef("Class", typeName)
                                + tagRef("DataProperty", NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element)))
                                + exportCmfMultiplicity(multiplicity)
                                + tag("AugmentationIndex", String.valueOf(augmentations++))
                                + "</AugmentationRecord>";
                    }
                }
            }
        }
        String schemaCmf = "";
        String namespaceCmf = tag("NamespaceURI", NamespaceModel.getSchemaURIForPrefix(prefix))
                + tag("NamespacePrefixText", prefix)
                + tag(documentationName, description);
        if (isOlderCmfVersion(cmfVersion, "1.0")) {
            namespaceCmf += tag("NamespaceKindCode", namespaceCategoryCode);
            schemaCmf = tag("SchemaDocument",
                    tag("NamespaceURI", NamespaceModel.getSchemaURIForPrefix(prefix))
                    + tag("NamespacePrefixText", prefix)
                    + tag("ConformanceTargetURIList", NiemModel.NDR_URI + conformanceTargetURI)
                    + tag("DocumentFilePathText", path)
                    //			+ tag("NamespaceKindCode", namespaceCategoryCode)
                    + tag("NIEMVersionText", NiemUmlModel.getNiemVersion())
                    //			+ tag("SchemaVersionText", "ps02")
                    + tag("SchemaLanguageName", XmlWriter.XML_LANG));
        } else
            namespaceCmf += tag("ConformanceTargetURI", NiemModel.NDR_URI + conformanceTargetURI)
                    + tag("DocumentFilePathText", path)
                    + tag("NamespaceCategoryCode", namespaceCategoryCode)
                    //		+ tag("NamespaceVersionText", "ps02")
                    + tag("NIEMVersionName", "NIEM" + NiemUmlModel.getNiemVersion())
                    + tag("NamespaceLanguageName", XmlWriter.XML_LANG)
                    + augmentationCmf;

        return tagId("Namespace", prefix, namespaceCmf) + schemaCmf;
    }

    /**
     * @param element
     * @return CMF property definition
     */
    // TODO exportCmfProperty: handle abstract properties
    private String exportCmfProperty(UmlClassInstance element) {
        String id = NamespaceModel.filterAttributePrefix(NamespaceModel.getPrefixedName(element));
        if (id == null || id.equals("")) {
            Log.trace("exportCmfProperty: unable to find id for " + NamespaceModel.getName(element));
            return null;
        }
        String propertyCmf;

        if (tagIds.contains(id))
            return tagRef(objectPropertyName, id);

        tagIds.add(id);
        String elementName = NamespaceModel.getName(element);
        //if (elementName.endsWith(NiemModel.AUGMENTATION_POINT_NAME))
        //    return "";
        Log.debug("exportCmfProperty: exporting property " + elementName);

        UmlClass baseType = getCmfBaseType(element);
        if (baseType == null || NiemModel.isAbstract(NamespaceModel.getName(baseType)))
            // abstract
            return tagId(dataPropertyName, id,
                    exportCmfComponent(element)
                    + tag("AbstractIndicator", "true"));
    
        String baseTypeName = NamespaceModel.getPrefixedName(baseType);
        if (dataTypeIds.contains(baseTypeName)) {
            // data property
            propertyCmf = exportCmfComponent(element);
            // augmentation
            String head = element.propertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY);
            if (head != null) {
                String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
                UmlClassInstance headElement = NiemUmlModel.getModel(uri).getElementByURI(uri);
                String headElementName = NamespaceModel.getPrefixedName(headElement);
                if (!headElementName.contains(NiemModel.AUGMENTATION_POINT_NAME))
                    propertyCmf += tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(headElement));
            }
            propertyCmf += tagRef("Datatype", baseTypeName);
            if (NamespaceModel.isAttribute(elementName))
                propertyCmf += tag("AttributeIndicator", "true");
            Log.debug("exportCmfProperty: exported data property " + elementName);
            return tagId(dataPropertyName, id, propertyCmf);
        } else {
            // object property
            propertyCmf = exportCmfComponent(element) + tagRef("Class", NamespaceModel.getPrefixedName(baseType));
            // augmentation
            String head = element.propertyValue(NiemUmlModel.SUBSTITUTION_PROPERTY);
            if (head != null) {
                String uri = NiemModel.getURI(NamespaceModel.getSchemaURI(head), head);
                UmlClassInstance headElement = NiemUmlModel.getModel(uri).getElementByURI(uri);
                propertyCmf += tagRef("SubPropertyOf", NamespaceModel.getPrefixedName(headElement));
            }
        }
        Log.debug("exportCmfProperty: exported object property " + elementName);
        return tagId(objectPropertyName, id, propertyCmf);

    }

    /**
     * @param item
     * @return base type related to a type or element as a UmlClass
     */
    private static UmlClass getCmfBaseType(UmlItem item) {

        UmlClass baseType = NiemModel.getBaseType(item);
        if (baseType == null)
            return null;

        String baseTypeName = NamespaceModel.getPrefixedName(baseType);
        if (baseTypeName.endsWith(NiemModel.SIMPLE_TYPE_NAME))
            return getCmfBaseType(baseType);

        // replace PROXY_PREFIX with XSD_PREFIX
        String prefix = NamespaceModel.getPrefix(baseType);
        if (prefix.equals(NiemModel.PROXY_PREFIX)) {
            String proxyTypeName = NamespaceModel.getPrefixedName(baseType);
            baseTypeName = NamespaceModel.getPrefixedName(NiemModel.XSD_PREFIX, NamespaceModel.getName(baseType));
            NiemModel model = NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(baseTypeName)) ? NiemUmlModel.getSubsetModel() : NiemUmlModel.getExtensionModel();
            baseType = model.getType(NamespaceModel.getSchemaURI(baseTypeName), baseTypeName);
            if (baseType == null) {
                Log.debug("getBaseType: unable to replace proxy type " + proxyTypeName + " with type " + baseTypeName);
                return null;
            }
        }

        return baseType;
    }

    /**
     * @param id
     * @return Boolean
     */
    private Boolean isClass(UmlClass type) {
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
        if (NiemModel.isAbstract(NamespaceModel.getName(type))) {
            dataTypeIds.add(id);
            return false;
        }

        Boolean properties = false;
        // check if type contains properties that are not attributes or augmentation points
        if (!type.stereotype().equals(NiemUmlModel.ENUM_STEREOTYPE))
            for (UmlItem item : type.children()) {
                if (item != null && item.kind() == anItemKind.anAttribute) {
                    UmlAttribute attribute = (UmlAttribute) item;
                    String attributeUri = NiemModel.getURI(attribute);
                    if (attributeUri == null) {
                        Log.trace("isClass: unable to find URI for " + NamespaceModel.getName(attribute));
                        continue;
                    }
                    NiemModel model2 = NiemUmlModel.getModel(attributeUri);
                    UmlClassInstance element = model2.getReferencedElement(item);
                    if (element != null) {
                        String elementName = NamespaceModel.getPrefixedName(element);
                        if (elementName.endsWith(NiemModel.AUGMENTATION_POINT_NAME))
                            continue;
                        if (NamespaceModel.isAttribute(elementName))
                            continue;
                        properties = true;
                        break;
                    }
                }
            }
        return properties;
    }

    /**
     * @param tag
     * @param content
     * @return XML tagged content
     */
    private String tag(String tag, String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    /**
     * @param tag
     * @param tagId
     * @param tagRef
     * @param content
     * @return XML tagged content
     */
    private String tag(String tag, String id, String ref, String content) {
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
    //private String tagId(String tag, String id) {
    //    return tag(tag, id, null, null);
    //}

    /**
     * @param tag
     * @param id
     * @param content
     * @return XML tagged content with ID
     */
    private String tagId(String tag, String id, String content) {
        return tag(tag, id, null, content);
    }

    /**
     * @param tag
     * @param id
     * @return XML reference tag
     */
    private String tagRef(String tag, String id) {
        return tag(tag, null, id, null);
    }

    /**
     * @param tag
     * @param id
     * @param content
     * @return XML reference tag with content
     */
    private String tagRef(String tag, String id, String content) {
        return tag(tag, null, id, content);
    }

}
