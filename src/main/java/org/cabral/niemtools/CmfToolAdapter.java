package org.cabral.niemtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class CmfToolAdapter {

    private final NiemUmlModel model;

    /**
     * Initializes CmfToolAdapter with a NiemUmlModel
     *
     * @param model The NiemUmlModel instance
     */
    CmfToolAdapter(NiemUmlModel model) {
        this.model = model;
    }

    /**
     * Executes a command in the system shell.
     *
     * @param execCommand
     */
    static int exec(String execCommand) throws IOException, InterruptedException {

        int exitCode;
        Log.trace("Executing command: " + execCommand);

        // parse the command into a list of arguments
        List<String> commandList = new ArrayList<>();
        String[] parts = execCommand.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (parts != null) {
            for (String part : parts) {
                commandList.add(part.replace("\"", ""));
            }
        }

        Process process = new ProcessBuilder(commandList).start(); // Start the process

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

    /**
     * * Publish XSD model schemas from the NiemUmlModel.
     *
     * @param model
     */
    protected void publishXSDModel() {

        //String exportCmfToXsdModel = model.properties.getProperty(ProjectProperties.EXPORT_CMF_TO_XSD_MODEL);
        ProjectProperties properties = model.properties;
        // use cmftool to generate XSDs from CMF
        //if (exportCmfToXsdModel.equals("true")) {
        Log.trace("Exporting CMF to XSD using cmftool");
        // Export CMF to XSD

        String xsdDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + properties.getProperty(ProjectProperties.EXPORT_XSD_MODEL_DIR);
        String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + CmfWriter.getCmfFilename(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE),
                        properties.getProperty(ProjectProperties.EXPORT_CMF_VERSION));

        //Verify xsdDir exists
        Path xsdPath = Paths.get(xsdDir);
        if (!Files.exists(xsdPath)) {
            try {
                Files.createDirectories(xsdPath);
            } catch (IOException e) {
                Log.trace("Exception 1 in publishXSDModel: could not create directory " + xsdDir + ": " + e.getMessage());
                return;
            }
        }
        // Verify cmfFile directory exists
        Path cmfPath = Paths.get(cmfFile).getParent();
        if (!Files.exists(cmfPath)) {
            try {
                Files.createDirectories(cmfPath);
            } catch (IOException e) {
                Log.trace("Exception 2 in publishXSDModel: could not create directory " + cmfPath + ": " + e.getMessage());
                return;
            }
        }

        String execCommandXsd = properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_XSD_MODEL) + " "
                + xsdDir + " " + cmfFile;
        try {
            exec(execCommandXsd);
        } catch (IOException | InterruptedException e) {
            Log.trace("Exception 3 in publishXSDModel: " + e.getMessage());
            System.exit(1);
        }

        //}  
    }

    /**
     * * Publish XSD schemas from the NiemUmlModel.
     *
     * @param model
     */
    protected void publishXSD() {

        //String exportCmfToXsd = model.properties.getProperty(ProjectProperties.EXPORT_CMF_TO_XSD);
        ProjectProperties properties = model.properties;
        // use cmftool to generate XSDs from CMF
        //if (exportCmfToXsd.equals("true")) {
        Log.trace("Exporting CMF to XSD using cmftool");
        // Export CMF to XSD

        String xsdDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + properties.getProperty(ProjectProperties.EXPORT_XSD_DIR);
        String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + CmfWriter.getCmfFilename(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE),
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

        /* } else {
            
            // generate XSDs in niem-tools
            Log.trace("Exporting XSDs using niem-tools");
            try {
                
                // Create NIEM models
                model.createNIEM();
                
                // Cache models
                model.cacheModels(false);
                
                // export code lists
                String xmlDir = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator +
                        properties.getProperty(ProjectProperties.EXPORT_CODELISTS_DIR);
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
            } */
    }

    /**
     * Publish JSON schemas from the NiemUmlModel.
     *
     * @param model The NiemUmlModel instance.
     */
    protected void publishJSON() {
        //String exportCmfToJson = model.properties.getProperty(ProjectProperties.EXPORT_CMF_TO_JSON);
        ProjectProperties properties = model.properties;

        // use cmftool to generate JSON schemas from CMF
        //if (exportCmfToJson.equals("true")) {
        Log.trace("Exporting CMF to JSON schema using cmftool");

        String jsonFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + JsonWriter.getJsonFilename(properties.getProperty(ProjectProperties.EXPORT_JSON_SCHEMA_FILE));
        String cmfFile = properties.getProperty(ProjectProperties.EXPORT_PROJECT_DIR) + File.separator
                + CmfWriter.getCmfFilename(properties.getProperty(ProjectProperties.EXPORT_CMF_FILE),
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
        String execCommandXsd
                = properties.getProperty(ProjectProperties.EXPORT_CMFTOOL_TO_JSON) + " "
                + jsonFile + " " + cmfFile + " ";
        try {
            exec(execCommandXsd);
        } catch (IOException | InterruptedException e) {
            Log.trace("Exception 3 in publishJSON: " + e.getMessage());
            System.exit(1);
        }
        /* } else {
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
        }*/
    }
}
