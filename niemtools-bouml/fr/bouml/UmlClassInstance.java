package fr.bouml;
import java.io.*;
// import java.util.*;


public class UmlClassInstance extends UmlBaseClassInstance {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlClassInstance(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "class instance";
  
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Class instance <b>");
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
  
    SlotRelation[] vr = relationsValue();
    
    n = vr.length;
    
    if (n != 0) {
      fw.write("<p>relations :</p><ul>\n");
      
      while (n-- > 0) {
        SlotRelation slot = vr[n];
  
        fw.write("<li>");
        slot.relation.write();
        fw.write(" = ");
        slot.value.write();
        fw.write("</li>\n");
      }
      fw.write("</ul>");
    }
    
    write_properties();
  
    unload(false, false);
  
  }

}
