package fr.bouml;

import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
class UmlRolePart extends UmlBaseRolePart {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRolePart(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return (isPart()) ? "part" : "role";
  
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write((isPart())
  	   ? "<table><tr><td><div class=\"element\">Part <b>"
  	   : "<table><tr><td><div class=\"element\">Role <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    write_description();
  
    fw.write("<p>Type : ");
    type().write();
    fw.write("</p>");
    
    if (multiplicity().length() != 0) {
      fw.write("<p>Multiplicity : ");
      writeq(multiplicity());
      fw.write("</p>");
    }
  
    write_properties();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

}
