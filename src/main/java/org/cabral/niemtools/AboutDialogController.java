package org.cabral.niemtools;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutDialogController {
    
    @FXML
    private Label versionLabel;
    
    private Stage stage;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    public void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }
    
    public void setVersion(String version) {
        if (versionLabel != null) {
            versionLabel.setText(version);
        }
    }
}
