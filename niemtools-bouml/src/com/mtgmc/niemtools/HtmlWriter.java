package com.mtgmc.niemtools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlItem;
import fr.bouml.UmlRelation;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class HtmlWriter {

	/** returns the html for a column */
	static String getColumnHtml(String value, String bgcolor, String fgcolor, Boolean wordwrap) {
		String style = wordwrap ? "word-wrap: break-word" : "";
		return "<td  style=\"" + style + "\" bgcolor=\"" + bgcolor + "\"><font color = \"" + fgcolor + "\">" + value
				+ "</font></td>";
	}

	/** writes a column of the NIEM mapping spreadsheet in HTML format to a file */
	static void writeItemHtml(FileWriter fw, UmlItem item) throws IOException {
		if (item.known) {
			fw.write("<a href=\"");
			if (!UmlItem.flat && (item.parent() != null) && (item.parent().kind() == anItemKind.aClass)) {
				fw.write("class");
				fw.write(String.valueOf(item.parent().getIdentifier()));
			} else
				fw.write("index");
			fw.write(HtmlWriter.HTML_FILE_TYPE + "#ref");
			fw.write(String.valueOf(item.kind().value()));
			fw.write('_');
			fw.write(String.valueOf(item.getIdentifier()));
			fw.write("\"><b>");
			fw.write(item.name());
			fw.write("</b></a>");
		} else
			fw.write(item.name());
	}

	static final String HTML_FILE_TYPE = ".html";

	/** writes a line of the NIEM mapping spreadsheet in HTML format to a file */
	void writeLineHtml(FileWriter fw, UmlItem item) {
		try {
			// Export Class, Property and Multiplicity
			// trace("writeLineHtml: " + item.name());
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
			int columnIndex;
			// String oldValue, container;
			String[] column = new String[NiemUmlClass.NIEM_STEREOTYPE_MAP.length];
			String extensionBGColor = "#ffd700";
			String defaultBGColor = "#ffffff";
			String invalidFGColor = "#ff0000"; // invalid NIEM mappings are red
			String changedFGColor = "#0000ff"; // changes from the previous
			// version are blue
			String defaultFGColor = "#000000";
			String fgcolor, bgcolor;
	
			if (item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE_TYPE)) {
				for (columnIndex = 4; columnIndex < NiemUmlClass.NIEM_STEREOTYPE_MAP.length; columnIndex++) {
					column[columnIndex] = (item.propertyValue(NiemUmlClass.getNiemProperty(columnIndex)));
					column[columnIndex] = (column[columnIndex] != null) ? column[columnIndex].trim() : "";
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
				fw.write(getColumnHtml(XPath, bgcolor, fgcolor, true));
	
				// export Type
				String typeName = column[5].trim();
				String typePrefix = NamespaceModel.getPrefix(typeName);
				bgcolor = defaultBGColor;
				fgcolor = defaultFGColor;
				if (!typeName.equals("")) {
					if (NamespaceModel.isNiemPrefix(NamespaceModel.getPrefix(typeName)) && !NiemUmlClass.isNiemType(typeName))
						fgcolor = invalidFGColor;
					if (!NamespaceModel.isNiemPrefix(typePrefix) && !NamespaceModel.isExternalPrefix(typePrefix))
						bgcolor = extensionBGColor;
				}
				fw.write(getColumnHtml(typeName, bgcolor, fgcolor, true));
	
				// export Property
				String elementLine = column[6];
				fgcolor = defaultFGColor;
				bgcolor = defaultBGColor;
				if (!elementLine.equals("")) {
					String[] elementNames = elementLine.split(",");
					for (String elementName : elementNames) {
						elementName = elementName.trim();
						Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(elementName);
						if (!mat.find()) {
							String prefix = NamespaceModel.getPrefix(elementName);
							if (NamespaceModel.isNiemPrefix(typePrefix) && NamespaceModel.isNiemPrefix(prefix)
									&& !NiemUmlClass.isNiemElementInType(typeName, elementName))
								fgcolor = invalidFGColor;
							prefix = NamespaceModel.getPrefix(elementLine);
							if (!NamespaceModel.isNiemPrefix(prefix) && !NamespaceModel.isExternalPrefix(prefix))
								bgcolor = extensionBGColor;
						}
					}
				}
				fw.write(getColumnHtml(elementLine, bgcolor, fgcolor, true));
	
				// export BaseType
				String baseType = column[7].trim();
				String basePrefix = NamespaceModel.getPrefix(baseType);
				bgcolor = defaultBGColor;
				fgcolor = defaultFGColor;
				if (!baseType.equals("") && !baseType.equals(NiemUmlClass.ABSTRACT_TYPE_NAME)) {
					if (!NamespaceModel.isNiemPrefix(basePrefix) && !NamespaceModel.isExternalPrefix(basePrefix))
						bgcolor = extensionBGColor;
					if (NamespaceModel.isNiemPrefix(basePrefix) && !NiemUmlClass.isNiemType(baseType))
						fgcolor = invalidFGColor;
				}
				fw.write(getColumnHtml(baseType, bgcolor, fgcolor, true));
	
				// export Multiplicity
				bgcolor = defaultBGColor;
				String multiplicity = column[8];
				fgcolor = (multiplicity.equals(column[10])) ? defaultFGColor : changedFGColor;
				String minOccurs = NiemUmlClass.getMinOccurs(multiplicity);
				String maxOccurs = NiemUmlClass.getMaxOccurs(multiplicity);
				try {
					if (Integer.parseInt(minOccurs) < 0)
						throw new NumberFormatException();
					if (!maxOccurs.equals("unbounded") && (Integer.parseInt(maxOccurs) < 1))
						throw new NumberFormatException();
				} catch (NumberFormatException e) {
					fgcolor = invalidFGColor;
				}
				fw.write(getColumnHtml(column[8], bgcolor, fgcolor, false));
	
				// export Old XPath, Multiplicity, Mapping Notes, code list
				fgcolor = defaultFGColor;
				fw.write(getColumnHtml(column[9], bgcolor, fgcolor, true));
				fw.write(getColumnHtml(column[10], bgcolor, fgcolor, false));
				fw.write(getColumnHtml(column[11], bgcolor, fgcolor, true));
				fw.write(getColumnHtml(column[12], bgcolor, fgcolor, true));
			}
			fw.write("</tr>");
		} catch (Exception e) {
			Log.trace("writeLineHtml: error " + e.toString());
		}
	}

}