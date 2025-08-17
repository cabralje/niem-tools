package fr.bouml;


/**
 *   Manage the components.
 */
abstract class UmlBaseComponent extends UmlItem {
  /**
   *  returns a new component named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlComponent create(UmlComponentView parent, String s) throws RuntimeException
  {
    return (UmlComponent) parent.create_(anItemKind.aComponent, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aComponent;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlComponentDiagram associatedDiagram() {
    read_if_needed_();
    
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlComponentDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    
    _assoc_diagram = d;
  
  }

  /**
   *  returns (in Java a copy of) the optional realizing classes
   */
  public UmlClass[] realizingClasses() {
    read_if_needed_();
    
    return (UmlClass[]) _realizing.clone();
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

  private UmlComponentDiagram _assoc_diagram;
  private UmlClass[] _realizing;
  private UmlClass[] _provided;
  private UmlClass[] _required;
  protected void read_uml_() {
    _assoc_diagram = (UmlComponentDiagram) UmlBaseItem.read_();
    super.read_uml_();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    _realizing = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
      _realizing[index] = (UmlClass) UmlBaseItem.read_();
    
    n = UmlCom.read_unsigned();
    _provided = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
      _provided[index] = (UmlClass) UmlBaseItem.read_();
    
    n = UmlCom.read_unsigned();
    _required = new UmlClass[n];
    
    for (index = 0; index != n; index += 1)
      _required[index] = (UmlClass) UmlBaseItem.read_();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseComponent(long id, String n) { super(id, n); }
};
