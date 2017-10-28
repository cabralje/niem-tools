package fr.bouml;

abstract class UmlBaseExpansionNode extends UmlActivityObject {
  /**
   *   returns a new expansion node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlExpansionNode create(UmlExpansionRegion parent, String s) throws RuntimeException
  {
    return (UmlExpansionNode) parent.create_(anItemKind.anExpansionNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anExpansionNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseExpansionNode(long id, String s) {
    super(id, s);
  }

}
