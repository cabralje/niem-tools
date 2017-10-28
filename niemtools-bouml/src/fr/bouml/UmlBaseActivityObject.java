package fr.bouml;

abstract class UmlBaseActivityObject extends UmlActivityNode {
  /**
   *   returns a new activity object named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityObject create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlActivityObject) parent.create_(anItemKind.anActivityObject, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anActivityObject;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityObject(long id, String s) {
    super(id, s);
  }

  /**
   *  return the type
   */
  public UmlTypeSpec type() {
    read_if_needed_();
    return _type;
  }

  /**
   *  set the type
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Type(UmlTypeSpec v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, v);
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
   *  return the inState attribute
   */
  public String inState() {
    read_if_needed_();
    return _in_state;
  }

  /**
   *  set the inState attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_InState(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setInStateCmd, v);
    UmlCom.check();
  
    _in_state = v;
  }

  /**
   *  return the selection in OCL
   */
  public String selection() {
    read_if_needed_();
    return _selection;
  }

  /**
   *  set the selection in OCL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Selection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUmlActivityCmd, v);
    UmlCom.check();
  
    _selection = v;
  }

  /**
   *  return the selection in C++
   */
  public String cppSelection() {
    read_if_needed_();
    return _cpp_selection;
  }

  /**
   *  set the selection in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppSelection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppActivityCmd, v);
    UmlCom.check();
  
    _cpp_selection = v;
  }

  /**
   *  return the selection in Java
   */
  public String javaSelection() {
    read_if_needed_();
    return _java_selection;
  }

  /**
   *  set the selection in Java
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaSelection(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaActivityCmd, v);
    UmlCom.check();
  
    _java_selection = v;
  }

  /**
   *  return the ordering
   */
  public anOrdering ordering() {
    read_if_needed_();
    return _ordering;
  }

  /**
   *  set the ordering
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Ordering(anOrdering v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setOrderingCmd, (byte) v.value());
    UmlCom.check();
  
    _ordering = v;
  }

  /**
   *  return the isControlType attribute, tells whether the type of the object node is to be treated as control
   */
  public boolean isControlType() {
    read_if_needed_();
    return _is_control;
  }

  /**
   *  set the isControlType attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IsControlType(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setFlagCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _is_control = v;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlActivityDiagram associatedDiagram() {
    read_if_needed_();
  
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlActivityDiagram d) throws RuntimeException {
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
    _type = null;
    _multiplicity = null;
    _in_state = null;
    _selection = null;
    _cpp_selection = null;
    _java_selection = null;
    super.unload(rec, del);
  }

  private UmlActivityDiagram _assoc_diagram;

  private UmlTypeSpec _type;

  private String _multiplicity;

  private String _in_state;

  private String _selection;

  private String _cpp_selection;

  private String _java_selection;

  private anOrdering _ordering;

  private boolean _is_control;

  protected void read_uml_() {
    _assoc_diagram = (kind() == anItemKind.anActivityObject) ? (UmlActivityDiagram) UmlBaseItem.read_() : null;
    super.read_uml_();
    _type = new UmlTypeSpec();
    _type.type = (UmlClass) UmlBaseItem.read_();
    if (_type.type == null)
      _type.explicit_type = UmlCom.read_string();
    _multiplicity = UmlCom.read_string();
    _in_state = UmlCom.read_string();
    _selection = UmlCom.read_string();
    _ordering = anOrdering.fromInt(UmlCom.read_char());
    _is_control = UmlCom.read_bool();
  }

  protected void read_cpp_() {
    _cpp_selection  = UmlCom.read_string();
  }

  protected void read_java_() {
    _java_selection = UmlCom.read_string();
  }

}
