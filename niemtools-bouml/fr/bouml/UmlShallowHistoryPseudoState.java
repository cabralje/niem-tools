package fr.bouml;

class UmlShallowHistoryPseudoState extends UmlBaseShallowHistoryPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "shallow history pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlShallowHistoryPseudoState(long id) {
    super(id, "");
  }

}
