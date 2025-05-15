package org.cabral.niemtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.bouml.UmlBasePackage;
import fr.bouml.UmlClass;
import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

// the program is called with the socket port number in argument
public class NiemtoolsBouml {

    public static void main(String argv[]) {
        Log.start("main");
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            //UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            Log.trace("Exception: " + e.toString());
        }

        // check for BOUML port from test harness
        int boumlPort = 0;
        try {
            File file = new File(TestHarness.filename);
            String buffer = new String(Files.readAllBytes(file.toPath()));
            boumlPort = Integer.parseInt(buffer);
            Log.debug("Port: " + boumlPort + "\n");

            // delete file - BOUML ports are one-time use
            file.delete();
        } catch (IOException | NumberFormatException e) {
            // get BOUML port from command line 
            if (argv.length >= 1) {
                try {
                    boumlPort = Integer.parseInt(argv[argv.length - 1]);
                } catch (NumberFormatException e2) {
                }
            }
        }

        if (boumlPort == 0) {
            System.out.println("No BOUML port.  Exiting.");
            System.exit(1);
        }

        System.out.println("Connecting to BOUML on port " + boumlPort);
        UmlCom.connect(boumlPort);
        Log.debug("Port: " + boumlPort + "\n");
        Log.debug("Classpath: " + System.getProperty("java.class.path") + "\n");
        UmlPackage root;
        UmlItem target;
        NiemUmlClass niemTools;

        try {
            //UmlCom.trace("<b>BOUML NIEM tools</b> release 0.1<br />");
            root = UmlBasePackage.getProject();
            //String propFile = System.getProperty("user.home") + "/" + root.name() + ".properties";

            Log.start("memo_ref");
            UmlCom.message("Memorize references ...");
            target = UmlCom.targetItem();
            target.memo_ref();
            Log.stop("memo_ref");
        } catch (Exception e) {
            System.out.print("Exception: " + e.getMessage());
            return;
        }

        try {
            // create PIM and PSM
            niemTools = new NiemUmlClass();

            //load properties
            //Properties properties = new Properties();
            //FileReader in;
            //try {
            //    in = new FileReader(propFile);
            //    properties.load(in);
            //    in.close();
            //} catch (FileNotFoundException e) {
            //    Log.trace("Properties file " + propFile + " does not exist.");
            //    command = "configure";
            //} catch (IOException ex) {
            //}
        } catch (Exception e) {
            System.out.print("Exception: " + e.getMessage());
            return;
        }

        try {
            String command = argv[0];
            switch (command) {
                case "configure":
                    //ConfigurationDialog configDialog = new ConfigurationDialog(root, properties);
                    //configDialog.setVisible(true); // Ensure the dialog is displayed
                    break;

                case "importSchema":
                    //niemTools.cacheModels();
                    importSchema(root, niemTools);
                    break;

                case "import":
                    niemTools.cacheModels(false);
                    importMapping(root, niemTools);
                    break;

                case "sort":
                    Log.trace("<b>Sort</b> release 5.0<br>");
                    niemTools.cacheModels(false);
                    UmlCom.targetItem().sort();
                    break;

                case "addStereotype":
                    Log.trace("<b>Add Stereotype<b>");
                    niemTools.cacheModels(false);
                    niemTools.addStereotype(UmlCom.targetItem());
                    Log.trace("<b>Add Stereotype complete<b>");
                    break;

                case "removeStereotype":
                    Log.trace("<b>Remove Stereotype<b>");
                    niemTools.cacheModels(false);
                    niemTools.removeStereotype(UmlCom.targetItem());
                    break;

                case "export":
                default:
                    if (!niemTools.verifyNIEM())
                        break;
                    niemTools.cacheModels(false);
                    generateModels(root, target, niemTools);
            }
            // store properties
            //try {
            //    Log.trace("Storing properties to " + propFile);
            //    try (FileWriter out = new FileWriter(propFile)) {
            //        properties.setProperty("htmlDir", root.propertyValue("html dir"));
            //        properties.store(out, "BOUML NiemTools plugout settings");
            //    }
            //} catch (IOException e) {
            //    Log.trace("Unable to write properties to " + propFile);
            //}
        } catch (IOException e) {
            Log.trace("IOException: " + e.getMessage());
        } catch (NumberFormatException e) {
            Log.trace("NumberFormatException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.trace("IllegalArgumentException: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.trace("NullPointerException: " + e.getMessage());
        } catch (RuntimeException e) {
            Log.trace("RuntimeException: " + e.getMessage());
        }
        finally {
            Log.trace("Done");
            UmlCom.message("");
            Log.stop("main");
            // must be called to cleanly inform that all is done
            UmlCom.bye(0);
            UmlCom.close();
        }
        System.exit(0);
    }

    /**
     * @param root
     * @param target
     * @param niemTools
     * @param properties
     * @throws IOException
     */

    private static void generateModels(UmlPackage root, UmlItem target, NiemUmlClass niemTools)
            throws IOException {
        Log.start("generateModels");

                // Configure generation options
        JFileChooser fc = null;
        String projectDir = root.propertyValue("projectDir");
        if (projectDir == null || projectDir.equals("")) {
            fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Project directory where artifacts will be generated");
            if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
                return;
        }
        if (fc != null) {
            projectDir = fc.getSelectedFile().getAbsolutePath();
            root.set_PropertyValue("projectDir", projectDir);
        }
        String xsdDir = null, xmlExampleDir = null, wsdlDir = null, jsonDir = null, jsonExampleDir = null, openapiDir = null, cmfDir = null;
        String exportXML = root.propertyValue("exportXML");
        if (exportXML == null) {
            exportXML = "true";
            root.set_PropertyValue("exportXML", exportXML);
        }
        if (exportXML.equals("true")) {
            //xsdDir = root.propertyValue("xsdDir");
            //xmlExampleDir = root.propertyValue("xmlExampleDir");
            xsdDir = projectDir + "\\schema";
            xmlExampleDir = projectDir + "\\examples"; 
        }
        
        String exportWSDL = root.propertyValue("exportWSDL");
        if (exportWSDL == null) {
            exportWSDL = "true";
            root.set_PropertyValue("exportWSDL", exportWSDL);
        }
        if (exportWSDL.equals("true")) {
            //wsdlDir = root.propertyValue("wsdlDir");
            wsdlDir = projectDir + "/WS-SIP";
        }
        String exportJSON = root.propertyValue("exportJSON");
        if (exportJSON == null) {
            exportJSON = "true";
            root.set_PropertyValue("exportJSON", exportJSON);
        }
        if (exportJSON.equals("true")) {
            // jsonDir = root.propertyValue("jsonDir");
            // jsonExampleDir = root.propertyValue("jsonExampleDir");
            jsonDir = projectDir + "\\json\\schema";
            jsonExampleDir = projectDir + "\\json\\examples";
        }
        String exportOpenAPI = root.propertyValue("exportOpenAPI");
        if (exportOpenAPI == null) {
            exportOpenAPI = "true";
            root.set_PropertyValue("exportOpenAPI", exportOpenAPI);
        }
        if (exportOpenAPI.equals("true")) {
            // openapiDir = root.propertyValue("openapiDir");
            openapiDir = projectDir + "\\json";
        }
        String cmfVersion = root.propertyValue("cmfVersion");
        if (cmfVersion == null) {
            cmfVersion = CmfWriter.CMF_VERSION;
            root.set_PropertyValue("cmfVersion", cmfVersion);
        }
        if (cmfVersion != null && !cmfVersion.equals("")) {
            // cmfDir = root.propertyValue("cmfDir");
            cmfDir = projectDir + "\\cmf";
        }
        String modelDir = root.propertyValue("html dir");
        if (modelDir == null || modelDir.equals("")) {
            modelDir = projectDir + "\\model";
            root.set_PropertyValue("html dir", modelDir);
        }

        // Generate UML Model HTML documentation
        if (root.propertyValue("exportHTML").equals("true")) {
            Log.trace("Generating HTML documentation");
            //	target.set_dir(argv.length - 1, argv);
            //String[] params = {root.propertyValue("html dir")};
            String[] params = {modelDir};
            target.set_dir(1, params);
            //target.set_dir(0,null);
            UmlItem.frame();
            UmlCom.message("Indexes ...");
            Log.start("generate_indexes");
            UmlItem.generate_indexes();
            Log.stop("generate_indexes");
            UmlItem.start_file("index", target.name() + "\nDocumentation", false);
            target.html(null, 0, 0);
            UmlItem.end_file();
            UmlItem.start_file("navig", null, true);
            UmlItem.end_file();
            Log.start("generate");
            UmlClass.generate();
            Log.stop("generate");
        }

        // Generate NIEM Mapping HTML
        niemTools.exportHtml(modelDir, "niem-mapping");

        // Generate NIEM Mapping CSV
        niemTools.exportCsv(modelDir, "niem-mapping.csv");

        // Clearing NIEM Models
        niemTools.deleteNIEM(false);
        niemTools.createNIEM();
        niemTools.cacheModels(false);

        // Generating NIEM Models
        niemTools.createSubsetAndExtension();

        // Generate NIEM Wantlist instance
        niemTools.exportWantlist(modelDir, "wantlist.xml");

        // Cache subsitutions
        niemTools.cacheModels(false);

        // Generate schemas
        niemTools.exportSpecification(xsdDir, wsdlDir, jsonDir, openapiDir, xmlExampleDir, jsonExampleDir, cmfDir, cmfVersion);
        Log.stop("generateModels");
    }

    /**
     * @param root
     * @param niemTools
     */
    public static void importMapping(UmlPackage root, NiemUmlClass niemTools) {
        Log.start("importMapping");
        niemTools.deleteMapping();
        JFileChooser fc2 = new JFileChooser(root.propertyValue("html dir"));
        fc2.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        fc2.setDialogTitle("NIEM Mapping CSV file");
        if (fc2.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
            return;
        String filename = fc2.getSelectedFile().getAbsolutePath();
        niemTools.importCsv(filename);
        Log.stop("importMapping");
    }

    /**
     * @param niemTools
     * @param properties
     * @throws IOException
     */
    public static void importSchema(UmlPackage root, NiemUmlClass niemTools) throws IOException {
        Log.start("importSchema");
        // Create PIM
        //NiemTools.createPIM(root);

        String maxEnumsString = root.propertyValue("maxEnums");
        Integer maxEnums = null;
        if (maxEnumsString != null && !maxEnumsString.equals("")) {
            try {
                maxEnums = Integer.valueOf(maxEnumsString);
            } catch (NumberFormatException | NullPointerException e) {
            }
        }
        if (maxEnums == null) {
            maxEnums = NiemModel.MAX_ENUMS;
            root.set_PropertyValue("maxEnums", maxEnums.toString());
        }
        NiemModel.maxEnums = maxEnums;

        // Import schema
        // in java it is very complicated to select
        // a directory through a dialog, and the dialog
        // is very slow and ugly
        JFileChooser fc = new JFileChooser(root.propertyValue("niemDir"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Directory of the schema to be imported");
        if (fc.showOpenDialog(new JFrame()) != JFileChooser.APPROVE_OPTION)
            return;
        String directory = fc.getSelectedFile().getAbsolutePath();
        root.set_PropertyValue("niemDir", directory);

        niemTools.deleteNIEM(true);
        niemTools.createNIEM();
        niemTools.cacheModels(true);
        niemTools.importSchemaDir(directory);
        Log.stop("importSchema");
    }
}
