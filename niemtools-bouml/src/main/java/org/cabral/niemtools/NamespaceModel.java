package org.cabral.niemtools;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import fr.bouml.UmlClassView;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class NamespaceModel {

	static final String ATTRIBUTE_PREFIX = "@";
	private static Set<String> externalPrefixes = new HashSet<String>();
	private static Map<String, String> externalSchemaURL = new HashMap<String, String>();
	public static final String NAMESPACE_ATTRIBUTE = XMLConstants.XMLNS_ATTRIBUTE;
	private static final String NAMESPACE_DELIMITER = ":";
	private static Map<String, Namespace> Namespaces = new HashMap<String, Namespace>();
	private static Map<String, String> Prefixes = new HashMap<String, String>();
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	
	/** adds a new namespace
	 * @param schemaURI
	 * @return
	 */
	static Namespace addNamespace(String schemaURI) {
		Namespace ns = new Namespace(schemaURI);
		Namespaces.put(schemaURI, ns);
		Log.debug("getNamespaceClassView: added namespace " + schemaURI);
		return ns;
	}

	/** adds a new prefix for a namespace
	 * @param schemaURI
	 * @param prefix
	 */
	static void addPrefix(String schemaURI, String prefix) {
		if (!getPrefixes().containsKey(prefix))
			Prefixes.put(prefix, schemaURI);
	}

	/** caches namespaces and prefixes for external schemas
	 * 
	 */
	static void cacheExternalSchemas() {
		String externalSchemas = NiemUmlClass.getProperty(ConfigurationDialog.IEPD_EXTERNAL_SCHEMAS_PROPERTY);
		String[] external = externalSchemas.split(",");
		for (int schemaIndex = 0; schemaIndex < external.length; schemaIndex++) {
			String[] part = external[schemaIndex].split("=");
			if (part.length > 2) {
				String prefix = part[0].trim();
				String schemaURI = part[1].trim();
				String schemaLocation = part[2].trim();
				externalPrefixes.add(prefix);
				Prefixes.put(prefix, schemaURI);
				externalSchemaURL.put(schemaURI, schemaLocation);
			}
		}
		Log.debug("cacheExternalSchemas: external schemas cached");
	}

	/**
	 * @param attributeName
	 * @return attribute name with prefix filtered as a String
	 */
	static String filterAttributePrefix(String attributeName) {
		return attributeName.replaceAll(ATTRIBUTE_PREFIX, "");
	}

	/** filter illegal characters in XML prefix
	 * @param prefix
	 * @return filtered prefix as a String
	 */
	static String filterPrefix(String prefix) {
		return prefix.replaceAll("[^-._A-Za-z0-9]", "");
	}

	/**
	 * @param prefix
	 * @return an extension schema URI as a String
	 */
	static String getExtensionSchema(String prefix) {
		return NiemUmlClass.getProperty(ConfigurationDialog.IEPD_URI_PROPERTY) + "/" + prefix;
	}
	
	/**
	 * @param schemaURI
	 * @return external schema URI for schemaURI as a String
	 */
	static String getExternalSchemaURL(String schemaURI) {
		return externalSchemaURL.get(schemaURI);
	}
	
	/**
	 * @param tagName
	 * @return tagname from XML tag as a String
	 */
	static String getName(String tagName) {
		if (tagName == null)
			return "";
		int i = tagName.indexOf(NAMESPACE_DELIMITER);
		String name = (i >= 0) ? tagName.substring(i + 1) : tagName;
		// return name.replaceAll("[^-._:A-Za-z0-9]", "");
		return name.replaceAll("[^-._:A-Za-z0-9@]", "");
	}

	/**
	 * @param item
	 * @return tagname for UmlItem item as a String
	 */
	static String getName(UmlItem item) {
		return getName(item.name());
	}

	/**
	 * @param schemaURI
	 * @return namespace for schemaURI as a Namespace
	 */
	static Namespace getNamespace(String schemaURI) {
		if (schemaURI == null)
			return null;
		return Namespaces.get(schemaURI);
	}

	/**
	 * @param model
	 * @param prefix
	 * @param schemaURI
	 * @return namespace class view for schema schemaURI in model modelPackage as a UmlClassView; create it with
	 * prefix if doesn't exist
	 */
	static UmlClassView getNamespaceClassView(NiemModel model, String prefix, String schemaURI) {
		if (schemaURI == null || prefix == null) {
			Log.trace("getNamespaceClassView: error - prefix is " + prefix + " and schemaURI is " + schemaURI);
			return null;
		}
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null)
			// create namespace
			ns = addNamespace(schemaURI);

		String prefix2 = filterPrefix(prefix);
		if (!Prefixes.containsKey(prefix2)) {
			// create prefix
			Prefixes.put(prefix2, schemaURI);
			Log.debug("getNamespaceClassView: added prefix " + prefix2 + " for " + schemaURI);
		}
		// select reference, subset or extension model
		if (model == null)
			model = isNiemPrefix(prefix) ? NiemUmlClass.getSubsetModel() : NiemUmlClass.getExtensionModel();

		// return classview if it exists
		UmlClassView namespaceClassView = (model == NiemUmlClass.getReferenceModel()) ? ns.getReferenceClassView() : ns.getNsClassView();	
		if (namespaceClassView != null)
			return namespaceClassView;

		// create classview
		Log.debug("getNamespaceClassView: adding classview for prefix " + prefix2 + " and schema " + schemaURI);
		String prefix3 = prefix2;

		int conflictCounter = 1;

		while (namespaceClassView == null) {
			try {
				namespaceClassView = UmlClassView.create(model.getModelPackage(), prefix3);
			} catch (Exception e) {
				Log.trace("getNamespaceClassView: multiple namespace URIs for prefix " + prefix3 + " " + schemaURI + " and "
						+ Prefixes.get(prefix2));
				prefix3 = prefix2 + conflictCounter++;
			}
		}
		namespaceClassView.set_PropertyValue(NiemModel.URI_PROPERTY, schemaURI);
		Log.debug("getNamespaceClassView: added class view " + namespaceClassView.name());

		if (prefix != null)
			namespaceClassView.set_PropertyValue(NiemUmlClass.PREFIX_PROPERTY, prefix2);

		if (model == NiemUmlClass.getReferenceModel())
			ns.setReferenceClassView(namespaceClassView);
		else {
			ns.setNsClassView(namespaceClassView);
			if (model == NiemUmlClass.getExtensionModel()) {
				ns.setNsClassView(namespaceClassView);
				ns.setFilepath(prefix2 + XmlWriter.XSD_FILE_TYPE);
				namespaceClassView.set_PropertyValue(NiemUmlClass.FILE_PATH_PROPERTY, ns.getFilepath());
			}
		}
		return namespaceClassView;
	}
	
	/**
	 * @param tagName
	 * @return namespace prefix from XML tag as a String
	 */
	static String getPrefix(String tagName) {
		if (tagName == null) {
			// trace("getPrefix: error - tagName is null");
			return null;
		}
		int i = tagName.indexOf(NAMESPACE_DELIMITER);
		if (i >= 0) {
			String prefix = tagName.substring(0, i).trim();
			return filterPrefix(prefix);
		} else
			return null;
	}

	/**
	 * @param item
	 * @return namespace prefix for UmlItem item as a String
	 */
	static String getPrefix(UmlItem item) {
		switch (item.kind().value()) {
		case anItemKind._aClass:
		case anItemKind._aClassInstance:
			return getPrefix(item.parent());
		case anItemKind._aClassView:
			String prefix = item.propertyValue(NiemUmlClass.PREFIX_PROPERTY);
			if (prefix != null)
				return filterPrefix(prefix);
		default:
			break;
		}
		Log.trace("getPrefix - error - no prefix for " + item.name());
		return null;
	}
	
	/**
	 * @param prefix
	 * @param tagName
	 * @return name with namespace prefix for an attribute with name tagName as a String
	 */
	static String getPrefixedAttributeName(String prefix, String tagName) {
		return prefix + NAMESPACE_DELIMITER + ATTRIBUTE_PREFIX + filterAttributePrefix(tagName);
	}

	/**
	 * @param prefix
	 * @param tagName
	 * @return name with namespace prefix for type or element with name tagName as a String
	 */
	static String getPrefixedName(String prefix, String tagName) {
		return prefix + NAMESPACE_DELIMITER + tagName;
	}
	/**
	 * @param item
	 * @return name with namespace prefix for UmlItem item as a String
	 */
	static String getPrefixedName(UmlItem item) {
		if (item == null) {
			Log.debug("getPrefixedName: null item");
			return null;
		}
		return getPrefix(item) + NAMESPACE_DELIMITER + item.name();
	}

	/**
	 * @return Map of prefixes to Namespaces
	 */
	static Map<String, String> getPrefixes() {
		return Prefixes;
	}

	/**
	 * @param tagName
	 * @return schemaURI for type or element with name tagname as a String
	 */
	static String getSchemaURI(String tagName) {
		String prefix = getPrefix(tagName);
		if (prefix == null)
			// prefix = LOCAL_PREFIX;
			return null;
		return getSchemaURIForPrefix(prefix);
	}

	/**
	 * @param prefix
	 * @return schemaURI for type or element with name tagname as a String
	 */
	static String getSchemaURIForPrefix(String prefix) {
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = getExtensionSchema(prefix);
		return schemaURI;
	}

	/**
	 * @return number of namespaces as an int
	 */
	static int getSize() {
		return Namespaces.size();
	}
	
	/** import namespaces and return target namespace
	 * @param doc
	 * @return target namespace
	 */
	static Namespace importNamespaces(Document doc) {
		
		// get target namespace
		Namespace ns = null;
		String targetSchemaURI=null;
		try {
			xPath.setNamespaceContext(new NamespaceResolver(doc, true));
			targetSchemaURI = xPath.evaluate("xs:schema/@targetNamespace", doc);
			if (targetSchemaURI == null)
				targetSchemaURI = NiemModel.LOCAL_URI;
			Log.debug("importNamespaces: target schema URI " + targetSchemaURI);
			
			// create namespaces and classviews
			NamedNodeMap nslist = doc.getDocumentElement().getAttributes();
			for (int nsIndex = 0; nsIndex < nslist.getLength(); nsIndex++) {
				Node attributeNode = nslist.item(nsIndex);
				String attributeNodeName = attributeNode.getNodeName();
				String schemaURI = attributeNode.getNodeValue();
				// parser filters "xml" namespace definition so it is hardcoded
				if (schemaURI.equals(NiemModel.XML_URI))
					attributeNodeName = NAMESPACE_ATTRIBUTE + NAMESPACE_DELIMITER + NiemModel.XML_PREFIX;
				Log.debug("importNamespaces: processing attribute " + attributeNodeName);
				if (attributeNodeName.startsWith(NAMESPACE_ATTRIBUTE) && !attributeNodeName.equals(NAMESPACE_ATTRIBUTE)) {

					String prefix = attributeNodeName.substring(6);
					UmlClassView classView = getNamespaceClassView(NiemUmlClass.getReferenceModel(), prefix, schemaURI);
					
					// get target namespace description
					if (schemaURI.equals(targetSchemaURI) && classView != null)
						classView.set_Description(xPath.evaluate("xs:schema/xs:annotation[1]/xs:documentation[1]", doc));
				}
			}
			ns = getNamespace(targetSchemaURI);
			if (ns == null) {
				UmlClassView classView = getNamespaceClassView(NiemUmlClass.getReferenceModel(), targetSchemaURI, targetSchemaURI);
				ns = getNamespace(targetSchemaURI);
				if (classView != null)
					classView.set_Description(xPath.evaluate("xs:schema/xs:annotation[1]/xs:documentation[1]", doc));
			}
		} catch (Exception e) {
			Log.trace("importNamespaces: error - could not create namespace for schema " + targetSchemaURI);
		} 
		
		return ns;
	}

	/**
	 * @param prefix
	 * @return true if type or element has a prefix in an external schema
	 */
	static Boolean isExternalPrefix(String prefix) {
		if (prefix == null)
			return false;
		return externalPrefixes.contains(prefix);
	}
	
	/**
	 * @param prefix
	 * @return true if a prefix exists in reference model
	 */
	static Boolean isNiemPrefix(String prefix) {
		if (prefix == null)
			return false;
		if (isExternalPrefix(prefix))
			return false;
		String schemaURI = Prefixes.get(prefix);
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null)
			return false;
		return ns.getReferenceClassView() != null;
	}

/**
	 * @param name
	 * @return attribute name with prefix filtered as a String
	 */
	static Boolean isAttribute(String name) {
		String tagname = getName(name);
		return tagname.startsWith(ATTRIBUTE_PREFIX);
	}
}

