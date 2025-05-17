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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;

import javax.swing.BoxLayout;
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
    private final GridBagConstraints labelLayout;
    private final GridBagConstraints fieldLayout;

    private String command = null;

    private static class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

        LineWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font(Font.DIALOG, Font.PLAIN, 25));
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

    private JPanel labeledField(String name, String property, int fieldColumns) {
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name, SwingConstants.RIGHT);
        panel.add(label, labelLayout);
        JTextField field = new JTextField(properties.getProperty(property), fieldColumns);
        field.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(property, field.getText());
        });
        panel.add(field, fieldLayout);
        return panel;
    }

    private JPanel checkedField(String name, String boxProperty, String fieldProperty, int fieldColumns) {
        
        JPanel panel = new JPanel();
        JCheckBox box = new JCheckBox(name, properties.getProperty(boxProperty).equals("true"));
        box.addItemListener((ItemEvent e) -> {
            properties.setProperty(boxProperty, String.valueOf(box.isSelected()));
        });
        panel.add(box, labelLayout);
        JTextField field = new JTextField(properties.getProperty(fieldProperty), fieldColumns);
        field.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(fieldProperty, field.getText());
        });
        panel.add(field, fieldLayout);
        return panel;
    }
    
    private JButton button(String name) {
        JButton button = new JButton(name);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setAlignmentY(CENTER_ALIGNMENT);
        button.addActionListener((ActionEvent e) -> {
            // Find the parent JTabbedPane and set the selected tab to "Import Reference Model"
            Component c = button;
            while (c != null && !(c instanceof JTabbedPane)) {
                c = c.getParent();
            }
            if (c instanceof JTabbedPane tabbedPane) {
                int index = tabbedPane.indexOfTab(name);
                if (index != -1)
                    tabbedPane.setSelectedIndex(index);
            }
        });
        return button;
    }

    private JButton commandButton(String name, String command) {
        JButton button = new JButton(name);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setAlignmentY(CENTER_ALIGNMENT);
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

    private static final String MAIN_TAB = "Modeling Workflow";
    private static final String IMPORT_TAB = "Import Reference Model";
    private static final String EXPORT_TAB = "Generate Specification";
    private static final String METADATA_TAB = "Configure Metadata";
    private static final String EXTERNAL_TAB = "Configure External Schemas";
    /**
     * @param root
     */
    ConfigurationDialog(ProjectProperties inputProperties) {

        // create dialog
        super(new JFrame(), "Niemtools Configuration", true);
        properties = inputProperties;
        setSize(800,800);

        // configure layouts
        labelLayout = new GridBagConstraints();
        labelLayout.gridx = 0;
        labelLayout.ipady = 20;
        fieldLayout = new GridBagConstraints();
        fieldLayout.gridx = 1;
        fieldLayout.ipadx = 1000;
        labelLayout.ipady = 20;
        fieldLayout.weightx = 1.0;

    }

    String showDialog() {

        // add overview panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel mainPanel1 = new JPanel();
        mainPanel1.setLayout(new BoxLayout(mainPanel1, BoxLayout.Y_AXIS));

        mainPanel1.add(button(IMPORT_TAB));
        mainPanel1.add(button(EXPORT_TAB));
        mainPanel1.add(button(METADATA_TAB));
        mainPanel1.add(button(EXTERNAL_TAB));
        mainPanel1.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel1.setAlignmentY(CENTER_ALIGNMENT);
        mainPanel.add(mainPanel1, BorderLayout.CENTER);

        // add IEPD panel
        JPanel iepdPanel = new JPanel(new BorderLayout());
        int fieldColumns = 30;
        
        JPanel iepdPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        iepdPanel1.add(labeledField("Name", ProjectProperties.IEPD_NAME, fieldColumns));
        iepdPanel1.add(labeledField("URI", ProjectProperties.EXPORT_URI, fieldColumns));
        iepdPanel1.add(labeledField("Version", ProjectProperties.IEPD_VERSION, fieldColumns)); 
        iepdPanel1.add(labeledField("Status", ProjectProperties.IEPD_STATUS, fieldColumns));
        iepdPanel1.add(labeledField("Organization", ProjectProperties.IEPD_ORGANIZATION, fieldColumns));
        iepdPanel1.add(labeledField("Contact", ProjectProperties.IEPD_CONTACT, fieldColumns));
        iepdPanel1.add(labeledField("Email", ProjectProperties.IEPD_EMAIL, fieldColumns));
        iepdPanel1.add(labeledField("License URL", ProjectProperties.IEPD_LICENSE_URL, fieldColumns));
        iepdPanel1.add(labeledField("Terms of Use URL", ProjectProperties.IEPD_TERMS_URL, fieldColumns));
        iepdPanel1.add(labeledField("ChangeLog File", ProjectProperties.IEPD_CHANGE_LOG_FILE, fieldColumns));
        iepdPanel1.add(labeledField("Readme File", ProjectProperties.IEPD_READ_ME_FILE, fieldColumns));
        iepdPanel.add(iepdPanel1, BorderLayout.CENTER);

        // add import panel
        JPanel importPanel = new JPanel(new BorderLayout());
        JPanel importPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // add reference model directory
        JPanel referenceModelPanel = new JPanel();
        referenceModelPanel.add(new JLabel("Reference Model Directory"));
        JTextField textField = new JTextField(properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR), 60);
        textField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR, textField.getText());
        });
        referenceModelPanel.add(textField);
        JButton button = new JButton("Browse...");
        button.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
                return;
            String value = fc.getSelectedFile().getAbsolutePath();
            textField.setText(value);
            properties.setProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR, textField.getText());
        });
        referenceModelPanel.add(button);
        importPanel.add(referenceModelPanel, BorderLayout.NORTH);

        // add import options
        importPanel1.add(labeledField("Include domains", ProjectProperties.IMPORT_INCLUDE_DOMAINS, fieldColumns));
        importPanel1.add(labeledField("Exclude domains", ProjectProperties.IMPORT_EXCLUDE_DOMAINS, fieldColumns));
        importPanel1.add(labeledField("Exclude codes", ProjectProperties.IMPORT_EXCLUDE_CODES, fieldColumns));
        importPanel1.add(labeledField("Maximum facets", ProjectProperties.IMPORT_MAX_FACETS, fieldColumns));
        importPanel.add(importPanel1, BorderLayout.CENTER);

        // add import button
        JButton importButton = commandButton("Import Reference Model","importSchema");
        importPanel.add(importButton, BorderLayout.SOUTH);

        // add export panel
        JPanel exportPanel = new JPanel(new BorderLayout());

        // add project directory
        JPanel exportPanel1 = new JPanel();
        exportPanel1.setLayout(new BoxLayout(exportPanel1, BoxLayout.Y_AXIS));

        JPanel projectPanel = new JPanel();
        projectPanel.add(new JLabel("Project Directory"));
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
        exportPanel1.add(projectPanel);

        JPanel modelPanel = new JPanel();
        modelPanel.add(new JLabel("Model Directory"));
        JTextField textField2 = new JTextField(properties.getProperty(ProjectProperties.EXPORT_MODEL_DIR), 60);
        textField1.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(ProjectProperties.EXPORT_MODEL_DIR, textField2.getText());
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, textField2.getText() + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        });
        modelPanel.add(textField2);
        JButton button2 = new JButton("Browse...");
        button1.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(properties.getProperty(ProjectProperties.EXPORT_MODEL_DIR));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
                return;
            String value = fc.getSelectedFile().getAbsolutePath();
            textField2.setText(value);
            properties.setProperty(ProjectProperties.EXPORT_MODEL_DIR, textField2.getText());
        });
        modelPanel.add(button2);
        exportPanel1.add(modelPanel);

        JPanel mappingPanel = new JPanel();
        mappingPanel.add(new JLabel("Mapping File"));
        JTextField textField3 = new JTextField(properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE), 60);
        textField1.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, textField3.getText());
        });
        mappingPanel.add(textField3);
        JButton button3 = new JButton("Browse...");
        button1.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
                return;
            String value = fc.getSelectedFile().getAbsolutePath();
            textField3.setText(value);
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, textField3.getText());
        });
        mappingPanel.add(button3);
        exportPanel1.add(mappingPanel);
        exportPanel.add(exportPanel1, BorderLayout.NORTH);

        fieldColumns = 20;
        // add checked text fields
        JPanel exportPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //exportPanel2.add(checkedField("Generate HTML", ProjectProperties.EXPORT_HTML, ProjectProperties.EXPORT_MODEL_DIR, 50));
        //exportPanel2.add(labeledField("Mapping File", ProjectProperties.EXPORT_MAPPING_FILE, 50));
        exportPanel2.add(checkedField("Generate CMF", ProjectProperties.EXPORT_CMF, ProjectProperties.EXPORT_CMF_DIR, fieldColumns));
        exportPanel2.add(labeledField("CMF Version", ProjectProperties.EXPORT_CMF_VERSION, fieldColumns));
        exportPanel2.add(checkedField("Generate XML Schema", ProjectProperties.EXPORT_XSD, ProjectProperties.EXPORT_XSD_DIR, fieldColumns));
        exportPanel2.add(labeledField("Generate XML Examples", ProjectProperties.EXPORT_XML_DIR, fieldColumns));
        exportPanel2.add(checkedField("Generate JSON Schema", ProjectProperties.EXPORT_JSON, ProjectProperties.EXPORT_JSON_SCHEMA_DIR, fieldColumns));
        exportPanel2.add(labeledField("Generate JSON Examples", ProjectProperties.EXPORT_JSON_DIR, fieldColumns));
        exportPanel2.add(checkedField("Generate WSDL", ProjectProperties.EXPORT_WSDL, ProjectProperties.EXPORT_WSDL_DIR, fieldColumns));
        exportPanel2.add(checkedField("Generate OpenAPI", ProjectProperties.EXPORT_OPENAPI, ProjectProperties.EXPORT_OPENAPI_DIR, fieldColumns));
        exportPanel2.add(labeledField("Wantlist File", ProjectProperties.EXPORT_WANTLIST_FILE, fieldColumns));
        //ProjectProperties.IMPORT_CODE_DESCRIPTIONS
        exportPanel.add(exportPanel2, BorderLayout.CENTER);

        // add mapping button
        JPanel exportPanel3 = new JPanel();
        JButton mappingButton = commandButton("Import mapping file","import");
        exportPanel3.add(mappingButton);

        // add export button
        JButton exportButton = commandButton("Generate Specification","export");
        exportPanel3.add(exportButton);
        exportPanel.add(exportPanel3, BorderLayout.SOUTH);

        // Add external panel
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
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.setDefaultRenderer(Object.class, new LineWrapCellRenderer());
        JScrollPane scrollPanel = new JScrollPane(table);
        JButton namespaceButton = new JButton("Add namespace");
        namespaceButton.addActionListener((ActionEvent e) -> {
            DefaultTableModel model1 = (DefaultTableModel) table.getModel();
            model1.addRow(new String[]{"", "", ""});
        });
        externalPanel.add(namespaceButton, BorderLayout.SOUTH);
        externalPanel.add(scrollPanel, BorderLayout.CENTER);

        // add panels
        JTabbedPane dialogPanel = new JTabbedPane();
        dialogPanel.addTab(MAIN_TAB, mainPanel);
        dialogPanel.addTab(IMPORT_TAB, importPanel);
        dialogPanel.addTab(EXPORT_TAB, exportPanel);
        dialogPanel.addTab(METADATA_TAB, iepdPanel);
        dialogPanel.addTab(EXTERNAL_TAB, externalPanel);
        add(dialogPanel);

        // add frame button
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
            // DefaultTableModel model = table.getModel();
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
                    @SuppressWarnings("unused")
                    URL url2 = new URL(url);
                } catch (MalformedURLException e1) {
                    Log.trace("URL " + url + " is malformed");
                }
                if (prefix != null && !prefix.equals("") && namespace != null && !namespace.equals("")
                        && !url.equals(""))
                    externalSchemas2.add(prefix + "=" + namespace + "=" + url);
            }
            properties.setProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, String.join(",", externalSchemas2));

        } catch (RuntimeException e1) {
            Log.trace("ConfigurationDialog: exception " + e1.toString());
        }

        return command;
    }
}
