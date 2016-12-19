import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
		catch (UnsupportedLookAndFeelException e)
		{
			// handle exception
		}
		catch (ClassNotFoundException e)
		{
			// handle exception
		}
		catch (InstantiationException e)
		{
			// handle exception
		}
		catch (IllegalAccessException e)
		{
			// handle exception
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
				String propFile = homeDir + "/niemtools.properties";
				Boolean genHtml = false;
				UmlItem target = UmlCom.targetItem();

				UmlCom.message("Memorize references ...");
				target.memo_ref();

				// create PIM and PSM
				UmlPackage root = UmlBasePackage.getProject();
				NiemTools.createPIM(root);
				//NiemTools.createPSM(root);
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
				String htmlDir = properties.getProperty("htmlDir", homeDir);
				root.set_PropertyValue("html dir", htmlDir);
				String xsdDir = properties.getProperty("xsdDir");
				String niemDir = properties.getProperty("niemDir", homeDir);
				
				// int argc = argv.length-1;
				switch (argv[0])
				{
				case "exportSchema":
					// Import extension and exchange schema
					UmlCom.message("Exporting extension and exchange schema ...");
					UmlCom.trace("Exporting extension and exchange schema");
					if (xsdDir == null)
					{
						fc = new JFileChooser(homeDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the schema to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						xsdDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("xsdDir", xsdDir);
					}
					NiemTools.exportSchema(xsdDir);
					break;

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
					NiemTools.importSchemaDir(directory,false);
					break;

				case "reset":
					// Globally reset NIEM Stereotype
					UmlCom.message("Resetting NIEM stereotype ...");
					//UmlCom.trace("Resetting NIEM stereotype");
					NiemTools.resetStereotype();
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
					NiemTools.importCsv(filename);
					break;

				case "sort":
			        UmlCom.trace("<b>Sort</b> release 5.0<br>");
			        UmlCom.targetItem().sort();
					break;
					
				default:

					// Generate UML Model HTML documentation
					if (genHtml)
					{
						//	target.set_dir(argv.length - 1, argv);
						target.set_dir(0,null);
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
					UmlCom.message ("Generating NIEM Mapping ...");
					UmlCom.trace("Generating NIEM Mapping");
					NiemTools.exportHtml(htmlDir, "niem-mapping");

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					UmlCom.trace("Generating NIEM Mapping CSV");
					NiemTools.exportCsv(htmlDir, "niem-mapping.csv");

					// Generate NIEM Wantlist instance
					UmlCom.message("Generating NIEM Wantlist ...");
					UmlCom.trace("Generating NIEM Wantlist");
					NiemTools.exportWantlist(htmlDir, "wantlist");

					// Generate extension schema
					UmlCom.message("Generating extension schema ...");
					UmlCom.trace("Generating extension schema");
					if (xsdDir == null)
					{
						fc = new JFileChooser(htmlDir);
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						fc.setDialogTitle("Directory of the schema to be exported");
						if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
							return;
						xsdDir = fc.getSelectedFile().getAbsolutePath();
						properties.setProperty("xsdDir", xsdDir);
					}
					NiemTools.exportSchema(xsdDir);

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
