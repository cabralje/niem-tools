package fr.bouml;


import java.io.*;
import java.util.*;


/**
 *  This class manages 'object diagrams', notes that the class 'UmlDiagram'
 *  is a mother class of all the diagrams, allowing to generalize their
 *  management
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlObjectDiagram extends UmlBaseObjectDiagram {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlObjectDiagram(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "object diagram";
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
      fw.write("<a href=\"objectdiagrams.html\" target = \"projectFrame\"><b> -Object Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Object Diagram", "objectdiagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }}
