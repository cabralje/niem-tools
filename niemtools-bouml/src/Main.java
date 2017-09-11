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
				//UmlCom.trace("<b>BOUML NIEM tools</b> release 0.1<br />");
				JFileChooser fc;
				String homeDir = System.getProperty("user.home");
				UmlPackage root = UmlBasePackage.getProject();
				//String propFile = homeDir + "/niemtools.properties";		
				String propFile = homeDir + "/" + root.name() + ".properties";
				Boolean genHtml = true;
				UmlItem target = UmlCom.targetItem();

				UmlCom.message("Memorize references ...");
				target.memo_ref();

				// create PIM and PSM
				//NiemTools.createPIM(root);

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
				String niemDir = properties.getProperty("niemDir", homeDir);
				
				// get IEPD properties 
				String IEPDURI = null, IEPDName = null, IEPDVersion = null, IEPDStatus = null, IEPDOrganization = null, IEPDContact = null, externalSchemas = null;
				try {
					IEPDURI = root.propertyValue("IEPDURI");
					if (IEPDURI==null)
					{
						IEPDURI = "http://local";
						root.set_PropertyValue("IEPDURI", IEPDURI);
					}
					IEPDName = root.propertyValue("IEPDName");
					if (IEPDName==null)
					{
						IEPDName = "IEPD";
						root.set_PropertyValue("IEPDName", IEPDName);
					}
					IEPDVersion = root.propertyValue("IEPDVersion");
					if (IEPDVersion==null)
					{
						IEPDVersion = "1.0";
						root.set_PropertyValue("IEPDVersion", IEPDName);
					}
					IEPDStatus = root.propertyValue("IEPDStatus");
					if (IEPDStatus==null)
					{
						IEPDStatus = "Draft";
						root.set_PropertyValue("IEPDStatus", IEPDStatus);
					}
					IEPDOrganization = root.propertyValue("IEPDOrganization");
					if (IEPDOrganization==null)
					{
						IEPDOrganization = "Organization";
						root.set_PropertyValue("IEPDOrganization", IEPDOrganization);
					}
					IEPDContact = root.propertyValue("IEPDContact");
					if (IEPDContact==null)
					{
						IEPDName = "Contact";
						root.set_PropertyValue("IEPDContact", IEPDContact);
					}
					externalSchemas = root.propertyValue("externalSchemas");
					if (externalSchemas==null)
					{
						// format for external namespaces: prefix = schemaURI = URL of schema file
						externalSchemas = "cac=urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonAggregateComponents-2.1.xsd,"
										+ "cbc=urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonBasicComponents-2.1.xsd,"
										+ "ds=http://www.w3.org/2000/09/xmldsig#=https://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
						root.set_PropertyValue("externalSchemas", externalSchemas);
					}
					
				} catch (Exception e1) {
					UmlCom.trace("main: Exception " + e1.toString());
				}
				
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
					NiemTools.createNIEM(root);
					NiemTools.importSchemaDir(directory,false, externalSchemas);
					break;

				case "import":
					UmlCom.trace("Deleting NIEM Mapping");
					NiemTools.deleteMapping();
					UmlCom.trace("Importing NIEM Mapping");
					JFileChooser fc2 = new JFileChooser(htmlDir);
					fc2.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
					fc2.setDialogTitle("NIEM Mapping CSV file");
					if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
						return;
					String filename = fc2.getSelectedFile().getAbsolutePath();
					NiemTools.importCsv(filename, externalSchemas);
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
					if (!NiemTools.verifyNIEM(root))
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
					NiemTools.exportHtml(htmlDir, "niem-mapping", externalSchemas);

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					UmlCom.trace("Generating NIEM Mapping CSV");
					NiemTools.exportCsv(htmlDir, "niem-mapping.csv", externalSchemas);

					// Generate NIEM Wantlist instance
					UmlCom.message("Generating NIEM Wantlist ...");
					UmlCom.trace("Generating NIEM Wantlist");
					NiemTools.createNIEM(root);
					NiemTools.createSubsetAndExtension(IEPDURI);
					NiemTools.exportWantlist(htmlDir, "wantlist.xml", externalSchemas);

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
					if (jsonDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the JSON schema to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						jsonDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("jsonDir", jsonDir);
					}
					NiemTools.exportSchema(IEPDURI, IEPDName, IEPDVersion, IEPDStatus, IEPDOrganization, IEPDContact, externalSchemas, xsdDir, jsonDir);
					NiemTools.exportNiemJsonSchema(jsonDir);
					
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
