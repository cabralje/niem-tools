package fr.bouml;


/**
 *   Manage the deployment views
 */
abstract class UmlBaseDeploymentView extends UmlView {
  /**
   *  returns a new deployment view named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlDeploymentView create(UmlPackage parent, String s) throws RuntimeException
  {
    return (UmlDeploymentView) parent.create_(anItemKind.aDeploymentView, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aDeploymentView;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseDeploymentView(long id, String n) { super(id, n); }

};
