package fr.bouml;


/**
 *  this class manages class instance reference
 */
class UmlBaseClassInstanceReference {
  /**
   *  return the type of the instance
   */
  public UmlClass type() {
    return (_instance != null) ? _instance.type() : _class;
  }

  /**
   *  return the corresponding instance in the model, or 0/null
   */
  public UmlClassInstance instance() {
    return _instance;
  }

  /**
   *  return the name of the instance
   */
  public String name() {
    return (_instance != null) ? _instance.name() : _name;
  }

  private UmlClass _class;

  private UmlClassInstance _instance;

  private String _name;

  /**
   *  internal, don't call it
   */
  public void read_() {
    _instance = (UmlClassInstance) UmlBaseItem.read_();
    if (_instance == null) {
      _name = UmlCom.read_string();
      _class = (UmlClass) UmlBaseItem.read_();
    }
  }

}
