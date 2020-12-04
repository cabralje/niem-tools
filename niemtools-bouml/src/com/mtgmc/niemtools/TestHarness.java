package com.mtgmc.niemtools;

import java.io.FileWriter;

public class TestHarness {

	public static String filename = "C:\\tmp\\boumlport.txt";
	
	public static void main(String argv[]) {

		if (argv.length >= 1)
		{
			int boumlPort = Integer.valueOf(argv[argv.length - 1]).intValue();
			
			try {
				FileWriter out = new FileWriter(filename);
				out.write(Integer.toString(boumlPort));
				out.close();
			} catch (Exception e) {
				// nothing to do
			}
		}
		System.exit(0);
	}
}
