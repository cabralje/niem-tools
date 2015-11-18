
import java.util.*;
/**
 *   Manage the classes
 */
abstract class UmlBaseClass extends UmlClassMember {
  /**
   *  returns a new class named 'name' created under 'parent' 
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlClass create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlClass) parent.create_(anItemKind.aClass, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aClass;
  }

  /**
   *  returns TRUE in case the class is abstract
   */
  public boolean isAbstract() {
    read_if_needed_();
    
    return _abstract;
  }

  /**
   *  the set if the class is abstract
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  for instance you cannot set it to FALSE in case the class have
   *  abstract operations
   */
  public void set_isAbstract(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsAbstractCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _abstract = y;
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
   *  returns the type on which the class (in fact a typedef) is based
   */
  public UmlTypeSpec baseType() {
    read_if_needed_();
    
    return _base_type;
  }

  /**
   *  to set the type on which the class (in fact a typedef) is based
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java, does not check that the class is (already) a typedef
   */
  public void set_BaseType(UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setBaseTypeCmd, t);
    UmlCom.check();
  
    _base_type = t;
  }

  /**
   *  returns (a copy of) the formals list
   */
  public UmlFormalParameter[] formals() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.formalsCmd);
    
    int n = UmlCom.read_unsigned();
    UmlFormalParameter[] formals = new UmlFormalParameter[n];
    
    for (int i = 0; i != n; i += 1) {
      UmlFormalParameter f = new UmlFormalParameter();
      
      f.read_();
      formals[i] = f;
    }
    
    return formals;
  }

  /**
   *  remove the formal of the given rank (0...), returns 0 on error
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void removeFormal(int rank) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeFormalCmd, rank);
    UmlCom.check();
  }

  /**
   *  adds a formal at the given rank (0...), returns 0 on error
   * 
   * On error return FALSE in C++, produce a RuntimeException in Java,
   * does not check that the class is (already) a typedef
   */
  public void addFormal(int rank, UmlFormalParameter formal) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addFormalCmd, rank, formal._name, 
  		   formal._type, formal._default_value, formal._extends);
    UmlCom.check();
  }

  /**
   *  replace the formal at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void replaceFormal(int rank, UmlFormalParameter formal) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceFormalCmd, rank, formal._name, 
  		   formal._type, formal._default_value, formal._extends);
    UmlCom.check();
  }

  /**
   *  returns (a copy of) the actuals list
   */
  public UmlActualParameter[] actuals() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.actualsCmd);
    
    int n = UmlCom.read_unsigned();
    UmlActualParameter[] actuals = new UmlActualParameter[n];
    
    for (int i = 0; i != n; i += 1) {
      UmlActualParameter a = new UmlActualParameter();
      
      a.read_();
      actuals[i] = a;
    }
    
    return actuals;
  }

  /**
   *  replace the actual value at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void replaceActual(int rank, UmlTypeSpec type) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceActualCmd, rank, type);
    UmlCom.check();
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlClassDiagram associatedDiagram() {
    read_if_needed_();
    
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlClassDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    _assoc_diagram = d;
  }

  /**
   *  returns the optional associated artifact,
   *  to set it refer to the UmlBaseArtifact's operations
   *  addAssociatedClass(), removeAssociatedClass() and
   *  setAssociatedClasses()
   */
  public UmlArtifact associatedArtifact() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.assocArtifactCmd);
    
    return (UmlArtifact) UmlBaseItem.read_();
  }

  /**
   *  returns the components realizing or providing the class.
   *  To set them refer to the UmlBaseComponent's operation setAssociatedClasses()
   */
  public UmlComponent[] associatedComponents() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.assocComponentCmd);
  
    int n = UmlCom.read_unsigned();
    UmlComponent[] result = new UmlComponent[n];
  
    for (int index = 0; index != n; index += 1)
      result[index] = (UmlComponent) UmlBaseItem.read_();
  
    return result;
  }

  /**
   *  returns TRUE if the class is external, its definition
   *  must contains how the name is made on the first line
   *  (isCppExternal by default), the other lines if they exist
   *  must contains the #include and may be using forms
   */
  public boolean isCppExternal() {
    read_if_needed_();
    
    return _cpp_external;
  }

  /**
   *  set if the class is external
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppExternal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppExternalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_external = y;
  }

  /**
   *  returns TRUE if the class is external, its definition
   *  must contain how the name is made on the first line
   *  (isJavaExternal by default), the other lines are ignored
   */
  public boolean isJavaExternal() {
    read_if_needed_();
    
    return _java_external;
  }

  /**
   *  set if the class is external
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaExternal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsJavaExternalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_external = y;
  }

  /**
   *  returns TRUE is the class is public
   */
  public boolean isJavaPublic() {
    return visibility() == aVisibility.PublicVisibility;
  }

  /**
   *  set if the class is public
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaPublic(boolean y) throws RuntimeException {
    set_Visibility((y) ? aVisibility.PublicVisibility : aVisibility.PackageVisibility);
  }

  /**
   *  returns TRUE is the class is final   
   */
  public boolean isJavaFinal() {
    read_if_needed_();
    
    return _java_final;
  }

  /**
   *  set if the class is final
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaFinal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsJavaFinalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_final = y;
  }

  /**
   *  returns TRUE if the class is external, its definition
   *  must contain how the name is made on the first line
   *  (isPhpExternal by default), the other lines are ignored
   */
  public boolean isPhpExternal() {
    read_if_needed_();
    
    return _php_external;
  }

  /**
   *  set if the class is external
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPhpExternal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsPhpExternalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _php_external = y;
  }

  /**
   *  returns TRUE is the class is final   
   */
  public boolean isPhpFinal() {
    read_if_needed_();
    
    return _php_final;
  }

  /**
   *  set if the class is final
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPhpFinal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsPhpFinalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _php_final = y;
  }

  /**
   *  returns TRUE if the class is external, its definition
   *  must contain how the name is made on the first line
   *  (isPythonExternal by default), the other lines are ignored
   */
  public boolean isPythonExternal() {
    read_if_needed_();
    
    return _python_external;
  }

  /**
   *  set if the class is external
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPythonExternal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsPythonExternalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _python_external = y;
  }

  /**
   *  returns TRUE is the class is a Python 2.2 class
   */
  public boolean isPython_2_2() {
    read_if_needed_();
    return _python_2_2;
  }

  /**
   *  set if the class is a Python 2.2 class
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPython_2_2(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsPython2_2Cmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _python_2_2 = v;
  }

  /**
   *  returns the switch's type, significant in case the class
   *  is an union in IDL
   */
  public UmlTypeSpec switchType() {
    read_if_needed_();
    
    return _switch_type.clone_it();
  }

  /**
   *  to set the switch type even the class is not (already) an IDL union
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_SwitchType(UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setSwitchTypeCmd, t);
    UmlCom.check();
  
    _switch_type = t.clone_it();
  }

  /**
   *  returns TRUE if the class is external, its definition
   *  must contain how the name is made on the first line
   *  (isIdlExternal by default), the other lines are ignored
   */
  public boolean isIdlExternal() {
    read_if_needed_();
    
    return _idl_external;
  }

  /**
   *  set if the class is external
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isIdlExternal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsIdlExternalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _idl_external = y;
  }

  /**
   *  returns TRUE if the class is local, have sense for an interface
   */
  public boolean isIdlLocal() {
    read_if_needed_();
    
    return _idl_local;
  }

  /**
   *  set if the class is local, have sense for an interface
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isIdlLocal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsIdlLocalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _idl_local = y;
  }

  /**
   *  returns TRUE is the class is custom, have sense for a valuetype
   */
  public boolean isIdlCustom() {
    read_if_needed_();
    
    return _idl_custom;
  }

  /**
   *  set if the class is custom have sense for aa valuetype
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isIdlCustom(boolean y) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsIdlCustomCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _idl_custom = y;
  }

  /**
   * returns the class having the name given in argument in case it
   * exist, else 0/null. In case the package is specified, the class must
   * be defined in a sub-level of the package
   */
  public static UmlClass get(String n, UmlPackage p)
  {
    if (p == null) {
      UmlClass x = (UmlClass) classes.get(n);
      
      if (x != null)
        return x;
    }
    
    UmlCom.send_cmd(CmdFamily.classGlobalCmd, ClassGlobalCmd._findClassCmd,
  		  (p != null) ? ((UmlBaseItem) p).identifier_() : 0, n);
    
    return (UmlClass) UmlBaseItem.read_();
  }

  /**
   *  Return the class supporting the stereotype corresponding to
   *  the first parameter being 'profile_name:stereotype_name', or 0/null
   */
  public static UmlClass findStereotype(String s, boolean caseSensitive)
  {
    UmlCom.send_cmd(CmdFamily.packageGlobalCmd, PackageGlobalCmd._findStereotypeCmd,
                     (caseSensitive) ? "y" : "n", s);
    return (UmlClass) UmlBaseItem.read_();
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
    _base_type = null;
    _switch_type = null;
    
    super.unload(rec, del);
  }

  /**
   * key includes package/class-container
   */
  private static Hashtable classes;
static { classes = new Hashtable(1001); }
  private boolean _abstract;
  private boolean _active;

  private boolean _cpp_external;

  private boolean _java_external;

  private boolean _java_final;
  private boolean _php_external;

  private boolean _php_final;
  private boolean _python_external;

  private boolean _python_2_2;

  private boolean _idl_external;

  private boolean _idl_local;

  private boolean _idl_custom;

  private UmlTypeSpec _base_type;
  private UmlClassDiagram _assoc_diagram;
  /**
   *  union
   */
  private UmlTypeSpec _switch_type;
  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseClass(long id, String n) { super(id, n); }

  protected void read_uml_() {
    _assoc_diagram = (UmlClassDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _abstract = UmlCom.read_bool();
    
    if (_stereotype.equals("typedef")) {
      _base_type = new UmlTypeSpec();
      _base_type.type = (UmlClass) UmlBaseItem.read_();
      if (_base_type.type == null)
        _base_type.explicit_type = UmlCom.read_string();
    }
    _active = UmlCom.read_bool();
  }

  protected void read_cpp_() {
    super.read_cpp_();  
    _cpp_external = UmlCom.read_bool();
  }

  protected void read_java_() {
    super.read_java_();  
    
    _java_final = UmlCom.read_bool();
    _java_external = UmlCom.read_bool();
  }

  protected void read_php_() {
    super.read_php_();  
    
    _php_final = UmlCom.read_bool();
    _php_external = UmlCom.read_bool();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
    super.read_python_();
    _python_2_2 = UmlCom.read_bool();
    _python_external = UmlCom.read_bool();
  }

  protected void read_idl_() {
    super.read_idl_();
    _switch_type = new UmlTypeSpec();
    _switch_type.type = (UmlClass) UmlBaseItem.read_();
    if (_switch_type.type == null)
      _switch_type.explicit_type = UmlCom.read_string();
    _idl_local = UmlCom.read_bool();
    _idl_custom = UmlCom.read_bool();
    _idl_external = UmlCom.read_bool();
  }

  protected void reread_if_needed_() {
    if (defined_()) {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.getUmlDefCmd);
      read_uml_();
    }
  }

  /**
   *  to set the name
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Name(String s) throws RuntimeException {
    super.set_Name(s);
  
    UmlItem[] ch = children();
    String destr = "~" + name();
  
    for (int i = 0; i != ch.length; i += 1) {
      if (ch[i].kind() == anItemKind.anOperation) {
        if (ch[i].name() == name())
  	ch[i].set_Name(s);
        else if (ch[i].name() == destr)
  	ch[i].set_Name("~" + s);
      }
    }
  }

};
