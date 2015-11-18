
import java.io.*;
import java.util.*;

class UmlActivityObject extends UmlBaseActivityObject {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityObject(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity object";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Activity object <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    html_internal(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html_internal(String pfix, int rank, int level) throws IOException {
    String s = description();
    
    if (s.length() != 0) {
      fw.write("<p>");
      writeq(s);
      fw.write("<br /></p>");
    }
  
    fw.write("<p>Type : ");
    write(type(), aLanguage.cppLanguage);
    fw.write("</p>");
    
    if (multiplicity().length() != 0) {
      fw.write("<p>Multiplicity : ");
      writeq(multiplicity());
      fw.write("</p>");
    }
    
    if (inState().length() != 0) {
      fw.write("<p>In State : ");
      writeq(inState());
      fw.write("</p>");
    }
    
    s = selection();
    
    String scpp = cppSelection();
    String sjava = javaSelection();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Selection :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
    
    if (ordering() != anOrdering.unordered) {
      fw.write("<p>Ordering : ");
      write(ordering());
      fw.write("</p>");
    }
  
    if (isControlType())
      fw.write("<p>Is control type</p>\n");
  
    UmlActivityDiagram d = associatedDiagram();
    
    if (d != null) {
      fw.write("<p>Diagram : ");
      d.write();
      fw.write("</p>");
    }
  
    write_properties();
  
    write_children(pfix, rank, level);
  }

}
