package fr.bouml;


abstract class UmlBaseObjectCompositeDiagram extends UmlDiagram {
  /**
   *   returns a new object composite diagram named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlObjectCompositeDiagram create(UmlClassInstance parent, String s) throws RuntimeException
  {
    return (UmlObjectCompositeDiagram) parent.create_(anItemKind.aClassCompositeDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aClassCompositeDiagram;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseObjectCompositeDiagram(long id, String s) {
    super(id, s);
  }

}
