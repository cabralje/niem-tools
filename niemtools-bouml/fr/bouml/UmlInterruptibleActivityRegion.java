package fr.bouml;

import java.io.*;
// import java.util.*;

class UmlInterruptibleActivityRegion extends UmlBaseInterruptibleActivityRegion {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlInterruptibleActivityRegion(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "interruptible activity region";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "Interruptible activity region", level, "interruptibleactivityregion");
  
    unload(false, false);
  }

}
