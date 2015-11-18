
abstract class UmlBaseState extends UmlItem {
  /**
   *   returns a new state named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlState create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlState) parent.create_(anItemKind.aState, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aState;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseState(long id, String s) {
    super(id, s);
  }

  /**
   *  return the entry behavior in OCL
   */
  public String entryBehavior() {
    read_if_needed_();
    return _uml.on_entry;
  }

  /**
   *  set the entry behavior in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_EntryBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlEntryBehaviorCmd, s);
    UmlCom.check();
  
    _uml.on_entry = s;
  }

  /**
   *  return the exit behavior in OCL
   */
  public String exitBehavior() {
    read_if_needed_();
    return _uml.on_exit;
  }

  /**
   *  set the exit behavior in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_ExitBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlExitBehaviorCmd, s);
    UmlCom.check();
  
    _uml.on_exit = s;
  }

  /**
   *  return the activity in OCL
   */
  public String doActivity() {
    read_if_needed_();
    return _uml.do_activity;
  }

  /**
   *  set the activity in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_DoActivity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlActivityCmd, s);
    UmlCom.check();
  
    _uml.do_activity = s;
  }

  /**
   *  return the entry behavior in C++
   */
  public String cppEntryBehavior() {
    read_if_needed_();
    return _cpp.on_entry;
  }

  /**
   *  set the entry behavior in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppEntryBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppEntryBehaviorCmd, s);
    UmlCom.check();
  
    _cpp.on_entry = s;
  }

  /**
   *  return the exit behavior in C++
   */
  public String cppExitBehavior() {
    read_if_needed_();
    return _cpp.on_exit;
  }

  /**
   *  set the exit behavior in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppExitBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppExitBehaviorCmd, s);
    UmlCom.check();
  
    _cpp.on_exit = s;
  }

  /**
   *  return the activity in C++
   */
  public String cppDoActivity() {
    read_if_needed_();
    return _cpp.do_activity;
  }

  /**
   *  set the activity in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppDoActivity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppActivityCmd, s);
    UmlCom.check();
  
    _cpp.do_activity = s;
  }

  /**
   *  return the entry behavior in Java
   */
  public String javaEntryBehavior() {
    read_if_needed_();
    return _java.on_entry;
  }

  /**
   *  set the entry behavior in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaEntryBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaEntryBehaviorCmd, s);
    UmlCom.check();
  
    _java.on_entry = s;
  }

  /**
   *  return the exit behavior in Java
   */
  public String javaExitBehavior() {
    read_if_needed_();
    return _java.on_exit;
  }

  /**
   *  set the exit behavior in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaExitBehavior(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaExitBehaviorCmd, s);
    UmlCom.check();
  
    _java.on_exit = s;
  }

  /**
   *  return the activity in Java
   */
  public String javaDoActivity() {
    read_if_needed_();
    return _java.do_activity;
  }

  /**
   *  set the activity in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaDoActivity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaActivityCmd, s);
    UmlCom.check();
  
    _java.do_activity = s;
  }

  /**
   *  return the  indicate if the class is active
   */
  public boolean isActive() {
    read_if_needed_();
    return _active;
  }

  /**
   *  set if the class is active
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isActive(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setActiveCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _active = v;
  }

  /**
   *  return the specification
   */
  public UmlOperation specification() {
    read_if_needed_();
    return _specification;
  }

  /**
   *  set the specification
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Specification(UmlOperation v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _specification = v;
  }

  /**
   *  return the the referenced sub machine state or 0/null
   *  if the state is not a sub machine state reference
   */
  public UmlState reference() {
    read_if_needed_();
    return _reference;
  }

  /**
   *  set the referenced sub machine state (may be 0/null)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Reference(UmlState v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDerivedCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _reference = v;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlStateDiagram associatedDiagram() {
    read_if_needed_();
  
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlStateDiagram d) {
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
    _uml = null;
    _cpp = null;
    _java = null;
    super.unload(rec, del);
  }

  private StateBehavior _uml;

  private StateBehavior _cpp;

  private StateBehavior _java;

  private boolean _active;

  private UmlState _reference;

  private UmlOperation _specification;

  private UmlStateDiagram _assoc_diagram;

  protected void read_uml_() {
    _assoc_diagram = (UmlStateDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _uml = new StateBehavior();
    _uml.read();
    _specification = (UmlOperation) UmlBaseItem.read_();
    _active = UmlCom.read_bool();
    _reference = (UmlState) UmlBaseItem.read_();
  }

  protected void read_cpp_() {
    _cpp = new StateBehavior();
    _cpp.read();
  }

  protected void read_java_() {
    _java = new StateBehavior();
    _java.read();
  }

}
