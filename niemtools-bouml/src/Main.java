import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

// the program is called with the socket port number in argument
class Main
{

	private static class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
		
	    LineWrapCellRenderer() {
	        setLineWrap(true);
	        setWrapStyleWord(true);
			setFont(new Font(Font.DIALOG, Font.PLAIN,25));
	    }
	    
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (value != null)
				setText(value.toString());
	        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
	        if (table.getRowHeight(row) != getPreferredSize().height) {
	            table.setRowHeight(row, getPreferredSize().height);
	        }
			return this;
		}
	}
	
	private static class ToggleBox extends JCheckBox {
		
		private static final long serialVersionUID = 1L;
		
		ToggleBox(String name, String initialValue, JPanel panel) {
			super(name, (initialValue == null || !initialValue.equals("false")));
			panel.setVisible(this.isSelected());
			addItemListener(new ItemListener() {    
			     public void itemStateChanged(ItemEvent e) {
			       	panel.setVisible(((JCheckBox)(e.getItem())).isSelected());
			     }    
			  }); 
		}
	}
	
	private static class FilePanel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		String value = null;
		
		FilePanel(String name, String initialValue, int columns, int fileType) {
			
			value = initialValue;
			
			// add field label
			if (name != null)
				add(new JLabel(name));
			
			// add text field
			JTextField textField1 = new JTextField(columns);
			textField1.setText(value);
			textField1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					value = textField1.getText();
				}				
			});
			add(textField1);
			
			// add field button
			JButton button1 = new JButton("Browse...");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					JFileChooser fc = new JFileChooser(textField1.getText());
					fc.setFileSelectionMode(fileType);
					if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
						return;
					value = fc.getSelectedFile().getAbsolutePath();
					textField1.setText(value);
			    }  
			});
			add(button1);
		}
	}

	public static void main(String argv[])
	{
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
				String command = argv[0];
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
					command = "configure";
				}
				
				switch (command)
				{
				case "configure":
					// create dialog
					JDialog dialog = new JDialog(new JFrame(), "Niem-tools Configuration", true);
					dialog.setSize(1400, 700);
					int fieldColumns = 50;
					
					// add IEPD panel
					JPanel iepdPanel = new JPanel(new GridBagLayout());
					GridBagConstraints labelLayout = new GridBagConstraints();
					labelLayout.gridx = 0;
					labelLayout.ipady = 20;
					GridBagConstraints fieldLayout = new GridBagConstraints();
					fieldLayout.gridx = 1;
					fieldLayout.ipadx = 1000;
					labelLayout.ipady = 20;
					fieldLayout.weightx = 1.0;
					
					iepdPanel.add(new JLabel("Name"), labelLayout);
					JTextField nameField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_NAME_PROPERTY), fieldColumns);
					iepdPanel.add(nameField, fieldLayout);
					
					iepdPanel.add(new JLabel("URI"), labelLayout);
					JTextField uriField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_URI_PROPERTY), fieldColumns);
					iepdPanel.add(uriField, fieldLayout);					
					
					iepdPanel.add(new JLabel("Version"), labelLayout);
					JTextField versionField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_VERSION_PROPERTY), fieldColumns);
					iepdPanel.add(versionField, fieldLayout);
					
					iepdPanel.add(new JLabel("Status"), labelLayout);
					JTextField statusField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_STATUS_PROPERTY), fieldColumns);
					iepdPanel.add(statusField, fieldLayout);
					
					iepdPanel.add(new JLabel("Organization"), labelLayout);
					JTextField organizationField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_ORGANIZATION_PROPERTY), fieldColumns);
					iepdPanel.add(organizationField, fieldLayout);
					
					iepdPanel.add(new JLabel("Contact"), labelLayout);
					JTextField contactField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_CONTACT_PROPERTY), fieldColumns);
					iepdPanel.add(contactField, fieldLayout);
					
					iepdPanel.add(new JLabel("Email"), labelLayout);
					JTextField emailField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_EMAIL_PROPERTY), fieldColumns);
					iepdPanel.add(emailField, fieldLayout);

					iepdPanel.add(new JLabel("License URL"), labelLayout);
					JTextField licenseField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_LICENSE_URL_PROPERTY), fieldColumns);
					iepdPanel.add(licenseField, fieldLayout);
					
					iepdPanel.add(new JLabel("Terms of Use"), labelLayout);
					JTextField termsField = new JTextField(NiemTools.getProperty(NiemTools.IEPD_TERMS_URL_PROPERTY), fieldColumns);
					iepdPanel.add(termsField, fieldLayout);
					
					iepdPanel.add(new JLabel("ChangeLog File"), labelLayout);
					FilePanel changelogPanel = new FilePanel(null, NiemTools.getProperty(NiemTools.IEPD_CHANGE_LOG_FILE_PROPERTY), fieldColumns, JFileChooser.FILES_ONLY);
					iepdPanel.add(changelogPanel, fieldLayout);
					
					iepdPanel.add(new JLabel("ReadMe File"), labelLayout);
					FilePanel readmePanel = new FilePanel(null, NiemTools.getProperty(NiemTools.IEPD_READ_ME_FILE_PROPERTY), fieldColumns, JFileChooser.FILES_ONLY);
					iepdPanel.add(readmePanel, fieldLayout);
					
					// add model panel
					JPanel modelPanel = new JPanel(new GridBagLayout());
					
					FilePanel htmlPanel = new FilePanel("Directory", root.propertyValue("html dir"), fieldColumns, JFileChooser.DIRECTORIES_ONLY);
					ToggleBox htmlBox = new ToggleBox("HTML", root.propertyValue("exportHTML"), htmlPanel);
					modelPanel.add(htmlBox, labelLayout);
					fieldLayout.gridy = 0;
					modelPanel.add(htmlPanel, fieldLayout);
					
					FilePanel xsdPanel = new FilePanel("Directory", properties.getProperty("xsdDir"), fieldColumns, JFileChooser.DIRECTORIES_ONLY);
					ToggleBox xsdBox = new ToggleBox("XML", root.propertyValue("exportXML"), xsdPanel);
					modelPanel.add(xsdBox, labelLayout);
					fieldLayout.gridy = 1;
					modelPanel.add(xsdPanel, fieldLayout);
					
					FilePanel wsdlPanel = new FilePanel("Directory", properties.getProperty("wsdlDir"), fieldColumns, JFileChooser.DIRECTORIES_ONLY);
					ToggleBox wsdlBox = new ToggleBox("WSDL", root.propertyValue("exportWSDL"), wsdlPanel);
					modelPanel.add(wsdlBox, labelLayout);
					fieldLayout.gridy = 2;
					modelPanel.add(wsdlPanel, fieldLayout);
					
					FilePanel jsonPanel = new FilePanel("Directory", properties.getProperty("jsonDir"), fieldColumns, JFileChooser.DIRECTORIES_ONLY);
					ToggleBox jsonBox = new ToggleBox("JSON", root.propertyValue("exportJSON"), jsonPanel);
					modelPanel.add(jsonBox, labelLayout);
					fieldLayout.gridy = 3;
					modelPanel.add(jsonPanel, fieldLayout);
					
					FilePanel openapiPanel = new FilePanel("Directory", properties.getProperty("openapiDir"), fieldColumns, JFileChooser.DIRECTORIES_ONLY);
					ToggleBox openapiBox = new ToggleBox("OpenAPI", root.propertyValue("exportOpenAPI"), openapiPanel);
					modelPanel.add(openapiBox, labelLayout);
					fieldLayout.gridy = 4;
					modelPanel.add(openapiPanel, fieldLayout);
					
					// Add external panel
					JPanel externalPanel = new JPanel(new BorderLayout());
					String[] externalNamespaces = NiemTools.getProperty(NiemTools.IEPD_EXTERNAL_SCHEMAS_PROPERTY).split(",");
					int row = 0;
					String[][] data = new String[externalNamespaces.length][3];
					for (String namespace : externalNamespaces) {
						String[] parts = namespace.split("=");
						if (parts.length == 3) {
							data[row] = parts;
							row++;
						}
					}
					DefaultTableModel model = new DefaultTableModel(data, new String[]{"Prefix","Namespace","URL"});
					JTable table = new JTable(model);
					table.setFont(new Font(Font.DIALOG, Font.PLAIN,25));
					table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
					table.getColumnModel().getColumn(0).setMaxWidth(80);
					table.setDefaultRenderer(Object.class, new LineWrapCellRenderer());
					JScrollPane scrollPanel = new JScrollPane(table);
					JButton namespaceButton = new JButton("Add namespace");
					namespaceButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e){
							DefaultTableModel model = (DefaultTableModel)table.getModel();
							model.addRow(new String[] {"", "", ""});
					    }  
					});
					externalPanel.add(namespaceButton, BorderLayout.SOUTH);
					externalPanel.add(scrollPanel, BorderLayout.CENTER);
					
					// add panels
					JTabbedPane dialogPanel = new JTabbedPane();
					dialogPanel.addTab("IEPD metadata", iepdPanel);
					dialogPanel.addTab("Model generation", modelPanel);
					dialogPanel.addTab("External Namespaces", externalPanel);
					dialog.add(dialogPanel);
					
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
					
					// save model values
					root.set_PropertyValue("html dir", htmlPanel.value);
					root.set_PropertyValue("exportHTML", String.valueOf(htmlBox.isSelected()));
					root.set_PropertyValue("exportXML", String.valueOf(xsdBox.isSelected()));
					root.set_PropertyValue("exportWSDL", String.valueOf(wsdlBox.isSelected()));
					root.set_PropertyValue("exportJSON", String.valueOf(jsonBox.isSelected()));
					root.set_PropertyValue("exportOpenAPI", String.valueOf(openapiBox.isSelected()));
					properties.setProperty("xsdDir", xsdPanel.value);
					properties.setProperty("wsdlDir", wsdlPanel.value);
					properties.setProperty("jsonDir", jsonPanel.value);
					properties.setProperty("openapiDir", openapiPanel.value);
					LinkedHashSet<String> externalSchemas2 = new LinkedHashSet<String>();
					//DefaultTableModel model = table.getModel();
					for (int i=0; i < model.getRowCount(); i++) {
						String prefix = model.getValueAt(i, 0).toString();
						String namespace = model.getValueAt(i, 1).toString();
						String url = model.getValueAt(i, 2).toString();
						if (prefix != null && !prefix.equals("") && namespace != null && !namespace.equals("") && url != null && !url.equals(""))
							externalSchemas2.add(prefix + "=" + namespace + "=" + url);
					}
					root.set_PropertyValue(NiemTools.IEPD_EXTERNAL_SCHEMAS_PROPERTY, String.join(",", externalSchemas2));
					
					// save IEPD values
					root.set_PropertyValue(NiemTools.IEPD_NAME_PROPERTY, nameField.getText());
					root.set_PropertyValue(NiemTools.IEPD_URI_PROPERTY, uriField.getText());
					root.set_PropertyValue(NiemTools.IEPD_VERSION_PROPERTY, versionField.getText());
					root.set_PropertyValue(NiemTools.IEPD_STATUS_PROPERTY, statusField.getText());
					root.set_PropertyValue(NiemTools.IEPD_ORGANIZATION_PROPERTY, organizationField.getText());
					root.set_PropertyValue(NiemTools.IEPD_CONTACT_PROPERTY, contactField.getText());
					root.set_PropertyValue(NiemTools.IEPD_EMAIL_PROPERTY, emailField.getText());
					root.set_PropertyValue(NiemTools.IEPD_LICENSE_URL_PROPERTY, licenseField.getText());
					root.set_PropertyValue(NiemTools.IEPD_TERMS_URL_PROPERTY, termsField.getText());
					root.set_PropertyValue(NiemTools.IEPD_CHANGE_LOG_FILE_PROPERTY, changelogPanel.value);
					root.set_PropertyValue(NiemTools.IEPD_READ_ME_FILE_PROPERTY, readmePanel.value);
				
					break;
				case "importSchema":
					// Create PIM
					//NiemTools.createPIM(root);
					
					// Import schema
					UmlCom.message("Importing NIEM schema");
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
					niemTools.cacheModels();
					niemTools.importSchemaDir(directory,false);
					break;

				case "import":
					UmlCom.trace("Deleting NIEM Mapping");
					niemTools.deleteMapping();
					UmlCom.trace("Importing NIEM Mapping");
					JFileChooser fc2 = new JFileChooser(root.propertyValue("html dir"));
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
					if (root.propertyValue("exportHTML").equals("true"))
					{
						UmlCom.trace("Generating HTML documentation");
						//	target.set_dir(argv.length - 1, argv);
						String[] params = {root.propertyValue("html dir")};
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
					niemTools.exportHtml(root.propertyValue("html dir"), "niem-mapping");

					// Generate NIEM Mapping CSV
					UmlCom.message("Generating NIEM Mapping CSV ...");
					UmlCom.trace("Generating NIEM Mapping CSV");
					niemTools.exportCsv(root.propertyValue("html dir"), "niem-mapping.csv"); 

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
					niemTools.exportWantlist(root.propertyValue("html dir"), "wantlist.xml");

					// Generate extension schema
					UmlCom.message("Generating extension schema ...");
					UmlCom.trace("Generating extension schema");
					niemTools.cacheModels(); // cache substitutions
					String xsdDir = (root.propertyValue("exportXML").equals("true")) ? properties.getProperty("xsdDir") : null;
					String wsdlDir = (root.propertyValue("exportWSDL").equals("true")) ? properties.getProperty("wsdlDir") : null;					
					String jsonDir = (root.propertyValue("exportJSON").equals("true")) ? properties.getProperty("jsonDir") : null;
					String openapiDir = (root.propertyValue("exportOpenAPI").equals("true")) ? properties.getProperty("openapiDir") : null;
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
