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

/**
 * ConfigurationDialog is a modal dialog for configuring NIEMtools project properties.
 * <p>
 * This dialog provides a Swing-based user interface for editing various configuration
 * options related to NIEM reference model import, mapping, export, and metadata.
 * It organizes configuration options into multiple tabs, including:
 * <ul>
 *   <li>Home: Navigation to other configuration sections.</li>
 *   <li>Reference Model: Options for importing NIEM reference models and configuring domains/codes.</li>
 *   <li>Mapping: Project directory selection and mapping file management.</li>
 *   <li>Publish: Export options for CMF, XSD, JSON, and related settings.</li>
 *   <li>Metadata: Message specification metadata such as name, version, organization, etc.</li>
 *   <li>External Schemas: Management of external namespace mappings.</li>
 * </ul>
 * <p>
 * The dialog updates the provided {@link ProjectProperties} instance in real time as the user
 * modifies fields. It also supports command-based actions (e.g., import, publish) that can be
 * triggered by buttons, returning the selected command string via {@link #showDialog()}.
 * <p>
 * <b>Note:</b> This class is currently implemented using Swing. Consider porting to JavaFX for
 * improved UI capabilities and maintainability.
 *
 * @author James E. Cabral Jr.
 * @see ProjectProperties
 */
package org.cabral.niemtools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

// NOTE: Consider porting this Swing-based ConfigurationDialog to JavaFX for the following reasons:
// Benefits:
// - JavaFX provides a modern UI toolkit with better styling capabilities using CSS.
// - It supports advanced features like animations, WebView, and hardware acceleration.
// - JavaFX is more suitable for modern applications and is actively maintained.
// Challenges:
// - The migration requires rewriting the UI components and event handling logic.
// - JavaFX has a different threading model, requiring careful handling of UI updates.
// - Dependencies on Swing-specific components or libraries may need to be replaced or adapted.
class ConfigurationDialog extends JDialog {

    private final ProjectProperties properties;

    private String command = null;

    private static class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

        private final java.util.Map<String, Integer> heightCache = new java.util.HashMap<>();

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
            String cacheKey = row + ":" + column;
            int columnWidth = table.getColumnModel().getColumn(column).getWidth();
            setSize(columnWidth, getPreferredSize().height);

            int preferredHeight = heightCache.computeIfAbsent(cacheKey, key -> getPreferredSize().height);
            if (table.getRowHeight(row) != preferredHeight) {
                table.setRowHeight(row, preferredHeight);
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
        JTextField field = new JTextField(sanitize(properties.getProperty(property)), fieldColumns);
        field.getDocument().addDocumentListener((SimpleDocumentListener) (DocumentEvent e) -> {
            properties.setProperty(property, sanitize(field.getText()));
        });
        panel.add(field);
        return panel;
    }

    private JCheckBox checkedBox(String name, String boxProperty) {
        JCheckBox box = new JCheckBox(name, "true".equals(sanitize(properties.getProperty(boxProperty))));
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
            c = findParentTabbedPane(c);
            if (c instanceof JTabbedPane tabbedPane) {
                int index = tabbedPane.indexOfTab(tab);
                if (index != -1)
                    tabbedPane.setSelectedIndex(index);
            }
        });
        return button;
    }

    /**
     * Utility method to find the parent JTabbedPane of a given component.
     * @param component The starting component.
     * @return The parent JTabbedPane, or null if not found.
     */
    private JTabbedPane findParentTabbedPane(Component component) {
        while (component != null && !(component instanceof JTabbedPane)) {
            component = component.getParent();
        }
        return (JTabbedPane) component;
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
        setSize(850,350);

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
        importPanel.add(navigationButton("Configure External Schemas", EXTERNAL_TAB), BorderLayout.NORTH);

        // import options
        JPanel importPanel1 = new JPanel(new BorderLayout());
        importPanel1.add(label("Version"), BorderLayout.NORTH);

        // Default to previously selected version
        String selectedVersion = properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION);
        if (selectedVersion == null || selectedVersion.isEmpty())
            selectedVersion = "Loading...";
        JComboBox<String> niemVersionDropdown = new JComboBox<>(new String[] {selectedVersion});

        // Populate the dropdown asynchronously
        populateNiemVersionDropdown(niemVersionDropdown, selectedVersion);

        // Store the selected version in properties when changed
        //niemVersionDropdown.addActionListener(e -> {
        //    String selected = (String) niemVersionDropdown.getSelectedItem();
        //    if (selected != null && !selected.equals("Loading...") && !selected.equals("Failed to load versions"))
        //        properties.setProperty(ProjectProperties.IMPORT_NIEM_VERSION, selected);
        //});
        
        importPanel1.add(niemVersionDropdown, BorderLayout.CENTER);

        // import button
        JButton importButton = commandButton("Import NIEM Reference Model","importReferenceModel");
        importButton.addActionListener((ActionEvent e) -> {
            String selected = (String) niemVersionDropdown.getSelectedItem();
            if (selected != null && !selected.equals("Loading...") && !selected.equals("Failed to load versions"))
                properties.setProperty(ProjectProperties.IMPORT_NIEM_VERSION, selected);
        });
        importPanel.add(importButton, BorderLayout.SOUTH);
        importPanel.add(importPanel1, BorderLayout.WEST);

        int fieldColumns = 20;
        JPanel importPanel2 = new JPanel(new BorderLayout());
        importPanel2.add(label("Domains"), BorderLayout.NORTH);
        importPanel2.add(labeledField("Include", ProjectProperties.IMPORT_INCLUDE_DOMAINS, fieldColumns), BorderLayout.CENTER);
        importPanel2.add(labeledField("Exclude", ProjectProperties.IMPORT_EXCLUDE_DOMAINS, fieldColumns), BorderLayout.SOUTH);
        importPanel.add(importPanel2, BorderLayout.CENTER);

        JPanel importPanel3 = new JPanel(new BorderLayout());
        importPanel3.add(label("Codes"), BorderLayout.NORTH);
        importPanel3.add(labeledField("Exclude", ProjectProperties.IMPORT_EXCLUDE_CODES, fieldColumns), BorderLayout.CENTER);
        importPanel3.add(labeledField("Maximum facets", ProjectProperties.IMPORT_MAX_FACETS, fieldColumns), BorderLayout.SOUTH);
        importPanel.add(importPanel3, BorderLayout.EAST);
 
        // mapping panel
        JPanel mappingPanel = new JPanel(new BorderLayout());

        // project directory
        JPanel projectPanel = new JPanel(new BorderLayout());
        JPanel projectPanel1 = new JPanel();
        projectPanel1.add(new JLabel("Project Directory", JLabel.CENTER));
        JTextField textField1 = new JTextField(properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR), 60);
        textField1.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            String projectDir = textField1.getText();
            properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, projectDir);
            String htmlDir = projectDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_HTML_DIR);
            properties.setProperty(ProjectProperties.EXPORT_HTML_DIR, htmlDir);
            String mappingFile = projectDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MAPPING_FILE);
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, mappingFile);
        });
            //properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, modelDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        projectPanel1.add(textField1);
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
        projectPanel1.add(button1);
        projectPanel.add(projectPanel1, BorderLayout.NORTH);
        projectPanel.add(labeledField("Documentation", ProjectProperties.EXPORT_HTML_DIR, 60), BorderLayout.CENTER);
        projectPanel.add(labeledField("Mapping File", ProjectProperties.EXPORT_MAPPING_FILE, 60), BorderLayout.SOUTH);
        mappingPanel.add(projectPanel, BorderLayout.NORTH);

        // publish UML panel
        mappingPanel.add(commandButton("Publish UML Model","publishUML"), BorderLayout.WEST);

        // import mapping panel
        mappingPanel.add(commandButton("Import Mapping File","importMapping"), BorderLayout.CENTER);

        // validate NIEM panel
        mappingPanel.add(commandButton("Validate NIEM Mapping","validateMapping"), BorderLayout.EAST);

        // export panel
        JPanel exportPanel = new JPanel(new BorderLayout());

        // uri field
        JPanel uriPanel = labeledField("URI for extensions", ProjectProperties.EXPORT_URI, fieldColumns);
        exportPanel.add(uriPanel, BorderLayout.NORTH);
 
        // publish CMF
        JPanel exportPanel1 = new JPanel(new BorderLayout());
        exportPanel1.add(label("Models: Common Model Format (CMF) and XSD"), BorderLayout.NORTH);
        JPanel exportPanel1a = new JPanel(new BorderLayout());
        JPanel exportPanel1b = new JPanel(new BorderLayout());
        exportPanel1b.add(labeledField("CMF File", ProjectProperties.EXPORT_CMF_FILE, fieldColumns), BorderLayout.NORTH);
        exportPanel1b.add(labeledField("XSD Directory", ProjectProperties.EXPORT_XSD_MODEL_DIR, fieldColumns), BorderLayout.CENTER);
        //exportPanel1b.add(labeledField("Version", ProjectProperties.EXPORT_CMF_VERSION, fieldColumns), BorderLayout.SOUTH);
        exportPanel1b.add(labeledField("cmftool", ProjectProperties.EXPORT_CMFTOOL_TO_XSD_MODEL, fieldColumns), BorderLayout.SOUTH);
        JPanel exportPanel1c = new JPanel(new BorderLayout());
        exportPanel1c.add(checkedBox("Include CMF in Message Specification", ProjectProperties.EXPORT_CMF), BorderLayout.CENTER);
        exportPanel1c.add(checkedBox("Use cmftool to generate XSD model from CMF", ProjectProperties.EXPORT_CMF_TO_XSD_MODEL), BorderLayout.SOUTH);
        exportPanel1a.add(exportPanel1b, BorderLayout.NORTH);
        exportPanel1a.add(exportPanel1c, BorderLayout.SOUTH);      
        exportPanel1.add(exportPanel1a, BorderLayout.CENTER);
        exportPanel1.add(commandButton("Publish CMF/XSD Models","publishCMF"), BorderLayout.SOUTH);
        exportPanel.add(exportPanel1, BorderLayout.WEST);

        // publish XSD
        JPanel exportPanel2 = new JPanel(new BorderLayout());
        exportPanel2.add(label("Messages: XML Schemas"), BorderLayout.NORTH);
        JPanel exportPanel2a = new JPanel(new BorderLayout());
        JPanel exportPanel2b = new JPanel(new BorderLayout());
        exportPanel2b.add(labeledField("Directory", ProjectProperties.EXPORT_XSD_DIR, fieldColumns), BorderLayout.NORTH);
        exportPanel2b.add(labeledField("Wantlist File", ProjectProperties.EXPORT_WANTLIST_FILE, fieldColumns), BorderLayout.CENTER);
        exportPanel2b.add(labeledField("cmffool", ProjectProperties.EXPORT_CMFTOOL_TO_XSD, fieldColumns), BorderLayout.SOUTH);
        JPanel exportPanel2c = new JPanel(new BorderLayout());
        exportPanel2c.add(checkedBox("Include XSD in Message Specification", ProjectProperties.EXPORT_XSD), BorderLayout.NORTH);
        exportPanel2c.add(checkedBox("Include WSDL in Message Specification", ProjectProperties.EXPORT_WSDL), BorderLayout.CENTER);
        exportPanel2c.add(checkedBox("Use cmftool to generate XSDs from CMF", ProjectProperties.EXPORT_CMF_TO_XSD), BorderLayout.SOUTH);       
        exportPanel2a.add(exportPanel2b, BorderLayout.NORTH);
        exportPanel2a.add(exportPanel2c, BorderLayout.SOUTH);
        exportPanel2.add(exportPanel2a, BorderLayout.CENTER);
        exportPanel2.add(commandButton("Publish XSD Message Schemas","publishXSD"), BorderLayout.SOUTH);
        exportPanel.add(exportPanel2, BorderLayout.CENTER);

        // publish JSON
        JPanel exportPanel3 = new JPanel(new BorderLayout());
        exportPanel3.add(label("Messages: JSON Schema"), BorderLayout.NORTH);
        JPanel exportPanel3a = new JPanel(new BorderLayout());
        JPanel exportPanel3b = new JPanel(new BorderLayout());
        //exportPanel3b.add(labeledField("Directory", ProjectProperties.EXPORT_JSON_SCHEMA_DIR, fieldColumns), BorderLayout.NORTH);
        exportPanel3b.add(labeledField("Schema File", ProjectProperties.EXPORT_JSON_SCHEMA_FILE, fieldColumns), BorderLayout.NORTH);
        exportPanel3b.add(labeledField("cmftool", ProjectProperties.EXPORT_CMFTOOL_TO_JSON, fieldColumns), BorderLayout.SOUTH);
        JPanel exportPanel3c = new JPanel(new BorderLayout());
        exportPanel3c.add(checkedBox("Include JSON in Message Specification", ProjectProperties.EXPORT_JSON), BorderLayout.NORTH);
        exportPanel3c.add(checkedBox("Include OpenAPI in Message Specification", ProjectProperties.EXPORT_OPENAPI), BorderLayout.CENTER);
        exportPanel3c.add(checkedBox("Use cmftool to generate JSON schema from  CMF", ProjectProperties.EXPORT_CMF_TO_JSON), BorderLayout.SOUTH);
        exportPanel3a.add(exportPanel3b, BorderLayout.NORTH);
        exportPanel3a.add(exportPanel3c, BorderLayout.SOUTH);
        exportPanel3.add(exportPanel3a, BorderLayout.CENTER);
        exportPanel3.add(commandButton("Publish JSON Schema Models","publishJSON"), BorderLayout.SOUTH);
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
        String externalSchemasProperty = properties.getProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, "");
        String[] externalNamespaces = externalSchemasProperty.isEmpty() ? new String[0] : externalSchemasProperty.split(",");
        // external schemas panel
        JPanel externalPanel = new JPanel(new BorderLayout());
        //String[] externalNamespaces = properties.getProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS).split(",");
        int row = 0;
        String[][] data = null;
        String[] columnNames = {"Prefix", "Namespace", "URL"};
        if (externalNamespaces != null) {
            data = new String[externalNamespaces.length][3];
            for (String namespace : externalNamespaces) {
                String[] parts = namespace.split("=");
                if (parts.length == 3)
                    data[row++] = parts;
            }

            if (data.length > 0 && data[0].length != columnNames.length)
                throw new IllegalArgumentException("Data column count does not match the expected column structure.");
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
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

        // When reading from the table, sanitize user input before using/storing
        try {
            LinkedHashSet<String> externalSchemas2 = new LinkedHashSet<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String prefix = "", namespace = "", url = "";
                Object prefixValue = model.getValueAt(i, 0);
                if (prefixValue != null)
                    prefix = sanitize(prefixValue.toString());
                Object namespaceValue = model.getValueAt(i, 1);
                if (namespaceValue != null)
                    namespace = sanitize(namespaceValue.toString());
                Object urlValue = model.getValueAt(i, 2);
                if (urlValue != null)
                    url = sanitize(urlValue.toString());
                if (url.startsWith("http"))
                    try {
                        URI uri = new URI(url);
                        uri.toURL();
                    } catch (MalformedURLException | URISyntaxException e1) {
                        Log.trace("URL " + url + " is malformed" + e1.getMessage());
                        throw new IllegalArgumentException("URL " + url + " is malformed", e1); 
                    }
                if (prefix != null && !prefix.isEmpty() && namespace != null && !namespace.isEmpty()
                        && !url.isEmpty())
                    externalSchemas2.add(prefix + "=" + namespace + "=" + url);
            }
            properties.setProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, String.join(",", externalSchemas2));
        } catch (IllegalArgumentException | IllegalStateException e1) {
            Log.trace("ConfigurationDialog: exception " + e1.toString());
            throw e1; // Rethrow the exception after logging
        }

        return command;
    }

    /**
     * Fetches the list of NIEM versions from GitHub tags API.
     * Uses built-in JSON parsing (no external dependencies).
     */
    private void populateNiemVersionDropdown(JComboBox<String> comboBox, String selectedVersion) {
        Log.debug("Starting NIEM version fetch..."); // Debug
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                Log.debug("doInBackground() started"); // Debug
                List<String> versions = new ArrayList<>();
                
                try {
                    URL url = URI.create("https://api.github.com/repos/niemopen/niem-model/tags").toURL();
                    Log.debug("Connecting to: " + url);
                    
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    conn.setRequestProperty("User-Agent", "NIEM-Tools/1.0");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    
                    int responseCode = conn.getResponseCode();
                    Log.debug("HTTP Response Code: " + responseCode);
                    
                    if (responseCode == 200) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            String json = sb.toString();
                            Log.debug("JSON Response length: " + json.length());
                            
                            // Simple JSON parsing without external libraries
                            versions = parseVersionsFromJson(json);
                            Log.debug("Found " + versions.size() + " versions");
                        }
                    } else {
                        Log.debug("HTTP Error: " + responseCode);
                    }
                } catch (IOException e) {
                    Log.trace("Timeout populating NIEM versions from niem-model GitHub repo: " + e.getMessage());
                    //e.printStackTrace();
                    //throw e;
                    versions.add("6.0");
                }
                
                return versions;
            }
            
            @Override
            protected void done() {
                Log.debug("done() called"); // Debug
                try {
                    List<String> versions = get();
                    comboBox.removeAllItems();
                    if (versions == null || versions.isEmpty()) {
                        comboBox.addItem("No versions found");
                    } else {
                            Log.debug("Retrieved " + versions.size() + " versions in done()");
                        for (String version : versions) {
                            comboBox.addItem(version);
                            Log.debug("Added to combo: " + version);
                        }
                    }
                    // Set the selected item to the item named selectedVersion, if present
                    if (selectedVersion != null && !selectedVersion.isEmpty()) {
                        for (int i = 0; i < comboBox.getItemCount(); i++) {
                            if (selectedVersion.equals(comboBox.getItemAt(i))) {
                                comboBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    comboBox.repaint();
                } catch (InterruptedException | ExecutionException e) {
                    Log.debug("Exception in done(): " + e.getMessage());
                    //e.printStackTrace();
                    comboBox.removeAllItems();
                    comboBox.addItem("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Simple JSON parsing to extract version names from GitHub tags API response.
     * Looks for "name" fields in the JSON array.
     */
    private List<String> parseVersionsFromJson(String json) {
        List<String> versions = new ArrayList<>();
        
        // Simple regex-based parsing for "name": "version_string"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            String version = matcher.group(1);
            versions.add(version);
            Log.debug("Parsed version: " + version);
        }
        
        return versions;
    }

    /**
     * Returns the sanitized command string.
     * @return
     */
    // Add a sanitization helper method
    private String sanitize(String input) {
        if (input == null) return "";
        // OWASP guidance: Remove control characters except for line breaks, tabs, and path separators, then trim whitespace.
        // Allow both Unix (/) and Windows (\) path separators.
        // Remove non-printable and dangerous characters, and normalize Unicode.
        String sanitized = input
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "") // Remove control chars except \r, \n, \t
            .replaceAll("[<>\"'`]", "")                // Remove HTML/script injection chars
            .replaceAll("[\\p{C}]", "")                // Remove other non-printable chars
            .replaceAll("(?<![\\\\/])[\\\\/](?![\\\\/])", "/") // Normalize single separators
            .trim();
        // Normalize Unicode to prevent homoglyph attacks
        sanitized = java.text.Normalizer.normalize(sanitized, java.text.Normalizer.Form.NFKC);
        return sanitized;
    }
}
