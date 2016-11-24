import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 */

/**
 * @author Jim
 *
 */
@SuppressWarnings("rawtypes")
public class UmlItem {

	public UmlItem() {
		super();
		// TODO Auto-generated constructor stub
		set_Stereotype(NiemTools.niemStereotype);
	}

	public UmlItem(long id, String n)
	{ 
		super();
		name = n;
		_name = n;
		known = false;
		set_Stereotype(NiemTools.niemStereotype);
	}

	public UmlItem(UmlItem p, anItemKind k, String n) {
		super();
		_parent = p;
		kind = k;
		name = n;
		_name = n;
		set_Stereotype(NiemTools.niemStereotype);
	}

	public UmlItem[] children()
	{
		if (children == null)
			return null;
		else
			return (UmlItem[]) children.toArray(new UmlItem[children.size()]);
	}

	public String pretty_name()
	{
		return name;
	}
	
	/**
	 *  returns the name
	 */
	public String name() { return _name; }

	public anItemKind kind()
	{
		return kind;
	}

	public UmlItem create_(anItemKind k, String s)
	{
		switch (k.value())
		{
		case anItemKind._aPackage:
			UmlPackage p = new UmlPackage(this,s);
			children.add(p);
			return p;
		case anItemKind._aClassView:
			UmlClassView c = new UmlClassView(this,s);
			children.add(c);
			return c;
		case anItemKind._aClass:
			UmlClass cc = new UmlClass(this,s);
			children.add(cc);
			return cc;
		case anItemKind._aClassInstance:
			UmlClassInstance ci = new UmlClassInstance(this,s);
			children.add(ci);
			return ci;
		case anItemKind._anExtraClassMember:
			UmlExtraClassMember e = new UmlExtraClassMember(this,s);
			children.add(e);
			return e;
		case anItemKind._anAttribute:
			UmlAttribute a = new UmlAttribute(this,s);
			children.add(a);
			return a;	
		case anItemKind._aRelation:
			// UmlCom.trace("Create relation" + s);
			UmlRelation r = new UmlRelation(this,s);
			// UmlCom.trace("Add relation" + s + " to " + this.pretty_name());
			children.add(r);
			return r;
		case anItemKind._aNcRelation:
			UmlNcRelation re = new UmlNcRelation(this,s);
			children.add(re);
			return re;
		default:
			UmlItem item = new UmlItem(this,k,s);
			children.add(item);
			return item;
		}
	}

	public String stereotype() {
		// read_if_needed_();

		return _stereotype;
	}

	public String sKind() {
		return "???";
	}

	public static void start_file(String f, String s, boolean withrefs) throws IOException
	{
		String filename = directory + "/" + f;
		boolean is_frame = (f == "index-withframe");

		fw = new FileWriter(filename + ".html");

		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fw.write("<!-- Documentation produced by the Html generator of Bouml (http://www.bouml.fr) -->\n");
		fw.write((is_frame)
				? "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">\n"
						: "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		fw.write((svg) ? "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:svg=\"http://www.w3.org/2000/svg\">\n"
				: "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		fw.write("\n");
		fw.write("<head>\n");

		if (s == null) {
			fw.write("<title>" + filename + "</title>\n");
			fw.write("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" />\n");
			fw.write("</head>\n");
			if (withrefs) {
				if (! is_frame)
					fw.write("<body bgcolor=\"#ffffff\">\n");
				// ref_indexes();
			}
		}
		else {
			fw.write("<title>"); fw.write(s); fw.write("</title>\n");
			fw.write("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" />\n");
			fw.write("</head>\n");
			fw.write("\n");
			if (! is_frame)
				fw.write("<body bgcolor=\"#ffffff\">\n");
			fw.write("\n");
			fw.write("<div class = \"title\">"); writeq(s); fw.write("</div>\n");
			fw.write("<p></p>\n");
			fw.write("\n");
			fw.write("<!-- ============================================================= -->\n");
			fw.write("\n");
		}

	}

	public static void end_file() throws IOException
	{
		fw.write("</body>\n");
		fw.write("</html>\n");

		fw.close();

	}

	public static void writeq(String s) throws IOException
	{
		if (tag_all)
			fw.write(s);
		else {
			int n = s.length();

			for (int index = 0; index != n; index += 1) {
				if ((s.charAt(index) == '<') &&
						((index + 6) < n) &&
						(s.charAt(index+1) == 'h') &&
						(s.charAt(index+2) == 't') &&
						(s.charAt(index+3) == 'm') &&
						(s.charAt(index+4) == 'l') &&
						(s.charAt(index+5) == '>')) {
					index += 6;

					while (index != n) {
						if ((s.charAt(index) == '<') &&
								((index + 7) < n) &&
								(s.charAt(index+1) == '/') &&
								(s.charAt(index+2) == 'h') &&
								(s.charAt(index+3) == 't') &&
								(s.charAt(index+4) == 'm') &&
								(s.charAt(index+5) == 'l') &&
								(s.charAt(index+6) == '>')) {
							index += 7;
							break;
						}
						else
							fw.write(s.charAt(index));
					}
				}
				else
					writeq(s.charAt(index));
			}
		}
	}

	public static void writeq(char c) throws IOException
	{
		switch (c) {
		case '<':
			fw.write("&lt;");
			break;
		case '>':
			fw.write("&gt;");
			break;
		case '&':
			fw.write("&amp;");
			break;
		case '@':
			fw.write("&#64;");
			break;
		case '\r':
			break;
		case '\n':
			fw.write("<br />");
			break;
		default:
			fw.write(c);
			break;
		}
	}

	public UmlItem parent() {

		return _parent;
	}

	public String propertyValue(String k) {
		//   read_if_needed_();

		if (_dict == null)
			return null;

		return (String) _dict.get(k);
	}

	public String description() {
		// read_if_needed_();

		return _description;
	}

	public void set_Description(String s) throws RuntimeException {
		_description = s;
	}

	@SuppressWarnings("unchecked")
	public void set_PropertyValue(String k, String v) throws RuntimeException {
		//  read_if_needed_();

		//    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCoupleValueCmd, k, v);
		//    UmlCom.check();

		if (_dict == null)
			_dict = new Hashtable();

		_dict.put(k, v);

	}

	public void write() throws IOException {
		if (known) {
			fw.write("<a href=\"");
			if (!flat && (parent() != null) && (parent().kind() == anItemKind.aClass)){
				fw.write("class");
				fw.write(String.valueOf(parent().getIdentifier()));
			}
			else
				fw.write("index");
			fw.write(".html#ref");
			fw.write(String.valueOf(kind().value()));
			fw.write('_');
			fw.write(String.valueOf(getIdentifier()));
			fw.write("\"><b>");
			writeq(pretty_name());
			fw.write("</b></a>");
		}
		else
			writeq(pretty_name());
	}

	public int getIdentifier() {
		//    read_if_needed_();

		return _modeler_id;
	}

	public void set_Stereotype(String s) throws RuntimeException {
		//  UmlCom.send_cmd(identifier_(), OnInstanceCmd.setStereotypeCmd, s);
		//  UmlCom.check();

		if (_defined) _stereotype = s;

	}

	public void applyStereotype() throws RuntimeException {
		//    UmlCom.send_cmd(identifier_(), OnInstanceCmd.applyStereotypeCmd);
		//    UmlCom.check();
		unload(false, false);
	}  

	public void unload(boolean rec, boolean del) {
		_defined = false;
		_stereotype = null;
		_dict = null;
		_description = null;

		/*  if (_children != null) {
		      if (rec)
		        for (int chindex = 0; chindex != _children.length; chindex += 1) {
		  	_children[chindex].unload(true, del);
		  	if (del)
		  	  _all.remove(new Long(_children[chindex].identifier_()));
		      }

		      _children = null; 
		    } */
	}

	ArrayList<UmlItem> children = new ArrayList<UmlItem>();
	UmlItem _parent;
	String name;
	anItemKind kind;
	private boolean _defined= true;
	private String _description;

	private Hashtable _dict;

	protected String _stereotype;

	protected static Vector all;

	static { all = new Vector(); }
	protected boolean known;

	protected static FileWriter fw;

	protected static String directory;

	protected static int nrefs= 0;

	protected static String letters;

	{ letters = new String(); }
	/**
	 * true => use SVG picture rather than PNG
	 */
	protected static boolean flat;

	/**
	 * true => classes and tables are generated in index.html, else an own files are generated
	 */
	protected static boolean svg;

	/**
	 * true => the description is produced without protecting special characters, for instance < is generated as < rather than &lts; to allow to produce html tags unchanged
	 */
	protected static boolean tag;

	/**
	 * true => all is produced without protecting special characters, for instance < is generated as < rather than &lts; to allow to produce html tags unchanged
	 */
	protected static boolean tag_all;

	private int _modeler_id;

	private String _name;

}
