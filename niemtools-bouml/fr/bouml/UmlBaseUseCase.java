package fr.bouml;

/**
 *  Manage the use cases
 */
abstract class UmlBaseUseCase extends UmlItem {
  /**
   *  returns a new use case named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlUseCase create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlUseCase) parent.create_(anItemKind.anUseCase, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anUseCase;
  }

  /**
   *  returns the optional associated diagram
   */
  public UmlUseCaseDiagram associatedDiagram() {
    read_if_needed_();
    
    return _assoc_diagram;
  }

  /**
   *  sets the associated diagram, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDiagram(UmlUseCaseDiagram d) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocDiagramCmd, (d == null) ? (long) 0 : d.identifier_());
    UmlCom.check();
    
    _assoc_diagram = d;
  
  }

  /**
   *  return the extension points
   */
  public String extensionPoints() {
    read_if_needed_();
    return _extension_points;
  }

  /**
   *  set the extension points
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_ExtensionPoints(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceExceptionCmd, v);
    UmlCom.check();
  
    _extension_points = v;
  }

  private String _extension_points;

  private UmlUseCaseDiagram _assoc_diagram;
  protected void read_uml_() {
    _assoc_diagram = (UmlUseCaseDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _extension_points = UmlCom.read_string();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseUseCase(long id, String n) { super(id, n); }

};
