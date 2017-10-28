
public class UmlBaseClassInstance extends UmlItem {

	protected UmlBaseClass _type;

	public UmlBaseClassInstance(UmlItem p, anItemKind aclassinstance, String n) {
		super(p,anItemKind.aClassInstance,n);
	}

	public UmlBaseClassInstance() {
		// TODO Auto-generated constructor stub
	}

	public void set_Type(UmlBaseClass v) throws RuntimeException {
	    // UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, v.identifier_());
	    // UmlCom.check();
	
	    _type = v;
	  }

	public static UmlClassInstance create(UmlClassView nsClassView, String filterUMLElement, UmlBaseClass baseType) {
		// TODO Auto-generated method stub
		return null;
	}

	public static UmlClassInstance create(UmlItem parent, String s, UmlBaseClass type) throws RuntimeException {
		/* UmlCom.send_cmd(parent.identifier_(), OnInstanceCmd.createCmd, anItemKind.aClassInstance,
	  		   type.identifier_());
	    UmlClassInstance result = (UmlClassInstance) UmlBaseItem.read_();
	
	    if (result != null) {
	      parent.reread_children_if_needed_();
	      if (name != null) result.set_Name(name);
	    }
	    else
	      throw new RuntimeException("Cannot create the class instance"); 
	    return result; */
	
		UmlClassInstance ci = (UmlClassInstance) parent.create_(anItemKind.aClassInstance, s);
		ci.set_Type(type);
		return ci;
	}

}
