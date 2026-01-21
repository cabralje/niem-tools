package org.cabral.niemtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.bouml.UmlCom;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class AppController {

    private ProjectProperties properties;
    private fr.bouml.UmlPackage project;
    private NiemUmlModel model;

    @FXML
    private TextField ExportProjectDir;

    @FXML
    private TextField IEPDChangeLogFile;

    @FXML
    private TextField IEPDContact;

    @FXML
    private TextField IEPDEmail;

    @FXML
    private TextField IEPDLicense;

    @FXML
    private TextField IEPDName;

    @FXML
    private TextField IEPDOrganization;

    @FXML
    private TextField IEPDReadMeFile;

    @FXML
    private TextField IEPDStatus;

    @FXML
    private TextField IEPDVersion;

    @FXML
    private TextField ExportURI;

    @FXML
    private TextField ImportExcludeDomains;

    @FXML
    private TextField ImportIncludeDomains;

    @FXML
    private TextField ImportMaxFacets;

    @FXML
    private ComboBox<String> ImportNIEMVersion;

    @FXML
    private TableView<String[]> ExternalNamespaceTable;

    @FXML
    private TableColumn<String[], String> LocalPathColumn;

    @FXML
    private TableColumn<String[], String> NamespaceColumn;

    @FXML
    private TableColumn<String[], String> PrefixColumn;

    @FXML
    private TableColumn<String[], String> URLColumn;
    @FXML
    private Button importMapping;

    @FXML
    private VBox mainWindow;

    /**
     * JavaFX automatic initialization method - called after FXML loading.
     */
    @FXML
    public void initialize() {
    }

    /**
     * Initialize the controller with project properties, UML project, and model.
     * Sets the initial values of all text fields from the corresponding properties.
     * Must be called after FXML loading is complete.
     *
     * @param properties The ProjectProperties containing configuration values
     * @param project The UML project
     * @param model The NIEM UML model
     */
    public void initializeData(ProjectProperties properties, fr.bouml.UmlPackage project, NiemUmlModel model) {
        this.properties = properties;
        this.project = project;
        this.model = model;
        
        // Set text field values from properties
        ExportProjectDir.setText(properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR));
        IEPDChangeLogFile.setText(properties.getProperty(ProjectProperties.IEPD_CHANGE_LOG_FILE));
        IEPDContact.setText(properties.getProperty(ProjectProperties.IEPD_CONTACT));
        IEPDEmail.setText(properties.getProperty(ProjectProperties.IEPD_EMAIL));
        IEPDLicense.setText(properties.getProperty(ProjectProperties.IEPD_LICENSE_URL));
        IEPDName.setText(properties.getProperty(ProjectProperties.IEPD_NAME));
        IEPDOrganization.setText(properties.getProperty(ProjectProperties.IEPD_ORGANIZATION));
        IEPDReadMeFile.setText(properties.getProperty(ProjectProperties.IEPD_READ_ME_FILE));
        IEPDStatus.setText(properties.getProperty(ProjectProperties.IEPD_STATUS));
        IEPDVersion.setText(properties.getProperty(ProjectProperties.IEPD_VERSION));
        ExportURI.setText(properties.getProperty(ProjectProperties.EXPORT_URI));
        ImportExcludeDomains.setText(properties.getProperty(ProjectProperties.IMPORT_EXCLUDE_DOMAINS));
        ImportIncludeDomains.setText(properties.getProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS));
        ImportMaxFacets.setText(properties.getProperty(ProjectProperties.IMPORT_MAX_FACETS));
        
        // Add focus listeners to save properties when focus is lost (e.g., TAB key)
        ExportProjectDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, ExportProjectDir.getText());
        });
        IEPDChangeLogFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_CHANGE_LOG_FILE, IEPDChangeLogFile.getText());
        });
        IEPDContact.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_CONTACT, IEPDContact.getText());
        });
        IEPDEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_EMAIL, IEPDEmail.getText());
        });
        IEPDLicense.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_LICENSE_URL, IEPDLicense.getText());
        });
        IEPDName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_NAME, IEPDName.getText());
        });
        IEPDOrganization.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_ORGANIZATION, IEPDOrganization.getText());
        });
        IEPDReadMeFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_READ_ME_FILE, IEPDReadMeFile.getText());
        });
        IEPDStatus.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_STATUS, IEPDStatus.getText());
        });
        IEPDVersion.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IEPD_VERSION, IEPDVersion.getText());
        });
        ExportURI.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.EXPORT_URI, ExportURI.getText());
        });
        ImportExcludeDomains.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IMPORT_EXCLUDE_DOMAINS, ImportExcludeDomains.getText());
        });
        ImportIncludeDomains.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS, ImportIncludeDomains.getText());
        });
        ImportMaxFacets.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) properties.setProperty(ProjectProperties.IMPORT_MAX_FACETS, ImportMaxFacets.getText());
        });

        // Populate NIEM version dropdown
        String niemVersion = properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION);
        if (niemVersion != null && ImportNIEMVersion != null) {
            ImportNIEMVersion.setValue(niemVersion);
        }
        populateNiemVersionDropdown(ImportNIEMVersion, niemVersion);

        // Populate External Schemas table
        String externalSchemasProperty = properties.getProperty(ProjectProperties.EXPORT_EXTERNAL_SCHEMAS, "");
        String[] externalNamespaces = externalSchemasProperty.isEmpty() ? new String[0] : externalSchemasProperty.split(",");
        
        if (ExternalNamespaceTable != null && PrefixColumn != null) {
            // Set up editable text field cells for each column
            PrefixColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
            PrefixColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            PrefixColumn.setOnEditCommit(event -> event.getRowValue()[0] = event.getNewValue());
            
            NamespaceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
            NamespaceColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            NamespaceColumn.setOnEditCommit(event -> event.getRowValue()[1] = event.getNewValue());
            
            URLColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
            URLColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            URLColumn.setOnEditCommit(event -> event.getRowValue()[2] = event.getNewValue());
            
            LocalPathColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
            LocalPathColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            LocalPathColumn.setOnEditCommit(event -> event.getRowValue()[3] = event.getNewValue());
            
            ExternalNamespaceTable.getItems().clear();
            for (String namespace : externalNamespaces) {
                String[] parts = namespace.split("=");
                if (parts.length == 4) {
                    ExternalNamespaceTable.getItems().add(parts);
                }
            }
            ExternalNamespaceTable.setEditable(true);
        }
    }

    @FXML
    public void aboutNiemtools(ActionEvent event) {
        try {
            // Load the FXML file
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("AboutDialog.fxml"));
            javafx.scene.layout.VBox root = loader.load();
            
            // Get the controller and set the stage
            AboutDialogController controller = loader.getController();
            
            // Create a new stage for the dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("About NIEM Tools");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Get the parent stage and center the dialog on it
            // Replace the Node cast with MenuItem handling
            MenuItem menuItem = (MenuItem) event.getSource();
            Stage parentStage = (Stage) mainWindow.getScene().getWindow();
            stage.initOwner(parentStage);
            
            controller.setStage(stage);
            
            // Show the dialog
            stage.showAndWait();
        } catch (IOException e) {
            Log.trace("Failed to load About dialog: " + e.getMessage());
        }
    }

    @FXML
    public void addNamespace(ActionEvent event) {
        if (ExternalNamespaceTable != null) {
            ExternalNamespaceTable.getItems().add(new String[]{"", "", "", ""});
        }
    }

    @FXML
    public void close(ActionEvent event) {
        Platform.exit();
        UmlCom.bye(0);
        UmlCom.close();
        System.exit(0);
    }

    @FXML
    public void copyTextArea(ActionEvent event) {
    }

    @FXML
    public void exportMapping(ActionEvent event) {
    }

    @FXML
    public void importMapping(ActionEvent event) {
    }

    @FXML
    public void importReferenceModel(ActionEvent event) {
    }

    @FXML
    public void openPreferences(ActionEvent event) {
        try {
            // Load the FXML file
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("PreferencesDialog.fxml"));
                javafx.scene.control.DialogPane root = loader.load();
            
            // Get the controller and set the stage
            PreferencesDialogController controller = loader.getController();
            
             // Initialize controller data
            if (controller != null) {
                controller.initializeData(properties);
            } else {
                Log.trace("Warning: AppController is null after FXML loading");
                }

            // Create a new stage for the dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Preferences - NIEM Tools");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Get the parent stage and center the dialog on it
            // Replace the Node cast with MenuItem handling
            MenuItem menuItem = (MenuItem) event.getSource();
            Stage parentStage = (Stage) mainWindow.getScene().getWindow();
            stage.initOwner(parentStage);
            
            controller.setStage(stage);
            
            // Show the dialog
            stage.showAndWait();
        } catch (IOException e) {
            Log.trace("Failed to load Preferences dialog: " + e.getMessage());
        }
    }

    @FXML
    public void publishCMF(ActionEvent event) {
    }

    @FXML
    public void publishCodelists(ActionEvent event) {
    }

    @FXML
    public void publishHTML(ActionEvent event) {
    }

    @FXML
    public void publishJSON(ActionEvent event) {
    }

    @FXML
    public void publishWS(ActionEvent event) {
    }

    @FXML
    public void publishXSD(ActionEvent event) {
    }

    @FXML
    public void publishXSDModel(ActionEvent event) {
    }

    @FXML
    public void selectProjectDirectory(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        String currentDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
        String newDir = null;
        DirectoryChooser dc = new DirectoryChooser();
        if (currentDir != null && new File(currentDir).exists()) {
            dc.setInitialDirectory(new File(currentDir));
        }
        File selectedDir = dc.showDialog(stage);
        if (selectedDir != null) {
            newDir = selectedDir.getAbsolutePath();
        }
        if (newDir != null) {
            ExportProjectDir.setText(newDir);
            properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, newDir);
        }
    }

    @FXML
    public void selectTextArea(ActionEvent event) {
    }

    @FXML
    public void setProjectProperty(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        String property = source.getId();
        String value = source.getText();
        if (property != null && value != null && !property.isEmpty())
            properties.setProperty(property, value);
    }

    @FXML
    public void unselectTextArea(ActionEvent event) {
    }

    @FXML
    public void validateMapping(ActionEvent event) {
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
}
