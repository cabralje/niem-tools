
abstract class UmlBaseClassInstance extends UmlItem {
  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aClassInstance;
  }

  /**
   *  Returns a new class instance
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...) return 0
   *   in C++ and produce a RuntimeException in Java
   */
  public static UmlClassInstance create(UmlItem parent, String name, UmlClass type) throws RuntimeException
  {
    UmlCom.send_cmd(parent.identifier_(), OnInstanceCmd.createCmd, anItemKind.aClassInstance,
  		   type.identifier_());
    UmlClassInstance result = (UmlClassInstance) UmlBaseItem.read_();
    
    if (result != null) {
      parent.reread_children_if_needed_();
      if (name != null) result.set_Name(name);
    }
    else
      throw new RuntimeException("Cannot create the class instance");
    return result;
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
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, v.identifier_());
    UmlCom.check();
  
    _type = v;
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
   *  Returns the attributes having a value
   */
  public SlotRelation[] relationsValue() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.relationsCmd, (long) 0);
  
    int n = UmlCom.read_unsigned();
    SlotRelation[] v = new SlotRelation[n];
  
    for (int index = 0; index != n; index += 1) {
      UmlRelation rel = (UmlRelation) UmlBaseItem.read_();
      
      v[index] = new SlotRelation(rel, (UmlClassInstance) UmlBaseItem.read_());
    }
    return v;
  }

  /**
   *  Returns all the attributes of the class instance,
   *  including the inherited
   */
  public UmlAttribute[] availableAttributes() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.attributesCmd, (byte) 1);
  
    int n = UmlCom.read_unsigned();
    UmlAttribute[] v = new UmlAttribute[n];
  
    for (int index = 0; index != n; index += 1)
      v[index] = (UmlAttribute) UmlBaseItem.read_();
    return v;
  }

  /**
   *  Returns all the possible relations from the current instance to 'other', including the inherited
   */
  public UmlRelation[] availableRelations(UmlClassInstance other) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.relationsCmd, other.identifier_());
  
    int n = UmlCom.read_unsigned();
    UmlRelation[] v = new UmlRelation[n];
  
    for (int index = 0; index != n; index += 1)
      v[index] = (UmlRelation) UmlBaseItem.read_();
    return v;
  }

  /**
   *  Remove the slot if the value is null.
   *  Else set the value for the given attribute, replacing it
   *  if the slot already exist.
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AttributeValue(UmlAttribute attribute, String value) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAttributeCmd, attribute.identifier_(), value);
    UmlCom.check();
  }

  /**
   *  Add the slot (does nothing if it already exist)
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void add_Relation(UmlRelation relation, UmlClassInstance other) throws RuntimeException {
    UmlItem[] v = new UmlItem[2];
  
    v[0] = relation;
    v[1] = other;
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addRelationCmd, v);
    UmlCom.check();
  }

  /**
   *  Remove the slot (does nothing if it doesn't exist)
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void remove_Relation(UmlRelation relation, UmlClassInstance other) throws RuntimeException {
    UmlItem[] v = new UmlItem[2];
  
    v[0] = relation;
    v[1] = other;
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeRelationCmd, v);
    UmlCom.check();
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlObjectDiagram associatedDiagram() {
    read_if_needed_();
  
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlObjectDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    _assoc_diagram = d;
  }

  private UmlClass _type;

  private UmlObjectDiagram _assoc_diagram;

  protected void read_uml_() {
    _assoc_diagram = (UmlObjectDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _type = (UmlClass) UmlBaseItem.read_();
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseClassInstance(long id, String s) {
    super(id, s);
  }

}
