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
public class UmlItem implements Comparable {

	public UmlItem() {
		super();
		set_Stereotype(NiemTools.NIEM_STEREOTYPE_TYPE);
	}

	public UmlItem(long id, String n)
	{ 
		super();
		name = n;
		_name = n;
		known = false;
		set_Stereotype(NiemTools.NIEM_STEREOTYPE_TYPE);
	}

	public UmlItem(UmlItem p, anItemKind k, String n) {
		super();
		_parent = p;
		kind = k;
		name = n;
		_name = n;
		set_Stereotype(NiemTools.NIEM_STEREOTYPE_TYPE);
	}

	public UmlItem[] children()
	{
		if (children == null)
			return null;
		else
			return (UmlItem[]) children.toArray(new UmlItem[children.size()]);
	}

	public void deleteIt() throws RuntimeException {
		    //UmlCom.send_cmd(identifier_(), OnInstanceCmd.deleteCmd);
		    //UmlCom.check();
		    parent().unload(true, false);
		    _defined = false;
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
	
	public boolean defined_() { return _defined; }
	
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
			UmlBaseClass cc = new UmlClass(this,s);
			children.add(cc);
			return cc;
		case anItemKind._aClassInstance:
			UmlBaseClassInstance ci = new UmlClassInstance(this,s);
			children.add(ci);
			return ci;
		case anItemKind._anExtraClassMember:
			UmlExtraClassMember e = new UmlExtraClassMember(this,s);
			children.add(e);
			return e;
		case anItemKind._anAttribute:
			UmlBaseAttribute a = new UmlAttribute(this,s);
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

	public static void sort(Vector v)
	  {
	    sort(v, 0, v.size() - 1);
	  }

	  public static void generate_indexes() throws IOException
	  {
	/*    UmlClass.generate_index();
	    UmlOperation.generate_index();
	    UmlAttribute.generate_index();
	    UmlPackage.generate_index();
	    UmlUseCase.generate_index();
	    UmlActivity.generate_index();
	    UmlState.generate_index();
	    UmlClassDiagram.generate_index();
	    UmlObjectDiagram.generate_index();
	    UmlActivityDiagram.generate_index();
	    UmlStateDiagram.generate_index();
	    UmlUseCaseDiagram.generate_index();
	    UmlSequenceDiagram.generate_index();
	    UmlCollaborationDiagram.generate_index();
	    UmlComponentDiagram.generate_index();
	    UmlDeploymentDiagram.generate_index();*/
	  
	    int n = all.size();
	    char previous;
	    
	    sort(all);
	    
	    previous = 0;
	    for (int i = 0; i != n; i += 1) {
	      UmlItem x = (UmlItem) all.elementAt(i);
	      String s = x.pretty_name();
	      
	      if (s.length() != 0) {
	        char c = x.pretty_name().charAt(0);
	        
	        if ((c >= 'a') && (c <= 'z'))
	  	c += 'A' - 'a';
	        
	        if (c != previous) {
	  	previous = c;
	  	letters += c;
	        }
	      }
	    }
	  
	    previous = 0;
	    for (int i = 0; i != n; i += 1) {
	      UmlItem x = (UmlItem) all.elementAt(i);
	      String s = x.pretty_name();
	      
	      if (s.length() != 0) {
	        char c = x.pretty_name().charAt(0);
	        
	        if ((c >= 'a') && (c <= 'z'))
	  	c += 'A' - 'a';
	        
	        if (c != previous) {
	  	if (previous != 0) {
	  	  fw.write("</table>\n");
	  	  end_file();
	  	}
	  	
	  	previous = c;
	  	
	  	String sn = new Integer(c & 255).toString();
	  	
	  	start_file(new String("index_") + sn, new String("") + c, true);
	  	
	  	fw.write("<table>\n");
	  	fw.write("<tr bgcolor=\"#f0f0f0\"><td align=\"center\"><b>Name</b></td><td align=\"center\"><b>Kind</b></td><td align=\"center\"><b>Description</b></td></tr>\n");
	        }
	        
	        fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
	        x.write("projectFrame");
	        fw.write("</td><td>");
	        fw.write(x.sKind());
	        fw.write("</td><td>");
	        writeq(x.description());
	        fw.write("</td></tr>\n");
	      }
	    }
	  
	    if (previous != 0) {
	      fw.write("</table>\n");
	      end_file();
	    }
	  
	  }
	  
	  private boolean gt(UmlItem other) {
		   String s1 = pretty_name();
		   String s2 = other.pretty_name();
		   int i = s1.compareToIgnoreCase(s2);
		   
		   return ((((i == 0) && (parent() != null) && (other.parent() != null))
		  	  ? parent().pretty_name().compareToIgnoreCase(other.parent().pretty_name())
		  	  : i)
		  	 > 0);
		  }

	@SuppressWarnings("unchecked")
	private static void sort(Vector v, int low, int high)
	  {
	    if (low < high) {
	      int lo = low;
	      int hi = high + 1;
	      UmlItem e = (UmlItem) v.elementAt(low);
	      
	      for (;;) {
	        while ((++lo < hi) && !((UmlItem) v.elementAt(lo)).gt(e))
	  	;
	        while (((UmlItem) v.elementAt(--hi)).gt(e));
	  	;
	        
	        if (lo < hi) {
	  	Object x = v.elementAt(lo);
	  	
	  	v.setElementAt(v.elementAt(hi), lo);
	  	v.setElementAt(x, hi);
	        }
	        else
	  	break;
	      }
	      
	      Object x = v.elementAt(low);
	  	
	      v.setElementAt(v.elementAt(hi), low);
	      v.setElementAt(x, hi);
	      
	      sort(v, low, hi - 1);
	      sort(v, hi + 1, high);
	    }
	  }

	public void write(String target) throws IOException {
	    if (known) {
	      fw.write("<a href=\"index.html#ref");
	      fw.write(String.valueOf(kind().value()));
	      fw.write('_');
	      fw.write(String.valueOf(getIdentifier()));
	      fw.write("\" target = \"");
	      fw.write(target);
	      fw.write("\"><b>");
	      writeq(pretty_name());
	      fw.write("</b></a>");
	    }
	    else
	      writeq(name());
	  }
	
	  @SuppressWarnings("unchecked")
	  public void memo_ref() {
	      all.addElement(this);
	      known = true;
	      
	      UmlItem[] ch = children();
	      
	      for (int i = 0; i != ch.length; i += 1)
	        ch[i].memo_ref();
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

	  public void moveAfter(UmlItem x) throws RuntimeException {
		//    UmlCom.send_cmd(identifier_(), OnInstanceCmd.moveAfterCmd, 
		//                    (x != null) ? x.identifier_() : 0);
		//    UmlCom.check();
		//    parent().reread_children_if_needed_();
		  }
	  
	//  added operations to support sorting

	public void sort() {
		UmlCom.trace("target not allowed, must be a <i>package</i>, any <i>view</i> or a <i>use case</i>");
	}

	public int orderWeight() {
		return 0;
	}

	public int compareTo(Object o) {
		UmlItem e1 = (UmlItem) this;
		UmlItem e2 = (UmlItem) o;
		int w1 = e1.orderWeight();
		int w2 = e2.orderWeight();

		return (w1 != w2)
				? w1 - w2
						: e1.name().compareTo(e2.name());
	}

	public void sortChildren() {
		UmlItem[] v = children();
		int sz = v.length;

		if (sz != 0) {
			// sort in memory
			java.util.Arrays.sort((Object[]) v);

			// update browser
			int u;
			UmlItem previous = null;

			for (u = 0; u != sz; u += 1) {
				v[u].moveAfter(previous);
				previous = v[u];
			}
		}
	}

}
