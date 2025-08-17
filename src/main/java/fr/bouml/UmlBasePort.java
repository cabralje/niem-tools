package fr.bouml;


abstract class UmlBasePort extends UmlItem {
  /**
   *   returns a new port named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlPort create(UmlClass parent, String s) throws RuntimeException
  {
    return (UmlPort) parent.create_(anItemKind.aPort, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aPort;
  }

  /**
   *  return if the port is read only
   */
  public boolean isReadOnly() {
    read_if_needed_();
    return _isReadOnly;
  }

  /**
   * if the port is read only
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isReadOnly(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setReadOnlyCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _isReadOnly = v;
  }

  /**
   *  return if the port is derived
   */
  public boolean isDerived() {
    read_if_needed_();
    return _isDerived;
  }

  /**
   * if the port is derived
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isDerived(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDerivedCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _isDerived = v;
  }

  /**
   *  return if the port is a behavior port
   */
  public boolean isBehavior() {
    read_if_needed_();
    return _isBehavior;
  }

  /**
   * if the port is a behavior port
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isBehavior(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlEntryBehaviorCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _isBehavior = v;
  }

  /**
   *  return if the port is Conjugated
   */
  public boolean isConjugated() {
    read_if_needed_();
    return _isConjugated;
  }

  /**
   * if the port is Conjugated
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isConjugated(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsConjugatedCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _isConjugated = v;
  }

  /**
   *  return if the port is used to provide the published functionality of an EncapsulatedClassifier
   */
  public boolean isService() {
    read_if_needed_();
    return _isService;
  }

  /**
   * if the port is used to provide the published functionality of an EncapsulatedClassifier
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isService(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsServiceCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _isService = v;
  }

  /**
   *  return the type
   */
  public UmlClass type() {
    read_if_needed_();
    return _type;
  }

  /**
   *  set the type
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Type(UmlClass v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _type = v;
  }

  /**
   *  return the multiplicity
   */
  public String multiplicity() {
    read_if_needed_();
    return _multiplicity;
  }

  /**
   *  set the multiplicity
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Multiplicity(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMultiplicityCmd, v);
    UmlCom.check();
  
    _multiplicity = v;
  }

  /**
   *  return the protocol
   */
  public UmlState protocol() {
    read_if_needed_();
    return _protocol;
  }

  /**
   *  set the protocol
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Protocol(UmlState v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setProtocolCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _protocol = v;
  }

  /**
   *  return the default value
   */
  public String defaultValue() {
    read_if_needed_();
    return _default_value;
  }

  /**
   *  set the default value
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_DefaultValue(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefaultValueCmd, v);
    UmlCom.check();
  
    _default_value = v;
  }

  /**
   *  returns (in Java a copy of) the optional provided classes
   */
  public UmlClass[] providedClasses() {
    read_if_needed_();
    
    return (UmlClass[]) _provided.clone();
  }

  /**
   *  returns (in Java a copy of) the optional required classes
   */
  public UmlClass[] requiredClasses() {
    read_if_needed_();
    
    return (UmlClass[]) _required.clone();
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
    _provided = null;
    _required = null;
    super.unload(rec, del);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBasePort(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _isReadOnly = UmlCom.read_bool();
    _isDerived = UmlCom.read_bool();
    _isBehavior = UmlCom.read_bool();
    _isConjugated = UmlCom.read_bool();
    _isService = UmlCom.read_bool();
    _type = (UmlClass) UmlBaseItem.read_();
    _multiplicity = UmlCom.read_string();
    _protocol = (UmlState) UmlBaseItem.read_();
    _default_value = UmlCom.read_string();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    _provided = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
    _provided[index] = (UmlClass) UmlBaseItem.read_();
    
    n = UmlCom.read_unsigned();
    _required = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
      _required[index] = (UmlClass) UmlBaseItem.read_();
  }

  private boolean _isReadOnly;

  private boolean _isDerived;

  private boolean _isBehavior;

  private boolean _isConjugated;

  private boolean _isService;

  private UmlClass _type;

  private String _multiplicity;

  private UmlState _protocol;

  private String _default_value;

  private UmlClass[] _provided;

  private UmlClass[] _required;

}
