/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlClassInstance extends UmlBaseClassInstance {

	/**
	 *
	 */
	public UmlClassInstance() {
		super();
	}

	public UmlClassInstance(UmlItem p, String n) {
		super(p,anItemKind.aClassInstance,n);
	}

	public UmlClass type() {
		// read_if_needed_();
		return (UmlClass)_type;
	}
}
