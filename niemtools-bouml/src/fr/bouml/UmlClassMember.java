package fr.bouml;

import java.io.*;
/**
 *  This class globaly manages class's relations, attributes, operations 
 *  and sub-classes
 * 
 *  You can modify it as you want (except the constructor)
 */
abstract class UmlClassMember extends UmlBaseClassMember {
  public UmlClassMember(long id, String n){ super(id, n); }

  public void annotation_constraint() throws IOException {
  String s = javaAnnotations();
    
  if (s.length() != 0) {
    fw.write("<p> Java annotation(s) :</p><ul>");
    writeq(s);
    fw.write("</ul>");
  }
  
  s = constraint();
    
  if (s.length() != 0) {
    fw.write("<p> Constraint :</p><ul>");
    writeq(s);
    fw.write("</ul>");
  }
  }

}
