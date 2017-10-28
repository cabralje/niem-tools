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

	private static XPath xPath = XPathFactory.newInstance().newXPath();

	static Set<String> externalPrefixes = new HashSet<String>();
	static Map<String, String> externalSchemaURL = new HashMap<String, String>();

	private static final String NAMESPACE_DELIMITER = ":";

	static Map<String, Namespace> Namespaces = new HashMap<String, Namespace>();

	static Map<String, String> Prefixes = new HashMap<String, String>();

	/** caches namespaces and prefixes for external schemas */
	static void cacheExternalSchemas() {
		String externalSchemas = NiemUmlClass.getProperty(NiemUmlClass.IEPD_EXTERNAL_SCHEMAS_PROPERTY);
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

	/** filter illegal characters in XML prefix */
	static String filterPrefix(String prefix) {
		return prefix.replaceAll("[^-._A-Za-z0-9]", "");
	}

	/** return tagname from XML tag */
	static String getName(String tagName) {
		if (tagName == null)
			return "";
		int i = tagName.indexOf(NAMESPACE_DELIMITER);
		String name = (i >= 0) ? tagName.substring(i + 1) : tagName;
		// return name.replaceAll("[^-._:A-Za-z0-9]", "");
		return name.replaceAll("[^-._:A-Za-z0-9@]", "");
	}

	/** return tagname for UmlItem item */
	static String getName(UmlItem item) {
		return getName(item.name());
	}

	/**
	 * return namespace for schema schemaURI in model modelPackage; create it with
	 * prefix if doesn't exist
	 */
	static UmlClassView getNamespace(NiemModel model, String prefix, String schemaURI) {
		if (schemaURI == null || prefix == null)
			return null;
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null) {
			// create namespace
			ns = new Namespace(schemaURI);
			Namespaces.put(schemaURI, ns);
			Log.debug("getNamespace: added namespace " + schemaURI);
		}
		String prefix2 = filterPrefix(prefix);
		if (!Prefixes.containsKey(prefix2)) {
			// create prefix
			Prefixes.put(prefix2, schemaURI);
			Log.debug("getNamespace: added prefix " + prefix2 + " for " + schemaURI);
		}
		// select reference, subset or extension model
		if (model == null)
			model = isNiemPrefix(prefix) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;

		// return classview if it exists
		if (model == NiemUmlClass.ReferenceModel) {
			if (ns.referenceClassView != null)
				return ns.referenceClassView;
		} else if (ns.nsClassView != null)
			return ns.nsClassView;

		// create classview
		String prefix3 = prefix2;
		UmlClassView namespaceClassView = null;
		int conflictCounter = 1;

		while (namespaceClassView == null) {
			try {
				namespaceClassView = UmlClassView.create(model.modelPackage, prefix3);
			} catch (Exception e) {
				Log.trace("getNamespace: multiple namespace URIs for prefix " + prefix3 + " " + schemaURI + " and "
						+ Prefixes.get(prefix2));
				prefix3 = prefix2 + conflictCounter;
				conflictCounter++;
			}
		}
		namespaceClassView.set_PropertyValue(NiemUmlClass.URI_PROPERTY, schemaURI);
		// trace("getNamespace: added class view " + namespaceClassView.name());

		if (prefix != null)
			namespaceClassView.set_PropertyValue(NiemUmlClass.PREFIX_PROPERTY, prefix2);

		if (model == NiemUmlClass.ReferenceModel)
			ns.referenceClassView = namespaceClassView;
		else {
			ns.nsClassView = namespaceClassView;
			if (model == NiemUmlClass.ExtensionModel) {
				ns.nsClassView = namespaceClassView;
				ns.filepath = prefix2 + NiemUmlClass.XSD_FILE_TYPE;
				namespaceClassView.set_PropertyValue(NiemUmlClass.FILE_PATH_PROPERTY, ns.filepath);
			}
		}
		return namespaceClassView;
	}

	/** return namespace prefix from XML tag */
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

	/** return namespace prefix for UmlItem item */
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

	/** return name with namespace prefix for an attribute with name tagName */
	static String getPrefixedAttributeName(String prefix, String tagName) {
		return prefix + NAMESPACE_DELIMITER + NiemUmlClass.ATTRIBUTE_PREFIX + NamespaceModel.filterAttributePrefix(tagName);
	}

	/** return name with namespace prefix for type or element with name tagName */
	static String getPrefixedName(String prefix, String tagName) {
		return prefix + NAMESPACE_DELIMITER + tagName;
	}
	/** return name with namespace prefix for UmlItem item */
	static String getPrefixedName(UmlItem item) {
		return getPrefix(item) + NAMESPACE_DELIMITER + item.name();
	}

	/** return true if type or element has a prefix in an external schema */
	static Boolean isExternalPrefix(String prefix) {
		if (prefix == null)
			return false;
		return externalPrefixes.contains(prefix);
	}

	/** return true if a prefix exists in reference model */
	static Boolean isNiemPrefix(String prefix) {
		if (prefix == null)
			return false;
		if (NamespaceModel.isExternalPrefix(prefix))
			return false;
		String schemaURI = Prefixes.get(prefix);
		Namespace ns = Namespaces.get(schemaURI);
		if (ns == null)
			return false;
		return ns.referenceClassView != null;
	}

	public static final String NAMESPACE_ATTRIBUTE = XMLConstants.XMLNS_ATTRIBUTE;

	/** import namespaces and return target namespace */
	static Namespace importNamespaces(Document doc) {
		NamedNodeMap nslist = doc.getDocumentElement().getAttributes();
		for (int nsIndex = 0; nsIndex < nslist.getLength(); nsIndex++) {
			Node attributeNode = nslist.item(nsIndex);
			String attributeNodeName = attributeNode.getNodeName();
			if (attributeNodeName.startsWith(NAMESPACE_ATTRIBUTE)) {
				String prefix = (attributeNodeName.equals(NAMESPACE_ATTRIBUTE)) ? attributeNode.getNodeValue()
						: attributeNodeName.substring(6);
				getNamespace(NiemUmlClass.ReferenceModel, prefix, attributeNode.getNodeValue());
			}
		}

		String schemaURI = null;
		Namespace ns = null;
		try {
			// get target namespace
			schemaURI = xPath.evaluate("xs:schema/@targetNamespace", doc);
			// schemaURI = doc.lookupNamespaceURI(null);
			ns = Namespaces.get(schemaURI);
			if (ns == null)
				return Namespaces.get(NiemUmlClass.LOCAL_PREFIX);
			// set namespace description
			ns.referenceClassView
			.set_Description(xPath.evaluate("xs:schema/xs:annotation[1]/xs:documentation[1]", doc));
		} catch (Exception e) {
			Log.trace("importNamespaces: error " + e.toString());
		}
		return ns;
	}

	/** return schemaURI for type or element with name tagname */
	static String getSchemaURI(String tagName) {
		String prefix = getPrefix(tagName);
		if (prefix == null)
			// prefix = LOCAL_PREFIX;
			return null;
		String schemaURI = Prefixes.get(prefix);
		if (schemaURI == null)
			schemaURI = XmlWriter.getExtensionSchema(prefix);
		return schemaURI;
	}

	/** return attribute name with prefix filtered */
	static String filterAttributePrefix(String attributeName) {
		return attributeName.replaceAll(NiemUmlClass.ATTRIBUTE_PREFIX, "");
	}

}
