package fr.bouml;


import java.io.*;

/**
 *  This class manages 'component view'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlComponentView extends UmlBaseComponentView {
  public UmlComponentView(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "component view";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "Component View", level, "view");
  
    unload(false, false);
  }

  public boolean chapterp() {
    return true;
  }

};
