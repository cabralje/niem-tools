
public class UmlBaseAttribute extends UmlItem {

	public static UmlAttribute create(UmlBaseClass parent, String s) throws RuntimeException {
	    return (UmlAttribute) parent.create_(anItemKind.anAttribute, s);
	  }

	public UmlBaseAttribute() {
		super();
	}

	public UmlBaseAttribute(long id, String n) {
		super(id, n);
	}

	public UmlBaseAttribute(UmlItem p, anItemKind k, String n) {
		super(p, k, n);
	}

}