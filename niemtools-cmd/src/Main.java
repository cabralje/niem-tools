/**
 * 
 */
import java.io.*;
// import javax.swing.UIManager;
// import javax.swing.UnsupportedLookAndFeelException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Jim
 *
 */
public class Main 
{

	String directory;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		JFrame frame = new JFrame();
		UmlPackage root = new UmlPackage();

		// setup directories
		root.set_PropertyValue("niem dir", "C:/Users/JamesECabral/OneDrive/xml/niem-3.2/niem");
		root.set_PropertyValue("html dir", "C:/Users/JamesECabral/Desktop/html");
		
		try {
			// Create PIM
			UmlCom.trace("Generating PIM");
			NiemTools.createPIM(root);
			
			// Import schemas
			UmlCom.trace("Importing NIEM schema");
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

			// Import NIEM Mapping CSV file
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
			
			// Generate indexes
			//UmlCom.trace("Memorizing references");
			//root.memo_ref();
			//UmlItem.generate_indexes();
			
			// Generate NIEM Mapping HTML
			UmlCom.trace("Generating NIEM Mapping");
			NiemTools.exportHtml(root.propertyValue("html dir"), "niem-mapping.html");

			// Generate NIEM Mapping CSV
			UmlCom.trace("Generating NIEM Mapping CSV");
			NiemTools.exportCsv(root.propertyValue("html dir"), "niem-mapping.csv");

			// Generate NIEM Wantlist instance
			UmlCom.trace("Generating NIEM Wantlist");
			NiemTools.exportWantlist(root.propertyValue("html dir"), "wantlist");

			// Generate extension schema
			UmlCom.trace("Generating extension schema");
			NiemTools.exportSchema(root.propertyValue("html dir"));
		}
		catch (IOException e)
		{
			UmlCom.trace("IOException: " + e.getMessage());
			return;
		}
		catch (NullPointerException re)
		{
			UmlCom.trace("NullPointerException: " + re.getMessage());
			return;
		}
		catch (RuntimeException re)
		{
			UmlCom.trace("RuntimeException: " + re.getMessage());
			return;
		}

		// output UML objects
		// NiemTools.outputUML(); 

		UmlCom.trace("Done");

	}
}
