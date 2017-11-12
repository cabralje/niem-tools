package fr.bouml;

import java.io.*;
//import java.util.*;

class UmlParameterSet extends UmlBaseParameterSet implements UmlActivityItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlParameterSet(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "parameter set";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    fw.write("<table><tr><td><div class=\"element\">Parameter set <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
    
    write_description();
  
    fw.write("<p>Contains :");
    
    UmlActivityPin[] p = pins();
    
    for (int i = 0; i != p.length; i += 1) {
      fw.write(" ");
      p[i].write();
    }
  
    fw.write("</p>");
  
    write_properties();
  
    unload(false, false);
  }

}
