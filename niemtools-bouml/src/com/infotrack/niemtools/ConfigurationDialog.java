package com.infotrack.niemtools;

/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2017 James E. Cabral Jr., MTG Management Consultants LLC, jcabral@mtgmc.com, http://github.com/cabralje
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import fr.bouml.UmlPackage;

class ConfigurationDialog extends JDialog {
	
	public static final String IEPD_CHANGE_LOG_FILE_DEFAULT = "changelog.txt";
	public static final String IEPD_CHANGE_LOG_FILE_PROPERTY = "IEPDChangeLogFile";
	public static final String IEPD_CONTACT_DEFAULT = "Contact";
	public static final String IEPD_CONTACT_PROPERTY = "IEPDContact";
	public static final String IEPD_EMAIL_DEFAULT = "email@example.com";
	public static final String IEPD_EMAIL_PROPERTY = "IEPDEmail";
	public static final String IEPD_EXTERNAL_SCHEMAS_DEFAULT = "cac=urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonAggregateComponents-2.1.xsd,"
			+ "cbc=urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2=http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/common/UBL-CommonBasicComponents-2.1.xsd,"
			+ "ds=http://www.w3.org/2000/09/xmldsig#=https://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
	// IEPD Properties
	public static final String IEPD_EXTERNAL_SCHEMAS_PROPERTY = "externalSchemas";
	public static final String IEPD_LICENSE_URL_DEFAULT = "https://opensource.org/licenses/BSD-3-Clause";
	public static final String IEPD_LICENSE_URL_PROPERTY = "IEPDLicense";
	public static final String IEPD_NAME_DEFAULT = "IEPD";
	public static final String IEPD_NAME_PROPERTY = "IEPDName";
	public static final String IEPD_ORGANIZATION_DEFAULT = "Organization";
	public static final String IEPD_ORGANIZATION_PROPERTY = "IEPDOrganization";
	public static final String IEPD_READ_ME_FILE_DEFAULT = "readme.txt";
	public static final String IEPD_READ_ME_FILE_PROPERTY = "IEPDReadMeFile";
	public static final String IEPD_STATUS_DEFAULT = "Draft";
	public static final String IEPD_STATUS_PROPERTY = "IEPDStatus";
	public static final String IEPD_TERMS_URL_DEFAULT = "example.com/terms";
	public static final String IEPD_TERMS_URL_PROPERTY = "IEPDTermsOfService";
	public static final String IEPD_URI_DEFAULT = "http://local";
	public static final String IEPD_URI_PROPERTY = "IEPDURI";

	public static final String IEPD_VERSION_DEFAULT = "1.0";

	public static final String IEPD_VERSION_PROPERTY = "IEPDVersion";

	private static final long serialVersionUID = 1L;

	private static class FilePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		String value = "";

		/**
		 * @param name
		 * @param initialValue
		 * @param columns
		 * @param fileType
		 */
		FilePanel(String name, String initialValue, int columns, int fileType) {

			if (initialValue != null)
				value = initialValue;

			// add field label
			if (name != null)
				add(new JLabel(name));

			// add text field
			JTextField textField1 = new JTextField(value, columns);
			textField1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					value = textField1.getText();
				}
			});
			add(textField1);

			// add field button
			JButton button1 = new JButton("Browse...");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(value);
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

	private static class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		LineWrapCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setFont(new Font(Font.DIALOG, Font.PLAIN, 25));
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
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

		/**
		 * @param name
		 * @param initialValue
		 * @param panel
		 */
		ToggleBox(String name, String initialValue, JPanel panel) {
			super(name, (initialValue == null || !initialValue.equals("false")));
			panel.setVisible(this.isSelected());
			addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					panel.setVisible(((JCheckBox) (e.getItem())).isSelected());
				}
			});
		}
	}

	/**
	 * initialize IEPD properties
	 */
	static void init() {

		// set IEPD configuration defaults
		setPropertyDefault(IEPD_URI_PROPERTY, IEPD_URI_DEFAULT);
		setPropertyDefault(IEPD_NAME_PROPERTY, IEPD_NAME_DEFAULT);
		setPropertyDefault(IEPD_VERSION_PROPERTY, IEPD_VERSION_DEFAULT);
		setPropertyDefault(IEPD_STATUS_PROPERTY, IEPD_STATUS_DEFAULT);
		setPropertyDefault(IEPD_ORGANIZATION_PROPERTY, IEPD_ORGANIZATION_DEFAULT);
		setPropertyDefault(IEPD_CONTACT_PROPERTY, IEPD_CONTACT_DEFAULT);
		setPropertyDefault(IEPD_EMAIL_PROPERTY, IEPD_EMAIL_DEFAULT);
		setPropertyDefault(IEPD_LICENSE_URL_PROPERTY, IEPD_LICENSE_URL_DEFAULT);
		setPropertyDefault(IEPD_TERMS_URL_PROPERTY, IEPD_TERMS_URL_DEFAULT);
		setPropertyDefault(IEPD_READ_ME_FILE_PROPERTY, IEPD_READ_ME_FILE_DEFAULT);
		setPropertyDefault(IEPD_CHANGE_LOG_FILE_PROPERTY, IEPD_CHANGE_LOG_FILE_DEFAULT);
		setPropertyDefault(IEPD_EXTERNAL_SCHEMAS_PROPERTY, IEPD_EXTERNAL_SCHEMAS_DEFAULT);

	}

	/** sets a project property */
	/**
	 * @param propertyName
	 * @param propertyValue
	 */
	static void setPropertyDefault(String propertyName, String propertyValue) {
		UmlPackage root = UmlPackage.getProject();
		if (root.propertyValue(propertyName) == null)
			root.set_PropertyValue(propertyName, propertyValue);
	}

	/**
	 * @param root
	 * @param properties
	 */
	ConfigurationDialog(UmlPackage root, Properties properties) {
		// create dialog
		super(new JFrame(), "Niem-tools Configuration", true);
		setSize(1400, 700);
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
		JTextField nameField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_NAME_PROPERTY),
				fieldColumns);
		iepdPanel.add(nameField, fieldLayout);

		iepdPanel.add(new JLabel("URI"), labelLayout);
		JTextField uriField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_URI_PROPERTY),
				fieldColumns);
		iepdPanel.add(uriField, fieldLayout);

		iepdPanel.add(new JLabel("Version"), labelLayout);
		JTextField versionField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_VERSION_PROPERTY),
				fieldColumns);
		iepdPanel.add(versionField, fieldLayout);

		iepdPanel.add(new JLabel("Status"), labelLayout);
		JTextField statusField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_STATUS_PROPERTY),
				fieldColumns);
		iepdPanel.add(statusField, fieldLayout);

		iepdPanel.add(new JLabel("Organization"), labelLayout);
		JTextField organizationField = new JTextField(
				NiemUmlClass.getProperty(ConfigurationDialog.IEPD_ORGANIZATION_PROPERTY), fieldColumns);
		iepdPanel.add(organizationField, fieldLayout);

		iepdPanel.add(new JLabel("Contact"), labelLayout);
		JTextField contactField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_CONTACT_PROPERTY),
				fieldColumns);
		iepdPanel.add(contactField, fieldLayout);

		iepdPanel.add(new JLabel("Email"), labelLayout);
		JTextField emailField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_EMAIL_PROPERTY),
				fieldColumns);
		iepdPanel.add(emailField, fieldLayout);

		iepdPanel.add(new JLabel("License URL"), labelLayout);
		JTextField licenseField = new JTextField(
				NiemUmlClass.getProperty(ConfigurationDialog.IEPD_LICENSE_URL_PROPERTY), fieldColumns);
		licenseField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent evt) {
				String value = licenseField.getText();
				if (value.startsWith("http"))
					try {
						new URL(value);
					} catch (MalformedURLException e1) {
						Log.trace("main: URL " + value + " is malformed");
					}
			}
		});
		iepdPanel.add(licenseField, fieldLayout);

		iepdPanel.add(new JLabel("Terms of Use URL"), labelLayout);
		JTextField termsField = new JTextField(NiemUmlClass.getProperty(ConfigurationDialog.IEPD_TERMS_URL_PROPERTY),
				fieldColumns);
		termsField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent evt) {
				String value = termsField.getText();
				if (value.startsWith("http"))
					try {
						new URL(value);
					} catch (MalformedURLException e1) {
						Log.trace("URL " + value + " is malformed");
					}
			}
		});
		iepdPanel.add(termsField, fieldLayout);

		iepdPanel.add(new JLabel("ChangeLog File"), labelLayout);
		ConfigurationDialog.FilePanel changelogPanel = new FilePanel(null,
				NiemUmlClass.getProperty(ConfigurationDialog.IEPD_CHANGE_LOG_FILE_PROPERTY), fieldColumns,
				JFileChooser.FILES_ONLY);
		iepdPanel.add(changelogPanel, fieldLayout);

		iepdPanel.add(new JLabel("ReadMe File"), labelLayout);
		ConfigurationDialog.FilePanel readmePanel = new FilePanel(null,
				NiemUmlClass.getProperty(ConfigurationDialog.IEPD_READ_ME_FILE_PROPERTY), fieldColumns,
				JFileChooser.FILES_ONLY);
		iepdPanel.add(readmePanel, fieldLayout);

		// add model panel
		JPanel modelPanel = new JPanel(new GridBagLayout());

		ConfigurationDialog.FilePanel htmlPanel = new FilePanel("Directory", root.propertyValue("html dir"),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox htmlBox = new ToggleBox("HTML", root.propertyValue("exportHTML"), htmlPanel);
		modelPanel.add(htmlBox, labelLayout);
		fieldLayout.gridy = 0;
		modelPanel.add(htmlPanel, fieldLayout);

		ConfigurationDialog.FilePanel xsdPanel = new FilePanel("Directory", properties.getProperty("xsdDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox xsdBox = new ToggleBox("XML Schema", root.propertyValue("exportXML"), xsdPanel);
		modelPanel.add(xsdBox, labelLayout);
		fieldLayout.gridy = 1;
		modelPanel.add(xsdPanel, fieldLayout);

		ConfigurationDialog.FilePanel xmlExamplePanel = new FilePanel("Directory", properties.getProperty("xmlExampleDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		modelPanel.add(new JLabel("XML Examples"), labelLayout);
		fieldLayout.gridy = 2;
		modelPanel.add(xmlExamplePanel, fieldLayout);
		
		ConfigurationDialog.FilePanel wsdlPanel = new FilePanel("Directory", properties.getProperty("wsdlDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox wsdlBox = new ToggleBox("WSDL", root.propertyValue("exportWSDL"), wsdlPanel);
		modelPanel.add(wsdlBox, labelLayout);
		fieldLayout.gridy = 3;
		modelPanel.add(wsdlPanel, fieldLayout);

		ConfigurationDialog.FilePanel jsonPanel = new FilePanel("Directory", properties.getProperty("jsonDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox jsonBox = new ToggleBox("JSON Schema", root.propertyValue("exportJSON"), jsonPanel);
		modelPanel.add(jsonBox, labelLayout);
		fieldLayout.gridy = 4;
		modelPanel.add(jsonPanel, fieldLayout);

		ConfigurationDialog.FilePanel jsonExamplePanel = new FilePanel("Directory", properties.getProperty("jsonExampleDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		modelPanel.add(new JLabel("JSON Examples"), labelLayout);
		fieldLayout.gridy = 5;
		modelPanel.add(jsonExamplePanel, fieldLayout);
		
		ConfigurationDialog.FilePanel openapiPanel = new FilePanel("Directory", properties.getProperty("openapiDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox openapiBox = new ToggleBox("OpenAPI", root.propertyValue("exportOpenAPI"),
				openapiPanel);
		modelPanel.add(openapiBox, labelLayout);
		fieldLayout.gridy = 6;
		modelPanel.add(openapiPanel, fieldLayout);
		
		ConfigurationDialog.FilePanel metamodelPanel = new FilePanel("Directory", properties.getProperty("metamodelDir", root.propertyValue("html dir")),
				fieldColumns, JFileChooser.DIRECTORIES_ONLY);
		ConfigurationDialog.ToggleBox metamodelBox = new ToggleBox("Metamodel", root.propertyValue("exportMetamodel"),
				metamodelPanel);
		modelPanel.add(metamodelBox, labelLayout);
		fieldLayout.gridy = 7;
		modelPanel.add(metamodelPanel, fieldLayout);

		// Add external panel
		JPanel externalPanel = new JPanel(new BorderLayout());
		String[] externalNamespaces = NiemUmlClass.getProperty(ConfigurationDialog.IEPD_EXTERNAL_SCHEMAS_PROPERTY)
				.split(",");
		int row = 0;
		String[][] data = new String[externalNamespaces.length][3];
		for (String namespace : externalNamespaces) {
			String[] parts = namespace.split("=");
			if (parts.length == 3)
				data[row++] = parts;
		}
		DefaultTableModel model = new DefaultTableModel(data, new String[] { "Prefix", "Namespace", "URL" });
		JTable table = new JTable(model);
		Font font = new Font(Font.DIALOG, Font.PLAIN, 25);
		table.setFont(font);
		Font font2 = new Font(Font.DIALOG, Font.BOLD, 25);
		JTableHeader header = table.getTableHeader();
		header.setFont(font2);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.setDefaultRenderer(Object.class, new LineWrapCellRenderer());
		JScrollPane scrollPanel = new JScrollPane(table);
		JButton namespaceButton = new JButton("Add namespace");
		namespaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.addRow(new String[] { "", "", "" });
			}
		});
		externalPanel.add(namespaceButton, BorderLayout.SOUTH);
		externalPanel.add(scrollPanel, BorderLayout.CENTER);

		// add panels
		JTabbedPane dialogPanel = new JTabbedPane();
		dialogPanel.addTab("IEPD metadata", iepdPanel);
		dialogPanel.addTab("Model generation", modelPanel);
		dialogPanel.addTab("External Namespaces", externalPanel);
		add(dialogPanel);

		// add frame button
		JButton frameButton = new JButton("OK");
		frameButton.setHorizontalAlignment(SwingConstants.CENTER);
		frameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(frameButton, BorderLayout.SOUTH);

		// show frame
		setVisible(true);

		try {
			// save model values
			root.set_PropertyValue("html dir", htmlPanel.value);
			root.set_PropertyValue("exportHTML", String.valueOf(htmlBox.isSelected()));
			root.set_PropertyValue("exportXML", String.valueOf(xsdBox.isSelected()));
			root.set_PropertyValue("exportWSDL", String.valueOf(wsdlBox.isSelected()));
			root.set_PropertyValue("exportJSON", String.valueOf(jsonBox.isSelected()));
			root.set_PropertyValue("exportOpenAPI", String.valueOf(openapiBox.isSelected()));
			root.set_PropertyValue("exportMetamodel", String.valueOf(metamodelBox.isSelected()));
			properties.setProperty("xsdDir", xsdPanel.value);
			properties.setProperty("xmlExampleDir", xmlExamplePanel.value);			
			properties.setProperty("wsdlDir", wsdlPanel.value);
			properties.setProperty("jsonDir", jsonPanel.value);
			properties.setProperty("jsonExampleDir", jsonExamplePanel.value);
			properties.setProperty("openapiDir", openapiPanel.value);
			properties.setProperty("metamodelDir", metamodelPanel.value);			
			LinkedHashSet<String> externalSchemas2 = new LinkedHashSet<String>();
			// DefaultTableModel model = table.getModel();
			if (model != null)
				for (int i = 0; i < model.getRowCount(); i++) {
					String prefix = "", namespace = "", url = "";
					Object prefixValue = model.getValueAt(i, 0);
					if (prefixValue != null)
						prefix = prefixValue.toString();
					Object namespaceValue = model.getValueAt(i, 1);
					if (namespaceValue != null)
						namespace = namespaceValue.toString();
					Object urlValue = model.getValueAt(i,2);
					if (urlValue != null)
						url = urlValue.toString();
					if (url.startsWith("http"))
						try {
							new URL(url);
						} catch (MalformedURLException e1) {
							Log.trace("URL " + url + " is malformed");
						}
					if (prefix != null && !prefix.equals("") && namespace != null && !namespace.equals("") && url != null
							&& !url.equals(""))
						externalSchemas2.add(prefix + "=" + namespace + "=" + url);
				}
			root.set_PropertyValue(ConfigurationDialog.IEPD_EXTERNAL_SCHEMAS_PROPERTY, String.join(",", externalSchemas2));

			// save IEPD values
			root.set_PropertyValue(ConfigurationDialog.IEPD_NAME_PROPERTY, nameField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_URI_PROPERTY, uriField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_VERSION_PROPERTY, versionField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_STATUS_PROPERTY, statusField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_ORGANIZATION_PROPERTY, organizationField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_CONTACT_PROPERTY, contactField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_EMAIL_PROPERTY, emailField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_LICENSE_URL_PROPERTY, licenseField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_TERMS_URL_PROPERTY, termsField.getText());
			root.set_PropertyValue(ConfigurationDialog.IEPD_CHANGE_LOG_FILE_PROPERTY, changelogPanel.value);
			root.set_PropertyValue(ConfigurationDialog.IEPD_READ_ME_FILE_PROPERTY, readmePanel.value);
		} catch (RuntimeException e1) {
			Log.trace("ConfigurationDialog: exception " + e1.toString());
		}
	}
}