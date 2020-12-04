package fr.bouml;


abstract class UmlBaseAccessVariableValueAction extends UmlActivityAction {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseAccessVariableValueAction(long id, String s) {
    super(id, s);
  }

  /**
   *  return the variable
   */
  public UmlVariable variable() {
    read_if_needed_();
    return _variable;
  }

  /**
   *  set the variable
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Variable(UmlVariable v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, v.identifier_());
    UmlCom.check();
  
    _variable = v;
  }

  protected void read_uml_() {
    super.read_uml_();
    _variable = (UmlVariable) UmlBaseItem.read_();
  }

  private UmlVariable _variable;

}
