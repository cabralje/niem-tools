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
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NamespaceResolver implements NamespaceContext {
	private static final String DEFAULT_NS = "DEFAULT";
	private Map<String, String> prefix2Uri = new HashMap<String, String>();
	private Map<String, String> uri2Prefix = new HashMap<String, String>();

	/**
	 * This constructor parses the document and stores all namespaces it can
	 * find. If toplevelOnly is true, only namespaces in the root are used.
	 * 
	 * @param document
	 *            source document
	 * @param toplevelOnly
	 *            restriction of the search to enhance performance
	 */
	public NamespaceResolver(Document document, boolean toplevelOnly) {
		examineNode(document.getFirstChild(), toplevelOnly);
		//   System.out.println("The list of the cached namespaces:");
		//   for (String key : prefix2Uri.keySet()) {
		//       System.out
		//               .println("prefix " + key + ": uri " + prefix2Uri.get(key));
		//   }
	}

	/**
	 * A single node is read, the namespace attributes are extracted and stored.
	 * 
	 * @param node
	 *            to examine
	 * @param attributesOnly,
	 *            if true no recursion happens
	 */
	private void examineNode(Node node, boolean attributesOnly) {

			if (node.hasAttributes())
			{
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);
					storeAttribute((Attr) attribute);
				}
			}

			if (!attributesOnly && node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if (child.getNodeType() == Node.ELEMENT_NODE)
						examineNode(child, false);
				}
			}

	}

	/**
	 * This method looks at an attribute and stores it, if it is a namespace
	 * attribute.
	 * 
	 * @param attribute
	 *            to examine
	 */
	private void storeAttribute(Attr attribute) {
		// examine the attributes in namespace xmlns
		if (attribute.getNamespaceURI() != null
				&& attribute.getNamespaceURI().equals(
						XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
			// Default namespace xmlns="uri goes here"
			if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
				putInCache(DEFAULT_NS, attribute.getNodeValue());
			} else {
				// The defined prefixes are stored here
				putInCache(attribute.getLocalName(), attribute.getNodeValue());
			}
		}

	}

	private void putInCache(String prefix, String uri) {
		prefix2Uri.put(prefix, uri);
		uri2Prefix.put(uri, prefix);
	}

	/**
	 * This method is called by XPath. It returns the default namespace, if the
	 * prefix is null or "".
	 * 
	 * @param prefix
	 *            to search for
	 * @return uri
	 */
	public String getNamespaceURI(String prefix) {
		if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))	
//			return prefix2Uri.get(DEFAULT_NS);
			return XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else if (prefix.equals("xs")  || prefix.equals("xsd"))
			return XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else if (prefix.equals("local"))
			return "http://local";
		else 
			return prefix2Uri.get(prefix);
	}

	/**
	 * This method is not needed in this context, but can be implemented in a
	 * similar way.
	 */
	public String getPrefix(String namespaceURI) {
		return uri2Prefix.get(namespaceURI);
	}

	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
		// Not implemented
		return null;
	}
}
