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
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class AppController {

    private ProjectProperties properties;
    private UmlPackage project;
    private UmlPackage umlPackage;
    private UmlPackage messagePackage;
    private UmlPackage baseTypesPackage;
    private NiemUmlModel model;
    private CmfToolAdapter cmftool;

    
    @FXML
    private ListView<String> DomainListView;

    @FXML
    private TextField ExportProjectDir;

    @FXML
    private TextField ExportURI;

    @FXML
    private TableView<String[]> ExternalNamespaceTable;

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
    private CheckBox ImportCodeDescriptions;
/*
    @FXML
    private TextField ImportExcludeDomains;

     @FXML
    private TextField ImportExcludeCodes;

    @FXML
    private TextField ImportIncludeCodes;

    @FXML
    private TextField ImportIncludeDomains; */

    @FXML
    private TextField ImportMaxFacets;

    @FXML
    private ComboBox<String> ImportNIEMVersion;

    @FXML
    private TableColumn<String[], String> LocalPathColumn;

    @FXML
    private TextArea LogArea;

    @FXML
    private Label MessageStatus;

    @FXML
    private TitledPane NIEMPane;

    @FXML
    private Label NIEMStatus;

    @FXML
    private TableColumn<String[], String> NamespaceColumn;

    @FXML
    private TableColumn<String[], String> PrefixColumn;

    @FXML
    private TitledPane ProjectPane;

    @FXML
    private Label ProjectStatus;

    @FXML
    private TableColumn<String[], String> URLColumn;

    @FXML
    private VBox mainControls;

    @FXML
    private VBox mainWindow;

    /**
     * JavaFX automatic initialization method - called after FXML loading.
     */
    @FXML
    public void initialize() {

        // Find project package
        project = UmlPackage.getProject();
        //UmlItem target = UmlCom.targetItem();
        properties = new ProjectProperties(project, ProjectProperties.getDefaults());
        properties.load();

        // Redirect logging
        Log.setMessageStatusLabel(MessageStatus);
        Log.setLogArea(LogArea);
        Log.setDebug(properties.getProperty(ProjectProperties.LOG_DEBUG).equals("true"));
        Log.setProfile(properties.getProperty(ProjectProperties.LOG_PROFILE).equals("true"));

        // Find UML package
        umlPackage = null;
        if (project != null && project.children() != null) {
            for (UmlItem pkg : project.children()) {
                if ((pkg.kind() == fr.bouml.anItemKind.aPackage) || pkg.name().equals("UML")) {
                    umlPackage = (UmlPackage) pkg;
                    for (UmlItem subpkg : umlPackage.children()) {
                        if (subpkg.name().equals("Messages")) {
                            messagePackage = (UmlPackage) subpkg;
                            break;
                        }
                    }
                    for (UmlItem subpkg : umlPackage.children()) {
                        if (subpkg.name().equals("Base Classes")) {
                            baseTypesPackage = (UmlPackage) subpkg;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        // create Platform Independent and Platform Specific UML models
        model = new NiemUmlModel(project, properties);

        // create cmftool adapter
        cmftool = new CmfToolAdapter(model);

        // cache UML model
        //("Memorize references ...");
        if (umlPackage != null) {
            umlPackage.memo_ref();
        } else if (project != null) {
            project.memo_ref();
        } else {
            Log.trace("Warning: project is null. Skipping memorization of references.");
        }

        // warn if enumerations are truncated
        String maxEnumsString = properties.getProperty(ProjectProperties.IMPORT_MAX_FACETS);
        if (maxEnumsString != null && !maxEnumsString.isEmpty()) {
            Log.trace("WARNING: NIEM codelists are currently truncated to " + maxEnumsString + " values. To use the complete code lists, import the reference model again.");
        }

        // Set text field values from properties
        String projectDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
        ExportProjectDir.setText(projectDir);
        ProjectPane.setExpanded(projectDir == null || projectDir.isEmpty());
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
        //ImportExcludeDomains.setText(properties.getProperty(ProjectProperties.IMPORT_EXCLUDE_DOMAINS));
        //ImportIncludeDomains.setText(properties.getProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS));
        //ImportExcludeCodes.setText(properties.getProperty(ProjectProperties.IMPORT_EXCLUDE_CODES));
        //ImportIncludeCodes.setText(properties.getProperty(ProjectProperties.IMPORT_INCLUDE_CODES));
        ImportMaxFacets.setText(properties.getProperty(ProjectProperties.IMPORT_MAX_FACETS));
        ImportCodeDescriptions.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.IMPORT_CODE_DESCRIPTIONS)));

        // Add focus listeners to save properties when focus is lost (e.g., TAB key)
        ExportProjectDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                updateProjectDirectory(ExportProjectDir.getText());
            }
        });
        IEPDChangeLogFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_CHANGE_LOG_FILE, IEPDChangeLogFile.getText());
            }
        });
        IEPDContact.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_CONTACT, IEPDContact.getText());
            }
        });
        IEPDEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_EMAIL, IEPDEmail.getText());
            }
        });
        IEPDLicense.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_LICENSE_URL, IEPDLicense.getText());
            }
        });
        IEPDName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_NAME, IEPDName.getText());
                ProjectStatus.setText(properties.getProperty(ProjectProperties.IEPD_NAME));
            }
        });
        IEPDOrganization.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_ORGANIZATION, IEPDOrganization.getText());
            }
        });
        IEPDReadMeFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_READ_ME_FILE, IEPDReadMeFile.getText());
            }
        });
        IEPDStatus.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_STATUS, IEPDStatus.getText());
            }
        });
        IEPDVersion.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IEPD_VERSION, IEPDVersion.getText());
            }
        });
        ExportURI.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.EXPORT_URI, ExportURI.getText());
            }
        });
/*         ImportExcludeDomains.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IMPORT_EXCLUDE_DOMAINS, ImportExcludeDomains.getText());
            }
        }); */
/*         ImportIncludeDomains.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS, ImportIncludeDomains.getText());
            }
        }); */
        /*
        ImportExcludeCodes.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IMPORT_EXCLUDE_CODES, ImportExcludeCodes.getText());
            }
        });
        ImportIncludeCodes.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IMPORT_INCLUDE_CODES, ImportIncludeCodes.getText());
            }
        });
        */
        ImportMaxFacets.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                properties.setProperty(ProjectProperties.IMPORT_MAX_FACETS, ImportMaxFacets.getText());
            }
        });

        // Populate NIEM version dropdown
        String niemVersion = properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION);
        if (niemVersion != null && ImportNIEMVersion != null) {
            ImportNIEMVersion.setValue(niemVersion);
        }
        populateNiemVersionDropdown(ImportNIEMVersion, niemVersion);

        // Expand NIEM pane if reference model doesn't exist
        NIEMPane.setExpanded(!model.verifyNIEM());

        if (NIEMPane.isExpanded()) {
            mainControls.setDisable(true);
            if (model.downloadReferenceModel(properties))
                reloadDomains();
            mainControls.setDisable(false);
        }

        // Download and reload when NIEM pane is expanded
        NIEMPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mainControls.setDisable(true);
                if (model.downloadReferenceModel(properties))
                    reloadDomains();
                mainControls.setDisable(false);
            }
        });

        // Add listener to update domains when selection changes
        DomainListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateDomainSelection();
        });

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

        // Set status
        ProjectStatus.setText(properties.getProperty(ProjectProperties.IEPD_NAME));
        NIEMStatus.setText("NIEM " + properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION));
        MessageStatus.setText("");
    }

    private void reloadDomains() {
        // Populate Domain List View
        String[] domains = model.getReferenceModelDomains(properties);
       if (domains != null && DomainListView != null) {
            DomainListView.getItems().clear();
            DomainListView.getItems().addAll(domains);
            DomainListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            
            // Select domains that match IMPORT_INCLUDE_DOMAINS property
            String includeDomains = properties.getProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS);
            if (includeDomains != null && !includeDomains.isEmpty()) {
                String[] domainsToInclude = includeDomains.split(",");
                for (String domain : domainsToInclude) {
                    String trimmedDomain = domain.trim();
                    for (int i = 0; i < DomainListView.getItems().size(); i++) {
                        if (DomainListView.getItems().get(i).equals(trimmedDomain)) {
                            DomainListView.getSelectionModel().select(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateDomainSelection() {
        if (DomainListView != null) {
            List<String> selectedDomains = DomainListView.getSelectionModel().getSelectedItems();
            String includeDomains = String.join(", ", selectedDomains);
            //ImportIncludeDomains.setText(includeDomains);
            properties.setProperty(ProjectProperties.IMPORT_INCLUDE_DOMAINS, includeDomains);
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
            //MenuItem menuItem = (MenuItem) event.getSource();
            Stage parentStage = (Stage) mainWindow.getScene().getWindow();
            stage.initOwner(parentStage);

            if (controller != null)
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
        String selectedText = LogArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(selectedText);
            clipboard.setContent(content);
        }
    }

    @FXML
    public void exportMapping(ActionEvent event) {

         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace ("Adding NIEM profile to Messages and Base Classes");
                    model.addStereotype(messagePackage);
                    model.addStereotype(baseTypesPackage);
                    Log.trace("Exporting mapping to " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " ...");
                    model.exportMappingCsv();
                    model.exportMappingHtml();
                    Log.trace("\nMapping exported. Next, edit the CSV mapping file as needed, then 'Import Mapping'.\n");
                } catch (Exception e) {
                    Log.trace("Error exporting mapping: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void importMapping(ActionEvent event) {

         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Importing mapping from " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " ...");
                    model.deleteMapping();
                    model.importCsv(model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
                    Log.trace("\nMapping imported. Next, 'Validate Mapping'.\n");
                } catch (Exception e) {
                    Log.trace("Error importing mapping: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 

    }

    @FXML
    public void importReferenceModel(ActionEvent event) {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Importing NIEM Reference Model...");
                    //updateIncludeDomains();
                    if (model.downloadReferenceModel(properties)) {
                        model.importReferenceModel(properties);
                        Log.trace("\nNIEM Reference Model imported. Next: Model in UML, apply the NIEM profile and then 'Export mapping'.\n");
                    } else {
                        Log.trace("NIEM Reference Model download failed or was skipped; import not performed.");
                    }
                } catch (Exception e) {
                    Log.trace("Error importing reference model: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
            }
        };
        new Thread(task).start();
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
            //MenuItem menuItem = (MenuItem) event.getSource();
            Stage parentStage = (Stage) mainWindow.getScene().getWindow();
            stage.initOwner(parentStage);

            if (controller != null)
                controller.setStage(stage);

            // Show the dialog
            stage.showAndWait();
        } catch (IOException e) {
            Log.trace("Failed to load Preferences dialog: " + e.getMessage());
        }
    }

    @FXML
    public void publishCmf(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try { 
                    Log.trace("Publishing CMF to " + model.properties.getProperty(ProjectProperties.EXPORT_CMF_FILE) + " ...");
                    model.createNIEM();
                    model.cacheModels(false);
                    model.exportCmf();
                    Log.trace("\nCMF published. Next, generate schemas with 'XML Model Schemas', 'XML MMessage Schemas', and/or 'JSON Message Schemas'.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing CMF: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishCodelists(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing Codelists to " + model.properties.getProperty(ProjectProperties.EXPORT_CODELISTS_DIR) + " ...");
                    model.exportCodelists();
                    Log.trace("\nCodelists published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing Codelists: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishHtml(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing HTML documentation to " + model.properties.getProperty(ProjectProperties.EXPORT_HTML_DIR) + " ...");
                    model.exportHtml((umlPackage == null) ? project : umlPackage);
                    Log.trace("\nHTML documentation published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing HTML: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishJson(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing JSON schema to " + model.properties.getProperty(ProjectProperties.EXPORT_JSON_DIR) + " ...");
                    cmftool.publishJson();
                    Log.trace("\nJSON schema published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing JSON: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                 return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishMpdCatalog(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing MPD catalog to " + model.properties.getProperty(ProjectProperties.EXPORT_XSD_DIR) + " ...");
                    model.exportMpdCatalog();
                    Log.trace("\nMPD catalog published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing MPD catalog: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishXmlCatalog(ActionEvent event) {
         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing XML catalog to " + model.properties.getProperty(ProjectProperties.EXPORT_XSD_DIR) + " ...");
                    model.exportXmlCatalog();
                    Log.trace("\nXML catalog published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing XML catalog: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    void publishWsdl(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing WSDL documents to " + model.properties.getProperty(ProjectProperties.EXPORT_WSDL_DIR) + " ...");
                    model.exportWsdls();
                    Log.trace("\nWSDL documents published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing WSDL documents: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    void publishOpenApi(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing OpenAPI documents to " + model.properties.getProperty(ProjectProperties.EXPORT_OPENAPI_DIR) + " ...");
                    model.exportOpenApis();
                    Log.trace("\nOpenAPI documents published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing OpenAPI documents: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishXsd(ActionEvent event) {

         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing XSD schemas to " + model.properties.getProperty(ProjectProperties.EXPORT_XSD_DIR) + " ...");
                    cmftool.publishXsd();
                    Log.trace("\nXSD schemas published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing XSD: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
    }

    @FXML
    public void publishXsdModel(ActionEvent event) {

         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Publishing XSD Model schemas to " + model.properties.getProperty(ProjectProperties.EXPORT_XSD_MODEL_DIR) + " ...");
                    cmftool.publishXsdModel();
                    Log.trace("\nXSD Model schemas published.\n");
                } catch (Exception e) {
                    Log.trace("Error publishing XSD Model: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
               return null;
             }
        };
        new Thread(task).start(); 
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
            updateProjectDirectory(newDir);
        }
    }

    public void updateProjectDirectory(String newDir) {
        properties.setProperty(ProjectProperties.EXPORT_PROJECT_DIR, newDir);
        properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, newDir + "/model/mapping/niem-mapping.csv");
        properties.setProperty(ProjectProperties.EXPORT_HTML_DIR, newDir + "/model/html");
    }

    @FXML
    public void selectTextArea(ActionEvent event) {
        LogArea.selectAll();
    }

    @FXML
    public void setProjectProperty(ActionEvent event) {
        Object source = event.getSource();
        String property = null;
        String value = null;
        
        switch (source) {
            case TextField textField -> {
                property = textField.getId();
                value = textField.getText();
            }
            case ComboBox<?> comboBox -> {
                property = comboBox.getId();
                Object selectedValue = comboBox.getValue();
                value = selectedValue != null ? selectedValue.toString() : null;
            }
            default -> {
            }
        }
        
        if (property != null && value != null && !property.isEmpty()) {
            properties.setProperty(property, value);
        }

        if ("ImportNIEMVersion".equals(property)) {
            if (model.downloadReferenceModel(properties)) {
                reloadDomains();
                NIEMStatus.setText("NIEM " + properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION));
            }
        }
        
        if ("IEPDName".equals(property)) {
            ProjectStatus.setText(properties.getProperty(ProjectProperties.IEPD_NAME));
        }   
    }

    @FXML
    void toggleProjectProperty(ActionEvent event) {
        CheckBox source = (CheckBox) event.getSource();
        switch (source.getId()) {
            case "ImportCodeDescriptions" ->
                properties.setProperty(ProjectProperties.IMPORT_CODE_DESCRIPTIONS, Boolean.toString(source.isSelected()));
            default -> {
            }
        }
    }

    @FXML
    public void unselectTextArea(ActionEvent event) {
        LogArea.deselect();
    }

    @FXML
    public void validateMapping(ActionEvent event) {

         Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception { 
                Platform.runLater(() -> mainControls.setDisable(true));
                try {
                    Log.trace("Validating mapping from " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " ...");
                    model.deleteNIEM(false);
                    model.createNIEM();
                    model.cacheModels(false);
                    model.createSubsetAndExtension();
                    Log.trace("\nNIEM Subset and Extensions created. Next, if any there are any mapping issues above, update " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " and 'Import Mapping' and 'Validate Mapping' again as needed.");
                    Log.trace("Otherwise, generate CMF file with 'Common Model Format (CMF)'.\n");
                } catch (Exception e) {
                    Log.trace("Error validating mapping: " + e.getMessage());
                } finally {
                    Platform.runLater(() -> mainControls.setDisable(false));
                }
                return null;
             }
        };
        new Thread(task).start(); 
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
