package org.cabral.niemtools;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

// the program is called with the socket port number in argument
public class BoumlPlugout {

    public static void main(String argv[]) {
        Log.start("main");

        // set look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Log.trace("Exception: " + e.getMessage());
            System.exit(1);
        }

        // locate the BOUML port
        int boumlPort = 0;
        try {
            // check for BOUML port from test harness
            File file = new File(TestHarness.filename);
            String buffer = new String(Files.readAllBytes(file.toPath()));
            boumlPort = Integer.parseInt(buffer);
            file.delete();
        } catch (IOException | NumberFormatException e) {
            // file not found, try to get port from command line
            if (argv.length >= 1) {
                try {
                    boumlPort = Integer.parseInt(argv[argv.length - 1]);
                } catch (NumberFormatException e2) {
                    Log.trace("Exception: " + e2.getMessage());
                    System.exit(1);
                }
            }
        }
        if (boumlPort == 0) {
            System.out.println("No BOUML port.  Exiting.");
            System.exit(1);
        }
        System.out.println("Connecting to BOUML on port " + boumlPort);
        Log.debug("Port: " + boumlPort + "\n");
        Log.debug("Classpath: " + System.getProperty("java.class.path") + "\n");

        // connect to BOUML port
        try {
            UmlCom.connect(boumlPort);
        } catch (RuntimeException e) {
            System.out.println("Error connecting to BOUML: " + e.getMessage());
            System.exit(1);
        }

        // cache UML model
        UmlPackage project = UmlPackage.getProject();
        //Log.start("memo_ref");
        UmlCom.message("Memorize references ...");
        UmlItem target = UmlCom.targetItem();
        target.memo_ref();
        //Log.stop("memo_ref");

        // load project properties
        ProjectProperties properties = new ProjectProperties(project, ProjectProperties.getDefaults());
        properties.load();

        // handle configuration
        String command = argv[0];
        if (command.equals("configure")) {
            ConfigurationDialog configDialog = new ConfigurationDialog(properties);
            command = configDialog.showDialog();
            properties.store();
        }

        // create Platform Independent and Platform Specific UML models
        NiemUmlModel model = new NiemUmlModel(project, properties);

        // execute command
        switch (command) {
            case "importSchema":
                try {
                    Log.start("importSchema");
                    String directory = properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR);
                    if (directory == null || directory.equals(""))
                        directory = selectDirectoryProperty(model, directory,
                                "Directory of the reference schemas to be imported");
                    model.deleteNIEM(true);
                    model.createNIEM();
                    model.cacheModels(true);
                    model.importSchemaDir(directory);
                    Log.stop("importSchema");
                } catch (IOException e) {
                    Log.trace("Exception: " + e.getMessage());
                    System.exit(1);
                }
                break;

            case "import":
                model.cacheModels(false);
                model.deleteMapping();
                String filename = selectFileProperty(model, ProjectProperties.EXPORT_MAPPING_FILE, "NIEM Mapping CSV file");
                model.importCsv(filename);
                break;

            case "sort":
                model.cacheModels(false);
                UmlCom.targetItem().sort();
                break;

            case "addStereotype":
                model.cacheModels(false);
                model.addStereotype(UmlCom.targetItem());
                break;

            case "removeStereotype":
                model.cacheModels(false);
                model.removeStereotype(UmlCom.targetItem());
                break;

            case "export":
            default:
                if (!model.verifyNIEM()) {
                    break;
                }
                model.cacheModels(false);
                try {
                    generateModels(model, target);
                } catch (IOException ex) {
                }
        }
        Log.trace("Done");
        UmlCom.message("");
        Log.stop("main");
        // must be called to cleanly inform that all is done
        UmlCom.bye(0);
        UmlCom.close();
        System.exit(0);
    }

    /**
     * @param root
     * @param target
     * @param model
     * @param properties
     * @throws IOException
     */
    private static void generateModels(NiemUmlModel model, UmlItem target) throws IOException {
        Log.start("generateModels");

        String projectDirectory = model.properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
        if (projectDirectory == null || projectDirectory.equals(""))
            projectDirectory = selectDirectoryProperty(model, ProjectProperties.EXPORT_PROJECT_DIR,
                    "Project directory where artifacts will be generated");

        // Generate HTML documentation
        model.exportHtml(target);

        // Generate NIEM Mapping HTML
        model.exportMappingHtml();

        // Generate NIEM Mapping CSV
        model.exportMappingCsv();

        // Clearing NIEM Models
        model.deleteNIEM(false);
        model.createNIEM();
        model.cacheModels(false);

        // Generating NIEM Models
        model.createSubsetAndExtension();

        // Generate NIEM Wantlist instance
        model.exportWantlist();

        // Cache subsitutions
        model.cacheModels(false);

        // Generate schemas
        model.exportSpecification();
        Log.stop("generateModels");
    }

    /**
     * Select a directory property using a file chooser dialog.
     *
     * @param model The NiemUmlModel instance.
     * @param propertyName The name of the property to set.
     * @param dialogTitle The title of the file chooser dialog.
     * @return The selected directory path.
     */
    private static String selectDirectoryProperty(NiemUmlModel model, String propertyName, String dialogTitle) throws HeadlessException {
        String directory = model.properties.getProperty(propertyName);
        JFileChooser fc = new JFileChooser(directory);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle(dialogTitle);
        if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            directory = fc.getSelectedFile().getAbsolutePath();
            model.properties.setProperty(propertyName, directory);
        }
        return directory;
    }

    /**
     * Select a file property using a file chooser dialog.
     *
     * @param model The NiemUmlModel instance.
     * @param propertyName The name of the property to set.
     * @param dialogTitle The title of the file chooser dialog.
     * @return The selected directory path.
     */
    private static String selectFileProperty(NiemUmlModel model, String propertyName, String dialogTitle) throws HeadlessException {
        String file = model.properties.getProperty(propertyName);
        //String file = filename;
        JFileChooser fc = new JFileChooser(file);
        fc.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        fc.setDialogTitle(dialogTitle);
        if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile().getAbsolutePath();
            //file = fc.getSelectedFile().getName();
            model.properties.setProperty(propertyName, file);
        }
        return file;
    }
}
