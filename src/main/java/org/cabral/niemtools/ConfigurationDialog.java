package org.cabral.niemtools;

/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2025 James E. Cabral Jr., jim@cabral.org, http://github.com/cabralje
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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

class ConfigurationDialog extends JDialog {

    private final ProjectProperties properties;

    private String command = null;

    private static class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

        LineWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value != null) {
                setText(value.toString());
            }
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) != getPreferredSize().height) {
                table.setRowHeight(row, getPreferredSize().height);
            }
            return this;
        }
    }

    @FunctionalInterface
    public interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }

    /**
     * Creates a label with the specified name.
     * @param name
     * @return
     */
    private JLabel label(String name) {
        JLabel label = new JLabel(name, JLabel.CENTER);
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        return label;
    }

    /**
     * Creates a labeled field with the specified name, property, and number of
     * columns. 
     * @param name
     * @param property
     * @param fieldColumns
     * @return
     */
    private JPanel labeledField(String name, String property, int fieldColumns) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(name, JLabel.RIGHT));
        JTextField field = new JTextField(properties.getProperty(property), fieldColumns);
        field.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(property, field.getText());
        });
        panel.add(field);
        return panel;
    }

    /**
     * Creates a labeled field with the specified name, property, and number of
     * columns.
     * @param name
     * @param boxProperty
     * @return
     */
    private JCheckBox checkedBox(String name, String boxProperty) {
        JCheckBox box = new JCheckBox(name, properties.getProperty(boxProperty).equals("true"));
        box.addItemListener((ItemEvent e) -> {
            properties.setProperty(boxProperty, String.valueOf(box.isSelected()));
        });
        return box;
    }

    /**
     * Creates a navigation button with the specified name and tab.
     * @param name
     * @param tab
     * @return
     */
    private JButton navigationButton(String name, String tab) {
        JButton button = new JButton(name);
        button.addActionListener((ActionEvent e) -> {
            // Find the parent JTabbedPane and set the selected tab to "Import Reference Model"
            Component c = button;
            while (c != null && !(c instanceof JTabbedPane)) {
                c = c.getParent();
            }
            if (c instanceof JTabbedPane tabbedPane) {
                int index = tabbedPane.indexOfTab(tab);
                if (index != -1)
                    tabbedPane.setSelectedIndex(index);
            }
        });
        return button;
    }

    /**
     * Creates a command button with the specified name and command.
     * @param name
     * @param command
     * @return
     */
    private JButton commandButton(String name, String command) {
        JButton button = new JButton(name);
        button.addActionListener((ActionEvent e) -> {
            // Find the parent JTabbedPane and set the selected tab to "Import Reference Model"
            Component c = button;
            while (c != null && !(c instanceof ConfigurationDialog)) {
                c = c.getParent();
            }
            if (c instanceof ConfigurationDialog dialog) {
                dialog.command = command;
                setVisible(false);
            }
        });
        return button;
    }

    private static final String MAIN_TAB = "Home";
    private static final String IMPORT_TAB = "Reference Model";
    private static final String MAPPING_TAB = "Mapping";
    private static final String EXPORT_TAB = "Publish";
    private static final String METADATA_TAB = "Metadata";
    private static final String EXTERNAL_TAB = "External Schemas";

    /**
     * Constructor that initializes the configuration dialog with the given
     * properties.
     * @param inputProperties
     */
    ConfigurationDialog(ProjectProperties inputProperties) {

        // create dialog
        super(new JFrame(), "Niemtools Configuration", true);
        properties = inputProperties;
        setSize(800,260);

    }

    String showDialog() {

        // overview panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(navigationButton("Import Reference Model", IMPORT_TAB), BorderLayout.NORTH);
        mainPanel.add(navigationButton("Map to NIEM", MAPPING_TAB), BorderLayout.WEST);
        mainPanel.add(navigationButton("Publish NIEM schemas", EXPORT_TAB), BorderLayout.CENTER);
        mainPanel.add(navigationButton("Publish NIEM message specification", EXPORT_TAB), BorderLayout.EAST);
        mainPanel.add(navigationButton("Configure message specification metadata", METADATA_TAB), BorderLayout.SOUTH);

        // import panel
        JPanel importPanel = new JPanel(new BorderLayout());

        // import button
        importPanel.add(navigationButton("Configure External Schemas", EXTERNAL_TAB), BorderLayout.NORTH);
        importPanel.add(commandButton("Import NIEM Reference Model","importReferenceModel"), BorderLayout.SOUTH);

        int fieldColumns = 20;
        // import options
        JPanel importPanel1 = new JPanel(new BorderLayout());
        importPanel1.add(label("Include domains"), BorderLayout.NORTH);
        importPanel1.add(labeledField("", ProjectProperties.IMPORT_INCLUDE_DOMAINS, fieldColumns), BorderLayout.CENTER);
        importPanel.add(importPanel1, BorderLayout.WEST);

        JPanel importPanel2 = new JPanel(new BorderLayout());
        importPanel2.add(label("Exclude domains"), BorderLayout.NORTH);
        importPanel2.add(labeledField("", ProjectProperties.IMPORT_EXCLUDE_DOMAINS, fieldColumns), BorderLayout.CENTER);
        importPanel.add(importPanel2, BorderLayout.CENTER);

        JPanel importPanel3 = new JPanel(new BorderLayout());
        importPanel3.add(label("Exclude codes"), BorderLayout.NORTH);
        importPanel3.add(labeledField("", ProjectProperties.IMPORT_EXCLUDE_CODES, fieldColumns), BorderLayout.CENTER);
        //importPanel2.add(labeledField("Maximum facets", ProjectProperties.IMPORT_MAX_FACETS, fieldColumns));
        importPanel.add(importPanel3, BorderLayout.EAST);
 
        // mapping panel
        JPanel mappingPanel = new JPanel(new BorderLayout());

        // project directory
        JPanel projectPanel = new JPanel();
        projectPanel.add(label("Project Directory"));
        JTextField textField1 = new JTextField(properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR), 60);
        textField1.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, textField1.getText());
            String modelDir = textField1.getText()+ File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MODEL_DIR);
            properties.setProperty(ProjectProperties.EXPORT_MODEL_DIR, modelDir);
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, modelDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        });
        projectPanel.add(textField1);
        JButton button1 = new JButton("Browse...");
        button1.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
                return;
            String value = fc.getSelectedFile().getAbsolutePath();
            textField1.setText(value);
            properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, textField1.getText());
        });
        projectPanel.add(button1);
        mappingPanel.add(projectPanel, BorderLayout.NORTH);

        // publish UML button
        JButton publishUMLButton = commandButton("Publish UML Model","publishUML");
        mappingPanel.add(publishUMLButton, BorderLayout.WEST);

        // import mapping button
        JButton importMappingButton = commandButton("Import Mapping File","importMapping");
        mappingPanel.add(importMappingButton, BorderLayout.CENTER);

        // validate NIEM button
        JButton validateMappingButton = commandButton("Validate NIEM Mapping","validateMapping");
        mappingPanel.add(validateMappingButton, BorderLayout.EAST);

        // export panel
        JPanel exportPanel = new JPanel(new BorderLayout());

        // uri field
        JPanel uriPanel = labeledField("URI for extensions", ProjectProperties.EXPORT_URI, fieldColumns);
        exportPanel.add(uriPanel, BorderLayout.NORTH);
 
        // publish UML
        JPanel exportPanel1 = new JPanel(new BorderLayout());
        exportPanel1.add(label("Common Model Format (CMF)"), BorderLayout.NORTH);
        JPanel exportPanel1a = new JPanel(new BorderLayout());
        exportPanel1a.add(labeledField("Directory", ProjectProperties.EXPORT_CMF_DIR, fieldColumns), BorderLayout.NORTH);
        exportPanel1a.add(labeledField("Version", ProjectProperties.EXPORT_CMF_VERSION, fieldColumns), BorderLayout.CENTER);
        exportPanel1a.add(checkedBox("Include CMF in Message Specification", ProjectProperties.EXPORT_CMF), BorderLayout.SOUTH);
        exportPanel1.add(exportPanel1a, BorderLayout.CENTER);
        exportPanel1.add(commandButton("Publish CMF","publishCMF"), BorderLayout.SOUTH);
        exportPanel.add(exportPanel1, BorderLayout.WEST);

        // publish XSD
        JPanel exportPanel2 = new JPanel(new BorderLayout());
        exportPanel2.add(label("XML Schema (XSD)"), BorderLayout.NORTH);
        JPanel exportPanel2a = new JPanel(new BorderLayout());
        exportPanel2a.add(labeledField("Directory", ProjectProperties.EXPORT_XSD_DIR, fieldColumns), BorderLayout.NORTH);
        //exportPanel2a.add(labeledField("Wantlist File", ProjectProperties.EXPORT_WANTLIST_FILE, fieldColumns), BorderLayout.CENTER);
        exportPanel2a.add(checkedBox("Include XSD in Message Specification", ProjectProperties.EXPORT_XSD), BorderLayout.CENTER);
        exportPanel2a.add(checkedBox("Include WSDL in Message Specification", ProjectProperties.EXPORT_WSDL), BorderLayout.SOUTH);
        exportPanel2.add(exportPanel2a, BorderLayout.CENTER);
        exportPanel2.add(commandButton("Publish XSD","publishXSD"), BorderLayout.SOUTH);
        exportPanel.add(exportPanel2, BorderLayout.CENTER);

        // publish JSON
        JPanel exportPanel3 = new JPanel(new BorderLayout());
        exportPanel3.add(label("JSON Schema"), BorderLayout.NORTH);
        JPanel exportPanel3a = new JPanel(new BorderLayout());
        exportPanel3a.add(labeledField("Directory", ProjectProperties.EXPORT_JSON_SCHEMA_DIR, fieldColumns), BorderLayout.NORTH);
        exportPanel3a.add(checkedBox("Include JSON in Message Specification", ProjectProperties.EXPORT_JSON), BorderLayout.CENTER);
        exportPanel3a.add(checkedBox("Include OpenAPI in Message Specification", ProjectProperties.EXPORT_OPENAPI), BorderLayout.SOUTH);
        exportPanel3.add(exportPanel3a, BorderLayout.CENTER);
        exportPanel3.add(commandButton("Publish JSON","publishJSON"), BorderLayout.SOUTH);
        exportPanel.add(exportPanel3, BorderLayout.EAST);

        // export button
        JButton publishSpecButton = commandButton("Publish NIEM Message Specification","publishSpecification");
        exportPanel.add(publishSpecButton, BorderLayout.SOUTH);

        // message specification metadata panel
        JPanel metadataPanel = new JPanel(new BorderLayout());
        fieldColumns = 20;
        
        JPanel metadataPanel1 = new JPanel(new BorderLayout());
        metadataPanel1.add(labeledField("Name", ProjectProperties.IEPD_NAME, fieldColumns), BorderLayout.NORTH);
        metadataPanel1.add(labeledField("Version", ProjectProperties.IEPD_VERSION, fieldColumns), BorderLayout.CENTER); 
        metadataPanel1.add(labeledField("Status", ProjectProperties.IEPD_STATUS, fieldColumns), BorderLayout.SOUTH);
        metadataPanel.add(metadataPanel1, BorderLayout.WEST);

        JPanel metadataPanel2 = new JPanel(new BorderLayout());
        metadataPanel2.add(labeledField("Organization", ProjectProperties.IEPD_ORGANIZATION, fieldColumns), BorderLayout.NORTH);
        metadataPanel2.add(labeledField("Contact", ProjectProperties.IEPD_CONTACT, fieldColumns), BorderLayout.CENTER);
        metadataPanel2.add(labeledField("Email", ProjectProperties.IEPD_EMAIL, fieldColumns), BorderLayout.SOUTH);
        metadataPanel.add(metadataPanel2, BorderLayout.CENTER);

        JPanel metadataPanel3 = new JPanel(new BorderLayout());
        metadataPanel3.add(labeledField("License URL", ProjectProperties.IEPD_LICENSE_URL, fieldColumns), BorderLayout.NORTH);
        //metadataPanel3.add(labeledField("Terms of Use URL", ProjectProperties.IEPD_TERMS_URL, fieldColumns), BorderLayout.CENTER);
        metadataPanel3.add(labeledField("ChangeLog", ProjectProperties.IEPD_CHANGE_LOG_FILE, fieldColumns), BorderLayout.CENTER);
        metadataPanel3.add(labeledField("Readme", ProjectProperties.IEPD_READ_ME_FILE, fieldColumns), BorderLayout.SOUTH);
        metadataPanel.add(metadataPanel3, BorderLayout.EAST);

        // external schemas panel
        JPanel externalPanel = new JPanel(new BorderLayout());
        String[] externalNamespaces = properties.getProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS).split(",");
        int row = 0;
        String[][] data = new String[externalNamespaces.length][3];
        for (String namespace : externalNamespaces) {
            String[] parts = namespace.split("=");
            if (parts.length == 3)
                data[row++] = parts;
        }
        DefaultTableModel model = new DefaultTableModel(data, new String[]{"Prefix", "Namespace", "URL"});
        JTable table = new JTable(model);
        Font font = new Font(Font.DIALOG, Font.PLAIN, 10);
        table.setFont(font);
        Font font2 = new Font(Font.DIALOG, Font.BOLD, 10);
        table.getTableHeader().setFont(font2);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //table.getColumnModel().getColumn(0).setMinWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setDefaultRenderer(Object.class, new LineWrapCellRenderer());
        JScrollPane scrollPanel = new JScrollPane(table);
        JButton namespaceButton = new JButton("Add namespace");
        namespaceButton.addActionListener((ActionEvent e) -> {
            DefaultTableModel model1 = (DefaultTableModel) table.getModel();
            model1.addRow(new String[]{"", "", ""});
        });
        externalPanel.add(namespaceButton, BorderLayout.SOUTH);
        externalPanel.add(scrollPanel, BorderLayout.CENTER);

        // tabbed panel
        JTabbedPane dialogPanel = new JTabbedPane();
        dialogPanel.addTab(MAIN_TAB, mainPanel);
        dialogPanel.addTab(IMPORT_TAB, importPanel);
        dialogPanel.addTab(MAPPING_TAB, mappingPanel);
        dialogPanel.addTab(EXPORT_TAB, exportPanel);
        dialogPanel.addTab(METADATA_TAB, metadataPanel);
        dialogPanel.addTab(EXTERNAL_TAB, externalPanel);
        add(dialogPanel);

        // frame button
        JButton frameButton = new JButton("OK");
        frameButton.setHorizontalAlignment(SwingConstants.CENTER);
        frameButton.addActionListener((ActionEvent e) -> {
            setVisible(false);
        });
        add(frameButton, BorderLayout.SOUTH);

        // show frame
        setVisible(true);

        try {
            LinkedHashSet<String> externalSchemas2 = new LinkedHashSet<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String prefix = "", namespace = "", url = "";
                Object prefixValue = model.getValueAt(i, 0);
                if (prefixValue != null)
                    prefix = prefixValue.toString();
                Object namespaceValue = model.getValueAt(i, 1);
                if (namespaceValue != null)
                    namespace = namespaceValue.toString();
                Object urlValue = model.getValueAt(i, 2);
                if (urlValue != null)
                    url = urlValue.toString();
                if (url.startsWith("http"))
					try {
                    URI uri = new URI(url);
                    URL url2 = uri.toURL();
                } catch (URISyntaxException | MalformedURLException e1) {
                    Log.trace("URL " + url + " is malformed");
                }
                if (prefix != null && !prefix.isEmpty() && namespace != null && !namespace.isEmpty()
                        && !url.isEmpty())
                    externalSchemas2.add(prefix + "=" + namespace + "=" + url);
            }
            properties.setProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, String.join(",", externalSchemas2));

        } catch (RuntimeException e1) {
            Log.trace("ConfigurationDialog: exception " + e1.toString());
        }

        return command;
    }
}
