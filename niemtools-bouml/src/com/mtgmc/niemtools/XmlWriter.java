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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlClassView;
import fr.bouml.UmlItem;
import fr.bouml.UmlOperation;
import fr.bouml.UmlParameter;
import fr.bouml.UmlRelation;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class XmlWriter {

	public XmlWriter(String initialDirectory) {
		super();
		directory = initialDirectory;
	}

	String directory;

	static final String AUGMENTATION_POINT_NAME = "AugmentationPoint";

	static final String CODELIST_CODE = "code";

	static final String CODELIST_DEFINITION = "definition";

	// NIEM code lists
	static final String CODELIST_URI = "http://reference.niem.gov/niem/specification/code-lists/1.0/";

	static final String CONFORMANCE_ASSERTION_FILE = "conformance-assertion.pdf";

	static final String GC_APPINFO_PREFIX = "gca";

	static final String GC_APPINFO_URI = "http://example.org/namespace/genericode-appinfo";

	static final String GC_FILE_TYPE = ".gc";

	static final String GC_LOCATION = "https://docs.oasis-open.org/codelist/cs-genericode-1.0/xsd/genericode.xsd";

	// Genericode
	static final String GC_PREFIX = "gc";

	static final String GC_URI = "http://docs.oasis-open.org/codelist/ns/genericode/1.0/";

	// private static final String JSON_SUFFIX = "JSON";
	static final String MESSAGE_WRAPPERS_FILE_NAME = "MessageWrappers";
	static final String MPD_CATALOG_FILE = "mpd-catalog.xml";
	static final String MPD_CATALOG_LOCATION = "https://tools.niem.gov/IEPD/mpd-catalog-3.0.xsd";
	static final String MPD_CATALOG_URI = "http://reference.niem.gov/niem/resource/mpd/catalog/3.0/";
	// private static final String MPD_CATALOG_LOCATION =
	// "../../mpd-toolkit-3.0/mpd-catalog-3.0.xsd";
	static final String MPD_NC_URI = "http://release.niem.gov/niem/niem-core/3.0/";
	static final String MPD_STRUCTURES_URI = "http://release.niem.gov/niem/structures/3.0/";
	// NIEM model package descriptions
	static final String MPD_URI = "http://reference.niem.gov/niem/specification/model-package-description/3.0/";
	// NIEM naming and design rules
	static final String NC_PREFIX = "nc";
	static final String NDR_URI = "http://reference.niem.gov/niem/specification/naming-and-design-rules/3.0/";
	static final String REQUEST_MESSAGE_SUFFIX = "Request";
	static final String RESPONSE_MESSAGE_SUFFIX = "Response";
	static final String SOAP_HTTP_BINDING_URI = "http://schemas.xmlsoap.org/soap/http";
	// Web services
	static final String SOAP_PREFIX = "soap";
	static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
	static final String WANTLIST_FILE = "wantlist.xml";
	static final String WRAPPER_PREFIX = "wrapper";
	static final String WSDL_FILE_TYPE = ".wsdl";
	// Niem-tools web services
	static final String WSDL_PREFIX = "tns";
	static final String WSDL_SCHEMA_PREFIX = "wsdl";
	static final String WSDL_SCHEMA_URI = "http://schemas.xmlsoap.org/wsdl/";
	static final String WSDL_SUFFIX = "WSDL";
	static final String WSP_POLICY = "MyPolicy";
	static final String WSP_PREFIX = "wsp";
	static final String WSP_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy";
	static final String WSRMP_PREFIX = "wsrmp";
	static final String WSRMP_URI = "http://docs.oasis-open.org/ws-rx/wsrmp/200702";
	static final String WSU_PREFIX = "wsu";
	static final String WSU_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	static final String XML_CATALOG_FILE = "xml-catalog.xml";
	static final String XML_CATALOG_HEADER = "<!DOCTYPE catalog PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\" \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\n";
	// XML Catalog
	static final String XML_CATALOG_URI = "urn:oasis:names:tc:entity:xmlns:xml:catalog";

	static Set<String> CodeListNamespaces = new HashSet<String>();

	/** exports a Genericode code list */
	void exportCodeLists(NiemModel model) {

		String version = NiemUmlClass.getProperty(NiemUmlClass.IEPD_VERSION_PROPERTY);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String today = dateFormat.format(date);

		// export code lists for extension elements
		for (UmlItem item : model.modelPackage.children()) {
			if (item.kind() != anItemKind.aClassView)
				continue;
			UmlClassView classView = (UmlClassView) item;
			// classView.sort();
			for (UmlItem item2 : classView.children()) {
				if (item2.kind() != anItemKind.aClassInstance)
					continue;
				UmlClassInstance element = (UmlClassInstance) item2;
				String elementName = NamespaceModel.getName(element);
				String codeList = element.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
				if (codeList == null || codeList.trim().equals(""))
					continue;
				String codeListURI = XmlWriter.getExtensionSchema(elementName);
				CodeListNamespaces.add(elementName);

				// export code list
				Log.debug("exportCodeList: exporting code list " + elementName + GC_FILE_TYPE);
				try {
					File file = Paths.get(directory, elementName + GC_FILE_TYPE).toFile();
					File parentFile = file.getParentFile();
					if (parentFile != null)
						parentFile.mkdirs();
					FileWriter fw = new FileWriter(file);
					fw.write(NiemUmlClass.XML_HEADER + NiemUmlClass.XML_ATTRIBUTION + "<gc:CodeList ");
					writeXmlNs(fw, NiemUmlClass.CT_PREFIX, NiemUmlClass.CT_URI);
					writeXmlNs(fw, GC_PREFIX, GC_URI);
					writeXmlNs(fw, GC_APPINFO_PREFIX, GC_APPINFO_URI);
					writeXmlNs(fw, NiemModel.XSI_PREFIX, NiemModel.XSI_URI);
					writeXmlAttribute(fw, NiemModel.XSI_PREFIX + ":schemaLocation", GC_URI + " " + GC_LOCATION);
					fw.write(">" + "<Annotation>" + "<AppInfo>" + "<gca:ConformanceTargets ct:conformanceTargets=\""
							+ CODELIST_URI + "#GenericodeCodeListDocument\"/>" + "</AppInfo>" + "</Annotation>"
							+ "<Identification>" + "<ShortName>" + elementName + "</ShortName>" + "<Version>" + version
							+ "</Version>" + "<CanonicalUri>" + codeListURI + "</CanonicalUri>"
							+ "<CanonicalVersionUri>" + codeListURI + "/" + today + "</CanonicalVersionUri>"
							+ "</Identification>" + "<ColumnSet>" + "<Column Id=\"" + CODELIST_CODE
							+ "\" Use=\"required\">" + "<ShortName>" + CODELIST_CODE + "</ShortName>" + "<CanonicalUri>"
							+ CODELIST_URI + "column/" + CODELIST_CODE + "</CanonicalUri>"
							+ "<Data Type=\"normalizedString\" Lang=\"en\"/>" + "</Column>" + "<Column Id=\""
							+ CODELIST_DEFINITION + "\" Use=\"optional\">" + "<ShortName>" + CODELIST_DEFINITION
							+ "</ShortName>" + "<CanonicalUri>" + CODELIST_URI + "column/" + CODELIST_DEFINITION
							+ "</CanonicalUri>" + "<Data Type=\"normalizedString\" Lang=\"en\"/>" + "</Column>"
							+ "<Key Id=\"codeKey\">" + "<ShortName>CodeKey</ShortName>" + "<ColumnRef Ref=\""
							+ CODELIST_CODE + "\"/>" + "</Key>" + "</ColumnSet>" + "<SimpleCodeList>");
					if (codeList.contains(NiemUmlClass.CODELIST_DELIMITER)) {
						String[] codes = codeList.split(NiemUmlClass.CODELIST_DELIMITER);
						for (String code : codes) {
							String[] pairs = code.split(NiemUmlClass.CODELIST_DEFINITION_DELIMITER);
							fw.write("<Row><Value ColumnRef=\"" + CODELIST_CODE + "\"><SimpleValue>" + pairs[0].trim()
									+ "</SimpleValue></Value>");
							if (pairs.length > 1)
								fw.write("<Value ColumnRef=\"" + CODELIST_DEFINITION + "\"><SimpleValue>"
										+ pairs[1].trim() + "</SimpleValue></Value>");
							fw.write("</Row>");
						}
					}
					fw.write("</SimpleCodeList></gc:CodeList>");
					fw.close();

				} catch (IOException e) {
					Log.trace("exportCodeList: IO exception: " + e.toString());
				} catch (RuntimeException e) {
					Log.trace("exportCodeList: Runtime Exception: " + e.toString());
				}
			}
		}
	}

	/** exports a NIEM MPD catalog */
	void exportMpdCatalog(Set<String> messages)
			throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String today = dateFormat.format(date);

		Log.trace("Generating MPD catalog");
		File file = Paths.get(directory, MPD_CATALOG_FILE).toFile();
		File parentFile = file.getParentFile();
		if (parentFile != null)
			parentFile.mkdirs();
		FileWriter xml = new FileWriter(file);
		xml.write(NiemUmlClass.XML_HEADER);
		xml.write("<c:Catalog");
		for (Entry<String, String> entry : NamespaceModel.Prefixes.entrySet()) {
			String prefix = entry.getKey();
			if (prefix.equals(NC_PREFIX))
				writeXmlNs(xml, prefix, MPD_NC_URI);
			else if (prefix.equals(NiemUmlClass.STRUCTURES_PREFIX))
				writeXmlNs(xml, prefix, MPD_STRUCTURES_URI);
			else
				writeXmlNs(xml, prefix, NamespaceModel.Prefixes.get(prefix));
		}
		writeXmlNs(xml, "c", MPD_CATALOG_URI);
		writeXmlAttribute(xml, NiemModel.XSI_PREFIX + ":schemaLocation", MPD_CATALOG_URI + " " + MPD_CATALOG_LOCATION);
		xml.write(">");
		xml.write("<c:MPD c:mpdURI=\"" + XmlWriter.getExtensionSchema("") + "\"");
		writeXmlAttribute(xml, "c:mpdClassURIList", MPD_URI + "#MPD " + MPD_URI + "#IEPD");
		xml.write(" c:mpdName=\"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_NAME_PROPERTY) + "\" c:mpdVersionID=\""
				+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_VERSION_PROPERTY) + "\">");
		xml.write("<c:MPDInformation>" + "<c:AuthoritativeSource>" + "<nc:EntityOrganization>" + "<nc:OrganizationName>"
				+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_ORGANIZATION_PROPERTY) + "</nc:OrganizationName>"
				+ "<nc:OrganizationPrimaryContactInformation>" + "<nc:ContactEmailID>"
				+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_EMAIL_PROPERTY) + "</nc:ContactEmailID>" + "<nc:ContactWebsiteURI>"
				+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_CONTACT_PROPERTY) + "</nc:ContactWebsiteURI>"
				+ "</nc:OrganizationPrimaryContactInformation>" + "</nc:EntityOrganization>"
				+ "</c:AuthoritativeSource>" + "<c:CreationDate>" + today + "</c:CreationDate>" + "<c:StatusText>"
				+ NiemUmlClass.getProperty(NiemUmlClass.IEPD_STATUS_PROPERTY) + "</c:StatusText>" + "</c:MPDInformation>");
		for (String message : messages) {
			UmlClassInstance element = null;
			if (NiemUmlClass.isNiemElement(message))
				element = NiemUmlClass.SubsetModel.getElement(NamespaceModel.getSchemaURI(message), message);
			else
				element = NiemUmlClass.ExtensionModel.getElement(NamespaceModel.getSchemaURI(message), message);
			xml.write("<c:IEPConformanceTarget structures:id=\"" + NamespaceModel.getName(message) + "\">");
			if (element == null)
				Log.trace("exportMPDCatalog: error - no root element " + message);
			else
				xml.write("<nc:DescriptionText>" + element.description() + "</nc:DescriptionText>");
			xml.write("<c:HasDocumentElement c:qualifiedNameList=\"" + message + "\"/>" + "<c:XMLSchemaValid>"
					+ "<c:XMLCatalog c:pathURI=\"" + XML_CATALOG_FILE + "\"/>" + "</c:XMLSchemaValid>"
					+ "<c:IEPSampleXMLDocument c:pathURI=\"" + NamespaceModel.getName(message) + NiemUmlClass.XML_FILE_TYPE + "\"/>"
					+ "</c:IEPConformanceTarget>");
		}
		xml.write("<c:ReadMe c:pathURI=\"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_READ_ME_FILE_PROPERTY) + "\"/>"
				+ "<c:MPDChangeLog c:pathURI=\"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_CHANGE_LOG_FILE_PROPERTY) + "\"/>"
				+ "<c:Wantlist c:pathURI=\"" + Paths.get(NiemUmlClass.NIEM_DIR, WANTLIST_FILE).toString() + "\"/>"
				+ "<c:ConformanceAssertion c:pathURI=\"" + CONFORMANCE_ASSERTION_FILE + " \"/>");

		for (Entry<String, String> entry : NamespaceModel.Prefixes.entrySet()) {
			String prefix = entry.getKey();
			String schemaURI = NamespaceModel.Prefixes.get(prefix);
			if (schemaURI != null) {
				Namespace ns = NamespaceModel.Namespaces.get(schemaURI);
				if (ns != null) {
					if ((ns.referenceClassView == null) && (ns.filepath != null))
						xml.write("<c:ExtensionSchemaDocument c:pathURI=\"" + ns.filepath + "\"/>");
				}
			}
		}
		for (String codeList : CodeListNamespaces)
			xml.write("<c:BusinessRulesArtifact c:pathURI=\"" + codeList + GC_FILE_TYPE + "\"/>\n");
		xml.write("<c:ReadMe c:pathURI=\"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_READ_ME_FILE_PROPERTY) + "\"/>");
		xml.write("<c:MPDChangeLog c:pathURI=\"" + NiemUmlClass.getProperty(NiemUmlClass.IEPD_CHANGE_LOG_FILE_PROPERTY) + "\"/>");
		xml.write("</c:MPD></c:Catalog>");
		xml.close();
		Log.debug("exportMPDCatalog: done generating MPD catalog");
	}

	/** exports a WSDL definitions file */
	void exportWsdl(String wsdlDir, Map<String, UmlClass> ports, Set<String> messageNamespaces) throws IOException {

		String WSDLURI = NiemUmlClass.getProperty(NiemUmlClass.IEPD_URI_PROPERTY) + WSDL_SUFFIX;
		String WRAPPERURI = NiemUmlClass.getProperty(NiemUmlClass.IEPD_URI_PROPERTY) + MESSAGE_WRAPPERS_FILE_NAME;

		Log.trace("Generating document/literal wrapper schema");
		TreeSet<String> xmlTypes = new TreeSet<String>();
		TreeSet<String> xmlElements = new TreeSet<String>();
		for (UmlClass port : ports.values()) {
			for (UmlItem item2 : port.children()) {
				if (item2.kind() != anItemKind.anOperation)
					continue;
				UmlOperation operation = (UmlOperation) item2;
				String operationName = operation.name();
				Log.debug("exportWSDL: generating document/literal wrapper for " + operationName);
				UmlClass outputType = null, inputType = null;
				UmlParameter[] params = operation.params();
				if (params != null) {
					String elementName = operationName + "Request";
					String inputTypeName = elementName + "Type";
					String inputTypeSchema = "<xs:complexType name=\"" + inputTypeName + "\">" + "<xs:sequence>";
					for (UmlParameter param : params) {
						// ignore RESTful parameters
						if (!param.name.equals("") && !param.name.equals("body"))
							continue;
						try {
							inputType = param.type.type;
						} catch (Exception e) {
							Log.trace("exportWSDL: error - no input message for " + operationName);
						}
						if (inputType == null || !inputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE_TYPE))
							continue;
						String inputMessage = inputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_XPATH);
						if (inputMessage == null || inputMessage.equals(""))
							continue;
						Log.debug("exportWSDL: input Message: " + inputMessage + " from operation " + operationName);
						messageNamespaces.add(NamespaceModel.getPrefix(inputMessage));
						String mult = param.multiplicity;
						String minOccurs = "1";
						String maxOccurs = "1";
						if (!(mult.equals(""))) {
							if (mult.contains("..")) {
								String[] occurs = mult.split("\\.\\.");
								minOccurs = occurs[0];
								maxOccurs = occurs[1];
							} else
								minOccurs = maxOccurs = mult;
						}
						if (maxOccurs.equals("*"))
							maxOccurs = "unbounded";
						if (NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(inputMessage)))
							inputTypeSchema += "<!--xs:element ref=\"" + inputMessage + "\" minOccurs=\"" + minOccurs
							+ "\" maxOccurs=\"" + maxOccurs + "\"/-->\n";
						else
							inputTypeSchema += "<xs:element ref=\"" + inputMessage + "\" minOccurs=\"" + minOccurs
							+ "\" maxOccurs=\"" + maxOccurs + "\"/>\n";
					}
					inputTypeSchema += "</xs:sequence>" + "</xs:complexType>";
					xmlTypes.add(inputTypeSchema);
					xmlElements.add("<xs:element name=\"" + elementName + "\" type=\""
							+ NamespaceModel.getPrefixedName(WRAPPER_PREFIX, inputTypeName) + "\"/>");

				}

				try {
					outputType = operation.returnType().type;
				} catch (Exception e) {
					Log.trace("exportWSDL: error - no output message for " + operationName + " " + e.toString());
				}
				if (outputType == null || !outputType.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE_TYPE))
					continue;
				String outputMessage = outputType.propertyValue(NiemUmlClass.NIEM_STEREOTYPE_XPATH);
				if (outputMessage == null || outputMessage.equals(""))
					continue;
				Log.debug("exportWSDL: output Message: " + outputMessage + " from operation " + operationName);
				String elementName = operationName + "Response";
				String outputTypeName = elementName + "Type";
				String outputTypeSchema = "<xs:complexType name=\"" + outputTypeName + "\">" + "<xs:sequence>";
				if (NamespaceModel.isExternalPrefix(NamespaceModel.getPrefix(outputMessage)))
					outputTypeSchema += "<!--xs:element ref=\"" + outputMessage + "\"/-->";
				else
					outputTypeSchema += "<xs:element ref=\"" + outputMessage + "\"/>";
				outputTypeSchema += "</xs:sequence>" + "</xs:complexType>";
				xmlTypes.add(outputTypeSchema);
				xmlElements.add("<xs:element name=\"" + elementName + "\" type=\""
						+ NamespaceModel.getPrefixedName(WRAPPER_PREFIX, outputTypeName) + "\"/>");
			}
		}

		// export message wrapper
		NamespaceModel.Prefixes.put(WRAPPER_PREFIX, WRAPPERURI);
		messageNamespaces.add(WRAPPER_PREFIX);
		String filename = Paths.get(directory, MESSAGE_WRAPPERS_FILE_NAME + NiemUmlClass.XSD_FILE_TYPE).toString();
		exportXmlSchema(filename, WRAPPERURI, xmlTypes, xmlElements, messageNamespaces);

		Log.trace("Generating WSDLs");
		for (UmlClass port : ports.values()) {
			String portName = port.name();
			Path p1 = Paths.get(wsdlDir, portName + WSDL_FILE_TYPE);
			File file = p1.toFile();
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			FileWriter wsdl = new FileWriter(file);
			Log.debug("WSDL: " + portName + WSDL_FILE_TYPE);
			wsdl.write("<definitions targetNamespace=\"" + WSDLURI + "/" + portName + "\"");
			writeXmlNs(wsdl, WSDL_PREFIX, WSDLURI + "/" + portName);
			writeXmlNs(wsdl, WRAPPER_PREFIX, WRAPPERURI);
			writeXmlNs(wsdl, "xsd", NiemUmlClass.XSD_URI);
			writeXmlNs(wsdl, SOAP_PREFIX, SOAP_URI);
			writeXmlNs(wsdl, WSDL_SCHEMA_PREFIX, WSDL_SCHEMA_URI);
			writeXmlNs(wsdl, "", WSDL_SCHEMA_URI);
			writeXmlNs(wsdl, WSP_PREFIX, WSP_URI);
			writeXmlNs(wsdl, WSRMP_PREFIX, WSRMP_URI);
			writeXmlNs(wsdl, WSU_PREFIX, WSU_URI);
			wsdl.write("><!-- " + port.description() + " -->");
			Path p2 = Paths.get(directory, MESSAGE_WRAPPERS_FILE_NAME + NiemUmlClass.XSD_FILE_TYPE);
			Path p3 = p1.getParent().relativize(p2);
			wsdl.write("<wsp:UsingPolicy wsdl:required=\"true\"/>" + "<wsp:Policy wsu:Id=\"" + WSP_POLICY + "\">"
					+ "<wsrmp:RMAssertion/>" + "</wsp:Policy>" + "<wsdl:types>" + "<xsd:schema>"
					+ "<xsd:import namespace=\"" + WRAPPERURI + "\" schemaLocation=\"" + p3.toString() + "\"/>"
					+ "</xsd:schema>" + "</wsdl:types>");

			wsdl.write("<!-- messages -->");
			for (UmlItem item : port.children()) {
				if (item.kind() == anItemKind.anOperation) {
					UmlOperation operation = (UmlOperation) item;
					String operationName = operation.name();
					wsdl.write("<message name=\"" + operationName + REQUEST_MESSAGE_SUFFIX + "\">"
							+ "<part name=\"body\" element=\"" + NamespaceModel.getPrefixedName(WRAPPER_PREFIX, operationName)
							+ REQUEST_MESSAGE_SUFFIX + "\"/>" + "</message>" + "<message name=\"" + operationName
							+ RESPONSE_MESSAGE_SUFFIX + "\">" + "<part name=\"body\" element=\""
							+ NamespaceModel.getPrefixedName(WRAPPER_PREFIX, operationName) + RESPONSE_MESSAGE_SUFFIX + "\"/>"
							+ "</message>");
				}
			}

			wsdl.write("<!-- ports -->");
			wsdl.write("<portType name=\"" + portName + "\">");
			for (UmlItem item : port.children()) {
				if (item.kind() == anItemKind.anOperation) {
					UmlOperation operation = (UmlOperation) item;
					String operationName = operation.name();
					wsdl.write("<operation name=\"" + operationName + "\">" + "<input message=\""
							+ NamespaceModel.getPrefixedName(WSDL_PREFIX, operationName) + REQUEST_MESSAGE_SUFFIX + "\"/>"
							+ "<output message=\"" + NamespaceModel.getPrefixedName(WSDL_PREFIX, operationName)
							+ RESPONSE_MESSAGE_SUFFIX + "\"/>" + "</operation>");
				}
			}
			wsdl.write("</portType>");

			wsdl.write("<!-- bindings -->");
			wsdl.write("<binding name=\"" + portName + "Soap\" type=\"" + NamespaceModel.getPrefixedName(WSDL_PREFIX, portName) + "\">"
					+ "<wsp:PolicyReference URI=\"#" + WSP_POLICY + "\"/>"
					+ "<soap:binding style=\"document\" transport=\"" + SOAP_HTTP_BINDING_URI + "\"/>");
			for (UmlItem item : port.children()) {
				if (item.kind() == anItemKind.anOperation) {
					UmlOperation oper = (UmlOperation) item;
					String operationName = oper.name();
					wsdl.write("<operation name=\"" + operationName + "\">");
					wsdl.write("<!-- " + item.description() + " -->");
					wsdl.write("<soap:operation soapAction=\"" + WSDLURI + "/" + portName + "/" + operationName + "\"/>"
							+ "<input>" + "	<soap:body use=\"literal\"/>" + "</input>" + "<output>"
							+ "	<soap:body use=\"literal\"/>" + "</output>" + "</operation>");
				}
			}
			wsdl.write("</binding>");
			wsdl.write(
					"<!-- services not defined here...defined in an implementation-specific WSDL that imports this one -->"
							+ "</definitions>");
			wsdl.close();
		}
	}

	/** exports XML catalog file */
	void exportXmlCatalog() throws IOException {
		FileWriter xml;
		Log.trace("Generating XML catalog");
		File file = Paths.get(directory, XML_CATALOG_FILE).toFile();
		File parentFile = file.getParentFile();
		if (parentFile != null)
			parentFile.mkdirs();
		xml = new FileWriter(file);
		xml.write(NiemUmlClass.XML_HEADER + NiemUmlClass.XML_ATTRIBUTION + XML_CATALOG_HEADER + "<catalog prefer=\"public\" "
				+ NamespaceModel.NAMESPACE_ATTRIBUTE + "=\"" + XML_CATALOG_URI + "\">\n");
		for (Entry<String, String> entry : NamespaceModel.Prefixes.entrySet()) {
			String prefix = entry.getKey();
			String schemaURI = NamespaceModel.Prefixes.get(prefix);
			Namespace ns = NamespaceModel.Namespaces.get(schemaURI);
			if (NamespaceModel.isExternalPrefix(prefix))
				xml.write("<uri name=\"" + schemaURI + "\" uri=\"" + NamespaceModel.externalSchemaURL.get(schemaURI) + "\"/>\n");
			else if (ns.referenceClassView == null)
				xml.write("<uri name=\"" + schemaURI + "\" uri=\"" + ns.filepath + "\"/>\n");
		}
		for (String codeList : CodeListNamespaces)
			xml.write("<uri name=\"" + XmlWriter.getExtensionSchema(codeList) + "\" uri=\"" + codeList + GC_FILE_TYPE + "\"/>\n");
		xml.write(
				"<nextCatalog  catalog=\"" + Paths.get(NiemUmlClass.NIEM_DIR, XML_CATALOG_FILE).toString() + "\" />\n</catalog>\n");
		xml.close();
	}

	/** return XML schema element in type definition */
	String exportXmlElementInTypeSchema(UmlClassInstance element, String multiplicity,
			String mappingNotes) {
		String elementSchema = "<xs:element ref=\"" + NamespaceModel.getPrefixedName(element) + "\" minOccurs=\""
				+ NiemUmlClass.getMinOccurs(multiplicity) + "\" maxOccurs=\"" + NiemUmlClass.getMaxOccurs(multiplicity) + "\"";
		if (mappingNotes != null && !mappingNotes.equals(""))
			elementSchema += "<!--" + mappingNotes + "-->\n</xs:element>\n";
		else
			elementSchema += "/>\n";
		return elementSchema;
	}

	/** return XML schema element definition */
	String exportXmlElementSchema(UmlClassInstance element) {
		String elementName = NamespaceModel.getName(element);
		String elementSchema = "<xs:element name=\"" + elementName + "\"";
		UmlClass baseType = NiemModel.getBaseType(element);
		if (baseType != null) {
			if (baseType == NiemUmlClass.SubsetModel.abstractType)
				elementSchema += " abstract=\"true\"";
			else
				elementSchema += " type=\"" + NamespaceModel.getPrefixedName(baseType) + "\"";
		}
		String headElement = element.propertyValue(NiemUmlClass.SUBSTITUTION_PROPERTY);
		if (headElement != null)
			elementSchema += " substitutionGroup=\"" + headElement + "\"";
		String isNillable = element.propertyValue(NiemUmlClass.NILLABLE_PROPERTY);
		if (isNillable == null)
			isNillable = "false";
		if (isNillable.equals("true"))
			elementSchema += " nillable=\"true\"";
		elementSchema += ">\n";
		String mappingNotes = element.propertyValue(NiemUmlClass.NOTES_PROPERTY);
		if (mappingNotes != null && !mappingNotes.equals(""))
			elementSchema += "<!--" + mappingNotes + "-->";
		String description = element.description();
		if (description != null && !description.equals("")) {
			elementSchema += "\n<xs:annotation>\n" + "<xs:documentation>" + description + "</xs:documentation>\n";
			String codeList = element.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
			if (codeList != null)
				elementSchema += "<xs:appinfo>" + "<clsa:SimpleCodeListBinding codeListURI=\""
						+ XmlWriter.getExtensionSchema(elementName) + "\"/>" + " </xs:appinfo>";
			elementSchema += "</xs:annotation>\n";
		}
		elementSchema += "</xs:element>\n";
		return elementSchema;
	}

	void exportXmlSchema(String filename, String nsSchemaURI, TreeSet<String> xmlTypes, TreeSet<String> xmlElements,
			Set<String> schemaNamespaces) {
		try {
			Log.debug("exportXMLSchema: exporting " + filename);
			File file = new File(filename);
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			FileWriter xml = new FileWriter(filename);
			xml.write(NiemUmlClass.XML_HEADER + NiemUmlClass.XML_ATTRIBUTION + "<" + "xs:schema targetNamespace=\"" + nsSchemaURI + "\"\n");

			// export XML namespace definitions
			writeXmlNs(xml, "", nsSchemaURI);
			writeXmlNs(xml, NiemModel.XSI_PREFIX, NiemModel.XSI_URI);
			if (schemaNamespaces.isEmpty())
				Log.trace("exportXMLSchema: error - no imported namespaces in " + filename);
			for (String nsPrefix : schemaNamespaces)
				if (!nsPrefix.equals(NiemUmlClass.LOCAL_PREFIX))
					writeXmlNs(xml, nsPrefix, NamespaceModel.Prefixes.get(nsPrefix));
			writeXmlNs(xml, NiemUmlClass.CODELIST_APPINFO_PREFIX, NiemUmlClass.CODELIST_APPINFO_URI);
			writeXmlNs(xml, NiemUmlClass.CT_PREFIX, NiemUmlClass.CT_URI);
			writeXmlNs(xml, NiemUmlClass.TERM_PREFIX, NiemUmlClass.TERM_URI);
			writeXmlAttribute(xml, "ct:conformanceTargets",
					NDR_URI + "#ExtensionSchemaDocument " + CODELIST_URI + "#SchemaDocument");
			writeXmlAttribute(xml, "elementFormDefault", "qualified");
			writeXmlAttribute(xml, "attributeFormDefault", "unqualified");
			writeXmlAttribute(xml, "version", NiemUmlClass.getProperty(NiemUmlClass.IEPD_VERSION_PROPERTY));
			// close top level element
			xml.write(">\n<xs:annotation>\n" + "<xs:documentation> Schema for namespace " + nsSchemaURI
					+ "</xs:documentation>\n" + "</xs:annotation>");
			// add import namespaces
			Log.debug("exportXMLSchema: exporting namespaces");
			Path path1 = Paths.get(file.getParent());
			for (String nsPrefix : schemaNamespaces) {
				if ((nsPrefix.equals(NiemUmlClass.LOCAL_PREFIX)) || (nsPrefix.equals(NiemUmlClass.XSD_PREFIX)))
					continue;
				// trace("exportSchema: exporting prefix " + nsPrefix);
				String nsSchemaURI2 = NamespaceModel.Prefixes.get(nsPrefix);
				if (nsSchemaURI2 == null) {
					Log.trace("exportXMLSchema: error - prefix " + nsPrefix + " not in model");
					continue;
				}
				if (NamespaceModel.isExternalPrefix(nsPrefix))
					xml.write("<xs:import namespace=\"" + nsSchemaURI2 + "\" schemaLocation=\""
							+ NamespaceModel.externalSchemaURL.get(nsSchemaURI2) + "\"/>");
				else {
					Namespace ns2 = NamespaceModel.Namespaces.get(nsSchemaURI2);
					if (ns2 == null) {
						Log.trace("exportXMLSchema: error - namespace " + nsSchemaURI2 + " not in model");
						continue;
					}
					Path p2 = Paths.get(directory, ns2.filepath);
					Path p3 = path1.relativize(p2);
					if (!nsSchemaURI2.equals(nsSchemaURI) && !nsSchemaURI2.equals(NiemUmlClass.LOCAL_URI)
							&& !nsSchemaURI2.equals(NiemUmlClass.XSD_URI))
						xml.write("<xs:import namespace=\"" + nsSchemaURI2 + "\" schemaLocation=\"" + p3.toString()
						+ "\"/>");
				}
			}
			// export attributes, types and element
			xml.write(String.join("", xmlTypes));
			xml.write(String.join("", xmlElements));

			// close schema
			xml.write("</xs:schema>\n");
			xml.close();
		} catch (Exception e1) {
			Log.trace("exportXMLSchema: error exporting XML schema " + e1.toString());
		}
	}

	/** return XML schema type definition */
	String exportXmlTypeSchema(UmlClass type) {
		String typeName = NamespaceModel.getName(type);
		String typeSchema = null;
		String codeList = type.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		boolean isComplexType = true;
		boolean isComplexContent = true;
		UmlClass baseType = NiemModel.getBaseType(type);
		if (baseType == null)
			baseType = NiemUmlClass.SubsetModel.objectType;
		String baseTypeCodeList = baseType.propertyValue(NiemUmlClass.CODELIST_PROPERTY);
		String baseTypeName = NamespaceModel.getPrefixedName(baseType);
		if (codeList != null && !codeList.equals("")) { // code list simple type
			Log.debug("exportXmlTypeSchema: exporting code list simple type " + typeName);
			isComplexType = false;
			isComplexContent = false;
		} else if (baseTypeCodeList != null && !baseTypeCodeList.equals("")) { // code list complex type
			Log.debug("exportXmlTypeSchema: exporting code list complex type " + typeName);
			isComplexContent = false;
		} else
			Log.debug("exportXmlTypeSchema: exporting complex type " + typeName); // complexContent

		TreeSet<String> xmlEnumerations = new TreeSet<String>();
		if (isComplexType == false && baseType != null)
			if (codeList != null && codeList.equals("")) {
				for (String code : codeList.split(NiemUmlClass.CODELIST_DELIMITER)) {
					if (code.equals(""))
						continue;
					String[] codeParams = code.replace("&", "&amp;").split(NiemUmlClass.CODELIST_DEFINITION_DELIMITER);
					String codeValue = codeParams[0].trim();
					if (codeValue.equals(""))
						continue;
					String codeDescription = (codeParams.length > 1 && !codeParams[1].equals("")) ? codeParams[1].trim()
							: "";
					String enumeration = "<xs:enumeration value=\"" + codeValue + "\">\n";
					if (!codeDescription.equals(""))
						enumeration += "<xs:annotation>\n" + "<xs:documentation>" + codeDescription
						+ "</xs:documentation>\n" + "</xs:annotation>\n";
					enumeration += "</xs:enumeration>\n";
					xmlEnumerations.add(enumeration);
				}
			}
		// type.sortChildren();
		// add elements, attributes and attribute groups in types
		TreeSet<String> xmlElementsInType = new TreeSet<String>();
		TreeSet<String> xmlAttributesInType = new TreeSet<String>();
		String anyElement = NamespaceModel.getPrefixedName(NiemUmlClass.XSD_PREFIX, NiemUmlClass.ANY_ELEMENT_NAME);
		UmlAttribute augmentationPoint = null;
		String xmlElementInType = null;
		for (UmlItem item : type.children()) {
			if (item.kind() == anItemKind.anAttribute) {
				UmlAttribute attribute = (UmlAttribute) item;
				NiemModel model = (NiemUmlClass.SubsetModel.elements.containsKey(NiemModel.getURI(attribute))) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;
				UmlClassInstance element = model.getElementByURI(NiemModel.getURI(attribute));
				if (element == null)
					continue;
				String elementName = NamespaceModel.getPrefixedName(element);
				Log.debug("exportXmlTypeSchema: exporting element in type " + elementName);
				String elementMappingNotes = null;
				if (elementName.endsWith(AUGMENTATION_POINT_NAME))
					augmentationPoint = attribute;
				if (isComplexContent)
					elementMappingNotes = attribute.propertyValue(NiemUmlClass.NOTES_PROPERTY);
				if (elementName.equals(anyElement)) {
					xmlElementsInType.add("<xs:any/>");
					continue;
				}
				// if (complexContent)
				String multiplicity = attribute.multiplicity();
				if (NiemUmlClass.isAttribute(element)) {
					String use = multiplicity.equals("1,1") ? "required" : "optional";
					xmlAttributesInType.add("<xs:attribute ref=\"" + elementName + "\" use = \"" + use + "\"/>");
				} else {
					xmlElementInType = exportXmlElementInTypeSchema(element, multiplicity, elementMappingNotes);
					if (xmlElementInType != null)
						xmlElementsInType.add(xmlElementInType);
				}
			}
			if (item.kind() == anItemKind.aRelation) {
				UmlRelation relation = (UmlRelation) item;
				if (relation.relationKind() == aRelationKind.aDirectionalAggregation) { // attributeGroup
					UmlClass sourceBaseType = relation.roleType();
					if (sourceBaseType != null) {
						xmlAttributesInType.add("<xs:attributeGroup ref=\"" + NamespaceModel.getPrefixedName(sourceBaseType) + "\"/>");
					}
				}
			}
		}
		if (augmentationPoint != null) {
			// if (complexContent) {
			NiemModel model = (NiemUmlClass.SubsetModel.elements.containsKey(NiemModel.getURI(augmentationPoint))) ? NiemUmlClass.SubsetModel : NiemUmlClass.ExtensionModel;
			UmlClassInstance element = model.getElementByURI(NiemModel.getURI(augmentationPoint));
			xmlElementsInType.add(exportXmlElementInTypeSchema(element, augmentationPoint.multiplicity(), null));
		}

		// write XML schema definition
		typeSchema = (isComplexType) ? "<xs:complexType" : "<xs:simpleType";
		typeSchema += " name=\"" + typeName + "\">\n";
		String mappingNotes = type.propertyValue(NiemUmlClass.NOTES_PROPERTY);
		if (mappingNotes != null && !mappingNotes.equals(""))
			typeSchema += "<!--" + mappingNotes + "-->";
		String description = type.description();
		if (description != null && !description.equals(""))
			typeSchema += "<xs:annotation>\n" + "<xs:documentation>" + description + "</xs:documentation>\n"
					+ "</xs:annotation>\n";
		if (isComplexType) {
			if (isComplexContent) {
				typeSchema += "<xs:complexContent>\n" + "<xs:extension base=\"" + baseTypeName + "\">\n"
						+ "<xs:sequence>\n" + String.join("", xmlElementsInType) + "\n" + "</xs:sequence>\n"
						+ "</xs:extension>\n" + String.join("", xmlAttributesInType) + "\n" + "</xs:complexContent>\n"
						+ "</xs:complexType>\n";
			} else {
				typeSchema += "<xs:simpleContent>\n" + "<xs:extension base=\"" + baseTypeName + "\"/>\n"
						+ String.join("", xmlAttributesInType) + "\n" + "</xs:simpleContent>\n" + "</xs:complexType>\n";
			}
		} else {
			typeSchema += "<xs:restriction base=\"" + baseTypeName + "\"";
			if (xmlEnumerations.size() > 0)
				typeSchema += String.join("", xmlEnumerations) + "\n";
			typeSchema += "</xs:restriction>\n" + "</xs:simpleType>\n";
		}
		return typeSchema;
	}

	/** writes an XML name value pair to a file */
	private void writeXmlAttribute(FileWriter fw, String name, String value) {
		try {
			fw.write(" " + name + "=\"" + value + "\"");
		} catch (Exception e) {
			Log.trace("xmlAttribute: error " + e.toString());
		}
	}

	/** writes an XML namespace attribute to a file */
	void writeXmlNs(FileWriter fw, String prefix, String value) {
		try {
			if (prefix.equals(""))
				fw.write(" " + NamespaceModel.NAMESPACE_ATTRIBUTE + "=\"" + value + "\"");
			else
				fw.write(" " + NamespaceModel.NAMESPACE_ATTRIBUTE + ":" + prefix + "=\"" + value + "\"");
		} catch (Exception e) {
			Log.trace("xmlNS: error " + e.toString());
		}
	}

	/** returns an extension schema URI */
	static String getExtensionSchema(String prefix) {
		return NiemUmlClass.getProperty(NiemUmlClass.IEPD_URI_PROPERTY) + prefix;
	}

}
