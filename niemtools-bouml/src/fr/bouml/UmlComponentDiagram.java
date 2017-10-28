package fr.bouml;

import java.io.*;
import java.util.*;

/**
 *  This class manages 'ccomponent diagrams', notes that the class 'UmlDiagram'
 *  is a mother class of all the diagrams, allowing to generalize their
 *  management
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlComponentDiagram extends UmlBaseComponentDiagram {
  public UmlComponentDiagram(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "component diagram";
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
      fw.write("<a href=\"componentdiagrams.html\" target = \"projectFrame\"><b> -Component Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Component Diagram", "componentdiagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }};
