package fr.bouml;

import java.io.IOException;

abstract class UmlActivityAction extends UmlBaseActivityAction {
  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html() throws IOException {
    UmlCom.message(name());
  
    String s = sKind();
    
    s = s.substring(0, 1).toUpperCase() + s.substring(1);
      
    html(s, (UmlDiagram) associatedDiagram());
    
    fw.write("<p>Defined in ");
    parent().write();
    fw.write("</p>");
    
    if (isLocallyReentrant())
      fw.write("<p>is locally reentrant</p>");
  
    String scpp, sjava;
    
    s = preCondition();
    scpp = cppPreCondition();
    sjava = javaPreCondition();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Pre Condition :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
    
    s = postCondition();
    scpp = cppPostCondition();
    sjava = javaPostCondition();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Post Condition :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    s = constraint();
    
    if (s.length() != 0) {
      fw.write("<p> Constraint :</p><ul>");
      writeq(s);
      fw.write("</ul>");
    }
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlActivityAction(long id, String s) {
    super(id, s);
  }

}
