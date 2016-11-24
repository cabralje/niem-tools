
abstract class UmlBaseTransition extends UmlItem {
  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aTransition;
  }

  /**
   *  Returns a new Transition from 'start' to 'end'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...) return 0
   *   in C++ and produce a RuntimeException in Java
   */
  public static UmlTransition create(UmlItem start, UmlItem end) throws RuntimeException
  {
    UmlCom.send_cmd(start.identifier_(), OnInstanceCmd.createCmd, anItemKind.aTransition,
  		   end.identifier_());
    UmlTransition result = (UmlTransition) UmlBaseItem.read_();
    
    if (result != null)
      start.reread_children_if_needed_();
    else
      throw new RuntimeException("Cannot create the Transition");
    return result;
  }

  /**
   *  returns the 'end' object (the 'start' object is the parent of the transition) no set !
   */
  public UmlItem target() {
    read_if_needed_();
    return _target;
  }

  /**
   *  return if the transition is internal or external,
   *  only self transitions can't be external
   */
  public boolean isExternal() {
    read_if_needed_();
    return _is_external;
  }

  /**
   *  set if the transition is internal or external,
   *  only a self transition may be set internal
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsExternal(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppExternalCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_external = v;
  }

  /**
   *  return the trigger in OCL
   */
  public String trigger() {
    read_if_needed_();
    return _uml.trigger;
  }

  /**
   *  set the trigger in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Trigger(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlTriggerCmd, s);
    UmlCom.check();
  
    _uml.trigger = s;
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
  public void set_Guard(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlGuardCmd, s);
    UmlCom.check();
  
    _uml.guard = s;
  }

  /**
   *  return the activity in OCL
   */
  public String activity() {
    read_if_needed_();
    return _uml.activity;
  }

  /**
   *  set the activity in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Activity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlActivityCmd, s);
    UmlCom.check();
  
    _uml.activity = s;
  }

  /**
   *  return the trigger in C++
   */
  public String cppTrigger() {
    read_if_needed_();
    return _cpp.trigger;
  }

  /**
   *  set the trigger in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppTrigger(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppTriggerCmd, s);
    UmlCom.check();
  
    _cpp.trigger = s;
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
  public void set_CppGuard(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppGuardCmd, s);
    UmlCom.check();
  
    _cpp.guard = s;
  }

  /**
   *  return the activity in C++
   */
  public String cppActivity() {
    read_if_needed_();
    return _cpp.activity;
  }

  /**
   *  set the activity in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppActivity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppActivityCmd, s);
    UmlCom.check();
  
    _cpp.activity = s;
  }

  /**
   *  return the trigger in Java
   */
  public String javaTrigger() {
    read_if_needed_();
    return _java.trigger;
  }

  /**
   *  set the trigger in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaTrigger(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaTriggerCmd, s);
    UmlCom.check();
  
    _java.trigger = s;
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
  public void set_JavaGuard(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaGuardCmd, s);
    UmlCom.check();
  
    _java.guard = s;
  }

  /**
   *  return the activity in Java
   */
  public String javaActivity() {
    read_if_needed_();
    return _java.activity;
  }

  /**
   *  set the activity in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaActivity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaActivityCmd, s);
    UmlCom.check();
  
    _java.activity = s;
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

  private UmlItem _target;

  public boolean _is_external;

  private TransitionBehavior _uml;

  private TransitionBehavior _cpp;

  private TransitionBehavior _java;

  protected void read_uml_() {
    super.read_uml_();
    _target = UmlBaseItem.read_();
    _uml = new TransitionBehavior();
    _uml.read();
    _is_external = !UmlCom.read_bool();
  }

  protected void read_cpp_() {
    _cpp = new TransitionBehavior();
    _cpp.read();
  }

  protected void read_java_() {
    _java = new TransitionBehavior();
    _java.read();
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseTransition(long id, String n) {
    super(id, n);
  }

}
