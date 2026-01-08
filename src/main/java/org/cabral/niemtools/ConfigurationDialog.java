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
 * This dialog provides a JavaFX-based user interface for editing various configuration
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
 * <b>Note:</b> This class has been ported to JavaFX for improved UI capabilities and maintainability.
 *
 * @author James E. Cabral Jr.
 * @see ProjectProperties
 */
package org.cabral.niemtools;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JavaFX-based Configuration Dialog for NIEMtools
 */
class ConfigurationDialog {

    private final ProjectProperties properties;
    private String command = null;
    private Stage stage;
    private TabPane tabPane;

    private static final String MAIN_TAB = "Home";
    private static final String IMPORT_TAB = "Reference Model";
    private static final String MAPPING_TAB = "Mapping";
    private static final String EXPORT_TAB = "Publish";
    private static final String METADATA_TAB = "Metadata";
    private static final String EXTERNAL_TAB = "External Schemas";

    /**
     * Constructor that initializes the configuration dialog with the given properties.
     * @param inputProperties
     */
    ConfigurationDialog(ProjectProperties inputProperties) {
        properties = inputProperties;
        // Stage and TabPane must be created on the JavaFX Application Thread.
        // They will be initialized in showDialog().
    }

    String showDialog() {
        // Ensure Stage is created on the FX Application Thread
        if (stage == null) {
            CompletableFuture<Void> initFuture = new CompletableFuture<>();
            Runnable initStage = () -> {
                stage = new Stage();
                stage.setTitle("Niemtools Configuration");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setWidth(850);
                stage.setHeight(350);
                tabPane = new TabPane();
                initFuture.complete(null);
            };
            if (Platform.isFxApplicationThread()) {
                initStage.run();
            } else {
                Platform.runLater(initStage);
            }
            try {
                initFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Failed to initialize JavaFX stage", e);
            }
        }
        // Overview panel
        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(10));
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.getChildren().addAll(
            navigationButton("Import Reference Model", IMPORT_TAB),
            navigationButton("Map to NIEM", MAPPING_TAB),
            navigationButton("Publish NIEM schemas", EXPORT_TAB),
            navigationButton("Publish NIEM message specification", EXPORT_TAB),
            navigationButton("Configure message specification metadata", METADATA_TAB)
        );

        // Import panel
        VBox importPanel = new VBox(10);
        importPanel.setPadding(new Insets(10));
        importPanel.getChildren().add(navigationButton("Configure External Schemas", EXTERNAL_TAB));

        // Version selection
        VBox importPanel1 = new VBox(5);
        importPanel1.getChildren().add(label("Version"));
        
        String selectedVersion = properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION);
        if (selectedVersion == null || selectedVersion.isEmpty())
            selectedVersion = "Loading...";
        ComboBox<String> niemVersionDropdown = new ComboBox<>();
        niemVersionDropdown.getItems().add(selectedVersion);
        niemVersionDropdown.setValue(selectedVersion);
        
        populateNiemVersionDropdown(niemVersionDropdown, selectedVersion);
        
        importPanel1.getChildren().add(niemVersionDropdown);

        Button importButton = commandButton("Import NIEM Reference Model", "importReferenceModel");
        importButton.setOnAction(e -> {
            String selected = niemVersionDropdown.getValue();
            if (selected != null && !selected.equals("Loading...") && !selected.equals("Failed to load versions"))
                properties.setProperty(ProjectProperties.IMPORT_NIEM_VERSION, selected);
            command = "importReferenceModel";
            stage.close();
        });

        HBox importRow = new HBox(10, importPanel1);
        importRow.setPadding(new Insets(10));

        int fieldColumns = 20;
        VBox importPanel2 = new VBox(5);
        importPanel2.getChildren().addAll(
            label("Domains"),
            labeledField("Include", ProjectProperties.IMPORT_INCLUDE_DOMAINS, fieldColumns),
            labeledField("Exclude", ProjectProperties.IMPORT_EXCLUDE_DOMAINS, fieldColumns)
        );

        VBox importPanel3 = new VBox(5);
        importPanel3.getChildren().addAll(
            label("Codes"),
            labeledField("Exclude", ProjectProperties.IMPORT_EXCLUDE_CODES, fieldColumns),
            labeledField("Maximum facets", ProjectProperties.IMPORT_MAX_FACETS, fieldColumns)
        );

        HBox importContent = new HBox(10, importRow, importPanel2, importPanel3);
        importPanel.getChildren().addAll(importContent, importButton);

        // Mapping panel
        VBox mappingPanel = new VBox(10);
        mappingPanel.setPadding(new Insets(10));

        VBox projectPanel = new VBox(5);
        TextField projectDirField = new TextField(properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR));
        projectDirField.setPrefColumnCount(60);
        
        TextField htmlDirField = new TextField(properties.getProperty(ProjectProperties.EXPORT_HTML_DIR));
        htmlDirField.setPrefColumnCount(60);
        htmlDirField.setEditable(false); // Make read-only since it's derived from projectDir
        
        TextField mappingFileField = new TextField(properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        mappingFileField.setPrefColumnCount(60);
        mappingFileField.setEditable(false); // Make read-only since it's derived from projectDir
        
        // Update derived fields when project directory changes
        projectDirField.textProperty().addListener((obs, oldVal, newVal) -> {
            String projectDir = sanitize(newVal);
            properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, projectDir);
            String htmlDir = projectDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_HTML_DIR);
            properties.setProperty(ProjectProperties.EXPORT_HTML_DIR, htmlDir);
            htmlDirField.setText(htmlDir);
            String mappingFile = projectDir + File.separator + ProjectProperties.getDefaults().getProperty(ProjectProperties.EXPORT_MAPPING_FILE);
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, mappingFile);
            mappingFileField.setText(mappingFile);
        });
        
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            String currentDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
            if (currentDir != null && new File(currentDir).exists()) {
                dc.setInitialDirectory(new File(currentDir));
            }
            File selectedDir = dc.showDialog(stage);
            if (selectedDir != null) {
                projectDirField.setText(selectedDir.getAbsolutePath());
            }
        });

        HBox projectDirRow = new HBox(10, new Label("Project Directory"), projectDirField, browseButton);
        HBox htmlDirRow = new HBox(10, new Label("Documentation"), htmlDirField);
        HBox mappingFileRow = new HBox(10, new Label("Mapping File"), mappingFileField);
        
        projectPanel.getChildren().addAll(projectDirRow, htmlDirRow, mappingFileRow);

        HBox mappingButtons = new HBox(10);
        mappingButtons.getChildren().addAll(
            commandButton("Publish UML Model", "publishUML"),
            commandButton("Import Mapping File", "importMapping"),
            commandButton("Validate NIEM Mapping", "validateMapping")
        );

        mappingPanel.getChildren().addAll(projectPanel, mappingButtons);

        // Export panel
        VBox exportPanel = new VBox(10);
        exportPanel.setPadding(new Insets(10));
        
        HBox uriRow = new HBox(10);
        uriRow.getChildren().addAll(labeledField("URI for extensions", ProjectProperties.EXPORT_URI, fieldColumns));
        exportPanel.getChildren().add(uriRow);

        // CMF/XSD Model
        VBox exportPanel1 = new VBox(5);
        exportPanel1.getChildren().add(label("Models: Common Model Format (CMF) and XSD"));
        exportPanel1.getChildren().addAll(
            labeledField("CMF File", ProjectProperties.EXPORT_CMF_FILE, fieldColumns),
            labeledField("XSD Directory", ProjectProperties.EXPORT_XSD_MODEL_DIR, fieldColumns),
            labeledField("cmftool", ProjectProperties.EXPORT_CMFTOOL_TO_XSD_MODEL, fieldColumns),
            checkedBox("Include CMF in Message Specification", ProjectProperties.EXPORT_CMF),
            checkedBox("Use cmftool to generate XSD model from CMF", ProjectProperties.EXPORT_CMF_TO_XSD_MODEL),
            commandButton("Publish CMF/XSD Models", "publishCMF")
        );

        // XSD Messages
        VBox exportPanel2 = new VBox(5);
        exportPanel2.getChildren().add(label("Messages: XML Schemas"));
        exportPanel2.getChildren().addAll(
            labeledField("Directory", ProjectProperties.EXPORT_XSD_DIR, fieldColumns),
            labeledField("Wantlist File", ProjectProperties.EXPORT_WANTLIST_FILE, fieldColumns),
            labeledField("cmftool", ProjectProperties.EXPORT_CMFTOOL_TO_XSD, fieldColumns),
            checkedBox("Include XSD in Message Specification", ProjectProperties.EXPORT_XSD),
            checkedBox("Include WSDL in Message Specification", ProjectProperties.EXPORT_WSDL),
            checkedBox("Use cmftool to generate XSDs from CMF", ProjectProperties.EXPORT_CMF_TO_XSD),
            commandButton("Publish XSD Message Schemas", "publishXSD")
        );

        // JSON
        VBox exportPanel3 = new VBox(5);
        exportPanel3.getChildren().add(label("Messages: JSON Schema"));
        exportPanel3.getChildren().addAll(
            labeledField("Schema File", ProjectProperties.EXPORT_JSON_SCHEMA_FILE, fieldColumns),
            labeledField("cmftool", ProjectProperties.EXPORT_CMFTOOL_TO_JSON, fieldColumns),
            checkedBox("Include JSON in Message Specification", ProjectProperties.EXPORT_JSON),
            checkedBox("Include OpenAPI in Message Specification", ProjectProperties.EXPORT_OPENAPI),
            checkedBox("Use cmftool to generate JSON schema from CMF", ProjectProperties.EXPORT_CMF_TO_JSON),
            commandButton("Publish JSON Schema Models", "publishJSON")
        );

        HBox exportContent = new HBox(10, exportPanel1, exportPanel2, exportPanel3);
        Button publishSpecButton = commandButton("Publish NIEM Message Specification", "publishSpecification");
        exportPanel.getChildren().addAll(exportContent, publishSpecButton);

        // Metadata panel
        HBox metadataPanel = new HBox(10);
        metadataPanel.setPadding(new Insets(10));
        
        VBox metadataPanel1 = new VBox(5);
        metadataPanel1.getChildren().addAll(
            labeledField("Name", ProjectProperties.IEPD_NAME, fieldColumns),
            labeledField("Version", ProjectProperties.IEPD_VERSION, fieldColumns),
            labeledField("Status", ProjectProperties.IEPD_STATUS, fieldColumns)
        );

        VBox metadataPanel2 = new VBox(5);
        metadataPanel2.getChildren().addAll(
            labeledField("Organization", ProjectProperties.IEPD_ORGANIZATION, fieldColumns),
            labeledField("Contact", ProjectProperties.IEPD_CONTACT, fieldColumns),
            labeledField("Email", ProjectProperties.IEPD_EMAIL, fieldColumns)
        );

        VBox metadataPanel3 = new VBox(5);
        metadataPanel3.getChildren().addAll(
            labeledField("License URL", ProjectProperties.IEPD_LICENSE_URL, fieldColumns),
            labeledField("ChangeLog", ProjectProperties.IEPD_CHANGE_LOG_FILE, fieldColumns),
            labeledField("Readme", ProjectProperties.IEPD_READ_ME_FILE, fieldColumns)
        );

        metadataPanel.getChildren().addAll(metadataPanel1, metadataPanel2, metadataPanel3);

        // External schemas panel
        VBox externalPanel = new VBox(10);
        externalPanel.setPadding(new Insets(10));
        
        String externalSchemasProperty = properties.getProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, "");
        String[] externalNamespaces = externalSchemasProperty.isEmpty() ? new String[0] : externalSchemasProperty.split(",");
        
        TableView<String[]> table = new TableView<>();
        table.setEditable(true);
        
        TableColumn<String[], String> prefixCol = new TableColumn<>("Prefix");
        prefixCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[0]));
        prefixCol.setCellFactory(TextFieldTableCell.forTableColumn());
        prefixCol.setOnEditCommit(event -> event.getRowValue()[0] = event.getNewValue());
        prefixCol.setMaxWidth(50);
        
        TableColumn<String[], String> namespaceCol = new TableColumn<>("Namespace");
        namespaceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[1]));
        namespaceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        namespaceCol.setOnEditCommit(event -> event.getRowValue()[1] = event.getNewValue());
        
        TableColumn<String[], String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[2]));
        urlCol.setCellFactory(TextFieldTableCell.forTableColumn());
        urlCol.setOnEditCommit(event -> event.getRowValue()[2] = event.getNewValue());
        
        TableColumn<String[], String> localPathCol = new TableColumn<>("LocalPath");
        localPathCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()[3]));
        localPathCol.setCellFactory(TextFieldTableCell.forTableColumn());
        localPathCol.setOnEditCommit(event -> event.getRowValue()[3] = event.getNewValue());
        
        table.getColumns().addAll(prefixCol, namespaceCol, urlCol, localPathCol);
        
        for (String namespace : externalNamespaces) {
            String[] parts = namespace.split("=");
            if (parts.length == 4) {
                table.getItems().add(parts);
            }
        }
        
        Button addNamespaceButton = new Button("Add namespace");
        addNamespaceButton.setOnAction(e -> table.getItems().add(new String[]{"", "", "", ""}));
        
        externalPanel.getChildren().addAll(table, addNamespaceButton);

        // Create tabs
        tabPane.getTabs().addAll(
            createTab(MAIN_TAB, mainPanel),
            createTab(IMPORT_TAB, importPanel),
            createTab(MAPPING_TAB, mappingPanel),
            createTab(EXPORT_TAB, new ScrollPane(exportPanel)),
            createTab(METADATA_TAB, metadataPanel),
            createTab(EXTERNAL_TAB, externalPanel)
        );

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> stage.close());

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(okButton);
        BorderPane.setAlignment(okButton, Pos.CENTER);
        BorderPane.setMargin(okButton, new Insets(10));

        Scene scene = new Scene(root);
        // Set scene on FX thread
        if (Platform.isFxApplicationThread()) {
            stage.setScene(scene);
        } else {
            CompletableFuture<Void> setSceneFuture = new CompletableFuture<>();
            Platform.runLater(() -> { stage.setScene(scene); setSceneFuture.complete(null); });
            try {
                setSceneFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Show and wait
        if (Platform.isFxApplicationThread()) {
            stage.showAndWait();
        } else {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                stage.showAndWait();
                future.complete(null);
            });
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Process external schemas table
        try {
            LinkedHashSet<String> externalSchemas2 = new LinkedHashSet<>();
            for (String[] row : table.getItems()) {
                String prefix = sanitize(row[0]);
                String namespace = sanitize(row[1]);
                String url = sanitize(row[2]);
                String localPath = sanitize(row[3]);
                
                if (url.startsWith("http")) {
                    try {
                        URI uri = new URI(url);
                        uri.toURL();
                    } catch (MalformedURLException | URISyntaxException e1) {
                        Log.trace("URL " + url + " is malformed: " + e1.getMessage());
                        throw new IllegalArgumentException("URL " + url + " is malformed", e1);
                    }
                }
                
                if (prefix != null && !prefix.isEmpty() && namespace != null && !namespace.isEmpty()
                        && !url.isEmpty() && !localPath.isEmpty()) {
                    externalSchemas2.add(prefix + "=" + namespace + "=" + url + "=" + localPath);
                }
            }
            properties.setProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, String.join(",", externalSchemas2));
        } catch (IllegalArgumentException | IllegalStateException e1) {
            Log.trace("ConfigurationDialog: exception " + e1.toString());
            throw e1;
        }

        return command;
    }

    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    private Label label(String name) {
        Label label = new Label(name);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private HBox labeledField(String name, String property, int fieldColumns) {
        HBox panel = new HBox(5);
        panel.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(name);
        lbl.setMinWidth(100);
        TextField field = new TextField(sanitize(properties.getProperty(property)));
        field.setPrefColumnCount(fieldColumns);
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            properties.setProperty(property, sanitize(newVal));
        });
        panel.getChildren().addAll(lbl, field);
        return panel;
    }

    private CheckBox checkedBox(String name, String boxProperty) {
        CheckBox box = new CheckBox(name);
        box.setSelected("true".equals(sanitize(properties.getProperty(boxProperty))));
        box.selectedProperty().addListener((obs, oldVal, newVal) -> {
            properties.setProperty(boxProperty, String.valueOf(newVal));
        });
        return box;
    }

    private Button navigationButton(String name, String tab) {
        Button button = new Button(name);
        button.setOnAction(e -> {
            for (Tab t : tabPane.getTabs()) {
                if (t.getText().equals(tab)) {
                    tabPane.getSelectionModel().select(t);
                    break;
                }
            }
        });
        return button;
    }

    private Button commandButton(String name, String cmd) {
        Button button = new Button(name);
        button.setOnAction(e -> {
            command = cmd;
            stage.close();
        });
        return button;
    }

    private void populateNiemVersionDropdown(ComboBox<String> comboBox, String selectedVersion) {
        Log.debug("Starting NIEM version fetch...");
        
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                Log.debug("Fetching versions in background task");
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
                            
                            versions = parseVersionsFromJson(json);
                            Log.debug("Found " + versions.size() + " versions");
                        }
                    } else {
                        Log.debug("HTTP Error: " + responseCode);
                    }
                } catch (IOException e) {
                    Log.trace("Timeout populating NIEM versions from niem-model GitHub repo: " + e.getMessage());
                    versions.add("6.0");
                }
                
                return versions;
            }
        };
        
        task.setOnSucceeded(event -> {
            Log.debug("Task succeeded");
            List<String> versions = task.getValue();
            comboBox.getItems().clear();
            if (versions == null || versions.isEmpty()) {
                comboBox.getItems().add("No versions found");
            } else {
                Log.debug("Retrieved " + versions.size() + " versions");
                comboBox.getItems().addAll(versions);
            }
            
            if (selectedVersion != null && !selectedVersion.isEmpty()) {
                for (String version : comboBox.getItems()) {
                    if (selectedVersion.equals(version)) {
                        comboBox.setValue(version);
                        break;
                    }
                }
            }
        });
        
        task.setOnFailed(event -> {
            Log.debug("Task failed: " + task.getException().getMessage());
            comboBox.getItems().clear();
            comboBox.getItems().add("Error: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }

    private List<String> parseVersionsFromJson(String json) {
        List<String> versions = new ArrayList<>();
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            String version = matcher.group(1);
            versions.add(version);
            Log.debug("Parsed version: " + version);
        }
        
        return versions;
    }

    private String sanitize(String input) {
        if (input == null) return "";
        String sanitized = input
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
            .replaceAll("[<>\"'`]", "")
            .replaceAll("[\\p{C}]", "")
            .replaceAll("(?<![\\\\/])[\\\\/](?![\\\\/])", "/")
            .trim();
        sanitized = java.text.Normalizer.normalize(sanitized, java.text.Normalizer.Form.NFKC);
        return sanitized;
    }
}
