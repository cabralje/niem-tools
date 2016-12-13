/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlClassInstance extends UmlItem {

	/**
	 *
	 */
	public UmlClassInstance() {
		super();
	}

	public UmlClassInstance(UmlItem p, String n) {
		super(p,anItemKind.aClassInstance,n);
	}

	  public void set_Type(UmlClass v) throws RuntimeException {
		    // UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, v.identifier_());
		    // UmlCom.check();
		  
		    _type = v;
		  }
	  
	public UmlClass type() {
		// read_if_needed_();
		return _type;
	}

	private UmlClass _type;
	 
	public static UmlClassInstance create(UmlItem parent, String s, UmlClass type) throws RuntimeException
	{
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
