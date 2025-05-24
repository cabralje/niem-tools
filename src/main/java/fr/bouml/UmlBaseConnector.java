package fr.bouml;


abstract class UmlBaseConnector extends UmlItem {
  /**
   *  returns a new connector between 'c1' and 'c2'
   * 
   *  In case it cannot be created return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlConnector create(UmlConnectable c1, UmlConnectable c2) throws RuntimeException
  {
    UmlCom.send_cmd(c1.identifier_(), OnInstanceCmd.createCmd,
  		  anItemKind.aConnector, c2.identifier_());
      UmlConnector result = (UmlConnector) UmlBaseItem.read_();
      
      if (result != null) {
        c1.reread_children_if_needed_();
        c2.reread_children_if_needed_();
        return result;
      }
      else
        throw new RuntimeException("can't be created");
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aConnector;
  }

  /**
   *  to set the name
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Name(String s) throws RuntimeException {
    if (!name().equals(s)) {
      super.set_Name(s);
      ((UmlBaseConnector) UmlBaseItem.read_()).set_Name(s);
    }
  }

  /**
   *  to set the stereotype
   *   On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Stereotype(String s) throws RuntimeException {
    super.set_Stereotype(s);
    
    UmlBaseConnector other = (UmlBaseConnector) UmlBaseItem.read_();
    
    if (other != null)
      other._stereotype = s;
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
   *  return the target of the connector, see also use side()
   */
  public UmlConnectable connectable() {
    read_if_needed_();
    return _connectable;
  }

  /**
   *  if 'first' is true returns the first side of the connector, else the second
   */
  public UmlConnector side(boolean first) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.sideCmd,
  		  (first) ? (byte) 1 : (byte) 0);
    
    return (UmlConnector) UmlBaseItem.read_();
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseConnector(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _multiplicity = UmlCom.read_string();
    _connectable = (UmlConnectable) UmlBaseItem.read_();
  }

  private String _multiplicity;

  private UmlConnectable _connectable;

}
