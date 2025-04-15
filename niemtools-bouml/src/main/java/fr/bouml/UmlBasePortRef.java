package fr.bouml;


abstract class UmlBasePortRef extends UmlConnectable {
  /**
   *   returns a new port reference named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlPortRef create(UmlPortRefOwner parent, String s, UmlPort port) throws RuntimeException
  {
    UmlCom.send_cmd(parent.identifier_(), OnInstanceCmd.createCmd, anItemKind.aPortRef,
  		  port.identifier_());
    UmlPortRef result = (UmlPortRef) UmlBaseItem.read_();
    
    if (result == null)
      throw new RuntimeException("Cannot create the port reference");
    return result;
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aPortRef;
  }

  /**
   *  return the port
   */
  public UmlPort port() {
    read_if_needed_();
    return _port;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBasePortRef(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _port = (UmlPort) UmlBaseItem.read_();
  }

  private UmlPort _port;

}
