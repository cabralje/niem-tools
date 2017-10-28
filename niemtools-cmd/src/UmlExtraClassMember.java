
public class UmlExtraClassMember extends UmlItem {

	public static UmlExtraClassMember create(UmlBaseClass parent, String name) throws RuntimeException
	{
		return (UmlExtraClassMember) parent.create_(anItemKind.anExtraClassMember, name);
	}

	public UmlExtraClassMember(UmlItem p, String n) {
		super(p,anItemKind.aClassInstance,n);
	}

	/**
	 *  returns the kind of the item
	 */
	public anItemKind kind() {
		return anItemKind.anExtraClassMember;
	}

	public String sKind() {
		return "extra class member";
	}
}
