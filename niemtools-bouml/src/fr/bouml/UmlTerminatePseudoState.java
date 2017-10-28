package fr.bouml;

class UmlTerminatePseudoState extends UmlBaseTerminatePseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "terminate pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlTerminatePseudoState(long id) {
    super(id, "");
  }

}
