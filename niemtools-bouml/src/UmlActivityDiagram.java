
import java.io.*;
import java.util.*;

/**
 *  This class manages 'activity diagrams', notes that the class 'UmlDiagram'
 *  is a mother class of all the diagrams, allowing to generalize their
 *  management
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlActivityDiagram extends UmlBaseActivityDiagram implements UmlActivityItem {
  /**
   * set he html ref
   * set the diagrams list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    diagrams.addElement(this);
    super.memo_ref();
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity diagram";
  }

  public static void ref_index() throws IOException
  {
    if (!diagrams.isEmpty())
      fw.write("<a href=\"activitydiagrams.html\" target = \"projectFrame\"><b> -Activity Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
   generate_index(diagrams, "Activity Diagram", "activitydiagrams");
  }

protected static Vector diagrams;

  static { diagrams = new Vector(); }  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityDiagram(long id, String s) {
    super(id, s);
  }

}
