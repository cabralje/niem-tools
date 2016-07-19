
import java.io.*;

/**
 *  This class manages 'use case view'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlUseCaseView extends UmlBaseUseCaseView {
  public UmlUseCaseView(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "use case view";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "Use Case View", level, "view");
  
    unload(false, false);
  }

  public boolean chapterp() {
    return true;
  }

};
