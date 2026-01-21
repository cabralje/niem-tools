package org.cabral.niemtools;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PreferencesDialogController {
    private Stage stage;
    private ProjectProperties properties;

    @FXML
    private TextField ExportCMFFile;

    @FXML
    private TextField ExportCMFTooltoJSON;

    @FXML
    private TextField ExportCMFTooltoXSD;

    @FXML
    private TextField ExportCMFTooltoXSDModel;

    @FXML
    private TextField ExportJSONDir;

    @FXML
    private TextField ExportOpenAPIDir;

    @FXML
    private TextField ExportWSDLDir;

    @FXML
    private TextField ExportWantlistFile;

    @FXML
    private TextField ExportXSDDir;

    @FXML
    private TextField ExportXSDModelDir;

    @FXML
    private TextField htmldir;

    @FXML
    private TextField niemmapping;

    public void initializeData(ProjectProperties properties) {
        this.properties = properties;
        ExportCMFFile.setText(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE));
        ExportCMFTooltoJSON.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_JSON));
        ExportCMFTooltoXSD.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD));
        ExportCMFTooltoXSDModel.setText(properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD_MODEL));
        ExportJSONDir.setText(properties.getProperty(ProjectProperties.EXPORT_JSON_DIR));
        ExportOpenAPIDir.setText(properties.getProperty(ProjectProperties.EXPORT_OPENAPI_DIR));
        ExportWSDLDir.setText(properties.getProperty(ProjectProperties.EXPORT_WSDL_DIR));
        ExportWantlistFile.setText(properties.getProperty(ProjectProperties.EXPORT_WANTLIST_FILE));
        ExportXSDDir.setText(properties.getProperty(ProjectProperties.EXPORT_XSD_DIR));
        ExportXSDModelDir.setText(properties.getProperty(ProjectProperties.EXPORT_XSD_MODEL_DIR));
        htmldir.setText(properties.getProperty(ProjectProperties.EXPORT_HTML_DIR));
        niemmapping.setText(properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE));

        // Add focus listeners to save properties when focus is lost (e.g., TAB key)
        ExportCMFFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportCMFFile, null));
        });
        ExportCMFTooltoJSON.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportCMFTooltoJSON, null));
        });
        ExportCMFTooltoXSD.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportCMFTooltoXSD, null));
        });
        ExportCMFTooltoXSDModel.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportCMFTooltoXSDModel, null));
        });
        ExportJSONDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportJSONDir, null));
        });
        ExportOpenAPIDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportOpenAPIDir, null));
        });
        ExportWSDLDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportWSDLDir, null));
        });
        ExportWantlistFile.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportWantlistFile, null));
        });
        ExportXSDDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportXSDDir, null));
        });
        ExportXSDModelDir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(ExportXSDModelDir, null));
        });
        htmldir.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(htmldir, null));
        });
        niemmapping.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) setProjectProperty(new ActionEvent(niemmapping, null));
        });

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
        if (property != null && value != null && !property.isEmpty())
            properties.setProperty(property, value);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    public void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }

}
