
/**
 *   Manage the nodes
 */
abstract class UmlBaseNode extends UmlItem {
  /**
   *  returns a new node named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlNode) parent.create_(anItemKind.aNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aNode;
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
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd,
  		  (d == null) ? 0 : (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    
    _assoc_diagram = d;
  }

  private UmlDeploymentDiagram _assoc_diagram;
  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseNode(long id, String n) { super(id, n); }

  protected void read_uml_() {
    _assoc_diagram = (UmlDeploymentDiagram) UmlBaseItem.read_();
    super.read_uml_();
  }

};
