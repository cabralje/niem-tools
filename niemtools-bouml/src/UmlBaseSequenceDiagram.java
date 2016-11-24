
/**
 *  Manage the sequence diagrams
 */
abstract class UmlBaseSequenceDiagram extends UmlDiagram {
  /**
   *  returns a new sequence diagram named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlSequenceDiagram create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlSequenceDiagram) parent.create_(anItemKind.aSequenceDiagram, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aSequenceDiagram;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseSequenceDiagram(long id, String n) { super(id, n);   _def = null;
  }

  /**
   *  return the semantic part of the diagram not present in the model
   */
  public UmlSequenceDiagramDefinition definition() {
    if (_def == null) {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.sideCmd);
      (_def = new UmlSequenceDiagramDefinition()).read_();
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

  private UmlSequenceDiagramDefinition _def;

};
