package fr.bouml;

abstract class UmlBaseTerminatePseudoState extends UmlPseudoState {
  /**
   *  returns a new terminate pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlTerminatePseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlTerminatePseudoState) parent.create_(anItemKind.aTerminatePseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aTerminatePseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseTerminatePseudoState(long id, String s) {
    super(id, s);
  }

}
