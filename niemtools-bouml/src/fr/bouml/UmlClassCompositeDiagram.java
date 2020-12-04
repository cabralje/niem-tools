package fr.bouml;


import java.io.*;
import java.util.*;


@SuppressWarnings("rawtypes")
class UmlClassCompositeDiagram extends UmlBaseClassCompositeDiagram {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlClassCompositeDiagram(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "class composite diagram";
  }

  /**
   * set he html ref
   * set the diagrams list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    diagrams.addElement(this);
    super.memo_ref();
  
  }

  public static void ref_index() throws IOException
  {
    if (!diagrams.isEmpty())
      fw.write("<a href=\"classcompositediagrams.html\" target = \"projectFrame\"><b> -Class Composite Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Class Composite Diagram", "classcompositediagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }
}
