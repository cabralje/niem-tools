package com.mtgmc.niemtools;

/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2017 James E. Cabral Jr., MTG Management Consultants LLC, jcabral@mtgmc.com, http://github.com/cabralje
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.bouml.UmlBasePackage;
import fr.bouml.UmlClass;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

// the program is called with the socket port number in argument
class Main
{

	public static void main(String argv[])
	{
		try
		{
			// Set System L&F
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName()
				//UIManager.getCrossPlatformLookAndFeelClassName()
			);
		}
		catch (Exception e)
		{
			Log.trace("Exception: " + e.toString());
		}

		if (argv.length >= 1)
		{
			int boumlPort = Integer.valueOf(argv[argv.length - 1]).intValue();
			UmlCom.connect(boumlPort);

			try
			{	
				//UmlCom.trace("<b>BOUML NIEM tools</b> release 0.1<br />");
				UmlPackage root = UmlBasePackage.getProject();
				String propFile = System.getProperty("user.home") + "/" + root.name() + ".properties";
				
				UmlCom.message("Memorize references ...");
				UmlItem target = UmlCom.targetItem();
				target.memo_ref();

				// create PIM and PSM
				NiemUmlClass niemTools = new NiemUmlClass();
				niemTools.cacheModels();

				//load properties
				String command = argv[0];
				Properties properties = new Properties();
				FileReader in = null;
				try {
					in = new FileReader(propFile);
					properties.load(in);
					in.close();
				}
				catch (FileNotFoundException e) 
				{
					Log.trace("Properties file " + propFile + " does not exist.");
					command = "configure";
				}
				
				switch (command)
				{
				case "configure":
					new ConfigurationDialog(root, properties);
					break;
					
				case "importSchema":
					importSchema(niemTools, properties);
					break;

				case "import":
					importMapping(root, niemTools);
					break;

				case "sort":
			        Log.trace("<b>Sort</b> release 5.0<br>");
			        UmlCom.targetItem().sort();
					break;
				
				case "export":
				default:
					if (!niemTools.verifyNIEM())
						break;

					generateModels(root, target, niemTools, properties);
					break;
				}
				// store properties
				try {
					FileWriter out = new FileWriter(propFile);
					properties.setProperty("htmlDir", root.propertyValue("html dir"));
					properties.store(out, "BOUML NiemTools plugout settings");
					out.close();
				}
				catch (IOException e)
				{
					Log.trace("Unable to write properties to " + propFile);
				}
				Log.trace("Done");
				UmlCom.message("");
			}
			catch (IOException e)
			{
				Log.trace("IOException: " + e.getMessage());
				UmlCom.bye(0);
				UmlCom.close();
				return;
			}
			catch (RuntimeException re)
			{
				Log.trace("RuntimeException: " + re.getMessage());
				UmlCom.bye(0);
				UmlCom.close();
				return;
			}
			finally {
				// must be called to cleanly inform that all is done
				UmlCom.bye(0);
				UmlCom.close();
			}
		}
		System.exit(0);
	}

	private static void generateModels(UmlPackage root, UmlItem target, NiemUmlClass niemTools, Properties properties)
			throws IOException {
		// Generate UML Model HTML documentation
		if (root.propertyValue("exportHTML").equals("true"))
		{
			Log.trace("Generating HTML documentation");
			//	target.set_dir(argv.length - 1, argv);
			String[] params = {root.propertyValue("html dir")};
			target.set_dir(1, params);
			//target.set_dir(0,null);
			UmlItem.frame();
			UmlCom.message("Indexes ...");
			UmlItem.generate_indexes();
			UmlItem.start_file("index", target.name() + "\nDocumentation", false);
			target.html(null, 0, 0);
			UmlItem.end_file();
			UmlItem.start_file("navig", null, true);
			UmlItem.end_file();
			UmlClass.generate(); 
		}
		
		// Generate NIEM Mapping HTML
		niemTools.exportHtml(root.propertyValue("html dir"), "niem-mapping");

		// Generate NIEM Mapping CSV
		niemTools.exportCsv(root.propertyValue("html dir"), "niem-mapping.csv"); 

		// Clearing NIEM Models
		niemTools.deleteNIEM(false);
		niemTools.createNIEM();
		niemTools.cacheModels();
		
		// Generating NIEM Models
		niemTools.createSubsetAndExtension();

		// Generate NIEM Wantlist instance
		niemTools.exportWantlist(root.propertyValue("html dir"), "wantlist.xml");

		// Generate extension schema
		niemTools.cacheModels(); // cache substitutions
		String xsdDir = (root.propertyValue("exportXML").equals("true")) ? properties.getProperty("xsdDir") : null;
		String wsdlDir = (root.propertyValue("exportWSDL").equals("true")) ? properties.getProperty("wsdlDir") : null;					
		String jsonDir = (root.propertyValue("exportJSON").equals("true")) ? properties.getProperty("jsonDir") : null;
		String openapiDir = (root.propertyValue("exportOpenAPI").equals("true")) ? properties.getProperty("openapiDir") : null;
		niemTools.exportIEPD(xsdDir, wsdlDir, jsonDir, openapiDir);
	}

	private static void importMapping(UmlPackage root, NiemUmlClass niemTools) {
		niemTools.deleteMapping();
		JFileChooser fc2 = new JFileChooser(root.propertyValue("html dir"));
		fc2.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
		fc2.setDialogTitle("NIEM Mapping CSV file");
		if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
			return;
		String filename = fc2.getSelectedFile().getAbsolutePath();
		niemTools.importCsv(filename);
	}

	private static void importSchema(NiemUmlClass niemTools, Properties properties) throws IOException {
		JFileChooser fc;
		// Create PIM
		//NiemTools.createPIM(root);
		
		// Import schema
		// in java it is very complicated to select
		// a directory through a dialog, and the dialog
		// is very slow and ugly
		fc = new JFileChooser(properties.getProperty("niemDir"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Directory of the schema to be imported");
		if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
			return;
		String directory = fc.getSelectedFile().getAbsolutePath();
		properties.setProperty("niemDir", directory);
		niemTools.deleteNIEM(true);
		niemTools.createNIEM();
		niemTools.cacheModels();
		niemTools.importSchemaDir(directory,false);
	}
}