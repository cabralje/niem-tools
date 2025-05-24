package org.cabral.niemtools;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

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

        // get arguments
        ArrayList<String> args = new ArrayList<>(Arrays.asList(argv));
        String command = null;

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
            if (args.size() >= 1) {
                try {
                    boumlPort = Integer.parseInt(args.removeLast());
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
        if (!args.isEmpty())
            command = args.get(0);
        if (command == null) {
            ConfigurationDialog configDialog = new ConfigurationDialog(properties);
            command = configDialog.showDialog();
            properties.store();
        }

        // create Platform Independent and Platform Specific UML models
        NiemUmlModel model = new NiemUmlModel(project, properties);

        if (command.equals("ImportReferenceModel"))
            try {
                Log.start("importReferenceModel");
                String directory = properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR);
                if (directory == null || directory.isEmpty())
                    directory = selectDirectoryProperty(model, directory,
                            "Directory of the reference schemas to be imported");
                model.deleteNIEM(true);
                model.createNIEM();
                model.cacheModels(true);
                model.importSchemaDir(directory);
                Log.stop("importReferenceModel");

                // Next step
                UmlCom.trace("\nNEXT STEP: Model content in UML, add NIEM stereotypes, and then select 'Publish UML'");
            } catch (IOException e) {
                Log.trace("Exception: " + e.getMessage());
                System.exit(1);
            }
        
        if (!model.verifyNIEM()) {
            UmlCom.trace("NEXT STEP: Select `Import Reference Model`");
            return;
        }
        // Create NIEM models
        model.createNIEM();

        // Cache models
        model.cacheModels(false);

        // Configure project directory
        String projectDirectory = model.properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);
        if (projectDirectory == null || projectDirectory.isEmpty())
            projectDirectory = selectDirectoryProperty(model, ProjectProperties.EXPORT_PROJECT_DIR,
                    "Project directory where artifacts will be generated");

        // execute command
        switch (command) {

            case "addStereotype":
                model.addStereotype(UmlCom.targetItem());
                break;

            case "removeStereotype":
                model.removeStereotype(UmlCom.targetItem());
                break;

            case "publishUML":
                try {
                    // Generate HTML documentation
                    model.exportHtml(target);
                    // Generate NIEM Mapping HTML
                    model.exportMappingHtml();
                    // Generate NIEM Mapping CSV
                    model.exportMappingCsv();
                    // Next steps
                    UmlCom.trace("\nNEXT STEP: map content to NIEM in " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " and then select 'Import Mapping File'");
                } catch (Exception e) {
                    Log.trace("Exception: " + e.getMessage());
                    System.exit(1);
                }
                break;

            case "importMapping":
                try {
                    // Delete previous mapping
                    model.deleteMapping();

                    // Import mapping
                    String filename = selectFileProperty(model, ProjectProperties.EXPORT_MAPPING_FILE, "NIEM Mapping CSV file");
                    model.importCsv(filename);

                    // Next steps
                    UmlCom.trace("\nNEXT STEP: Select 'Validate NIEM mapping'");
                    
                } catch (Exception e) {
                    Log.trace("Exception: " + e.getMessage());
                    System.exit(1);
                }
                break;

            case "validateMapping":
                // Clearing NIEM Models
                model.deleteNIEM(false);
                model.createNIEM();
                model.cacheModels(false);

                // Generating NIEM Models
                model.createSubsetAndExtension();

                // Next steps
                UmlCom.trace("\nNEXT STEP: If any there are any mapping issues above, update " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " and import mappings and validate again as needed.");
                UmlCom.trace("Otherwise, select 'Publish NIEM schemas` and publish CMF, XSD and/or JSON schemas.");
                break;

            case "publishCMF":
                // Export CMF
                model.exportCmf();

                // Next steps
                UmlCom.trace("\nNEXT STEP: Use cmftools to generate XSD and/or JSON schemas");
                break;

            case "publishXSD":
                try {
                    // Generate wantlist for the subset
                    model.exportWantlist();

                    // export code lists
                    String xmlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                        properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);
                    XmlWriter xmlWriter = new XmlWriter(xmlDir);
                    xmlWriter.exportCodeLists(model.getExtensionModel());
                    xmlWriter.exportCodeLists(model.getSubsetModel());

                    // Generate extension schemas

                    // export XML catalog
                    xmlWriter.exportXmlCatalog();

                    // Next steps
                    UmlCom.trace("\nNEXT STEP: Select 'Publish Message Specification'");
                } catch (Exception e) {
                    Log.trace("Exception: " + e.getMessage());
                    System.exit(1);
                }
                break;

            case "publishJSON":
                // Generate subset and extension schemas

                // Next steps
                UmlCom.trace("\nNEXT STEP: Select 'Publish Message Specification'");

                break;

            case "publishSpecification":
                try {
                    model.exportSpecification();
                } catch (Exception ex) {
                    Log.trace("Exception: " + ex.getMessage());
                    System.exit(1);
                }
            default:
                break;  
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
