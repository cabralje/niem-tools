import java.io.*;

class UmlEntryPointPseudoState extends UmlBaseEntryPointPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "entry point pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlEntryPointPseudoState(long id, String s) {
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
    if (parent().kind() == anItemKind.aRegion)
      parent().parent().write();
    else
      parent().write();
     fw.write("</p>");
    
    if (reference() != null) {
      fw.write("<p>References ");
      reference().write();
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
    
    unload(false, false);
  }

}
