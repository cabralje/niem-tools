import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

// the program is called with the socket port number in argument

class Main
{
	private static class ModelPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		String directory = null;
		
		ModelPanel(String modelName, String initialDirectory) {
			
			directory = initialDirectory;
			
			// add field panels
			//JPanel panel1 = new JPanel();
			setLayout(new FlowLayout());
			JPanel panel2 = new JPanel();
			panel2.setLayout(new FlowLayout());
			
			// add field label
			JLabel label1 = new JLabel("Directory");
			panel2.add(label1);
			
			// add text field
			JTextField textField1 = new JTextField();
			textField1.setText(directory);
			textField1.setToolTipText("Select the directory for HTML models");   
			panel2.add(textField1);
			
			// add field button
			JButton button1 = new JButton("Browse...");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					JFileChooser fc = new JFileChooser(textField1.getText());
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setDialogTitle(modelName + " directory");
					if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
						return;
					textField1.setText(fc.getSelectedFile().getAbsolutePath());
			    }  
			});     
			panel2.add(button1);
			
			// add checkbox
			JCheckBox checkBox1 = new JCheckBox(modelName, true);
			setVisible(checkBox1.isSelected());
			checkBox1.addItemListener(new ItemListener() {    
			     public void itemStateChanged(ItemEvent e) {
			       	panel2.setVisible(checkBox1.isSelected());
			     }    
			  });    
			add(checkBox1);
			add(panel2);
		}
	}

	public static void main(String argv[])
	{
		Boolean genHtml = true;
				
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
				String htmlDir = NiemTools.getProperty("html dir");
				//String htmlDir = properties.getProperty("htmlDir", homeDir);
				root.set_PropertyValue("html dir", htmlDir);
				String xsdDir = properties.getProperty("xsdDir");
				String jsonDir = properties.getProperty("jsonDir");
				String niemDir = properties.getProperty("niemDir");
				String wsdlDir = properties.getProperty("wsdlDir");
				String openapiDir = properties.getProperty("openapiDir");
				
				String command = argv[0];
				if (xsdDir == null || jsonDir == null || wsdlDir == null || openapiDir == null)
					command = "configure";
				
				// int argc = argv.length-1;
				switch (command)
				{
				case "configure":
					// create dialog
					JDialog dialog = new JDialog(new JFrame(), "Niem-tools Configuration", true);
					dialog.setSize(1000, 600);

					// add frame label
					JLabel frameLabel = new JLabel("Select models and directories to generate");
					frameLabel.setHorizontalAlignment(SwingConstants.CENTER);
					dialog.add(frameLabel, BorderLayout.NORTH);
					
					// add center content pane
					JPanel panel0 = new JPanel();
					panel0.setLayout(new BoxLayout(panel0, BoxLayout.Y_AXIS));
					dialog.add(panel0, BorderLayout.CENTER);
					
					// add model panels
					//ModelPanel niemPanel = new ModelPanel(niemDir);
					ModelPanel htmlPanel = new ModelPanel("HTML", htmlDir);
					ModelPanel xsdPanel = new ModelPanel("XML", xsdDir);
					ModelPanel wsdlPanel = new ModelPanel("WSDL", wsdlDir);
					ModelPanel jsonPanel = new ModelPanel("JSON", jsonDir);
					ModelPanel openapiPanel = new ModelPanel("OpenAPI", openapiDir);
					panel0.add(htmlPanel);
					panel0.add(xsdPanel);
					panel0.add(wsdlPanel);
					panel0.add(jsonPanel);
					panel0.add(openapiPanel);

					// add frame button
					JButton frameButton = new JButton("OK");
					frameButton.setHorizontalAlignment(SwingConstants.CENTER);
					frameButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e){
							dialog.setVisible(false);
				        }  
				    });     
					dialog.add(frameButton, BorderLayout.SOUTH);
					
					// show frame
					dialog.setVisible(true);
					
					// get values
					htmlDir = htmlPanel.directory;
					properties.setProperty("html dir", htmlDir);
					xsdDir = xsdPanel.directory;
					properties.setProperty("xsdDir", xsdDir);
					wsdlDir = wsdlPanel.directory;
					properties.setProperty("wsdlDir", wsdlDir);
					jsonDir = jsonPanel.directory;
					properties.setProperty("jsonDir", jsonDir);
					openapiDir = openapiPanel.directory;
					properties.setProperty("openapiDir", openapiDir);

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
					if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
						return;
					String directory = fc.getSelectedFile().getAbsolutePath();
					properties.setProperty("niemDir", directory);
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
					niemTools.cacheModels(); // cache substitutions
					niemTools.exportIEPD(xsdDir, wsdlDir, jsonDir, openapiDir);

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
