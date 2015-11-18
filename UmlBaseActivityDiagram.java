
abstract class UmlBaseActivityDiagram extends UmlDiagram {
  /**
   *   returns a new activity diagram named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityDiagram create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlActivityDiagram) parent.create_(anItemKind.anActivityDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anActivityDiagram;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityDiagram(long id, String s) {
    super(id, s);
  }

}
