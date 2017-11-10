package com.mtgmc.niemtools;

import fr.bouml.UmlCom;

public class Log {

	// Debugging options
	//private static Boolean _DEBUG = true;
	private static final Boolean _DEBUG = false;

	/** outputs debugging information */
	static void debug(String output) {
		if (_DEBUG)
			Log.trace(output);
	}

	public static void trace(String s) {
		UmlCom.trace(s);
	}

}
