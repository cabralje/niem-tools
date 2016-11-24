
public class UmlNcRelation extends UmlItem {

	  public UmlNcRelation(long id, String n){ super(id, n); }

	  /**
	   * returns a string indicating the king of the element
	   */
	  public String sKind() {
	    return "non class relation";
	  }
	  
	  public static UmlNcRelation create(aRelationKind kind, UmlItem start, UmlItem end) throws RuntimeException
	  {
	    //UmlCom.send_cmd(start.identifier_(), OnInstanceCmd.createCmd, anItemKind.aNcRelation,
	  	//	  kind, end.identifier_());
	    //UmlNcRelation result = (UmlNcRelation) UmlBaseItem.read_();
	    
	    //if (result != null)
	    //  start.reread_children_if_needed_();
	    //else
	    //  throw new RuntimeException("can't be created");
	    
	   // UmlNcRelation result = new UmlNcRelation();
		UmlNcRelation r = (UmlNcRelation)start.create_(anItemKind.aNcRelation, "substitutionGroup");
		r._target = end;
	    return r;
	  }
	
	  public UmlNcRelation(UmlItem p, String n) {
			// super(p,anItemKind.aRelation,n);
			kind = anItemKind.aRelation;
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
	    //read_if_needed_();
	    
	    return _rel_kind;
	  }

	  /**
	   *  returns the 'end' object (the 'start' object is the parent of the relation) no set !
	   */
	  public UmlItem target() {
	    //read_if_needed_();
	    
	    return _target;
	  }

	  private aRelationKind _rel_kind;

	  private UmlItem _target;
}
