package fr.bouml;

import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
class UmlPort extends UmlBasePort {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlPort(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "port";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Port <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    boolean need;
    
    if (isReadOnly()) {
      need = false;
      fw.write("<p>isReadOnly");
    }
    else
      need = true;
    
    if (isDerived()) {
      if (need) { need = false; fw.write("<p>"); } else fw.write(", ");
      fw.write("isDerived");
    }
    
    if (isBehavior()) {
      if (need) { need = false; fw.write("<p>"); } else fw.write(", ");
      fw.write("isBehavior");
    }
    
    if (isConjugated()) {
      if (need) { need = false; fw.write("<p>"); } else fw.write(", ");
      fw.write("isConjugated");
    }
    
    if (isService()) {
      if (need) { need = false; fw.write("<p>"); } else fw.write(", ");
      fw.write("isService");
    }
  
    if (need)
      fw.write("</p>");
      
    write_description();
  
    fw.write("<p>Type : ");
    type().write();
    fw.write("</p>");
    
    if (multiplicity().length() != 0) {
      fw.write("<p>Multiplicity : ");
      writeq(multiplicity());
      fw.write("</p>");
    }
  
    if (protocol() != null)  {
      fw.write("<p>Protocol : ");
      protocol().write();
      fw.write("</p>");
    }
    
    if (defaultValue().length() != 0) {
      fw.write("<p>Default Value : ");
      writeq(defaultValue());
      fw.write("</p>");
    }
  
    UmlClass[] cls;
    
    cls = providedClasses();
  
    if (cls.length != 0) {
      String sep = "<p>provided classes : ";
      
      for (int i = 0; i != cls.length; i += 1) {
        fw.write(sep);
        sep = ", ";
        cls[i].write();
      }
      fw.write("</p>\n");
    }
  
    cls = requiredClasses();
  
    if (cls.length != 0) {
      String sep = "<p>required classes : ";
      
      for (int i = 0; i != cls.length; i += 1) {
        fw.write(sep);
        sep = ", ";
        cls[i].write();
      }
      fw.write("</p>\n");
    }
  
    write_properties();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

}
