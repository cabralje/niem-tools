package fr.bouml;

import java.io.*;
import java.util.*;


@SuppressWarnings("unused")
class UmlRolePartInstance extends UmlBaseRolePartInstance {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRolePartInstance(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return (type().isPart()) ? "part instance" : "role instance";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write((type().isPart())
  	   ? "<table><tr><td><div class=\"element\">Part instance  <b>"
  	   : "<table><tr><td><div class=\"element\">Role instance  <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
    
    write_description();
  
    fw.write("<p>type :");
    type().write();
    fw.write("</p>");
  
    SlotAttribute[] va = attributesValue();
    int n = va.length;
    
    if (n != 0) {
      fw.write("<p>attributes :</p><ul>\n");
      
      while (n-- > 0) {
        SlotAttribute slot = va[n];
  
        fw.write("<li>");
        slot.attribute.write();
        fw.write(" = ");
        writeq(slot.value);
        fw.write("</li>\n");
      }
      fw.write("</ul>");
    }
    
    write_properties();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  
  }

}
