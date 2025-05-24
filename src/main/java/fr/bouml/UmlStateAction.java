package fr.bouml;


import java.io.*;

class UmlStateAction extends UmlBaseStateAction implements UmlStateItem {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "state action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
  
    String s = stereotype();
    
    if (s == null)
      s = "State action";
    else
      s = "State action " + s;
      
    html(s, null);
    
    fw.write("<p>Defined in ");
    if (parent().kind() == anItemKind.aRegion)
      parent().parent().write();
    else
      parent().write();
     fw.write("</p>");
  
    s = expression();
    
    String scpp = cppExpression();
    String sjava = javaExpression();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Expression :</p><ul>");
      
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
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlStateAction(long id) {
    super(id, "");
  }

}
