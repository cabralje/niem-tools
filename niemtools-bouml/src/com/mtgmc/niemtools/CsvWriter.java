package com.mtgmc.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Iterator;

import com.opencsv.CSVWriter;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlItem;
import fr.bouml.UmlRelation;
import fr.bouml.anItemKind;

public class CsvWriter {

	/** returns a line of the NIEM mapping spreadsheet in CSV format */
	String[] getItemCsv(UmlItem item) {
		String[] nextLine = new String[NiemUmlClass.NIEM_STEREOTYPE_MAP.length];
		try {
			// Export Class and Property
			switch (item.kind().value()) {
			case anItemKind._aClass:
				nextLine[0] = item.name();
				nextLine[1] = "";
				nextLine[2] = "";
				break;
			case anItemKind._anAttribute:
				nextLine[0] = item.parent().name();
				nextLine[1] = item.name();
				UmlAttribute a = (UmlAttribute) item;
				nextLine[2] = a.multiplicity();
				break;
			case anItemKind._aRelation:
				nextLine[0] = item.parent().name();
				UmlRelation r = (UmlRelation) item;
				nextLine[1] = r.name();
				nextLine[2] = r.multiplicity();
				break;
			case anItemKind._aClassInstance:
				nextLine[0] = "";
				nextLine[1] = item.name();
				nextLine[2] = "";
				break;
			default:
				nextLine[0] = item.parent().name();
				nextLine[1] = item.name();
				nextLine[2] = "";
				break;
			}
		} catch (Exception e) {
			Log.trace("itemCsv: error importing class, property multiplicity " + e.toString());
		}

		// Export Description
		nextLine[3] = item.description();

		// Export NIEM Mapping
		if (item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
			for (int column = 4; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length; column++)
				nextLine[column] = item.propertyValue(NiemUmlClass.getNiemProperty(column));

		return nextLine;
	}

	/**export CSV file */
	void exportCsv(String directory, String filename) {
		File file = Paths.get(directory, filename).toFile();
	
		try {
			File parentFile = file.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			FileWriter fw = new FileWriter(file);
			Log.debug("exportCsv: open CSV " + file.toString());
			CSVWriter writer = new CSVWriter(fw);
	
			// Write header
			String[] nextLine = new String[NiemUmlClass.NIEM_STEREOTYPE_MAP.length];
			for (int column = 0; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length; column++)
				nextLine[column] = NiemUmlClass.NIEM_STEREOTYPE_MAP[column][0];
			writer.writeNext(nextLine);
	
			// Export NIEM Mappings for Classes
			@SuppressWarnings("unchecked")
			Iterator<UmlItem> it = (UmlClass.classes.iterator());
			while (it.hasNext()) {
				UmlItem thisClass = it.next();
				Log.debug("exportCsv: " + thisClass.name());
				if (!thisClass.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
					continue;
				nextLine = getItemCsv(thisClass);
				writer.writeNext(nextLine);
	
				// Export NIEM Mapping for Attributes and Relations
				for (UmlItem item : thisClass.children()) {
					if (!item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
						continue;
					nextLine = getItemCsv(item);
					if (nextLine != null)
						writer.writeNext(nextLine);
				}
			}
			writer.close();
			Log.debug("exportCsv: CSV file created " + file.toString());
	
		} catch (Exception e) {
			Log.trace("exportCsv: error " + e.toString());
		}
	}

}
