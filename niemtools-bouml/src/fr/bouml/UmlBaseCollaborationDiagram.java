package fr.bouml;

/**
 *   Manage the collaboration diagrams
 */
abstract class UmlBaseCollaborationDiagram extends UmlDiagram {
  /**
   *  returns a new collaboration diagram named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlCollaborationDiagram create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlCollaborationDiagram) parent.create_(anItemKind.aCollaborationDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aCollaborationDiagram;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseCollaborationDiagram(long id, String n) { super(id, n);   _def = null;
  }

  /**
   *  return the semantic part of the diagram not present in the model
   */
  public UmlCollaborationDiagramDefinition definition() {
    if (_def == null) {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.sideCmd);
      (_def = new UmlCollaborationDiagramDefinition()).read_();
    }
    return _def;
  }

  /**
   *  to unload the object to free memory, it will be reloaded automatically
   *  if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _def = null;
    super.unload(rec, del);
  }

  private UmlCollaborationDiagramDefinition _def;

};
