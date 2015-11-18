
abstract class UmlBaseActivityPartition extends UmlItem {
  /**
   *   returns a new activity partition named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityPartition create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlActivityPartition) parent.create_(anItemKind.aPartition, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aPartition;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityPartition(long id, String s) {
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

  /**
   *  return the  return the isDimension attribute
   */
  public boolean isDimension() {
    read_if_needed_();
    return _dimension;
  }

  /**
   *  set the isDimension attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isDimension(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMultiplicityCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _dimension = v;
  }

  /**
   *  return the  return the isExternal attribute
   */
  public boolean isExternal() {
    read_if_needed_();
    return _external;
  }

  /**
   *  set the isExternal attribute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isExternal(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppExternalCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _external = v;
  }

  /**
   *  return the represents
   */
  public UmlItem represents() {
    read_if_needed_();
    return _represents;
  }

  /**
   *  set the represents
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Represents(UmlItem v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDerivedCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _represents = v;
  }

  private UmlActivityDiagram _assoc_diagram;

  private boolean _dimension;

  private boolean _external;

  private UmlItem _represents;

  protected void read_uml_() {
    _assoc_diagram = (UmlActivityDiagram) UmlBaseItem.read_();
    super.read_uml_();
    _dimension = UmlCom.read_bool();
    _external = UmlCom.read_bool();
    _represents = (UmlItem) UmlBaseItem.read_();
  }

}
