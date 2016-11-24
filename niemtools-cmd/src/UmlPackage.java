
/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlPackage extends UmlItem {

	public UmlPackage() {
		super();
		// TODO Auto-generated constructor stub
		kind = anItemKind.aPackage;
	}

	public UmlPackage(UmlItem p, String n) {
		super(p,anItemKind.aPackage,n);
	}

	public static UmlPackage create(UmlPackage parent, String prefix)
	{
		return (UmlPackage)parent.create_(anItemKind.aPackage, prefix);
	}

	public String sKind() {
		return (stereotype().equals("profile"))
				? "profile" : "package";
	}
}
