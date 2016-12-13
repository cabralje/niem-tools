
/**
 *
 */

/**
 * @author Jim
 *
 */
public class UmlClassView extends UmlItem {

	public UmlClassView() {
		super();
		kind = anItemKind.aClassView;
	}

	public UmlClassView(UmlItem p, String n) {
		super(p,anItemKind.aClassView,n);
	}

	public String sKind() {
		return "class view";
	}

	public static UmlClassView create(UmlPackage parent, String prefix)
	{
		return (UmlClassView)(parent.create_(anItemKind.aClassView, prefix));
	}

	public void sort() {
		sortChildren();
	}

	public UmlClassView(long id, String n){ super(id, n); }

	public int orderWeight() {
		return 3;
	}

}
