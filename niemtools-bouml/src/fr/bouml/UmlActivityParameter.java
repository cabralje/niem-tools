package fr.bouml;

import java.io.*;
// import java.util.*;

class UmlActivityParameter extends UmlBaseActivityParameter {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityParameter(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity parameter";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Activity parameter <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    String s = defaultValue();
    
    if (s.length() != 0) {
      fw.write("<p>Default value :</p><ul>");
      fw.write("<pre>\n");
      writeq(s);
      fw.write("</pre></ul>");
    }
  
    super.html(pfix, rank, level);
  
    unload(false, false);
  }

}
