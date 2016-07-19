
import java.io.*;
import java.util.*;

/**
 *  This class manages 'use case diagrams', notes that the class 'UmlDiagram'
 *  is a mother class of all the diagrams, allowing to generalize their
 *  management
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlUseCaseDiagram extends UmlBaseUseCaseDiagram {
  public UmlUseCaseDiagram(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "use case diagram";
  }

  /**
   * set he html ref
   * set the diagrams list
   */
  public void memo_ref() {
    diagrams.addElement(this);
    super.memo_ref();
  }

  public static void ref_index() throws IOException
  {
    if (!diagrams.isEmpty())
      fw.write("<a href=\"usecasediagrams.html\" target = \"projectFrame\"><b> -Use Case Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Use Case Diagram", "usecasediagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }};
