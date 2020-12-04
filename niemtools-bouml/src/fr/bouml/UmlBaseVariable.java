package fr.bouml;


abstract class UmlBaseVariable extends UmlItem {
  /**
   *   returns a new variable named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlVariable create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlVariable) parent.create_(anItemKind.aVariable, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aVariable;
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
   *  return the property 'ordered'
   */
  public boolean isOrdered() {
    read_if_needed_();
    return _ordered;
  }

  /**
   *  set the property 'ordered'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isOrdered(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setOrderingCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _ordered = v;
  }

  /**
   *  return the property 'unique'
   */
  public boolean isUnique() {
    read_if_needed_();
    return _unique;
  }

  /**
   *  set the property 'unique'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isUnique(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUniqueCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _unique = v;
  }

  /**
   *  return the visibility
   */
  public aVisibility visibility() {
    read_if_needed_();
    return _visibility;
  }

  /**
   *  set the visibility
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Visibility(aVisibility v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setVisibilityCmd, (byte) v.value());
    UmlCom.check();
  
    _visibility = v;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseVariable(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _type = new UmlTypeSpec();
    _type.type = (UmlClass) UmlBaseItem.read_();
    if (_type.type == null)
      _type.explicit_type = UmlCom.read_string();
    _multiplicity = UmlCom.read_string();
    _ordered = UmlCom.read_bool();
    _unique = UmlCom.read_bool();
    _visibility = aVisibility.fromInt(UmlCom.read_char());
  }

  private UmlTypeSpec _type;

  private String _multiplicity;

  private boolean _ordered;

  private boolean _unique;

  private aVisibility _visibility;

}
