package fr.bouml;

import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
class UmlPortRef extends UmlBasePortRef {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlPortRef(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "port reference";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Port reference <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
    
    fw.write("<p>port :");
    port().write();
    fw.write("</p>");
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  
  }

}
