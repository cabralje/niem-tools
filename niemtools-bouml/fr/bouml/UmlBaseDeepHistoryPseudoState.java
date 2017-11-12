package fr.bouml;

abstract class UmlBaseDeepHistoryPseudoState extends UmlPseudoState {
  /**
   *  returns a new deep history pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlDeepHistoryPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlDeepHistoryPseudoState) parent.create_(anItemKind.aDeepHistoryPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aDeepHistoryPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseDeepHistoryPseudoState(long id, String s) {
    super(id, s);
  }

}
