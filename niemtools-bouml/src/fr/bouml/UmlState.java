package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("rawtypes")
class UmlState extends UmlBaseState implements UmlStateItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlState(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return (parent().kind() == anItemKind.aClassView)
      ? "state machine" : "state";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    chapter((parent().kind() == anItemKind.aClassView)
  	  ? "StateMachine" : "State",
  	  pfix, rank, "state", level);
  
    write_description();
    
    UmlState ref = reference();
    
    if (ref != null) {
      fw.write("<p>References ");
      ref.write();
      fw.write("</p>");
    }
    else {
      if (isActive())
        fw.write("<p>Active state</p>\n");
      
      UmlOperation beh = specification();
      
      if (beh != null) {
        fw.write("<p>Implements ");
        beh.write();
        fw.write("</p>");
      }
      
      String scpp = cppEntryBehavior();
      String sjava = javaEntryBehavior();
      String s = entryBehavior();
      
      if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
        fw.write("<p>Entry Behavior :</p><ul>");
        
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
      
      s = exitBehavior();
      scpp = cppExitBehavior();
      sjava = javaExitBehavior();
      
      if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
        fw.write("<p>Exit Behavior :</p><ul>");
        
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
      
      s = doActivity();
      scpp = cppDoActivity();
      sjava = javaDoActivity();
      
      if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
        fw.write("<p>Do activity :</p><ul>");
        
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
    }
   
    UmlStateDiagram d = associatedDiagram();
    
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
   * set the state list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    states.addElement(this);
    super.memo_ref();
  
  }

  public static void ref_index() throws IOException
  {
    if (!states.isEmpty())
      fw.write("<a href=\"states.html\" target = \"projectFrame\"><b> -States- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(states, "States", "states");
  }

  public boolean chapterp() {
    return true;
  }

  protected static Vector states;

  static { states = new Vector(); }
}
