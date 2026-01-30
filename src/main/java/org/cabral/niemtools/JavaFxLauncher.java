package org.cabral.niemtools;

import java.io.IOException;

import fr.bouml.UmlCom;
import javafx.application.Platform;

public class JavaFxLauncher {

    public static void launch() {
        // Set additional JavaFX properties right before initialization
        setAllJavaFxProperties();
        
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
     * Set all possible JavaFX boolean properties to prevent null toLowerCase() errors
     */
    private static void setAllJavaFxProperties() {
        
        // Set JavaFX system properties to prevent null parsing errors
        // Core Prism properties
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "false");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.targetvram", "0");
        System.setProperty("prism.poolstats", "false");
        System.setProperty("prism.vsync", "true");
        
        // JavaFX animation properties
        System.setProperty("javafx.animation.fullspeed", "false");
        System.setProperty("javafx.animation.framerate", "60");
       //System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("javafx.pulseLogger", "false");
        
        // Glass/Platform properties
        System.setProperty("glass.win.renderScale", "100%");
        System.setProperty("javafx.platform", "win");
        System.setProperty("glass.accessible.force", "false");
        
        // Quantum properties
        System.setProperty("quantum.multithreaded", "false");
        
        // FXML and Parent properties
        System.setProperty("javafx.preloader", "false");
        System.setProperty("javafx.embed.isEventThread", "false");
        System.setProperty("com.sun.javafx.isEmbedded", "false");
        System.setProperty("javafx.live.resize", "true");
        System.setProperty("javafx.allowAppletMode", "false");
        
        // CSS properties
        System.setProperty("binary.css", "false");
        
        // Ensure all animation/pulse related properties have values
        if (System.getProperty("pulse.duration") == null) {
            System.setProperty("pulse.duration", "16");
        }
        if (System.getProperty("os.name") == null) {
            System.setProperty("os.name", "Windows");
        }

        // Set all accessibility-related properties
        String[] booleanProperties = {
            "glass.accessible.force",
            "com.sun.javafx.isEmbedded",
            "javafx.preloader",
            "javafx.embed.isEventThread",
            "javafx.allowAppletMode",
            "binary.css",
            "javafx.live.resize",
            "prism.verbose",
            "javafx.animation.fullspeed",
            "javafx.pulseLogger",
            "quantum.multithreaded",
            "prism.poolstats",
            "prism.vsync",
            "javafx.autoproxy.disable",
            "javafx.ignoreHiDPI",
            "javafx.disableCoherenceCulling",
            "prism.disableBadDriverWarning",
            "prism.forceGPU",
            "prism.forceUploadingPainter",
            "prism.printAllocs",
            "prism.showdirty",
            "prism.showoverdraw",
            "prism.skipMeshNormalComputation",
            "prism.useFontBitmaps",
            "javafx.sg.warn"
        };
        
        for (String prop : booleanProperties) {
            if (System.getProperty(prop) == null) {
                System.setProperty(prop, "false");
            }
        }
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
