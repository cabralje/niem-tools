package fr.bouml;


import java.io.*;
import java.util.*;


@SuppressWarnings("rawtypes")
class UmlObjectCompositeDiagram extends UmlBaseObjectCompositeDiagram {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlObjectCompositeDiagram(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "object composite diagram";
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
      fw.write("<a href=\"objectcompositediagrams.html\" target = \"projectFrame\"><b> -Object Composite Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Object Composite Diagram", "objectcompositediagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }
}
