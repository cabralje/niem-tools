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
package org.cabral.niemtools;

import java.io.FileWriter;
import java.io.IOException;

public class TestHarness {

    public static String filename = "C:\\tmp\\boumlport.txt";

    public static void main(String argv[]) {

        int boumlPort = 0;
        if (argv.length >= 1) {
            boumlPort = Integer.parseInt(argv[argv.length - 1]);

        }

        try {
            try (FileWriter out = new FileWriter(filename)) {
                out.write(Integer.toString(boumlPort));
            }
        } catch (IOException e) {
            // nothing to do
        }
        System.exit(0);
    }
}
