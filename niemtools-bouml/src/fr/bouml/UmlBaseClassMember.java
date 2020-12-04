package fr.bouml;


/**
 *  This class manages 'true' class's items : attributes, relation,
 *  operation and sub-classes
 */
abstract class UmlBaseClassMember extends UmlClassItem {
  /**
   *  Indicate if the member is a 'class member' (static)
   *  Always false in case of a class
   */
  public boolean isClassMember() {
    read_if_needed_();
    
    return _class_member;
  }

  /**
   *  Set if the member is a 'class member' (static), not significant for a class
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void set_isClassMember(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsClassMemberCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _class_member = y;
  }

  /**
   *  Indicate if the member is 'volatile'
   *  Always false in case of a class
   */
  public boolean isVolatile() {
    read_if_needed_();
    
    return _volatile;
  }

  /**
   *  Set if the member is 'volatile', not significant for a class
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void set_isVolatile(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsVolatileCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _volatile = y;
  }

  /**
   *  The member's visibility
   */
  public aVisibility visibility() {
    read_if_needed_();
    
    return _visibility;
  }

  /**
   *  Set the member visibility,  DefaultVisibility is not legal
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void set_Visibility(aVisibility v) throws RuntimeException {
    if (v == aVisibility.DefaultVisibility)
      throw new RuntimeException("Cannot be done");
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setVisibilityCmd, (byte) v.value());
    UmlCom.check();
  
    _visibility =  v;
  }

  /**
   *  Special case for C++, thank to the 'friend class X' it may be
   *  usefull to have a C++ visibility != other visibility
   *  In case the C++ visibility must follow the other, return
   *  DefaultVisibility
   */
  public aVisibility cppVisibility() {
    read_if_needed_();
    
    return _cpp_visibility;
  }

  /**
   *  Set the visibility for C++, DefaultVisibility is legal
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void set_CppVisibility(aVisibility v) throws RuntimeException {
    if (v == aVisibility.DefaultVisibility)
      throw new RuntimeException("Cannot be done");
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppVisibilityCmd, (byte) v.value());
    UmlCom.check();
  
    _cpp_visibility = v;
  }

  /**
   *  returns the Java annotations
   */
  public String javaAnnotations() {
    read_if_needed_();
    
    return _java_annotation;
  }

  /**
   *  set the Java annotations
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaAnnotations(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaAnnotationCmd, v);
    UmlCom.check();
    
    _java_annotation = v;
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

  private boolean _class_member;
  private boolean _volatile;
  private aVisibility _visibility;

  private aVisibility _cpp_visibility;

  private String _java_annotation;

  private String _constraint;

  protected UmlBaseClassMember(long id, String n) { super(id, n); }

  protected void read_uml_() {
    super.read_uml_();
    _class_member = UmlCom.read_bool();
    _volatile = UmlCom.read_bool();
    _visibility = aVisibility.fromInt(UmlCom.read_char());
    _constraint = UmlCom.read_string();
  }

  protected void read_cpp_() {
    super.read_cpp_();
    _cpp_visibility = aVisibility.fromInt(UmlCom.read_char());
  
  }

  protected void read_java_() {
    super.read_java_();
    _java_annotation = UmlCom.read_string();
  }

}
