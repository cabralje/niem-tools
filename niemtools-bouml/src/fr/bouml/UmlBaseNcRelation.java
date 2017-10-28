package fr.bouml;

/**
 *  Manage the relations between non class objects, for instances
 *  dependency between packages and use case generalization
 */
abstract class UmlBaseNcRelation extends UmlItem {
  /**
   *  returns a new non class relation of the given 'kind' from 'start' to 'end'
   * 
   *  In case it cannot be created ('parent' cannot contain it etc ...)
   *  return 0 in C++ and produce a RuntimeException in Java
   */
  public static UmlNcRelation create(aRelationKind kind, UmlItem start, UmlItem end) throws RuntimeException
  {
    UmlCom.send_cmd(start.identifier_(), OnInstanceCmd.createCmd, anItemKind.aNcRelation,
  		  kind, end.identifier_());
    UmlNcRelation result = (UmlNcRelation) UmlBaseItem.read_();
    
    if (result != null)
      start.reread_children_if_needed_();
    else
      throw new RuntimeException("can't be created");
    
    return result;
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aNcRelation;
  }

  /**
   *  returns the kind of the relation : aGeneralisation or a Dependency, the other are not allowed
   *  for the non class relations
   */
  public aRelationKind relationKind() {
    read_if_needed_();
    
    return _rel_kind;
  }

  /**
   *  returns the 'end' object (the 'start' object is the parent of the relation) no set !
   */
  public UmlItem target() {
    read_if_needed_();
    
    return _target;
  }

  private aRelationKind _rel_kind;

  private UmlItem _target;

  protected void read_uml_() {
    super.read_uml_();
    _rel_kind = aRelationKind.fromInt(UmlCom.read_char());
    _target = UmlBaseItem.read_();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseNcRelation(long id, String n) { super(id, n); }

}
