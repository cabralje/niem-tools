package org.cabral.niemtools;

import java.io.IOException;

import fr.bouml.UmlCom;
import javafx.application.Platform;

public class JavaFxLauncher {

    public static void launch() {
        // Start JavaFX toolkit before any UI interaction
        startJavaFxIfNeeded();

        // Prevent JavaFX from exiting when last window closes - we'll control exit explicitly
        Platform.setImplicitExit(false);

        // Start Java FX from main.fxml
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        BoumlPlugout.class.getResource("/org/cabral/niemtools/App.fxml")
                );
                javafx.scene.Parent root = loader.load();
                AppController controller = loader.getController();
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                stage.setScene(scene);
                stage.setTitle("NIEM Tools");

                // Handle window close to properly exit application
                stage.setOnCloseRequest(event -> {
                    Platform.exit();
                    UmlCom.bye(0);
                    UmlCom.close();
                    System.exit(0);
                });

                stage.show();
            } catch (IOException e) {
                Log.trace("Error loading App.fxml: " + e.getMessage());
            }
        });
    }

    /**
     * Ensure JavaFX toolkit is initialized. Safe to call multiple times.
     */
    private static void startJavaFxIfNeeded() {
        try {
            // If toolkit is not initialized, this will start it.
            // If already initialized, IllegalStateException is thrown and can be ignored.
            Platform.startup(() -> {
            });
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit already initialized; no action needed.
        }
    }
}
