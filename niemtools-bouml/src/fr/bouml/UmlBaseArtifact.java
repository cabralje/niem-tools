package fr.bouml;

/**
 * Manage the artifacts
 */
abstract class UmlBaseArtifact extends UmlItem {
  /**
   *  returns a new artifact named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlArtifact create(UmlDeploymentView parent, String s) throws RuntimeException
  {
    return (UmlArtifact) parent.create_(anItemKind.anArtifact, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anArtifact;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlDeploymentDiagram associatedDiagram() {
    read_if_needed_();
    
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlDeploymentDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    
    _assoc_diagram = d;
  
  }

  /**
   *  returns (in Java a copy of) the optional associated elements
   *  (classes or UmlExtraDefinition)
   *  significant when the artifact is stereotyped <<source>>
   */
  public UmlItem[] associatedElements() {
    read_if_needed_();
  
    return (UmlItem[]) _assoc_elems.clone();
  }

  /**
   *  adds 'cl' at the end of the associated element list,
   *  returns false if 'cl' is already an associate class
   *  significant when the artifact is stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addAssociatedClass(UmlClass cl) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addAssocClassCmd, cl.identifier_());
    UmlCom.check();
    
    if (defined_()) {
      // code compatible with old Java versions
      int n = _assoc_elems.length;
      UmlItem[] a = new UmlItem[n + 1];
      
      a[n] = cl;
      
      while (n-- != 0)
        a[n] = _assoc_elems[n];
      
      _assoc_elems = a;
    }
  }

  /**
   *  removes an associated class
   *  significant when the artifact is not stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void removeAssociatedClass(UmlClass cl) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeAssocClassCmd, cl.identifier_());
    UmlCom.check();
    
    if (defined_()) {
      // code compatible with old Java versions
      int n = _assoc_elems.length;
      UmlItem[] a = new UmlItem[n - 1];
      int i;
      
      for (i = 0; _assoc_elems[i] != cl; i += 1)
        a[i] = _assoc_elems[i];
      
      while (++i != n)
        a[i - 1] = _assoc_elems[i];
      
      _assoc_elems = a;
    }
  }

  /**
   *  set the associated classes list
   *  significant when the artifact is stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedElements(UmlItem[] l) throws RuntimeException {
    for (int i = 0; i != l.length; i += 1) {
      switch (l[i].kind().value()) {
      case anItemKind._aClass:
      case anItemKind._anExtraArtifactDefinition:
        break;
      default:
        throw new RuntimeException("elements must be a class or an extra artifact definition");
      }
    }
  
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocClassesCmd, l);
    UmlCom.check();
    
    _assoc_elems = (UmlItem[]) l.clone();
  }

  /**
   *  returns (in Java a copy of) the associated artifacts list
   *  significant when the artifact is not stereotyped <<source>>
   */
  public UmlArtifact[] associatedArtifacts() {
    read_if_needed_();
    
    return (UmlArtifact[]) _associated.clone();
  }

  /**
   *  adds an associated artifacts, returns false if 'cp' is already
   *  an associate artifact.
   *  significant when the artifact is not stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addAssociatedArtifact(UmlArtifact cp) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addAssocArtifactCmd, cp.identifier_());
    UmlCom.check();
    
    if (defined_()) {
      // code compatible with old Java versions
      int n = _associated.length;
      UmlArtifact[] a = new UmlArtifact[n + 1];
      
      a[n] = cp;
      
      while (n-- != 0)
        a[n] = _associated[n];
      
      _associated = a;
    }
  }

  /**
   *  removes an associated artifacts
   *  significant when the artifact is not stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void removeAssociatedArtifact(UmlArtifact cp) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeAssocArtifactCmd, cp.identifier_());
    UmlCom.check();
    
    if (defined_()) {
      // code compatible with old Java versions
      int n = _associated.length;
      UmlArtifact[] a = new UmlArtifact[n - 1];
      int i;
      
      for (i = 0; _associated[i] != cp; i += 1)
        a[i] = _associated[i];
      
      while (++i != n)
        a[i - 1] = _associated[i];
      
      _associated = a;
    }
  
  }

  /**
   *  removes all associated artifacts
   *  significant when the artifact is not stereotyped <<source>>
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void removeAllAssociatedArtifacts() throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeAllAssocArtifactsCmd);
    UmlCom.check();
    
    _associated = new UmlArtifact[0];
  }

  /**
   *  returns the C++ header file definition
   */
  public String cppHeader() {
    read_if_needed_();
    
    return _cpp_h;
  }

  /**
   *  to set the C++ header file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppHeader(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppHCmd, s);
    UmlCom.check();
  
    _cpp_h = s;
  }

  /**
   *  returns the C++ source file definition
   */
  public String cppSource() {
    read_if_needed_();
    
    return _cpp_src;
  }

  /**
   *  to set the C++ source file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppSrcCmd, s);
    UmlCom.check();
  
    _cpp_src = s;
  }

  /**
   *  returns the Java file definition
   */
  public String javaSource() {
    read_if_needed_();
    
    return _java_src;
  }

  /**
   *  to set the Java file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaSrcCmd, s);
    UmlCom.check();
  
    _java_src = s;
  }

  /**
   *  returns the Php file definition
   */
  public String phpSource() {
    read_if_needed_();
    
    return _php_src;
  }

  /**
   *  to set the Php file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpSrcCmd, s);
    UmlCom.check();
  
    _php_src = s;
  }

  /**
   *  returns the Python file definition
   */
  public String pythonSource() {
    read_if_needed_();
    
    return _python_src;
  }

  /**
   *  to set the Python file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonSrcCmd, s);
    UmlCom.check();
  
    _python_src = s;
  }

  /**
   *  returns the Idl file definition
   */
  public String idlSource() {
    read_if_needed_();
    
    return _idl_src;
  }

  /**
   *  to set the Idl file definition
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlSrcCmd, s);
    UmlCom.check();
  
    _idl_src = s;
  }

  /**
   *  returns the MySql file definition
   */
  public String mysqlSource() {
    read_if_needed_();
  
    return _mysql_src;
  }

  /**
   *  to set the MySql file definition
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlSource(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlSrcCmd, s);
    UmlCom.check();
  
    _mysql_src = s;
  }

  /**
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _assoc_elems = null;
    _associated = null;
    _cpp_h = null;
    _cpp_src = null;
    _java_src = null;
    _php_src = null;
    _python_src = null;
    _mysql_src = null;
    _idl_src = null;
    super.unload(rec, del);
  }

  private UmlDeploymentDiagram _assoc_diagram;
  private UmlItem[] _assoc_elems;
  private UmlArtifact[] _associated;

  private String _cpp_h;
  private String _cpp_src;
  private String _java_src;
  private String _php_src;
  private String _python_src;

  private String _mysql_src;

  private String _idl_src;
  protected void read_uml_() {
    _assoc_diagram = (UmlDeploymentDiagram) UmlBaseItem.read_();
    super.read_uml_();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    _assoc_elems = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
      _assoc_elems[index] = UmlBaseItem.read_();
    
    n = UmlCom.read_unsigned();
    _associated = new UmlArtifact[n];
      
    for (index = 0; index != n; index += 1)
      _associated[index] = (UmlArtifact) UmlBaseItem.read_();
  }

  protected void read_cpp_() {
    _cpp_h = UmlCom.read_string();
    _cpp_src = UmlCom.read_string();
  }

  protected void read_java_() {
    _java_src = UmlCom.read_string();
  }

  protected void read_php_() {
    _php_src = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
    _python_src = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_mysql_() {
    _mysql_src = UmlCom.read_string();
  }

  protected void read_idl_() {
    _idl_src = UmlCom.read_string();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseArtifact(long id, String n) { super(id, n); }
};
