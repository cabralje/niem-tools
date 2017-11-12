package fr.bouml;

abstract class UmlBaseShallowHistoryPseudoState extends UmlPseudoState {
  /**
   *  returns a new shallow history pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlShallowHistoryPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlShallowHistoryPseudoState) parent.create_(anItemKind.aShallowHistoryPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aShallowHistoryPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseShallowHistoryPseudoState(long id, String s) {
    super(id, s);
  }

}
