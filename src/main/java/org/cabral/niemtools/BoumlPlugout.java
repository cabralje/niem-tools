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
 * Entry point for the BoumlPlugout application.
 * <p>
 * This method initializes the application, processes command-line arguments,
 * establishes a connection to the BOUML tool via a specified port, loads project properties,
 * and executes various commands related to NIEM UML model processing and artifact generation.
 * </p>
 *
 * <p>
 * The main workflow includes:
 * <ul>
 *   <li>Setting the system look and feel for the UI.</li>
 *   <li>Determining the BOUML port from a test harness file or command-line arguments.</li>
 *   <li>Connecting to the BOUML tool using the specified port.</li>
 *   <li>Loading and configuring project properties.</li>
 *   <li>Handling user commands such as importing reference models, adding/removing stereotypes,
 *       publishing UML, importing and validating mappings, and exporting CMF, XSD, JSON, and message specifications.</li>
 *   <li>Managing NIEM UML models and related artifacts.</li>
 *   <li>Logging and tracing execution steps and errors.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Supported commands include:
 * <ul>
 *   <li><b>ImportReferenceModel</b>: Automates the import of reference schemas into the UML model.</li>
 *   <li><b>addStereotype</b>: Adds NIEM stereotypes to UML elements.</li>
 *   <li><b>removeStereotype</b>: Removes NIEM stereotypes from UML elements.</li>
 *   <li><b>publishUML</b>: Generates HTML documentation and mapping files from the UML model.</li>
 *   <li><b>importMapping</b>: Imports NIEM mapping from a CSV file.</li>
 *   <li><b>validateMapping</b>: Validates the imported NIEM mapping and generates subset/extension models.</li>
 *   <li><b>publishCMF</b>: Exports the Canonical Model Format (CMF) for the NIEM model.</li>
 *   <li><b>publishXSD</b>: Generates XSD schemas from the NIEM model or via external tools.</li>
 *   <li><b>publishJSON</b>: Generates JSON schemas from the NIEM model or via external tools.</li>
 *   <li><b>publishSpecification</b>: Generates the message specification documentation.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The method ensures proper cleanup and termination of the connection to BOUML upon completion or error.
 * </p>
 *
 * @author James Cabral
 * @version 1.0
 * @param argv Command-line arguments specifying the operation mode and options.
 */

package org.cabral.niemtools;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

public class BoumlPlugout {


    @SuppressWarnings("unused")
    public static void main(String argv[]) {
        Log.start("main");

        // get arguments
        ArrayList<String> args = new ArrayList<>(Arrays.asList(argv));
        String command = null;

        // set look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Log.trace(e.getMessage());
            System.exit(1);
        }

        // locate the BOUML port
        // the program is called with the socket port number in argument
        int boumlPort = 0;
        try {
            // check for BOUML port from test harness
            File file = new File(TestHarness.filename);
            String buffer = new String(Files.readAllBytes(file.toPath()));
            boumlPort = Integer.parseInt(buffer);
            if (!file.delete()) {
                Log.trace("Warning: Failed to delete the test harness file: " + file.getAbsolutePath());
            }
        } catch (IOException | NumberFormatException e) {
            // file not found, try to get port from command line
            if (!args.isEmpty()) {
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
        //UmlItem target = UmlCom.targetItem();
        ProjectProperties properties = new ProjectProperties(project, ProjectProperties.getDefaults());
        properties.load();

        // Find UML package
        UmlPackage umlPackage = null;
        if (project != null)
            for (UmlItem pkg : project.children())
                if ((pkg.kind() == fr.bouml.anItemKind.aPackage) || pkg.name().equals("UML")) {   
                    umlPackage = (UmlPackage)pkg;
                    break;
                }

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

        // Configure project directory
        String projectDirectory = model.properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR);

        // cache UML model
        UmlCom.message("Memorize references ...");
        if (umlPackage != null)
            umlPackage.memo_ref();
        else if (project != null)
            project.memo_ref();
        else
            Log.trace("Warning: project is null. Skipping memorization of references.");

        switch (command) {
            
            case "importReferenceModel":

                //TODO automate download from GitHub and import of the reference model
                // Automate download from GitHub and import of the reference model
                // Implementation Plan:
                // 1. Define the GitHub repository URL and the project directory for the download.
                // 2. Use a library like Apache HttpClient or Java's HttpURLConnection to fetch the files.
                // 3. Save the downloaded files to the specified directory.
                // 4. Validate the downloaded files (e.g., check file integrity or structure).
                // 5. Proceed with importing the reference model into the UML project.

                String importDir = System.getProperty("java.io.tmpdir");
                try {
                    String githubRepoUrl = "https://github.com/niemopen/niem-model/archive/refs/tags/";
                    String modelUrl = githubRepoUrl + properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION) + ".zip";
                    String importFile = importDir + File.separator + "reference_model.zip";
//                   String targetDirectory = properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR);
                    //if (targetDirectory == null || targetDirectory.isEmpty()) {
                    // Download the reference model from GitHub
                    // Download the reference model from GitHub using HttpURLConnection with timeouts
                    java.net.URL url = java.net.URI.create(modelUrl).toURL();
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000); // 10 seconds timeout for connection
                    connection.setReadTimeout(10000);    // 10 seconds timeout for reading
                    try (java.io.InputStream in = connection.getInputStream()) {
                        java.nio.file.Files.copy(in, new File(importFile).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } finally {
                        connection.disconnect();
                    }
                    // Download the reference model from GitHub using Java's built-in URL/Streams
                    try (java.io.InputStream in = java.net.URI.create(modelUrl).toURL().openStream()) {
                        java.nio.file.Files.copy(in, new File(importFile).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Unzip the downloaded file
                    try (java.io.InputStream fis = new java.io.FileInputStream(importFile);
                         java.util.zip.ZipInputStream zis =
                             new java.util.zip.ZipInputStream(fis)) {
                        java.util.zip.ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            File outFile = new File(importDir, entry.getName());
                            if (entry.isDirectory()) {
                                outFile.mkdirs();
                            } else {
                                outFile.getParentFile().mkdirs();
                                try (java.io.OutputStream os = new java.io.FileOutputStream(outFile)) {
                                    byte[] buffer = new byte[4096];
                                    int len;
                                    while ((len = zis.read(buffer)) != -1) {
                                        os.write(buffer, 0, len);
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    Log.trace("Exception 1 in importReferenceModel " + e.getMessage());
                    System.exit(1); 
                }

                try {
                    Log.start("importReferenceModel");
                    String directory = importDir + File.separator + "niem-model-" + properties.getProperty(ProjectProperties.IMPORT_NIEM_VERSION) + File.separator + "xsd";
                    //String directory = properties.getProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR);
                    properties.setProperty(ProjectProperties.IMPORT_REFERENCE_MODEL_DIR, directory);
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
                    Log.trace("Exception 2 in importReferenceModel: " + e.getMessage());
                    System.exit(1);
                }
                
                if (!model.verifyNIEM()) {
                    UmlCom.trace("NEXT STEP: Select `Import Reference Model`");
                    return;
                }
                break;
                
            case "addStereotype":
                
                model.addStereotype(project);
                break;
                
            case "removeStereotype":
                
                model.removeStereotype(project);
                break;
                
            case "publishUML":
                try {

                    // Generate HTML documentation
                    model.exportHtml((umlPackage == null) ? project : umlPackage);
                    
                    // Generate NIEM Mapping HTML
                    model.exportMappingHtml();
                    
                    // Generate NIEM Mapping CSV
                    model.exportMappingCsv();
                    
                    // Next steps
                    UmlCom.trace("\nNEXT STEP: map content to NIEM in " + model.properties.getProperty(ProjectProperties.EXPORT_MAPPING_FILE) + " and then select 'Import Mapping File'");
                } catch (Exception e) {
                    Log.trace("Exception in publishUML: " + e.getMessage());
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
                    UmlCom.trace("\nNEXT: 'Validating NIEM mapping'");
                    
                } catch (HeadlessException e) {
                    Log.trace("Exception in importMapping: " + e.getMessage());
                    System.exit(1);
                }

                // automatically validate mapping
                // break;
                
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
                
                // Create NIEM models
                model.createNIEM();

                // Cache models
                model.cacheModels(false);
                
                // Export CMF
                model.exportCmf();
                
                // Next steps
                UmlCom.trace("\nNEXT STEP: Use cmftools to generate XSD and/or JSON schemas");
                break;
                
            case "publishXSD":

                publishXSD(model);
                break;
                
            case "publishJSON":
                
                publishJSON(model);
                break;
                
            case "publishSpecification":
                try {  
                    
                    // Generate HTML documentation
                    model.exportHtml(project);
                    
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
                    
                    // Cache models
                    model.cacheModels(false);

                    // Export CMF
                    String exportCmf = model.properties.getProperty(ProjectProperties.EXPORT_CMF);
                    if (exportCmf.equals("true")) {
                        Log.trace("Exporting CMF");
                        model.exportCmf();

                        String exportXsd = model.properties.getProperty(ProjectProperties.EXPORT_XSD);
                        if (exportXsd.equals("true")) {
                            Log.trace("Exporting XSD");
                            publishXSD(model);
                        }

                        String exportJson = model.properties.getProperty(ProjectProperties.EXPORT_JSON);
                        if (exportJson.equals("true")) {
                            Log.trace("Exporting JSON schema");
                            publishJSON(model);
                        }
                    }
                    
                    // Generate message specification
                    model.exportSpecification();
                } catch (Exception ex) {
                    Log.trace("Exception in publishSpecification: " + ex.getMessage());
                    System.exit(1);
                }
                break;
                
            default:
                Log.trace("Error: Unrecognized command '" + command + "'. Please check the available commands and try again.");
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
            if (fc.getSelectedFile() != null) {
                directory = fc.getSelectedFile().getAbsolutePath();
                model.properties.setProperty(propertyName, directory);
            }
        } else {
            Log.trace("File chooser dialog canceled. No directory selected.");
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
    //@SuppressWarnings("unused")
    private static String selectFileProperty(NiemUmlModel model, String propertyName, String dialogTitle) throws HeadlessException {
        String file = model.properties.getProperty(propertyName);
        JFileChooser fc = new JFileChooser(file);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            if (fc.getSelectedFile() != null) {
                file = fc.getSelectedFile().getAbsolutePath();
                model.properties.setProperty(propertyName, file);
            }
        } else {
            Log.trace("File selection canceled by the user.");
            return null;
        }
            model.properties.setProperty(propertyName, file);

        return file;
    }

    /**
     * Executes a command in the system shell.
     * @param execCommand
    */
    static int exec(String execCommand) throws IOException, InterruptedException {

        int exitCode;
        Log.trace("Executing command: " + execCommand);

        // parse the command into a list of arguments
        List<String> commandList = new ArrayList<>();
        String[] parts = execCommand.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String part : parts) {
            commandList.add(part.replace("\"", ""));
        }
        ProcessBuilder pb = new ProcessBuilder(commandList);
        Process process = pb.start();

        // Read the output of the command
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Log.trace(line);
            }
        }

        // Read the error stream of the command
        try (BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line2;
            while ((line2 = reader2.readLine()) != null) {
                Log.trace(line2);
            }
        }

        // Wait for the command to complete
        exitCode = process.waitFor();
        Log.debug("Command exited with code: " + exitCode);

        return exitCode;
    }
    /** * Publish XSD schemas from the NiemUmlModel.
     * 
     * @param model
     */
    static void publishXSD(NiemUmlModel model) {
        
        String exportCmfToXsd = model.properties.getProperty(ProjectProperties.EXPORT_CMF_TO_XSD);

        ProjectProperties properties = model.properties;
        // use cmftool to generate XSDs from CMF
        if (exportCmfToXsd.equals("true")) {
            Log.trace("Exporting CMF to XSD using cmftool");
            // Export CMF to XSD

            String xsdDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);
            String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                properties.getProperty(ProjectProperties.EXPORT_CMF_DIR) + File.separator +
                CmfWriter.getCmfFilename(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE), 
                    properties.getProperty(ProjectProperties.EXPORT_CMF_VERSION)); 

            //Verify xsdDir exists
            Path xsdPath = Paths.get(xsdDir);
            if (!Files.exists(xsdPath)) {
                try {
                    Files.createDirectories(xsdPath);
                } catch (IOException e) {
                    Log.trace("Exception 1 in publishXSD: could not create directory " + xsdDir + ": " + e.getMessage());
                    return;
                }
            }
            // Verify cmfFile directory exists
            Path cmfPath = Paths.get(cmfFile).getParent();
            if (!Files.exists(cmfPath)) {
                try {
                    Files.createDirectories(cmfPath);
                } catch (IOException e) {
                    Log.trace("Exception 2 in publishXSD: could not create directory " + cmfPath + ": " + e.getMessage());
                    return;
                }
            }
            
            String execCommandXsd = properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD) + " " 
                + xsdDir + " " + cmfFile;
            try {     
                exec(execCommandXsd);
            } catch (IOException | InterruptedException e) {
                Log.trace("Exception 3 in publishXSD: " + e.getMessage());
                System.exit(1); 
            }

        } else {
            // generate XSDs in niem-tools
            Log.trace("Exporting XSDs using niem-tools");
            try {
                
                // Create NIEM models
                model.createNIEM();
                
                // Cache models
                model.cacheModels(false);
                
                // Generate wantlist for the subset
                model.exportWantlist();
                
                // export code lists
                String xmlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                        properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);
                XmlWriter xmlWriter = new XmlWriter(xmlDir);
                xmlWriter.exportCodeLists(NiemUmlModel.getExtensionModel());
                xmlWriter.exportCodeLists(NiemUmlModel.getSubsetModel());
                
                // Generate XSD extension schemas
                model.exportSpecification();
                
                // export XML catalog
                xmlWriter.exportXmlCatalog();
                
                // Next steps
                UmlCom.trace("\nNEXT STEP: Select 'Publish Message Specification'");
            } catch (IOException e) {
                Log.trace("Exception 4 in publishXSD: " + e.getMessage());
                System.exit(1);
            }
        }   

    }

    /**
     * Publish JSON schemas from the NiemUmlModel.
     *
     * @param model The NiemUmlModel instance.
     */
    static void publishJSON(NiemUmlModel model) {
        String exportCmfToJson = model.properties.getProperty(ProjectProperties.EXPORT_CMF_TO_JSON);
        ProjectProperties properties = model.properties;

        // use cmftool to generate JSON schemas from CMF
        if (exportCmfToJson.equals("true")) {
            Log.trace("Exporting CMF to JSON schema using cmftool");

            String jsonFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                properties.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_DIR) + File.separator +
                JsonWriter.getJsonFilename(properties.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_FILE));
            String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                properties.getProperty(ProjectProperties.EXPORT_CMF_DIR) + File.separator +
                CmfWriter.getCmfFilename(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE), 
                    properties.getProperty(ProjectProperties.EXPORT_CMF_VERSION));

            // Verify jsonFile directory exists
            Path jsonPath = Paths.get(jsonFile).getParent();
            if (!Files.exists(jsonPath)) {
                try {
                    Files.createDirectories(jsonPath);
                } catch (IOException e) {
                    Log.trace("Exception 1 in publish JSON: could not create directory " + jsonPath + ": " + e.getMessage());
                    return;
                }
            }

            // Verify cmfFile directory exists
            Path cmfPath = Paths.get(cmfFile).getParent();
            if (!Files.exists(cmfPath)) {
                try {
                    Files.createDirectories(cmfPath);
                } catch (IOException e) {
                    Log.trace("Exception 2 in publishJSON: could not create directory " + cmfPath + ": " + e.getMessage());
                    return;
                }
            }

            // Export CMF to XSD
            String execCommandXsd =
                properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_JSON) + " " +
                jsonFile + " " + cmfFile + " ";
            try {     
                exec(execCommandXsd);
            } catch (IOException | InterruptedException e) {
                Log.trace("Exception 3 in publishJSON: " + e.getMessage());
                System.exit(1);
            }
        } else {
            // generate JSON schema in niem-tools
            Log.trace("Exporting JSON schema using niem-tools");
            try {
                // Create NIEM models
                model.createNIEM();

                // Cache models
                model.cacheModels(false);
                
                // Generate JSON subset and extension schemas
                model.exportSpecification();
                
                // Next steps
                UmlCom.trace("\nNEXT STEP: Select 'Publish Message Specification'");
            } catch (Exception e) {
                Log.trace("Exception 4 in publishJSON: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}

