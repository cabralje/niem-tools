import java.io.IOException;

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

				UmlItem target = UmlCom.targetItem();

				UmlCom.message("Memorize references ...");
				target.memo_ref();

				UmlPackage root = UmlBasePackage.getProject();
				NiemTools.createPIM(root);
				
				// int argc = argv.length-1;
				switch (argv[0])
				{
				case "exportSchema":
					// Import extension and exchange schema
					UmlCom.message("Exporting extension and exchange schema ...");
					UmlCom.trace("Exporting extension and exchange schema");
					NiemTools.exportSchema(root.propertyValue("html dir"));
					break;

				case "importSchema":
					// Create PIM
					//NiemTools.createPIM(root);
					
					// Imort schema
					UmlCom.message("Importing NIEM schema");
					// in java it is very complicated to select
					// a directory through a dialog, and the dialog
					// is very slow and ugly
					JFileChooser fc = new JFileChooser(root.propertyValue("niem dir"));
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setDialogTitle("Directory of the schema to be imported");
					if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
						return;
					String directory = fc.getSelectedFile().getAbsolutePath();
					root.set_PropertyValue("niem dir",directory);
					NiemTools.importSchemaDir(directory,false);
					UmlCom.message("Done");
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
					JFileChooser fc2 = new JFileChooser(root.propertyValue("html dir"));
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
			        UmlCom.trace("Done<br>");
					break;
					
				default:

					// Generate UML Model HTML documentation
					//	target.set_dir(argv.length - 1, argv);
/*					target.set_dir(0,null);
					UmlItem.frame();
					UmlCom.message("Indexes ...");
					UmlItem.generate_indexes();
					UmlItem.start_file("index", target.name() + "\nDocumentation", false);
					target.html(null, 0, 0);
					UmlItem.end_file();
					UmlItem.start_file("navig", null, true);
					UmlItem.end_file();
					UmlClass.generate();  */
					
					// Generate NIEM Mapping HTML
					UmlCom.message ("Generating NIEM Mapping ...");
					UmlCom.trace("Generating NIEM Mapping");
					NiemTools.exportHtml(root.propertyValue("html dir"), "niem-mapping");

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					UmlCom.trace("Generating NIEM Mapping CSV");
					NiemTools.exportCsv(root.propertyValue("html dir"), "niem-mapping.csv");

					// Generate NIEM Wantlist instance
					UmlCom.message("Generating NIEM Wantlist ...");
					UmlCom.trace("Generating NIEM Wantlist");
					NiemTools.exportWantlist(root.propertyValue("html dir"), "wantlist");

					// Generate extension schema
					UmlCom.message("Generating extension schema ...");
					UmlCom.trace("Generating extension schema");
					NiemTools.exportSchema(root.propertyValue("html dir"));

					// output UML objects
					//NiemTools.outputUML();

					break;
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
