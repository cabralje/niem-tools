
public class UmlBaseClass extends UmlItem {

	public static UmlClass create(UmlItem parent, String s) throws RuntimeException {
		return (UmlClass) parent.create_(anItemKind.aClass, s);
	}

	public UmlBaseClass() {
		super();
	}

	public UmlBaseClass(long id, String n) {
		super(id, n);
	}

	public UmlBaseClass(UmlItem p, anItemKind k, String n) {
		super(p, k, n);
	}

}