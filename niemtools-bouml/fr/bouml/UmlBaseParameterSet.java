package fr.bouml;

abstract class UmlBaseParameterSet extends UmlItem {
  /**
   *   returns a new parameter set named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityParameter create(UmlActivityAction parent, String s) throws RuntimeException
  {
    return (UmlActivityParameter) parent.create_(anItemKind.aParameterSet, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aParameterSet;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseParameterSet(long id, String s) {
    super(id, s);
  }

  /**
   *  return the pins part of the parameter set
   */
  public UmlActivityPin[] pins() {
    read_if_needed_();
    return (UmlActivityPin[]) _pins.clone();
  }

  /**
   *  set the pins
   */
  public void set_Pins(UmlActivityPin[] v) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceParameterCmd, v);
    UmlCom.check();
    _pins = (UmlActivityPin[]) v.clone();
  }

  /**
   *  to unload the object to free memory, it will be reloaded automatically
   *  if needed. Recursively done for the sub items if 'rec' is TRUE. 
   * 
   *  if 'del' is true the sub items are deleted in C++, and removed from the
   *  internal dictionnary in C++ and Java (to allow it to be garbaged),
   *  you will have to call Children() to re-access to them
   */
  public void unload(boolean rec, boolean del) {
    _pins = null;
    super.unload(rec, del);
  }

  private UmlActivityPin[] _pins;

  protected void read_uml_() {
    super.read_uml_();
    
    int n = UmlCom.read_unsigned();
    
    _pins = new UmlActivityPin[n];
  
    for (int index = 0; index != n; index += 1)
      _pins[index] = (UmlActivityPin) UmlBaseItem.read_();
  }

}
