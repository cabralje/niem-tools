
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
	//private static Boolean _TRACE = true;
	private static Boolean _TRACE = false;
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
	private static String niemDir = "niem";
	public static int importPass;
	public static String importPath;
	private static String filepathProperty = "path";
	private static String localPrefix = "local";
	private static String structuresPrefix = "structures";
	private static String extensionSchemaURI = "http://local/";
	private static String notesProperty = "Notes";
	private static String nillableProperty = "isNillable";
	private static String uriProperty = "URI";
	private static String substitutionProperty = "substitutesFor";
	private static String codeListProperty = "codeList";
	private static String codeListDelimiter = ";";
	private static String codeListDefDelimiter = "=";
	public static final String niemStereotype = "niem-profile:niem";
	private static String stereotypeDelimiter = ":";
	private static String WSDLPrefix = "WSDL";
	private static String WSDLXSDFile = "MessageWrappers";
	private static String xmlPrefix = "xs";
	private static Set<String> externalPrefixes = new HashSet<String>();
	private static Map<String, String> externalSchemaURL = new HashMap<String, String>();

	// NIEM mapping spreadsheet column headings, NIEM profile profile stereotype
	private static final String[][] map = { { "Model Class", "", }, { "Model Attribute", "", },
			{ "Model Multiplicity", "", }, { "Model Definition", "", }, { "NIEM XPath", "XPath" },
			{ "NIEM Type", "Type" }, { "NIEM Property, @Reference, (Representation)", "Property" },
			{ "NIEM Base Type", "BaseType" }, { "NIEM Multiplicity", "Multiplicity" }, { "Old XPath", "OldXPath" },
			{ "Old Multiplicity", "OldMultiplicity" }, { "NIEM Mapping Notes", "Notes" },
			{ "Code List Code=Definition;", "CodeList" } };
	private static String namespaceDelimiter = ":";
	private static Map<String, UmlItem> NiemElements = new HashMap<String, UmlItem>();
	private static Map<String, List<UmlClassInstance>> NiemElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static Map<String, Namespace> Namespaces = new HashMap<String, Namespace>();
	private static Map<String, String> Prefixes = new HashMap<String, String>();
	private static Map<String, UmlClass> NiemTypes = new HashMap<String, UmlClass>();
	private static Map<String, List<UmlClassInstance>> Substitutions = new HashMap<String, List<UmlClassInstance>>();

	private static Map<String, UmlItem> SubsetElements = new HashMap<String, UmlItem>();
	private static Map<String, List<UmlClassInstance>> SubsetElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static Map<String, UmlClass> SubsetTypes = new HashMap<String, UmlClass>();

	private static Map<String, UmlItem> ExtensionElements = new HashMap<String, UmlItem>();
	private static Map<String, List<UmlClassInstance>> ExtensionElementsInType = new HashMap<String, List<UmlClassInstance>>();
	private static Map<String, UmlClass> ExtensionTypes = new HashMap<String, UmlClass>();

	private static UmlPackage subsetPackage = null, extensionPackage = null, referencePackage = null;

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
		// if (isExternal(typeName))
		// return null;

		trace("Copying " + elementName + " to subset");
		// get schemaURI
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		trace("Namespace found");
		// if element already exists in subset, return
		String elementName2 = getName(elementName);
		UmlClassInstance element;
		if (isNiemElement(elementName))
			element = findElement(subsetPackage, schemaURI, elementName2);
		else
			element = findElement(extensionPackage, schemaURI, elementName2);
		if (element != null) {
			trace("Element " + element + " already exists in subset/extension");
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
		UmlClassView nsClassView;
		if (isNiemElement(elementName))
			nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		else
			nsClassView = addNamespace(extensionPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("Subset/extension classview not found for " + schemaURI);
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
		if (baseType == null) {
			if (!isExternalPrefix(typePrefix))
				UmlCom.trace(
						"Base type not found in extension/subset for " + typePrefix + namespaceDelimiter + typeName2);
			baseType = subsetAbstractType;
			// return null;
		}

		// create element
		trace("Copying element " + element + " in subset");
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
		// if (isExternal(typeName) || isExternal(propertyName))
		// return null;

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
		// if (isExternal(typeName))
		// return null;

		if (element == null) {
			UmlCom.trace("addElementInType: element not found in type " + typeName);
			return null;
		}
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		trace("Namespace found");
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
		String propertyName2 = propertyName.replace("-", "");
		UmlAttribute at = null;
		for (UmlItem item : type.children()) {
			if (item.kind() == anItemKind.anAttribute && item.name().equals(propertyName2)) {
				at = (UmlAttribute) item;
				String previousMultiplicity = at.multiplicity();
				if (!previousMultiplicity.equals(multiplicity))
					UmlCom.trace("addElementInType:  " + type.parent().name() + namespaceDelimiter + type.pretty_name()
							+ "/" + element.parent().name() + ":" + element.pretty_name()
							+ " has conflicting multiplicities " + previousMultiplicity + " and " + multiplicity);
				return at;
			}
		}
		if (at == null)
			try {
				at = UmlAttribute.create(type, propertyName2);
			} catch (Exception re) {
				trace("addElementInType: element " + element.name() + " already exists in type " + type.name()
						+ re.toString());
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
		String propertyName2 = propertyName.replace("\"", "&quot;");
		// String propertyName2 = propertyName;
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
			trace("Adding namespace " + schemaURI);
			ns = new Namespace(schemaURI);
			Namespaces.put(schemaURI, ns);
		}
		// remove special characters in prefix names
		String prefix2 = prefix.replaceAll("[^a-zA-Z0-9-_]+", "");
		if (!Prefixes.containsKey(prefix2)) {
			trace("Adding prefix " + prefix2);
			Prefixes.put(prefix2, schemaURI);
		}

		if (parentPackage == referencePackage) {
			if (ns.referenceClassView != null)
				return ns.referenceClassView;
		} else if (ns.nsClassView != null) {
			trace("Classview for subset/extension already exists for schema " + prefix);
			return ns.nsClassView;
		}
		trace("Creating classview for " + schemaURI);
		UmlClassView namespaceClassView = null;
		try {
			namespaceClassView = UmlClassView.create(parentPackage, prefix2);
			namespaceClassView.set_PropertyValue(uriProperty, schemaURI);
		} catch (Exception e) {
			UmlCom.trace("Multiple namespace URIs for prefix " + prefix2 + " " + schemaURI + " and "
					+ Prefixes.get(prefix2));
			namespaceClassView = UmlClassView.create(parentPackage,
					prefix2 + "-" + ThreadLocalRandom.current().nextInt());
		}
		if (parentPackage == referencePackage)
			ns.referenceClassView = namespaceClassView;
		else
			ns.nsClassView = namespaceClassView;
		namespaceClassView.set_PropertyValue(uriProperty, schemaURI);

		if (parentPackage == extensionPackage) {
			ns.filepath = prefix2 + ".xsd";
			namespaceClassView.set_PropertyValue(filepathProperty, prefix2 + ".xsd");
		}

		return namespaceClassView;
	}

	// add type to extension
	public static UmlClass addType(String typeName, String description, String notes) {
		// abort if external schema
		// if (isExternal(typeName))
		// return null;

		trace("Copying " + typeName + " to subset");
		// get schemaURI
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = extensionSchema(prefix);

		trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		UmlClass typeClass = findType(extensionPackage, schemaURI, typeName2);
		if (typeClass != null) {
			String currentDescription = typeClass.description();
			if ((currentDescription.equals("")) && (description != null))
				typeClass.set_Description(description);
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
		UmlClassView nsClassView;
		if (isNiemType(typeName))
			nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		else
			nsClassView = addNamespace(extensionPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("Subset/extension classview not found for " + schemaURI);
			return null;
		}
		// create type
		trace("Copying type " + typeName + " to subset");
		try {
			typeClass = UmlClass.create(nsClassView, typeName2);
		} catch (RuntimeException re) {
			trace("addType: type already exists in extension " + typeName);
		}
		if (typeClass != null) {
			typeClass.set_Description(description);
			typeClass.set_PropertyValue(uriProperty, schemaURI + hashDelimiter + typeName2);
			if (!notes.equals(""))
				typeClass.set_PropertyValue(notesProperty, notes);
		}
		return typeClass;
	}

	// add type to reference model or extension
	public static UmlClass addType(UmlClassView parentClassView, String schemaURI, String tagName, String description,
			String notes) {
		String tagName2 = tagName.replace("-", "");
		// String tagName2 = tagName;
		trace("addType:" + tagName);
		UmlClass typeClass = findType((UmlPackage) (parentClassView.parent()), schemaURI, tagName2);
		if (typeClass == null) {
			// if (tagName2.equals("abstract"))
			trace("findType: abstract");
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

	// cache NIEM reference, subset or extension models
	public static void cacheModel(UmlPackage rootPackage) {
		if (rootPackage == null)
			return;

		// Cache namespaces, types and elements
		String schemaURI;
		Map<String, UmlItem> Elements = null;
		Map<String, List<UmlClassInstance>> ElementsInType = null;
		Map<String, UmlClass> Types = null;

		if (rootPackage == referencePackage) {
			Elements = NiemElements;
			ElementsInType = NiemElementsInType;
			Types = NiemTypes;
		} else if (rootPackage == subsetPackage) {
			Elements = SubsetElements;
			ElementsInType = SubsetElementsInType;
			Types = SubsetTypes;
		} else if (rootPackage == extensionPackage) {
			Elements = ExtensionElements;
			ElementsInType = ExtensionElementsInType;
			Types = ExtensionTypes;
		} else
			return;

		for (UmlItem cv : rootPackage.children()) {
			if (cv.kind() != anItemKind.aClassView)
				continue;

			schemaURI = cv.propertyValue(uriProperty);
			String prefix = cv.pretty_name();
			if (!Prefixes.containsKey(prefix)) {
				trace("Adding prefix " + prefix);
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

			if (rootPackage == referencePackage || rootPackage == extensionPackage)
				ns.filepath = cv.propertyValue(filepathProperty);

			for (UmlItem c : cv.children()) {
				schemaURI = c.propertyValue(uriProperty);
				if (schemaURI != null)
					switch (c.kind().value()) {
					case anItemKind._aClass:
						Types.put(schemaURI, (UmlClass) c);
						break;
					case anItemKind._aClassInstance:
						UmlClassInstance ci = (UmlClassInstance)c;
						Elements.put(schemaURI, ci);
						String headElement = ci.propertyValue(substitutionProperty);
						if (headElement != null) {
							List<UmlClassInstance> enlist = (List<UmlClassInstance>) (Substitutions.get(headElement));
							if (enlist == null) {
								enlist = new ArrayList<UmlClassInstance>();
								Substitutions.put(headElement, enlist);
							}
							if (!enlist.contains(ci))
								enlist.add(ci);
						}
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
					trace("Caching " + a.propertyValue(uriProperty));
					UmlClassInstance ci = (UmlClassInstance) Elements.get(a.propertyValue(uriProperty));
					enlist.add(ci);
				}
		}

		if (rootPackage == referencePackage) {
			NiemElements = Elements;
			NiemElementsInType = ElementsInType;
			NiemTypes = Types;
			referenceAbstractType = NiemTypes.get(localPrefix + hashDelimiter + abstractTypeName);
			referenceAnyElement = (UmlClassInstance) NiemElements
					.get(XMLConstants.W3C_XML_SCHEMA_NS_URI + hashDelimiter + anyElementName);
			// referenceAbstractType = findType(referencePackage, localSchemaURI +
			// localPrefix, abstractTypeName);
			// if (referenceAbstractType == null)
			trace("cacheModel: reference abstract type not found");
		} else if (rootPackage == subsetPackage) {
			SubsetElements = Elements;
			SubsetElementsInType = ElementsInType;
			SubsetTypes = Types;
			subsetAbstractType = SubsetTypes.get(localPrefix + hashDelimiter + abstractTypeName);
			subsetAugmentationType = SubsetTypes.get(structuresPrefix + hashDelimiter + augmentationTypeName);
			subsetObjectType = SubsetTypes.get(structuresPrefix + hashDelimiter + objectTypeName);
			// subsetAbstractType = findType(subsetPackage, localSchemaURI + localPrefix,
			// abstractTypeName);
			// if (subsetAbstractType == null)
			trace("cacheModel: subset abstract type not found");
		} else if (rootPackage == extensionPackage) {
			ExtensionElements = Elements;
			ExtensionElementsInType = ElementsInType;
			ExtensionTypes = Types;
		}
	}

	protected static String columnHtml(String value, String bgcolor, String fgcolor, Boolean wordwrap) {
		String style = wordwrap ? "word-wrap: break-word" : "";
		return "<td  style=\"" + style + "\" bgcolor=\"" + bgcolor + "\"><font color = \"" + fgcolor + "\">" + value
				+ "</font></td>";
	}

	// copy element from NIEM reference model to subset
	public static UmlClassInstance copyElement(String elementName) {
		trace("Copying " + elementName + " to subset");
		// get schemaURI
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("Namespace not found for prefix " + prefix);
			return null;
		}
		trace("Namespace found");
		// if element already exists in subset, return
		String elementName2 = getName(elementName);
		UmlClassInstance element = findElement(subsetPackage, schemaURI, elementName2);
		if (element != null) {
			trace("Element " + element + " already exists in subset");
			return element;
		}

		// if type doesn't exist in reference model, return
		UmlClassInstance sourceElement = findElement(referencePackage, schemaURI, elementName2);
		if (sourceElement == null) {
			UmlCom.trace("Element " + schemaURI + namespaceDelimiter + elementName2 + " not found in reference");
			return null;
		}
		trace("Element found in reference model");

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
						String basePrefix = sourceBaseType.parent().name();
						baseType = copyType(baseSchemaURI, basePrefix, pt);
					}
					if (baseType != null)
						SubsetTypes.put(baseType.propertyValue(uriProperty), baseType);
				}
				if (baseType == null) {
					UmlCom.trace("Base type not found in subset for " + baseSchemaURI + namespaceDelimiter + pt);
					return null;
				}
			}
		// create element
		trace("Copying element " + element + " in subset");
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

		trace("Namespace found");
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
		 * if (type == null) { UmlCom.trace("copyElementInType: type is null"); return
		 * null; } if (element == null) {
		 * UmlCom.trace("copElementInType: element is null"); return null; }
		 */
		trace("copyElementInType: Adding " + element.pretty_name() + " to type " + type.pretty_name());
		UmlAttribute at = null;
		for (UmlItem item : type.children()) {
			if (item.kind() == anItemKind.anAttribute && item.name().equals(element.pretty_name())) {
				at = (UmlAttribute) item;
				String previousMultiplicity = at.multiplicity();
				if (!previousMultiplicity.equals(multiplicity))
					UmlCom.trace("copyElementInType:  " + type.parent().name() + namespaceDelimiter + type.pretty_name()
							+ "/" + element.parent().name() + ":" + element.pretty_name()
							+ " has conflicting multiplicities " + previousMultiplicity + " and " + multiplicity);
				return at;
			}
		}
		if (at == null)
			try {
				at = UmlAttribute.create(type, element.pretty_name());
			} catch (RuntimeException re) {
				trace("copyElementInType: attribute already exists " + element + " " + re.toString());
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
		trace("Copying " + typeName + " to subset");
		// get schemaURI
		String prefix = getPrefix(typeName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("copyType: Namespace not found for prefix " + prefix);
			return null;
		}
		trace("Namespace found");
		// if type already exists in subset, return
		String typeName2 = getName(typeName);
		return copyType(schemaURI, prefix, typeName2);
	}

	// copy type from reference model to subset
	public static UmlClass copyType(String schemaURI, String prefix, String typeName2) {
		UmlClass typeClass = findType(subsetPackage, schemaURI, typeName2);
		if (typeClass != null) {
			trace("Type " + prefix + ":" + typeName2 + " already exists in subset");
			return typeClass;
		}

		// if type doesn't exist in reference model, return
		UmlClass sourceType = findType(referencePackage, schemaURI, typeName2);
		if (sourceType == null) {
			UmlCom.trace("copyType: Type " + schemaURI + namespaceDelimiter + typeName2 + " not found in reference");
			return null;
		}
		trace("Type " + prefix + ":" + typeName2 + " found in reference model");
		// if namespace doesn't exist, create it
		UmlClassView nsClassView = addNamespace(subsetPackage, prefix, schemaURI);
		if (nsClassView == null) {
			UmlCom.trace("copyType: Subset classview not found for " + schemaURI);
			return null;
		}
		trace("Subset classview created " + nsClassView.pretty_name() + " " + nsClassView.propertyValue(uriProperty));
		// create type
		trace("Copying type " + typeName2 + " to subset schema " + nsClassView.pretty_name());
		try {
			typeClass = UmlClass.create(nsClassView, typeName2);
		} catch (Exception e) {
			UmlCom.trace("copyType: type not found " + typeName2 + " " + e.toString());
		}
		if (typeClass == null) {
			UmlCom.trace("copyType: type not found " + typeName2 + " ");
			return null;
		}

		SubsetTypes.put(typeClass.propertyValue(uriProperty), typeClass);
		typeClass.set_Description(sourceType.description());
		typeClass.set_PropertyValue(uriProperty, sourceType.propertyValue(uriProperty));
		String codeList = sourceType.propertyValue(codeListProperty);
		if (codeList != null)
			typeClass.set_PropertyValue(codeListProperty, codeList);

		// copy base type
		for (UmlItem item : sourceType.children()) {
			if (item.kind() == anItemKind.aRelation) {
				UmlRelation r = (UmlRelation) item;
				if (r.relationKind() == aRelationKind.aGeneralisation) {
					UmlClass sourceBaseType = r.roleType();
					String sourceBasePrefix = sourceBaseType.parent().name();
					String sourceBaseSchemaURI = Prefixes.get(sourceBasePrefix);
					String sourceBaseTagName = sourceBaseType.name();
					UmlClass baseType = copyType(sourceBaseSchemaURI, sourceBasePrefix, sourceBaseTagName);
					if (baseType == null) {
						UmlCom.trace("copyType: base type not found " + sourceBaseTagName);
						continue;
					}
					SubsetTypes.put(baseType.propertyValue(uriProperty), baseType);
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, typeClass, baseType);
					} catch (Exception re) {
						UmlCom.trace("createSubset: cannot relate " + typeClass + " to " + sourceBaseTagName + " "
								+ re.toString());
					}
				}
			}
		}
		return typeClass;
	}

	// create Platform Independent Model (NIEM)
	public static void createNIEM(UmlPackage root) {
		UmlPackage pimPackage = null;

		trace("Creating NIEM folders");
		// Find or create NIEM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("NIEM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			pimPackage = UmlPackage.create(root, "NIEM");
			trace("Creating NIEM");
		}

		// Find or create package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMSubset"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					subsetPackage = (UmlPackage) ch;
					break;
				}
		}
		if (subsetPackage == null) {
			trace("Creating NIEMSubset");
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
		if (extensionPackage == null) {
			trace("Creating NIEMExtension");
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
		referencePackage.set_Stereotype("framework");
	}

	// create NIEM subset and extension
	public static void createSubsetAndExtension(String IEPDURI) {
		extensionSchemaURI = IEPDURI;

		// String[] nextLine = new String[map.length];

		trace("Creating subset");
		// cache NIEM namespaces, elements and types
		trace("createSubset: Cache reference model");
		cacheModel(referencePackage);
		trace("createSubset: Cache subset model");
		cacheModel(subsetPackage);
		trace("createSubset: Cache extension model");
		cacheModel(extensionPackage);

		// add abstract types
		if (subsetAbstractType == null) {
			String localUri = localPrefix;
			UmlClassView cv = addNamespace(subsetPackage, localPrefix, localUri);
			subsetAbstractType = addType(cv, cv.propertyValue(uriProperty), abstractTypeName, "abstract type", "");
			if (subsetAbstractType != null)
				SubsetTypes.put(subsetAbstractType.propertyValue(uriProperty), subsetAbstractType);
			// subsetAbstractType = copyType("local:abstract");
		}

		// Copy subset base types
		trace("createSubset: Copy subset base types");
		if (subsetObjectType == null) {
			subsetObjectType = copyType(structuresPrefix + namespaceDelimiter + objectTypeName);
			if (subsetObjectType != null)
				SubsetTypes.put(subsetObjectType.propertyValue(uriProperty), subsetObjectType);
		}
		if (subsetAugmentationType == null) {
			subsetAugmentationType = copyType(structuresPrefix + namespaceDelimiter + augmentationTypeName);
			if (subsetAugmentationType != null)
				SubsetTypes.put(subsetAugmentationType.propertyValue(uriProperty), subsetAugmentationType);
		}
		UmlClass type;
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String baseTypeName = c.propertyValue(niemProperty(7));
				// if (!baseTypeName.equals("") && !baseTypeName.equals("??") &&
				// !isExternal(baseTypeName))
				if (!baseTypeName.equals("") && !baseTypeName.equals("??")) {
					if (isNiemType(baseTypeName)) {
						trace("Adding type " + baseTypeName + " to subset");
						type = copyType(baseTypeName);
						if (type != null)
							SubsetTypes.put(type.propertyValue(uriProperty), type);
					}
				}
			}
		}

		// Copy subset types and create extension types
		trace("createSubset: Copy subset types and create extension types");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String elementName = c.propertyValue(niemProperty(6)).trim();
				String notes = c.propertyValue(niemProperty(11)).trim();
				String description = null;
				if (elementName.equals(""))
					description = c.description().trim();
				if (typeName.endsWith("AugmentationType"))
					description = "An augmentation type";

				// if (!typeName.equals("") && !typeName.equals("??") && !isExternal(typeName))
				// {
				if (!typeName.equals("") && !typeName.equals("??")) {
					if (isNiemType(typeName)) {
						trace("Adding type " + typeName + " to subset");
						type = copyType(typeName);
						if (type != null)
							SubsetTypes.put(type.propertyValue(uriProperty), type);
					} else {
						trace("Adding type " + typeName + " to extension");
						String prefix = getPrefix(typeName);
						String schemaURI = Prefixes.get(prefix);
						Namespace ns = findNamespace(schemaURI);
						if (ns != null && ns.referenceClassView != null) {
							UmlCom.trace("createSubset: type " + typeName + " not found in reference model");
							continue;
						}
						type = addType(typeName, description, notes);
						if (type != null) {
							String uri = type.propertyValue(uriProperty);
							trace("Added " + uri + " to extension");
							ExtensionTypes.put(uri, type);
						}
					}
				}
			}
		}

		// Copy subset elements and create extension elements
		trace("createSubset: Copy subset elements and create extension elements");
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

				if (elementName.contains("Augmentation"))
					description = "An augmentation";

				// if (!elementName.equals("") && !elementName.equals("??") &&
				// !isExternal(elementName)) {
				if (!elementName.equals("") && !elementName.equals("??")) {
					// String elementName2 = elementName.replace(" ", "").replace("(",
					// "").replace(")", "");
					String[] elements = elementName.split(",");
					String headElement = null;
					Boolean substitution = elementName.contains("(");
					for (String e : elements) {
						String e1 = e.trim();
						String e2 = e1;
						if (substitution && headElement == null)
							headElement = e2;
						boolean representation = false;
						if (e2.startsWith("(") && e2.endsWith(")")) {
							representation = true;
							e2 = e2.substring(1, e2.length() - 1);
						}
						Boolean isNillable = e2.startsWith("@");
						if (isNillable)
							e2 = e2.substring(1);
						if (isNiemElement(e2)) {
							trace("Adding element " + e2 + " in subset");
							UmlClassInstance element = copyElement(e2);
							if (element == null) {
								UmlCom.trace("createSubset: could not create element " + e2);
								continue;
							}
							SubsetElements.put(element.propertyValue(uriProperty), element);
							if (isNillable)
								element.set_PropertyValue(nillableProperty, "true");
							if (substitution && representation && headElement != null)
								element.set_PropertyValue(substitutionProperty, headElement);
							if (!codeList.equals("") && (!substitution || representation))
								element.set_PropertyValue(codeListProperty, codeList);
							if (!representation && !typeName.equals("") && isNiemElementInType(typeName, e2)) {
								trace("Adding element " + e2 + " in type " + typeName + " in subset");
								String cn = element.parent().propertyValue(uriProperty);
								List<UmlClassInstance> enlist = (List<UmlClassInstance>) (SubsetElementsInType.get(cn));
								if (enlist == null) {
									enlist = new ArrayList<UmlClassInstance>();
									SubsetElementsInType.put(cn, enlist);
								}
								copyElementInType(typeName, element, multiplicity);
							}
						} else {
							trace("Adding element " + e + " in extension");
							String prefix = getPrefix(e2);
							String schemaURI = Prefixes.get(prefix);
							String baseTagName = baseTypeName.trim();
							if (substitution && !representation)
								baseTagName = "abstract";
							Namespace ns = findNamespace(schemaURI);
							if (ns != null && ns.referenceClassView != null) {
								UmlCom.trace("createSubset: element " + e2 + " not found in reference model");
								continue;
							}
							if (baseTagName.equals("")) {
								UmlCom.trace("createSubset: base type not found for element " + e2);
								continue;
							}
							UmlClassInstance ci = addElement(e2, baseTagName, description, mappingNotes);
							if (ci == null) {
								UmlCom.trace("createSubset: could not create element " + e2);
								continue;
							}
							ExtensionElements.put(ci.propertyValue(uriProperty), ci);
							if (isNillable)
								ci.set_PropertyValue(nillableProperty, "true");
							if (substitution && representation && headElement != null)
								ci.set_PropertyValue(substitutionProperty, headElement);
							if (!codeList.equals("") && (!substitution || representation))
								ci.set_PropertyValue(codeListProperty, codeList);
						}
					}
				}
			}
		}

		// Create extension base types
		trace("createSubset: Copy subset base types and create extension base types");
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem c = (UmlItem) UmlItem.all.elementAt(i);
			if (c.stereotype().equals(niemStereotype)) {
				String typeName = c.propertyValue(niemProperty(5)).trim();
				String elementName = c.propertyValue(niemProperty(6)).trim();
				String baseTypeName = c.propertyValue(niemProperty(7)).trim();

				if (!typeName.equals("") && !typeName.equals("??") && !baseTypeName.equals("")
						&& elementName.equals("")) {
					String tagName = getName(typeName);
					String prefix = getPrefix(typeName);
					String schemaURI = Prefixes.get(prefix);
					String baseTagName = getName(baseTypeName);
					String basePrefix = getPrefix(baseTypeName);
					String baseSchemaURI = Prefixes.get(basePrefix);
					UmlClass baseType;
					// if (isNiemType(typeName) || isExternal(typeName))
					if (isNiemType(typeName))
						continue;
					type = findType(extensionPackage, schemaURI, tagName);
					if (type == null) {
						UmlCom.trace("createSubset: type not found " + typeName);
						continue;
					}
					if (isNiemType(baseTypeName))
						baseType = findType(subsetPackage, baseSchemaURI, baseTagName);
					else
						baseType = findType(extensionPackage, baseSchemaURI, baseTagName);
					if (baseType == null) {
						UmlCom.trace("createSubset: base type not found " + baseTypeName);
						continue;
					}
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, type, baseType);
					} catch (Exception re) {
						trace("createSubset: " + typeName + " already related to " + baseTypeName + " "
								+ re.toString());
					}
				}

				// Add generalizations for extension augmentations
				if (typeName.endsWith(augmentationTypeName)) {
					String tagName = getName(typeName);
					String prefix = getPrefix(typeName);
					String schemaURI = Prefixes.get(prefix);
					// if (isNiemType(typeName) || isExternal(typeName))
					if (isNiemType(typeName))
						continue;
					type = findType(extensionPackage, schemaURI, tagName);
					if (type == null) {
						UmlCom.trace("createSubset: type not found " + typeName);
						continue;
					}
					try {
						UmlBaseRelation.create(aRelationKind.aGeneralisation, type, subsetAugmentationType);
					} catch (Exception re) {
						trace("createSubset: " + typeName + " already related to " + baseTypeName + " "
								+ re.toString());
					}
				}
			}
		}

		// Add extension elements in type
		trace("createSubset: Add extension elements in type");
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
							Boolean isNillable = e1.startsWith("@");
							if (isNillable)
								e1 = e1.substring(1);
							trace("Adding element " + e1 + " in type " + typeName + " in subset");
							UmlClassInstance element;
							if (isNiemElement(e1))
								element = findElement(subsetPackage, e1);
							else
								element = findElement(extensionPackage, e1);
							if (element != null) {
								String cn = element.parent().propertyValue(uriProperty);
								List<UmlClassInstance> enlist = (List<UmlClassInstance>) (ExtensionElementsInType
										.get(cn));
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
		trace("Sorting namespaces");
		subsetPackage.sort();
		extensionPackage.sort();
	}

	// reset NIEM mappings
	public static void deleteMapping() {
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (item.stereotype().equals(niemStereotype) && item.kind() != anItemKind.aClassInstance)
				for (int p = 4; p < map.length; p++)
					item.set_PropertyValue(niemProperty(p), "");
		}
	}

	// delete PIM model
	public static void deleteNIEM(UmlPackage root) {
		UmlPackage pimPackage = null;

		trace("Deleting PIM");
		deleteSubset(root);

		// Delete reference model
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("NIEM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					// pimPackage.deleteIt();
					break;
				}
		}
		// Find PIM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("NIEM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			UmlCom.trace("NIEM not found");
			return;
		}

		// Delete children of package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMReference"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					referencePackage = (UmlPackage) ch;
					trace("Deleting NIEMExtension");
					// subsetPackage.deleteIt();
					break;
				}
		}
		for (UmlItem item : referencePackage.children())
			item.deleteIt();
	}

	// delete PIM model
	public static void deleteSubset(UmlPackage root) {
		UmlPackage pimPackage = null;

		trace("Deleting Subset");

		// Find PIM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("NIEM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage == null) {
			UmlCom.trace("NIEM not found");
			return;
		}

		// Delete children of package "NIEMSubset"
		for (UmlItem ch : pimPackage.children()) {
			if (ch.pretty_name().equals("NIEMSubset"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					subsetPackage = (UmlPackage) ch;
					trace("Deleting NIEMSubset");
					// subsetPackage.deleteIt();
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
					trace("Deleting NIEMExtension");
					// extensionPackage.deleteIt();
					break;
				}
		}
		for (UmlItem item : extensionPackage.children())
			item.deleteIt();
	}

	// generate Genericode code list
	public static void exportCodeList(String dir, String elementName, String codeListURI, String codeList,
			String version, String date) {
		try {
			FileWriter fw;

			// export code list
			fw = new FileWriter(dir + "/" + elementName + ".gc");
			fw.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
					+ "<!-- Genericode code list generated by BOUML niemtools plug_out (https://github.com/cabralje/niem-tools) -->\n"
					+ "<gc:CodeList xmlns:ct=\"http://release.niem.gov/niem/conformanceTargets/3.0/\" xmlns:gc=\"http://docs.oasis-open.org/codelist/ns/genericode/1.0/\" xmlns:gca=\"http://example.org/namespace/genericode-appinfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://docs.oasis-open.org/codelist/ns/genericode/1.0/ https://docs.oasis-open.org/codelist/cs-genericode-1.0/xsd/genericode.xsd\">"
					+ "<Annotation><AppInfo><gca:ConformanceTargets ct:conformanceTargets=\"http://reference.niem.gov/niem/specification/code-lists/1.0/#GenericodeCodeListDocument\"/></AppInfo></Annotation>"
					+ "<Identification><ShortName>" + elementName + "</ShortName>" + "<Version>" + version
					+ "</Version>" + "<CanonicalUri>" + codeListURI + "</CanonicalUri>" + "<CanonicalVersionUri>"
					+ codeListURI + "/" + date + "</CanonicalVersionUri>" + "</Identification>" + "<ColumnSet>"
					+ "<Column Id=\"code\" Use=\"required\"><ShortName>code</ShortName>"
					+ "<CanonicalUri>http://reference.niem.gov/niem/specification/code-lists/1.0/column/code</CanonicalUri>"
					+ "<Data Type=\"normalizedString\" Lang=\"en\"/></Column>"
					+ "<Column Id=\"definition\" Use=\"optional\"><ShortName>definition</ShortName>"
					+ "<CanonicalUri>http://reference.niem.gov/niem/specification/code-lists/1.0/column/definition</CanonicalUri>"
					+ "<Data Type=\"normalizedString\" Lang=\"en\"/></Column>"
					+ "<Key Id=\"codeKey\"><ShortName>CodeKey</ShortName><ColumnRef Ref=\"code\"/></Key>"
					+ "</ColumnSet>" + "<SimpleCodeList>");
			if (codeList.contains(codeListDelimiter)) {
				String[] codes = codeList.split(codeListDelimiter);
				for (String code : codes) {
					String[] pairs = code.split(codeListDefDelimiter);
					fw.write("<Row><Value ColumnRef=\"code\"><SimpleValue>" + pairs[0].trim()
							+ "</SimpleValue></Value>");
					if (pairs.length > 1)
						fw.write("<Value ColumnRef=\"definition\"><SimpleValue>" + pairs[1].trim()
								+ "</SimpleValue></Value>");
					fw.write("</Row>");
				}
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
	public static void exportCsv(String dir, String filename, String externalSchemas) {

		setExternalSchemas(externalSchemas);

		UmlItem.directory = dir;

		trace("Creating CSV file" + dir + "/" + filename);

		try {
			FileWriter fw = new FileWriter(dir + "/" + filename);
			CSVWriter writer = new CSVWriter(fw);

			trace("CSV file created " + dir + "/" + filename);

			// Write header
			String[] nextLine = new String[map.length];
			trace("Header");
			for (int i = 0; i < map.length; i++) {
				nextLine[i] = map[i][0];
				trace(nextLine[i]);
			}
			writer.writeNext(nextLine);

			// Export NIEM Mappings for Classes
			trace("Rows");
			for (int i = 0; i < UmlClass.classes.size(); i++) {
				UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);

				if (c.stereotype().equals(niemStereotype)) {
					nextLine = itemCsv(c);
					trace("0:" + nextLine[0] + " 1:" + nextLine[1] + "2:" + nextLine[2] + " 3:" + nextLine[3]);
					writer.writeNext(nextLine);

					// Export NIEM Mapping for Attributes and Relations
					for (UmlItem ch : c.children())
						if (ch.stereotype().equals(niemStereotype)) {
							nextLine = itemCsv(ch);
							if (nextLine != null) {
								trace("0:" + nextLine[0] + " 1:" + nextLine[1] + " 2:" + nextLine[2] + " 3:"
										+ nextLine[3]);
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
	public static void exportHtml(String dir, String filename, String externalSchemas) {

		setExternalSchemas(externalSchemas);
		// cache NIEM namespaces, elements and types
		cacheModel(referencePackage);
		try {
			// Write rest of header
			FileWriter fw = new FileWriter(dir + "/" + filename + ".html");
			fw.write("<html>");
			fw.write(
					"<head><title>NIEM Mapping</title><link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /></head>"
							+ "<body><div class = \"title\">NIEM Mapping</div>"
							+ "<table style=\"table-layout: fixed; width: 100%\"><tr bgcolor=\"#f0f0f0\">");
			for (int i = 0; i < map.length; i++)
				fw.write("<td style=\"word-wrap: break-word\">" + map[i][0] + "</td>");
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
	public static void exportSchema(UmlPackage schemaPackage, String externalSchemas, String xmlDir, String jsonDir, String IEPDVersion) {
		
		Boolean exportXML = (xmlDir != null);
		Boolean exportJSON = (jsonDir != null);
		
		try {
			FileWriter xml = null, json = null;
			//Set<String> CodeListNamespaces = new HashSet<String>();

			// export each schema
			for (UmlItem item : schemaPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView cv = (UmlClassView) item;
					cv.sort();
					String prefix = cv.name();
					if (isExternalPrefix(prefix))
						continue;
					String schemaURI = cv.propertyValue(uriProperty);
					trace("Exporting schema " + prefix);
				
					// build list of referenced namespaces
					Set<String> RefNamespaces = new TreeSet<String>();
					RefNamespaces.add(xmlPrefix);
					// RefNamespaces.add(structuresPrefix);
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass) {
							UmlClass c = (UmlClass) item2;
							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.aRelation) {
									UmlRelation r = (UmlRelation) item3;
									if (r.relationKind() == aRelationKind.aGeneralisation) {
										UmlClass baseType = r.roleType();
										RefNamespaces.add(baseType.parent().name());
										break;
									}
								}
							UmlClass t = c, baseType = null;
							while (t != null) {
								for (UmlItem item4 : t.children()) {
									if (item4.kind() == anItemKind.anAttribute) {
										UmlAttribute a = (UmlAttribute) item4;
										String elementUri = a.propertyValue(uriProperty);
										UmlClassInstance ci;
										if (SubsetElements.containsKey(elementUri))
											ci = (UmlClassInstance) SubsetElements.get(elementUri);
										else
											ci = (UmlClassInstance) ExtensionElements.get(elementUri);
										RefNamespaces.add(ci.parent().name());
									}
									if (item4.kind() == anItemKind.aRelation) {
										UmlRelation r = (UmlRelation) item4;
										if (r.relationKind() == aRelationKind.aGeneralisation)
											baseType = r.roleType();
									}
								}
								t = baseType;
								baseType = null;
							}
						}
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance ci = (UmlClassInstance) item2;
							UmlClass baseType = ci.type();
							RefNamespaces.add(baseType.parent().name());
							String headElement = ci.propertyValue(substitutionProperty);
							if (headElement != null)
								RefNamespaces.add(getPrefix(headElement));
						}

					if (exportXML) {
						// Open XSD file for each extension schema and write header
						String nsSchemaURI = Prefixes.get(prefix);
						Namespace ns = Namespaces.get(nsSchemaURI);
						if (ns.filepath == null)
							continue;
						File file = new File(xmlDir + "/" + ns.filepath);
						file.getParentFile().mkdirs();
						//xml = new FileWriter(xmlDir + "/" + prefix + ".xsd");
						xml = new FileWriter(xmlDir + "/" + ns.filepath);
						trace("exportSchema: schema " + prefix + ".xsd");
						// xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
						xml.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n"
								+ "<!-- NIEM extension schema generated by BOUML niemtools plug_out (https://github.com/cabralje/niem-tools) -->\n"
								+ "<xs:schema targetNamespace=\"" + schemaURI + "\"\n");

						// export XML namespace definitions
						xml.write(" xmlns=\"" + cv.propertyValue(uriProperty) + "\"\n");
						xml.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
						for (String nsPrefix : RefNamespaces)
							xml.write(" xmlns:" + nsPrefix + "=\"" + Prefixes.get(nsPrefix) + "\"");

						// export schemaLocation
						// xml.write(" xsi:schemaLocation = \"");
						// for (String nsPrefix : RefNamespaces)
						// xml.write(Prefixes.get(nsPrefix) + " " + nsPrefix + ".xsd" + " ");
						// xml.write("\"");

						xml.write(
								" xmlns:clsa=\"http://reference.niem.gov/niem/specification/code-lists/1.0/code-lists-schema-appinfo/\""
										+ " xmlns:ct=\"http://release.niem.gov/niem/conformanceTargets/3.0/\""
										+ " xmlns:term=\"http://release.niem.gov/niem/localTerminology/3.0/\""
										+ " ct:conformanceTargets=\"http://reference.niem.gov/niem/specification/naming-and-design-rules/3.0/#ExtensionSchemaDocument http://reference.niem.gov/niem/specification/code-lists/1.0/#SchemaDocument\""
										+ " elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" version=\""
										+ IEPDVersion + "\"");

						// close top level element
						xml.write(">\n");
						xml.write("<xs:annotation>\n"
									+ "<xs:documentation> Schema for namespace " + Prefixes.get(prefix) + "</xs:documentation>\n"
								+ "</xs:annotation>");

						// export import namespaces
						if (file != null) {
							Path p1 = Paths.get(file.getParent());
							if (p1 == null)
								UmlCom.trace("P1 is null");
							for (String nsPrefix : RefNamespaces) {
								String nsSchemaURI2 = Prefixes.get(nsPrefix);
								if (isExternalPrefix(nsPrefix))
									xml.write("<xs:import namespace=\"" + nsSchemaURI2 + "\" schemaLocation=\""
											+ externalSchemaURL.get(nsSchemaURI2) + "\"/>");
								else {
									Namespace ns2 = Namespaces.get(nsSchemaURI2);
									if (ns2.filepath == null)
										continue;
									Path p2 = Paths.get(xmlDir + "/" + ns2.filepath);
									Path p3 = p1.relativize(p2);
									if (!nsSchemaURI2.equals(schemaURI) && !nsSchemaURI2.equals(localPrefix)
											&& !nsSchemaURI2.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) && ns2 != null)
										xml.write("<xs:import namespace=\"" + nsSchemaURI2 + "\" schemaLocation=\""
												+ p3.toString() + "\"/>");
								}
							}
						}
					}

					if (exportJSON) {
						// Open JSON schema file for each extension schema and write header
						json = new FileWriter(jsonDir + "/" + prefix + ".schema.json");
						trace("exportSchema: schema " + prefix + ".schema.json");
						// xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
						json.write("{" + "\"$id\" : \"" + schemaURI + "\",\n"
								+ "\"$schema\" : \"http://json-schema.org/draft-04/schema#\",\n"
								+ "\"type\" : \"object\",\n"
								+ "\"additionalProperties\" : false,\n");

						// export JSON-LD namespace definitions
						json.write("\"@context\" : {\n");
						for (String nsPrefix : RefNamespaces)
							json.write("\"" + nsPrefix + "\":\"" + Prefixes.get(nsPrefix) + "#\",\n");
						json.write(
								"\"clsa\" : \"http://reference.niem.gov/niem/specification/code-lists/1.0/code-lists-schema-appinfo/#\",\n"
										+ "\"ct\" : \"http://release.niem.gov/niem/conformanceTargets/3.0/#\",\n"
										+ "\"term\" : \"http://release.niem.gov/niem/localTerminology/3.0/#\"\n" + "},");
					}

					// export types
					Set<String> types = new HashSet<String>();
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass) {
							UmlClass c = (UmlClass) item2;
							String type = null;
							String typeName = c.name();
							String description = c.description();
							String baseTypeName = "";
							String baseTypeCodeList = "";
							String mappingNotes = c.propertyValue(notesProperty);
							String codelist = c.propertyValue(codeListProperty);
							if (mappingNotes != null && !mappingNotes.equals(""))
								xml.write("<!--" + mappingNotes + "-->");
							String augmentationPoint = null, augmentationPointMin = null, augmentationPointMax = null;
							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.aRelation) {
									UmlRelation r = (UmlRelation) item3;
									if (r.relationKind() == aRelationKind.aGeneralisation) {
										UmlClass baseType = r.roleType();
										baseTypeName = baseType.parent().name() + namespaceDelimiter + baseType.name();
										baseTypeCodeList = baseType.propertyValue(codeListProperty);
										break;
									}
								}
							// if (typeName.endsWith("AugmentationType"))
							// baseTypeName = "structures:AugmentationType";
							//if (baseTypeName.equals("")) // abstract
							//	UmlCom.trace("exportSchema: type " + prefix + namespaceDelimiter + typeName
							//			+ " has no base type");
							
							if (codelist != null && !codelist.equals("")) {
								if (exportXML) {
									xml.write("<xs:simpleType name=\"" + typeName + "\">\n" 
											+ "<xs:annotation>\n"
												+ "<xs:documentation>" + description + "</xs:documentation>\n"
											+ "</xs:annotation>\n" 
											+ "<xs:restriction base=\"" + baseTypeName + "\">\n");
								}
								if (exportJSON)
									type = "\"" + prefix + ":" + typeName + "\": {\n"
											+ "\"description\": \"" + description + "\",\n"
											+ "\"$ref\": \"" + getJSONType(baseTypeName, prefix) + "\",\n"; 
								String[] codes = codelist.split(codeListDelimiter);
								Set<String> enums = new HashSet<String>();
								for (String code : codes) {
									if (!code.equals("")) {
										String[] codeParams = code.split(codeListDefDelimiter);
										String code2 = codeParams[0].trim().replace("&", "&amp;");
										String codeDescription = "";
										if (codeParams.length > 1 && !codeParams[1].equals(""))
											codeDescription = codeParams[1].trim().replace("&", "&amp;");
										if (!code2.equals("")) {
											enums.add("\"" + code2 + "\"");
											if (exportXML) {
												xml.write("<xs:enumeration value=\"" + code2 + "\">\n");
												if (!codeDescription.equals(""))
													xml.write("<xs:annotation>\n"
																+ "<xs:documentation>" + codeDescription + "</xs:documentation>\n"
															+ "</xs:annotation>\n");
													xml.write("</xs:enumeration>\n"); 
											}
										}
									}
								}
								if (exportXML)
									xml.write("</xs:restriction>"
											+ "</xs:simpleType>");
								if (exportJSON) {
									type += "\"enums\": [" + String.join(",", enums) + "]\n"
										+ "}\n";
									types.add(type);
								}
								continue;
							} 
							if ((baseTypeCodeList != null && !baseTypeCodeList.equals("")) || (getPrefix(baseTypeName).equals("xs"))){
								if (exportXML) {
									xml.write("<xs:complexType name=\"" + typeName + "\">\n" 
												+ "<xs:annotation>\n"
													+ "<xs:documentation>" + description + "</xs:documentation>\n"
												+ "</xs:annotation>\n" 
												+ "<xs:simpleContent>\n"
													+ "<xs:extension base=\"" + baseTypeName + "\">\n"
														+ "<xs:attributeGroup ref=\"structures:SimpleObjectAttributeGroup\"/>\n"
													+ "</xs:extension>\n"
												+ "</xs:simpleContent>\n"
											+ "</xs:complexType>\n"); 
								}
								if (exportJSON) {
									type = "\"" + prefix + ":" + typeName + "\": {\n"
											+ "\"description\": \"" + description + "\",\n"
											+ "\"$ref\": \"" + getJSONType(baseTypeName, prefix) + "\"\n" 
											+ "}\n";
									types.add(type);
								}
								continue;
							} 
							if (exportXML) {
								if (baseTypeName.equals("")) // abstract)
										xml.write("<xs:complexType name=\"" + typeName + "\" abstract=\"true\">\n" 
												+ "<xs:annotation>\n"
													+ "<xs:documentation>" + description + "</xs:documentation>\n"
												+ "</xs:annotation>\n"); 
								else 								
									xml.write("<xs:complexType name=\"" + typeName + "\">\n" 
											+ "<xs:annotation>\n"
												+ "<xs:documentation>" + description + "</xs:documentation>\n"
											+ "</xs:annotation>\n" 
										+ "<xs:complexContent>\n"
										+ "<xs:extension base=\"" + baseTypeName + "\">\n");
								xml.write("<xs:sequence>\n");
							}

							c.sortChildren();
							Set<String> required = new HashSet<String>();
							Set<String> properties = new HashSet<String>();
							Boolean anyJSON = false;
							UmlClass t = c, baseType = null;
							while (t != null && !t.parent().name().equals(structuresPrefix)) {
								for (UmlItem item4 : t.children()) {									
									if (item4.kind() == anItemKind.anAttribute) {
										UmlAttribute a = (UmlAttribute) item4;
										String elementUri = a.propertyValue(uriProperty);
										UmlClassInstance ci;
										if (SubsetElements.containsKey(elementUri))
											ci = (UmlClassInstance) SubsetElements.get(elementUri);
										else
											ci = (UmlClassInstance) ExtensionElements.get(elementUri);
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
										try {
											if (Integer.parseInt(minoccurs) < 0)
												throw new NumberFormatException();
											if (!maxoccurs.equals("unbounded") && (Integer.parseInt(maxoccurs) < 1))
												throw new NumberFormatException();
										} catch (NumberFormatException e) {
											UmlCom.trace("Invalid multiplicity " + multiplicity + " for " + prefix
													+ namespaceDelimiter + typeName + "/" + elementName);
										}
										String elementMappingNotes = a.propertyValue(notesProperty);
										if (elementMappingNotes != null && !elementMappingNotes.equals(""))
											if (t == c && exportXML)
												xml.write("<!--" + elementMappingNotes + "-->");
										if (elementName.equals("xs:any")) {
											if (t == c && exportXML)
												xml.write("<xs:any/>");
											if (exportJSON) {
												anyJSON = true;
											}
										}
										// else if (isExternal(elementName))
										// xml.write("<!--xs:element ref=\"" + elementName + "\" minOccurs=\"" +
										// minoccurs + "\" maxOccurs=\"" + maxoccurs + "\"/-->\n");
										else if (t == c && elementName.endsWith("AugmentationPoint")) {
											augmentationPoint = elementName;
											augmentationPointMin = minoccurs;
											augmentationPointMax = maxoccurs;
										} else {
											if (t == c && exportXML)
												xml.write("<xs:element ref=\"" + elementName + "\" minOccurs=\"" + minoccurs
														+ "\" maxOccurs=\"" + maxoccurs + "\"/>\n");
											if (exportJSON) {
												UmlClass ciBaseType = ci.type();
												if ((ciBaseType != subsetAbstractType) && (ciBaseType != referenceAbstractType)) {
													properties.add(getJSONProperty(elementName, minoccurs, maxoccurs, prefix));
													if (Integer.parseInt(minoccurs) > 0)
														required.add("\"" + elementName + "\"");
												}
												if (Substitutions.containsKey(elementName)) {
													List<UmlClassInstance> enlist = (List<UmlClassInstance>) (Substitutions.get(elementName));
													for (UmlClassInstance ci2 : enlist)
														properties.add(getJSONProperty(ci2.parent().name()+":"+ci2.name(),minoccurs,maxoccurs, prefix));
												}
											}
										}
									}
									if (item4.kind() == anItemKind.aRelation) {
										UmlRelation r = (UmlRelation) item4;
										if (r.relationKind() == aRelationKind.aGeneralisation)
											baseType = r.roleType();
									}
								}
								t = baseType;
								baseType = null;
							}
							// xml.write("<xs:element ref=\"" + elementName + "\" minOccurs=\"" + minoccurs
							// + "\" maxOccurs=\"" + maxoccurs + "\"/>");
							if (augmentationPoint != null) {
								if (exportXML)
									xml.write("<xs:element ref=\"" + augmentationPoint + "\" minOccurs=\""
											+ augmentationPointMin + "\" maxOccurs=\"" + augmentationPointMax
											+ "\"/>\n");
							}
							if (exportXML) {
								xml.write("</xs:sequence>\n");
								if (!baseTypeName.equals(""))
									xml.write("</xs:extension>\n"
										+ "</xs:complexContent>\n");
									xml.write("</xs:complexType>\n");
							}
							if (exportJSON) {
								type = "\"" + prefix + ":" + typeName + "\": {\n"
												+ "\"description\": \"" + description + "\",\n"
												+ "\"type\": \"object\",\n"
												+ "\"additionalProperties\" :" + anyJSON + ",\n"
												+ "\"properties\": {\n"
													+ String.join(",", properties) + "\n"
												+ "},\n"
												+ "\"required\" : [\n"
													+ String.join(",", required) + "\n"
												+ "]\n"
											+ "}\n";
								types.add(type);
							}
						}

					// export elements
					Set<String> elements = new HashSet<String>();
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance ci = (UmlClassInstance) item2;
							String elementName = ci.name();
							String description = ci.description();
							UmlClass baseType = ci.type();
							String baseTypeName = baseType.parent().name() + namespaceDelimiter + baseType.name();
							String mappingNotes = ci.propertyValue(notesProperty);
							String headElement = ci.propertyValue(substitutionProperty);
							String codeList = ci.propertyValue(codeListProperty);
							String isNillable = ci.propertyValue(nillableProperty);
							if (isNillable == null)
								isNillable = "false";
							if (mappingNotes != null && !mappingNotes.equals(""))
								if (exportXML)
									xml.write("<!--" + mappingNotes + "-->");
							if ((baseType == subsetAbstractType) || (baseType == referenceAbstractType)) {
								if (exportXML)
									xml.write("<xs:element name=\"" + elementName + "\" abstract=\"true\">\n");
							} else if (headElement != null) {
								if (exportXML)
									xml.write("<xs:element name=\"" + elementName + "\" type=\"" + baseTypeName
											+ "\" substitutionGroup=\"" + headElement + "\" nillable=\"" + isNillable
											+ "\">\n");
								if (exportJSON)
									elements.add("\"" + prefix + ":" + elementName + "\": {\n" 
												+ "\"description\": \"" + description + "\",\n"
												+ "\"$ref\": \"" + getJSONType(baseTypeName, prefix) + "\"\n" 
												+ "}");
							} else {
								if (exportXML)
									xml.write("<xs:element name=\"" + elementName + "\" type=\"" + baseTypeName
											+ "\" nillable=\"" + isNillable + "\">\n");
								if (exportJSON) {
									elements.add("\"" + prefix + ":" + elementName + "\": {\n" 
												+ "\"description\": \"" + description + "\",\n"
												+ "\"$ref\": \"" + getJSONType(baseTypeName, prefix) + "\"\n" 
												+ "}");
								}
							}
							if (exportXML)
								xml.write(
										"<xs:annotation>\n"
										+ "<xs:documentation>" + description + "</xs:documentation>\n");
							if (codeList != null) {
								String codeListURI = extensionSchema(elementName);
								if (exportXML)
									xml.write("<xs:appinfo>" + "<clsa:SimpleCodeListBinding codeListURI=\""
											+ codeListURI + "\"/>" + " </xs:appinfo>");
							}
							if (exportXML)
									xml.write("</xs:annotation>\n"
										+ "</xs:element>\n");
						}
					// TODO export attributes and attribute groups (used in structures.xsd)
					if (exportXML) {
						xml.write("</xs:schema>\n");
						xml.close();
					}
					if (exportJSON) {
						//types.addAll(elements);
						json.write("\"definitions\": {\n"
										+ String.join(",",  types) + "\n"
									+ "},\n"
									+ "\"properties\" : {"
										+ String.join(",", elements)
										+ "}\n"
									+ "}");
						json.close();
					}
				}
		} catch (IOException e) {
			UmlCom.trace("exportSchema: IO exception: " + e.toString());
		} catch (RuntimeException e) {
			UmlCom.trace("exportSchema: RuntimeException: " + e.toString());
		}
	}
	
	// generate extension and exchange schema
	public static void exportSchema(String IEPDURI, String IEPDName, String IEPDVersion, String IEPDStatus,
			String IEPDOrganization, String IEPDContact, String externalSchemas, String xmlDir, String jsonDir) {

		setExternalSchemas(externalSchemas);
		cacheModel(referencePackage);
		cacheModel(subsetPackage);
		cacheModel(extensionPackage);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String today = dateFormat.format(date);

		exportSchema(subsetPackage, externalSchemas, null, jsonDir + "/" + niemDir , IEPDVersion);
		exportSchema(extensionPackage, externalSchemas, xmlDir, jsonDir, IEPDVersion);
		
		Boolean exportXML = (xmlDir != null);
		Boolean exportJSON = (jsonDir != null);
		
		try {
			FileWriter xml = null;
			Set<String> CodeListNamespaces = new HashSet<String>();

			// export code lists for extension elements
			for (UmlItem item : extensionPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView cv = (UmlClassView) item;
					cv.sort();
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance ci = (UmlClassInstance) item2;
							String elementName = ci.name();
							String codeList = ci.propertyValue(codeListProperty);
							if (codeList != null) {
								String codeListURI = extensionSchema(elementName);
								exportCodeList(xmlDir, elementName, codeListURI, codeList, IEPDVersion, today);
								CodeListNamespaces.add(elementName);
							}
						}
				}
			
			// export code lists for subset elements
			for (UmlItem item : subsetPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView cv = (UmlClassView) item;
					cv.sort();
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance ci = (UmlClassInstance) item2;
							String elementName = ci.name();
							String codeList = ci.propertyValue(codeListProperty);
							if (codeList != null) {
								String codeListURI = extensionSchema(elementName);
								exportCodeList(xmlDir, elementName, codeListURI, codeList, IEPDVersion, today);
								CodeListNamespaces.add(elementName);
							}
						}
				}

			if (exportXML) {
				// export catalog file
				UmlCom.trace("Generating XML catalog");
				xml = new FileWriter(xmlDir + "/xml-catalog.xml");
				xml.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n"
						+ "<!-- XML catalog generated by BOUML niemtools plug_out (https://github.com/cabralje/niem-tools) -->\n"
						+ "<!DOCTYPE catalog PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\" \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\n"
						+ "<catalog prefer=\"public\" xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n");
				for (Entry<String, String> entry : Prefixes.entrySet()) {
					String prefix = entry.getKey();
					String schemaURI = Prefixes.get(prefix);
					Namespace ns = Namespaces.get(schemaURI);
					if (isExternalPrefix(prefix))
						xml.write(
								"<uri name=\"" + schemaURI + "\" uri=\"" + externalSchemaURL.get(schemaURI) + "\"/>\n");
					else if (ns.referenceClassView == null)
						xml.write("<uri name=\"" + schemaURI + "\" uri=\"" + ns.filepath + "\"/>\n");
				}
				for (String codeList : CodeListNamespaces)
					xml.write("<uri name=\"" + extensionSchema(codeList) + "\" uri=\"" + codeList + ".gc\"/>\n");
				xml.write("<nextCatalog  catalog=\"niem/xml-catalog.xml\" />\n</catalog>\n");
				xml.close();
			}

			// cache list of ports, operations and messages
			trace("exportSchema: cache ports, operations and messages");
			Map<String, UmlClass> ports = new TreeMap<String, UmlClass>();
			Map<String, UmlOperation> operations = new TreeMap<String, UmlOperation>();
			Set<String> messages = new TreeSet<String>();
			Map<String, String> outputMessages = new TreeMap<String, String>();
			Map<String, ArrayList<String>> inputMessages = new TreeMap<String, ArrayList<String>>();
			for (int i = 0; i < UmlClass.classes.size(); i++) {
				UmlItem item = (UmlItem) UmlClass.classes.elementAt(i);
				if (item.stereotype().equals("interface")) {
					UmlClass c = (UmlClass) item;
					String portName = c.name();
					ports.put(portName, c);
					trace("Port: " + c.name());
					for (UmlItem item2 : c.children()) {
						if (item2.kind() == anItemKind.anOperation) {
							UmlOperation operation = (UmlOperation) item2;
							String operationName = operation.name();
							trace("Operation: " + operationName);
							operations.put(operationName, operation);
							UmlClass c1 = null, c2 = null;
							UmlParameter[] params = operation.params();
							if (params != null)
								for (UmlParameter param : params) {
									try {
										c2 = param.type.type;
										// String mult = param.multiplicity;
									} catch (Exception e) {
										UmlCom.trace("exportSchema: could not find input message for " + operationName);
									}
									if (c2 != null) {
										if (c2.stereotype().equals(niemStereotype)) {
											String inputMessage = c2.propertyValue(niemProperty(4));
											if (inputMessage != null && !inputMessage.equals("")) {
												trace("Input Message: " + inputMessage + " from operation "
														+ operationName);
												messages.add(inputMessage);
												if (param.multiplicity != null)
													inputMessage = inputMessage + "," + param.multiplicity;
												else
													inputMessage = inputMessage + ",1";
												ArrayList<String> inputs = inputMessages.get(operationName);
												if (inputs == null)
													inputs = new ArrayList<String>();
												if (!inputs.contains(inputMessage))
													inputs.add(inputMessage);
												inputMessages.put(operationName, inputs);
											}
										}
									}
								}
							try {
								c1 = operation.returnType().type;
							} catch (Exception e) {
								UmlCom.trace("exportSchema: could not find output message for " + operationName + " "
										+ e.toString());
							}
							if (c1 != null) {
								if (c1.stereotype().equals(niemStereotype)) {
									String outputMessage = c1.propertyValue(niemProperty(4));
									if (outputMessage != null && !outputMessage.equals("")) {
										trace("Output Message: " + outputMessage + " from operation " + operationName);
										messages.add(outputMessage);
										outputMessages.put(operationName, outputMessage);
									}
								}
							}
						}
					}
				}
			}

			if (exportXML) {
				// export MPD catalog
				UmlCom.trace("Generating MPD catalog");
				xml = new FileWriter(xmlDir + "/mpd-catalog.xml");
				xml.write("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>");
				xml.write("<c:Catalog");
				for (Entry<String, String> entry : Prefixes.entrySet()) {
					String prefix = entry.getKey();
					xml.write(" xmlns:" + prefix + "=\"" + Prefixes.get(prefix) + "\"");
				}
				xml.write(
						" xmlns:c=\"http://reference.niem.gov/niem/resource/mpd/catalog/3.0/\" xsi:schemaLocation=\"http://reference.niem.gov/niem/resource/mpd/catalog/3.0/ ../../mpd-toolkit-3.0/mpd-catalog-3.0.xsd\">");
				xml.write("<c:MPD c:mpdURI=\"" + extensionSchema("")
						+ "\" c:mpdClassURIList=\"http://reference.niem.gov/niem/specification/model-package-description/3.0/#MPD http://reference.niem.gov/niem/specification/model-package-description/3.0/#IEPD\" c:mpdName=\""
						+ IEPDName + "\" c:mpdVersionID=\"" + IEPDVersion + "\">" + "<c:MPDInformation>"
						+ "<c:AuthoritativeSource>" + "<nc:EntityOrganization>" + "<nc:OrganizationName>"
						+ IEPDOrganization + "</nc:OrganizationName>" + "<nc:OrganizationPrimaryContactInformation>"
						+ "<nc:ContactWebsiteURI>" + IEPDContact + "</nc:ContactWebsiteURI>"
						+ "</nc:OrganizationPrimaryContactInformation>" + "</nc:EntityOrganization>"
						+ "</c:AuthoritativeSource>" + "<c:CreationDate>" + today + "</c:CreationDate>"
						+ "<c:StatusText>" + IEPDStatus + "</c:StatusText>" + "</c:MPDInformation>");
				for (String message : messages) {
					String elementName = getName(message);
					UmlClassInstance ci = null;
					if (isNiemElement(message))
						ci = findElement(subsetPackage, message);
					else
						ci = findElement(extensionPackage, message);
					xml.write("<c:IEPConformanceTarget structures:id=\"" + elementName + "\">");
					if (ci == null)
						UmlCom.trace("exportSchema: could not find root element " + message);
					else
						xml.write("<nc:DescriptionText>" + ci.description() + "</nc:DescriptionText>");
					xml.write("<c:HasDocumentElement c:qualifiedNameList=\"" + message + "\"/>" + "<c:XMLSchemaValid>"
							+ "<c:XMLCatalog c:pathURI=\"xml-catalog.xml\"/>" + "</c:XMLSchemaValid>"
							+ "<c:IEPSampleXMLDocument c:pathURI=\"" + elementName + ".xml\"/>"
							+ "</c:IEPConformanceTarget>");
				}
				xml.write("<c:ReadMe c:pathURI=\"readme.txt\"/>" + "<c:MPDChangeLog c:pathURI=\"changelog.txt\"/>"
						+ "<c:Wantlist c:pathURI=\"niem/wantlist.xml\"/>"
						+ "<c:ConformanceAssertion c:pathURI=\"conformance-assertion.pdf\"/>");

				for (Entry<String, String> entry : Prefixes.entrySet()) {
					String prefix = entry.getKey();
					String schemaURI = Prefixes.get(prefix);
					if (schemaURI != null) {
						Namespace ns = Namespaces.get(schemaURI);
						if (ns != null) {
							if ((ns.referenceClassView == null) && (ns.filepath != null))
								xml.write("<c:ExtensionSchemaDocument c:pathURI=\"" + ns.filepath + "\"/>");
						}
					}
				}
				xml.write("</c:MPD></c:Catalog>");
				xml.close();
				UmlCom.trace("Done generating MPD catalog");
			}

			if (exportXML) {
				// export WSDL definitions
				String WSDLURI = IEPDURI + WSDLPrefix;
				String WSDLXSDURI = IEPDURI + WSDLXSDFile;

				UmlCom.trace("Generating document/literal wrapper schema");
				xml = new FileWriter(xmlDir + "/" + WSDLXSDFile + ".xsd");
				xml.write("<xs:schema targetNamespace=\"" + WSDLXSDURI + "\" xmlns:wrapper=\"" + WSDLXSDURI + "\"");

				// build list of referenced namespaces
				Set<String> RefNamespaces = new TreeSet<String>();
				RefNamespaces.add(xmlPrefix);
				// RefNamespaces.add(structuresPrefix);
				for (String message : messages)
					RefNamespaces.add(getPrefix(message));
				for (String nsPrefix : RefNamespaces)
					xml.write(" xmlns:" + nsPrefix + "=\"" + Prefixes.get(nsPrefix) + "\"");
				xml.write(" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
				for (String nsPrefix : RefNamespaces) {
					String nsSchemaURI = Prefixes.get(nsPrefix);
					if (isExternalPrefix(nsPrefix)) {
						if (nsSchemaURI != null)
							xml.write("<xs:import namespace=\"" + nsSchemaURI + "\" schemaLocation=\""
									+ externalSchemaURL.get(nsSchemaURI) + "\"/>");
					} else if (!nsPrefix.equals(xmlPrefix)) {
						Namespace ns = Namespaces.get(nsSchemaURI);
						if (ns != null)
							xml.write("<xs:import namespace=\"" + Prefixes.get(nsPrefix) + "\" schemaLocation=\""
									+ ns.filepath + "\"/>");
					}
				}

				xml.write("<!-- document/literal wrappers -->");
				for (UmlOperation operation : operations.values()) {
					String operationName = operation.name();
					trace("Generating document/literal wrapper for " + operationName);
					ArrayList<String> inputs = inputMessages.get(operationName);
					if (inputs != null) {
						String inputType = operationName + "RequestType";
						xml.write("<xs:complexType name=\"" + inputType + "\">" + "<xs:sequence>");
						for (String inputMessage : inputs) {
							String inputMessage2 = inputMessage;
							String mult = "1";
							if (inputMessage.contains(",")) {
								String inputMessageParts[] = inputMessage.split(",");
								inputMessage2 = inputMessageParts[0];
								if (inputMessageParts.length > 1) {
									mult = inputMessageParts[1];
								}
							}
							String minoccurs = "1";
							String maxoccurs = "1";
							if (!(mult.equals(""))) {
								if (mult.contains("..")) {
									String[] occurs = mult.split("\\.\\.");
									minoccurs = occurs[0];
									maxoccurs = occurs[1];
								} else
									minoccurs = maxoccurs = mult;
							}
							if (maxoccurs.equals("*"))
								maxoccurs = "unbounded";

							if (isExternalPrefix(getPrefix(inputMessage2)))
								xml.write("<!--xs:element ref=\"" + inputMessage2 + "\" minOccurs=\"" + minoccurs
										+ "\" maxOccurs=\"" + maxoccurs + "\"/-->\n");
							else
								xml.write("<xs:element ref=\"" + inputMessage2 + "\" minOccurs=\"" + minoccurs
										+ "\" maxOccurs=\"" + maxoccurs + "\"/>\n");

						}
						xml.write("</xs:sequence>" + "</xs:complexType>" + "<xs:element name=\"" + operationName
								+ "Request\" type=\"wrapper:" + inputType + "\"/>");
					}
					String outputMessage = outputMessages.get(operationName);
					if (outputMessage != null) {
						String outputType = operationName + "ResponseType";
						xml.write("<xs:complexType name=\"" + outputType + "\">" + "<xs:sequence>");
						if (isExternalPrefix(getPrefix(outputMessage)))
							xml.write("<!--xs:element ref=\"" + outputMessage + "\"/-->");
						else
							xml.write("<xs:element ref=\"" + outputMessage + "\"/>");
						xml.write("</xs:sequence>" + "</xs:complexType>" + "<xs:element name=\"" + operationName
								+ "Response\" type=\"wrapper:" + outputType + "\"/>");
					}
				}
				xml.write("</xs:schema>");
				xml.close();

				UmlCom.trace("Generating WSDLs");
				for (UmlClass port : ports.values()) {
					String portName = port.name();
					xml = new FileWriter(xmlDir + "/" + portName + ".wsdl");
					// UmlCom.trace("WSDL: " + portName + ".wsdl");
					xml.write("<definitions targetNamespace=\"" + WSDLURI + "/" + portName + "\" xmlns:tns=\"" + WSDLURI
							+ "/" + portName + "\"" + " xmlns:wrapper=\"" + WSDLXSDURI + "\""
							+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
							+ " xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\""
							+ " xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\""
							+ " xmlns=\"http://schemas.xmlsoap.org/wsdl/\""
							+ " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\""
							+ " xmlns:wsrmp=\"http://docs.oasis-open.org/ws-rx/wsrmp/200702\""
							+ " xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-361 wssecurity-utility-1.0.xsd\">"
							+ "<wsp:UsingPolicy wsdl:required=\"true\"/>" + "<wsp:Policy wsu:Id=\"MyPolicy\">"
							+ "<wsrmp:RMAssertion/>" + "</wsp:Policy>" + "<wsdl:types>" + "<xsd:schema>"
							+ "<xsd:import namespace=\"" + WSDLXSDURI + "\" schemaLocation=\"" + WSDLXSDFile
							+ ".xsd\"/>" + "</xsd:schema>" + "</wsdl:types>");

					xml.write("<!-- messages -->");
					// for (UmlOperation operation : operations.values())
					// {
					// String operationName = operation.name();
					for (UmlItem item : port.children()) {
						if (item.kind() == anItemKind.anOperation) {
							UmlOperation operation = (UmlOperation) item;
							String operationName = operation.name();
							xml.write("<message name=\"" + operationName + "Request\">"
									+ "<part name=\"body\" element=\"" + "wrapper:" + operationName + "Request\"/>"
									+ "</message>" + "<message name=\"" + operationName + "Response\">"
									+ "<part name=\"body\" element=\"" + "wrapper:" + operationName + "Response\"/>"
									+ "</message>");
						}
					}

					xml.write("<!-- ports -->");
					// for (UmlClass port : ports.values())
					// {
					// String portName = port.name();
					xml.write("<portType name=\"" + portName + "\">");
					for (UmlItem item : port.children()) {
						if (item.kind() == anItemKind.anOperation) {
							UmlOperation operation = (UmlOperation) item;
							String operationName = operation.name();
							xml.write("<operation name=\"" + operationName + "\">" + "<input message=\"tns:"
									+ operationName + "Request\"/>" + "<output message=\"tns:" + operationName
									+ "Response\"/>" + "</operation>");
						}
					}
					xml.write("</portType>");
					// }

					xml.write("<!-- bindings -->");
					// for (UmlClass port : ports.values())
					// {
					// String portName = port.name();
					xml.write("<binding name=\"" + portName + "Soap\" type=\"tns:" + portName + "\">"
							+ "<wsp:PolicyReference URI=\"#MyPolicy\"/>"
							+ "<soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>");
					for (UmlItem item : port.children()) {
						if (item.kind() == anItemKind.anOperation) {
							UmlOperation oper = (UmlOperation) item;
							String operationName = oper.name();
							xml.write("<operation name=\"" + operationName + "\">" + "<soap:operation soapAction=\""
									+ WSDLURI + "/" + portName + "/" + operationName + "\"/>" + "<input>"
									+ "	<soap:body use=\"literal\"/>" + "</input>" + "<output>"
									+ "	<soap:body use=\"literal\"/>" + "</output>" + "</operation>");
						}
					}
					xml.write("</binding>");
					// }
					xml.write(
							"<!-- services not defined here...defined in an implementation-specific WSDL that imports this one -->"
									+ "</definitions>");
					xml.close();
				}
			}

			if (exportJSON) {
				// TODO export OpenAPI file
			}
		} catch (IOException e) {
			UmlCom.trace("exportSchema: IO exception: " + e.toString());
		}
	}

	// generate NIEM wantlist for import into Subset Schema Generator Tool (SSGT)
	public static void exportWantlist(String dir, String filename, String externalSchemas) {

		// createSubset();
		setExternalSchemas(externalSchemas);

		UmlItem.directory = dir;
		try {
			// Export schema
			trace("exportWantlist: create header");
			FileWriter fw = new FileWriter(dir + "/" + filename);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write(
					"<!-- NIEM wantlist generated by BOUML niemtools plug_out (https://github.com/cabralje/niem-tools) -->\n");
			fw.write("<w:WantList w:release=\"" + getNiemVersion()
					+ "\" w:product=\"NIEM\" w:nillableDefault=\"true\" ");
			for (UmlItem item : subsetPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					String prefix = item.name();
					String schemaURI = Prefixes.get(prefix);
					if (!prefix.equals(localPrefix))
						fw.write(" xmlns:" + prefix + "=\"" + schemaURI + "\"");
				}
			fw.write(" xmlns:w=\"http://niem.gov/niem/wantlist/2.2\">\n");

			// export elements
			for (UmlItem item : subsetPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView cv = (UmlClassView) item;
					String prefix = cv.name();
					if (prefix.equals(localPrefix))
						continue;
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClassInstance) {
							UmlClassInstance ci = (UmlClassInstance) item2;
							String elementName = ci.name();
							if (prefix.equals(xmlPrefix) && elementName.equals(anyElementName))
								continue;
							trace("exportWantlist: export element " + elementName);
							String isNillable = ci.propertyValue(nillableProperty);
							if (isNillable == null)
								isNillable = "false";
							fw.write("<w:Element w:name=\"" + prefix + namespaceDelimiter + elementName
									+ "\" w:isReference=\"false\" w:nillable=\"" + isNillable + "\"/>\n");
						}
				}

			// export types
			for (UmlItem item : subsetPackage.children())
				if (item.kind() == anItemKind.aClassView) {
					UmlClassView cv = (UmlClassView) item;
					String prefix = cv.name();
					if (prefix.equals(localPrefix))
						continue;
					// String schemaURI = Prefixes.get(prefix);
					for (UmlItem item2 : cv.children())
						if (item2.kind() == anItemKind.aClass) {
							UmlClass c = (UmlClass) item2;
							String typeName = c.name();
							trace("exportWantlist: export type " + typeName);
							if (prefix.equals(structuresPrefix) && typeName.equals(augmentationTypeName))
								continue;
							fw.write("<w:Type w:name=\"" + prefix + namespaceDelimiter + typeName
									+ "\" w:isRequested=\"true\">\n");

							for (UmlItem item3 : c.children())
								if (item3.kind() == anItemKind.anAttribute) {
									UmlAttribute a = (UmlAttribute) item3;
									String uri = a.propertyValue(uriProperty);
									UmlClassInstance ci = (UmlClassInstance) SubsetElements.get(uri);
									String elementPrefix = ci.parent().name();
									String elementName = a.name();
									trace("exportWantlist: adding attribute " + elementName);
									String multiplicity = a.multiplicity();
									trace("exportWantlist: with multiplicity " + multiplicity);
									String minoccurs, maxoccurs;
									if ((multiplicity.equals("")))
										minoccurs = maxoccurs = "1";
									else if (multiplicity.contains(",")) {
										String[] occurs = multiplicity.split(",");
										minoccurs = occurs[0];
										maxoccurs = occurs[1];
									} else
										minoccurs = maxoccurs = multiplicity;
									try {
										if (Integer.parseInt(minoccurs) < 0)
											throw new NumberFormatException();
										if (!maxoccurs.equals("unbounded") && (Integer.parseInt(maxoccurs) < 1))
											throw new NumberFormatException();
									} catch (NumberFormatException e) {
										UmlCom.trace("Invalid multiplicity " + multiplicity + " for " + prefix
												+ namespaceDelimiter + typeName + "/" + elementPrefix
												+ namespaceDelimiter + elementName);
									}
									// if (((!minoccurs.equals("0") && !minoccurs.equals("1"))) ||
									// ((!maxoccurs.equals("1") && !maxoccurs.equals("unbounded"))))
									// UmlCom.trace("createSubset: unusual multiplicity " + multiplicity + " for
									// element " + elementName);;

									trace("exportWantlist: export element " + elementName + " in type " + typeName);
									fw.write("\t<w:ElementInType w:name=\"" + elementPrefix + namespaceDelimiter
											+ elementName + "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs
											+ "\" w:maxOccurs=\"" + maxoccurs + "\"/>\n");
								}

							// export enumerations
							String codeList = c.propertyValue(codeListProperty);
							if (codeList != null) {
								trace("exportWantlist: Exporting numerations for " + c.pretty_name());
								String[] codes = codeList.split(codeListDelimiter);
								for (String co : codes) {
									co = co.trim();
									if (!co.equals(""))
										fw.write("<w:Facet w:facet=\"enumeration\" w:value=\""
												+ co.replace("\"", "&quot;") + "\"/>");
								}
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
	public static String extensionSchema(String prefix) {
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
	public static UmlClass findType(UmlPackage parentPackage, String schemaURI, String tagName) {
		trace("findType: " + schemaURI + namespaceDelimiter + tagName);
		if (tagName.equals("abstract")) {
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

	// JSON Pointer to an element
	public static String getJSONElement(String elementName, String localPrefix) {
		if (elementName == null)
			return "";
		String prefix = getPrefix(elementName);
		if (isExternalPrefix(prefix))
			return "";
		else if (prefix.equals(localPrefix))
			return "#/properties/" + elementName;
		else if (isNiemPrefix(prefix) && !isNiemPrefix(localPrefix))
			return niemDir + "/" + prefix + ".schema.json#/properties/" + elementName;
		else if (!isNiemPrefix(prefix) && isNiemPrefix(localPrefix))
			return "../" + prefix + ".schema.json#/properties/" + elementName;		
		else
			return "" + prefix + ".schema.json#/properties/" + elementName;
	}

	// JSON Pointer to a type
	public static String getJSONType(String typeName, String localPrefix) {
		if (typeName == null)
			return "";
		String prefix = getPrefix(typeName);
		if (isExternalPrefix(prefix))
			return "";
		else if (prefix.equals(localPrefix))
			return "#/definitions/" + typeName;
		if (isNiemPrefix(prefix) && !isNiemPrefix(localPrefix))
			return niemDir + "/" + prefix + ".schema.json#/definitions/" + typeName;
		else if (!isNiemPrefix(prefix) && isNiemPrefix(localPrefix))
			return "../" + prefix + ".schema.json#/definitions/" + typeName;		
		else
			return "" + prefix + ".schema.json#/definitions/" + typeName;
	}
	
	// JSON property description of an element
	public static String getJSONProperty(String elementName, String minoccurs, String maxoccurs, String localPrefix) {
		String property = "";
		property += "\"" + elementName + "\": {\n";
		if (maxoccurs.equals("1"))
			property += "\"$ref\": \"" + getJSONElement(elementName, localPrefix) + "\"\n";
		else {
			property += "\"oneOf\": [";
			if (minoccurs.equals("0") || minoccurs.equals("1")) {
				property += "{\n" 
							+ "\"$ref\": \"" + getJSONElement(elementName, localPrefix) + "\"\n"
						+ "},\n";
			}
			property += "{\n" 
							+ "\"items\": {\n"
								+ "\"$ref\": \"" + getJSONElement(elementName, localPrefix) + "\"\n"
							+ "},\n"
							+ "\n\"minItems\": " + minoccurs
						+ ",\n";
			if (!maxoccurs.equals("unbounded"))
				property += "\n\"maxItems\": " + maxoccurs + ",\n";
			property 		+= "\"type\": \"array\"\n"
						+ "}\n"
					+ "]\n";
		}
		property += "}";
		
		return property;
	}
	// extract tagname from XML tag
	public static String getName(String typeName) {
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i >= 0) ? typeName.substring(i + 1) : typeName;
	}

	// return NIEM version
	public static String getNiemVersion() {
		String niemVersion = "4.0";
		/*
		 * String schemaURI = Prefixes.get("nc"); //Matcher mat =
		 * Pattern.compile(".*?niem-core/.*?").matcher(schemaURI); Matcher mat =
		 * Pattern.compile(".*niem-core/(.*?)/").matcher(schemaURI);
		 * //UmlCom.trace("NIEM URI: " + schemaURI); if (mat.find()) niemVersion =
		 * mat.group(1);
		 */
		UmlCom.trace("NIEM version: " + niemVersion);
		return niemVersion;
	}

	// extract namespace prefix from XML tag
	public static String getPrefix(String typeName) {
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i >= 0) ? typeName.substring(0, i).trim() : "";
	}

	// hide reference model from documentation
	public static void hideReferenceModel() {
		hideItem(referencePackage);
	}

	// hide item from documentation
	public static void hideItem(UmlItem item) {
		item.known = false;
		for (UmlItem child : item.children())
			hideItem(child);
	}

	// import NIEM mapping spreadsheet in CSV format
	public static void importCsv(String filename, String externalSchemas) {

		setExternalSchemas(externalSchemas);
		// Cache UML classes
		Map<String, UmlClass> UMLClasses = new HashMap<String, UmlClass>();
		Map<String, UmlClassInstance> UMLInstances = new HashMap<String, UmlClassInstance>();
		for (int i = 0; i < UmlItem.all.size(); i++) {
			UmlItem item = (UmlItem) UmlItem.all.elementAt(i);
			if (item.stereotype().equals(niemStereotype))
				if (item.kind() == anItemKind.aClass) {
					UmlClass c = (UmlClass) item;
					if (!UMLClasses.containsKey(c.pretty_name()))
						UMLClasses.put(c.pretty_name(), c);
				} else if (item.kind() == anItemKind.aClassInstance) {
					UmlClassInstance ci = (UmlClassInstance) item;
					if (!UMLInstances.containsKey(ci.pretty_name()))
						UMLInstances.put(ci.pretty_name(), ci);
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

				if (!className.equals("")) {
					UmlClass c = UMLClasses.get(className);
					if (c != null) {
						if (attributeName.equals("")) {
							// Import NIEM Mapping to Class
							UmlCom.trace("Importing NIEM mapping for " + className);
							for (int p = 4; p < map.length && p < nextLine.length; p++)
								c.set_PropertyValue(niemProperty(p), nextLine[p]);
						} else {
							// Import NIEM Mapping to Attribute
							for (UmlItem item : c.children())
								if (item.stereotype().equals(niemStereotype)
										&& (item.pretty_name().equals(attributeName)))
									for (int p = 4; p < map.length && p < nextLine.length; p++)
										item.set_PropertyValue(niemProperty(p), nextLine[p]);
						}
					}
				} else if (!attributeName.equals("")) {
					UmlClassInstance ci = UMLInstances.get(attributeName);
					if (ci != null) {
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
	public static Namespace importElements(DocumentBuilder db, String filename) {
		trace("Importing elements from schema " + filename);
		String fn = "\n" + filename + "\n";
		Namespace ns = null;
		try {
			// parse the schema
			Document doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = importNamespaces(doc);

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

		return ns;
	}

	// import NIEM reference model elements in Types into HashMaps
	public static Namespace importElementsInTypes(DocumentBuilder db, String filename) {
		trace("Importing elements in types from schema " + filename);
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
		// TODO import elements in complex types without complexContent (used in structures.xsd)
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
				List<UmlClassInstance> enlist = (List<UmlClassInstance>) (NiemElementsInType.get(uri));
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

		return ns;
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
	public static void importSchemaDir(String dir, Boolean includeEnums, String externalSchemas) throws IOException {

		// cache reference model
		cacheModel(referencePackage);
		setExternalSchemas(externalSchemas);

		// import abstract types
		UmlClassView cv;
		if (referenceAbstractType == null) {
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
		if (referenceAnyElement == null) {
			referenceAnyElement = addElement(null, cv, XMLConstants.W3C_XML_SCHEMA_NS_URI, anyElementName, null, "");
			if (referenceAnyElement != null)
				NiemElements.put(referenceAnyElement.propertyValue(uriProperty), referenceAnyElement);
		}

		// Configure DOM
		Path path = FileSystems.getDefault().getPath(dir);
		String importPath = path.toString();
		// String importPath = "";

		int passes = (includeEnums) ? 4 : 3;

		// Walk directory to import in passes (0: types, 1: elements, 2:
		// elements in types, 3: enumerations
		for (importPass = 0; importPass < passes; importPass++) {
			switch (NiemTools.importPass) {
			case 0:
				UmlCom.trace("\nImporting types");
				break;
			case 1:
				UmlCom.trace("\nImporting elements");
				break;
			case 2:
				UmlCom.trace("\nImporting elements in types");
				break;
			// TODO import attributes and attribute groups (used in structures.xsd)
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
						UmlCom.trace("importSchemaDir: could not configure parser " + e.toString());
					}

					String filename = file.toString();
					String filepath1 = filename.replaceFirst(java.util.regex.Matcher.quoteReplacement(importPath), "");
					String filepath = filepath1.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), "/");
					if (filename.endsWith(".xsd")) {
						UmlCom.trace("Importing " + filepath);
						switch (NiemTools.importPass) {
						case 0:
							Namespace ns = importTypes(db, filename);
							ns.referenceClassView.set_PropertyValue(filepathProperty, "niem" + filepath);
							break;
						case 1:
							importElements(db, filename);
							break;
						case 2:
							importElementsInTypes(db, filename);
							break;
						}
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
	public static Namespace importTypes(DocumentBuilder db, String filename) {
		trace("Importing types from schema " + filename);
		String fn = "\n" + filename + "\n";
		Namespace ns = null;
		try {
			// parse the schema
			Document doc = db.parse(new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			ns = importNamespaces(doc);

			// compile XPath queries
			XPathExpression xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");
			XPathExpression xe1 = xPath.compile("xs:restriction[1]/xs:enumeration");
			// XPathExpression xe2 = xPath.compile("xs:annotation[1]/xs:documentation[1]");

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
					if (nodeType == "xs:simpleType") {
						c.set_Stereotype("enum_pattern");
						// import enumerated values for simple types (codes)
						NodeList elist = (NodeList) xe1.evaluate(e, XPathConstants.NODESET);
						String codeList = "";
						for (int j = 0; j < elist.getLength(); j++) {
							Element e2 = (Element) elist.item(j);
							String v = e2.getAttribute("value");
							// String d = xe2.evaluate(e2);
							// codeList += v + "=" + d + "; ";
							String codeDescription = xe.evaluate(e2);
							v.replace(";",",").replace("=", "-");
					/*		if (codeDescription != null && !codeDescription.equals("")) {
								codeDescription.replace(";",",").replace("=", "-");
								codeList += v + codeListDefDelimiter + codeDescription + codeListDelimiter + " ";
							} else */
								codeList += v + codeListDelimiter + " ";
						}
						if (!codeList.equals(""))
							c.set_PropertyValue(codeListProperty, codeList);
					}
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

		return ns;
	}

	// identify UBL types and elements
	public static Boolean isExternalPrefix(String prefix) {
		// String prefix = getPrefix(tagName);
		return externalPrefixes.contains(prefix);
	}

	// identify UBL types and elements
	public static Boolean isNiemPrefix(String prefix) {
		String schemaURI = Prefixes.get(prefix);
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null)
			return false;
		return ns.referenceClassView != null;
	}

	public static void setExternalSchemas(String externalSchemas) {
		String[] external = externalSchemas.split(",");
		for (int i = 0; i < external.length; i++) {
			String[] part = external[i].split("=");
			if (part.length > 2) {
				String prefix = part[0].trim();
				String schemaURI = part[1].trim();
				String schemaLocation = part[2].trim();
				externalPrefixes.add(prefix);
				Prefixes.put(prefix, schemaURI);
				externalSchemaURL.put(schemaURI, schemaLocation);
			}
		}
	}

	// indicate whether an element exists in reference model
	public static Boolean isNiemElement(String elementName) {

		if (elementName.equals("") || elementName.equals("??") || isExternalPrefix(getPrefix(elementName)))
			return false;

		trace("isNiemElement: Find element " + elementName);
		String prefix = getPrefix(elementName);
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null) {
			UmlCom.trace("isNiemElement: Cannot find prefix " + prefix + " for element " + elementName);
			return false;
		}
		String tagName = getName(elementName);
		String tagName2 = tagName.replace("-", "");
		trace("isNiemElement Searching for element " + schemaURI + hashDelimiter + tagName2);
		return NiemElements.containsKey(schemaURI + hashDelimiter + tagName2);
	}

	// get type by prefix and tagname
	public static Boolean isNiemElementInType(String typeName, String elementName) {
		trace("isNiemElement: is element " + elementName + " in type " + typeName + "?");
		if (!isNiemType(typeName) || !isNiemElement(elementName))
			return false;

		// parse type
		String typePrefix = getPrefix(typeName);
		String typeSchemaURI = Prefixes.get(typePrefix);
		if (typeSchemaURI == null) {
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
		if (list == null) {
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
		if (typeName.equals("") || typeName.equals("??") || isExternalPrefix(getPrefix(typeName)))
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

	// return the NIEM stereotype associated with a column in the NIEM mapping
	// spreadsheet
	public static String niemProperty(int p) {
		return niemStereotype + stereotypeDelimiter + map[p][1];
	}

	// output UML objects
	/*
	 * public static void outputUML() {
	 * 
	 * @SuppressWarnings("unchecked") Iterator<UmlItem> it =
	 * UmlClass.all.iterator(); while (it.hasNext()) { UmlItem item = it.next(); if
	 * (item.known) { UmlCom.trace("\nKind: " +
	 * String.valueOf(item.kind().value())); UmlCom.trace("ID: " +
	 * String.valueOf(item.getIdentifier())); UmlCom.trace("Name: " +
	 * item.pretty_name()); if (item.parent() != null) { UmlCom.trace("Parent: " +
	 * String.valueOf(item.parent().getIdentifier())); UmlCom.trace("Parent Name: "
	 * + String.valueOf(item.parent().pretty_name())); } } else
	 * UmlCom.trace("\nName: " + item.pretty_name()); } }
	 */

	// (re-)associate the NIEM stereotype with all properties in the UML
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

	// output debugging information
	public static void trace(String output) {
		if (_TRACE)
			UmlCom.trace(output);
	}

	// verify reference model exists (NIEM)
	public static boolean verifyNIEM(UmlPackage root) {
		UmlPackage pimPackage = null;

		trace("Creating NIEM folders");
		// Find NIEM package
		for (UmlItem ch : root.children()) {
			if (ch.pretty_name().equals("NIEM"))
				if ((ch.kind().value() == anItemKind._aPackage)) {
					pimPackage = (UmlPackage) ch;
					break;
				}
		}
		if (pimPackage != null)
			// Find package "NIEMReference"
			for (UmlItem ch : pimPackage.children()) {
				if (ch.pretty_name().equals("NIEMReference"))
					if ((ch.kind().value() == anItemKind._aPackage)) {
						referencePackage = (UmlPackage) ch;
						return true;
					}
			}

		UmlCom.trace("NIEM reference model does not exist.  Import NIEM reference schemas first.");
		return false;

	}

	// output a column of the NIEM mapping spreadsheet in HTML format
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
			trace(item.pretty_name());
			switch (item.kind().value()) {
			case anItemKind._aClass: {
				fw.write("<tr bgcolor=\"#f0f0f0\"><td style=\"word-wrap: break-word\">");
				writeItemHtml(fw, item);
				fw.write("</td><td>");
				fw.write("</td><td>");
			}
				break;
			case anItemKind._anAttribute: {
				fw.write("<tr><td style=\"word-wrap: break-word\">");
				writeItemHtml(fw, item.parent());
				fw.write("</td><td style=\"word-wrap: break-word\">");
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
					fw.write("<tr><td style=\"word-wrap: break-word\">");
					writeItemHtml(fw, item.parent());
					fw.write("</td><td style=\"word-wrap: break-word\">");
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

				// determine if this is an extension
				/*
				 * Boolean extension = false; String[] xPathElements = column[4].split("/"); for
				 * (String element : xPathElements) { String prefix = getPrefix(element); if
				 * (!prefix.equals("") && !isNiemSchema(prefix) &&
				 * !isExternalPrefix(getPrefix(element.trim()))) { extension = true; continue; }
				 * }
				 */

				// export XPath
				String XPath = column[4].trim();
				String oldXPath = column[9].trim();
				// bgcolor = (extension) ? extensionBGColor : defaultBGColor;
				bgcolor = defaultBGColor;
				fgcolor = (XPath.equals(oldXPath)) ? defaultFGColor : changedFGColor;
				fw.write(columnHtml(XPath, bgcolor, fgcolor, true));

				// export Type
				String type = column[5].trim();
				String typePrefix = getPrefix(type);
				bgcolor = defaultBGColor;
				fgcolor = defaultFGColor;
				if (!type.equals("")) {
					if (isNiemPrefix(getPrefix(type)) && !isNiemType(type))
						fgcolor = invalidFGColor;
					if (!isNiemPrefix(typePrefix) && !isExternalPrefix(typePrefix))
						bgcolor = extensionBGColor;
				}
				fw.write(columnHtml(type, bgcolor, fgcolor, true));

				// export Property
				String property = column[6];
				fgcolor = defaultFGColor;
				bgcolor = defaultBGColor;
				if (!property.equals("")) {
					String[] pp = property.split(",");
					for (String ppp : pp) {
						ppp = ppp.trim();
						Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
						if (!mat.find()) {
							String prefix = getPrefix(ppp);
							if (isNiemPrefix(typePrefix) && isNiemPrefix(prefix) && !isNiemElementInType(type, ppp))
								fgcolor = invalidFGColor;
							prefix = getPrefix(property);
							if (!isNiemPrefix(prefix) && !isExternalPrefix(prefix))
								bgcolor = extensionBGColor;
						}
					}
				}
				fw.write(columnHtml(property, bgcolor, fgcolor, true));

				// export BaseType
				String baseType = column[7].trim();
				String basePrefix = getPrefix(baseType);
				bgcolor = defaultBGColor;
				fgcolor = defaultFGColor;
				if (!baseType.equals("") && !baseType.equals(abstractTypeName)) {
					if (!isNiemPrefix(basePrefix) && !isExternalPrefix(basePrefix))
						bgcolor = extensionBGColor;
					if (isNiemPrefix(basePrefix) && !isNiemType(baseType))
						fgcolor = invalidFGColor;
				}
				fw.write(columnHtml(baseType, bgcolor, fgcolor, true));

				// export Multiplicity
				bgcolor = defaultBGColor;
				String multiplicity = column[8];
				fgcolor = (multiplicity.equals(column[10])) ? defaultFGColor : changedFGColor;
				String minoccurs, maxoccurs;
				if ((multiplicity.equals("")))
					minoccurs = maxoccurs = "1";
				else if (multiplicity.contains(",")) {
					String[] occurs = multiplicity.split(",");
					minoccurs = occurs[0];
					maxoccurs = occurs[1];
				} else
					minoccurs = maxoccurs = multiplicity;
				try {
					if (Integer.parseInt(minoccurs) < 0)
						throw new NumberFormatException();
					if (!maxoccurs.equals("unbounded") && (Integer.parseInt(maxoccurs) < 1))
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					fgcolor = invalidFGColor;
				}
				fw.write(columnHtml(column[8], bgcolor, fgcolor, false));

				// export Old XPath
				bgcolor = defaultBGColor;
				fgcolor = defaultFGColor;
				fw.write(columnHtml(column[9], bgcolor, fgcolor, true));

				// export Old Multiplicity
				bgcolor = defaultBGColor;
				fw.write(columnHtml(column[10], bgcolor, fgcolor, false));

				// export NIEM Mapping Notes
				bgcolor = defaultBGColor;
				fw.write(columnHtml(column[11], bgcolor, fgcolor, true));

				// export code list
				bgcolor = defaultBGColor;
				fw.write(columnHtml(column[12], bgcolor, fgcolor, true));
			}

			fw.write("</tr>");
		} catch (Exception e) {
			UmlCom.trace("writeLineHtml: " + e.toString());
		}
	}
}
