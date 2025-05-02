package org.cabral.niemtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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

	/** returns a line of the NIEM mapping spreadsheet in CSV format */
	/**
	 * @param item
	 * @return
	 */
	String[] getItemCsv(UmlItem item) {
		String[] nextLine = new String[NiemUmlClass.NIEM_STEREOTYPE_MAP.length];
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
		if (item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
			for (int column = 5; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length; column++)
				nextLine[column] = item.propertyValue(NiemUmlClass.getNiemProperty(column));

		return nextLine;
	}
	
	/**export CSV file */
	/**
	 * @param directory
	 * @param filename
	 */
	void exportCsv(String directory, String filename) {
		File file = Paths.get(directory, filename).toFile();

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
				String[] nextLine = new String[NiemUmlClass.NIEM_STEREOTYPE_MAP.length];
				for (int column = 0; column < NiemUmlClass.NIEM_STEREOTYPE_MAP.length; column++)
					nextLine[column] = NiemUmlClass.NIEM_STEREOTYPE_MAP[column][0];
				try {
					writer.writeNext(nextLine);
				} catch (Exception e) {
					Log.trace("exportCsv: writing error" + e.toString());
				}
				
				// Export NIEM Mappings for Classes
				@SuppressWarnings("unchecked")
				Iterator<UmlItem> it = (UmlClass.classes.iterator());
				while (it.hasNext()) {
					UmlItem thisClass = it.next();
					Log.debug("exportCsv: " + thisClass.name());
					if (!thisClass.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
						continue;
					nextLine = getItemCsv(thisClass);
					Log.debug("exportCsv: write line");
					writer.writeNext(nextLine);
		
					// Export NIEM Mapping for Attributes and Relations
					for (UmlItem item : thisClass.children()) {
						if (!item.stereotype().equals(NiemUmlClass.NIEM_STEREOTYPE))
							continue;
						nextLine = getItemCsv(item);
						Log.debug("exportCsv: write line");
						if (nextLine != null)
							writer.writeNext(nextLine);
					}
				}
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
