package fr.bouml;

import java.io.*;

/**
 *  This class allows to manage extra class member, It allows to insert
 *  C++ pre-processor directive (even they may be placed in the other member
 *  definition/declaration), etc ...
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlExtraClassMember extends UmlBaseExtraClassMember {
  public UmlExtraClassMember(long id, String n) { super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "extra class member";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Extra Class Member <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
  
    String s;
  
    if ((cppDecl().length() != 0) || (javaDecl().length() != 0)) {
      fw.write("<p>Definition :</p><ul>");
      
      s = cppDecl();
      
      if (s.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      s = javaDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
  
      fw.write("</ul>");
    }
   
    write_description();
  
    unload(false, false);
  }

}
