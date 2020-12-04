package fr.bouml;


abstract class UmlBaseClassCompositeDiagram extends UmlDiagram {
  /**
   *   returns a new class composite diagram named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlClassCompositeDiagram create(UmlClass parent, String s) throws RuntimeException
  {
    return (UmlClassCompositeDiagram) parent.create_(anItemKind.aClassCompositeDiagram, s);
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
  protected  UmlBaseClassCompositeDiagram(long id, String s) {
    super(id, s);
  }

}
