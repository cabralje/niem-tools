package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
class UmlActivityPin extends UmlBaseActivityPin {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityPin(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity action pin";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Activity action pin <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    super.html(pfix, rank, level);
  
    unload(false, false);
  }

}
