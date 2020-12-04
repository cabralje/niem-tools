package fr.bouml;


abstract class UmlBaseInitialPseudoState extends UmlPseudoState {
  /**
   *  returns a new initial pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlInitialPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlInitialPseudoState) parent.create_(anItemKind.anInitialPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anInitialPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseInitialPseudoState(long id, String s) {
    super(id, s);
  }

}
