package fr.bouml;


abstract class UmlBaseActivityParameter extends UmlPinParameter {
  /**
   *   returns a new activity parameter named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityParameter create(UmlActivity parent, String s) throws RuntimeException
  {
    return (UmlActivityParameter) parent.create_(anItemKind.anActivityParameter, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anActivityParameter;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityParameter(long id, String s) {
    super(id, s);
  }

  /**
   *  return the default value
   */
  public String defaultValue() {
    read_if_needed_();
    return _default_value;
  }

  /**
   *  set the default value
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_DefaultValue(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefaultValueCmd, v);
    UmlCom.check();
  
    _default_value = v;
  }

  /**
   *  to unload the object to free memory, it will be reloaded automatically
   *  if needed. Recursively done for the sub items if 'rec' is TRUE. 
   * 
   *  if 'del' is true the sub items are deleted in C++, and removed from the
   *  internal dictionnary in C++ and Java (to allow it to be garbaged),
   *  you will have to call Children() to re-access to them
   */
  public void unload(boolean rec, boolean del) {
    _default_value = null;
    super.unload(rec, del);
  }

  private String _default_value;

  protected void read_uml_() {
    super.read_uml_();
    _default_value = UmlCom.read_string();
  }

}
