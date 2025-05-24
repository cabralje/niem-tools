package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlItem;
import fr.bouml.UmlRelation;
import fr.bouml.UmlTypeSpec;
import fr.bouml.aRelationKind;
import fr.bouml.anItemKind;

public class HtmlWriter {

    private static final String HTML_FILE_TYPE = ".html";

    /**
     * @param directory
     * @param filename
     */
    void exportMappingHtml(String filename) {
        try {
            // Write rest of header
            File file = new File(filename);
            Log.debug("exportMappingHtml: path " + file.getAbsolutePath());
            File parentFile = file.getParentFile();
            if (parentFile != null)
                parentFile.mkdirs();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("<html>");
                fw.write("<head><title>" + NiemUmlModel.MAPPING_SPREADSHEET_TITLE
                        + "</title><link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" /></head>"
                        + "<body><div class = \"title\">" + NiemUmlModel.MAPPING_SPREADSHEET_TITLE + "</div>"
                        + "<table style=\"table-layout: fixed; width: 100%\"><tr bgcolor=\"#f0f0f0\">");
                for (String[] column : NiemUmlModel.getNiemMap())
                    fw.write("<td style=\"word-wrap: break-word\">" + column[0] + "</td>");
                fw.write("</tr>\n");

                // Show NIEM Mappings for Classes
                Iterator<UmlItem> it = (UmlClass.classes.iterator());
                while (it.hasNext()) {
                    UmlItem thisClass = it.next();
                    if (thisClass == null) {
                        Log.trace("exportHtml: null class");    
                        continue;
                    }
                    if (NiemUmlModel.isNiemUml(thisClass)) {
                        writeLineHtml(fw, thisClass);

                        // Show NIEM Mapping for Attributes and Relations
                       // if (!NiemUmlModel.isEnumeration(thisClass))
                            for (UmlItem item : thisClass.children())
                            //    if (NiemUmlModel.isNiemUml(item) && !NiemUmlModel.isFacet(item))
                                if (NiemUmlModel.isNiemUml(item))
                                    writeLineHtml(fw, item);
                    }
                }
                fw.write("</table>\n");
                fw.write("</body></html>");
            }
        } catch (IOException e) {
            Log.trace("exportMappingHtml: error " + e.toString());
        } catch (Exception e) {
            Log.trace("exportMappingHtml: error " + e.toString());
        }
    }

    /**
     * returns the html for a column
     */
    /**
     * @param value
     * @param bgcolor
     * @param fgcolor
     * @param wordwrap
     * @return
     */
    private String getColumnHtml(String value, String bgcolor, String fgcolor, Boolean wordwrap) {
        String style = wordwrap ? "word-wrap: break-word" : "";
        return "<td  style=\"" + style + "\" bgcolor=\"" + bgcolor + "\"><font color = \"" + fgcolor + "\">" + value
                + "</font></td>";
    }

    /**
     * writes a column of the NIEM mapping spreadsheet in HTML format to a file
     */
    /**
     * @param fw
     * @param item
     * @throws IOException
     */
    private void writeItemHtml(FileWriter fw, UmlItem item) throws IOException {
        if (item.known) {
            fw.write("<a href=\"");
            if (!UmlItem.flat && (item.parent() != null) && (item.parent().kind() == anItemKind.aClass)) {
                fw.write("class");
                fw.write(String.valueOf(item.parent().getIdentifier()));
            } else
                fw.write("index");
            fw.write(HTML_FILE_TYPE + "#ref");
            fw.write(String.valueOf(item.kind().value()));
            fw.write('_');
            fw.write(String.valueOf(item.getIdentifier()));
            fw.write("\"><b>");
            fw.write(item.name());
            fw.write("</b></a>");
        } else
            fw.write(item.name());
    }

    /**
     * writes a line of the NIEM mapping spreadsheet in HTML format to a file
     */
    /**
     * @param fw
     * @param item
     */
    private void writeLineHtml(FileWriter fw, UmlItem item) {
        try {
            // Export Class, Property and Multiplicity
            // trace("writeLineHtml: " + item.name());
            switch (item.kind().value()) {
                case anItemKind._aClass -> {
                    fw.write("<tr bgcolor=\"#f0f0f0\"><td style=\"word-wrap: break-word\">");
                    writeItemHtml(fw, item);
                    fw.write("</td><td>");
                    fw.write("</td><td>");
                    fw.write("</td><td>");
                }
                case anItemKind._anAttribute -> {
                    fw.write("<tr><td style=\"word-wrap: break-word\">");
                    writeItemHtml(fw, item.parent());
                    fw.write("</td><td style=\"word-wrap: break-word\">");
                    writeItemHtml(fw, item);
                    fw.write("</td><td>");
                    UmlAttribute a = (UmlAttribute) item;
                    UmlTypeSpec t = a.type();
                    if (t != null)
                        fw.write(t.toString());
                    fw.write("</td><td>");
                    fw.write(a.multiplicity());
                }
                case anItemKind._aRelation -> {
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
                        fw.write("</td><td>");
                        fw.write(rel.multiplicity());
                    }
                }
                default -> {
                    return;
                }
            }
            fw.write("</td><td>");
        } catch (IOException e) {
            Log.trace("writeLineHtml: error " + e.toString());
        } catch (Exception e) {
            Log.trace("writeLineHtml: error " + e.toString());
        }

        try {
            // Export Description
            if (item.description() != null)
                fw.write(item.description());
            fw.write("</td>");

            // Export NIEM Mapping
            int columnIndex;
            // String oldValue, container;
            int mapLength = NiemUmlModel.getNiemMap().length;
            String[] column = new String[mapLength];
            String extensionBGColor = "#ffd700";
            String defaultBGColor = "#ffffff";
            String invalidFGColor = "#ff0000"; // invalid NIEM mappings are red
            String changedFGColor = "#0000ff"; // changes from the previous
            // version are blue
            String defaultFGColor = "#000000";
            String fgcolor, bgcolor;

            if (NiemUmlModel.isNiemUml(item)) {
                for (columnIndex = 5; columnIndex < mapLength; columnIndex++) {
                    column[columnIndex] = (item.propertyValue(NiemUmlModel.getNiemProperty(columnIndex)));
                    column[columnIndex] = (column[columnIndex] != null) ? column[columnIndex].trim() : "";
                }

                // determine if this is an extension
                /*
				 * Boolean extension = false; String[] xPathElements = column[5].split("/"); for
				 * (String element : xPathElements) { String prefix = getPrefix(element); if
				 * (!prefix.isEmpty() && !isNiemSchema(prefix) &&
				 * !isExternalPrefix(getPrefix(element.trim()))) { extension = true; continue; }
				 * }
                 */
                // export XPath
                try {
                    String XPath = column[5].trim();
                    String oldXPath = column[10].trim();
                    // bgcolor = (extension) ? extensionBGColor : defaultBGColor;
                    bgcolor = defaultBGColor;
                    fgcolor = (XPath.equals(oldXPath)) ? defaultFGColor : changedFGColor;
                    fw.write(getColumnHtml(XPath, bgcolor, fgcolor, true));
                } catch (Exception e) {
                    Log.trace("writeLineHtml: error writing XPath " + e.toString());
                }
                
                // export Type
                String typeName = column[6].trim();
                String typePrefix = NamespaceModel.getPrefix(typeName);
                bgcolor = defaultBGColor;
                fgcolor = defaultFGColor;
                try {
                    if (typeName != null && !typeName.isEmpty()) {
                        if (typePrefix == null) {
                            fgcolor = invalidFGColor;
                            Log.trace("writeLineHtml: type " + typeName + " has no prefix");
                            return;
                        }
                        if (NamespaceModel.isNiemPrefix(typePrefix) && !NiemUmlModel.isNiemType(typeName)) {
                            fgcolor = invalidFGColor;
                            Log.trace("writeLineHtml: type " + typeName + " is not in the NIEM reference model");
                        }
                        if (!NamespaceModel.isNiemPrefix(typePrefix) && !NamespaceModel.isExternalPrefix(typePrefix))
                            bgcolor = extensionBGColor;
                    }
                    fw.write(getColumnHtml(typeName, bgcolor, fgcolor, true));
                } catch (Exception e) {
                    Log.trace("writeLineHtml: error writing type " + typeName + " " + e.toString());
                }

                // export Property
                try {
                    String elementLine = column[7];
                    fgcolor = defaultFGColor;
                    bgcolor = defaultBGColor;
                    if (!elementLine.isEmpty()) {
                        String[] elementNames = elementLine.split(",");
                        for (String elementName : elementNames) {
                            elementName = elementName.trim();
                            Matcher mat = Pattern.compile("\\((.*?)\\)").matcher(elementName);
                            if (!mat.find()) {
                                String prefix = NamespaceModel.getPrefix(elementName);
                                if (NamespaceModel.isNiemPrefix(typePrefix) && NamespaceModel.isNiemPrefix(prefix)
                                        && !NamespaceModel.isAttribute(elementName) && !NiemUmlModel.isNiemElementInType(typeName, elementName)) {
                                    fgcolor = invalidFGColor;
                                    Log.trace("writeLineHtml: element " + elementName + " is not in type " + typeName + " in the NIEM reference model");
                                }
                                prefix = NamespaceModel.getPrefix(elementLine);
                                if (!NamespaceModel.isNiemPrefix(prefix) && !NamespaceModel.isExternalPrefix(prefix))
                                    bgcolor = extensionBGColor;
                            }
                        }
                    }
                    fw.write(getColumnHtml(elementLine, bgcolor, fgcolor, true));
                } catch (Exception e) { 
                    Log.trace("writeLineHtml: error writing element " + e.toString());
                }

                // export BaseType
                try {
                    String baseType = column[8].trim();
                    String basePrefix = NamespaceModel.getPrefix(baseType);
                    bgcolor = defaultBGColor;
                    fgcolor = defaultFGColor;
                    if (!baseType.isEmpty() && !NiemModel.isAbstract(baseType)) {
                        if (!NamespaceModel.isNiemPrefix(basePrefix) && !NamespaceModel.isExternalPrefix(basePrefix))
                            bgcolor = extensionBGColor;
                        if (NamespaceModel.isNiemPrefix(basePrefix) && !NiemUmlModel.isNiemType(baseType))
                            fgcolor = invalidFGColor;
                    }
                    fw.write(getColumnHtml(baseType, bgcolor, fgcolor, true));
                } catch (Exception e) {
                    Log.trace("writeLineHtml: error writing base type " + e.toString());
                }

                // export Multiplicity
                try {
                    bgcolor = defaultBGColor;
                    String multiplicity = column[9];
                    fgcolor = (multiplicity.equals(column[11])) ? defaultFGColor : changedFGColor;
                    String minOccurs = NiemUmlModel.getMinOccurs(multiplicity);
                    String maxOccurs = NiemUmlModel.getMaxOccurs(multiplicity);
                    try {
                        if (Integer.parseInt(minOccurs) < 0)
                            throw new NumberFormatException();
                        if (!maxOccurs.equals("unbounded") && (Integer.parseInt(maxOccurs) < 1))
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        fgcolor = invalidFGColor;
                        Log.trace("writeLineHtml: illegal multiplicity " + multiplicity + " in type " + typeName);
                    }
                    fw.write(getColumnHtml(column[9], bgcolor, fgcolor, false));
                } catch (Exception e) {
                    Log.trace("writeLineHtml: error writing multiplicity " + e.toString());
                }

                // export Old XPath, Multiplicity, Mapping Notes, code list
                try {
                    fgcolor = defaultFGColor;
                    fw.write(getColumnHtml(column[10], bgcolor, fgcolor, true));
                    fw.write(getColumnHtml(column[11], bgcolor, fgcolor, false));
                    fw.write(getColumnHtml(column[12], bgcolor, fgcolor, true));
                    fw.write(getColumnHtml(column[13], bgcolor, fgcolor, true));
                } catch (Exception e) {
                    Log.trace("writeLineHtml: error writing old XPath, multiplicity, mapping notes, code list " + e.toString());
                }
            }
            fw.write("</tr>");
        } catch (IOException e) {
            Log.trace("writeLineHtml: error " + e.toString());
        } catch (Exception e) {
            Log.trace("writeLineHtml: error " + e.toString());
        }
    }



}
