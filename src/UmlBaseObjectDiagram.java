
abstract class UmlBaseObjectDiagram extends UmlDiagram {
  /**
   *   returns a new object diagram named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlObjectDiagram create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlObjectDiagram) parent.create_(anItemKind.anObjectDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anObjectDiagram;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseObjectDiagram(long id, String s) {
    super(id, s);
  }

}
