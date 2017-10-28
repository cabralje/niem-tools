package fr.bouml;

import java.io.*;
import java.util.*;

@SuppressWarnings("rawtypes")
class UmlActivity extends UmlBaseActivity {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivity(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    chapter("Activity", pfix, rank, "activity", level);
  
    write_description();
    
    if (isActive())
      fw.write("<p>Active activity</p>\n");
  
    UmlOperation beh = specification();
    
    if (beh != null) {
      fw.write("<p>Implements ");
      beh.write();
      fw.write("</p>");
    }
  
    if (isReadOnly()) {
      if (isSingleExecution())
        fw.write("<p>Read only, single execution</p>");
      else
        fw.write("<p>Read only</p>");
    }
    else if (isSingleExecution())
      fw.write("<p>Single execution</p>");
  
    String scpp = cppPreCondition();
    String sjava = javaPreCondition();
    String s = preCondition();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Pre Condition :</p><ul>");
      
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
    
    s = postCondition();
    scpp = cppPostCondition();
    sjava = javaPostCondition();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Post Condition :</p><ul>");
      
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
  
    s = constraint();
    
    if (s.length() != 0) {
      fw.write("<p> Constraint :</p><ul>");
      writeq(s);
      fw.write("</ul>");
    }
   
    UmlActivityDiagram d = associatedDiagram();
    
    if (d != null) {
      fw.write("<p>Diagram : ");
      d.write();
      fw.write("</p>");
    }
    
    write_properties();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   * set the html ref
   * set the activity list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    activities.addElement(this);
    super.memo_ref();
  
  }

  public static void ref_index() throws IOException
  {
    if (!activities.isEmpty())
      fw.write("<a href=\"activities.html\" target = \"projectFrame\"><b> -Activities- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(activities, "Activities", "activities");
  }

  public boolean chapterp() {
    return true;
  }

protected static Vector activities;

  static { activities = new Vector(); }
}
