import java.io.*;

// the program is called with the socket port number in argument

class Main {
  public static void main(String argv[]) {

    if (argv.length >= 1) {
      UmlCom.connect(Integer.valueOf(argv[argv.length - 1]).intValue());

      try {
        UmlCom.trace("<b>BOUML2NIEM</b> release 0.1<br />");

        UmlItem target = UmlCom.targetItem();
	try {

	  UmlCom.message("Memorize references ...");
	  target.memo_ref();

          int argc = argv.length-1;
          switch (argv[0]) {
            case "importSchema":

              // Import NIEM schema
              UmlCom.trace("Importing NIEM schema");
              NiemTools.importSchemaDir();
              break;

            case "reset":

              // Globally reset NIEM Stereotype
              UmlCom.trace("Resetting NIEM stereotype");
              NiemTools.resetStereotype();
              break;

            case "import":

              // Import NIEM Mapping CSV file
	      UmlCom.trace("Deleting NIEM Mapping");
              NiemTools.deleteMapping();
	      UmlCom.trace("Importing NIEM Mapping");
              NiemTools.importCsv();
              break;

            default:

            // Import NIEM schema
            UmlCom.trace("Importing NIEM schema");
            NiemTools.importSchemaDir();
            
	      // Generate UML Model HTML documentation
//	      target.set_dir(argv.length - 1, argv);
	      target.set_dir(0,null);
  	      UmlItem.frame();
	      UmlCom.message("Indexes ...");
	      UmlItem.generate_indexes();
	      UmlItem.start_file("index", target.name() + "\nDocumentation", false);
	      target.html(null, 0, 0);
	      UmlItem.end_file();
	      UmlItem.start_file("navig", null, true);
	      UmlItem.end_file();
	      UmlClass.generate();

        // Generate NIEM Mapping HTML
	      UmlCom.trace("Generating NIEM Mapping");
        NiemTools.exportHtml();

        // Generate NIEM Mapping CSV
	      UmlCom.trace("Generating NIEM Mapping CSV");
        NiemTools.exportCsv();

        // Generate NIEM Wantlist instance
	      UmlCom.trace("Generating NIEM Wantlist");
        NiemTools.exportWantlist();
        break;
	  }
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
