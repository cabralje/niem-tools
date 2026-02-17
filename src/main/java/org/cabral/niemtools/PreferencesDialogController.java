package org.cabral.niemtools;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PreferencesDialogController {

    private Stage stage;
    private ProjectProperties properties;

    @FXML
    private CheckBox ExportCMF;

    @FXML
    private TextField ExportCMFFile;

    @FXML
    private TextField ExportCMFTooltoJSON;

    @FXML
    private TextField ExportCMFTooltoXSD;

    @FXML
    private TextField ExportCMFTooltoXSDModel;

    @FXML
    private TextField ExportCodeListsDir;

    @FXML
    private CheckBox ExportCodelists;

    @FXML
    private CheckBox ExportHTML;

    @FXML
    private CheckBox ExportJSON;

    @FXML
    private TextField ExportJSONDir;

    @FXML
    private CheckBox ExportMpdCatalog;

    @FXML
    private TextField ExportMpdCatalogFile;

    @FXML
    private CheckBox ExportOpenAPI;

    @FXML
    private TextField ExportOpenAPIDir;

    @FXML
    private CheckBox ExportWSDL;

    @FXML
    private TextField ExportWSDLDir;

    @FXML
    private CheckBox ExportWantlist;

    @FXML
    private TextField ExportWantlistFile;

    @FXML
    private CheckBox ExportXmlCatalog;

    @FXML
    private TextField ExportXmlCatalogFile;

    @FXML
    private CheckBox ExportXSD;

    @FXML
    private TextField ExportXSDDir;

    @FXML
    private CheckBox ExportXSDModel;

    @FXML
    private TextField ExportXSDModelDir;

    @FXML
    private CheckBox LogDebug;

    @FXML
    private CheckBox LogProfile;

    @FXML
    private TextField htmldir;

    @FXML
    private TextField niemmapping;

    public void initializeData(ProjectProperties properties) {
        this.properties = properties;

        // Generation tab
        ExportCMF.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_CMF)));
        ExportCMFFile.setText(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE));
        ExportCMFFile.setDisable(!ExportCMF.isSelected());
        ExportCMFFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportCMFFile, null));
            }
        });

        ExportXSDModel.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_XSD_MODEL)));
        ExportXSDModelDir.setText(properties.getProperty(ProjectProperties.EXPORT_XSD_MODEL_DIR));
        ExportXSDModelDir.setDisable(!ExportXSDModel.isSelected());
        ExportXSDModelDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportXSDModelDir, null));
            }
        });

        ExportCodelists.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_CODELISTS)));
        ExportCodeListsDir.setText(properties.getProperty(ProjectProperties.EXPORT_CODELISTS_DIR));
        ExportCodeListsDir.setDisable(!ExportCodelists.isSelected());
        ExportCodeListsDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportCodeListsDir, null));
            }
        });

        ExportWSDL.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_WSDL)));
        ExportWSDLDir.setText(properties.getProperty(ProjectProperties.EXPORT_WSDL_DIR));
        ExportWSDLDir.setDisable(!ExportWSDL.isSelected());
        ExportWSDLDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportWSDLDir, null));
            }
        });

        ExportXSD.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_XSD)));
        ExportXSDDir.setText(properties.getProperty(ProjectProperties.EXPORT_XSD_DIR));
        ExportXSDDir.setDisable(!ExportXSD.isSelected());
        ExportXSDDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportXSDDir, null));
            }
        });

        ExportJSON.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_JSON)));
        ExportJSONDir.setText(properties.getProperty(ProjectProperties.EXPORT_JSON_DIR));
        ExportJSONDir.setDisable(!ExportJSON.isSelected());
        ExportJSONDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportJSONDir, null));
            }
        });

        ExportOpenAPI.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_OPENAPI)));
        ExportOpenAPIDir.setText(properties.getProperty(ProjectProperties.EXPORT_OPENAPI_DIR));
        ExportOpenAPIDir.setDisable(!ExportOpenAPI.isSelected());
        ExportOpenAPIDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportOpenAPIDir, null));
            }
        });

        ExportXmlCatalog.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_XML_CATALOG)));
        ExportXmlCatalogFile.setText(properties.getProperty(ProjectProperties.EXPORT_XML_CATALOG_FILE));
        ExportXmlCatalogFile.setDisable(!ExportXmlCatalog.isSelected());
        ExportXmlCatalogFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportXmlCatalogFile, null));
            }
        });
        
        ExportMpdCatalog.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_MPD_CATALOG)));
        ExportMpdCatalogFile.setText(properties.getProperty(ProjectProperties.EXPORT_MPD_CATALOG_FILE));
        ExportMpdCatalogFile.setDisable(!ExportMpdCatalog.isSelected());
        ExportMpdCatalogFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportMpdCatalogFile, null));
            }
        });

        ExportWantlist.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_WANTLIST)));
        ExportWantlistFile.setText(properties.getProperty(ProjectProperties.EXPORT_WANTLIST_FILE));
        ExportWantlistFile.setDisable(!ExportWantlist.isSelected());
        ExportWantlistFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportWantlistFile, null));
            }
        });

        ExportHTML.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.EXPORT_HTML)));
        htmldir.setText(properties.getProperty(ProjectProperties.EXPORT_HTML_DIR));
        htmldir.setDisable(!ExportHTML.isSelected());
        htmldir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(htmldir, null));
            }
        });

        niemmapping.setText(properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));
        niemmapping.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(niemmapping, null));
            }
        });

        // cmftool tab
        ExportCMFTooltoXSDModel.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD_MODEL));
        ExportCMFTooltoXSDModel.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportCMFTooltoXSDModel, null));
            }
        });

        ExportCMFTooltoXSD.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD));
        ExportCMFTooltoXSD.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportCMFTooltoXSD, null));
            }
        });

        ExportCMFTooltoJSON.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_JSON));
        ExportCMFTooltoJSON.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setProjectProperty(new ActionEvent(ExportCMFTooltoJSON, null));
            }
        });

        // logging tab
        LogDebug.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.LOG_DEBUG)));
        LogProfile.setSelected(Boolean.parseBoolean(properties.getProperty(ProjectProperties.LOG_PROFILE)));

    }

    public void setStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }
        this.stage = stage;
    }

    @FXML
    public void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void selectDocumentationDirectory(ActionEvent event) {
        //Node source = (Node) event.getSource();
        //Stage stage = (Stage) source.getScene().getWindow();
        String currentDir = properties.getProperty(ProjectProperties.EXPORT_HTML_DIR);
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
            htmldir.setText(newDir);
            properties.setProperty(ProjectProperties.EXPORT_HTML_DIR, newDir);
        }
    }

    @FXML
    void selectMappingFile(ActionEvent event) {
        //Node source = (Node) event.getSource();
        //Stage stage = (Stage) source.getScene().getWindow();
        String file = properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE);
        //String newDir = null;
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        if (file != null) {
            File f = new File(file);
            if (f.exists()) {
                fc.setInitialFileName(file);
                fc.setInitialDirectory(f.getParentFile());
            }
        }
        File selectedFile = fc.showOpenDialog(stage);
        if (selectedFile != null) {
            file = selectedFile.getAbsolutePath();
            niemmapping.setText(file);
            properties.setProperty(ProjectProperties.EXPORT_MAPPING_FILE, file);
        }
    }

    @FXML
    void setProjectProperty(ActionEvent event) {
        TextField source = (TextField) event.getSource();
        String property = source.getId();
        if (property.equals("htmldir")) {
            property = ProjectProperties.EXPORT_HTML_DIR;
        } else if (property.equals("niemmapping")) {
            property = ProjectProperties.EXPORT_MAPPING_FILE;
        }
        String value = source.getText();
        if (property != null && value != null && !property.isEmpty()) {
            properties.setProperty(property, value);
        }
    }

    @FXML
    void toggleProjectProperty(ActionEvent event) {
        CheckBox source = (CheckBox) event.getSource();
        switch (source.getId()) {
            case "ExportCMF" -> {
                ExportCMFFile.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_CMF, Boolean.toString(source.isSelected()));
            }
            case "ExportXSDModel" -> {
                ExportXSDModelDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_XSD_MODEL, Boolean.toString(source.isSelected()));
            }
            case "ExportCodelists" -> {
                ExportCodeListsDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_CODELISTS, Boolean.toString(source.isSelected()));
            }
            case "ExportWSDL" -> {
                ExportWSDLDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_WSDL, Boolean.toString(source.isSelected()));
            }
            case "ExportXSD" -> {
                ExportXSDDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_XSD, Boolean.toString(source.isSelected()));
            }
            case "ExportJSON" -> {
                ExportJSONDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_JSON, Boolean.toString(source.isSelected()));
            }
            case "ExportOpenAPI" -> {
                ExportOpenAPIDir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_OPENAPI, Boolean.toString(source.isSelected()));
            }
            case "ExportXmlCatalog" -> {
                ExportXmlCatalogFile.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_XML_CATALOG, Boolean.toString(source.isSelected()));
            }
            case "ExportMpdCatalog" -> {
                ExportMpdCatalogFile.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_MPD_CATALOG, Boolean.toString(source.isSelected()));
            }
            case "ExportWantlist" -> {
                ExportWantlistFile.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_WANTLIST, Boolean.toString(source.isSelected()));
            }
            case "ExportHTML" -> {
                htmldir.setDisable(!source.isSelected());
                properties.setProperty(ProjectProperties.EXPORT_HTML, Boolean.toString(source.isSelected()));
            }
            case "LogDebug" -> {
                properties.setProperty(ProjectProperties.LOG_DEBUG, Boolean.toString(source.isSelected()));
                Log.setDebug(source.isSelected());
            }
            case "LogProfile" -> {
                properties.setProperty(ProjectProperties.LOG_PROFILE, Boolean.toString(source.isSelected()));
                Log.setProfile(source.isSelected());
            }
            default -> {
            }
        }
    }

}
