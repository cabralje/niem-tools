package fr.bouml;


abstract class UmlBaseFlow extends UmlItem {
  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aFlow;
  }

  /**
   *  Returns a new flow from 'start' to 'end'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...) return 0
   *   in C++ and produce a RuntimeException in Java
   */
  public static UmlFlow create(UmlActivityNode start, UmlActivityNode end) throws RuntimeException
  {
    UmlCom.send_cmd(start.identifier_(), OnInstanceCmd.createCmd, anItemKind.aFlow,
  		   end.identifier_());
    UmlFlow result = (UmlFlow) UmlBaseItem.read_();
    
    if (result != null)
      start.reread_children_if_needed_();
    else
      throw new RuntimeException("Cannot create the flow");
    return result;
  }

  /**
   *  returns the 'end' object (the 'start' object is the parent of the flow) no set !
   */
  public UmlActivityNode target() {
    read_if_needed_();
    return _target;
  }

  /**
   *  return the weight in OCL
   */
  public String weight() {
    read_if_needed_();
    return _uml.weight;
  }

  /**
   *  set the weight in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Weight(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlActivityCmd, v);
    UmlCom.check();
  
    _uml.weight = v;
  }

  /**
   *  return the guard in OCL
   */
  public String guard() {
    read_if_needed_();
    return _uml.guard;
  }

  /**
   *  set the guard in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Guard(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlGuardCmd, v);
    UmlCom.check();
  
    _uml.guard = v;
  }

  /**
   *  return the selection in OCL
   */
  public String selection() {
    read_if_needed_();
    return _uml.selection;
  }

  /**
   *  set the selection in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Selection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlEntryBehaviorCmd, v);
    UmlCom.check();
  
    _uml.selection = v;
  }

  /**
   *  return the transformation in OCL
   */
  public String transformation() {
    read_if_needed_();
    return _uml.transformation;
  }

  /**
   *  set the transformation in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Transformation(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlExitBehaviorCmd, v);
    UmlCom.check();
  
    _uml.transformation = v;
  }

  /**
   *  return the weight in C++
   */
  public String cppWeight() {
    read_if_needed_();
    return _cpp.weight;
  }

  /**
   *  set the weight in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppWeight(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppActivityCmd, v);
    UmlCom.check();
  
    _cpp.weight = v;
  }

  /**
   *  return the guard in C++
   */
  public String cppGuard() {
    read_if_needed_();
    return _cpp.guard;
  }

  /**
   *  set the guard in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppGuard(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppGuardCmd, v);
    UmlCom.check();
  
    _cpp.guard = v;
  }

  /**
   *  return the selection in C++
   */
  public String cppSelection() {
    read_if_needed_();
    return _cpp.selection;
  }

  /**
   *  set the selection in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppSelection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppEntryBehaviorCmd, v);
    UmlCom.check();
  
    _cpp.selection = v;
  }

  /**
   *  return the transformation in C++
   */
  public String cppTransformation() {
    read_if_needed_();
    return _cpp.transformation;
  }

  /**
   *  set the transformation in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppTransformation(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppExitBehaviorCmd, v);
    UmlCom.check();
  
    _cpp.transformation = v;
  }

  /**
   *  return the weight in Java
   */
  public String javaWeight() {
    read_if_needed_();
    return _java.weight;
  }

  /**
   *  set the weight in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaWeight(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaActivityCmd, v);
    UmlCom.check();
  
    _java.weight = v;
  }

  /**
   *  return the guard in Java
   */
  public String javaGuard() {
    read_if_needed_();
    return _java.guard;
  }

  /**
   *  set the guard in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaGuard(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaGuardCmd, v);
    UmlCom.check();
  
    _java.guard = v;
  }

  /**
   *  return the selection in Java
   */
  public String javaSelection() {
    read_if_needed_();
    return _java.selection;
  }

  /**
   *  set the selection in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaSelection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaEntryBehaviorCmd, v);
    UmlCom.check();
  
    _java.selection = v;
  }

  /**
   *  return the transformation in Java
   */
  public String javaTransformation() {
    read_if_needed_();
    return _java.transformation;
  }

  /**
   *  set the transformation in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaTransformation(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaExitBehaviorCmd, v);
    UmlCom.check();
  
    _java.transformation = v;
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

  private UmlActivityNode _target;

  private FlowBehavior _uml;

  private FlowBehavior _cpp;

  private FlowBehavior _java;

  protected void read_uml_() {
    super.read_uml_();
    _target = (UmlActivityNode) UmlBaseItem.read_();
    _uml = new FlowBehavior();
    _uml.read();
  }

  protected void read_cpp_() {
    _cpp = new FlowBehavior();
    _cpp.read();
  }

  protected void read_java_() {
    _java = new FlowBehavior();
    _java.read();
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseFlow(long id, String s) {
    super(id, s);
  }

}
