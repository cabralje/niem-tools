
abstract class UmlBaseOnSignalAction extends UmlActivityAction {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseOnSignalAction(long id, String s) {
    super(id, s);
  }

  /**
   *  return the signal
   */
  public String signal() {
    read_if_needed_();
    return _signal;
  }

  /**
   *  set the signal
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Signal(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlActivityCmd, v);
    UmlCom.check();
  
    _signal = v;
  }

  /**
   *  return the signal in C++
   */
  public String cppSignal() {
    read_if_needed_();
    return _cpp_signal;
  }

  /**
   *  set the signal in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppSignal(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppActivityCmd, v);
    UmlCom.check();
  
    _cpp_signal = v;
  }

  /**
   *  return the signal in Java
   */
  public String javaSignal() {
    read_if_needed_();
    return _java_signal;
  }

  /**
   *  set the signal in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaSignal(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaActivityCmd, v);
    UmlCom.check();
  
    _java_signal = v;
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
    _signal = null;
    _cpp_signal = null;
    _java_signal = null;
    super.unload(rec, del);
  }

  private String _signal;

  private String _cpp_signal;

  private String _java_signal;

  protected void read_uml_() {
    super.read_uml_();
    _signal = UmlCom.read_string();
  }

  protected void read_cpp_() {
    super.read_cpp_();
    _cpp_signal = UmlCom.read_string();
  }

  protected void read_java_() {
    super.read_java_();
    _java_signal = UmlCom.read_string();
  }

}
