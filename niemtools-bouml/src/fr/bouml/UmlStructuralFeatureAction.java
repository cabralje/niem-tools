package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
abstract class UmlStructuralFeatureAction extends UmlBaseStructuralFeatureAction {
  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html() throws IOException {
    super.html();
  
    if (structuralFeature() != null){
      fw.write("<p>structural feature : ");
      structuralFeature().write();
      fw.write("</p>");
    }
  
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlStructuralFeatureAction(long id, String s) {
    super(id, s);
  }

}
