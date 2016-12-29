// James E. Cabral Jr, MTG Management Consultants LLC jcabral@mtgmc.com
// Original version:  9/24/15
// Current version: 11/23/16

// This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) v3.2 defined at http://niem.gov.
// Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
// UML Platform Speciic Model (PSM), the NIEM reference model, and a UML Platform Specific Model (PSM), NIEM XML Schema.
//
// NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
// DOM
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// OpenCSV library
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

class NiemTools {
	private static UmlClass referenceAbstractType = null;
	private static UmlClass subsetAbstractType = null;
	private static UmlClass subsetAugmentationType = null;
	private static UmlClass subsetObjectType = null;
	private static UmlClassInstance referenceAnyElement = null;
	private static String anyElementName = "any";
	private static String abstractTypeName = "abstract";
	private static String augmentationTypeName = "AugmentationType";
	private static String objectTypeName = "ObjectType";
	private static String hashDelimiter = ",";
	public static int importPass;
	private static String localPrefix = "local";
	private static String structuresPrefix = "structures";
	private static String extensionSchemaURI = "http://local/";
	private static String notesProperty = "Notes";
	private static String uriProperty = "URI";
	private static String substitutionProperty = "substitutesFor";
	private static String codeListProperty = "codeList";

	// NIEM mapping spreadsheet column headings, NIEM profile profile stereotype
	private static final String[][] map = { { "Model Class", "", }, { "Model Attribute", "", },
			{ "Model Multiplicity", "", }, { "Model Definition", "", }, { "NIEM XPath", "XPath" },
			{ "NIEM Type", "Type" }, { "NIEM Property (Representation)", "Property" }, { "NIEM Base Type", "BaseType" },
			{ "NIEM Multiplicity", "Multiplicity" }, { "Old XPath", "OldXPath" },
			{ "Old Multiplicity", "OldMultiplicity" }, { "NIEM Mapping Notes", "Notes" } , { "Code List Code=Definition;" , "CodeList"} };
	private static String namespaceDelimiter = ":";
	private static HashMap<String, UmlItem> NiemElements = new HashMap<String, UmlItem>();
	private static HashMap<String, List<UmlClassInstance>> NiemElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static HashMap<String, List<UmlExtraClassMember>> NiemEnumerations = new HashMap<String, List<UmlExtraClassMember>>();
	private static HashMap<String, Namespace> Namespaces = new HashMap<String, Namespace>();
	private static HashMap<String, String> Prefixes = new HashMap<String, String>();
	public static final String niemStereotype = "niem-profile:niem";
	private static HashMap<String, UmlClass> NiemTypes = new HashMap<String, UmlClass>();

	private static HashMap<String, UmlItem> SubsetElements = new HashMap<String, UmlItem>();
	private static HashMap<String, List<UmlClassInstance>> SubsetElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static HashMap<String, List<UmlExtraClassMember>> SubsetEnumerations = new HashMap<String, List<UmlExtraClassMember>>();
	private static HashMap<String, UmlClass> SubsetTypes = new HashMap<String, UmlClass>();

	private static HashMap<String, UmlItem> ExtensionElements = new HashMap<String, UmlItem>();
	private static HashMap<String, List<UmlClassInstance>> ExtensionElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static HashMap<String, List<UmlExtraClassMember>> ExtensionEnumerations = new HashMap<String, List<UmlExtraClassMember>>();
	private static HashMap<String, UmlClass> ExtensionTypes = new HashMap<String, UmlClass>();

	private static String stereotypeDelimiter = ":";
	private static UmlPackage subsetPackage = null, extensionPackage = null, referencePackage = null;
	private static UmlPackage subsetXSDPackage = null, extensionXSDPackage = null, referenceXSDPackage = null;

	private static String xmlPrefix = "xs";
	private static XPath xPath = XPathFactory.newInstance().newXPath();

	// add element to reference model
	public static UmlClassInstance addElement(Document doc, UmlClassView parentClassView, String schemaURI,
			String propertyName, String baseName, String description) {
		String propertyName2 = propertyName.replace("-", "");
		// String propertyName2 = propertyName;
		UmlClassInstance ci = findElement((UmlPackage) (parentClassView.parent()), schemaURI, propertyName2);
		UmlPackage rootPackage = (UmlPackage) (parentClassView.parent());
		if (ci == null) {
			UmlClass base;
			if (baseName == null) // abstract
			{
				if (rootPackage == referencePackage)
					base = referenceAbstractType;
				else
					base = subsetAbstractType;
			} else {
				base = findTypeByPrefix(doc, (UmlPackage) (parentClassView.parent()), baseName);
				if (base == null)
					return null;
			}
			ci = UmlClassInstance.create(parentClassView, propertyName2, base);
			String en = schemaURI + hashDelimiter + propertyName2;
			ci.set_PropertyValue(uriProperty, en);
			ci.set_Description(description);
		}
		return ci;
	}

	// add element to extension
	public static UmlClassInstance addElement(String elementName, String typeName, String description, String notes) {
		// abort if external schame
		if (isExternal(typeName))
			return null;

		// UmlCom.trace("Copying " + elementName + " to subset");
		// get schemaURI
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		// UmlCom.trace("Namespace found");
		// if element already exists in subset, return
		String elementName2 = getName(elementName);
		UmlClassInstance element;
		if (isNiemElement(elementName))
			element = findElement(subsetPackage, schemaURI, elementName2);
		else
			element = findElement(extensionPackage, schemaURI, elementName2);
		if (element != null) {
			// UmlCom.trace("Element " + element + " already exists in subset/extension");
			String currentNotes = element.propertyValue(notesProperty);
			if (currentNotes == null)
				currentNotes = notes;
			else if (!currentNotes.contains(notes))
				currentNotes = currentNotes + "; " + notes;
			if (!currentNotes.equals(""))
				element.set_PropertyValue(notesProperty, currentNotes);
			return element;
		}

		// if namespace doesn't exist, create it
		UmlClassView nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("Subset classview not found for " + schemaURI);
			return null;
		}

		// find base type
		String typePrefix = getPrefix(typeName);
		String typeSchemaURI = Prefixes.get(typePrefix);
		String typeName2 = getName(typeName);
		UmlClass baseType;
		if (isNiemType(typeName))
			baseType = findType(subsetPackage, typeSchemaURI, typeName2);
		else
			baseType = findType(extensionPackage, typeSchemaURI, typeName2);
		if (baseType == null) 
		{
			UmlCom.trace("Base type not found in extension/subset for " + typePrefix + ":" + typeName2);
			return null;
		}

		// create element
		// UmlCom.trace("Copying element " + element + " in subset");
		element = UmlClassInstance.create(nsClassView, elementName2, baseType);
		element.set_Description(description);
		element.set_PropertyValue(uriProperty, schemaURI + hashDelimiter + elementName2);
		if (!notes.equals(""))
			element.set_PropertyValue(notesProperty, notes);

		return element;
	}

	// add element in type to reference model
	public static UmlAttribute addElementInType(Document doc, UmlClassView parentClassView, String schemaURI,
			String typeName, String propertyName, String multiplicity) {
		// abort if external schame
		if (isExternal(typeName) || isExternal(propertyName))
			return null;

		UmlClass type = findType((UmlPackage) (parentClassView.parent()), schemaURI, typeName);
		if (type == null) {
			UmlCom.trace("addElementInType: type " + typeName + " not found");
			return null;
		}
		UmlClassInstance ci = findElementByPrefix(doc, (UmlPackage) (parentClassView.parent()), propertyName);
		if (ci == null) {
			UmlCom.trace("addElementInType: element " + propertyName + " not found");
			return null;
		}
		return addElementInType(type, ci, multiplicity);
	}

	// add element in type to extension
	public static UmlAttribute addElementInType(String typeName, UmlClassInstance element, String multiplicity) {
		// abort if external schame
		if (isExternal(typeName))
			return null;

		if (element == null)
		{
			UmlCom.trace("addElementInType: element not found in type " + typeName);
			return null;
		}
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		// UmlCom.trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		UmlClass type = findType(extensionPackage, schemaURI, typeName2);
		if (type == null) {
			UmlCom.trace("addElementInType: type " + typeName + " not found");
			return null;
		}
		return addElementInType(type, element, multiplicity);
	}

	// add element in type to reference model or extension
	public static UmlAttribute addElementInType(UmlClass type, UmlClassInstance element, String multiplicity) {
		if (type == null) {
			UmlCom.trace("addElementInType: type not found");
			return null;
		}
		if (element == null) {
			UmlCom.trace("addElementInType: element not found");
			return null;
		}
		String propertyName = element.parent().name() + namespaceDelimiter + element.pretty_name();
		UmlAttribute at = null;
		try {
			at = UmlAttribute.create(type, propertyName);
		} catch (Exception re) {
			// UmlCom.trace("addElementInType: element " + element.name() + " already exists in type " + type.name() + re.toString());
		}
		if (at != null) {
			at.set_Description(element.description());
			at.set_PropertyValue(uriProperty, element.propertyValue(uriProperty));
			UmlTypeSpec ct2 = new UmlTypeSpec();
			ct2.type = element.type();
			if (ct2.type != null)
				at.set_Type(ct2);
			at.set_Multiplicity(multiplicity);
		}
		return at;
	}

	// add enumeration to type in reference model
	public static UmlExtraClassMember addEnumeration(UmlClassView parentClassView, String schemaURI, String typeName,
			String propertyName) {
		// String propertyName2 = propertyName.replace("-", "");
		String propertyName2 = propertyName;
		UmlClass type = findType((UmlPackage) (parentClassView.parent()), schemaURI, typeName);
		if (type == null)
			return null;
		UmlExtraClassMember cm = UmlExtraClassMember.create(type, propertyName2);
		return cm;
	}

	// import NIEM and non-NIEM namespaces
	public static UmlClassView addNamespace(UmlPackage parentPackage, String prefix, String schemaURI) {
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null) {
			// create namespace
			// UmlCom.trace("Adding namespace " + schemaURI);
			ns = new Namespace(schemaURI);
			Namespaces.put(schemaURI, ns);
		}
		// remove special characters in prefix names
		String prefix2 = prefix.replaceAll("[^a-zA-Z0-9-_]+","");
		if (!Prefixes.containsKey(prefix2)) {
			// UmlCom.trace("Adding prefix " + prefix2);
			Prefixes.put(prefix, schemaURI);
		}

		if (parentPackage == referencePackage) {
			if (ns.referenceClassView != null)
				return ns.referenceClassView;
		} else if (ns.nsClassView != null) {
			// UmlCom.trace("Classview for subset/extension already exists for
			// schema " + prefix);
			return ns.nsClassView;
		}
		// UmlCom.trace("Creating classview for " + schemaURI);
		UmlClassView namespaceClassView = null;
		try {
			namespaceClassView = UmlClassView.create(parentPackage, prefix2);
			namespaceClassView.set_PropertyValue(uriProperty, schemaURI);
		} catch (Exception e) {
			UmlCom.trace("Multiple namespace URIs for prefix " + prefix2);
			namespaceClassView = UmlClassView.create(parentPackage, prefix2 + "-" + ThreadLocalRandom.current().nextInt());
		}
		if (parentPackage == referencePackage)
			ns.referenceClassView = namespaceClassView;
		else
			ns.nsClassView = namespaceClassView;
		namespaceClassView.set_PropertyValue(uriProperty, schemaURI);

		return namespaceClassView;
	}

	// add type to extension
	public static UmlClass addType(String typeName, String description, String notes) {
		// abort if external schema
		if (isExternal(typeName))
			return null;

		// UmlCom.trace("Copying " + typeName + " to subset");
		// get schemaURI
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		// UmlCom.trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		UmlClass typeClass = findType(extensionPackage, schemaURI, typeName2);
		if (typeClass != null)
		{
			String currentNotes = typeClass.propertyValue(notesProperty);
			if (currentNotes == null)
				currentNotes = notes;
			else if (!currentNotes.contains(notes))
				currentNotes = currentNotes + "; " + notes;
			if (!currentNotes.equals(""))
				typeClass.set_PropertyValue(notesProperty, currentNotes);
			return typeClass;
		}

		// if namespace doesn't exist, create it
		UmlClassView nsClassView = addNamespace(extensionPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("Extension classview not found for " + schemaURI);
			return null;
		}
		// create type
		// UmlCom.trace("Copying type " + typeName + " to subset");
		try {
			typeClass = UmlClass.create(nsClassView, typeName2);
		} catch (RuntimeException re) {
			//UmlCom.trace("addType: type already exists in extension " + typeName);
		}
		if (typeClass != null)
		{
			typeClass.set_Description(description);
			typeClass.set_PropertyValue(uriProperty, schemaURI + hashDelimiter + typeName2);
			if (!notes.equals(""))
				typeClass.set_PropertyValue(notesProperty, notes);
		}
		return typeClass;
	}

	// add type to reference model or extension
	public static UmlClass addType(UmlClassView parentClassView, String schemaURI, String tagName, String description, String notes) {
		String tagName2 = tagName.replace("-", "");
		// String tagName2 = tagName;
		// UmlCom.trace("addType:"  + tagName);
		UmlClass typeClass = findType((UmlPackage) (parentClassView.parent()), schemaURI, tagName2);
		if (typeClass == null) {
			//if (tagName2.equals("abstract"))
			//	UmlCom.trace("findType: abstract");
			try {
				typeClass = UmlClass.create(parentClassView, tagName2);
			} catch (Exception e) {
				UmlCom.trace("addType: type not found " + tagName + " " + e.toString());
			}
			String tn = schemaURI + hashDelimiter + tagName2;
			typeClass.set_PropertyValue(uriProperty, tn);
			if (!notes.equals(""))
				typeClass.set_PropertyValue(notesProperty, notes);
			typeClass.set_Description(description);
		}
		return typeClass;
	}

	// cache NIEM extensions
	public static void cacheModel(UmlPackage rootPackage) {
		// Cache namespaces, types and elements
		String schemaURI;
		HashMap<String, UmlItem> Elements = null;
		HashMap<String, List<UmlClassInstance>> ElementsInType = null;
		HashMap<String, List<UmlExtraClassMember>> Enumerations = null;
		HashMap<String, UmlClass> Types = null;

		if (rootPackage == referencePackage) {
			Elements = NiemElements;
			ElementsInType = NiemElementsInType;
			Enumerations = NiemEnumerations;
			Types = NiemTypes;
		} else if (rootPackage == subsetPackage) {
			Elements = SubsetElements;
			ElementsInType = SubsetElementsInType;
			Enumerations = SubsetEnumerations;
			Types = SubsetTypes;
		}
		if (rootPackage == extensionPackage) {
			Elements = ExtensionElements;
			ElementsInType = ExtensionElementsInType;
			Enumerations = ExtensionEnumerations;
			Types = ExtensionTypes;
		}

		for (UmlItem cv : rootPackage.children()) {
			if (cv.kind() != anItemKind.aClassView)
				continue;

			schemaURI = cv.propertyValue(uriProperty);
			String prefix = cv.pretty_name();
			if (!Prefixes.containsKey(prefix)) {
				//UmlCom.trace("Adding prefix " + prefix);
				Prefixes.put(prefix, schemaURI);
			}
			Namespace ns = Namespaces.get(schemaURI);
			if (ns == null) {
				// create namespace
				ns = new Namespace(schemaURI);
				Namespaces.put(schemaURI, ns);
			}
			if (rootPackage == referencePackage)
				ns.referenceClassView = (UmlClassView) cv;
			else
				ns.nsClassView = (UmlClassView) cv;

			for (UmlItem c : cv.children()) {
				schemaURI = c.propertyValue(uriProperty);
				if (schemaURI != null)
					switch (c.kind().value()) {
					case anItemKind._aClass:
						Types.put(schemaURI, (UmlClass) c);
						break;
					case anItemKind._aClassInstance:
						Elements.put(schemaURI, (UmlClassInstance) c);
						break;
					default:
						break;
					}
			}
		}

		// Cache elements in types
		for (UmlClass c : Types.values()) {
			String cn = c.propertyValue(uriProperty);
			List<UmlClassInstance> enlist = (List<UmlClassInstance>) (ElementsInType.get(cn));
			if (enlist == null) {
				enlist = new ArrayList<UmlClassInstance>();
				ElementsInType.put(cn, enlist);
			}
			for (UmlItem a : c.children())
				if (a.kind() == anItemKind.anAttribute) {
					// UmlCom.trace("Caching " + a.propertyValue(uriProperty));
					UmlClassInstance ci = (UmlClassInstance) Elements.get(a.propertyValue(uriProperty));
					enlist.add(ci);
				}
		}

		if (rootPackage == referencePackage) {
			NiemElements = Elements;
			NiemElementsInType = ElementsInType;
			NiemEnumerations = Enumerations;
			NiemTypes = Types;
			referenceAbstractType = NiemTypes.get(localPrefix + hashDelimiter + abstractTypeName);
			referenceAnyElement = (UmlClassInstance)NiemElements.get(XMLConstants.W3C_XML_SCHEMA_NS_URI + hashDelimiter + anyElementName);
			//referenceAbstractType = findType(referencePackage, localSchemaURI + localPrefix, abstractTypeName);
			//if (referenceAbstractType == null)
			//	UmlCom.trace("cacheModel: reference abstract type not found");
		} else if (rootPackage == subsetPackage) {
			SubsetElements = Elements;
			SubsetElementsInType = ElementsInType;
			SubsetEnumerations = Enumerations;
			SubsetTypes = Types;
			subsetAbstractType = SubsetTypes.get(localPrefix + hashDelimiter + abstractTypeName);
			subsetAugmentationType = SubsetTypes.get(structuresPrefix + hashDelimiter + augmentationTypeName);
			subsetObjectType = SubsetTypes.get(structuresPrefix + hashDelimiter + objectTypeName);
			//subsetAbstractType = findType(subsetPackage, localSchemaURI + localPrefix, abstractTypeName);
			//if (subsetAbstractType == null)
			//	UmlCom.trace("cacheModel: subset abstract type not found");
		}
		if (rootPackage == extensionPackage) {
			ExtensionElements = Elements;
			ExtensionElementsInType = ElementsInType;
			ExtensionEnumerations = Enumerations;
			ExtensionTypes = Types;
		}
	}

	protected static String columnHtml(String value, String bgcolor, String fgcolor) {
		return "<td bgcolor=\"" + bgcolor + "\"><font color = \"" + fgcolor + "\">" + value + "</font></td>";
	}

	// copy element from NIEM reference model to subset
	public static UmlClassInstance copyElement(String elementName) {
		// UmlCom.trace("Copying " + elementName + " to subset");
		// get schemaURI
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("Namespace not found for prefix " + prefix);
			return null;
		}
		// UmlCom.trace("Namespace found");
		// if element already exists in subset, return
		String elementName2 = getName(elementName);
		UmlClassInstance element = findElement(subsetPackage, schemaURI, elementName2);
		if (element != null) {
			// UmlCom.trace("Element " + element + " already exists in subset");
			return element;
		}

		// if type doesn't exist in reference model, return
		UmlClassInstance sourceElement = findElement(referencePackage, schemaURI, elementName2);
		if (sourceElement == null) {
			UmlCom.trace("Element " + schemaURI + ":" + elementName2 + " not found in reference");
			return null;
		}
		// UmlCom.trace("Element found in reference model");

		// if namespace doesn't exist, create it
		UmlClassView nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("Subset classview not found for " + schemaURI);
			return null;
		}

		// find base type
		UmlClass sourceBaseType = sourceElement.type();
		String pt = sourceBaseType.pretty_name();
		String baseSchemaURI = sourceBaseType.parent().propertyValue(uriProperty);
		UmlClass baseType = findType(subsetPackage, baseSchemaURI, pt);
		if (baseType == null)
			if (baseType == null) {
				Namespace ns = Namespaces.get(schemaURI);
				if (ns != null) {
					UmlClassView cv = ns.referenceClassView;
					if (cv != null) {
						String basePrefix = cv.name();
						baseType = copyType(baseSchemaURI, basePrefix, pt);
					}
					if (baseType != null) 
						SubsetTypes.put(baseType.propertyValue(uriProperty), baseType);
				}
				if (baseType == null) {
					UmlCom.trace("Base type not found in subset for " + baseSchemaURI + ":" + pt);
					return null;
				}
			}
		// create element
		// UmlCom.trace("Copying element " + element + " in subset");
		element = UmlClassInstance.create(nsClassView, elementName2, baseType);
		element.set_Description(sourceElement.description());
		element.set_PropertyValue(uriProperty, sourceElement.propertyValue(uriProperty));

		return element;
	}

	// copy element in type from NIEM reference model to subset
	public static UmlAttribute copyElementInType(String typeName, UmlClassInstance element, String multiplicity) {
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			return null;

		// UmlCom.trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		UmlClass type = findType(subsetPackage, schemaURI, typeName2);
		if (type == null) {
			UmlCom.trace("copyElementInType: type " + typeName + " not found");
			return null;
		}
		return copyElementInType(type, element, multiplicity);
	}

	// copy element in type from NIEM reference model to subset
	public static UmlAttribute copyElementInType(UmlClass type, UmlClassInstance element, String multiplicity) {
		/*
		 * if (type == null) { UmlCom.trace("copElementInType: type is null");
		 * return null; } if (element == null) {
		 * UmlCom.trace("copElementInType: element is null"); return null; }
		 */
		// UmlCom.trace("copyElementInType: Adding " + element.pretty_name() + " to type " + type.pretty_name());
		UmlAttribute at = null;
		try {
			at = UmlAttribute.create(type, element.pretty_name());
		} catch (RuntimeException re) {
			// UmlCom.trace("copyElementInType: attribute already exists " + element + " " + re.toString());
		}
		if (at != null) {
			at.set_Description(element.description());
			at.set_PropertyValue(uriProperty, element.propertyValue(uriProperty));
			UmlTypeSpec ct2 = new UmlTypeSpec();
			ct2.type = element.type();
			if (ct2.type != null)
				at.set_Type(ct2);
			at.set_Multiplicity(multiplicity);
		}
		return at;
	}

	// copy type from NIEM reference model to subset
	public static UmlClass copyType(String typeName) {
		// UmlCom.trace("Copying " + typeName + " to subset");
		// get schemaURI
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("copyType: Namespace not found for prefix " + prefix);
			return null;
		}
		// UmlCom.trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		return copyType(schemaURI, prefix, typeName2);
	}

	// copy type from reference model to subset
	public static UmlClass copyType(String schemaURI, String prefix, String typeName2) {
		UmlClass typeClass = findType(subsetPackage, schemaURI, typeName2);
		if (typeClass != null) {
			// UmlCom.trace("Type " + typeName + " already exists in subset");
			return typeClass;
		}

		// if type doesn't exist in reference model, return
		UmlClass sourceType = findType(referencePackage, schemaURI, typeName2);
		if (sourceType == null) {
			UmlCom.trace("copyType: Type " + schemaURI + ":" + typeName2 + " not found in reference");
			return null;
		}
		// UmlCom.trace("Type found in reference model");
		// if namespace doesn't exist, create it
		UmlClassView nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("copyType: Subset classview not found for " + schemaURI);
			return null;
		}
		// UmlCom.trace("Subset classview created " + nsClassView.pretty_name()
		// + " " + nsClassView.propertyValue(uriProperty));
		// create type
		//UmlCom.trace("Copying type " + typeName2 + " to subset schema " + nsClassView.pretty_name());
		try {
			typeClass = UmlClass.create(nsClassView, typeName2);
		} catch (Exception e) {
			UmlCom.trace("copyType: type not found " + typeName2 + " " + e.toString());
		}
		if (typeClass == null)
		{
			UmlCom.trace("copyType: type not found " + typeName2 + " ");
			return null;
		}

		SubsetTypes.put(typeClass.propertyValue(uriProperty), typeClass);
		typeClass.set_Description(sourceType.description());
		typeClass.set_PropertyValue(uriProperty, sourceType.propertyValue(uriProperty));

		// copy base type
		for (UmlItem item : sourceType.children())
		{
			if (item.kind() == anItemKind.aRelation)
			{
				UmlRelation r = (UmlRelation)item;
				if (r.relationKind() == aRelationKind.aGeneralisation)
				{
					UmlClass sourceBaseType = r.roleType();
					String sourceBasePrefix = sourceBaseType.parent().name();
					String sourceBaseSchemaURI = Prefixes.get(sourceBasePrefix);
					String sourceBaseTagName = sourceBaseType.name();
					UmlClass baseType = copyType(sourceBaseSchemaURI, sourceBasePrefix, sourceBaseTagName);
					if (baseType == null)
					{
						UmlCom.trace("copyType: base type not found " + sourceBaseTagName);
						continue;
					}
					SubsetTypes.put(baseType.propertyValue(uriProperty), baseType);
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, typeClass, baseType);
					} catch (Exception re) {
						UmlCom.trace("createSubset: cannot relate " + typeClass + " to " + sourceBaseTagName + " " + re.toString());
					}
				}
			}
		}
		return typeClass;
	}

	// create Platform Independent Model (PIM)
	public static void createPIM(UmlPackage root) {
		UmlPackage pimPackage = null;

		//UmlCom.trace("Creating PIM folders");
		// Find or create PIM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("PIM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			pimPackage = UmlPackage.create(root, "PIM");
			UmlCom.trace("Creating PIM");
		}

		// Find or create package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMSubset"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					subsetPackage = (UmlPackage) ch;
					break;
				}
		}
		if (subsetPackage == null)
		{
			UmlCom.trace("Creating NIEMSubset");
			subsetPackage = UmlPackage.create(pimPackage, "NIEMSubset");
		}
		// Find or create package "NIEMExtension"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMExtension"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					extensionPackage = (UmlPackage) ch;
					break;
				}
		}
		if (extensionPackage == null)
		{
			UmlCom.trace("Creating NIEMExtension");
			extensionPackage = UmlPackage.create(pimPackage, "NIEMExtension");
		}

		// Find or create package "NIEMReference"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMReference"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					referencePackage = (UmlPackage) ch;
					break;
				}
		}
		if (referencePackage == null)
			referencePackage = UmlPackage.create(pimPackage, "NIEMReference");
	}

	// create Platform Specific Model (PSM)
	public static void createPSM(UmlPackage root) {
		UmlCom.trace("Creating PSM");
		UmlPackage psmPackage = null;

		// Find or create PSM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("PSM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					psmPackage = (UmlPackage) ch;
					break;
				}
		}
		if (psmPackage == null)
			psmPackage = UmlPackage.create(root, "PSM");

		// Find or create package "NIEMSubsetXSD"
		for (UmlItem ch : psmPackage.children()) {
			if (ch.pretty_name().equals("NIEMSubsetXSD"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					subsetXSDPackage = (UmlPackage) ch;
					break;
				}
		}
		if (subsetXSDPackage == null)
			subsetXSDPackage = UmlPackage.create(psmPackage, "NIEMSubsetXSD");

		// Find or create package "NIEMExtensionXSD"
		for (UmlItem ch : psmPackage.children()) {
			if (ch.pretty_name().equals("NIEMExtensionXSD"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					extensionXSDPackage = (UmlPackage) ch;
					break;
				}
		}
		if (extensionXSDPackage == null)
			extensionXSDPackage = UmlPackage.create(psmPackage, "NIEMExtensionXSD");

		// Find or create package "NIEMReferenceXSD"
		for (UmlItem ch : psmPackage.children()) {
			if (ch.pretty_name().equals("NIEMReferenceXSD"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					referenceXSDPackage = (UmlPackage) ch;
					break;
				}
		}
		if (referenceXSDPackage == null)
			referenceXSDPackage = UmlPackage.create(psmPackage, "NIEMReferenceXSD");
	}

	// create NIEM subset and extension
	public static void createSubset(String extensionURI) {
		extensionSchemaURI = extensionURI;

		// String[] nextLine = new String[map.length];

		//UmlCom.trace("Creating subset");
		// cache NIEM namespaces, elements and types
		//UmlCom.trace("createSubset: Cache reference model");
		cacheModel(referencePackage);
		//UmlCom.trace("createSubset: Cache subset model");
		cacheModel(subsetPackage);
		//UmlCom.trace("createSubset: Cache extension model");
		cacheModel(extensionPackage);

		// add abstract types
		if (subsetAbstractType == null)
		{
			String localUri = localPrefix;
			UmlClassView cv = addNamespace(subsetPackage, localPrefix, localUri);
			subsetAbstractType = addType(cv, cv.propertyValue(uriProperty), abstractTypeName, "abstract type", "");
			if (subsetAbstractType != null)
				SubsetTypes.put(subsetAbstractType.propertyValue(uriProperty), subsetAbstractType);
			//subsetAbstractType = copyType("local:abstract");
		}

		// Copy subset base types
		//UmlCom.trace("createSubset: Copy subset base types");
		if (subsetObjectType == null)
		{
			subsetObjectType = copyType(structuresPrefix + namespaceDelimiter + objectTypeName);
			if (subsetObjectType != null)
				SubsetTypes.put(subsetObjectType.propertyValue(uriProperty), subsetObjectType);
		}
		if (subsetAugmentationType == null)
		{
			subsetAugmentationType = copyType(structuresPrefix + namespaceDelimiter + augmentationTypeName);
			if (subsetAugmentationType != null)
				SubsetTypes.put(subsetAugmentationType.propertyValue(uriProperty), subsetAugmentationType);
		}
		UmlClass type;
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String baseTypeName = c.propertyValue(niemProperty(7));
				if (!baseTypeName.equals("") && !baseTypeName.equals("??") && !isExternal(baseTypeName)) 
				{
					if (isNiemType(baseTypeName)) 
					{
						// UmlCom.trace("Adding type " + baseTypeName + " to subset");
						type = copyType(baseTypeName);
						if (type != null)
							SubsetTypes.put(type.propertyValue(uriProperty), type);
					}
				}
			}
		}

		// Copy subset types and create extension types
		//UmlCom.trace("createSubset: Copy subset types and create extension types");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String notes = c.propertyValue(niemProperty(11)).trim();
				String description = c.description().trim();

				if (!typeName.equals("") && !typeName.equals("??") && !isExternal(typeName)) {
					if (isNiemType(typeName)) {
						//UmlCom.trace("Adding type " + typeName + " to subset");
						type = copyType(typeName);
						if (type != null)
							SubsetTypes.put(type.propertyValue(uriProperty), type);
					} else {
						// UmlCom.trace("Adding type " + typeName + " to extension");
						String prefix = getPrefix(typeName);
						String schemaURI = Prefixes.get(prefix);
						Namespace ns = findNamespace(schemaURI);
						if (ns != null && ns.referenceClassView != null)
						{
							UmlCom.trace("createSubset: type " + typeName + " not found in reference model");
							continue;
						}
						type = addType(typeName, description, notes);
						if (type != null)
						{
							String uri = type.propertyValue(uriProperty);
							// UmlCom.trace("Added " + uri + " to extension");
							ExtensionTypes.put(uri, type);
						}
					}
				}
			}
		}

		// Copy subset elements and create extension elements
		//UmlCom.trace("createSubset: Copy subset elements and create extension elements");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String elementName = c.propertyValue(niemProperty(6)).trim();
				String baseTypeName = c.propertyValue(niemProperty(7)).trim();
				String multiplicity = c.propertyValue(niemProperty(8)).trim();
				String description = c.description().trim();
				String mappingNotes = c.propertyValue(niemProperty(11)).trim();
				String codeList = c.propertyValue(niemProperty(12)).trim();

				if (!elementName.equals("") && !elementName.equals("??") && !isExternal(elementName)) {
					//	String elementName2 = elementName.replace(" ", "").replace("(", "").replace(")", "");
					String[] elements = elementName.split(",");
					String headElement = null;
					Boolean substitution = elementName.contains("(");
					for (String e : elements) 
					{
						String e1 = e.trim();
						String e2 = e1;
						if (substitution && headElement == null)
							headElement = e2;
						boolean representation = false;
						if (e2.startsWith("(") && e2.endsWith(")"))
						{
							representation = true;
							e2 = e2.substring(1,e2.length()-1);
						}
						if (isNiemElement(e2)) {
							//UmlCom.trace("Adding element " + e2 + " in subset");
							UmlClassInstance element = copyElement(e2);
							if (element == null) {
								UmlCom.trace("createSubset: could not create element " + e2);
								continue;
							}
							SubsetElements.put(element.propertyValue(uriProperty), element);
							if (substitution && representation && headElement != null)
								element.set_PropertyValue(substitutionProperty, headElement);
							if (!representation && !typeName.equals("") && isNiemElementInType(typeName, e2)) {
								//UmlCom.trace("Adding element " + e2 + " in type " + typeName + " in subset");
								String cn = element.parent().propertyValue(uriProperty);
								List<UmlClassInstance> enlist = (List<UmlClassInstance>) (SubsetElementsInType.get(cn));
								if (enlist == null) {
									enlist = new ArrayList<UmlClassInstance>();
									SubsetElementsInType.put(cn, enlist);
								}
								copyElementInType(typeName, element, multiplicity);
							}
						} else {
							//UmlCom.trace("Adding element " + e + " in extension");
							String prefix = getPrefix(e2);
							String schemaURI = Prefixes.get(prefix);
							String baseTagName = baseTypeName.trim();
							if (substitution && !representation)
								baseTagName = "abstract";
							Namespace ns = findNamespace(schemaURI);
							if (ns != null && ns.referenceClassView != null)
							{
								UmlCom.trace("createSubset: element " + e2 + " not found in reference model");
								continue;
							}
							if (baseTagName.equals(""))
							{
								UmlCom.trace("createSubset: base type not found for element " +  e2);
								continue;
							}
							UmlClassInstance ci = addElement(e2, baseTagName, description, mappingNotes);
							if (ci == null) {
								UmlCom.trace("createSubset: could not create element " + e2);
								continue;
							}
							ExtensionElements.put(ci.propertyValue(uriProperty), ci);
							if (substitution && representation && headElement != null)
								ci.set_PropertyValue(substitutionProperty, headElement);
							if (!codeList.equals(""))
								ci.set_PropertyValue(codeListProperty, codeList);
						}
					}
				}
			}
		}

		// Create extension base types
		//UmlCom.trace("createSubset: Copy subset base types and create extension base types");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String elementName = c.propertyValue(niemProperty(6)).trim();
				String baseTypeName = c.propertyValue(niemProperty(7)).trim();

				if (!typeName.equals("") && !typeName.equals("??") && !baseTypeName.equals("") && elementName.equals("")) 
				{
					String tagName = getName(typeName);
					String prefix = getPrefix(typeName);
					String schemaURI = Prefixes.get(prefix);
					String baseTagName = getName(baseTypeName);
					String basePrefix = getPrefix(baseTypeName);
					String baseSchemaURI = Prefixes.get(basePrefix);
					UmlClass baseType;
					if (isNiemType(typeName) || isExternal(typeName))
						continue;
					type = findType(extensionPackage, schemaURI, tagName);
					if (type == null)
					{
						UmlCom.trace("createSubset: type not found " + typeName);
						continue;
					}						
					if (isNiemType(baseTypeName))
						baseType = findType(subsetPackage, baseSchemaURI, baseTagName);
					else
						baseType = findType(extensionPackage, baseSchemaURI, baseTagName);
					if (baseType == null)
					{
						UmlCom.trace("createSubset: base type not found " + baseTypeName);
						continue;
					}
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, type, baseType);
					} catch (Exception re) {
						//UmlCom.trace("createSubset: " + typeName + " already related to " + baseTypeName + " " + re.toString());
					}
				}

				// Add generalizations for extension augmentations
				if (typeName.endsWith(augmentationTypeName)) 
				{
					String tagName = getName(typeName);
					String prefix = getPrefix(typeName);
					String schemaURI = Prefixes.get(prefix);
					if (isNiemType(typeName) || isExternal(typeName))
						continue;
					type = findType(extensionPackage, schemaURI, tagName);
					if (type == null)
					{
						UmlCom.trace("createSubset: type not found " + typeName);
						continue;
					}	
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, type, subsetAugmentationType);
					} catch (Exception re) {
						//UmlCom.trace("createSubset: " + typeName + " already related to " + baseTypeName + " " + re.toString());
					}
				}
			}
		}

		// Add extension elements in type
		//UmlCom.trace("createSubset: Add extension elements in type");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String elementName = c.propertyValue(niemProperty(6)).trim();
				String multiplicity = c.propertyValue(niemProperty(8)).trim();

				if (!typeName.equals("") && !isNiemType(typeName)) {
					if (!elementName.equals("") && !elementName.equals("??")) {
						String[] elements = elementName.split(",");
						for (String e : elements) {
							String e1 = e.trim();
							if (e1.startsWith("(") && e1.endsWith(")"))
								continue;
							//UmlCom.trace("Adding element " + e1 + " in type " + typeName + " in subset");
							UmlClassInstance element;
							if (isNiemElement(e1))
								element = findElement(subsetPackage, e1);
							else
								element = findElement(extensionPackage, e1);
							if (element != null)
							{
								String cn = element.parent().propertyValue(uriProperty);
								List<UmlClassInstance> enlist = (List<UmlClassInstance>) (ExtensionElementsInType.get(cn));
								if (enlist == null) {
									enlist = new ArrayList<UmlClassInstance>();
									ExtensionElementsInType.put(cn, enlist);
								}
								enlist.add(element);
								addElementInType(typeName, element, multiplicity);
							}
						}
					}
				}
			}
		}

		// Sorting
		UmlCom.trace("Sorting namespaces");
		subsetPackage.sort();
		extensionPackage.sort();
	}

	// reset NIEM mappings
	public static void deleteMapping() {
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (item.stereotype().equals(niemStereotype))
				for (int p = 4; p < map.length; p++)
					item.set_PropertyValue(niemProperty(p), "");
		}
	}

	// delete PIM model
	public static void deletePIM(UmlPackage root) {
		UmlPackage pimPackage = null;

		//UmlCom.trace("Deleting PIM");
		deleteSubset(root);

		// Delete reference model
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("PIM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					//pimPackage.deleteIt();
					break;
				}
		}
		// Find PIM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("PIM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			UmlCom.trace("PIM not found");
			return;
		}

		// Delete children of package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMReference"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					referencePackage = (UmlPackage) ch;
					//UmlCom.trace("Deleting NIEMExtension");
					//subsetPackage.deleteIt();
					break;
				}
		}
		for (UmlItem item : referencePackage.children())
			item.deleteIt();
	}

	// delete PIM model
	public static void deleteSubset(UmlPackage root) {
		UmlPackage pimPackage = null;

		//UmlCom.trace("Deleting Subset");

		// Find PIM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("PIM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			UmlCom.trace("PIM not found");
			return;
		}

		// Delete children of package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMSubset"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					subsetPackage = (UmlPackage) ch;
					//UmlCom.trace("Deleting NIEMSubset");
					//subsetPackage.deleteIt();
					break;
				}
		}
		for (UmlItem item : subsetPackage.children())
			item.deleteIt();

		// Delete package "NIEMExtension"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMExtension"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					extensionPackage = (UmlPackage) ch;
					//UmlCom.trace("Deleting NIEMExtension");
					//extensionPackage.deleteIt();
					break;
				}
		}
		for (UmlItem item : extensionPackage.children())
			item.deleteIt();
	}

	// generate Genericode code list
	public static void exportCodeList(String dir, String elementName, String codeListURI, String codeList, String version)
	{
		try {
			FileWriter fw;

			// export catalog file
			fw = new FileWriter(dir + "/" + elementName + ".gc");
			fw.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>");
			fw.write("<gc:CodeList xmlns:ct=\"http://release.niem.gov/niem/conformanceTargets/3.0/\" xmlns:gc=\"http://docs.oasis-open.org/codelist/ns/genericode/1.0/\" xmlns:gca=\"http://example.org/namespace/genericode-appinfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://docs.oasis-open.org/codelist/ns/genericode/1.0/ https://docs.oasis-open.org/codelist/cs-genericode-1.0/xsd/genericode.xsd\">");
			fw.write("<Annotation><AppInfo><gca:ConformanceTargets ct:conformanceTargets=\"http://reference.niem.gov/niem/specification/code-lists/1.0/#GenericodeCodeListDocument\"/></AppInfo></Annotation>");
			fw.write("<Identification><ShortName>" + elementName + "</ShortName>");
			fw.write("<Version>" + version + "</Version>");
			fw.write("<CanonicalUri>" + codeListURI + "</CanonicalUri>");
			fw.write("<CanonicalVersionUri>" + codeListURI + "/" + version + "</CanonicalVersionUri>");
			fw.write("</Identification>");
			fw.write("<ColumnSet>");
			fw.write("<Column Id=\"code\" Use=\"required\"><ShortName>code</ShortName>");
			fw.write("<CanonicalUri>http://reference.niem.gov/niem/specification/code-lists/1.0/column/code</CanonicalUri>");
			fw.write("<Data Type=\"normalizedString\" Lang=\"en\"/></Column>");
			fw.write("<Column Id=\"definition\" Use=\"optional\"><ShortName>definition</ShortName>");
			fw.write("<CanonicalUri>http://reference.niem.gov/niem/specification/code-lists/1.0/column/definition</CanonicalUri>");
			fw.write("<Data Type=\"normalizedString\" Lang=\"en\"/></Column>");
			fw.write("<Key Id=\"codeKey\"><ShortName>CodeKey</ShortName><ColumnRef Ref=\"code\"/></Key>");
			fw.write("</ColumnSet>");
			fw.write("<SimpleCodeList>");

			String[] codes = codeList.split(";");
			for (String code : codes)
			{
				String[] pairs = code.split("=");
				if (pairs.length != 2)
				{
					UmlCom.trace("exportCodeList: invalid code list value " + code);
					continue;
				}
				String value = pairs[0].trim();
				String definition = pairs[1].trim();
				fw.write("<Row><Value ColumnRef=\"code\"><SimpleValue>" + value + "</SimpleValue></Value>");
				fw.write("<Value ColumnRef=\"definition\"><SimpleValue>" + definition + "</SimpleValue></Value></Row>");
			}
			fw.write("</SimpleCodeList></gc:CodeList>");
			fw.close();

		} catch (IOException e) {
			UmlCom.trace("exportCodeList: IO exception: " + e.toString());
		} catch (RuntimeException e) {
			UmlCom.trace("exportCodeList: Runtime Exception: " + e.toString());
		}

	}

	// generate NIEM mapping spreadsheet in CSV format
	// roundtripping is supported with importCsv()
	public static void exportCsv(String dir, String filename) {
		UmlItem.directory = dir;

		try {
			FileWriter fw = new FileWriter(dir + "/" + filename);
			CSVWriter writer = new CSVWriter(fw);

			// Write header
			String[] nextLine = new String[map.length];
			for (int i = 0; i < map.length; i++)
				nextLine[i] = map[i][0];
			writer.writeNext(nextLine);

			// Export NIEM Mappings for Classes
			for (int i = 0; i < UmlClass.classes.size(); i++) {
				UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);

				if (c.stereotype().equals(niemStereotype)) {
					nextLine = itemCsv(c);
					// UmlCom.trace("0:" + nextLine[0] + " 1:" + nextLine[1] + "
					// 2:" + nextLine[2] + " 3:" + nextLine[3] );
					writer.writeNext(nextLine);

					// Export NIEM Mapping for Attributes and Relations
					for (UmlItem ch : c.children())
						if (ch.stereotype().equals(niemStereotype)) {
							nextLine = itemCsv(ch);
							if (nextLine != null) {
								// UmlCom.trace("0:" + nextLine[0] + " 1:" +
								// nextLine[1] + " 2:" + nextLine[2] + " 3:" +
								// nextLine[3] );
								writer.writeNext(nextLine);
							}
						}
				}
			}
			writer.close();

		} catch (Exception e) {
			UmlCom.trace("exportCsv: exception " + e.toString());
		}
	}

	// generate NIEM mapping spreadsheet in HTML format
	public static void exportHtml(String dir, String filename) {
		// cache NIEM namespaces, elements and types
		cacheModel(referencePackage);
		try {
			// Write rest of header
			FileWriter fw = new FileWriter(dir + "/" + filename + ".html");
			fw.write("<html>");
			fw.write(
					"<head><title>NIEM Mapping</title><link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /></head>");
			fw.write("<body><div class = \"title\">NIEM Mapping</div>");
			fw.write("<table><tr bgcolor=\"#f0f0f0\">");
			for (int i = 0; i < map.length; i++)
				fw.write("<td>" + map[i][0] + "</td>");
			fw.write("</tr>\n");

			// Show NIEM Mappings for Classes
			for (int i = 0; i < UmlClass.classes.size(); i++) {
				UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);
				if (c.stereotype().equals(niemStereotype)) {
					writeLineHtml(fw, c);

					// Show NIEM Mapping for Attributes and Relations
					for (UmlItem ch : c.children())
						if (ch.stereotype().equals(niemStereotype))
							writeLineHtml(fw, ch);
				}
			}
			fw.write("</table>\n");
			fw.write("</body></html>");
			fw.close();
		} catch (Exception e) {
			UmlCom.trace("exportHtml: exception " + e.toString());
		}
	}

	// generate extension and exchange schema
	public static void exportSchema(String dir) {

		cacheModel(referencePackage);
		cacheModel(subsetPackage);
		cacheModel(extensionPackage);

		try {
			FileWriter fw;

			// export catalog file
			fw = new FileWriter(dir + "/xml-catalog.xml");
			fw.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n");
			fw.write("<!DOCTYPE catalog PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\" \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\n");
			fw.write("<catalog prefer=\"public\" xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n");
			for (Entry<String, String> entry : Prefixes.entrySet())
			{
				String prefix = entry.getKey();
				String schemaURI = Prefixes.get(prefix);
				Namespace ns = Namespaces.get(schemaURI);
				if (ns.referenceClassView == null)
					fw.write("<uri name=\"" + schemaURI + "\" uri=\"" + prefix + ".xsd\"/>\n");
			}
			fw.write("<nextCatalog  catalog=\"codes/xml-catalog.xml\" />\n");
			fw.write("<nextCatalog  catalog=\"Subset/niem/xml-catalog.xml\" />\n");
			fw.write("</catalog>\n");
			fw.close();

			// export each schema
			for (UmlItem item: extensionPackage.children())
				if (item.kind() == anItemKind.aClassView)
				{
					UmlClassView cv = (UmlClassView)item;
					cv.sort();
					String prefix = cv.name();
					String schemaURI = cv.propertyValue(uriProperty);

					// build list of referenced namespaces
					HashSet<String> RefNamespaces = new HashSet<String>();
					RefNamespaces.add(xmlPrefix);
					RefNamespaces.add(structuresPrefix);
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass)
						{
							UmlClass c = (UmlClass)item2;
							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.aRelation)
								{
									UmlRelation r = (UmlRelation)item3;
									if (r.relationKind() == aRelationKind.aGeneralisation)
									{
										UmlClass baseType = r.roleType();
										RefNamespaces.add(baseType.parent().name());
										break;
									}
								}
							for (UmlItem item4 : c.children())
								if (item4.kind() == anItemKind.anAttribute)
								{
									UmlAttribute a = (UmlAttribute)item4;
									String elementUri = a.propertyValue(uriProperty);
									UmlClassInstance ci;
									if (SubsetElements.containsKey(elementUri))
										ci = (UmlClassInstance)SubsetElements.get(elementUri);
									else
										ci = (UmlClassInstance)ExtensionElements.get(elementUri);
									RefNamespaces.add(ci.parent().name());
								}
						}
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance)
						{
							UmlClassInstance ci = (UmlClassInstance)item2;
							UmlClass baseType = ci.type();
							RefNamespaces.add(baseType.parent().name());
							String headElement = ci.propertyValue(substitutionProperty);
							if (headElement != null)
								RefNamespaces.add(getPrefix(headElement));
						}

					// Open file for each extension schema and write header
					fw = new FileWriter(dir + "/" + prefix + ".xsd");
					//UmlCom.trace("exportSchema: schema " + prefix + ".xsd");
					//fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					fw.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n");
					fw.write("<!-- NIEM extension schema generated by BOUML niemtools plug_out -->\n");
					fw.write("<xs:schema targetNamespace=\"" + schemaURI + "\"\n");

					// export namespace definitions
					fw.write(" xmlns=\"" + cv.propertyValue(uriProperty) + "\"\n");
					fw.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
					for (String nsPrefix : RefNamespaces)
						fw.write(" xmlns:" + nsPrefix + "=\"" + Prefixes.get(nsPrefix) + "\"");

					// export schemaLocation
					//fw.write(" xsi:schemaLocation = \"");
					//for (String nsPrefix : RefNamespaces)
					//  fw.write(Prefixes.get(nsPrefix) + " " + nsPrefix + ".xsd" + " ");
					//fw.write("\"");

					fw.write(" version=\"1\"");
					fw.write(" xmlns:clsa=\"http://reference.niem.gov/niem/specification/code-lists/1.0/code-lists-schema-appinfo/\"");
					fw.write(" xmlns:ct=\"http://release.niem.gov/niem/conformanceTargets/3.0/\"");
					fw.write(" ct:conformanceTargets=\"http://reference.niem.gov/niem/specification/naming-and-design-rules/3.0/#ReferenceSchemaDocument http://reference.niem.gov/niem/specification/code-lists/1.0/#SchemaDocument\"");

					// close top level element
					fw.write(">\n");

					// export import namespaces
					for (String nsPrefix : RefNamespaces)
					{
						String nsSchemaURI = Prefixes.get(nsPrefix);
						if (!nsSchemaURI.equals(schemaURI) && !nsSchemaURI.equals(localPrefix) && !nsSchemaURI.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
						{
							//if (nsPrefix.equals("j"))
							//	fw.write("<xs:import namespace= \"" + Prefixes.get(nsPrefix) + "\" schemaLocation=\"Subset/niem/domains/jxdm/5.2/jxdm.xsd\"/>\n");
							//else if (nsPrefix.equals("nc"))
							//	fw.write("<xs:import namespace= \"" + Prefixes.get(nsPrefix) + "\" schemaLocation=\"Subset/niem/niem-core/3.0/niem-core.xsd\"/>\n");
							//else
							//	fw.write("<xs:import namespace= \"" + Prefixes.get(nsPrefix) + "\" schemaLocation=\"" + nsPrefix + ".xsd\"/>\n");
							fw.write("<xs:import namespace= \"" + nsSchemaURI + "\"/>");
						}
					}
					// export types
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass)
						{
							UmlClass c = (UmlClass)item2;
							String typeName = c.name();
							String description = c.description();
							String baseTypeName = "";
							String mappingNotes = c.propertyValue(notesProperty);
							if (mappingNotes != null && !mappingNotes.equals(""))
								fw.write("<!--" + mappingNotes + "-->");
							fw.write("<xs:complexType name=\"" + typeName + "\">\n");
							fw.write("<xs:annotation>\n");
							fw.write("<xs:documentation>" + description + "</xs:documentation>\n");
							fw.write("</xs:annotation>\n");
							fw.write("<xs:complexContent>\n");
							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.aRelation)
								{
									UmlRelation r = (UmlRelation)item3;
									if (r.relationKind() == aRelationKind.aGeneralisation)
									{
										UmlClass baseType = r.roleType();
										baseTypeName = baseType.parent().name() + namespaceDelimiter + baseType.name();
										break;
									}
								}
							//if (typeName.endsWith("AugmentationType"))
							//	baseTypeName = "structures:AugmentationType";
							if (baseTypeName.equals(""))
								UmlCom.trace("exportSchema: type " + prefix + ":" + typeName + " has no base type");
							fw.write("<xs:extension base=\"" + baseTypeName + "\">\n");
							fw.write("<xs:sequence>\n");
							c.sortChildren();
							for (UmlItem item4 : c.children())
								if (item4.kind() == anItemKind.anAttribute)
								{
									UmlAttribute a = (UmlAttribute)item4;
									String elementUri = a.propertyValue(uriProperty);
									UmlClassInstance ci;
									if (SubsetElements.containsKey(elementUri))
										ci = (UmlClassInstance)SubsetElements.get(elementUri);
									else
										ci = (UmlClassInstance)ExtensionElements.get(elementUri);
									String elementName = ci.parent().name() + namespaceDelimiter + ci.name();
									String multiplicity = a.multiplicity();
									String minoccurs, maxoccurs;
									if ((multiplicity.equals("")))
										minoccurs = maxoccurs = "1";
									else if (multiplicity.contains(",")) {
										String[] occurs = multiplicity.split(",");
										minoccurs = occurs[0];
										maxoccurs = occurs[1];
									} else
										minoccurs = maxoccurs = multiplicity;
									String elementMappingNotes = a.propertyValue(notesProperty);
									if (elementMappingNotes != null && !elementMappingNotes.equals(""))
										fw.write("<!--" + elementMappingNotes + "-->");
									if (elementName.equals("xs:any"))
										fw.write("<xs:any/>");
									else										
										fw.write("<xs:element ref=\"" + elementName + "\" minOccurs=\"" + minoccurs + "\" maxOccurs=\"" + maxoccurs + "\"/>\n");
								}
							//	fw.write("<xs:element ref=\"" + elementName + "\" minOccurs=\"" + minoccurs + "\" maxOccurs=\"" + maxoccurs + "\"/>");
							fw.write("</xs:sequence>\n");
							fw.write("</xs:extension>\n");
							fw.write("</xs:complexContent>\n");
							fw.write("</xs:complexType>\n");
						}

					// export elements
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance)
						{
							UmlClassInstance ci = (UmlClassInstance)item2;
							String elementName = ci.name();
							String description = ci.description();
							UmlClass baseType = ci.type();
							String baseTypeName = baseType.parent().name() + namespaceDelimiter + baseType.name();
							String mappingNotes = ci.propertyValue(notesProperty);
							String headElement = ci.propertyValue(substitutionProperty);
							String codeList = ci.propertyValue(codeListProperty);
							if (mappingNotes != null && !mappingNotes.equals(""))
								fw.write("<!--" + mappingNotes + "-->");
							if (baseType == subsetAbstractType)
								fw.write("<xs:element name=\"" + elementName + "\" abstract=\"true\">\n");
							else if (headElement != null)
								fw.write("<xs:element name=\"" + elementName + "\" type=\"" + baseTypeName + "\" substitutionGroup=\"" + headElement + "\" nillable=\"true\">\n");
							else
								fw.write("<xs:element name=\"" + elementName + "\" type=\"" + baseTypeName + "\" nillable=\"true\">\n");
							fw.write("<xs:annotation>\n");
							fw.write("<xs:documentation>" + description + "</xs:documentation>\n");
							if (codeList != null) 
							{
								String codeListURI = extensionSchema(elementName);
								fw.write("<xs:appinfo>");
								fw.write("<clsa:SimpleCodeListBinding codeListURI=\"" + codeListURI + "\" columnName=\"code\"/>");
								fw.write("</xs:appinfo>");
								exportCodeList(dir, elementName, codeListURI, codeList, "5.0");
							}
							fw.write("</xs:annotation>\n");
							fw.write("</xs:element>\n");
						}
					fw.write("</xs:schema>\n");
					fw.close();
				}
		} catch (IOException e) {
			UmlCom.trace("exportSchema: IO exception: " + e.toString());
		}
	}


	// generate NIEM wantlist for import into Subset Schema Generator Tool (SSGT)
	public static void exportWantlist(String dir, String filename) {

		//createSubset();

		UmlItem.directory = dir;
		try {
			// Export schema
			//UmlCom.trace("exportWantlist: create header");
			FileWriter fw = new FileWriter(dir + "/" + filename);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write("<!-- NIEM Wantlist generated by BOUML niemtools plug_out -->\n");
			fw.write("<w:WantList w:release=\"3.2\" w:product=\"NIEM\" w:nillableDefault=\"true\" ");
			for (UmlItem item: subsetPackage.children())
				if (item.kind() == anItemKind.aClassView)
				{
					String prefix = item.name();
					String schemaURI = Prefixes.get(prefix);
					if (!prefix.equals(localPrefix))
						fw.write(" xmlns:" + prefix+ "=\"" + schemaURI + "\"");
				}
			fw.write(" xmlns:w=\"http://niem.gov/niem/wantlist/2.2\">\n");

			// export elements
			for (UmlItem item: subsetPackage.children())
				if (item.kind() == anItemKind.aClassView)
				{
					UmlClassView cv = (UmlClassView)item;
					String prefix = cv.name();
					if (prefix.equals(localPrefix))
						continue;
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance)
						{
							UmlClassInstance ci = (UmlClassInstance)item2;
							String elementName = ci.name();
							if (prefix.equals(xmlPrefix) && elementName.equals(anyElementName))
								continue;
							//UmlCom.trace("exportWantlist: export element " + elementName);
							fw.write("<w:Element w:name=\"" + prefix + namespaceDelimiter + elementName
									+ "\" w:isReference=\"false\" w:nillable=\"false\"/>\n");
						}
				}

			// export types
			for (UmlItem item: subsetPackage.children())
				if (item.kind() == anItemKind.aClassView)
				{
					UmlClassView cv = (UmlClassView)item;
					String prefix = cv.name();
					if (prefix.equals(localPrefix))
						continue;
					//String schemaURI = Prefixes.get(prefix);
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass)
						{
							UmlClass c = (UmlClass)item2;
							String typeName = c.name();
							//UmlCom.trace("exportWantlist: export type " + typeName);
							if (prefix.equals(structuresPrefix) && typeName.equals(augmentationTypeName))
								continue;
							fw.write("<w:Type w:name=\"" + prefix + namespaceDelimiter + typeName + "\" w:isRequested=\"true\">\n");

							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.anAttribute)
								{
									UmlAttribute a = (UmlAttribute)item3;
									String uri = a.propertyValue(uriProperty);
									UmlClassInstance ci = (UmlClassInstance)SubsetElements.get(uri);
									String elementPrefix = ci.parent().name();
									String elementName = a.name();
									//UmlCom.trace("exportWantlist: adding attribute " + elementName);
									String multiplicity = a.multiplicity();
									//UmlCom.trace("exportWantlist: with multiplicity " + multiplicity);
									String minoccurs, maxoccurs;
									if ((multiplicity.equals("")))
										minoccurs = maxoccurs = "1";
									else if (multiplicity.contains(",")) {
										String[] occurs = multiplicity.split(",");
										minoccurs = occurs[0];
										maxoccurs = occurs[1];
									} else
										minoccurs = maxoccurs = multiplicity;
									//if (((!minoccurs.equals("0") && !minoccurs.equals("1"))) || ((!maxoccurs.equals("1") && !maxoccurs.equals("unbounded"))))
									//	UmlCom.trace("createSubset: unusual multiplicity " + multiplicity + " for element " + elementName);;

									//UmlCom.trace("exportWantlist: export element " + elementName + " in type " + typeName);
									fw.write("\t<w:ElementInType w:name=\"" + elementPrefix + namespaceDelimiter + elementName
											+ "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs
											+ "\" w:maxOccurs=\"" + maxoccurs + "\"/>\n");
								}
							fw.write("</w:Type>");
						} 
				}

			fw.write("</w:WantList>");
			fw.close();

		} catch (IOException e) {
			UmlCom.trace("exportWantlist: IO exception: " + e.toString());
		}
	}

	// return extension schema URI
	public static String extensionSchema(String prefix)
	{
		return extensionSchemaURI + prefix;
	}

	// get element by schemaURI and tagname
	public static UmlClassInstance findElement(UmlPackage parentPackage, String elementName) {
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		String tagName = getName(elementName);
		return findElement(parentPackage, schemaURI, tagName);
	}

	// get element by schemaURI and tagname
	public static UmlClassInstance findElement(UmlPackage parentPackage, String schemaURI, String tagName) {
		String tagName2 = tagName.replace("-", "");
		String uri = schemaURI + hashDelimiter + tagName2;
		if (parentPackage == referencePackage)
			return (UmlClassInstance) NiemElements.get(uri);
		else if (parentPackage == subsetPackage)
			return (UmlClassInstance) SubsetElements.get(uri);
		else if (parentPackage == extensionPackage)
			return (UmlClassInstance) ExtensionElements.get(uri);
		return null;
	}

	// get element by prefix and tagname
	public static UmlClassInstance findElementByPrefix(Document doc, UmlPackage parentPackage, String tagName) {
		String prefix = getPrefix(tagName);
		String schemaURI;
		if (prefix.equals(""))
			schemaURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else
			schemaURI = doc.lookupNamespaceURI(prefix);
		String typeName = getName(tagName);

		UmlClassInstance ci = findElement(parentPackage, schemaURI, typeName);
		if (ci == null) {
			UmlCom.trace("findElementsByPrefix: cannot find element " + tagName);
		}
		return ci;
	}

	// get namespace by schemaURI
	public static Namespace findNamespace(String schemaURI) {
		return Namespaces.get(schemaURI);
	}

	// get type by schemaURI and tagname
	public static UmlClass findType(UmlPackage parentPackage, String schemaURI, String tagName) 
	{
		// UmlCom.trace("findType: " + schemaURI + ":" + tagName);
		if (tagName.equals("abstract"))
		{
			if (parentPackage == referencePackage)
				return referenceAbstractType;
			else
				return subsetAbstractType;
		}
		String tagName2 = tagName.replace("-", "");
		String uri = schemaURI + hashDelimiter + tagName2;

		if (parentPackage == referencePackage)
			return (UmlClass) NiemTypes.get(uri);
		else if (parentPackage == subsetPackage)
			return (UmlClass) SubsetTypes.get(uri);
		else if (parentPackage == extensionPackage) {
			return (UmlClass) ExtensionTypes.get(uri);
		}
		return null;
	}

	// get type by prefix and tagname
	public static UmlClass findTypeByPrefix(Document doc, UmlPackage parentPackage, String tagName) {
		if (tagName.equals("abstract"))
			if (parentPackage == referencePackage)
				return referenceAbstractType;
			else
				return subsetAbstractType;
		String prefix = getPrefix(tagName);
		String schemaURI;
		if (prefix.equals(""))
			schemaURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else if (prefix.equals(localPrefix))
			schemaURI = localPrefix;
		else
			schemaURI = doc.lookupNamespaceURI(prefix);
		String typeName = getName(tagName);

		return findType(parentPackage, schemaURI, typeName);
	}

	// extract tagname from XML tag
	public static String getName(String typeName) {
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i >= 0) ? typeName.substring(i + 1) : typeName;
	}

	// extract namespace prefix from XML tag
	public static String getPrefix(String typeName) {
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i >= 0) ? typeName.substring(0, i) : "";
	}

	// import NIEM mapping spreadsheet in CSV format
	public static void importCsv(String filename) {
		// Cache CIM classes
		HashMap<String, UmlClass> CIMClasses = new HashMap<String, UmlClass>();
		HashMap<String, UmlClassInstance> CIMInstances = new HashMap<String, UmlClassInstance>();
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem item = (UmlItem)UmlItem.all.elementAt(i);
			if (item.stereotype().equals(niemStereotype))
				if (item.kind() == anItemKind.aClass)
				{
					UmlClass c = (UmlClass)item;
					if (!CIMClasses.containsKey(c.pretty_name()))
						CIMClasses.put(c.pretty_name(), c);
				} else if (item.kind() == anItemKind.aClassInstance)
				{
					UmlClassInstance ci = (UmlClassInstance)item;
					if (!CIMInstances.containsKey(ci.pretty_name()))
						CIMInstances.put(ci.pretty_name(), ci);
				}
		}
		try {

			CSVReader reader = new CSVReader(new FileReader(filename));
			String[] nextLine;

			// Read header
			reader.readNext();

			// NIEM Read Mappings
			while ((nextLine = reader.readNext()) != null) {
				String className = nextLine[0].trim();
				String attributeName = nextLine[1].trim();

				if (!className.equals(""))
				{
					UmlClass c = CIMClasses.get(className);
					if (c != null) 
					{
						if (attributeName.equals("")) 
						{
							// Import NIEM Mapping to Class
							UmlCom.trace("Importing NIEM mapping for " + className);
							for (int p = 4; p < map.length && p < nextLine.length; p++)
								c.set_PropertyValue(niemProperty(p), nextLine[p]);
						} else {
							// Import NIEM Mapping to Attribute
							for (UmlItem item : c.children())
								if (item.stereotype().equals(niemStereotype) && (item.pretty_name().equals(attributeName)))
									for (int p = 4; p < map.length && p < nextLine.length; p++)
										item.set_PropertyValue(niemProperty(p), nextLine[p]);
						}
					}
				} else if (!attributeName.equals(""))
				{
					UmlClassInstance ci = CIMInstances.get(attributeName);
					if (ci != null)
					{
						// Import NIEM Mapping to Class
						UmlCom.trace("Importing NIEM mapping for " + attributeName);
						for (int p = 4; p < map.length && p < nextLine.length; p++)
							ci.set_PropertyValue(niemProperty(p), nextLine[p]);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			UmlCom.trace("importCsv: File not found" + e.toString());
		} catch (IOException e) {
			UmlCom.trace("importCsv: IO exception" + e.toString());
		}
	}

	// import NIEM reference model elements into HashMaps
	public static void importElements(DocumentBuilder db, String filename) {
		// UmlCom.trace("Importing elements from schema " + filename);
		String fn = "\n" + filename + "\n";
		try {
			// parse the schema
			Document doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			Namespace ns = importNamespaces(doc);

			// compile XPath queries
			XPathExpression xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import elements
			NodeList list = (NodeList) xPath.evaluate("xs:element[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element e = (Element) list.item(i);
				String en = e.getAttribute("name");
				String et = e.getAttribute("type");
				if (et.equals(""))
					et = localPrefix + namespaceDelimiter + abstractTypeName;
				try {
					UmlClassInstance ci = addElement(doc, ns.referenceClassView, ns.schemaURI, en, et, xe.evaluate(e));
					if (ci != null)
						NiemElements.put(ci.propertyValue(uriProperty), ci);
				} catch (Exception re) {
					UmlCom.trace(fn + "importElements: cannot create element " + en + " of type " + et + " "
							+ re.toString());
					fn = "";
				}
			}
		} catch (Exception e) {
			UmlCom.trace(fn + "importElements: " + e.toString());
			fn = "";
		}
	}

	// import NIEM reference model elements in Types into HashMaps
	public static void importElementsInTypes(DocumentBuilder db, String filename) {
		// UmlCom.trace("Importing elements in types from schema " + filename);
		String fn = "\n" + filename + "\n";
		Document doc = null;
		Namespace ns = null;
		try {
			// parse the schema
			doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = importNamespaces(doc);
		} catch (Exception e) {
			UmlCom.trace(filename + "\nimportElementsInTypes: parse the schema " + e.toString());
		}

		Node root = null;
		NodeList list = null;
		String en = null;
		// import base types for simple types (codes)
		try {
			root = doc.getDocumentElement();
			list = (NodeList) xPath.evaluate("xs:simpleType[@name]/xs:restriction[1][@base]", root,
					XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element r = (Element) list.item(i);
				Element s = (Element) r.getParentNode();
				en = s.getAttribute("name");
				String pt = r.getAttribute("base");

				UmlClass c = findType((UmlPackage) (ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null) {
					UmlCom.trace(fn + "importElementsInType: type not found: " + en);
					fn = "";
					continue;
				}
				UmlClass p = findTypeByPrefix(doc, (UmlPackage) (ns.referenceClassView.parent()), pt);
				if (p == null) {
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt);
					fn = "";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation, c, p);
				} catch (Exception re) {
					UmlCom.trace(fn = "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString());
					fn = "";
				}
			}
		} catch (Exception e) {
			UmlCom.trace(filename + "\nimportElementsInTypes: import base types for smple types " + e.toString());
		}

		// import base types for complex types (codes)
		try {
			list = (NodeList) xPath.evaluate("xs:complexType[@name]/xs:simpleContent[1]/xs:extension[1][@base]", root,
					XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element e = (Element) list.item(i);
				Element ct = (Element) e.getParentNode().getParentNode();
				en = ct.getAttribute("name");
				String pt = e.getAttribute("base");

				UmlClass c = findType((UmlPackage) (ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null) {
					UmlCom.trace(fn + "importElementsInType: type not found: " + en);
					fn = "";
					continue;
				}
				UmlClass p = findTypeByPrefix(doc, (UmlPackage) (ns.referenceClassView.parent()), pt);
				if (p == null) {
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt);
					fn = "";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation, c, p);
				} catch (Exception re) {
					UmlCom.trace(fn + "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString());
					fn = "";
				}
			}
		} catch (Exception e) {
			UmlCom.trace(filename + "\nimportElementsInTypes: base types for complex types" + e.toString());
		}

		// import base types and elements for complex types
		try {
			list = (NodeList) xPath.evaluate("xs:complexType[@name]/xs:complexContent[1]/xs:extension[1][@base]", root,
					XPathConstants.NODESET);
		} catch (Exception e) {
			UmlCom.trace(filename + "\nimportElementsInTypes: pase base types and elements" + e.toString());
		}
		for (int i = 0; i < list.getLength(); i++) {
			Element e = null;
			try {

				e = (Element) list.item(i);
				if (e == null)
					continue;
				Element ct = (Element) e.getParentNode().getParentNode();
				en = ct.getAttribute("name");
				String pt = e.getAttribute("base");

				UmlClass c = findType((UmlPackage) (ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null) {
					UmlCom.trace(fn + "importElementsInType: type not found: " + en);
					fn = "";
					continue;
				}
				UmlClass p = findTypeByPrefix(doc, (UmlPackage) (ns.referenceClassView.parent()), pt);
				if (p == null) {
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt);
					fn = "";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation, c, p);
				} catch (Exception re) {
					UmlCom.trace(fn + "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString());
					fn = "";
				}
			} catch (Exception re) {
				UmlCom.trace(filename + "\nimportElementsInTypes: import base types and elements  " + re.toString());
			}

			// import elements in type
			try {
				String uri = ns.schemaURI + hashDelimiter + en;
				List<UmlClassInstance> enlist = (List<UmlClassInstance>) (NiemElementsInType
						.get(uri));
				if (enlist == null) {
					enlist = new ArrayList<UmlClassInstance>();
					NiemElementsInType.put(uri, enlist);
				}
				NodeList elist = (NodeList) xPath.evaluate("xs:sequence[1]/xs:element[@ref]", e,
						XPathConstants.NODESET);
				if (elist == null)
					continue;
				for (int j = 0; j < elist.getLength(); j++) {
					Element e2 = (Element) elist.item(j);
					String et = e2.getAttribute("ref");
					String minOccurs = e2.getAttribute("minOccurs");
					if (minOccurs.equals(""))
						minOccurs = "1";
					String maxOccurs = e2.getAttribute("maxOccurs");
					if (maxOccurs.equals(""))
						maxOccurs = "1";
					String multiplicity = minOccurs + "," + maxOccurs;
					UmlAttribute a = null;
					try {
						a = addElementInType(doc, ns.referenceClassView, ns.schemaURI, en, et, multiplicity);
					} catch (Exception re) {
						UmlCom.trace(fn + "importElementsInTypes: cannot create element " + et + " in type " + en + " "
								+ re.toString());
						fn = "";
					}
					if (a == null) {
						UmlCom.trace(fn + "importElementsInTypes: cannot create element " + et + " in type " + en);
						continue;
					}
					UmlClassInstance ci = null;
					try {
						ci = findElementByPrefix(doc, (UmlPackage) (ns.referenceClassView.parent()), et);
					} catch (Exception re) {
						UmlCom.trace(fn + "importElementsInTypes: cannot find element " + et + " " + re.toString());
						fn = "";
					}
					if (ci == null) {
						UmlCom.trace(fn + "importElementsInTypes: cannot find element " + et);
						fn = "";
						continue;
					}
					try {
						UmlTypeSpec ct2 = new UmlTypeSpec();
						ct2.type = ci.type();
						if (ct2.type != null)
							a.set_Type(ct2);
					} catch (Exception re) {
						UmlCom.trace(filename + "\nimportElementsInTypes: set description and type " + re.toString());
					}
					enlist.add(ci);
				}
			}

			catch (Exception re) {
				UmlCom.trace(filename + "\nimportElementsInTypes: import elements in type " + re.toString());
			}
		}
	}

	// import NIEM reference model elements in Types into HashMaps
	public static void importEnumerations(DocumentBuilder db, String filename) {
		// UmlCom.trace("Importing enumerations from schema " + filename);
		String fn = "\n" + filename + "\n";
		try {
			// parse schema
			Document doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			Namespace ns = importNamespaces(doc);

			// compile XPath queries
			XPathExpression xe1 = xPath.compile("xs:enumeration");
			XPathExpression xe2 = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import enumerated values for simple types (codes)
			NodeList list = (NodeList) xPath.evaluate("xs:simpleType[@name]/xs:restriction[1]/[ns:enumeration]",
					doc.getDocumentElement(), XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element r = (Element) list.item(i);
				Element s = (Element) r.getParentNode();
				String en = s.getAttribute("name");

				// import elements in type
				List<UmlExtraClassMember> cmlist = (List<UmlExtraClassMember>) (NiemEnumerations
						.get(ns.schemaURI + hashDelimiter + en));
				if (cmlist == null) {
					cmlist = new ArrayList<UmlExtraClassMember>();
					NiemEnumerations.put(ns.schemaURI + hashDelimiter + en, cmlist);
				}
				NodeList elist = (NodeList) xe1.evaluate(r, XPathConstants.NODESET);
				for (int j = 0; j < elist.getLength(); j++) {
					Element e = (Element) elist.item(j);
					String et = e.getAttribute("value");
					try {
						UmlExtraClassMember cm = addEnumeration(ns.referenceClassView, ns.schemaURI, en, et);
						cm.set_Description(xe2.evaluate(e));
						cmlist.add(cm);
					} catch (Exception re) {
						UmlCom.trace(fn + "importEnumerations: cannot create value " + et + " in type " + en + " "
								+ re.toString());
						fn = "";
					}
				}
			}
		} catch (Exception e) {
			UmlCom.trace(fn + "importEnumerations: " + e.toString());
			fn = "";
		}
	}

	// import namespaces and return target namespace
	public static Namespace importNamespaces(Document doc) {
		// reset prefixes
		// Prefixes.clear();

		NamedNodeMap nslist = doc.getDocumentElement().getAttributes();
		for (int i = 0; i < nslist.getLength(); i++) {
			Node attr = nslist.item(i);
			String aname = attr.getNodeName();
			if (aname.startsWith("xmlns")) {
				String prefix = (aname.equals("xmlns")) ? attr.getNodeValue() : aname.substring(6);
				addNamespace(referencePackage, prefix, attr.getNodeValue());
			}
		}

		// get target namespace
		Namespace ns = null;
		try {
			String schemaURI = xPath.evaluate("xs:schema/@targetNamespace", doc);
			ns = Namespaces.get(schemaURI);
			if (ns == null)
				return findNamespace(localPrefix);

			// set namespace description
			ns.referenceClassView
			.set_Description(xPath.evaluate("xs:schema/xs:annotation[1]/xs:documentation[1]", doc));
		} catch (NullPointerException re) {
			UmlCom.trace("importTypes: null pointer ");
		} catch (Exception e) {
			UmlCom.trace("importNamespaces: " + e.toString());
		}
		return ns;
	}

	// import NIEM reference model into HashMaps to support validation of NIEM
	// elements and types
	public static void importSchemaDir(String dir, Boolean includeEnums) throws IOException {
		// cache reference model
		cacheModel(referencePackage);

		// import abstract types
		UmlClassView cv;
		if (referenceAbstractType == null)
		{
			String localUri = localPrefix;
			cv = addNamespace(referencePackage, localPrefix, localUri);
			referenceAbstractType = addType(cv, cv.propertyValue(uriProperty), abstractTypeName, "abstract type", "");
			NiemTypes.put(referenceAbstractType.propertyValue(uriProperty), referenceAbstractType);
		}

		// import XML namespace and simple types
		cv = addNamespace(referencePackage, xmlPrefix, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		String[] xmlTypeNames = { "anyURI", "base64Binary", "blockSet", "boolean", "byte", "date", "dateTime",
				"decimal", "derivationControl", "derivationSet", "double", "duration", "ENTITIES", "ENTITY", "float",
				"formChoice", "fullDerivationSet", "gDay", "gMonth", "gMonthDay", "gYear", "gYearMonth", "hexBinary",
				"int", "integer", "language", "long", "Name", "namespaceList", "NCName", "negativeInteger", "NMTOKEN",
				"NMTOKENS", "nonNegativeInteger", "nonPositiveInteger", "normalizedString", "NOTATION",
				"positiveInteger", "public", "QName", "short", "simpleDerivationSet", "string", "time", "token",
				"unsignedByte", "unsignedInt", "unsignedLong", "unsignedShort" };
		for (String s : xmlTypeNames) {
			// UmlClass c = UmlClass.create(cv, s);
			// NiemTypes.put(XMLConstants.W3C_XML_SCHEMA_NS_URI + hashDelimiter
			// + s, c);
			UmlClass type = addType(cv, XMLConstants.W3C_XML_SCHEMA_NS_URI, s, "", "");
			if (type != null)
				NiemTypes.put(type.propertyValue(uriProperty), type);
		}

		// import xs:any element
		if (referenceAnyElement == null)
		{
			referenceAnyElement = addElement(null, cv, XMLConstants.W3C_XML_SCHEMA_NS_URI, anyElementName, null, "");
			if (referenceAnyElement != null)
				NiemElements.put(referenceAnyElement.propertyValue(uriProperty), referenceAnyElement);
		}

		// Configure DOM
		Path path = FileSystems.getDefault().getPath(dir);

		int passes = (includeEnums) ? 4 : 3;

		// Walk directory to import in passes (1: types, 2: elements, 3:
		// elements in types, 4: enumerations
		for (importPass = 0; importPass < passes; importPass++) {
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
						UmlCom.trace("importSchemaDir: could not configure parser " + e.toString());
					}

					String filename = file.toString();
					if (filename.endsWith(".xsd"))
						switch (NiemTools.importPass) {
						case 0:
							importTypes(db, filename);
							break;
						case 1:
							importElements(db, filename);
							break;
						case 2:
							importElementsInTypes(db, filename);
							break;
							/*
							 * case 3: importEnumerations(db, filename); break;
							 */
						}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		// Sorting
		UmlCom.trace("Sorting namespaces");
		referencePackage.sort();

		UmlCom.trace("Namespaces: " + Namespaces.size());
		UmlCom.trace("Types: " + NiemTypes.size());
		UmlCom.trace("Elements: " + NiemElements.size());
	}

	// import NIEM reference model types into HashMaps
	public static void importTypes(DocumentBuilder db, String filename) {
		// UmlCom.trace("Importing types from schema " + filename);
		String fn = "\n" + filename + "\n";
		try {
			// parse the schema
			Document doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			Namespace ns = importNamespaces(doc);

			// compile XPath queries
			XPathExpression xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import types
			NodeList list = (NodeList) xPath.evaluate("xs:complexType|xs:simpleType[@name]", doc.getDocumentElement(),
					XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element e = (Element) list.item(i);
				String nodeType = e.getNodeName();
				String en = e.getAttribute("name");
				try {
					UmlClass c = addType(ns.referenceClassView, ns.schemaURI, en, xe.evaluate(e), "");
					if (c == null) {
						UmlCom.trace("importTypes: cannot create type " + en);
						continue;
					}
					NiemTypes.put(c.propertyValue(uriProperty), c);
					if (nodeType == "xs:simpleType")
						c.set_Stereotype("enum_pattern");
				} catch (NullPointerException re) {
					UmlCom.trace(fn + "importTypes: null pointer " + en);
					fn = "";
				} catch (Exception re) {
					UmlCom.trace(fn + "importTypes: cannot create type " + en + " " + re.toString());
					fn = "";
				}
			}
		} catch (NullPointerException re) {
			UmlCom.trace(fn + "importTypes: null pointer ");
			fn = "";
		} catch (Exception re) {
			UmlCom.trace(fn + "importTypes: " + re.toString());
			fn = "";
		}
	}

	// identify UBL types and elements
	public static Boolean isExternal(String tagName) {
		String prefix = getPrefix(tagName);
		return (prefix.equals("cac") || prefix.equals("cbc") || prefix.equals("ds"));
	}

	// indicate whether an element exists in reference model
	public static Boolean isNiemElement(String elementName) {

		if (elementName.equals("") || elementName.equals("??") || isExternal(elementName))
			return false;

		// UmlCom.trace("isNiemElement: Find element " + elementName);
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("isNiemElement: Cannot find prefix " + prefix);
			return false;
		}
		String tagName = getName(elementName);
		String tagName2 = tagName.replace("-", "");
		// UmlCom.trace("isNiemElement Searching for element " + schemaURI +
		// hashDelimiter + tagName2);
		return NiemElements.containsKey(schemaURI + hashDelimiter + tagName2);
	}

	// get type by prefix and tagname
	public static Boolean isNiemElementInType(String typeName, String elementName) {
		//UmlCom.trace("isNiemElement: is element " + elementName + " in type " + typeName + "?");
		if (!isNiemType(typeName) || !isNiemElement(elementName))
			return false;

		// parse type
		String typePrefix = getPrefix(typeName);
		String typeSchemaURI = Prefixes.get(typePrefix);
		if (typeSchemaURI == null)
		{
			UmlCom.trace("isNiemElementInType: Cannot find prefix " + typePrefix + "in element " + typeName);
			return false;
		}
		String typeTagName = getName(typeName);

		// parse property
		String elementPrefix = getPrefix(elementName);
		String elementSchemaURI = Prefixes.get(elementPrefix);
		if (elementSchemaURI == null) {
			UmlCom.trace("isNiemElementInType: Cannot find prefix " + elementPrefix + "in element " + elementName);
			return false;
		}
		String tagName = getName(elementName);
		String tagName2 = tagName.replace("-", "");
		String uri = elementSchemaURI + hashDelimiter + tagName2;
		UmlClassInstance ci = (UmlClassInstance) NiemElements.get(uri);
		if (ci == null) {
			UmlCom.trace("isNiemElementInType: element not found: " + uri);
			return false;
		}
		List<UmlClassInstance> list = (List<UmlClassInstance>) (NiemElementsInType
				.get(typeSchemaURI + hashDelimiter + typeTagName));
		if (list == null)
		{
			UmlCom.trace("isNiemElementInType: elements not found in type " + typeTagName);
			return false;
		}
		for (UmlClassInstance ci2 : list)
			if (ci.equals(ci2))
				return true;

		UmlCom.trace("isNiemElementInType: element " + elementName + " not found in type " + typeName);
		return false;
	}

	// indicate whether an XML prefix matches a NIEM namespace
	public static boolean isNiemSchema(String prefix) {
		if (prefix == null || prefix.equals(""))
			return false;
		return (Prefixes.containsKey(prefix));
	}

	// output one column of the NIEM mapping spreadsheet in HTML format
	// protected static String columnHtml(String newValue, String oldValue,
	// String container, String schema) throws IOException

	// indicate whether a type exists in reference model
	public static Boolean isNiemType(String typeName) {
		if (typeName.equals("") || typeName.equals("??") || isExternal(typeName))
			return false;

		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			return false;
		String tagName = getName(typeName);
		String tagName2 = tagName.replace("-", "");
		return NiemTypes.containsKey(schemaURI + hashDelimiter + tagName2);
	}

	// output one line of the NIEM mapping spreadsheet in CSV format
	public static String[] itemCsv(UmlItem item) {
		String[] nextLine = new String[map.length];
		try {
			// Export Class and Property
			switch (item.kind().value()) {
			case anItemKind._aClass:
				nextLine[0] = item.pretty_name();
				nextLine[1] = "";
				nextLine[2] = "";
				break;
			case anItemKind._anAttribute:
				nextLine[0] = item.parent().pretty_name();
				nextLine[1] = item.pretty_name();
				UmlAttribute a = (UmlAttribute) item;
				nextLine[2] = a.multiplicity();
				break;
			case anItemKind._aRelation:
				nextLine[0] = item.parent().pretty_name();
				UmlRelation r = (UmlRelation) item;
				nextLine[1] = r.pretty_name();
				nextLine[2] = r.multiplicity();
				break;
			case anItemKind._aClassInstance:
				nextLine[0] = "";
				nextLine[1] = item.pretty_name();
				nextLine[2] = "";
				break;
			default:
				nextLine[0] = item.parent().pretty_name();
				nextLine[1] = item.pretty_name();
				nextLine[2] = "";
				break;
			}
		} catch (Exception e) {
			UmlCom.trace("itemCsv: class, property multiplicity " + e.toString());
		}

		// Export Description
		nextLine[3] = item.description();

		// Export NIEM Mapping
		if (item.stereotype().equals(niemStereotype))
			// String prefix =
			// getPrefix((String)(item.propertyValue(niemStereotype+":Type")));
			for (int p = 4; p < map.length; p++)
				nextLine[p] = item.propertyValue(niemProperty(p));

		return nextLine;
	}

	// return the NIEM steteotype associated with a column in the NIEM mapping
	// spreadsheet
	public static String niemProperty(int p) {
		return niemStereotype + stereotypeDelimiter + map[p][1];
	}

	// output UML objects
	/*	public static void outputUML() {
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlClass.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (item.known) {
				UmlCom.trace("\nKind: " + String.valueOf(item.kind().value()));
				UmlCom.trace("ID: " + String.valueOf(item.getIdentifier()));
				UmlCom.trace("Name: " + item.pretty_name());
				if (item.parent() != null) {
					UmlCom.trace("Parent: " + String.valueOf(item.parent().getIdentifier()));
					UmlCom.trace("Parent Name: " + String.valueOf(item.parent().pretty_name()));
				}
			} else
				UmlCom.trace("\nName: " + item.pretty_name());
		}
	}*/

	// (re-)associate the NIEM stereotype with all properties in the CIM
	public static void resetStereotype() {
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = (UmlItem) it.next();
			if (item.stereotype().equals("niem:niem")) {
				item.set_Stereotype(niemStereotype);
				item.applyStereotype();
			}
		}
	}

	public static void writeItemHtml(FileWriter fw, UmlItem item) throws IOException {
		if (item.known) {
			fw.write("<a href=\"");
			if (!UmlItem.flat && (item.parent() != null) && (item.parent().kind() == anItemKind.aClass)) {
				fw.write("class");
				fw.write(String.valueOf(item.parent().getIdentifier()));
			} else
				fw.write("index");
			fw.write(".html#ref");
			fw.write(String.valueOf(item.kind().value()));
			fw.write('_');
			fw.write(String.valueOf(item.getIdentifier()));
			fw.write("\"><b>");
			fw.write(item.pretty_name());
			fw.write("</b></a>");
		} else
			fw.write(item.pretty_name());
	}

	// output a line of the NIEM mapping spreadhseet in HTML format
	public static void writeLineHtml(FileWriter fw, UmlItem item) {
		try {
			// Export Class, Property and Multiplicity
			// UmlCom.trace(item.pretty_name());
			switch (item.kind().value()) {
			case anItemKind._aClass: {
				fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
				writeItemHtml(fw, item);
				fw.write("</td><td>");
				fw.write("</td><td>");
			}
			break;
			case anItemKind._anAttribute: {
				fw.write("<tr><td>");
				writeItemHtml(fw, item.parent());
				fw.write("</td><td>");
				writeItemHtml(fw, item);
				fw.write("</td><td>");
				UmlAttribute a = (UmlAttribute) item;
				fw.write(a.multiplicity());
			}
			break;
			case anItemKind._aRelation: {
				UmlRelation rel = (UmlRelation) item;
				if ((rel.relationKind() == aRelationKind.aGeneralisation)
						|| (rel.relationKind() == aRelationKind.aRealization))
					return;
				else {
					fw.write("<tr><td>");
					writeItemHtml(fw, item.parent());
					fw.write("</td><td>");
					writeItemHtml(fw, item);
					fw.write("</td><td>");
					fw.write(rel.multiplicity());
				}
			}
			break;
			default:
				return;
			}
			fw.write("</td><td>");

			// Export Description
			if (item.description() != null)
				fw.write(item.description());
			fw.write("</td>");

			// Export NIEM Mapping
			int p;
			// String oldValue, container;
			String[] column = new String[map.length];
			String extensionBGColor = "#ffd700";
			String defaultBGColor = "#ffffff";
			String invalidFGColor = "#ff0000"; // invalid NIEM mappings are red
			String changedFGColor = "#0000ff"; // changes from the previous
			// version are blue
			String defaultFGColor = "#000000";
			String fgcolor, bgcolor;

			if (item.stereotype().equals(niemStereotype)) {
				for (p = 4; p < map.length; p++) {
					column[p] = (String) (item.propertyValue(niemProperty(p)));
					column[p] = (column[p] != null) ? column[p].trim() : "";
				}

				// export XPath
				String prefix = getPrefix(column[5]);
				Boolean extension = ((prefix != null) && (!prefix.equals("")) && (!isNiemSchema(prefix)));
				bgcolor = (extension) ? extensionBGColor : defaultBGColor;
				fgcolor = (column[4].equals(column[9])) ? defaultFGColor : changedFGColor;
				fw.write(columnHtml(column[4], bgcolor, fgcolor));

				// export Type
				fgcolor = defaultFGColor;
				if (!extension) {
					String[] tt = column[5].split(",");
					for (String ttt : tt) {
						ttt = ttt.trim();
						if (!isNiemType(ttt))
							fgcolor = invalidFGColor;
					}
				}
				fw.write(columnHtml(column[5], bgcolor, fgcolor));

				// export Property
				fgcolor = defaultFGColor;
				if ((!column[5].equals("") && (!extension))) {
					String[] pp = column[6].split(",");
					for (String ppp : pp) {
						ppp = ppp.trim();
						Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
						if (!mat.find())
							if (!isNiemElementInType(column[5], ppp))
								fgcolor = invalidFGColor;
					}
				}
				fw.write(columnHtml(column[6], bgcolor, fgcolor));

				// export BaseType
				fgcolor = defaultFGColor;
				if (!extension) {
					String[] bb = column[7].split(",");
					for (String bbb : bb) {
						bbb = bbb.trim();
						if (!isNiemType(bbb))
							fgcolor = invalidFGColor;
					}
				}
				fw.write(columnHtml(column[7], bgcolor, fgcolor));

				// export Multiplicity
				fgcolor = (column[8].equals(column[10])) ? defaultFGColor : changedFGColor;
				fw.write(columnHtml(column[8], bgcolor, fgcolor));

				// export Old XPath
				fgcolor = defaultFGColor;
				fw.write(columnHtml(column[9], bgcolor, fgcolor));

				// export Old Multiplicity
				fw.write(columnHtml(column[10], bgcolor, fgcolor));

				// export NIEM Mapping Notes
				fw.write(columnHtml(column[11], bgcolor, fgcolor));
			}

			fw.write("</tr>");
		} catch (Exception e) {
			UmlCom.trace("writeLineHtml: " + e.toString());
		}
	}
}
