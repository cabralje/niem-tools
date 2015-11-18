
abstract class UmlBaseEntryPointPseudoState extends UmlPseudoState {
  /**
   *   returns a new entry point pseudo state named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlEntryPointPseudoState create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlEntryPointPseudoState) parent.create_(anItemKind.anEntryPointPseudoState, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anEntryPointPseudoState;
  }

  /**
   *  return the the referenced sub machine state or 0/null
   *  if the state is not a sub machine state reference
   */
  public UmlEntryPointPseudoState reference() {
    read_if_needed_();
    return _reference;
  }

  /**
   *  set the referenced sub machine state (may be 0/null)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Reference(UmlEntryPointPseudoState v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDerivedCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _reference = v;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseEntryPointPseudoState(long id, String s) {
    super(id, s);
  }

  private UmlEntryPointPseudoState _reference;

  protected void read_uml_() {
    super.read_uml_();
    _reference = (UmlEntryPointPseudoState) UmlBaseItem.read_();
  }

}
