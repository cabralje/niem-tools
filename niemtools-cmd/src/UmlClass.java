import java.util.Vector;

/**
 *
 */

/**
 * @author Jim
 *
 */
@SuppressWarnings("rawtypes")
public class UmlClass extends UmlItem {

	/**
	 *
	 */

	protected Vector inherited_opers;

	protected static Vector classes;

	static { classes = new Vector(); }

/*	@SuppressWarnings("unchecked")
	public UmlClass() {
		// TODO Auto-generated constructor stub
		super();
		kind = anItemKind.aClass;
	    _base_type = new UmlTypeSpec();
	    _base_type.type = this;
		classes.addElement(this);
	}*/

	@SuppressWarnings("unchecked")
	public UmlClass(long id, String n)
	{ 
		super(id, n); 
		//inherited_opers = null;
		kind = anItemKind.aClass;
	    _base_type = new UmlTypeSpec();
	    _base_type.type = this;
	    _base_type.explicit_type = n;
		classes.addElement(this);
	}

	@SuppressWarnings("unchecked")
	public UmlClass(UmlItem p, String n) {
		super(p,anItemKind.aClass,n);
	    _base_type = new UmlTypeSpec();
	    _base_type.type = this;
	    _base_type.explicit_type = n;
		classes.addElement(this);
	}

	  public UmlTypeSpec baseType() {
		    // read_if_needed_();
		    return _base_type;
		  }
	  private UmlTypeSpec _base_type;
	  
	  /**
	   * returns the class having the name given in argument in case it
	   * exist, else 0/null. In case the package is specified, the class must
	   * be defined in a sub-level of the package
	   */
	  public static UmlClass get(String n, UmlPackage p)
	  {
	   for (int i=0; i<classes.size(); i++)
	   {
	     UmlClass c = (UmlClass) classes.elementAt(i);
	     if (c.pretty_name().equals(n))
	    	 return c;
	   }
	   return null;
	  }
	  
	public static UmlClass create(UmlItem parent, String s) throws RuntimeException
	{
		return (UmlClass) parent.create_(anItemKind.aClass, s);
	}
}
