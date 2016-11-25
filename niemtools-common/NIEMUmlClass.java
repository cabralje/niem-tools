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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
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

class NiemTools
{
	private static UmlClass abstractType = null;
	private static String abstractTypeName = "abstract";
	protected static String directory;
	private static Document doc;
	private static DocumentBuilder docBuilder;
	private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

	private static FileWriter fw;
	private static String hashDelimiter = ",";
	public static int importPass;

	private static String localPrefix = "local";

	private static String localSchemaURI = "http://" + localPrefix;

	// NIEM mapping spreadsheet column headings, NIEM profile profile steroetype
	public static final String[][] map = {
			{"Model Class","",},
			{"Model Attribute","",},
			{"Model Multiplicity","",},
			{"Model Definition","",},
			{"NIEM XPath","XPath"},
			{"NIEM Type","Type"},
			{"NIEM Property (Representation)","Property"},
			{"NIEM Base Type","BaseType"},
			{"NIEM Multiplicity","Multiplicity"},
			{"Old XPath","OldXPath"},
			{"Old Multiplicity","OldMultiplicity"},
			{"NIEM Mapping Notes","Notes"}};
	private static String namespaceDelimiter = ":";
	private static HashMap<String,UmlItem> NiemElements = new HashMap<String,UmlItem>();
	private static HashMap<String,List<UmlClassInstance>> NiemElementsInType = new HashMap<String,List<UmlClassInstance>>();
	private static HashMap<String,List<UmlExtraClassMember>> NiemEnumerations = new HashMap<String,List<UmlExtraClassMember>>();
	private static HashMap<String,Namespace> NiemNamespaces = new HashMap<String,Namespace>();
	//private static HashMap<String,String> NiemPrefixes = new HashMap<String,String>();
	public static final String niemStereotype = "niem-profile:niem";
	private static HashMap<String,UmlClass> NiemTypes = new HashMap<String,UmlClass>();
	private static String stereotypeDelimiter = ":";
	private static UmlPackage subsetPackage = null, extensionPackage = null, referencePackage = null;
	private static String xmlPrefix = "xs";
	private static XPath xPath = XPathFactory.newInstance().newXPath();

	// add element to PIM
	public static UmlClassInstance addElement(UmlClassView parentClassView, String schemaURI, String propertyName, String baseName)
	{
		String propertyName2 = propertyName.replace("-", "");
		UmlClassInstance ci = findElement((UmlPackage)(parentClassView.parent()), schemaURI, propertyName2);
		if (ci == null)
		{
			UmlClass base;
			if (baseName == null) // abstract
				base = abstractType;
			else
			{
				base = findTypeByPrefix((UmlPackage)(parentClassView.parent()),baseName);
				if (base == null)
					return null;    	
			}
			ci = UmlClassInstance.create(parentClassView, propertyName2, base);
			NiemElements.put(schemaURI + hashDelimiter + propertyName, ci);
		}
		return ci;
	}

	// add element in type to PIM
	public static UmlAttribute addElementInType(UmlClassView parentClassView, String schemaURI, String typeName, String propertyName)
	{
		String propertyName2 = propertyName.replace("-", "");
		UmlClass type = findType((UmlPackage)(parentClassView.parent()), schemaURI, typeName);
		if (type == null)
			return null;
		UmlAttribute at = UmlAttribute.create(type, propertyName2);
		return at;
	}

	// add enumeration to type in PIM
	public static UmlExtraClassMember addEnumeration(UmlClassView parentClassView, String schemaURI, String typeName, String propertyName)
	{
		String propertyName2 = propertyName.replace("-", "");
		UmlClass type = findType((UmlPackage)(parentClassView.parent()), schemaURI, typeName);
		if (type == null)
			return null;
		UmlExtraClassMember cm = UmlExtraClassMember.create(type, propertyName2);
		return cm;
	}

	// import NIEM and non-NIEM namespaces
	public static UmlClassView addNamespace(UmlPackage parentPackage, String prefix, String schemaURI)
	{
		Namespace ns = NiemNamespaces.get(schemaURI);
		if (ns != null) // namespace exists
		{
			if (parentPackage == referencePackage)
				return ns.referenceClassView;
			else
				return ns.nsClassView;
		}

		// create namespace
		ns = new Namespace(schemaURI);
		NiemNamespaces.put(schemaURI, ns);
		UmlClassView namespaceClassView = null;
		try {
			namespaceClassView = UmlClassView.create(parentPackage, prefix);
		} catch (Exception e) {
			UmlCom.trace("Multiple namespaces URIs for prefix " + prefix);
			namespaceClassView = UmlClassView.create(parentPackage, prefix + ThreadLocalRandom.current().nextInt()); 
		}
		if (parentPackage == referencePackage)
			ns.referenceClassView = namespaceClassView;
		else
			ns.nsClassView = namespaceClassView;
		return namespaceClassView;
	}

	// add type to PIM
	public static UmlClass addType(UmlClassView parentClassView, String schemaURI, String tagName)
	{
		String tagName2 = tagName.replace("-","");
		UmlClass typeClass = findType((UmlPackage)(parentClassView.parent()), schemaURI, tagName2);
		if (typeClass == null)
		{
			try {
				typeClass = UmlClass.create(parentClassView, tagName2);
			}
			catch (Exception e) { UmlCom.trace("addType: could not add type " + tagName + e.toString()); }
			NiemTypes.put(schemaURI + hashDelimiter + tagName, typeClass);
		}
		return typeClass;
	}

	protected static String columnHtml(String value, String bgcolor, String fgcolor)
	{
		return "<td bgcolor=\"" + bgcolor +"\"><font color = \"" + fgcolor +"\">" + value + "</font></td>";
	}

	// create PIM
	public static void createPIM(UmlPackage root)
	{	
		UmlPackage pimPackage = null;

		// Find or create PIM package
		for (UmlItem ch : root.children())
		{
			if (ch.pretty_name().equals("PIM"))
				if ((ch.kind().value() == anItemKind._aPackage))
				{
					pimPackage = (UmlPackage)ch;
					break;
				}
		}
		if (pimPackage == null)
		{
			pimPackage = UmlPackage.create(root,"PIM");
			// UmlCom.trace("PIM root-level package created");
		}

		// Find or create package "NIEMSubset"
		for (UmlItem ch : pimPackage.children())
		{
			if (ch.pretty_name().equals("NIEMSubset"))
				if ((ch.kind().value() == anItemKind._aPackage))
				{
					subsetPackage = (UmlPackage)ch;
					break;
				}
		}
		if (subsetPackage == null)
		{
			subsetPackage = UmlPackage.create(pimPackage,"NIEMSubset");
			// UmlCom.trace("NIEMSubset package created");
		}

		// Find or create package "NIEMExtension"
		for (UmlItem ch : pimPackage.children())
		{
			if (ch.pretty_name().equals("NIEMExtension"))
				if ((ch.kind().value() == anItemKind._aPackage))
				{
					extensionPackage = (UmlPackage)ch;
					break;
				}
		}
		if (extensionPackage == null)
		{
			extensionPackage = UmlPackage.create(pimPackage,"NIEMExtension");
			// UmlCom.trace("NIEMExtension package created");
		}

		// Find or create package "NIEMReference"
		for (UmlItem ch : pimPackage.children())
		{
			if (ch.pretty_name().equals("NIEMReference"))
				if ((ch.kind().value() == anItemKind._aPackage))
				{
					referencePackage = (UmlPackage)ch;
					break;
				}
		}
		if (referencePackage == null)
		{
			referencePackage = UmlPackage.create(pimPackage,"NIEMReference");
			// UmlCom.trace("NIEMReference package created");
		}

		// import XML namespace and simple types
		UmlClassView cv = addNamespace(referencePackage, xmlPrefix, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		String[] xmlTypeNames = {"anyURI", "base64Binary", "blockSet", "boolean", "byte", "date", "dateTime", "decimal", 
				"derivationControl", "derivationSet", "double", "duration", "ENTITIES", "ENTITY", "float", "formChoice", "fullDerivationSet",
				"gDay", "gMonth", "gMonthDay", "gYear", "gYearMonth", "hexBinary", "int", "integer", "language", "long",  "Name", "namespaceList",
				"NCName", "negativeInteger", "NMTOKEN", "NMTOKENS", "nonNegativeInteger", "nonPositiveInteger", "normalizedString", "NOTATION",
				"positiveInteger", "public", "QName", "short", "simpleDerivationSet", "string", "time", "token", "unsignedByte", "unsignedInt",
				"unsignedLong", "unsignedShort"};
		for (String s : xmlTypeNames)
		{
			UmlClass c = UmlClass.create(cv, s);
			NiemTypes.put(XMLConstants.W3C_XML_SCHEMA_NS_URI + hashDelimiter + s, c);
		}

		// import local namespace and abstract type
		cv = addNamespace(referencePackage, localPrefix, localSchemaURI);
		abstractType = UmlClass.create(cv,abstractTypeName);
		NiemTypes.put(localSchemaURI + hashDelimiter + abstractTypeName, abstractType);
	}

	// reset NIEM mappings
	public static void deleteMapping()
	{
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlClass.all.iterator();
		while (it.hasNext())
		{
			UmlItem item = it.next();
			if (item.stereotype().equals(niemStereotype))
				for (int p=4;p<map.length;p++)
					item.set_PropertyValue(niemProperty(p),"");
		}
	}

	// generate NIEM mapping spreadsheet in CSV format
	// roundtripping is supported with importCsv()
	public static void exportCsv()
	{
		try
		{
			fw = new FileWriter(directory + "/niem-mapping.csv");
			CSVWriter writer = new CSVWriter(fw);

			// Write header
			String[] nextLine = new String[map.length];
			for (int i=0;i<map.length;i++)
				nextLine[i]=map[i][0];
			writer.writeNext(nextLine);

			// Export NIEM Mappings for Classes
			for (int i=0; i<UmlClass.classes.size(); i++)
			{
				UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);

				if (c.stereotype().equals(niemStereotype))
				{
					writer.writeNext(itemCsv(c));

					// Export NIEM Mapping for Attributes and Relations
					for (UmlItem ch : c.children())
						if (ch.stereotype().equals(niemStereotype))
						{
							nextLine = itemCsv(ch);
							if (nextLine != null)
								writer.writeNext(nextLine);
						}
				}
			}
			writer.close();

		} 
		catch (FileNotFoundException e) { UmlCom.trace("exportCsv: File not found: " + e.toString()); }
		catch (IOException e) { UmlCom.trace("exportCsv: IO exception: " + e.toString()); }
	}

	// generate NIEM mapping spreadsheet in HTML format
	public static void exportHtml()
	{
		try {
			UmlItem.start_file("niem_mapping", "NIEM Mapping", true);

			// Write header
			fw.write("<table><tr bgcolor=\"#f0f0f0\">");
			for (int i=0;i<map.length;i++)
				fw.write("<td>" + map[i][0] + "</td>");
			fw.write("</tr>\n");

			// Show NIEM Mappings for Classes
			for (int i=0; i<UmlClass.classes.size(); i++) {
				UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);
				if (c.stereotype().equals(niemStereotype))
				{
					writeLineHtml(c);

					// Show NIEM Mapping for Attributes and Relations
					for (UmlItem ch : c.children())
					{
						if (ch.stereotype().equals(niemStereotype))
							writeLineHtml(ch);
					}
				}
			}
			fw.write("</table>\n");

			UmlItem.end_file();

		}
		catch (IOException e) { UmlCom.trace("exportHtml: IO exception: " + e.toString()); }
	}

	// generate extension and exhange schema
	public static void exportSchema()
	{
		try {
			Vector<FileWriter> sw = new Vector<FileWriter>();
			// int swsize = 0;
			HashMap<String,Integer> nsfile = new HashMap<String,Integer>();

			for (Entry<String,Namespace> entry : NiemNamespaces.entrySet())
			{
				String prefix = entry.getKey();
				String schema = entry.getValue().schemaURI;
				if (entry.getValue().nsClassView == null) {
					// UmlCom.trace("Namespace " + prefix + " is not in the model");
					continue;
				}
				if (entry.getValue().nsClassView.parent().equals(extensionPackage))
				{
					// Open file for each extension schema
					//fw = new FileWriter(directory + prefix + ".xsd");
					sw.addElement(new FileWriter(directory + "/" + prefix + ".xsd"));
					nsfile.put(prefix,sw.size()-1);
					UmlCom.trace(schema + " -> " + prefix + ".xsd");
					sw.lastElement().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					sw.lastElement().write("<!-- NIEM extension schema generated by BOUML niemtools plug_out -->\n");
					sw.lastElement().write("<xs:schema targetNamespace=\"" + schema + "\"");
					for (Entry<String, Namespace> entry2 : NiemNamespaces.entrySet())
						sw.lastElement().write(" xmlns:" + entry2.getKey() + "=\"" + entry2.getValue().schemaURI + "\"");
					sw.lastElement().write(">\n");
				}
			}

			// String tn = niemStereotype + stereotypeDelimiter + map[5][1];
			String pn = niemStereotype + stereotypeDelimiter + map[6][1];
			String bn = niemStereotype + stereotypeDelimiter + map[7][1];
			// String mn = niemStereotype + stereotypeDelimiter + map[8][1];
			String p, b, d;
			// Boolean invalid;

			// Iterate over all items with NIEM stereotype to export elements
			for (int i=0; i < UmlClass.all.size() ; i++) {
				UmlItem c = (UmlItem) UmlClass.all.elementAt(i);
				if (c.stereotype().equals(niemStereotype))
				{
					p = c.propertyValue(pn);
					b = c.propertyValue(bn);
					d = c.description();
					if (!p.equals("") && (!b.equals("")))
					{
						String[] pp = p.split(",");
						for (String ppp : pp)
						{
							ppp = ppp.trim();
							Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
							if (mat.find())
								ppp = mat.group(1);
							String prefix2 = getPrefix(ppp);
							if (!isNiemSchema(prefix2))
							{
								// String sg = "";
								//String ssg = "substitutionGroup=\"" + sg;
								String ssg = "";
								if (nsfile.containsKey(prefix2))
								{
									int nsindex = nsfile.get(prefix2);
									UmlCom.trace(ppp + " -> " + prefix2 + "(file " + nsindex + ")");
									sw.elementAt(nsindex).write("<xs:element name=\"" + getName(ppp) +"\" type=\"" + b + "\" " + ssg + " nillable=\"true\"><xs:annotation><xs:documentation>" + d + "</xs:documentation></xs:annotation></xs:element>\n");
								}
							}
						}
					}
				}
			}
			/*    // Iterate over all items with NIEM stereotype to export types
    for (int i=0; i < all.size() ; i++)
    {
      invalid = false;
      UmlItem c = (UmlItem) all.elementAt(i);
      if (c.stereotype().equals(niemStereotype))
      {
        t = c.propertyValue(tn);
        p = c.propertyValue(pn);
        b = c.propertyValue(bn);
        m = c.propertyValue(mn);
        if (p.equals(""))
        {
          String[] tt = t.split(",");
          for (String ttt : tt)
          {
            ttt = ttt.trim();
            // Type Mapping
            if (!isNiemSchema(getPrefix(ttt)))
            {

              // Export NIEM Types
              if (NiemTypes.containsKey(ttt))
                fw.write("<w:Type w:name=\"" + ttt + "\" w:isRequested=\"true\"/>\n");
              else
                fw.write("<!--w:Type w:name=\"" + ttt + "\" w:isRequested=\"true\"/-->\n");
            } else {

              String[] bb = b.split(",");
              for (String bbb : bb)
              {
                bbb = bbb.trim();
                if (isNiemSchema(getPrefix(bbb)))
                {
                  if (NiemTypes.containsKey(bbb))
                    // Export NIEM Base Types for Non-NIEM Types
                    fw.write("<w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/>\n");
                    else
                    fw.write("<!--w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/-->\n");
                }
              }
            }
          }
        } else {

          // NIEM Element in Type Mapping
          if (isNiemSchema(getPrefix(t)))
          {

            // Export NIEM Types
            if (NiemTypes.containsKey(t))
              fw.write("<w:Type w:name=\"" + t + "\" w:isRequested=\"true\">\n");
            else {
              fw.write("<!--w:Type w:name=\"" + t + "\" w:isRequested=\"true\"-->\n");
              invalid = true;
            }
            if (isNiemSchema(getPrefix(p)))
            {

              // Export Element in Type
              if ((m.equals("")))
                minoccurs = maxoccurs = "1";
              else if (m.contains(",")) {
                String[] occurs = m.split(",");
                minoccurs = occurs[0];
                maxoccurs = occurs[1];
              } else
                minoccurs = maxoccurs = m;

              if (isNiemSchema(getPrefix(t)))
              {
                String[] pp = p.split(",");
                for (String ppp : pp)
                {
                  ppp = ppp.trim();
                  Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
                  if (mat.find())
                    continue;
                  List<String> list = (List)(NiemElementsInType.get(t));
                  if ((!invalid) && ((list != null) && (list.contains(ppp))))
                    fw.write("\t<w:ElementInType w:name=\"" + ppp + "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs + "\" w:maxOccurs=\"" + maxoccurs + "\"/>\n");
                  else
                    fw.write("\t<!--w:ElementInType w:name=\"" + ppp + "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs + "\" w:maxOccurs=\"" + maxoccurs + "\"/-->\n");
                }
              }
              if (!invalid)
                fw.write("</w:Type>");
              else
                fw.write("<!--/w:Type-->");

           } else {
              if (!invalid)
                fw.write("</w:Type>");
              else
                fw.write("<!--/w:Type-->");
              if (isNiemSchema(getPrefix(b)))
              {
                // Export NIEM Base Types for Non-NIEM Properties
                String[] bb = b.split(",");
                for (String bbb : bb)
                {
                  bbb = bbb.trim();
                  if (isNiemSchema(getPrefix(bbb)))
                  {
                    if (NiemTypes.containsKey(bbb))
                      // Export NIEM Base Types for Non-NIEM Types
                      fw.write("<w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/>\n");
                      else
                      fw.write("<!--w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/-->\n");
                  }
                }
              }
            }
          }
        }
      }
    }
			 */
			for (int i=0; i < sw.size(); i++)
			{
				sw.elementAt(i).write("</xs:schema>");
				sw.elementAt(i).close();
			}
		}
		catch (IOException e) { UmlCom.trace("exportSchema: IO exception: " + e.toString()); }
	}

	// generate NIEM wantlist for import into Subset Schema Generator Tool (SSGT)
	public static void exportWantlist()
	{
		try {
			// Export schema
			fw = new FileWriter(directory + "/wantlist.xml");
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write("<!-- NIEM Wantlist generated by BOUML niemtools plug_out -->\n");
			fw.write("<w:WantList w:release=\"3.2\" w:product=\"NIEM\" w:nillableDefault=\"true\" ");
			for (Entry<String, Namespace> entry : NiemNamespaces.entrySet())
			{
				UmlClassView cv = entry.getValue().nsClassView;
				if (cv == null)
				{
					// UmlCom.trace("Namespace " + entry.getKey() + " is not in the model");
					continue;
				}
				if (cv.parent().equals(subsetPackage))
					fw.write(" xmlns:" + entry.getKey() + "=\"" + entry.getValue() + "\"");
			}
			fw.write(" xmlns:w=\"http://niem.gov/niem/wantlist/2.2\">\n");

			String tn = niemStereotype + stereotypeDelimiter + map[5][1];
			String pn = niemStereotype + stereotypeDelimiter + map[6][1];
			String bn = niemStereotype + stereotypeDelimiter + map[7][1];
			String mn = niemStereotype + stereotypeDelimiter + map[8][1];
			String t, p, m, b, minoccurs, maxoccurs;
			Boolean invalid;

			// Iterate over all items with NIEM stereotype to export elements
			for (int i=0; i < UmlClass.all.size() ; i++) {
				UmlItem c = (UmlItem) UmlClass.all.elementAt(i);
				if (c.stereotype().equals(niemStereotype))
				{
					p = c.propertyValue(pn);
					if (!p.equals("")) {
						String[] pp = p.split(",");
						for (String ppp : pp)
						{
							ppp = ppp.trim();
							Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
							if (mat.find())
								ppp = mat.group(1);
							if (isNiemSchema(getPrefix(ppp)))
								if (NiemElements.containsKey(ppp))
									fw.write("<w:Element w:name=\"" + ppp + "\" w:isReference=\"false\" w:nillable=\"false\"/>\n");
								else
									fw.write("<!--w:Element w:name=\"" + ppp + "\" w:isReference=\"false\" w:nillable=\"false\"/-->\n");
						}
					}
				}
			}

			// Iterate over all items with NIEM stereotype to export types
			for (int i=0; i < UmlClass.all.size() ; i++)
			{
				invalid = false;
				UmlItem c = (UmlItem) UmlClass.all.elementAt(i);
				if (c.stereotype().equals(niemStereotype))
				{
					t = c.propertyValue(tn);
					p = c.propertyValue(pn);
					b = c.propertyValue(bn);
					m = c.propertyValue(mn);
					if (p.equals(""))
					{
						String[] tt = t.split(",");
						for (String ttt : tt)
						{
							ttt = ttt.trim();
							// NIEM Type Mapping
							if (isNiemSchema(getPrefix(ttt)))
							{
								// Export NIEM Types
								if (NiemTypes.containsKey(ttt))
									fw.write("<w:Type w:name=\"" + ttt + "\" w:isRequested=\"true\"/>\n");
								else
									fw.write("<!--w:Type w:name=\"" + ttt + "\" w:isRequested=\"true\"/-->\n");
							} else {
								String[] bb = b.split(",");
								for (String bbb : bb)
								{
									bbb = bbb.trim();
									if (isNiemSchema(getPrefix(bbb)))
									{
										if (NiemTypes.containsKey(bbb))
											// Export NIEM Base Types for Non-NIEM Types
											fw.write("<w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/>\n");
										else
											fw.write("<!--w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/-->\n");
									}
								}
							}
						}
					} else {

						// NIEM Element in Type Mapping
						if (isNiemSchema(getPrefix(t)))
						{

							// Export NIEM Types
							if (NiemTypes.containsKey(t))
								fw.write("<w:Type w:name=\"" + t + "\" w:isRequested=\"true\">\n");
							else {
								fw.write("<!--w:Type w:name=\"" + t + "\" w:isRequested=\"true\"-->\n");
								invalid = true;
							}
							if (isNiemSchema(getPrefix(p)))
							{
								// Export Element in Type
								if ((m.equals("")))
									minoccurs = maxoccurs = "1";
								else if (m.contains(",")) {
									String[] occurs = m.split(",");
									minoccurs = occurs[0];
									maxoccurs = occurs[1];
								} else
									minoccurs = maxoccurs = m;

								if (isNiemSchema(getPrefix(t)))
								{
									String[] pp = p.split(",");
									for (String ppp : pp)
									{
										ppp = ppp.trim();
										Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
										if (mat.find())
											continue;
										List<UmlClassInstance> list = (List<UmlClassInstance>)(NiemElementsInType.get(t));
										if ((!invalid) && ((list != null) && (list.contains(ppp))))
											fw.write("\t<w:ElementInType w:name=\"" + ppp + "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs + "\" w:maxOccurs=\"" + maxoccurs + "\"/>\n");
										else
											fw.write("\t<!--w:ElementInType w:name=\"" + ppp + "\" w:isReference=\"false\" w:minOccurs=\"" + minoccurs + "\" w:maxOccurs=\"" + maxoccurs + "\"/-->\n");
									}
								}
								if (!invalid)
									fw.write("</w:Type>");
								else
									fw.write("<!--/w:Type-->");

							} else {
								if (!invalid)
									fw.write("</w:Type>");
								else
									fw.write("<!--/w:Type-->");
								if (isNiemSchema(getPrefix(b)))
								{
									// Export NIEM Base Types for Non-NIEM Properties
									String[] bb = b.split(",");
									for (String bbb : bb)
									{
										bbb = bbb.trim();
										if (isNiemSchema(getPrefix(bbb)))
										{
											if (NiemTypes.containsKey(bbb))
												// Export NIEM Base Types for Non-NIEM Types
												fw.write("<w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/>\n");
											else
												fw.write("<!--w:Type w:name=\"" + bbb + "\" w:isRequested=\"true\"/-->\n");
										}
									}
								}
							}
						}
					}
				}
			}

			fw.write("</w:WantList>");
			fw.close();

		}
		catch (IOException e) { UmlCom.trace("exportWantlist: IO exception: " + e.toString()); }
	}

	// get element by schemaURI and tagname
	public static UmlClassInstance findElement(UmlPackage parentPackage, String schemaURI, String tagName)
	{	
		if (parentPackage == referencePackage)
			return (UmlClassInstance)NiemElements.get(schemaURI + hashDelimiter + tagName);
		return null;
	}

	// get element by prefix and tagname
	public static UmlClassInstance findElementByPrefix(UmlPackage parentPackage, String tagName)
	{	
		String prefix = getPrefix(tagName);
		String schemaURI;
		if (prefix.equals(""))
			schemaURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else
			schemaURI = doc.lookupNamespaceURI(prefix);
		String typeName = getName(tagName);

		return findElement(parentPackage, schemaURI, typeName);
	}

	// get namespace by schemaURI
	public static Namespace findNamespace(String schemaURI)
	{
		return NiemNamespaces.get(schemaURI);
	}

	// get type by schemaURI and tagname
	public static UmlClass findType(UmlPackage parentPackage, String schemaURI, String tagName)
	{	
		if (parentPackage == referencePackage)
			return (UmlClass)NiemTypes.get(schemaURI + hashDelimiter + tagName);
		else
			return null;
	}

	// get type by prefix and tagname
	public static UmlClass findTypeByPrefix(UmlPackage parentPackage, String tagName)
	{
		String prefix = getPrefix(tagName);
		String schemaURI;
		if (prefix.equals(""))
			schemaURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		else if (prefix.equals(localPrefix))
			schemaURI = localSchemaURI;
		else
			schemaURI = doc.lookupNamespaceURI(prefix);
		String typeName = getName(tagName);

		return findType(parentPackage, schemaURI, typeName);
	}

	// extract tagname from XML tag
	public static String getName(String typeName)
	{
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i>=0) ? typeName.substring(i+1) : typeName;
	}

	// extract namespace prefix from XML tag
	public static String getPrefix(String typeName)
	{
		if (typeName == null)
			return "";
		int i = typeName.indexOf(namespaceDelimiter);
		return (i>=0) ? typeName.substring(0,i) : "";
	}

	// import NIEM mapping spreadsheet in CSV format
	public static void importCsv()
	{
		try {
			JFileChooser fc = new JFileChooser(directory);
			fc.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
			fc.setDialogTitle("NIEM Mapping CSV file");

			if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
				return;

			String filename = fc.getSelectedFile().getAbsolutePath();
			CSVReader reader = new CSVReader(new FileReader(filename));
			String[] nextLine;

			// Read header
			reader.readNext();

			// NIEM Read Mappings
			while ((nextLine = reader.readNext()) != null)
			{
				String className = nextLine[0].trim();
				String attributeName = nextLine[1].trim();
				for (int i=0;i<UmlClass.classes.size();i++)
				{
					UmlItem c = (UmlItem) UmlClass.classes.elementAt(i);
					if (c.stereotype().equals(niemStereotype) && (c.pretty_name().equals(className)))
					{
						if (attributeName.equals(""))
						{
							// Import NIEM Mapping to Class
							UmlCom.trace("Importing NIEM mapping for " + className);
							for (int p=4; p<map.length && p<nextLine.length; p++)
								c.set_PropertyValue(niemProperty(p),nextLine[p]);
							break;

						} else
						{
							UmlItem[] ch = c.children();
							for (UmlItem item: ch)
								if (item.stereotype().equals(niemStereotype) && (item.pretty_name().equals(attributeName)))
								{
									// Import NIEM Mapping to Attribute
									for (int p=4; p<map.length && p<nextLine.length; p++)
										item.set_PropertyValue(niemProperty(p),nextLine[p]);
									break;
								}
						}
					}
				}
			}
			reader.close();
		} 
		catch (FileNotFoundException e) { UmlCom.trace("importCsv: File not found" + e.toString()); }
		catch (IOException e) { UmlCom.trace("importCsv: IO exception" + e.toString()); }
	}

	// import NIEM reference model elements into HashMaps
	public static void importElements(String filename)
	{
		// UmlCom.trace("Importing elements from schema " + filename);
		String fn = "\n" + filename + "\n";
		try
		{
			// parse the schema
			doc = docBuilder.parse (new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc,true));
			Namespace ns = importNamespaces();


			// compile XPath queries
			XPathExpression xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import elements
			NodeList list = (NodeList)xPath.evaluate("xs:element[@name]", doc.getDocumentElement(), XPathConstants.NODESET);	
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element e = (Element)list.item(i);
				String en = e.getAttribute("name");
				String et = e.getAttribute("type");
				if (et.equals(""))
					et = localPrefix + namespaceDelimiter + abstractTypeName;
				try 
				{
					UmlClassInstance ci = addElement(ns.referenceClassView, ns.schemaURI, en, et);
					if (ci != null)
						ci.set_Description(xe.evaluate(e));
				}
				catch (Exception re) { UmlCom.trace(fn + "importElements: cannot create element " + en + " of type " + et + " "+ re.toString()); fn="";}
			}
		}
		catch (Exception e) { UmlCom.trace(fn + "importElements: " + e.toString()); fn= ""; }
	}

	// import NIEM reference model elements in Types into HashMaps
	public static void importElementsInTypes(String filename)
	{
		// UmlCom.trace("Importing elements in types from schema " + filename);
		String fn = "\n" + filename + "\n";
		try
		{
			// parse the schema
			doc = docBuilder.parse (new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc,true));
			Namespace ns = importNamespaces();

			// import base types for simple types (codes)
			Node root = doc.getDocumentElement();
			NodeList list = (NodeList)xPath.evaluate("xs:simpleType[@name]/xs:restriction[1][@base]", root, XPathConstants.NODESET);
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element r = (Element)list.item(i);
				Element s = (Element)r.getParentNode();
				String en = s.getAttribute("name");
				String pt = r.getAttribute("base");

				UmlClass c = findType((UmlPackage)(ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: type not found: " + en); 
					fn="";
					continue;
				}
				UmlClass p = findTypeByPrefix((UmlPackage)(ns.referenceClassView.parent()), pt);
				if (p == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt); 
					fn="";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation,c,p);
				}
				catch (Exception re)  { UmlCom.trace(fn = "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString()); fn="";}
			}

			// import base types for complex types (codes)
			list = (NodeList)xPath.evaluate("xs:complexType[@name]/xs:simpleContent[1]/xs:extension[1][@base]", root, XPathConstants.NODESET);
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element e = (Element)list.item(i);
				Element ct = (Element)e.getParentNode().getParentNode();
				String en = ct.getAttribute("name");
				String pt = e.getAttribute("base");

				UmlClass c = findType((UmlPackage)(ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: type not found: " + en); 
					fn="";
					continue;
				}
				UmlClass p = findTypeByPrefix((UmlPackage)(ns.referenceClassView.parent()), pt);
				if (p == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt); 
					fn="";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation,c,p);
				}
				catch (Exception re)  { UmlCom.trace(fn + "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString()); fn="";}
			}

			// import base types and elements for complex types
			list = (NodeList)xPath.evaluate("xs:complexType[@name]/xs:complexContent[1]/xs:extension[1][@base]", root, XPathConstants.NODESET);
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element e = (Element)list.item(i);
				Element ct = (Element)e.getParentNode().getParentNode();
				String en = ct.getAttribute("name");
				String pt = e.getAttribute("base");

				UmlClass c = findType((UmlPackage)(ns.referenceClassView.parent()), ns.schemaURI, en);
				if (c == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: type not found: " + en); 
					fn="";
					continue;
				}
				UmlClass p = findTypeByPrefix((UmlPackage)(ns.referenceClassView.parent()), pt);
				if (p == null)
				{ 
					UmlCom.trace(fn + "importElementsInType: base type not found: " + pt); 
					fn="";
					continue;
				}
				try {
					UmlBaseRelation.create(aRelationKind.aGeneralisation,c,p);
				}
				catch (Exception re)  { UmlCom.trace(fn + "importElementsInType: cannot relate " + en + " to " + pt + " " + re.toString()); fn="";}

				// import elements in type
				List<UmlClassInstance> enlist = (List<UmlClassInstance>)(NiemElementsInType.get(ns.schemaURI + hashDelimiter + en));
				if (enlist == null)
				{
					enlist = new ArrayList<UmlClassInstance>();
					NiemElementsInType.put(ns.schemaURI + hashDelimiter + en, enlist);
				}
				NodeList elist = (NodeList)xPath.evaluate("xs:sequence[1]/xs:element[@ref]", e, XPathConstants.NODESET);
				for (int j=0; j < elist.getLength(); j++)
				{
					Element e2 = (Element)elist.item(j);
					String et = e2.getAttribute("ref");
					UmlAttribute a = null;
					try {
						a = addElementInType(ns.referenceClassView, ns.schemaURI, en, et);
					}
					catch (Exception re) { UmlCom.trace(fn + "importElementsInTypes: cannot create element " + et + " in type " + en + " " + re.toString()); fn="";}
					UmlClassInstance ci = null;
					try {
						ci = findElementByPrefix((UmlPackage)(ns.referenceClassView.parent()), et);
					}
					catch (Exception re) { UmlCom.trace(fn + "importElementsInTypes: cannot find element " + et + " " + re.toString()); fn=""; }
					if (ci == null)
					{ 
						UmlCom.trace(fn + "importElementsInTypes: cannot find element " + et); 
						fn="";
						continue;
					}
					String d = ci.description();
					if (d != null)
						a.set_Description(d);
					UmlClass t = ci.type();
					if (t != null)
					{
						UmlTypeSpec ct2 = new UmlTypeSpec();
						ct2.type = ci.type();
						a.set_Type(ct2);
					}
					enlist.add(ci);
				}	
			}
		}
		catch (Exception e) { UmlCom.trace(filename + "\nimportElementsInTypes: " + e.toString()); }
	}

	// import NIEM reference model elements in Types into HashMaps
	public static void importEnumerations(String filename)
	{
		// UmlCom.trace("Importing enumerations from schema " + filename);
		String fn = "\n" + filename + "\n";
		try
		{
			// parse schema
			doc = docBuilder.parse (new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc,true));
			Namespace ns = importNamespaces();

			// compile XPath queries
			XPathExpression xe1 = xPath.compile("xs:enumeration");
			XPathExpression xe2 = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import enumerated values for simple types (codes)
			NodeList list = (NodeList)xPath.evaluate("xs:simpleType[@name]/xs:restriction[1]/[ns:enumeration]", doc.getDocumentElement(), XPathConstants.NODESET);
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element r = (Element)list.item(i);
				Element s = (Element)r.getParentNode();
				String en = s.getAttribute("name");

				// import elements in type
				List<UmlExtraClassMember> cmlist = (List<UmlExtraClassMember>)(NiemEnumerations.get(ns.schemaURI + hashDelimiter + en));
				if (cmlist == null)
				{
					cmlist = new ArrayList<UmlExtraClassMember>();
					NiemEnumerations.put(ns.schemaURI + hashDelimiter + en, cmlist);
				}
				NodeList elist = (NodeList)xe1.evaluate(r, XPathConstants.NODESET);
				for (int j=0; j < elist.getLength(); j++)
				{
					Element e = (Element)elist.item(j);
					String et = e.getAttribute("value");
					try {
						UmlExtraClassMember cm = addEnumeration(ns.referenceClassView, ns.schemaURI, en, et);
						cm.set_Description(xe2.evaluate(e));
						cmlist.add(cm);
					}
					catch (Exception re) { UmlCom.trace(fn + "importEnumerations: cannot create value " + et + " in type " + en + " " + re.toString()); fn="";}
				}
			}
		}
		catch (Exception e) { UmlCom.trace(fn + "importEnumerations: " + e.toString()); fn="";}
	}

	// import namespaces and return target namespace
	public static Namespace importNamespaces()
	{
		// reset prefixes
		//NiemPrefixes.clear();

		NamedNodeMap nslist = doc.getDocumentElement().getAttributes();
		for(int i = 0 ; i < nslist.getLength(); i++)
		{
			Node attr = nslist.item(i);
			String aname = attr.getNodeName();
			if (aname.startsWith("xmlns"))
			{
				String prefix = (aname.equals("xmlns")) ? attr.getNodeValue() : aname.substring(6);
				addNamespace(referencePackage, prefix, attr.getNodeValue());
			}
		}

		// get target namespace
		Namespace ns = null;
		try {
			String schemaURI = xPath.evaluate("xs:schema/@targetNamespace",doc);
			ns = NiemNamespaces.get(schemaURI);
			if (ns == null)
				return findNamespace(localSchemaURI);

			// set namespace description
			ns.referenceClassView.set_Description(xPath.evaluate("xs:schema/xs:annotation[1]/xs:documentation[1]", doc));
		}
		catch (NullPointerException re) { UmlCom.trace("importTypes: null pointer "); }
		catch (Exception e) { UmlCom.trace("importNamespaces: " + e.toString()); }
		return ns;
	}

	// import NIEM refernce model into HashMaps to support validation of NIEM elements and types
	public static void importSchemaDir(UmlPackage root, Boolean includeEnums) throws IOException
	{
		// Configure DOM
		try {
			docBuilderFactory.setNamespaceAware(true);
			docBuilder = docBuilderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) { UmlCom.trace("importSchemaDir: could not configure parser " + e.toString()); }

		directory = root.propertyValue("niem dir");

		// in java it is very complicated to select
		// a directory through a dialog, and the dialog
		// is very slow and ugly
		JFrame frame = new JFrame();
		JFileChooser fc = new JFileChooser(directory);

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Directory of the schema to be imported");

		if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
			return;

		directory = fc.getSelectedFile().getAbsolutePath();
		root.set_PropertyValue("niem dir",directory);
		Path path = FileSystems.getDefault().getPath(directory);

		int passes = (includeEnums) ? 4 : 3;
		
		// Walk directory to import in passes (1: types, 2: elements, 3: elements in types, 4: enumerations 
		for (importPass = 0; importPass< passes; importPass++) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					String filename = file.toString();
					if (filename.endsWith(".xsd"))
						switch (NiemTools.importPass)
						{
						case 0:
							importTypes(filename);
							break;
						case 1:
							importElements(filename);
							break;
						case 2:
							importElementsInTypes(filename);
							break;
							/* case 3:
							importEnumerations(filename);
							break;*/
						}
					return FileVisitResult.CONTINUE;
				}
			}
		);
		}

		// Sorting
		UmlCom.trace("Sorting namespaces");
        referencePackage.sort();
		
		UmlCom.trace("Namespaces: " + NiemNamespaces.size());
		UmlCom.trace("Types: " + NiemTypes.size());
		UmlCom.trace("Elements: " + NiemElements.size());
	}

	// import NIEM reference model types into HashMaps
	public static void importTypes(String filename)
	{
		//UmlCom.trace("Importing types from schema " + filename);
		String fn = "\n" + filename + "\n";
		try
		{
			// parse the schema
			doc = docBuilder.parse (new File(filename));
			xPath.setNamespaceContext(new NamespaceResolver(doc,true));
			Namespace ns = importNamespaces();

			// compile XPath queries
			XPathExpression xe = xPath.compile("xs:annotation[1]/xs:documentation[1]");

			// import types
			NodeList list = (NodeList)xPath.evaluate("xs:complexType|xs:simpleType[@name]", doc.getDocumentElement(), XPathConstants.NODESET);
			for(int i = 0 ; i < list.getLength(); i++)
			{
				Element e = (Element)list.item(i);
				String nodeType = e.getNodeName();
				String en = e.getAttribute("name");
				try 
				{
					UmlClass c = addType(ns.referenceClassView, ns.schemaURI, en);
					if (c == null)
					{
						UmlCom.trace("importTypes: cannot create type " + en);
						continue;
					}
					c.set_Description(xe.evaluate(e));
					if (nodeType == "xs:simpleType")
						c.set_Stereotype("enum_pattern");
					NiemTypes.put(ns.schemaURI + hashDelimiter + en, c);
				}
				catch (NullPointerException re) { UmlCom.trace(fn + "importTypes: null pointer " + en); fn="";}
				catch (Exception re) { UmlCom.trace(fn + "importTypes: cannot create type " + en + " " + re.toString()); fn="";}
			}
		}
		catch (NullPointerException re) { UmlCom.trace(fn + "importTypes: null pointer "); fn="";}
		catch (Exception re) { UmlCom.trace(fn + "importTypes: " + re.toString()); fn = "";}
	}

	// indicate whether an XML prefix matches a NIEM namespace
	public static boolean isNiemSchema(String prefix)
	{
		//return Arrays.asList(niemSchema).contains(schema);
		if (NiemNamespaces.containsKey(prefix))
			return (NiemNamespaces.get(prefix).nsClassView.parent().equals(subsetPackage));
		else
			return false;
	}

	// output one column of the NIEM mapping spreadsheet in HTML format
	//  protected static String columnHtml(String newValue, String oldValue, String container, String schema) throws IOException

	// output one line of the NIEM mapping spreadsheet in CSV format
	public static String[] itemCsv(UmlItem item)
	{
		String[] nextLine = new String[map.length];

		// Export Class and Property
		switch (item.kind().value())
		{
		case anItemKind._aClass:
			nextLine[0]=item.pretty_name();
			nextLine[1]="";
			break;
		default:
			nextLine[0]=item.parent().pretty_name();
			nextLine[1]=item.pretty_name();
			break;
		}

		// Export Multiplicity
		switch (item.kind().value())
		{
		case anItemKind._aClass:
			nextLine[2]="";
			break;
		case anItemKind._aRelation:
			UmlRelation rel = (UmlRelation) item;
			if ((rel.relationKind() == aRelationKind.aGeneralisation) ||
					(rel.relationKind() == aRelationKind.aRealization))
				return null;
			nextLine[2]=rel.multiplicity();
			break;
		case anItemKind._anAttribute:
			UmlAttribute a = (UmlAttribute) item;
			nextLine[2]=a.multiplicity();
			break;
		default:
			return null;
		}

		// Export Description
		nextLine[3]=item.description();

		// Export NIEM Mapping
		if (item.stereotype().equals(niemStereotype))
		{
			// String prefix = getPrefix((String)(item.propertyValue(niemStereotype+":Type")));
			for (int p=4;p<map.length;p++)
				nextLine[p]=item.propertyValue(niemProperty(p));
		}
		return nextLine;
	}

	// return the NIEM steteotype associated with a column in the NIEM mapping spreadsheet
	public static String niemProperty(int p)
	{
		return niemStereotype + stereotypeDelimiter + map[p][1];
	}

	// output UML objects
	public static void outputUML()
	{
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlClass.all.iterator();
		while (it.hasNext())
		{
			UmlItem item = it.next();
			if (item.known)
			{
				UmlCom.trace("\nKind: " + String.valueOf(item.kind().value()));
				UmlCom.trace("ID: " + String.valueOf(item.getIdentifier()));
				UmlCom.trace("Name: " + item.pretty_name());
				if (item.parent() != null)
				{
					UmlCom.trace("Parent: " + String.valueOf(item.parent().getIdentifier()));
					UmlCom.trace("Parent Name: " + String.valueOf(item.parent().pretty_name()));
				}
			}
			else
				UmlCom.trace("\nName: " + item.pretty_name());
		}
	}

	// (re-)associate the NIEM stereotype with all properties in the CIM
	public static void resetStereotype()
	{
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it=UmlClass.all.iterator();
		while (it.hasNext())
		{
			UmlItem item = (UmlItem)it.next();
			if (item.stereotype().equals("niem:niem"))
			{
				item.set_Stereotype(niemStereotype);
				item.applyStereotype();
			}
		}
	}

	// output a line of the NIEM mapping spreadhseet in HTML format
	public static void writeLineHtml(UmlItem item)
	{
		try {
			// Export Class, Property and Multiplicity
			// UmlCom.trace(item.pretty_name());
			switch (item.kind().value())
			{
			case anItemKind._aClass:
			{
				fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
				item.write();
				fw.write("</td><td>");
				fw.write("</td><td>");
			}
			break;
			case anItemKind._anAttribute:
			{
				fw.write("<tr><td>");
				item.parent().write();
				fw.write("</td><td>");
				item.write();
				fw.write("</td><td>");
				UmlAttribute a = (UmlAttribute) item;
				fw.write(a.multiplicity());
			}
			break;
			case anItemKind._aRelation:
			{
				UmlRelation rel = (UmlRelation) item;
				if ((rel.relationKind() == aRelationKind.aGeneralisation) ||
						(rel.relationKind() == aRelationKind.aRealization))
					return;
				else {
					fw.write("<tr><td>");
					item.parent().write();
					fw.write("</td><td>");
					item.write();
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
			String invalidFGColor = "#ff0000";
			String changedFGColor = "#0000ff";
			String defaultFGColor = "#000000";
			String fgcolor, bgcolor;

			if (item.stereotype().equals(niemStereotype))
			{
				for (p=4;p<map.length;p++)
				{
					column[p]= (String)(item.propertyValue(niemProperty(p)));
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
				if (!extension)
				{

					String[] tt = column[5].split(",");
					for (String ttt : tt)
					{
						ttt = ttt.trim();
						if (!NiemTypes.containsKey(ttt))
							fgcolor = invalidFGColor;
					}
				}
				fw.write(columnHtml(column[5], bgcolor, fgcolor));

				// export Property
				fgcolor = defaultFGColor;
				if ((!column[5].equals("") && (!extension)))
				{
					String[] pp = column[6].split(",");
					for (String ppp : pp)
					{
						ppp = ppp.trim();
						Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(ppp);
						if (!mat.find())
						{
							List<UmlClassInstance> list = (List<UmlClassInstance>)(NiemElementsInType.get(column[5]));
							if (list == null)
								fgcolor = invalidFGColor;
							else if (!list.contains(ppp))
								fgcolor = invalidFGColor;
						}
					}
				}
				fw.write(columnHtml(column[6], bgcolor, fgcolor));

				// export BaseType
				fgcolor = defaultFGColor;
				if (!extension)
				{
					String[] bb = column[7].split(",");
					for (String bbb : bb)
					{
						bbb = bbb.trim();
						if (!NiemTypes.containsKey(bbb))
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
		}
		catch (IOException e) { UmlCom.trace("writeLineHtml IO exception: " + e.toString()); }
	}
}
