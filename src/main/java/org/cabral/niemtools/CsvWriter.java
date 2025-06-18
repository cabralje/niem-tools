/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2025 James E. Cabral Jr., jim@cabral.org, http://github.com/cabralje
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
/**
 * CsvWriter is responsible for exporting UML model elements and their NIEM mappings to a CSV file.
 * <p>
 * It uses the OpenCSV library to write CSV files and expects UML model elements (classes, attributes,
 * relations, and class instances) from the Bouml tool. The CSV output includes class and property
 * information, descriptions, and NIEM mapping properties as defined in the NiemUmlModel.
 * </p>
 * <p>
 * Main functionalities:
 * <ul>
 *   <li>Converts UML items to CSV row representations based on their kind (class, attribute, relation, etc.).</li>
 *   <li>Exports all relevant UML classes and their children (attributes and relations) to a CSV file.</li>
 *   <li>Handles file creation, writing headers, and error logging.</li>
 * </ul>
 * </p>
 * <p>
 * Dependencies:
 * <ul>
 *   <li>OpenCSV for CSV writing</li>
 *   <li>Bouml UML model classes (UmlItem, UmlClass, UmlAttribute, UmlRelation, etc.)</li>
 *   <li>NiemUmlModel for NIEM mapping logic</li>
 *   <li>Log utility for tracing and debugging</li>
 * </ul>
 * </p>
 * @author James Cabral
 * @version 1.0
 */
package org.cabral.niemtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.opencsv.CSVWriter;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;
import fr.bouml.UmlRelation;
import fr.bouml.UmlTypeSpec;
import fr.bouml.anItemKind;

public class CsvWriter {

    /**
     * returns a line of the NIEM mapping spreadsheet in CSV format
     */
    /**
     * @param item
     * @return
     */
    String[] getItemCsv(UmlItem item) {
        String[] nextLine = new String[NiemUmlModel.getNiemMap().length];
        try {
            // Export Class and Property
            switch (item.kind().value()) {
                case anItemKind._aClass -> {
                    nextLine[0] = item.name();
                    nextLine[1] = "";
                    nextLine[2] = "";
                    nextLine[3] = "";
                }
                case anItemKind._anAttribute -> {
                    UmlAttribute a = (UmlAttribute) item;
                    nextLine[0] = item.parent().name();
                    nextLine[1] = item.name();
                    UmlTypeSpec t = a.type();
                    if (t != null)
                        nextLine[2] = t.toString(); 
                    else
                        nextLine[2] = "";
                    nextLine[3] = a.multiplicity();
                }
                case anItemKind._aRelation -> {
                    UmlRelation r = (UmlRelation) item;
                    nextLine[0] = item.parent().name();
                    nextLine[1] = r.name();
                    nextLine[2] = "";
                    nextLine[3] = r.multiplicity();
                }
                case anItemKind._aClassInstance -> {
                    UmlClassInstance ci = (UmlClassInstance) item;
                    nextLine[0] = "";
                    nextLine[1] = item.name();
                    UmlClass c = ci.type();
                    if (c != null)
                        nextLine[2] = c.name(); 
                    else
                        nextLine[2] = "";
                    nextLine[3] = "";
                }
                default -> {
                    nextLine[0] = item.parent().name();
                    nextLine[1] = item.name();
                    nextLine[3] = "";
                }
            }
        } catch (Exception e) {
            Log.trace("itemCsv: error importing class, property multiplicity " + e.toString());
        }

        // Export Description
        nextLine[4] = item.description();

        // Export NIEM Mapping
        if (NiemUmlModel.isNiemUml(item))
            for (int column = 5; column < NiemUmlModel.getNiemMap().length; column++)
                nextLine[column] = item.propertyValue(NiemUmlModel.getNiemProperty(column));

        return nextLine;
    }

    /**
     * export CSV file
     */
    /**
     * @param directory
     * @param filename
     */
    //@SuppressWarnings("unchecked")
    void exportCsv(String filename) {
        //File file = Paths.get(directory, filename).toFile();
        File file = new File(filename);
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null)
                parentFile.mkdirs();
            FileWriter fw = new FileWriter(file);
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(fw);
            } finally {

                // Write header
                final String[][] map = NiemUmlModel.getNiemMap();
                String[] nextLine = new String[map.length];
                for (int column = 0; column < map.length; column++)
                    nextLine[column] = map[column][0];
                try {
                    if (writer != null)
                        writer.writeNext(nextLine);
                } catch (Exception e) {
                    Log.trace("exportCsv: writing error" + e.toString());
                }

                // Export NIEM Mappings for Classes
                @SuppressWarnings("unchecked")
                ArrayList<UmlItem> classes = new ArrayList<>(UmlClass.classes);
                Iterator<UmlItem> it = (classes.iterator());
                while (it.hasNext()) {
                    UmlItem thisClass = it.next();
                    Log.debug("exportCsv: " + thisClass.name());
                    if (!NiemUmlModel.isNiemUml(thisClass))
                        continue;
                    nextLine = getItemCsv(thisClass);
                    Log.debug("exportCsv: write line");
                    if (writer != null)
                        writer.writeNext(nextLine);

                    // Export NIEM Mapping for Attributes and Relations
                    for (UmlItem item : thisClass.children()) {
                        if (!NiemUmlModel.isNiemUml(item))
                            continue;
                        nextLine = getItemCsv(item);
                        Log.debug("exportCsv: write line");
                        if (writer != null && nextLine != null)
                            writer.writeNext(nextLine);
                    }
                }
                if (writer != null)
                    writer.close();
                Log.debug("exportCsv: CSV file created " + file.toString());
            }

        } catch (FileNotFoundException e) {
            Log.trace("exportCsv: file not found error " + e.toString());
        } catch (NullPointerException e) {
            Log.trace("exportCsv: null pointer error " + e.toString());
        //} catch (ClassNotFoundException e) {
        //	Log.trace("exportCsv: class not found error " + e.toString());
        } catch (IOException e) {
            Log.trace("exportCsv: IO error " + e.toString());
        } catch (Exception e) {
            Log.trace("exportCsv: error " + e.toString());
        } finally {
            Log.debug("exportCsv: exiting");
        }
    }

}
