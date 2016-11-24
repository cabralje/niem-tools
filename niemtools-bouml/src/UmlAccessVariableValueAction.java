
import java.io.*;
// import java.util.*;

abstract class UmlAccessVariableValueAction extends UmlBaseAccessVariableValueAction {
  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (variable() != null){
      fw.write("<p>Variable : ");
      variable().write();
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlAccessVariableValueAction(long id, String s) {
    super(id, s);
  }

}
