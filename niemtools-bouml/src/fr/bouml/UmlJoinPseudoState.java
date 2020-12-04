package fr.bouml;


class UmlJoinPseudoState extends UmlBaseJoinPseudoState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "join pseudo state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlJoinPseudoState(long id) {
    super(id, "");
  }

}
