
/**
 *   Represent an operation's parameter
 */
class UmlBaseParameter {
  public UmlBaseParameter() {
    type = new UmlTypeSpec();
  }

  /**
   *   Clone the parameter, defined only in Java which does not have 'const' or 'value passing'
   */
  public UmlParameter clone_it() {
    UmlParameter p = new UmlParameter();
    
    p.dir = dir;
    p.type = type.clone_it();
    p.name = name;
    p.default_value = default_value;
  
    return p;
  }

  public String name;
  public String multiplicity;

  public String default_value;
  public UmlTypeSpec type;
  public aDirection dir;
};
