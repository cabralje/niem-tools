package fr.bouml;

import java.io.*;

class UmlTransition extends UmlBaseTransition implements UmlStateItem {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "transition";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    fw.write("<table><tr><td><div class=\"element\">Transition <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
    
    if (parent() == target()) {
      fw.write("<p>Self relation of ");
      parent().write();
      fw.write((isExternal()) ? ", external</p>" : ", internal</p>");
    }
    else {
      fw.write("<p>From ");
      parent().write();
      fw.write(" To ");
      target().write();
      fw.write("</p>");
    }
  
    write_description();
  
    String scpp = cppTrigger();
    String sjava = javaTrigger();
    String s = trigger();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Trigger :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    s = cppGuard();
    scpp = guard();
    sjava = javaGuard();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Guard :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    s = cppActivity();
    scpp = activity();
    sjava = javaActivity();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Activity :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
   
    write_properties();
  
    unload(false, false);
  
  }

  public  UmlTransition(long id, String n) {
    super(id, n);
  }

}
