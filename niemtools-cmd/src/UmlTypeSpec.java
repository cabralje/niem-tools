
public class UmlTypeSpec {

	  /**
	   *  returns the type as a string in all cases
	   */
	  public String toString() {
	    return (type != null) ? type.name() : explicit_type;
	  }

	  /**
	   *  Clone the type specification, defined only in Java which does not have 'const' or 'value passing'
	   */
	  public UmlTypeSpec clone_it() {
	    UmlTypeSpec result = new UmlTypeSpec();
	    
	    result.type = type;
	    result.explicit_type = explicit_type;
	    
	    return result;
	  }

	  /**
	   *  significant in case type == 0
	   */
	  public String explicit_type;
	  public UmlClass type;
}
