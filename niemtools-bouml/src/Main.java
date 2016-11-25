import java.io.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

// the program is called with the socket port number in argument

class Main
{
	public static void main(String argv[])
	{

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

				// int argc = argv.length-1;
				switch (argv[0])
				{
				case "exportSchema":
					// Import extension and exchange schema
					UmlCom.message("Exporting extension and exchange schema ...");
					//UmlCom.trace("Exporting extension and exchange schema");
					NiemTools.exportSchema();
					break;

				case "importSchema":
					// Import NIEM schema
					UmlCom.message("Importing NIEM schema ...");
					//UmlCom.trace("Importing NIEM schema");
					NiemTools.importSchemaDir(root, false);
					break;

				case "reset":
					// Globally reset NIEM Stereotype
					UmlCom.message("Resetting NIEM stereotype ...");
					//UmlCom.trace("Resetting NIEM stereotype");
					NiemTools.resetStereotype();
					break;

				case "import":
					// Import NIEM Mapping CSV file
					UmlCom.message("Deleting NIEM Mapping ...");
					//UmlCom.trace("Deleting NIEM Mapping");
					NiemTools.deleteMapping();
					UmlCom.message("Importing NIEM Mapping ...");
					//UmlCom.trace("Importing NIEM Mapping");
					NiemTools.importCsv();
					break;

				default:

					// Create PIM
					NiemTools.createPIM(root);

				// Import NIEM schema
					UmlCom.message("Importing NIEM schema ...");
					//UmlCom.trace("Importing NIEM schema");
					NiemTools.importSchemaDir(root, false); 

					// Generate UML Model HTML documentation
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

					// Generate NIEM Mapping HTML
					UmlCom.message ("Generating NIEM Mapping ...");
					//UmlCom.trace("Generating NIEM Mapping");
					NiemTools.exportHtml();

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					//UmlCom.trace("Generating NIEM Mapping CSV");
					NiemTools.exportCsv();

					// Generate NIEM Wantlist instance
					UmlCom.message("Generating NIEM Wantlist ...");
					//UmlCom.trace("Generating NIEM Wantlist");
					NiemTools.exportWantlist();

/*					// Generate extension schema
					UmlCom.message("Generating extension schema ...");
					//UmlCom.trace("Generating extension schema");
					NiemTools.exportSchema();*/

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
