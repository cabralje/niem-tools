// the program is called with the socket port number in argument

class Main {
  public static void main(String argv[]) {

    if (argv.length == 1) {
      UmlCom.connect(Integer.valueOf(argv[0]).intValue());

      try {
        // does something on the target, here suppose that a virtual
        // operation exist at UmlItem level (and probably sub_level !)
        UmlCom.trace("<b>Sort</b> release 5.0<br>");
        UmlCom.targetItem().sort();
        UmlCom.trace("Done<br>");
      }
      finally {
        // must be called to cleanly inform that all is done
        UmlCom.bye();
        UmlCom.close();
      }
    }
    System.exit(0);
  }
}
