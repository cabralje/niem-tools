
/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlPackage extends UmlBasePackage {

	public UmlPackage() {
		super();
		kind = anItemKind.aPackage;
	}

	public UmlPackage(UmlItem p, String n) {
		super(p,anItemKind.aPackage,n);
	}

	public String sKind() {
		return (stereotype().equals("profile"))
				? "profile" : "package";
	}

	// support sorting

	public void sort() {
		sortChildren();
	}

	public int orderWeight() {
		return 1;
	}
}
