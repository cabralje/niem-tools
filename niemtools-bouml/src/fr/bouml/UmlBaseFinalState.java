package fr.bouml;

abstract class UmlBaseFinalState extends UmlPseudoState {
  /**
   *  returns a new final state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlFinalState create(UmlItem parent) throws RuntimeException
  {
    return (UmlFinalState) parent.create_(anItemKind.aFinalState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aFinalState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseFinalState(long id, String s) {
    super(id, s);
  }

}
