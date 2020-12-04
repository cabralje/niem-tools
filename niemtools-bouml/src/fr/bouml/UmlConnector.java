package fr.bouml;

import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
class UmlConnector extends UmlBaseConnector {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlConnector(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "connector";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Connector <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
  
    write_description();
    
    fw.write("<p>Connecting : ");
    connectable().write();
    fw.write("</p>");
    
    if (multiplicity().length() != 0) {
      fw.write("<p>Multiplicity : ");
      writeq(multiplicity());
      fw.write("</p>");
    }
    
    write_properties();
   
    unload(false, false);
  }

}
