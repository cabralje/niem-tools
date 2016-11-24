
abstract class UmlBaseJunctionPseudoState extends UmlPseudoState {
  /**
   *  returns a new junction pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlJunctionPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlJunctionPseudoState) parent.create_(anItemKind.aJunctionPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aJunctionPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseJunctionPseudoState(long id, String s) {
    super(id, s);
  }

}
