package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("rawtypes")
class UmlVariable extends UmlBaseVariable {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlVariable(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "variable";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Variable <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
    
    fw.write("<p>");
    write(visibility(), aLanguage.umlLanguage);
    if (isOrdered())
      fw.write(" ordered");
  
    if (isUnique())
      fw.write(" unique");
    fw.write("</p>");
    
    write_description();
    
    fw.write("<p>type :");
    write(type());
    fw.write("</p>");
    
    if (multiplicity().length() != 0) {
      fw.write("<p>Multiplicity : ");
      writeq(multiplicity());
      fw.write("</p>");
    }
  
    write_properties();
  
    unload(false, false);
  }

  /**
   * set the html ref
   * set the variable list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    variables.addElement(this);
    super.memo_ref();
  
  }

  public static void ref_index() throws IOException
  {
    if (!variables.isEmpty())
      fw.write("<a href=\"variables.html\" target = \"projectFrame\"><b> -Variables- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(variables, "Variables", "variables");
  }

  protected static Vector variables;

  static { variables = new Vector(); }
}
