package fr.bouml;

class UmlForkPseudoState extends UmlBaseForkPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "fork pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlForkPseudoState(long id) {
    super(id, "");
  }

}
