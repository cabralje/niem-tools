package org.cabral.niemtools;

import java.io.FileWriter;
import java.io.IOException;

public class TestHarness {

	public static String filename = "C:\\tmp\\boumlport.txt";
	
	public static void main(String argv[]) {

		if (argv.length >= 1)
		{
			int boumlPort = Integer.parseInt(argv[argv.length - 1]);
			
			try {
                            try (FileWriter out = new FileWriter(filename)) {
                                out.write(Integer.toString(boumlPort));
                            }
			} catch (IOException e) {
				// nothing to do
			}
		}
		System.exit(0);
	}
}
