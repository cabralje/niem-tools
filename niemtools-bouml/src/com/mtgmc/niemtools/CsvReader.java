package com.mtgmc.niemtools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.opencsv.CSVReader;

import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;
import fr.bouml.anItemKind;

public class CsvReader {

	/**
	 * @param filename
	 */
	void importCsv(String filename) {
		// cache UML classes
		Map<String, UmlClass> UMLClasses = new HashMap<String, UmlClass>();
		Map<String, UmlClassInstance> UMLInstances = new HashMap<String, UmlClassInstance>();
		@SuppressWarnings("unchecked")
		Iterator<UmlItem> it = UmlItem.all.iterator();
		while (it.hasNext()) {
			UmlItem item = it.next();
			if (item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
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
		try {
			CSVReader reader = new CSVReader(new FileReader(filename));
			String[] nextLine;
	
			// read header
			reader.readNext();
			// read mappings
			while ((nextLine = reader.readNext()) != null) {
				String className = nextLine[0].trim();
				String attributeName = nextLine[1].trim();
	
				if (!className.equals("")) {
					UmlClass type = UMLClasses.get(className);
					if (type != null) {
						if (attributeName.equals("")) {
							// import NIEM mapping to class
							Log.debug("importCsv: importing NIEM mapping for " + className);
							for (int column = 4; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length
									&& column < nextLine.length; column++)
								type.set_PropertyValue(NiemUmlClass.getNiemProperty(column), nextLine[column]);
						} else {
							// import NIEM Mapping to attribute
							for (UmlItem item : type.children())
								if (item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE)
										&& (item.name().equals(attributeName)))
									for (int column = 4; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length
											&& column < nextLine.length; column++)
										item.set_PropertyValue(NiemUmlClass.getNiemProperty(column), nextLine[column]);
						}
					}
				} else if (!attributeName.equals("")) {
					UmlClassInstance element = UMLInstances.get(attributeName);
					if (element != null) {
						// import NIEM mapping to class
						Log.debug("importCsv: importing NIEM mapping for " + attributeName);
						for (int column = 4; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length && column < nextLine.length; column++)
							element.set_PropertyValue(NiemUmlClass.getNiemProperty(column), nextLine[column]);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Log.trace("importCsv: error - file not found" + e.toString());
		} catch (IOException e) {
			Log.trace("importCsv: error - IO exception" + e.toString());
		}
	}

}
