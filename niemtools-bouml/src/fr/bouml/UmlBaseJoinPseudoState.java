package fr.bouml;


abstract class UmlBaseJoinPseudoState extends UmlPseudoState {
  /**
   *  returns a new join pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlJoinPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlJoinPseudoState) parent.create_(anItemKind.aJoinPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aJoinPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseJoinPseudoState(long id, String s) {
    super(id, s);
  }

}
