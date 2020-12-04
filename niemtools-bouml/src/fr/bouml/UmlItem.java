package fr.bouml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.mtgmc.niemtools.NiemUmlClass;



/**
 *  This class is a mother class for all the other Uml* classes, this
 *  allows to generalize their management, declare virtual operations etc ...
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
public abstract class UmlItem extends UmlBaseItem implements Comparable {
  public UmlItem(long id, String n){ super(id, n); known = false; }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "???";
  }

  /**
   *   Set the directory where the files will be generated
   *   initializing 'directory'.
   * 
   *   May be remove already existing html files and css
   */
  public void set_dir(int argc, String[] argv) {
    boolean ask;
    boolean rem;
    boolean replace_css;
    int index0;
    
    if ((argc != 0) && argv[0].equals("-flat")) {
      flat = true;
      index0 = 1;
      argc -= 1;
    }
    else{
      flat = false;
      index0 = 0;
    }
  
    if ((argc != 0) && argv[index0].equals("-svg")) {
      svg = true;
      index0 += 1;
      argc -= 1;
    }
    else
      svg = false;
  
    if ((argc != 0) && argv[index0].equals("-tag")) {
      tag = true;
      index0 += 1;
      argc -= 1;
    }
    else
      tag = false;
  
    if ((argc != 0) && argv[index0].equals("-tag_all")) {
      tag_all = true;
      index0 += 1;
      argc -= 1;
    }
    else
      tag_all = false;
  
    if (argc == 0) {
      directory = UmlBasePackage.getProject().propertyValue("html dir");
      if (directory == null)
        directory = new String("/tmp/") + name() + "_html";
      
      // in java it is very complicated to select
      // a directory through a dialog, and the dialog
      // is very slow and ugly
      JFrame frame = new JFrame();
      JFileChooser fc = new JFileChooser(directory);
      
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setDialogTitle("Directory where the files will be produced");
      
      if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
        throw new RuntimeException("abort");
      
      directory = fc.getSelectedFile().getAbsolutePath();	// !
      
      ask = true;
      rem = false;
      replace_css = true;
    }
    else {
      directory = argv[index0];
      ask = false;
      
      if ((argc >= 2) && (argv[index0 + 1].equals("-del_html"))) {
        rem = true;
        replace_css = ((argc == 3) && argv[index0 + 2].equals("-del_css"));
      }
      else {
        rem = false;
        replace_css = ((argc == 2) && argv[index0 + 1].equals("-del_css"));
      }
    }
    
    File dir = new File(directory);
    int i;
    
    if (dir.exists()) {
      if (dir.isDirectory()) {
        File[] files = dir.listFiles();
  	
        if (ask) {
  	// remove html old files ?
  	for (i = 0; i != files.length; i += 1) {
  	  if (files[i].isFile() &&
  	      (files[i].getName().toLowerCase().endsWith(".html"))) {
  	    if (!rem) {
  	      ConfirmBox msg = new ConfirmBox("Delete already existing html files ?");
  	      
  	      if (!msg.ok())
  		break;
  	      rem = true;
  	    }
  	    files[i].delete();
  	  }
  	}
  
  	// remove old css file ?
  	for (i = 0; i != files.length; i += 1) {
  	  if (files[i].isFile() &&
  	      (files[i].getName().equals("style.css"))) {
  	    ConfirmBox msg = new ConfirmBox("Delete already existing style.css ?");
  	    
  	    replace_css = msg.ok();
  	    break;
  	  }
  	}
        }
        else {
  	if (rem) {
  	  for (i = 0; i != files.length; i += 1) {
  	    if (files[i].isFile() &&
  		(files[i].getName().toLowerCase().endsWith(".html"))) {
  	      files[i].delete();
  	    }
  	  }
  	}
  	
  	if (!replace_css) {
  	  replace_css = true;	// to create it
  	  
  	  for (i = 0; i != files.length; i += 1) {
  	    if (files[i].isFile() &&
  		(files[i].getName().equals("style.css"))) {
  	      // already exist, don't replace
  	      replace_css = false;
  	      break;
  	    }
  	  }
  	}
        }
      }
    }
    else {
      dir.mkdir();
      replace_css = true;	// to create it
    }
  
    if (ask) {
      try {
        UmlBasePackage.getProject().set_PropertyValue("html dir", directory);
      }
      catch (RuntimeException e) {
      }
    }
  
    if (replace_css) {
      try {
        // write css file
        FileWriter fw = new FileWriter(directory + "/style.css");
        
        fw.write("div.title { font-size: 150%; background: #87ceff; text-align: center; font-weight: bold; }\n");
        fw.write("\n");
        fw.write("div.sub { margin-left : 20px; }\n");
        fw.write("div.element { background: #d3d3d3; }\n");
        fw.write("\n");
        fw.write("h1.package { background: #ffe4c4; }\n");
        fw.write("h1.profile { background: #ffe4c4; }\n");
        fw.write("h1.view { background: #98fb98; }\n");
        fw.write("\n");
        fw.write("h2.package { background: #ffe4c4; }\n");
        fw.write("h2.profile { background: #ffe4c4; }\n");
        fw.write("h2.view { background: #98fb98; }\n");
        fw.write("h2.class { background: #87ceff; }\n");
        fw.write("h2.table { background: #87ceff; }\n");
        fw.write("h2.usecase { background: #87ceff; }\n");
        fw.write("h2.state { background: #87ceff; }\n");
        fw.write("h2.activity { background: #87ceff; }\n");
        fw.write("\n");
        fw.write("h3.package { background: #ffe4c4; }\n");
        fw.write("h3.profile { background: #ffe4c4; }\n");
        fw.write("h3.view { background: #98fb98; }\n");
        fw.write("h3.class { background: #87ceff; }\n");
        fw.write("h3.table { background: #87ceff; }\n");
        fw.write("h3.usecase { background: #87ceff; }\n");
        fw.write("h3.state { background: #87ceff; }\n");
        fw.write("h3.stateregion { background: #87ceff; }\n");
        fw.write("h3.activity { background: #87ceff; }\n");
        fw.write("\n");
        fw.write("h4.package { background: #ffe4c4; }\n");
        fw.write("h4.profile { background: #ffe4c4; }\n");
        fw.write("h4.view { background: #98fb98; }\n");
        fw.write("h4.class { background: #87ceff; }\n");
        fw.write("h4.table { background: #87ceff; }\n");
        fw.write("h4.usecase { background: #87ceff; }\n");
        fw.write("h4.state { background: #87ceff; }\n");
        fw.write("h4.stateregion { background: #87ceff; }\n");
        fw.write("h4.activity { background: #87ceff; }\n");
        fw.close();
      }
      catch (java.io.IOException e) {
      }
    }
  }

  @SuppressWarnings("unchecked")
public void memo_ref() {
    all.addElement(this);
    known = true;
    
    UmlItem[] ch = children();
    
    for (int i = 0; i != ch.length; i += 1)
      ch[i].memo_ref();
  }

  public void define() throws IOException {
    fw.write("<a name=\"ref");
    fw.write(String.valueOf(kind().value()));
    fw.write('_');
    fw.write(String.valueOf(getIdentifier()));
    fw.write("\"></a>\n");
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
        ref_indexes();
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

  @SuppressWarnings("deprecation")
public static void ref_indexes() throws IOException
  {
    fw.write("<hr />\n<p><a href=\"index.html\" target = \"projectFrame\"><b> -Top- </b></a>");
    
    UmlClass.ref_index();
    UmlOperation.ref_index();
    UmlAttribute.ref_index();
    UmlPackage.ref_index();
    UmlUseCase.ref_index();
    UmlActivity.ref_index();
    UmlVariable.ref_index();
    UmlState.ref_index();
    UmlClassDiagram.ref_index();
    UmlClassCompositeDiagram.ref_index();
    UmlObjectDiagram.ref_index();
    UmlObjectCompositeDiagram.ref_index();
    UmlActivityDiagram.ref_index();
    UmlStateDiagram.ref_index();
    UmlUseCaseDiagram.ref_index();
    UmlSequenceDiagram.ref_index();
    UmlCollaborationDiagram.ref_index();
    UmlComponentDiagram.ref_index();
    UmlDeploymentDiagram.ref_index();
    fw.write("</p>\n<p>\n</p>\n<p>");
    
    for (int i = 0; i != letters.length(); i += 1) {
      fw.write("<a href=\"index_");
      fw.write(new Integer(letters.charAt(i) & 255).toString());
      fw.write(".html\" target = \"projectFrame\"><b> ");
      writeq(letters.charAt(i));
      fw.write(" </b></a>");
    }
  
    fw.write("</p>\n");
  }

  public static void generate_indexes() throws IOException
  {		
	NiemUmlClass.hideReferenceModel();
		
    UmlClass.generate_index();
    UmlOperation.generate_index();
    UmlAttribute.generate_index();
    UmlPackage.generate_index();
    UmlUseCase.generate_index();
    UmlActivity.generate_index();
    UmlVariable.generate_index();
    UmlState.generate_index();
    UmlClassDiagram.generate_index();
    UmlClassCompositeDiagram.generate_index();
    UmlObjectDiagram.generate_index();
    UmlObjectCompositeDiagram.generate_index();
    UmlActivityDiagram.generate_index();
    UmlStateDiagram.generate_index();
    UmlUseCaseDiagram.generate_index();
    UmlSequenceDiagram.generate_index();
    UmlCollaborationDiagram.generate_index();
    UmlComponentDiagram.generate_index();
    UmlDeploymentDiagram.generate_index();
  
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
  	
  	@SuppressWarnings("deprecation")
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

  public static void frame() throws IOException
  {
    start_file("index-withframe", null, false);
  
    fw.write("<frameset cols=\"20%,80%\">\n");
    fw.write("  <noframes>\n");
    fw.write("    <body>\n");
    fw.write("      <h2>Frame Alert</h2>\n");
    fw.write("      <p>This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.</p>\n");
    fw.write("      <p>Link to <a href=\"index.html\">Non-frame version.</a></p>\n");
    fw.write("    </body>\n");
    fw.write("  </noframes>\n");
    fw.write("  <frame src=\"classes_list.html\" name=\"classesListFrame\" />\n");
    fw.write("  <frameset rows=\"150,*\">\n");
    fw.write("    <frame src=\"navig.html\" name=\"navigFrame\" />\n");
    fw.write("    <frame src=\"index.html\" name=\"projectFrame\" />\n");
    fw.write("  </frameset>\n");
    fw.write("</frameset>\n");
   
    fw.write("</html>");
    fw.close();
  
    UmlCom.trace("document with frame produced in <i>"
  	       + directory + "/index-withframe.html");
  
    UmlCom.trace("document without frame produced in <i>"
  	       + directory + "/index.html");
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public abstract void html(String pfix, int rank, int level) throws IOException ;

  public boolean chapterp() {
    return false;
  }

  public void html(String pfix, int rank, String what, int level, String kind) throws IOException {
	  
	if (!known)
		return;
		
    define();
   
    chapter(what, pfix, rank, kind, level);
  
    write_description();
  
    write_properties();
    
    UmlItem[] ch = children();
    
    if (ch.length != 0) {
      String spfix = (rank == 0)
        ? ""
        : (pfix + String.valueOf(rank) + ".");
      
      level += 1;
      rank = 1;
      fw.write("<div class=\"sub\">\n");    
      for (int i = 0; i != ch.length; i += 1) {
        ch[i].html(spfix, rank, level);
        if (ch[i].chapterp())
  	rank += 1;
      }
      fw.write("</div>\n");
    }
  }

  public void html(String what, UmlDiagram diagram) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">");
    fw.write(what);
    fw.write(" <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
    
    write_description();
    
    write_dependencies();
  
    if (diagram != null) {
      fw.write("<p>Diagram : ");
      diagram.write();
      fw.write("</p>\n");
    }
  
    write_properties();
  }

  public void write_children(String pfix, int rank, int level) throws IOException {
    UmlItem[] ch = children();
      
    if (ch.length != 0) {
      String spfix;
      int srank = 1;
      
      if (rank == 0)
        spfix = "";
      else {
        spfix = pfix + String.valueOf(rank) + ".";
        fw.write("<div class=\"sub\">\n");
      }
      
      level += 1; 
      
      for (int i = 0; i != ch.length; i += 1) {
        ch[i].html(spfix, srank, level);
        if (ch[i].chapterp())
  	srank += 1;
      }
      
      if (rank != 0)
        fw.write("</div>\n");
    }
  }

  public void write_dependencies() throws IOException {
    UmlItem[] ch = children();
    
    for (int i = 0; i != ch.length; i += 1) {
      if ((ch[i].kind() == anItemKind.aNcRelation) &&
  	(((UmlNcRelation) ch[i]).relationKind() == aRelationKind.aDependency)) {
        fw.write("<p>Depends on ");
        ((UmlNcRelation) ch[i]).target().write();
        fw.write("</p>");
      }
    }
  }

  public void write_description() throws IOException {
    String d = description();
    
    if (d.length() != 0) {
      fw.write("<p>");
      if (tag)
        fw.write(d);
      else
        writeq(d);
      fw.write("<br /></p>\n");
    }
  }

  public void write_properties() throws IOException {
    if (!stereotype().equals("")) {
      fw.write("<p>Stereotype: ");
      writeq(stereotype());
      fw.write("</p>\n");
    }
      
    Hashtable d = properties();
    
    if (! d.isEmpty()) {
      fw.write("<p>Properties:</p><ul>\n");
      
      Set set = d.entrySet();
      Iterator i = set.iterator();
      
      while(i.hasNext()){
        Map.Entry e = (Map.Entry) i.next();
  
        fw.write("<li>");
        writeq((String) e.getKey());
        fw.write(":<br /><div class=\"sub\">");
        writeq((String) e.getValue());
        fw.write("</div></li>\n");
      }
      
      fw.write("</ul>\n");
    }
  }

  public void chapter(String k, String pfix, int rank, String kind, int level) throws IOException {
    if (rank != 0) {
      if (level > 4)
        level = 4;
      
      fw.write("<h");
      fw.write(String.valueOf(level));
      if (!kind.equals("")) {
        fw.write(" class =\"");
        fw.write(kind);
        fw.write("\">");
      }
      else
        fw.write(">");
  
      fw.write(pfix);
      fw.write(String.valueOf(rank));
      fw.write(' ');
      fw.write(k);
      fw.write(' ');
      writeq(name());
      fw.write("</h");
      fw.write(String.valueOf(level));
      fw.write(">\n");
    }
  }

  /**
   * bypass the comments at the beginning of the form
   */
  public int bypass_comment(String s) {
    int index = 0;
    int n = s.length();
    
    while (index != n) {
      if (Character.isWhitespace(s.charAt(index)))
        index += 1;
      else if ((s.charAt(index) == '/') && ((index + 1) != n)) {
        if (s.charAt(index + 1) == '/') {
  	do 
  	  index += 1;
  	while ((index != n) &&
  	       (s.charAt(index) != '\n') && (s.charAt(index) != '\r'));
        }
        else if (s.charAt(index + 1) == '*') {
  	int index2 = s.indexOf("*/", index + 2);
  	
  	if (index2 != -1)
  	  index = index2 + 2;
  	else
  	  return index;
        }
        else
  	return index;
      }
      else if (s.charAt(index) == '#') {
        do {
  	if ((++index == n) ||
  	    ((s.charAt(index) == '\\') && (++index == n)))
  	  return index;
        } while ((s.charAt(index) != '\n') && (s.charAt(index) != '\r'));
      }
      else
        return index;
    }
  
    return index;
  
  }

  public int manage_alias(String s, int index) throws IOException {
    // s[index] is '@'
    int index2;
    
    if ((s.charAt(index + 1) == '{') && ((index2 = s.indexOf('}', index + 2)) != -1)) {
      String key = s.substring(index + 2, index2);
      String value;
      UmlItem node = this;
  
      do {
        if ((value = node.propertyValue(key)) != null)
  	break;
        node = node.parent();
      } while (node != null);
      
      if (node != null)
        // find, insert the value
        writeq(value);
      else {
        // not find, insert the key
        fw.write("@{");
        writeq(key);
        fw.write("}");
      }
  
      // bypass the key
      return index2 + 1;
    }
    else
      // bypass '@'
      return index + 1;
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

  public static void write(UmlTypeSpec t, aLanguage lang) throws IOException
  {
    if (t.type != null)
      t.type.write();
    else if (lang == aLanguage.cppLanguage)
      writeq(CppSettings.type(t.toString()));
    else if (lang == aLanguage.javaLanguage)
      writeq(JavaSettings.type(t.toString()));
    else
      writeq(t.toString());
  
  }

  public static void write(UmlTypeSpec t) throws IOException
  {
    if (t.type != null)
      t.type.write();
    else
      writeq(t.toString());
  }

  public static void write(aVisibility v, aLanguage lang) throws IOException
  {
    switch (v.value()) {
    case aVisibility._PublicVisibility:
      fw.write("public");
      break;
    case aVisibility._ProtectedVisibility:
      fw.write("protected");
      break;
    case aVisibility._PrivateVisibility:
      fw.write("private");
      break;
    case aVisibility._PackageVisibility:
      if (lang == aLanguage.cppLanguage)
        fw.write("public");
      else if (lang == aLanguage.javaLanguage)
        fw.write("package");
      break;
    default:
      fw.write("???");
    }
  }

  public static void write(aVisibility v) throws IOException
  {
    switch (v.value()) {
    case aVisibility._PublicVisibility:
      fw.write("+ ");
      break;
    case aVisibility._ProtectedVisibility:
      fw.write("# ");
      break;
    case aVisibility._PrivateVisibility:
      fw.write("- ");
      break;
    default:
      // aVisibility._PackageVisibility:
      fw.write("~ ");
    }
  }

  public static void write(aDirection d) throws IOException
  {
    switch (d.value()) {
    case aDirection._InputOutputDirection:
      fw.write("input output");
      break;
    case aDirection._InputDirection:
      fw.write("input");
      break;
    case aDirection._OutputDirection:
      fw.write("output");
      break;
    case aDirection._ReturnDirection:
      fw.write("return");
      break;
    default:
      fw.write("???");
    }
  }

  public static void write(aParameterEffectKind d) throws IOException
  {
    switch (d.value()) {
    case aParameterEffectKind._noEffect:
      fw.write("none");
      break;
    case aParameterEffectKind._createEffect:
      fw.write("create");
      break;
    case aParameterEffectKind._readEffect:
      fw.write("read");
      break;
    case aParameterEffectKind._updateEffect:
      fw.write("update");
      break;
    case aParameterEffectKind._deleteEffect:
      fw.write("delete");
      break;
    default:
      fw.write("???");
    }
  }

  public static void write(anOrdering d) throws IOException
  {
    switch (d.value()) {
    case anOrdering._unordered:
      fw.write("unordered");
      break;
    case anOrdering._ordered:
      fw.write("ordered");
      break;
    case anOrdering._lifo:
      fw.write("lifo");
      break;
    case anOrdering._fifo:
      fw.write("fifo");
      break;
    default:
      fw.write("???");
    }
  }

  public static void generate_index(Vector v, String k, String r) throws IOException
  {
    int n = v.size();
    
    if (n != 0) {
      sort(v);
      
      start_file(r, k + " Index", true);
      
      fw.write("<table>\n");
      
      for (int i = 0; i != n; i += 1) {
        UmlItem x = (UmlItem) v.elementAt(i);
        
        fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
        x.write("projectFrame");
        fw.write("</td><td>");
        writeq(x.stereotype());
        fw.write("</td><td>");
        writeq(x.description());
        fw.write("</td></tr>\n");
      }
      fw.write("</table>\n");
      
      end_file();
    }
  }

  public static void sort(Vector v)
  {
    sort(v, 0, v.size() - 1);
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

  private boolean gt(UmlItem other) {
   String s1 = pretty_name();
   String s2 = other.pretty_name();
   int i = s1.compareToIgnoreCase(s2);
   
   return ((((i == 0) && (parent() != null) && (other.parent() != null))
  	  ? parent().pretty_name().compareToIgnoreCase(other.parent().pretty_name())
  	  : i)
  	 > 0);
  }

  public String pretty_name() {
    return name().length() == 0 ? sKind() : name();
  }

  public static Vector all;

  static { all = new Vector(); }
  public boolean known;

  protected static FileWriter fw;

  public static String directory;

  protected static int nrefs= 0;

  protected static String letters;

{ letters = new String(); }
  /**
   * true => use SVG picture rather than PNG
   */
  public static boolean flat;

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

};
