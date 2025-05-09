package org.cabral.niemtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.bouml.UmlBasePackage;
import fr.bouml.UmlClass;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

// the program is called with the socket port number in argument
public class NiemtoolsBouml
{

	public static void main(String argv[])
	{
		Log.start("main");
		try
		{
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName()
					//UIManager.getCrossPlatformLookAndFeelClassName()
					);
		}
		catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e)
		{
			Log.trace("Exception: " + e.toString());
		}

		// check for BOUML port from test harness
		int boumlPort = 0;
		try {
			File file = new File(TestHarness.filename);
			String buffer = new String(Files.readAllBytes(file.toPath()));
			boumlPort = Integer.parseInt(buffer);
			Log.debug("Port: " + boumlPort + "\n");
			
			// delete file - BOUML ports are one-time use
			file.delete();
		}
		catch (IOException | NumberFormatException e) 
		{
			// get BOUML port from command line 
			if (argv.length >= 1)
				boumlPort = Integer.parseInt(argv[argv.length - 1]);
		}
		
		System.out.println("Connecting to BOUML on port " + boumlPort);

		if (boumlPort != 0) {
			

			UmlCom.connect(boumlPort);
			Log.debug("Port: " + boumlPort + "\n");
			Log.debug("Classpath: " + System.getProperty("java.class.path") + "\n");
			
			try
			{	
				//UmlCom.trace("<b>BOUML NIEM tools</b> release 0.1<br />");
				UmlPackage root = UmlBasePackage.getProject();
				String propFile = System.getProperty("user.home") + "/" + root.name() + ".properties";

				Log.start("memo_ref");
				UmlCom.message("Memorize references ...");
				UmlItem target = UmlCom.targetItem();
				target.memo_ref();
				Log.stop("memo_ref");
				
				// create PIM and PSM
				NiemUmlClass niemTools = new NiemUmlClass();

				//load properties
				String command = argv[0];
				Properties properties = new Properties();
				FileReader in;
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
					ConfigurationDialog configDialog = new ConfigurationDialog(root, properties);
					configDialog.setVisible(true); // Ensure the dialog is displayed
					break;

				case "importSchema":
					//niemTools.cacheModels();
					importSchema(niemTools, properties);
					break;

				case "import":
					niemTools.cacheModels(false);
					importMapping(root, niemTools);
					break;

				case "sort":
					Log.trace("<b>Sort</b> release 5.0<br>");
					niemTools.cacheModels(false);
					UmlCom.targetItem().sort();
					break;

				case "addStereotype":
					Log.trace("<b>Add Stereotype<b>");
					niemTools.cacheModels(false);
					niemTools.addStereotype(UmlCom.targetItem());
					Log.trace("<b>Add Stereotype complete<b>");
					break;
					
				case "removeStereotype":
					Log.trace("<b>Remove Stereotype<b>");
					niemTools.cacheModels(false);
					niemTools.removeStereotype(UmlCom.targetItem());
					break;
					
				case "export":
				default:
					if (!niemTools.verifyNIEM())
						break;
					niemTools.cacheModels(false);
					generateModels(root, target, niemTools, properties);
				}
				// store properties
				try {
					Log.trace("Storing properties to " + propFile);
                                    try (FileWriter out = new FileWriter(propFile)) {
                                        properties.setProperty("htmlDir", root.propertyValue("html dir"));
                                        properties.store(out, "BOUML NiemTools plugout settings");
                                    }
				}
				catch (IOException e)
				{
					Log.trace("Unable to write properties to " + propFile);
				}
			} catch (java.net.SocketException e)
			{
				Log.trace("SocketException: " + e.getMessage());
			}
			catch (java.lang.NullPointerException e)
			{
				Log.trace("NullPointerException: " + e.getMessage());
			}
			catch (java.lang.ArrayIndexOutOfBoundsException e)
			{
				Log.trace("ArrayIndexOutOfBoundsException: " + e.getMessage());
			}
			catch (java.lang.ClassCastException e)
			{
				Log.trace("ClassCastException: " + e.getMessage());
			}
			catch (IOException e)
			{
				Log.trace("IOException: " + e.getMessage());
			}
			catch (RuntimeException re)
			{
				Log.trace("RuntimeException: " + re.getMessage());
			}
			catch (Exception e)
			{
				Log.trace("Exception: " + e.getMessage());
			}
			finally {
				Log.trace("Done");
				UmlCom.message("");
				Log.stop("main");
				// must be called to cleanly inform that all is done
				UmlCom.bye(0);
				UmlCom.close();
			}
		}
		System.exit(0);
	}

	/**
	 * @param root
	 * @param target
	 * @param niemTools
	 * @param properties
	 * @throws IOException
	 */
	private static void generateModels(UmlPackage root, UmlItem target, NiemUmlClass niemTools, Properties properties)
			throws IOException {
		Log.start("generateModels");
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
			Log.start("generate_indexes");
			UmlItem.generate_indexes();
			Log.stop("generate_indexes");
			UmlItem.start_file("index", target.name() + "\nDocumentation", false);
			target.html(null, 0, 0);
			UmlItem.end_file();
			UmlItem.start_file("navig", null, true);
			UmlItem.end_file();
			Log.start("generate");
			UmlClass.generate();
			Log.stop("generate");
		}

		// Generate NIEM Mapping HTML
		niemTools.exportHtml(root.propertyValue("html dir"), "niem-mapping");

		// Generate NIEM Mapping CSV
		niemTools.exportCsv(root.propertyValue("html dir"), "niem-mapping.csv"); 
		
		// Clearing NIEM Models
		niemTools.deleteNIEM(false);
		niemTools.createNIEM();
		niemTools.cacheModels(false);

		// Generating NIEM Models
		niemTools.createSubsetAndExtension();

		// Generate NIEM Wantlist instance
		niemTools.exportWantlist(root.propertyValue("html dir"), "wantlist.xml");

		// Generate extension schema
		niemTools.cacheModels(false); // cache substitutions
		String xsdDir = (root.propertyValue("exportXML").equals("true")) ? properties.getProperty("xsdDir") : null;
		String wsdlDir = (root.propertyValue("exportWSDL").equals("true")) ? properties.getProperty("wsdlDir") : null;					
		String jsonDir = (root.propertyValue("exportJSON").equals("true")) ? properties.getProperty("jsonDir") : null;
		String openapiDir = (root.propertyValue("exportOpenAPI").equals("true")) ? properties.getProperty("openapiDir") : null;
		String xmlExampleDir = (root.propertyValue("exportXML").equals("true")) ? properties.getProperty("xmlExampleDir") : null;
		String jsonExampleDir = (root.propertyValue("exportJSON").equals("true")) ? properties.getProperty("jsonExampleDir") : null;
		String cmfDir = (root.propertyValue("exportMetamodel").equals("true")) ? properties.getProperty("metamodelDir") : null;
		String cmfVersion = "0.8";
		
		niemTools.exportSpecification(xsdDir, wsdlDir, jsonDir, openapiDir, xmlExampleDir, jsonExampleDir, cmfDir, cmfVersion);
		Log.stop("generateModels");
	}

	/**
	 * @param root
	 * @param niemTools
	 */
	private static void importMapping(UmlPackage root, NiemUmlClass niemTools) {
		Log.start("importMapping");
		niemTools.deleteMapping();
		JFileChooser fc2 = new JFileChooser(root.propertyValue("html dir"));
		fc2.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
		fc2.setDialogTitle("NIEM Mapping CSV file");
		if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
			return;
		String filename = fc2.getSelectedFile().getAbsolutePath();
		niemTools.importCsv(filename);
		Log.stop("importMapping");
	}

	/**
	 * @param niemTools
	 * @param properties
	 * @throws IOException
	 */
	private static void importSchema(NiemUmlClass niemTools, Properties properties) throws IOException {
		Log.start("importSchema");
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
		niemTools.cacheModels(true);
		niemTools.importSchemaDir(directory,false);
		Log.stop("importSchema");
	}
}
