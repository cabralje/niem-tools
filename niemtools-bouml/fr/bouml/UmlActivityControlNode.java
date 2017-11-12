package fr.bouml;

import java.io.*;
// import java.util.*;

abstract class UmlActivityControlNode extends UmlBaseActivityControlNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlActivityControlNode(long id, String s) {
    super(id, s);
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
    
    UmlCom.message(name());
    
    String s = sKind();
      
    html(s.substring(0, 1).toUpperCase().concat(s.substring(1)), null);
    
    fw.write("<p>Defined in ");
    parent().write();
    fw.write("</p>");
    
    write_children(pfix, rank, level);
    
    unload(false, false);
  }

}
