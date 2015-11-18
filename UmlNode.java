
import java.io.*;

/**
 *  This class manages 'nodes'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlNode extends UmlBaseNode {
  public UmlNode(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "node";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
  
    html("Node", associatedDiagram());
  
    unload(false, false);
  }

};
