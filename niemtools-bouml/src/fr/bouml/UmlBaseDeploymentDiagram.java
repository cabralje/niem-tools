package fr.bouml;


/**
 *   Manage the deployment diagrams
 */
abstract class UmlBaseDeploymentDiagram extends UmlDiagram {
  /**
   *  returns a new deployment diagram named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlDeploymentDiagram create(UmlDeploymentView parent, String s) throws RuntimeException
  {
    return (UmlDeploymentDiagram) parent.create_(anItemKind.aDeploymentDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aDeploymentDiagram;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseDeploymentDiagram(long id, String n) { super(id, n); }

};
