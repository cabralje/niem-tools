package fr.bouml;
import java.io.*;

// the program is called with the socket port number in argument

class Main {
  public static void main(String argv[]) {

    if (argv.length >= 1) {
      UmlCom.connect(Integer.valueOf(argv[argv.length - 1]).intValue());

      try {
        UmlCom.trace("<b>Html generator</b> release 6.4 (Java version)<br />");

        UmlItem target = UmlCom.targetItem();
	try {
	  target.set_dir(argv.length - 1, argv);
	  UmlCom.message("Memorize references ...");
	  target.memo_ref();
	  UmlItem.frame();
	  UmlCom.message("Indexes ...");
	  UmlItem.generate_indexes();
	  UmlItem.start_file("index", target.name() + "\nDocumentation", false);
	  target.html(null, 0, 0);
	  UmlItem.end_file();
	  UmlItem.start_file("navig", null, true);
	  UmlItem.end_file();
	  UmlClass.generate();
	  UmlCom.trace("Done");
	  UmlCom.message("");
	}
	catch (IOException e) {
	}
      }
      finally {
        // must be called to cleanly inform that all is done
        UmlCom.bye(0);
        UmlCom.close();
      }
    }
    System.exit(0);
  }
}
