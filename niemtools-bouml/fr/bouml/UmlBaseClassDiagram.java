package fr.bouml;

/**
 *  Manage the class diagrams
 */
abstract class UmlBaseClassDiagram extends UmlDiagram {
  /**
   *  returns a new class diagram named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlClassDiagram create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlClassDiagram) parent.create_(anItemKind.aClassDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aClassDiagram;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseClassDiagram(long id, String n) { super(id, n); }

};
