
import java.io.*;

/**
 *  This class manages 'class view'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlClassView extends UmlBaseClassView {
	public UmlClassView(long id, String n){ super(id, n); }

	/**
	 * returns a string indicating the king of the element
	 */
	public String sKind() {
		return "class view";
	}

	/**
	 * entry to produce the html code receiving chapter number
	 * path, rank in the mother and level in the browser tree
	 */
	public void html(String pfix, int rank, int level) throws IOException {
		html(pfix, rank, "Class View", level, "view");

		unload(false, false);
	}

	public boolean chapterp() {
		return true;
	}

	public void sort() {
		sortChildren();
	}

	public int orderWeight() {
		return 3;
	}

};
