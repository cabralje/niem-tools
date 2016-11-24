
abstract class UmlBaseExpansionRegion extends UmlActivityRegion {
  /**
   *   returns a new expansion region named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlExpansionRegion create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlExpansionRegion) parent.create_(anItemKind.anExpansionRegion, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anExpansionRegion;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseExpansionRegion(long id, String s) {
    super(id, s);
  }

  /**
   *  return the isMustIsolate attribute, if TRUE the actions in the region execute in isolation from actions outside the region.
   */
  public boolean isMustIsolate() {
    read_if_needed_();
    return _must_isolate;
  }

  /**
   *  set the isMustIsolate attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMustIsolate(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setFlagCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _must_isolate = v;
  }

  /**
   *  return the mode attribute, the way in which the execution interact.
   */
  public anExpansionKind mode() {
    read_if_needed_();
    return _mode;
  }

  /**
   *  set the mode attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Mode(anExpansionKind v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, (byte) v.value());
    UmlCom.check();
  
    _mode = v;
  }

  private boolean _must_isolate;

  private anExpansionKind _mode;

  protected void read_uml_() {
    super.read_uml_();
    _must_isolate = UmlCom.read_bool();
    _mode = anExpansionKind.fromInt(UmlCom.read_char());
  }

}
