package fr.bouml;

import java.io.*;
import java.util.*;

/**
 *  This class manages 'use case'
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlUseCase extends UmlBaseUseCase {
  public UmlUseCase(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "use case";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html(pfix, rank, "Use Case", level, "usecase");
  
    String v;
  
    if (!(v = extensionPoints()).equals("")) {
      fw.write("<p>Extension points:<br /><div class=\"sub\">");
      writeq(v);
      fw.write("</div></p>\n");
    }
      
    UmlDiagram d = associatedDiagram();
    
    if (d != null) {
      fw.write("<p>Diagram : ");
      d.write();
      fw.write("</p>\n");
    }
    
    unload(false, false);
  }

  /**
   * set the html ref
   * set the use case list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    usecases.addElement(this);
    super.memo_ref();
  
  }

  public static void ref_index() throws IOException
  {
    if (!usecases.isEmpty())
      fw.write("<a href=\"usecases.html\" target = \"projectFrame\"><b> -Use Cases- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(usecases, "Use Cases", "usecases");
  }

  public boolean chapterp() {
    return true;
  }

  protected static Vector usecases;

  static { usecases = new Vector(); }
};
