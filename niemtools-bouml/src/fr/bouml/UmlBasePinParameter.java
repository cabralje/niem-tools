package fr.bouml;


abstract class UmlBasePinParameter extends UmlActivityObject {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBasePinParameter(long id, String s) {
    super(id, s);
  }

  /**
   *  return the isUnique attribute
   */
  public boolean isUnique() {
    read_if_needed_();
    return _unique;
  }

  /**
   *  set the isUnique attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsUnique(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUniqueCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _unique = v;
  }

  /**
   *  return the isException attribute, exclusive with isStream
   */
  public boolean isException() {
    read_if_needed_();
    return _exception;
  }

  /**
   *  set the isException attribute, exclusive with isStream
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsException(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceExceptionCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _exception = v;
  }

  /**
   *  return the isStream attribute
   */
  public boolean isStream() {
    read_if_needed_();
    return _stream;
  }

  /**
   *  set the isStream attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsStream(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setStreamCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _stream = v;
  }

  /**
   *  return the direction
   */
  public aDirection direction() {
    read_if_needed_();
    return _dir;
  }

  /**
   *  set the direction
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Direction(aDirection v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlDirCmd, (byte) v.value());
    UmlCom.check();
  
    _dir = v;
  }

  /**
   *  return the effect
   */
  public aParameterEffectKind effect() {
    read_if_needed_();
    return _effect;
  }

  /**
   *  set the effect
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Effect(aParameterEffectKind v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceParameterCmd, (byte) v.value());
    UmlCom.check();
  
    _effect = v;
  }

  private boolean _unique;

  private boolean _exception;

  private boolean _stream;

  private aDirection _dir;

  private aParameterEffectKind _effect;

  protected void read_uml_() {
    super.read_uml_();
    _unique = UmlCom.read_bool();
    _exception = UmlCom.read_bool();
    _stream = UmlCom.read_bool();
    _dir = aDirection.fromInt(UmlCom.read_char());
    _effect = aParameterEffectKind.fromInt(UmlCom.read_char());
  }

}
