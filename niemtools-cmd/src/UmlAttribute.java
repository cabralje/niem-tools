/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlAttribute extends UmlBaseAttribute {

	/**
	 *
	 */
	public UmlAttribute() {
		
		super();
	}

	public UmlAttribute(UmlItem p, String n) {
		super(p,anItemKind.anAttribute,n);
	}
	  
	  /**
	   *  returns the attribute UML type
	   */
	  public UmlTypeSpec type() {
	    // read_if_needed_();
	    
	    return _type;
	  }

	  /**
	   *  to set the attribute UML type
	   *  
	   *  On error return FALSE in C++, produce a RuntimeException in Java
	   */
	  public void set_Type(UmlTypeSpec t) throws RuntimeException {
	    //UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, t);
	    //UmlCom.check();
	  
	    _type = t;
	  }
	  public void set_Multiplicity(String s) throws RuntimeException {
		   // UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMultiplicityCmd, s);
		    //UmlCom.check();
		  
		    _multiplicity = s;
		  }
	  
	  public String multiplicity() {
		   // read_if_needed_();
		    
		    return _multiplicity;
		  }
	  
	  private String _multiplicity = "";
	  
	  private UmlTypeSpec _type;
}
