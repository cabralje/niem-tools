import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

// the program is called with the socket port number in argument

class Main
{
	public static void main(String argv[])
	{
		Boolean genHtml = false;
		
		JFrame frame = new JFrame();
		
		try
		{
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			//            UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e)
		{
			UmlCom.trace("Exception: " + e.toString());
		}

		// new SwingApplication(); //Create and show the GUI.

		if (argv.length >= 1)
		{
			UmlCom.connect(Integer.valueOf(argv[argv.length - 1]).intValue());

			try
			{
				// TODO user interface for setting/confirming project properties
				
				//UmlCom.trace("<b>BOUML NIEM tools</b> release 0.1<br />");
				JFileChooser fc;
				String homeDir = System.getProperty("user.home");
				UmlPackage root = UmlBasePackage.getProject();
				//String propFile = homeDir + "/niemtools.properties";		
				String propFile = homeDir + "/" + root.name() + ".properties";

				UmlItem target = UmlCom.targetItem();

				UmlCom.message("Memorize references ...");
				target.memo_ref();

				// create PIM and PSM
				//NiemTools.createPIM(root);
				NiemTools niemTools = new NiemTools();
				niemTools.cacheModels();

				//load properties
				Properties properties = new Properties();
				FileReader in = null;
				try {
					in = new FileReader(propFile);
					properties.load(in);
					in.close();
				}
				catch (FileNotFoundException e)
				{
					UmlCom.trace("Properties file " + propFile + " does not exist.");
				}
				// get local locations
				String htmlDir = properties.getProperty("htmlDir", homeDir);
				root.set_PropertyValue("html dir", htmlDir);
				String xsdDir = properties.getProperty("xsdDir");
				String jsonDir = properties.getProperty("jsonDir");
				String niemDir = properties.getProperty("niemDir");
				String wsdlDir = properties.getProperty("wsdlDir");
				String openapiDir = properties.getProperty("openapiDir");
				
				// int argc = argv.length-1;
				switch (argv[0])
				{
				case "importSchema":
					// Create PIM
					//NiemTools.createPIM(root);
					
					// Import schema
					UmlCom.message("Importing NIEM schema");
					// in java it is very complicated to select
					// a directory through a dialog, and the dialog
					// is very slow and ugly
					fc = new JFileChooser(niemDir);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setDialogTitle("Directory of the schema to be imported");
					if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
						return;
					String directory = fc.getSelectedFile().getAbsolutePath();
					properties.setProperty("niemDir", directory);
					//NiemTools.deletePIM(root);
					//UmlCom.trace("Saving project");
					//UmlBasePackage.saveProject();
					//UmlBasePackage.loadProject("");
					niemTools.deleteNIEM(true);
					niemTools.createNIEM();
					niemTools.cacheModels();
					niemTools.importSchemaDir(directory,false);
					break;

				case "import":
					UmlCom.trace("Deleting NIEM Mapping");
					niemTools.deleteMapping();
					UmlCom.trace("Importing NIEM Mapping");
					JFileChooser fc2 = new JFileChooser(htmlDir);
					fc2.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
					fc2.setDialogTitle("NIEM Mapping CSV file");
					if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
						return;
					String filename = fc2.getSelectedFile().getAbsolutePath();
					niemTools.importCsv(filename);
					break;

				case "sort":
			        UmlCom.trace("<b>Sort</b> release 5.0<br>");
			        UmlCom.targetItem().sort();
					break;
					
				default:
					//NiemTools.deleteSubset(root);
					//UmlCom.trace("Saving project");
					//UmlBasePackage.saveProject();
					//UmlBasePackage.loadProject("");
					if (!niemTools.verifyNIEM())
						break;

					// Generate UML Model HTML documentation
					 if (genHtml)
					{
						UmlCom.trace("Generating HTML documentation");
						//	target.set_dir(argv.length - 1, argv);
						String[] params = {htmlDir};
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
					UmlCom.message ("Generating NIEM Mapping HTML ...");
					UmlCom.trace("Generating NIEM Mapping HTML");
					niemTools.exportHtml(htmlDir, "niem-mapping");

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					UmlCom.trace("Generating NIEM Mapping CSV");
					niemTools.exportCsv(htmlDir, "niem-mapping.csv"); 

					// Clearing NIEM Models
					UmlCom.message("Resetting NIEM models");
					UmlCom.trace("Resetting NIEM models");
					niemTools.deleteNIEM(false);
					niemTools.createNIEM();
					niemTools.cacheModels();
					
					// Generating NIEM Models
					UmlCom.message("Generating NIEM subset and extension models");
					UmlCom.trace("Generating NIEM subset and extension models");
					niemTools.createSubsetAndExtension();

					
					// Generate NIEM Wantlist instance
					UmlCom.message("Generating NIEM Wantlist ...");
					UmlCom.trace("Generating NIEM Wantlist");
					niemTools.exportWantlist(htmlDir, "wantlist.xml");

					// Generate extension schema
					UmlCom.message("Generating extension schema ...");
					UmlCom.trace("Generating extension schema");
					if (xsdDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the XML schema to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						xsdDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("xsdDir", xsdDir);
					}
					if (wsdlDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the WSDL to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						wsdlDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("wsdlDir", wsdlDir);
					}
					if (jsonDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the JSON to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						jsonDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("jsonDir", jsonDir);
					}
					if (openapiDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the OpenAPI to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						openapiDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("openapiDir", openapiDir);
					}
					niemTools.cacheModels(); // cache substitutions
					niemTools.exportIEPD(xsdDir, wsdlDir, jsonDir, openapiDir);
					
					// output UML objects
					//NiemTools.outputUML();

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
					UmlCom.trace("Unable to write properties to " + propFile);
				}
				UmlCom.trace("Done");
				UmlCom.message("");
			}
			catch (IOException e)
			{
				UmlCom.trace("IOException: " + e.getMessage());
				UmlCom.bye(0);
				UmlCom.close();
				return;
			}
			catch (RuntimeException re)
			{
				UmlCom.trace("RuntimeException: " + re.getMessage());
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
}
