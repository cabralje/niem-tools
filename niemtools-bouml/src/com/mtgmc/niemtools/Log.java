package com.mtgmc.niemtools;

import java.util.HashMap;

import fr.bouml.UmlCom;

public class Log {

	// Debugging options
	//private static Boolean _DEBUG = true;
	private static final Boolean _DEBUG = false;

	//private static final Boolean _PROFILE = true;
	private static final Boolean _PROFILE = false;
	
	private static HashMap<String, Long> timer = new HashMap<String, Long>();
	
	/** outputs debugging information */
	static void debug(String output) {
		if (_DEBUG)
			Log.trace(output);
	}

	public static void trace(String s) {
		UmlCom.trace(s);
	}

	public static void start(String s) {
		if (_PROFILE) {
			trace("ELAPSED TIME (" + s + "): starting timer");
			timer.put(s, System.nanoTime());
		}
	}
	
	public static void stop(String s) {
		if (_PROFILE) {
			if (!timer.containsKey(s)) {
				trace("ELAPSED TIME: (" + s + "): not set");
				return;
			}
			long startTime = timer.get(s);
			long stopTime = System.nanoTime();
			long elapsedTime = (stopTime - startTime)/1000000000L;
			trace("ELAPSED TIME (" + s + "): " + elapsedTime + " sec");
			timer.remove(s);
		}
	}
}
