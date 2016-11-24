
import java.io.*;

/**
 *  This class manages 'components'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlComponent extends UmlBaseComponent {
  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
  
    html("Component", associatedDiagram());
  
    if (parent().kind() == anItemKind.aComponent) {
      fw.write("<p>nested in ");
      parent().write();
      fw.write("</p>\n");
    }
  
    UmlClass[] cls;
    
    cls = providedClasses();
  
    if (cls.length != 0) {
      String sep = "<p>provided classes : ";
      
      for (int i = 0; i != cls.length; i += 1) {
        fw.write(sep);
        sep = ", ";
        cls[i].write();
      }
      fw.write("</p>\n");
    }
  
    cls = requiredClasses();
  
    if (cls.length != 0) {
      String sep = "<p>required classes : ";
      
      for (int i = 0; i != cls.length; i += 1) {
        fw.write(sep);
        sep = ", ";
        cls[i].write();
      }
      fw.write("</p>\n");
    }
  
    cls = realizingClasses();
  
    if (cls.length != 0) {
      String sep = "<p>realizing classes : ";
      
      for (int i = 0; i != cls.length; i += 1) {
        fw.write(sep);
        sep = ", ";
        cls[i].write();
      }
      fw.write("</p>\n");
    }
  
    write_children(pfix, rank, level);
      
    unload(false, false);
  }

  public String sKind() {
    return "component";
  }

  public UmlComponent(long id, String n){ super(id, n); }

};
