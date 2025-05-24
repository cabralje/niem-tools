package fr.bouml;


abstract class UmlBaseRolePartInstance extends UmlPortRefOwner {
  /**
   *   returns a new role/part instance named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlRolePartInstance create(UmlClassInstance parent, String s, UmlRolePart type) throws RuntimeException
  {
    UmlCom.send_cmd(parent.identifier_(), OnInstanceCmd.createCmd, anItemKind.aRolePartInstance,
  		  type.identifier_());
    UmlRolePartInstance result = (UmlRolePartInstance) UmlBaseItem.read_();
    
    if (result != null) {
      parent.reread_children_if_needed_();
      if (s != null) result.set_Name(s);
    }
    else
      throw new RuntimeException("Cannot create the role/part instance");
    return result;
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aRolePartInstance;
  }

  /**
   *  return the role/part typing the instance
   */
  public UmlRolePart type() {
    read_if_needed_();
    return _type;
  }

  /**
   *  Returns the attributes having a value
   */
  public SlotAttribute[] attributesValue() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.attributesCmd, (byte) 0);
  
    int n = UmlCom.read_unsigned();
    SlotAttribute[] v = new SlotAttribute[n];
  
    for (int index = 0; index != n; index += 1) {
      UmlAttribute at = (UmlAttribute) UmlBaseItem.read_();
      
      v[index] = new SlotAttribute(at, UmlCom.read_string());
    }
    return v;
  }

  /**
   *  Remove the slot if the value is null.
   *  Else set the value for the given attribute, replacing it
   *  if the slot already exist.
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AttributeValue(UmlAttribute attribute, String value) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAttributeCmd, attribute.identifier_(), value);
    UmlCom.check();
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseRolePartInstance(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _type = (UmlRolePart) UmlBaseItem.read_();
  }

  private UmlRolePart _type;

}
