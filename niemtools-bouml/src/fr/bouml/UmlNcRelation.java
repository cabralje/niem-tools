package fr.bouml;

import java.io.*;

/**
 * This class manages 'relations' between non class objects
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlNcRelation extends UmlBaseNcRelation {
  public UmlNcRelation(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "non class relation";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
  }

  public void memo_ref() {
  }

};
