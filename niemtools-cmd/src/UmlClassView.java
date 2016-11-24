
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
		// TODO Auto-generated constructor stub
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

}
