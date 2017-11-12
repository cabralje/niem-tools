package fr.bouml;

abstract class UmlBaseActivityPin extends UmlPinParameter {
  /**
   *   returns a new activity action pin named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityPin create(UmlActivityAction parent, String s) throws RuntimeException
  {
    return (UmlActivityPin) parent.create_(anItemKind.anActivityPin, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anActivityPin;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityPin(long id, String s) {
    super(id, s);
  }

}
