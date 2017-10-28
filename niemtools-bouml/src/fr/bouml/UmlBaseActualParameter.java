package fr.bouml;

/**
 *  This class manages 'actual parameters' of a template class. For instance T in
 * 
 * 	class Cl1 : public Cl2<T> ...
 */
abstract class UmlBaseActualParameter {
  public UmlBaseActualParameter() {}

  /**
   *  returns the class of the corresponding formal
   */
  public final UmlClass superClass() { return _superClass; }

  /**
   *  returns the actual rank (0...)
   */
  public final int rank() { return _rank; }

  /**
   *  returns the actual's value
   */
  public final UmlTypeSpec value() { return _value; }

  protected UmlClass _superClass;
  protected int _rank;
  protected UmlTypeSpec _value;
  protected void read_() {
    _superClass = (UmlClass) UmlBaseItem.read_();	// cannot be 0
    _rank = UmlCom.read_unsigned();
    _value = new UmlTypeSpec();
    _value.type = (UmlClass) UmlBaseItem.read_();
    if (_value.type == null)
      _value.explicit_type = UmlCom.read_string();
  }

}
