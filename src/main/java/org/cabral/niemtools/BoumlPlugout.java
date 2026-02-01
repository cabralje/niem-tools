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
 * establishes a connection to the BOUML tool via a specified port, loads
 * project properties, and executes various commands related to NIEM UML model
 * processing and artifact generation.
 * </p>
 *
 * <p>
 * Supported commands include:
 * <ul>
 * <li><b>addStereotype</b>: Adds NIEM stereotypes to UML elements.</li>
 * <li><b>removeStereotype</b>: Removes NIEM stereotypes from UML elements.</li>
 * <li><b>test</b>: Run in debugger mode.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The method ensures proper cleanup and termination of the connection to BOUML
 * upon completion or error.
 * </p>
 *
 * @author James Cabral
 * @version 1.0
 * @param argv Command-line arguments specifying the operation mode and options.
 */
package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import fr.bouml.UmlCom;
import fr.bouml.UmlItem;
import fr.bouml.UmlPackage;

public class BoumlPlugout {

    public static String filename = System.getProperty("java.io.tmpdir") + File.separator + "boumlport.txt";

    public static void main(String argv[]) {

        // get arguments
        ArrayList<String> args = new ArrayList<>(Arrays.asList(argv));
        String command;

        // locate the BOUML port
        // the program is called with the socket port number in argument
        int boumlPort = 0;
        try {
            // check for BOUML port from test harness
            File file = new File(filename);
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

        command = !args.isEmpty() ? args.get(0) : null;

        // handle test mode
        if (command != null && command.equals("test")) {
            // test mode - just write the port to the temp file and exit
            try {
                try (FileWriter out = new FileWriter(filename)) {
                    out.write(Integer.toString(boumlPort));
                }
            } catch (IOException e) {
                // nothing to do
            }
            System.exit(0);
        }

        // connect to BOUML port
        System.out.println("Connecting to BOUML on port " + boumlPort);
        Log.debug("Port: " + boumlPort + "\n");
        Log.debug("Classpath: " + System.getProperty("java.class.path") + "\n");
        try {
            UmlCom.connect(boumlPort);
        } catch (RuntimeException e) {
            System.out.println("Error connecting to BOUML: " + e.getMessage());
            System.exit(1);
        }
        UmlCom.message("Running NIEM Tools...");

        // Handle command line operations
        if (command != null) {

            UmlPackage project = UmlPackage.getProject();
            UmlItem target = UmlCom.targetItem();
            ProjectProperties properties = new ProjectProperties(project, ProjectProperties.getDefaults());
            properties.load();

            NiemUmlModel model = new NiemUmlModel(project, properties);

            switch (command) {
                case "addStereotype" -> {
                    model.addStereotype(target);
                    exitGracefully();
                }
                case "removeStereotype" -> {
                    model.removeStereotype(target);
                    exitGracefully();
                }
                case "debug" -> properties.setProperty(ProjectProperties.LOG_DEBUG, "true");
            }
        }

        // Start JavaFX UI via launcher (loaded only when available)
        if (!isJavaFxAvailable()) {
            Log.trace("JavaFX not available. Skipping UI.");
            return;
        }
        JavaFxLauncher.launch();
    }

    private static boolean isJavaFxAvailable() {
        try {
            Class.forName("javafx.application.Platform", false, BoumlPlugout.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void exitGracefully() {
        Log.trace("Done");
        UmlCom.message("");
        // must be called to cleanly inform that all is done
        UmlCom.bye(0);
        UmlCom.close();
        System.exit(0);
    }
}
