package fr.bouml;

/**
 *  This class manages 'formal parameters' of a template class. For instance 'T' in 
 * 
 * 	template class Cl<class T> ...
 */
abstract class UmlBaseFormalParameter {
  public UmlBaseFormalParameter(){}

  /**
   *  returns the name of the formal
   */
  public String name() { return _name; }

  /**
   *  returns the type of a formal, probably "class"
   */
  public String type() { return _type; }

  /**
   *  returns the default actual value
   */
  public UmlTypeSpec defaultValue() { return _default_value; }

  /**
   *   return the optional extend (Java)
   */
  public UmlTypeSpec extend() { return _extends; }
  protected String _name;
  /**
   *  "class" ...
   */
  protected String _type;
  protected UmlTypeSpec _default_value;
  protected UmlTypeSpec _extends;
  protected void read_() {
    _name = UmlCom.read_string();
    _type = UmlCom.read_string();
    _default_value = new UmlTypeSpec();
    _default_value.type = (UmlClass) UmlBaseItem.read_();
    if (_default_value.type == null)
      _default_value.explicit_type = UmlCom.read_string();
    _extends = new UmlTypeSpec();
    _extends.type = (UmlClass) UmlBaseItem.read_();
    if (_extends.type == null)
      _extends.explicit_type = UmlCom.read_string();
  }

};
