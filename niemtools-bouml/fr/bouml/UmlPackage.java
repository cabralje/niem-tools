package fr.bouml;

import java.io.*;
import java.util.*;

/**
 *  This class manages a 'package', notes that the project itself is a package
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
public
class UmlPackage extends UmlBasePackage {
	public UmlPackage(long id, String n){ super(id, n); }

	/**
	 * returns a string indicating the king of the element
	 */
	public String sKind() {
		return (stereotype().equals("profile"))
				? "profile" : "package";
	}

	/**
	 * entry to produce the html code receiving chapter number
	 * path, rank in the mother and level in the browser tree
	 */
	public void html(String pfix, int rank, int level) throws IOException {
		define();

		if (stereotype().equals("profile"))
			chapter("Profile", pfix, rank, "profile", level);
		else
			chapter("Package", pfix, rank, "package", level);

		write_description();

		boolean ul = false;
		String s = cppNamespace();

		if (s.length() != 0) {
			fw.write("<p></p><ul>\n");
			ul = true;
			fw.write("<li>C++ namespace : ");
			writeq(s);
			fw.write("</li>\n");
		}

		s = javaPackage();

		if (s.length() != 0) {
			if (! ul)
				fw.write("<p></p><ul>");
			ul = true;
			fw.write("<li>Java package : ");
			writeq(s);
			fw.write("</li>\n");
		}

		if (ul)
			fw.write("</ul>\n");

		write_dependencies();

		UmlDiagram d = associatedDiagram();

		if (d != null) {
			fw.write("<p>Diagram : ");
			d.write();
			fw.write("</p>\n");
		}

		write_properties();

		write_children(pfix, rank, level);

		unload(false, false);
	}

	/**
	 * set the html ref
	 * set the package list
	 */
	@SuppressWarnings("unchecked")
	public void memo_ref() {
		packages.addElement(this);
		super.memo_ref();

	}

	public static void ref_index() throws IOException
	{
		if (!packages.isEmpty())
			fw.write("<a href=\"packages.html\" target = \"projectFrame\"><b> -Packages- </b></a>");
	}

	public static void generate_index() throws IOException
	{
		generate_index(packages, "Package", "packages");
	}

	public boolean chapterp() {
		return true;
	}

	protected static Vector packages;

	static { packages = new Vector(); }

	public void sort() {
		sortChildren();
	}

	public int orderWeight() {
		return 1;
	}

};
