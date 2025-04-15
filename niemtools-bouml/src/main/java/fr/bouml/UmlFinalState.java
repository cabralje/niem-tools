package fr.bouml;


class UmlFinalState extends UmlBaseFinalState {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "final state";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlFinalState(long id) {
    super(id, "");
  }

}
