package fr.bouml;

import java.io.*;
// import java.util.*;

abstract class UmlOnSignalAction extends UmlBaseOnSignalAction {
  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    String s = signal();
    String scpp = cppSignal();
    String sjava = javaSignal();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Signal :</p><ul>");
  
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
  protected  UmlOnSignalAction(long id, String s) {
    super(id, s);
  }

}
