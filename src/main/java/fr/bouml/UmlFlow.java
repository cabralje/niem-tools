package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
class UmlFlow extends UmlBaseFlow implements UmlActivityItem {
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
    fw.write("<table><tr><td><div class=\"element\">Flow <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
  
    fw.write("<p>From ");
    parent().write();
    fw.write(" To ");
    target().write();
    fw.write("</p>");
  
    write_description();
    
    String scpp = cppWeight();
    String sjava = javaWeight();
    String s = weight();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Weight :</p><ul>");
  
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
    scpp = cppGuard();
    sjava = javaGuard();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Guard :</p><ul>");  
  
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
  
    s = selection();
    scpp = cppSelection();
    sjava = javaSelection();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Selection :</p><ul>");
      
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
  
    s = transformation();
    scpp = cppTransformation();
    sjava = javaTransformation();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Transformation :</p><ul>");
      
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
    
    write_properties();
  
    unload(false, false);
  
  }

  public  UmlFlow(long id, String n) {
    super(id, n);
  }

}
