
public class UmlRelation extends UmlBaseRelation {

	public UmlRelation() {
		// TODO Auto-generated constructor stub
		super();
		kind = anItemKind.aRelation;
		_role_name = "Generalisation";
	}
	
	public UmlRelation(long id, String n)
	{ 
		super(id, n); 
		//inherited_opers = null; 
		kind = anItemKind.aRelation;
		_role_name = "Generalisation";
	}

	public UmlRelation(UmlItem p, String n) {
		// super(p,anItemKind.aRelation,n);
		kind = anItemKind.aRelation;
		_role_name = "Generalisation";
	}
	  
	  public aRelationKind relationKind() {
		 //   read_if_needed_();
		    
		    return _rel_kind;
		  }
	  
	  public String multiplicity() {
		 //   read_if_needed_();
		    
		    return _multiplicity;
		  }
	  
	  public String pretty_name() {
		    return roleName();
		  }
	  private String _role_name;
	  
	  public String roleName() {
		    // read_if_needed_();
		    
		    return _role_name;
		  }
	  
	  private aRelationKind _rel_kind = aRelationKind.aGeneralisation;
	  private String _multiplicity;
	  
}
