
abstract class UmlBaseAccessVariableValueAction extends UmlActivityAction {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseAccessVariableValueAction(long id, String s) {
    super(id, s);
  }

  /**
   *  return the variable, may be an attribute or a relation
   */
  public UmlItem variable() {
    read_if_needed_();
    return _variable;
  }

  /**
   *  set the variable
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Variable(UmlItem v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, v.identifier_());
    UmlCom.check();
  
    _variable = v;
  }

  private UmlItem _variable;

  protected void read_uml_() {
    super.read_uml_();
    _variable = UmlBaseItem.read_();
  }

}
