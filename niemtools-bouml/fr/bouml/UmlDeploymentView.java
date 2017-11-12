package fr.bouml;

import java.io.*;

/**
 *  This class manages 'deployment view'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlDeploymentView extends UmlBaseDeploymentView {
  public UmlDeploymentView(long id, String n){ super(id, n); }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "Deployment View", level, "view");
  
    unload(false, false);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "deployment view";
  }

  public boolean chapterp() {
    return true;
  }

};
