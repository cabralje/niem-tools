
abstract class UmlBaseRegion extends UmlItem {
  /**
   *   returns a new region named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlRegion create(UmlState parent, String s) throws RuntimeException
  {
    return (UmlRegion) parent.create_(anItemKind.aRegion, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aRegion;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseRegion(long id, String s) {
    super(id, s);
  }

}
