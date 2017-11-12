package fr.bouml;

import java.io.*;
import java.util.*;

/**
 *  This class manages 'deployment diagrams', notes that the class 'UmlDiagram'
 *  is a mother class of all the diagrams, allowing to generalize their
 *  management
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlDeploymentDiagram extends UmlBaseDeploymentDiagram {
  public UmlDeploymentDiagram(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "deployment diagram";
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
      fw.write("<a href=\"deploymentdiagrams.html\" target = \"projectFrame\"><b> -Deployment Diagrams- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(diagrams, "Deployment Diagram", "deploymentdiagrams");
  }

  protected static Vector diagrams;

  static { diagrams = new Vector(); }};
