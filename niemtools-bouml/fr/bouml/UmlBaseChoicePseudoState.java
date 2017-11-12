package fr.bouml;

abstract class UmlBaseChoicePseudoState extends UmlPseudoState {
  /**
   *  returns a new choice pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlChoicePseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlChoicePseudoState) parent.create_(anItemKind.aChoicePseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aChoicePseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseChoicePseudoState(long id, String s) {
    super(id, s);
  }

}
