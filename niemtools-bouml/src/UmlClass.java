
import java.io.*;
import java.util.*;

/**
 *  This class manages 'classes', notes that the class 'ClassItem'
 *  is a mother class of the class's children.
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlClass extends UmlBaseClass {
  public UmlClass(long id, String n){ super(id, n); inherited_opers = null; }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return (stereotype().equals("stereotype"))
      ? "stereotype" : "class";
  }

  /**
   * set he html ref
   * set the classes list
   * set the operations list including inherited ones
   * set the daugther inheritance classes list
   */
  @SuppressWarnings("unchecked")
public void memo_ref() {
    classes.addElement(this);
    super.memo_ref();
    
    UmlItem[] ch = children();
    
    if (inherited_opers == null)
      add_inherited_opers(null);
  	
    for (int i = 0; i != ch.length; i += 1) {
      if (ch[i].kind() == anItemKind.aRelation) {
        UmlRelation rel = (UmlRelation) ch[i];
        aRelationKind k = rel.relationKind();
        
        if ((k == aRelationKind.aGeneralisation) ||
  	  (k == aRelationKind.aRealization))
  	rel.roleType().subClasses.addElement(this);
      }
    }
  
    unload(true, false);
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    String s = stereotype();
    
    if (flat) {
      define();
      if (s.equals("stereotype"))
        chapter("Stereotype", pfix, rank, "stereotype", level);
      else if (s.equals("metaclass"))
        chapter("Metaclass", pfix, rank, "metaclass", level);
      else if (s.equals("table"))
        chapter("Table", pfix, rank, "table", level);
      else
        chapter("Class", pfix, rank, "class", level);
      gen_html(pfix, rank, level);
      unload(false, false);
    }
    else {
      if (s.equals("stereotype"))
        fw.write("<table><tr><td><div class=\"element\">Stereotype <b>");
      else if (s.equals("metaclass"))
        fw.write("<table><tr><td><div class=\"element\">Metaclass <b>");
      else if (s.equals("table"))
        fw.write("<table><tr><td><div class=\"element\">Table <b>");
      else
        fw.write("<table><tr><td><div class=\"element\">Class <b>");
      write();
      fw.write("</b></div></td></tr></table>\n");
    }
  }

  /**
   * generate the html definition in an own file
   */
  public void html() throws IOException {
	  
	if (!known)
		return;
	
    UmlCom.message(name());
    
    if (stereotype().equals("stereotype"))
      start_file("class" + String.valueOf(getIdentifier()), "Stereotype " + name(), true);
    else if (stereotype().equals("metaclass"))
      start_file("class" + String.valueOf(getIdentifier()), "Metaclass " + name(), true);
    else if (stereotype().equals("table"))
      start_file("class" + String.valueOf(getIdentifier()), "Table " + name(), true);
    else
      start_file("class" + String.valueOf(getIdentifier()), "Class " + name(), true);
    define();
    gen_html(null, 0, 0);
    end_file();
  
    unload(false, false);
  }

  /**
   * generate the html definition except header/chapter in the current file
   */
  public final void gen_html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
    
    String s;
   
    s = description();
    
    if (isActive())
      fw.write("<p>Active class</p>\n");
    
    if (s.length() != 0) {
      fw.write("<p>");
      if (javaDecl().length() != 0)
        gen_java_decl(s, true);
      else if (phpDecl().length() != 0)
        gen_php_decl(s, true);
      else if (pythonDecl().length() != 0)
        gen_python_decl(s, true);
      else 
        gen_cpp_decl(s, true);
      fw.write("<br /></p>\n");
    }
    
    if (!stereotype().equals("table") &&
        ((cppDecl().length() != 0) || 
         (javaDecl().length() != 0) || 
         (phpDecl().length() != 0) || 
         (pythonDecl().length() != 0))) {
      fw.write("<p>Declaration :</p><ul>\n");
      
      s = cppDecl();
      
      if (s.length() != 0) {
        fw.write("<li>C++ : ");
        gen_cpp_decl(s, false);
        fw.write("</li>");
      }
      
      s = javaDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Java : ");
        gen_java_decl(s, false);
        fw.write("</li>");
      }
      
      s = phpDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Php : ");
        gen_php_decl(s, false);
        fw.write("</li>");
      }
      
      s = pythonDecl();
      
      if (s.length() != 0) {
        fw.write("<li>Python : ");
        gen_python_decl(s, false);
        fw.write("</li>");
      }
      
      fw.write("</ul>");
    }
  
    if (subClasses.size() != 0) {
      sort(subClasses);
      fw.write("<p>Directly inherited by : ");
      
      for (int i = 0; i != subClasses.size(); i += 1) {
        ((UmlItem) subClasses.elementAt(i)).write();
        fw.write(' ');
      }
      fw.write("</p>\n");    
    }
    
    write_dependencies();
    
    annotation_constraint();
    
    boolean p = false;
    UmlItem x;
    
    if ((x = associatedArtifact()) != null) {
      p = true;
      fw.write("<p>Artifact : ");
      x.write();
    }
  
    UmlComponent[] comps = associatedComponents();
    
    if (comps.length != 0) {
      if (p) 
        fw.write(", Component(s) :");
      else {
        p = true;
        fw.write("<p>Component(s) :");
      }
      
      for (int i = 0; i != comps.length; i += 1) {
        fw.write(' ');
        ((UmlItem) comps[i]).write();
      }
    }
  
    if ((x = associatedDiagram()) != null) {
      if (p) 
        fw.write(", Diagram : ");
      else {
        p = true;
        fw.write("<p>Diagram : ");
      }
      x.write();
    }
  
    if (p)
      fw.write("</p>\n");
  
    if (parent().kind() == anItemKind.aClass) {
      fw.write("<p>nested in ");
      parent().write();
      fw.write("</p>\n");
    }
  
    write_properties();
    
    //
  
    UmlItem[] ch = children();
    
    if (ch.length != 0) {
      if (stereotype().equals("enum_pattern")) {
        p = false;
        
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.anAttribute) {
  	  if (!p) {
  	    p = true;
  	    fw.write("<div class=\"sub\">\n<p>Items :</p><ul>\n");
  	  }
  	  fw.write("<li>");
  	  writeq(ch[i].name());
  	  fw.write("</li>\n");
  	}
        }
        if (p)
  	fw.write("</ul>\n</div>\n");
      }
      else {
        fw.write("<div class=\"sub\">\n");
        
        if (stereotype().equals("enum")) {
  	int i;
  	
  	p = false;
  	
  	for (i = 0; i != ch.length; i += 1) {
  	  if ((ch[i].kind() == anItemKind.anAttribute) &&
  	      !ch[i].stereotype().equals("attribute")) {
  	    if (!p) {
  	      p = true;
  	      fw.write("<p>Items :</p><ul>\n");
  	    }
  	    fw.write("<li>");
  	    writeq(ch[i].name());
  	    fw.write("</li>\n");
  	  }
  	}
  	if (p)
  	  fw.write("</ul>\n");
  	
  	s = "";
  	
  	for (i = 0; i != ch.length; i += 1)
  	  if ((ch[i].kind() != anItemKind.anAttribute) ||
  	      ch[i].stereotype().equals("attribute"))
  	    ch[i].html(s, 0, 0);
        }
        else if (flat)
  	write_children(pfix, rank, level);
        else {
  	// non flat
  	s = "";
  	
  	for (int i = 0; i != ch.length; i += 1)
  	  ch[i].html(s, 0, 0);
        }
        
        fw.write("</div>\n");
      }
    }
  
    sort(inherited_opers);
    boolean already = false;
    
    for (int i = 0; i != inherited_opers.size(); i += 1) {
      if (already)
        fw.write(", ");
      else {
        already = true;
        fw.write("<p>All public operations : ");
      }
      ((UmlItem) inherited_opers.elementAt(i)).write();
    }
    if (already)
      fw.write("</p>\n");
  }

  /**
   * write a html ref to the class
   */
  public void write() throws IOException {
    if (!known)
      writeq(name());
    else {
      if (flat)
        fw.write("<a href=\"index");
      else {
        fw.write("<a href=\"class");
        fw.write(String.valueOf(getIdentifier()));
      }
      fw.write(".html#ref");
      fw.write(String.valueOf(kind().value()));
      fw.write('_');
      fw.write(String.valueOf(getIdentifier()));
      fw.write("\"><b>");
      writeq(name());
      fw.write("</b></a>");
    }
  }

  /**
   * write a html ref to the class going to an other html file
   */
  public void write(String target) throws IOException {
    if (known) {
      if (flat)
        fw.write("<a href=\"index");
      else {
        fw.write("<a href=\"class");
        fw.write(String.valueOf(getIdentifier()));
      }
      fw.write(".html#ref");
      fw.write(String.valueOf(kind().value()));
      fw.write('_');
      fw.write(String.valueOf(getIdentifier()));
      fw.write("\" target = \"");
      fw.write(target);
      fw.write("\"><b>");
      writeq(name());
      fw.write("</b></a>");
    }
    else
      writeq(name());
  }

  public static void ref_index() throws IOException
  {
    if (!classes.isEmpty())
      fw.write("<a href=\"classes.html\" target = \"projectFrame\"><b> -Classes- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    generate_index(classes, "Classes", "classes");
  
    int n = classes.size();
    
    start_file("classes_list", "Classes", false);
    
    fw.write("<table border=\"0\" width=\"100%\">\n<tr>\n<td nowrap=\"nowrap\">");
    
    for (int i = 0; i != n; i += 1) {
    	UmlItem item = (UmlItem)classes.elementAt(i);
    	if (item.known)
    	{
    		((UmlItem) classes.elementAt(i)).write("projectFrame");
    		fw.write("<br />\n");
    	}
    }
    
    fw.write("</td>\n</tr>\n</table>\n");
    end_file();
  
  }

@SuppressWarnings("unchecked")
private void add_inherited_opers(Vector ops) {
    if (inherited_opers == null) {
      UmlItem[] ch = children();
      
      inherited_opers = new Vector();
  	
      for (int i = 0; i != ch.length; i += 1) {
        switch (ch[i].kind().value()) {
        case anItemKind._aRelation:
  	{
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if ((k == aRelationKind.aGeneralisation) ||
  	      (k == aRelationKind.aRealization))
  	    rel.roleType().add_inherited_opers(inherited_opers);
  	}
  	break;
        case anItemKind._anOperation:
  	{
  	  UmlOperation op = (UmlOperation) ch[i];
  	  
  	  if ((op.visibility() == aVisibility.PublicVisibility) &&
  	      (op.name().charAt(0) != '~') &&
  	      !op.name().equals(name()))
  	    inherited_opers.addElement(op);
  	}
        default:
  	break;
        }
      }
    }
  
    if (ops != null)
      for (int i = 0; i != inherited_opers.size(); i += 1)
        if (! ops.contains(inherited_opers.elementAt(i)))
  	ops.addElement(inherited_opers.elementAt(i));
  
    unload(true, false);
  }

protected Vector inherited_opers;

protected static Vector classes;

  static { classes = new Vector(); }
  /**
   * produce the definition in C++
   */
  private void gen_cpp_decl(String s, boolean descr) throws IOException {
    int n = s.length();
    int index = (descr) ? 0 : bypass_comment(s);
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${type}", index)) {
        index += 7;
        boolean find = false;
  
        if (baseType().type != null) {
  	UmlClass mother = baseType().type;
  	UmlItem[] ch = children();
  	
  	for (int i = 0; i != ch.length; i += 1) {
  	  if (ch[i].kind() == anItemKind.aRelation) {
  	    UmlRelation rel = (UmlRelation) ch[i];
  	    aRelationKind k = rel.relationKind();
  	    
  	    if (((k == aRelationKind.aGeneralisation) ||
  		 (k == aRelationKind.aRealization)) &&
  		(rel.roleType() == mother)) {
  	      rel.roleType().write();
  	      generate(actuals(), rel.roleType(), true);
  	      find = true;
  	      break;
  	    }
  	  }
  	}
        }
        if (! find)
  	write(baseType(), aLanguage.cppLanguage);
      }
      else if (s.startsWith("${template}", index)) {
        index += 11;
        generate(formals());
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
      }
      else if (s.startsWith("${inherit}", index)) {
        index += 10;
  
        UmlItem[] ch = children();
        String sep = " : ";
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if (((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.cppDecl().length() != 0)) {
  	    fw.write(sep);
  	    write((cppVisibility() == aVisibility.DefaultVisibility)
  		  ? visibility() : cppVisibility(),
  		  aLanguage.cppLanguage);
  	    fw.write((rel.cppVirtualInheritance()) ? " virtual " : " ");
  	    rel.roleType().write();
  	    generate(actuals(), rel.roleType(), true);
  	    sep = ", ";
  	  }
  	}
        }
      }
      else if (s.charAt(index) == '{') {
        if (descr)
  	fw.write(s.charAt(index++));
        else
  	break;
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '\n') {
        if (descr) {
  	if (tag || tag_all || unchanged)
  	  fw.write('\n');
  	else
  	  fw.write("<br />");
  	index += 1;
        }
        else {
  	fw.write(' ');
  	
  	do
  	  index += 1;
  	while ((index != n) && Character.isWhitespace(s.charAt(index)));
        }
      }
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else if (descr) {
        if (tag || tag_all)
  	fw.write(s.charAt(index++));
        else if (unchanged) {
  	if (s.startsWith("</html>", index)) {
  	  unchanged = false;
  	  index += 7;
  	}
  	else
  	  fw.write(s.charAt(index++));
        }
        else if (s.startsWith("<html>", index)) {
  	unchanged = true;
  	index += 6;
        }
        else
  	fw.write(s.charAt(index++));
      }
      else
        writeq(s.charAt(index++));
    }
  
  }

  /**
   * produce the definition in Java
   */
  private void gen_java_decl(String s, boolean descr) throws IOException {
    int n = s.length();
    int index = bypass_comment(s);
    UmlRelation extend = null;
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${public}", index)) {
        index += 9;
        if (isJavaPublic())
  	fw.write("public ");
      }
      else if (s.startsWith("${visibility}", index)) {
        index += 13;
        write(visibility(), aLanguage.javaLanguage);
        fw.write(' ');
      }
      else if (s.startsWith("${final}", index)) {
        index += 8;
        if (isJavaFinal())
  	fw.write("final ");
      }
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isJavaStatic() && (parent().kind() == anItemKind.aClass))
  	fw.write("static ");
      }
      else if (s.startsWith("${abstract}", index)) {
        index += 11;
        if (isAbstract())
  	fw.write("abstract ");
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
        generics();
      }
      else if (s.startsWith("${extends}", index)) {
        index += 10;
  
        UmlItem[] ch = children();
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if (((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.javaDecl().length() != 0) &&
  	      (JavaSettings.classStereotype(stereotype()).equals("interface") ||
  	       !JavaSettings.classStereotype(rel.roleType().stereotype()).equals("interface"))) {
  	    extend = rel;
  	    fw.write(" extends ");
  	    rel.roleType().write();
  	    generate(actuals(), rel.roleType(), false);
  	    break;
  	  }
  	}
        }
      }
      else if (s.startsWith("${implements}", index)) {
        index += 13;
  
        UmlItem[] ch = children();
        String sep = " implements ";
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if ((rel != extend) &&
  	      ((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.javaDecl().length() != 0)) {
  	    fw.write(sep);
  	    sep = ", ";
  	    rel.roleType().write();
  	    generate(actuals(), rel.roleType(), false);
  	  }
  	}
        }
      }
      else if (s.startsWith("${@}", index)) {
        index += 4;
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '\n') {
        if (descr) {
  	if (tag || tag_all || unchanged)
  	  fw.write('\n');
  	else
  	  fw.write("<br />");
  	index += 1;
        }
        else {
  	fw.write(' ');
  	
  	do
  	  index += 1;
  	while ((index != n) && Character.isWhitespace(s.charAt(index)));
        }
      }
      else if ((s.charAt(index) == '{') || (s.charAt(index) == ';')) {
        if (descr)
  	fw.write(s.charAt(index++));
        else
  	break;
      }
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else if (descr) {
        if (tag || tag_all)
  	fw.write(s.charAt(index++));
        else if (unchanged) {
  	if (s.startsWith("</html>", index)) {
  	  unchanged = false;
  	  index += 7;
  	}
  	else
  	  fw.write(s.charAt(index++));
        }
        else if (s.startsWith("<html>", index)) {
  	unchanged = true;
  	index += 6;
        }
        else
  	fw.write(s.charAt(index++));
      }
      else
        writeq(s.charAt(index++));
    }
  }

  /**
   * produce the definition in Php
   */
  private void gen_php_decl(String s, boolean descr) throws IOException {
    String st = PhpSettings.classStereotype(stereotype());
    
    if (st.equals("ignored"))
      return;
      
    int n = s.length();
    int index = bypass_comment(s);
    UmlRelation extend = null;
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${visibility}", index)) {
        index += 13;
        write(visibility(), aLanguage.phpLanguage);
        fw.write(' ');
      }
      else if (s.startsWith("${final}", index)) {
        index += 8;
        if (isPhpFinal())
  	fw.write("final ");
      }
      else if (s.startsWith("${abstract}", index)) {
        index += 11;
        if (isAbstract())
  	fw.write("abstract ");
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
        generics();
      }
      else if (s.startsWith("${extends}", index)) {
        index += 10;
  
        UmlItem[] ch = children();
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if (((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.phpDecl().length() != 0) &&
  	      (st.equals("interface") ||
  	       !PhpSettings.classStereotype(rel.roleType().stereotype()).equals("interface"))) {
  	    extend = rel;
  	    fw.write(" extends ");
  	    rel.roleType().write();
  	    break;
  	  }
  	}
        }
      }
      else if (s.startsWith("${implements}", index)) {
        index += 13;
  
        UmlItem[] ch = children();
        String sep = " implements ";
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if ((rel != extend) &&
  	      ((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.phpDecl().length() != 0)) {
  	    fw.write(sep);
  	    sep = ", ";
  	    rel.roleType().write();
  	  }
  	}
        }
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '\n') {
        if (descr) {
  	if (tag || tag_all || unchanged)
  	  fw.write('\n');
  	else
  	  fw.write("<br />");
  	index += 1;
        }
        else {
  	fw.write(' ');
  	
  	do
  	  index += 1;
  	while ((index != n) && Character.isWhitespace(s.charAt(index)));
        }
      }
      else if ((s.charAt(index) == '{') || (s.charAt(index) == ';')) {
        if (descr)
  	fw.write(s.charAt(index++));
        else
  	break;
      }
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else if (descr) {
        if (tag || tag_all)
  	fw.write(s.charAt(index++));
        else if (unchanged) {
  	if (s.startsWith("</html>", index)) {
  	  unchanged = false;
  	  index += 7;
  	}
  	else
  	  fw.write(s.charAt(index++));
        }
        else if (s.startsWith("<html>", index)) {
  	unchanged = true;
  	index += 6;
        }
        else
  	fw.write(s.charAt(index++));
      }
      else
        writeq(s.charAt(index++));
    }
  }

  /**
   * produce the definition in Python
   */
  private void gen_python_decl(String s, boolean descr) throws IOException {
    String st = PythonSettings.classStereotype(stereotype());
    
    if (st.equals("ignored"))
      return;
      
    int n = s.length();
    int index = bypass_comment(s);
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${docstring}", index))
        index += 12;
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
      }
      else if (s.startsWith("${extends}", index)) {
        index += 10;
  
        UmlItem[] ch = children();
        boolean inh = false;
  
        for (int i = 0; i != ch.length; i += 1) {
  	if (ch[i].kind() == anItemKind.aRelation) {
  	  UmlRelation rel = (UmlRelation) ch[i];
  	  aRelationKind k = rel.relationKind();
  	  
  	  if (((k == aRelationKind.aGeneralisation) ||
  	       (k == aRelationKind.aRealization)) &&
  	      (rel.pythonDecl().length() != 0)) {
  	    if (inh)
  	      fw.write(", ");
  	    else  {
  	      inh = true;
  	      fw.write('(');
  	    }
  	    rel.roleType().write();
  	  }
  	}
        }
        
        if (inh)
  	fw.write(')');
        else if (isPython_2_2())
  	fw.write("(object)");
        break;
      }
      else if (!descr &&
  	     ((s.charAt(index) == '\r') ||
  	      (s.charAt(index) == '\n') ||
  	      (s.charAt(index) == ':')))
        break;
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else
        writeq(s.charAt(index++));
    }
  }

  private void generate(UmlActualParameter[] a, UmlClass mother, boolean cpp) throws IOException {
    int i;
    
    // search first actual of mother
    for (i = 0; ; i += 1) {
      if (i == a.length)
        return;
      if (a[i].superClass() == mother)
        break;
    }
      
    String sep = "<";
    aLanguage lang = (cpp) ? aLanguage.cppLanguage : aLanguage.javaLanguage;
    
    do {
      writeq(sep);
      write(a[i].value(), lang);
      sep = ", ";
    }
    while ((++i != a.length) && (a[i].superClass() == mother));
  
    writeq('>');
  }

  private void generate(UmlFormalParameter[] f) throws IOException {
    if (f.length != 0) {
      String sep = "template<";
      int i;
      
      for (i = 0; i != f.length; i += 1) {
        writeq(sep);
        writeq(f[i].type());
        fw.write(' ');
        writeq(f[i].name());
        sep = ", ";
      }
      
      writeq("> ");
    }
    else if (name().indexOf("<") != -1)
      fw.write("template<> ");
  }

  private void generics() throws IOException {
    UmlFormalParameter[] f = formals();
    
    if (f.length != 0) {
      String sep = "<";
      int i;
      
      for (i = 0; i != f.length; i += 1) {
        writeq(sep);
        sep = ", ";
        writeq(f[i].name());
        
        UmlTypeSpec t = f[i].extend();
        
        if ((t.type != null) || (t.explicit_type.length() != 0)) {
  	fw.write(" extends ");
  	UmlItem.write(t, aLanguage.javaLanguage);
        }
      }
      
      writeq('>');
    }
  }

  public static void generate() throws IOException
  {
    if (! flat) {
      int n = classes.size();
      
      for (int i = 0; i != n; i += 1)
        ((UmlClass) classes.elementAt(i)).html();
    }
  }

  protected Vector subClasses;

{ subClasses = new Vector(); }
  public boolean chapterp() {
    return flat;
  }

};
