package fr.bouml;


abstract class UmlBaseExtraArtifactDefinition extends UmlItem {
  /**
   *   returns a new extra artifact definition named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlExtraArtifactDefinition create(UmlArtifact parent, String s) throws RuntimeException
  {
    return (UmlExtraArtifactDefinition) parent.create_(anItemKind.anExtraArtifactDefinition, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anExtraArtifactDefinition;
  }

  /**
   *  return the C++ declaration
   */
  public String cppDecl() {
    read_if_needed_();
    return _cpp_decl;
  }

  /**
   *  set the C++ declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppDeclCmd, v);
    UmlCom.check();
  
    _cpp_decl = v;
  }

  /**
   *  return the C++ definition
   */
  public String cppDef() {
    read_if_needed_();
    return _cpp_def;
  }

  /**
   *  set the C++ definition
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppDef(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppDefCmd, v);
    UmlCom.check();
  
    _cpp_def = v;
  }

  /**
   *  return the Java declaration
   */
  public String javaDecl() {
    read_if_needed_();
    return _java_decl;
  }

  /**
   *  set the Java declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaDeclCmd, v);
    UmlCom.check();
  
    _java_decl = v;
  }

  /**
   *  return the Php declaration
   */
  public String phpDecl() {
    read_if_needed_();
    return _php_decl;
  }

  /**
   *  set the Php declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpDeclCmd, v);
    UmlCom.check();
  
    _php_decl = v;
  }

  /**
   *  return the Python declaration
   */
  public String pythonDecl() {
    read_if_needed_();
    return _python_decl;
  }

  /**
   *  set the Python declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpDeclCmd, v);
    UmlCom.check();
  
    _python_decl = v;
  }

  /**
   *  return the Idl declaration
   */
  public String idlDecl() {
    read_if_needed_();
    return _idl_decl;
  }

  /**
   *  set the Idl declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpDeclCmd, v);
    UmlCom.check();
  
    _idl_decl = v;
  }

  /**
   *  return the Mysql declaration
   */
  public String mysqlDecl() {
    read_if_needed_();
    return _mysql_decl;
  }

  /**
   *  set the Mysql declaration
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlDecl(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpDeclCmd, v);
    UmlCom.check();
  
    _mysql_decl = v;
  }

  /**
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _cpp_def = null;
    _cpp_decl = null;
    _java_decl = null;
    _idl_decl = null;
    _php_decl = null;
    _python_decl = null;
    _mysql_decl = null;
    super.unload(rec, del);
  }

  private String _cpp_decl;

  private String _cpp_def;

  private String _java_decl;

  private String _php_decl;

  private String _python_decl;

  private String _idl_decl;

  private String _mysql_decl;

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseExtraArtifactDefinition(long id, String s) {
    super(id, s);
  }

  protected void read_cpp_() {
    _cpp_decl = UmlCom.read_string();
    _cpp_def = UmlCom.read_string();
  }

  protected void read_java_() {
    _java_decl = UmlCom.read_string();
  }

  protected void read_php_() {
    _php_decl = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
    _python_decl = UmlCom.read_string();
  }

  protected void read_idl_() {
    _idl_decl = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_mysql_() {
    _mysql_decl = UmlCom.read_string();
  }

}
