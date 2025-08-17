package fr.bouml;


/**
 *   Manage the use case views
 */
abstract class UmlBaseUseCaseView extends UmlView {
  /**
   *  returns a new use case view named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlUseCaseView create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlUseCaseView) parent.create_(anItemKind.anUseCaseView, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anUseCaseView;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseUseCaseView(long id, String n) { super(id, n); }

};
