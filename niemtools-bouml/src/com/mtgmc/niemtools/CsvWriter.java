package com.mtgmc.niemtools;

import fr.bouml.UmlAttribute;
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

}
