package fr.bouml;


import java.io.*;

class UmlExtraArtifactDefinition extends UmlBaseExtraArtifactDefinition {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlExtraArtifactDefinition(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "extra artifact definition";
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
  
    if ((cppDef().length() != 0) || (cppDecl().length() != 0) || (javaDecl().length() != 0)) {
      fw.write("<p>Definition :</p><ul>");
      
      s = cppDecl();
      
      if (s.length() != 0) {
        fw.write("<li>C++ (decl): <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      s = cppDef();
      
      if (s.length() != 0) {
        fw.write("<li>C++ (def): <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      s = javaDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      s = phpDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Php : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      s = pythonDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Python : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
  
      fw.write("</ul>");
    }
   
    write_description();
  
    unload(false, false);
  }

}
