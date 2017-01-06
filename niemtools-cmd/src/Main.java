/**
 * 
 */
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
// import javax.swing.UIManager;
// import javax.swing.UnsupportedLookAndFeelException;
import java.util.Properties;

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
		String homeDir = System.getProperty("user.home");
		String propFile = homeDir + "/niemtools.properties";
		//load properties
		Properties properties = new Properties();
		FileReader in = null;
		try {
			in = new FileReader(propFile);
			properties.load(in);
			in.close();
		}
		catch (Exception e)
		{
			UmlCom.trace("Properties file " + propFile + " does not exist.");
		}
		String htmlDir = properties.getProperty("htmlDir", homeDir);
		root.set_PropertyValue("html dir", htmlDir);
		String xsdDir = properties.getProperty("xsdDir");
		String niemDir = properties.getProperty("niemDir", homeDir);
		String extensionURI = properties.getProperty("extensionURI", "http://local/");
		String externalSchemas = "cac=urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonAggregateComponents-2.1.xsd,"
				+ "cbc=urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonBasicComponents-2.1.xsd,"
				+ "ds=http://www.w3.org/2000/09/xmldsig#=https://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
		
		try {
			// Create PIM
			UmlCom.trace("Generating PIM");
			NiemTools.createPIM(root);
			
			// Import schemas
			UmlCom.trace("Importing NIEM schema");
			// in java it is very complicated to select
			// a directory through a dialog, and the dialog
			// is very slow and ugly
			JFileChooser fc = new JFileChooser(niemDir);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Directory of the schema to be imported");
			if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
				return;
			String directory = fc.getSelectedFile().getAbsolutePath();
			properties.setProperty("niemDir", directory);
			NiemTools.importSchemaDir(directory,false,externalSchemas);

			// Import NIEM Mapping CSV file
			UmlCom.trace("Deleting NIEM Mapping");
			NiemTools.deleteMapping();
			UmlCom.trace("Importing NIEM Mapping");
			JFileChooser fc2 = new JFileChooser(htmlDir);
			fc2.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
			fc2.setDialogTitle("NIEM Mapping CSV file");
			if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
				return;
			String filename = fc2.getSelectedFile().getAbsolutePath();
			NiemTools.importCsv(filename,externalSchemas);
			
			// Generate indexes
			//UmlCom.trace("Memorizing references");
			//root.memo_ref();
			//UmlItem.generate_indexes();
			
			// Generate NIEM Mapping HTML
			UmlCom.trace("Generating NIEM Mapping");
			NiemTools.exportHtml(htmlDir, "niem-mapping.html",externalSchemas);

			// Generate NIEM Mapping CSV
			UmlCom.trace("Generating NIEM Mapping CSV");
			NiemTools.exportCsv(htmlDir, "niem-mapping.csv",externalSchemas);

			// Generate NIEM Wantlist instance
			UmlCom.trace("Generating NIEM Wantlist");
			NiemTools.createSubsetAndExtension(extensionURI);
			NiemTools.exportWantlist(htmlDir, "wantlist",externalSchemas);

			// Generate extension schema
			UmlCom.trace("Generating extension schema");
			NiemTools.exportSchema(xsdDir, "http://example.org/iepd", "IEPD", "1.0", "Draft", "Organization", "Contact", externalSchemas);
			
			// store properties
			try {
				FileWriter out = new FileWriter(propFile);
				properties.setProperty("htmlDir", root.propertyValue("html dir"));
				properties.setProperty("extensionURI", extensionURI);
				properties.store(out, "BOUML NiemTools plugout settings");
				out.close();
			}
			catch (IOException e)
			{
				UmlCom.trace("Unable to write properties to " + propFile);
			}
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
