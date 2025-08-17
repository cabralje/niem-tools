package fr.bouml;


abstract class UmlBaseInterruptibleActivityRegion extends UmlActivityRegion {
  /**
   *   returns a new interruptible activity region named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlInterruptibleActivityRegion create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlInterruptibleActivityRegion) parent.create_(anItemKind.anInterruptibleActivityRegion, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anInterruptibleActivityRegion;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseInterruptibleActivityRegion(long id, String s) {
    super(id, s);
  }

}
