package fr.bouml;


abstract class UmlBaseRolePart extends UmlPortRefOwner {
  /**
   *   returns a new role/part named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlRolePart create(UmlClass parent, String s) throws RuntimeException
  {
    return (UmlRolePart) parent.create_(anItemKind.aRolePart, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aRolePart;
  }

  /**
   *  return the element is a part if true, else a role
   */
  public boolean isPart() {
    read_if_needed_();
    return _isPart;
  }

  /**
   *  set set if the element is a part or a role
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPart(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsPartCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _isPart = v;
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
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseRolePart(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _isPart = UmlCom.read_bool();
    _type = (UmlClass) UmlBaseItem.read_();
    _multiplicity = UmlCom.read_string();
  }

  private boolean _isPart;

  private UmlClass _type;

  private String _multiplicity;

}
