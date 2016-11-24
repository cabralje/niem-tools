
import java.io.*;
// import java.util.*;

abstract class UmlPinParameter extends UmlBasePinParameter {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlPinParameter(long id, String s) {
    super(id, s);
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    if (isUnique()) {
      if (isException())
        fw.write("<p>Unique, exception</p>");
      else
        fw.write("<p>Read only</p>");
    }
    else if (isException())
      fw.write("<p>Exception</p>");
  
    fw.write("<p>Direction : ");
    write(direction());
    fw.write("</p>");
  
    if (effect() != aParameterEffectKind.noEffect) {
      fw.write("<p>Effect : ");
      write(effect());
      fw.write("</p>");
    }
  
    html_internal(pfix, rank, level);
  }

}
