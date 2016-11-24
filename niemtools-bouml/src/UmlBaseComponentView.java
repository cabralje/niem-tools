
/**
 *   Manage the component views
 */
abstract class UmlBaseComponentView extends UmlView {
  /**
   *  returns a new component view named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlComponentView create(UmlPackage parent, String s) throws RuntimeException
  {
    return (UmlComponentView) parent.create_(anItemKind.aComponentView, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aComponentView;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseComponentView(long id, String n) { super(id, n); }

};
