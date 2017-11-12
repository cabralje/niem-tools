package fr.bouml;

abstract class UmlBaseStateAction extends UmlItem {
  /**
   *  returns a new state action created under 'parent'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlStateAction create(UmlItem parent) throws RuntimeException
  {
    return (UmlStateAction) parent.create_(anItemKind.aStateAction, "");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aStateAction;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseStateAction(long id, String s) {
    super(id, s);
  }

  /**
   *  return the expression in OCL
   */
  public String expression() {
    read_if_needed_();
    return _uml;
  }

  /**
   *  set the expression in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Expression(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, s);
    UmlCom.check();
  
    _uml = s;
  }

  /**
   *  return the expression in C++
   */
  public String cppExpression() {
    read_if_needed_();
    return _cpp;
  }

  /**
   *  set the expression in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppExpression(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppDeclCmd, s);
    UmlCom.check();
  
    _cpp = s;
  }

  /**
   *  return the expression in Java
   */
  public String javaExpression() {
    read_if_needed_();
    return _java;
  }

  /**
   *  set the expression in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaExpression(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaDeclCmd, s);
    UmlCom.check();
  
    _java = s;
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
    _uml = null;
    _cpp = null;
    _java = null;
    super.unload(rec, del);
  }

  private String _uml;

  private String _cpp;

  private String _java;

  protected void read_uml_() {
    super.read_uml_();
    _uml = UmlCom.read_string();
  }

  protected void read_cpp_() {
    _cpp  = UmlCom.read_string();
  }

  protected void read_java_() {
    _java = UmlCom.read_string();
  }

}
