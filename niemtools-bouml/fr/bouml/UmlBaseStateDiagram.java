package fr.bouml;

abstract class UmlBaseStateDiagram extends UmlDiagram {
  /**
   *   returns a new state diagram named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlStateDiagram create(UmlState parent, String s) throws RuntimeException
  {
    return (UmlStateDiagram) parent.create_(anItemKind.aStateDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aStateDiagram;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseStateDiagram(long id, String s) {
    super(id, s);
  }

}
