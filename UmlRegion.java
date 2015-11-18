
import java.io.*;

class UmlRegion extends UmlBaseRegion implements UmlStateItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRegion(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "region";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "State region", level, "stateregion");
  
    unload(false, false);
  }

  public boolean chapterp() {
    return true;
  }

}
