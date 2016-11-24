import java.io.*;
// import java.util.*;


class UmlActivityPartition extends UmlBaseActivityPartition implements UmlActivityItem {
  public boolean chapterp() {
    return true;
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity partition";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
    
    chapter("Activity partition", pfix, rank, "activitypartition", level);
  
    write_description();
    
    if (isDimension())
      fw.write((isExternal())
  	     ? "<p>is dimension, is external</p>\n"
  	     : "<p>is dimension</p>\n");
    else if (isExternal())
      fw.write("<p>is external</p>\n");
      
    if (represents() != null) {
      fw.write("<p>represents ");
      represents().write();
      fw.write("</p>");
    }
      
    write_dependencies();
    
    UmlDiagram d = associatedDiagram();
    
    if (d != null) {
      fw.write("<p>Diagram : ");
      d.write();
      fw.write("</p>\n");
    }
  
    write_properties();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityPartition(long id, String s) {
    super(id, s);
  }

}
