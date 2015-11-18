
abstract class UmlBaseForkPseudoState extends UmlPseudoState {
  /**
   *  returns a new fork pseudo state created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlForkPseudoState create(UmlItem parent) throws RuntimeException
  {
    return (UmlForkPseudoState) parent.create_(anItemKind.aForkPseudoState, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aForkPseudoState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseForkPseudoState(long id, String s) {
    super(id, s);
  }

}
