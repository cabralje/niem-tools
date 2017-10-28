package fr.bouml;

abstract class UmlBaseActivityAction extends UmlActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityAction(long id, String s) {
    super(id, s);
  }

  /**
   *  return the pre condition
   */
  public String preCondition() {
    read_if_needed_();
    return _pre_condition;
  }

  /**
   *  set the pre condition
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PreCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlEntryBehaviorCmd, v);
    UmlCom.check();
  
    _pre_condition = v;
  }

  /**
   *  return the post condition
   */
  public String postCondition() {
    read_if_needed_();
    return _post_condition;
  }

  /**
   *  set the post condition
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PostCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlExitBehaviorCmd, v);
    UmlCom.check();
  
    _post_condition = v;
  }

  /**
   *  return the pre condition in C++
   */
  public String cppPreCondition() {
    read_if_needed_();
    return _cpp_pre_condition;
  }

  /**
   *  set the pre condition in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppPreCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppEntryBehaviorCmd, v);
    UmlCom.check();
  
    _cpp_pre_condition = v;
  }

  /**
   *  return the post condition in C++
   */
  public String cppPostCondition() {
    read_if_needed_();
    return _cpp_post_condition;
  }

  /**
   *  set the post condition in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppPostCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppExitBehaviorCmd, v);
    UmlCom.check();
  
    _cpp_post_condition = v;
  }

  /**
   *  return the pre condition in Java
   */
  public String javaPreCondition() {
    read_if_needed_();
    return _java_pre_condition;
  }

  /**
   *  set the pre condition in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaPreCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaEntryBehaviorCmd, v);
    UmlCom.check();
  
    _java_pre_condition = v;
  }

  /**
   *  return the post condition in Java
   */
  public String javaPostCondition() {
    read_if_needed_();
    return _java_post_condition;
  }

  /**
   *  set the post condition in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaPostCondition(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaExitBehaviorCmd, v);
    UmlCom.check();
  
    _java_post_condition = v;
  }

  /**
   *  return the constraint
   */
  public String constraint() {
    read_if_needed_();
    return _constraint;
  }

  /**
   *  set the constraint
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Constraint(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setConstraintCmd, v);
    UmlCom.check();
  
    _constraint = v;
  }

  /**
   *  return the  if the action is locally reentrant
   */
  public boolean isLocallyReentrant() {
    read_if_needed_();
    return _is_locally_reentrant;
  }

  /**
   *  set if the action is locally reentrant
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsLocallyReentrant(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsLocallyReentrantCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_locally_reentrant = v;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlDiagram associatedDiagram() {
    read_if_needed_();
  
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    _assoc_diagram = d;
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
    _pre_condition = null;
    _post_condition = null;
    _cpp_pre_condition = null;
    _cpp_post_condition = null;
    _java_pre_condition = null;
    _java_post_condition = null;
    super.unload(rec, del);
    _constraint = null;
  }

  private String _pre_condition;

  private String _post_condition;

  private String _cpp_pre_condition;

  private String _cpp_post_condition;

  private String _java_pre_condition;

  private String _java_post_condition;

  private String _constraint;

  private boolean _is_locally_reentrant;

  private UmlDiagram _assoc_diagram;

  protected void read_uml_() {
    _assoc_diagram = (UmlDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _pre_condition = UmlCom.read_string();
    _post_condition = UmlCom.read_string();
    _constraint = UmlCom.read_string();
    _is_locally_reentrant = UmlCom.read_bool();
  }

  protected void read_cpp_() {
    _cpp_pre_condition = UmlCom.read_string();
    _cpp_post_condition = UmlCom.read_string();
  }

  protected void read_java_() {
    _java_pre_condition = UmlCom.read_string();
    _java_post_condition = UmlCom.read_string();
  }

}
