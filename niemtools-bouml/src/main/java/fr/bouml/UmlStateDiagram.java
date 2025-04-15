package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
class UmlStateDiagram extends UmlBaseStateDiagram implements UmlStateItem {
  /**
   * set he html ref
   * set the diagrams list
   */
  public void memo_ref() {
    diagrams.addElement(this);
    super.memo_ref();
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "state diagram";
  }

  public static void ref_index() throws IOException
  {
    if (!diagrams.isEmpty())
      fw.write("<a href=\"statediagrams.html\" target = \"projectFrame\"><b> -State Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
   generate_index(diagrams, "State Diagram", "statediagrams");
  }

  protected static Vector<UmlStateDiagram> diagrams;

  static { diagrams = new Vector(); }  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlStateDiagram(long id, String s) {
    super(id, s);
  }

}
