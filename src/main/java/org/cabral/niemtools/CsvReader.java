package org.cabral.niemtools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class CsvReader {

    /**
     * @param filename
     */
    // TODO importCsv: import non-nillable elements
    void importCsv(String filename) {
        // cache UML classes
        Map<String, UmlClass> UMLClasses = new ConcurrentHashMap<>();
        Map<String, UmlClassInstance> UMLInstances = new ConcurrentHashMap<>();
        //@SuppressWarnings("unchecked")
        Iterator<UmlItem> it = UmlItem.all.iterator();
        while (it.hasNext()) {
            UmlItem item = it.next();
            if (NiemUmlModel.isNiemUml(item)) {
                if (item.kind() == anItemKind.aClass) {
                    UmlClass c = (UmlClass) item;
                    if (!UMLClasses.containsKey(c.name()))
                        UMLClasses.put(c.name(), c);
                } else if (item.kind() == anItemKind.aClassInstance) {
                    UmlClassInstance ci = (UmlClassInstance) item;
                    if (!UMLInstances.containsKey(ci.name()))
                        UMLInstances.put(ci.name(), ci);
                }
            }
        }
        try {
            FileReader fr = new FileReader(filename);
            Log.debug("importCsv: file read");
            CSVReader reader;
            try {
                reader = new CSVReader(fr);
            } catch (NoClassDefFoundError e) {
                Log.trace("importCsv: error - Exception" + e.toString());
                return;
            }
            Log.debug("importCsv: file parsed");
            String[] nextLine;

            // read header
            reader.readNext();

            Log.debug("importCsv: header read");

            // read mappings
            int mapLength = NiemUmlModel.getNiemMap().length;
            while ((nextLine = reader.readNext()) != null) {
                String className = nextLine[0].trim();
                String attributeName = nextLine[1].trim();

                if (!className.isEmpty()) {
                    UmlClass type = UMLClasses.get(className);
                    if (type != null) {
                        if (attributeName.isEmpty()) {
                            // import NIEM mapping to class
                            Log.debug("importCsv: importing NIEM mapping for " + className);
                            for (int column = 5; column < mapLength
                                    && column < nextLine.length; column++)
                                type.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column]);
                        } else {
                            // import NIEM Mapping to attribute
                            for (UmlItem item : type.children())
                                if (NiemUmlModel.isNiemUml(item)&& (item.name().equals(attributeName)))
                                    for (int column = 5; column < mapLength
                                            && column < nextLine.length; column++)
                                        item.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column]);
                        }
                    }
                } else if (!attributeName.isEmpty()) {
                    UmlClassInstance element = UMLInstances.get(attributeName);
                    if (element != null) {
                        // import NIEM mapping to class
                        Log.debug("importCsv: importing NIEM mapping for " + attributeName);
                        for (int column = 5; column < mapLength && column < nextLine.length; column++)
                            element.set_PropertyValue(NiemUmlModel.getNiemProperty(column), nextLine[column]);
                    }
                }
            }
            reader.close();
        } catch (CsvValidationException e) {
            Log.trace("importCsv: error reading CSV " + e.toString());
        } catch (FileNotFoundException e) {
            Log.trace("importCsv: error - file not found" + e.toString());
        } catch (IOException e) {
            Log.trace("importCsv: error - IO exception" + e.toString());
        }
    }

}
