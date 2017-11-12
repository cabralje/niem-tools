package fr.bouml;

abstract class UmlBaseActivityRegion extends UmlItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityRegion(long id, String s) {
    super(id, s);
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

  private UmlActivityDiagram _assoc_diagram;

  protected void read_uml_() {
    _assoc_diagram = (UmlActivityDiagram) UmlBaseItem.read_();
    super.read_uml_();
  }

}
