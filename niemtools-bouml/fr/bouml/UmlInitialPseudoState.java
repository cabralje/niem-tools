package fr.bouml;

class UmlInitialPseudoState extends UmlBaseInitialPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "initial pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlInitialPseudoState(long id) {
    super(id, "");
  }

}
