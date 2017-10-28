package fr.bouml;

class UmlJunctionPseudoState extends UmlBaseJunctionPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "junction pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlJunctionPseudoState(long id) {
    super(id, "");
  }

}
