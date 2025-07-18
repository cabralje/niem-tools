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
 * Imports NIEM mappings from a CSV file and applies them to UML classes and instances.
 * <p>
 * This method reads a CSV file specified by the {@code filename} parameter, parses its contents,
 * and updates UML classes and class instances with NIEM property values based on the mappings
 * defined in the CSV. The method uses OpenCSV for parsing and expects the CSV to have a specific
 * structure:
 * <ul>
 *   <li>The first row is treated as a header and skipped.</li>
 *   <li>Each subsequent row should contain at least the class name and attribute name in the first two columns.</li>
 *   <li>NIEM property values are expected to start from the sixth column (index 5).</li>
 * </ul>
 * <p>
 * The method caches UML classes and instances for efficient lookup, and only updates those
 * that are recognized as NIEM UML elements. It handles mappings both at the class and attribute
 * levels, as well as for class instances.
 * <p>
 * Exceptions during file reading or CSV parsing are logged using the {@code Log} utility.
 * 
 * @author James Cabral
 * @version 1.0
 * @param filename the path to the CSV file containing NIEM mappings
 */
package org.cabral.niemtools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class CsvReader {

    /** Imports NIEM mappings from a CSV file and applies them to UML classes and instances.
     * @param filename
     */
    void importCsv(String filename) {
        // cache UML classes
        Map<String, UmlClass> UMLClasses = new ConcurrentHashMap<>();
        Map<String, UmlClassInstance> UMLInstances = new ConcurrentHashMap<>();
        @SuppressWarnings("unchecked")
        ArrayList<UmlItem> all = new ArrayList<>(
            UmlItem.all != null ? UmlItem.all : Collections.emptyList()
        );
        // Use streams to populate UMLClasses and UMLInstances
        all.stream()
            .filter(NiemUmlModel::isNiemUml)
            .forEach(item -> {
            if (item.kind() == anItemKind.aClass) {
                UmlClass c = (UmlClass) item;
                UMLClasses.putIfAbsent(c.name(), c);
            } else if (item.kind() == anItemKind.aClassInstance) {
                UmlClassInstance ci = (UmlClassInstance) item;
                UMLInstances.putIfAbsent(ci.name(), ci);
            }
            });

        try (FileReader fr = new FileReader(filename);
            CSVReader reader = new CSVReader(fr)) {
                Log.debug("importCsv: file read");
                Log.debug("importCsv: file parsed");

                // read header
                reader.readNext();
                Log.debug("importCsv: header read");

                int mapLength = NiemUmlModel.getNiemMap().length;
                while (true) {
                    String[] nextLine = reader.readNext();
                    if (nextLine == null) break;
                    String className = nextLine[0].trim();
                    String attributeName = nextLine[1].trim();

                    if (!className.isEmpty()) {
                        UmlClass type = UMLClasses.get(className);
                        if (type != null) {
                            if (attributeName.isEmpty()) {
                                // import NIEM mapping to class
                                Log.debug("importCsv: importing NIEM mapping for " + className);
                                IntStream.range(5, Math.min(mapLength, nextLine.length))
                                .forEach(column -> type.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column]));
                            } else {
                                // import NIEM Mapping to attribute
                                Arrays.stream(type.children())
                                .filter(item -> NiemUmlModel.isNiemUml(item) && item.name().equals(attributeName))
                                .forEach(item -> IntStream.range(5, Math.min(mapLength, nextLine.length))
                                    .forEach(column -> item.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column])));
                            }
                        }
                    } else if (!attributeName.isEmpty()) {
                        UmlClassInstance element = UMLInstances.get(attributeName);
                        if (element != null) {
                            // import NIEM mapping to class instance
                            Log.debug("importCsv: importing NIEM mapping for " + attributeName);
                            IntStream.range(5, Math.min(mapLength, nextLine.length))
                                .forEach(column -> element.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column]));
                        }
                    }
                }
    } catch (NoClassDefFoundError e) {
            Log.trace("importCsv: error - Exception" + e.toString());
        } catch (CsvValidationException e) {
            Log.trace("importCsv: error reading CSV " + e.toString());
        } catch (FileNotFoundException e) {
            Log.trace("importCsv: error - file not found" + e.toString());
        } catch (IOException e) {
            Log.trace("importCsv: error - IO exception" + e.toString());
        }
    }

}
