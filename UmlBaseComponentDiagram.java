
/**
 *   Manage the component diagrams
 */
abstract class UmlBaseComponentDiagram extends UmlDiagram {
  /**
   *  returns a new component diagram named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlComponentDiagram create(UmlComponentView parent, String s) throws RuntimeException
  {
    return (UmlComponentDiagram) parent.create_(anItemKind.aComponentDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aComponentDiagram;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseComponentDiagram(long id, String n) { super(id, n); }

};
