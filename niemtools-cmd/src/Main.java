/**
 * 
 */
import java.io.*;
// import javax.swing.UIManager;
// import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Jim
 *
 */
public class Main 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

		// Create PIM
		UmlCom.trace("Generating PIM");
		UmlPackage root = new UmlPackage();
		root.set_PropertyValue("niem dir", "C:/Users/JamesECabral/OneDrive/xml/niem-3.2/niem");
		NiemTools.createPIM(root);

		try {
			// Import schemas
			UmlCom.trace("Importing NIEM schema");
			NiemTools.importSchemaDir(root,false);

			// Generate indexes
			UmlCom.trace("Memorizing references");
			root.memo_ref();
			UmlItem.generate_indexes();

			// Generate NIEM Mapping HTML
			UmlCom.trace("Generating NIEM Mapping");
			NiemTools.exportHtml();

			// Generate NIEM Mapping CSV
			UmlCom.trace("Generating NIEM Mapping CSV");
			NiemTools.exportCsv();

			// Generate NIEM Wantlist instance
			UmlCom.trace("Generating NIEM Wantlist");
			NiemTools.exportWantlist();

			// Generate extension schema
	//		UmlCom.trace("Generating extension schema");
	//		NiemTools.exportSchema();
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
		NiemTools.outputUML(); 

		UmlCom.trace("Done");

	}
}
