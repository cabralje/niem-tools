
import java.io.*;
import java.util.*;

/**
 *  This class manages 'attribute', notes that the class 'UmlClassItem'
 *  is a mother class of the class's children.
 * 
 *  You can modify it as you want (except the constructor)
 */
@SuppressWarnings("rawtypes")
class UmlAttribute extends UmlBaseAttribute {
  public UmlAttribute(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "attribute";
  }

  @SuppressWarnings("unchecked")
public void memo_ref() {
    if (visibility() == aVisibility.PublicVisibility) {
     
      String s = parent().stereotype();
      
      if (!s.equals("enum") && 
          !s.equals("enum_pattern") &&
          !s.equals("enum_class") &&
          !s.equals("table"))
        attrs.addElement(this);
    }
    super.memo_ref();
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Attribute <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>\n");
  
    String s = description();
    
    if (s.length() != 0) {
      fw.write("<p>");
      if (javaDecl().length() != 0)
        gen_java_decl(s, true);
      else if (phpDecl().length() != 0)
        gen_php_decl(s, true);
      else if (pythonDecl().length() != 0)
        gen_python_decl(s);
      else 
        gen_cpp_decl(s, true);
      fw.write("<br /></p>");
    }
  
    fw.write("<p>Declaration :</p><ul>");
    
    fw.write("<li>Uml : ");
    gen_uml_decl();
    fw.write("</li>");
    
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
      gen_python_decl(s);
      fw.write("</li>");
    }
  
    s = mysqlDecl();
  
    if (s.length() != 0) {
      fw.write("<li>MySQL : ");
      gen_mysql_decl(s);
      fw.write("</li>");
    }
  
    fw.write("</ul>");
    
    annotation_constraint();
    write_properties();
  
    unload(false, false);
  }

  public static void ref_index() throws IOException
  {
    if (!attrs.isEmpty())
      fw.write("<a href=\"public_properties.html\" target = \"projectFrame\"><b> -Public Properties- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    int n = attrs.size();
    
    if (n != 0) {
      sort(attrs);  
  
      start_file("public_properties", "Public Properties Index", true);
      
      fw.write("<table>\n");
      fw.write("<tr bgcolor=\"#f0f0f0\"><td align=\"center\"><b>Property</b></td><td align=\"center\"><b>Class</b></td><td align=\"center\"><b>Description</b></td></tr>\n");
        
      for (int i = 0; i != n; i += 1) {
        UmlItem prop = (UmlItem) attrs.elementAt(i);
        
        fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
        prop.write();
        fw.write("</td><td>");
        prop.parent().write();
        fw.write("</td><td>");
        writeq(prop.description());
        fw.write("</td></tr>\n");
      }
      fw.write("</table>\n");
      
      end_file();
    }
  }

  /**
   * produce the definition in C++
   */
  private void gen_cpp_decl(String s, boolean descr) throws IOException {
    int n = s.length();
    int index;
    boolean unchanged = false;
  
    if (! descr) {
      write((cppVisibility() == aVisibility.DefaultVisibility)
  	  ? visibility() : cppVisibility(),
  	  aLanguage.cppLanguage);
      fw.write(": ");
      index = bypass_comment(s);
    }
    else
      index = 0;
    
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
      }
      else if (s.startsWith("${multiplicity}", index)) {
        index += 15;
        
        String m = multiplicity();
        
        if ((m.length() == 0) || (m.charAt(0) != '[')) {
    	fw.write("[");
    	writeq(m);
    	fw.write("]");
        }
        else
    	writeq(m);
      }
      else if (s.startsWith("${stereotype}", index)) {
        index += 13;
        writeq(CppSettings.relationAttributeStereotype(stereotype()));
      }
      else if (s.startsWith("${value}", index) ||
  	     s.startsWith("${h_value}", index))
        break;
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("static ");
      }
      else if (s.startsWith("${const}", index)) {
        index += 8;
        if (isCppConstExpr())
  	fw.write("constexpr ");
        else if (isReadOnly())
  	fw.write("const ");
      }
      else if (s.startsWith("${thread_local}", index)) {
        index += 15;
        if (isCppThreadLocal())
  	fw.write("thread_local ");
      }
      else if (s.startsWith("${mutable}", index)) {
        index += 10;
        if (isCppMutable())
  	fw.write("mutable ");
      }
      else if (s.startsWith("${volatile}", index)) {
        index += 11;
        if (isVolatile())
  	fw.write("volatile ");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(type(), aLanguage.cppLanguage);
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
      else if (s.charAt(index) == ';') {
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
   * produce the definition in Java
   */
  private void gen_java_decl(String s, boolean descr) throws IOException {
    int n = s.length();
    int index = bypass_comment(s);
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
      }
      else if (s.startsWith("${multiplicity}", index)) {
        index += 15;
        
        String m = multiplicity();
        int mn = m.length();
        
        if (mn != 0) {
    	if (m.charAt(0) != '[')
    	  fw.write("[]");
    	else {
    	  for (int index2 = 0; index2 != mn; index2 += 1) {
    	    switch (m.charAt(index2++)) {
    	    case '[':
    	      fw.write('[');
    	      break;
    	    case ']':
    	      fw.write(']');
    	    default:
    	      break;
    	    }
    	  }
    	}
        }
      }
      else if (s.startsWith("${stereotype}", index)) {
        index += 13;
        writeq(JavaSettings.relationAttributeStereotype(stereotype()));
      }
      else if (s.startsWith("${value}", index)) {
        index += 8;
      }
      else if (s.startsWith("${class}", index)) {
        index += 8;
        writeq(parent().name());
      }
      else if (s.startsWith("${visibility}", index)) {
        index += 13;
        write(visibility(), aLanguage.javaLanguage);
        fw.write(' ');
      }
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("static ");
      }
      else if (s.startsWith("${transient}", index)) {
        index += 12;
        if (isJavaTransient())
  	fw.write("transient ");
      }
      else if (s.startsWith("${volatile}", index)) {
        index += 11;
        if (isVolatile())
  	fw.write("volatile ");
      }
      else if (s.startsWith("${final}", index)) {
        index += 8;
        if (isReadOnly())
  	fw.write("final ");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(type(), aLanguage.javaLanguage);
      }
      else if (s.startsWith("${@}", index)) {
        index += 4;
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '\r') {
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
    int n = s.length();
    int index = bypass_comment(s);
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${name}", index)) {
        index += 7;
        if (!st.equals("enum") && !isReadOnly())
  	fw.write('$');
        writeq(name());
      }
      else if (s.startsWith("${var}", index)) {
        index += 6;
        if ((st != "enum") &&
  	  !isReadOnly() &&
  	  !isClassMember() &&
  	  (visibility() == aVisibility.PackageVisibility))
  	fw.write("var ");
      }
      else if (s.startsWith("${value}", index)) {
        index += 8;
      }
      else if (s.startsWith("${visibility}", index)) {
        index += 13;
        write(visibility(), aLanguage.phpLanguage);
        fw.write(' ');
      }
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("static ");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(type(), aLanguage.phpLanguage);
      }
      else if (s.startsWith("${const}")) {
        index += 8;
        if (isReadOnly())
  	fw.write("const ");
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
  private void gen_python_decl(String s) throws IOException {
    String st = PythonSettings.classStereotype(stereotype());
    int n = s.length();
    int index = bypass_comment(s);
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(name());
      }
      else if (s.startsWith("${value}", index)) {
        index += 8;
        writeq((defaultValue().equals("")) ? "None" : defaultValue());
      }
      else if (s.startsWith("${self}", index)) {
        index += 7;
        if (!isClassMember())
  	fw.write("self.");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(type(), aLanguage.pythonLanguage);
      }
      else if (s.startsWith("${stereotype}")) {
        index += 13;
        writeq(st);
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else
        writeq(s.charAt(index++));
    }
  }

  /**
   * produce the definition in C++
   */
  private void gen_mysql_decl(String s) throws IOException {
    int n = s.length();
    int index = 0;
    aMySqlKind k = mysqlKind();
  
    while ((index != n) && ((s.charAt(index) == ' ') || (s.charAt(index) == '\t')))
      index += 1;
    
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${name}", index)) {
        index += 7;
  	if (k != aMySqlKind.aPrimaryKey) {
  	  if (k != aMySqlKind.aColumn)
  	    fw.write(' ');
  	  writeq(name());
  	}
      }
      else if (s.startsWith("${stereotype}", index)) {
        index += 13;
        writeq(stereotype());
      }
      else if (s.startsWith("${default}", index)) {
        index += 10;
        if (!defaultValue().equals("")) {
  	fw.write(" DEFAULT '");
  	writeq(defaultValue());
  	fw.write('\'');
        }
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        
        if (k == aMySqlKind.aColumn) {
  	String t = type().explicit_type;
  	String m = multiplicity();
  	
  	if (! m.equals("")) {
  	  int i = t.indexOf(' ');
  	  
  	  if (i == -1) {
  	    fw.write(t);
  	    fw.write('(');
  	    fw.write(m);
  	    fw.write(')');
  	  }
  	  else {
  	    fw.write(t.substring(0, i));
  	    fw.write('(');
  	    fw.write(m);
  	    fw.write(')');
  	    fw.write(t.substring(i));
  	  }
  	}
  	else
  	  fw.write(t);
        }
        else if ((k == aMySqlKind.aPrimaryKey) ||
  	       (k == aMySqlKind.anUniqueKey) ||
  	       (k == aMySqlKind.aKey)) {
  	if (isMysqlUsingBtree())
  	  fw.write(" USING BTREE");
  	else if (isMysqlUsingHash())
  	  fw.write(" USING HASH");
        }
      }
      else if (s.startsWith("${ref}", index))
        // only a relation is a foreigh key
        index += 6;
      else if (s.startsWith("${constraint}", index)) {
        index += 13;
        
        if ((k == aMySqlKind.aPrimaryKey) || (k == aMySqlKind.anUniqueKey)) {
  	if (!mysqlConstraint().equals("")) {
  	  fw.write("CONSTRAINT ");
  	  fw.write(mysqlConstraint());
  	  fw.write(' ');
  	}
        }
      }
      else if (s.startsWith("${modifier}", index)) {
        index += 11;
        if (k == aMySqlKind.aPrimaryKey)
  	fw.write("PRIMARY ");
        else if (k == aMySqlKind.anUniqueKey)
  	fw.write("UNIQUE ");
        else if (k == aMySqlKind.aFulltextKey)
  	fw.write("FULLTEXT ");
        else if (k == aMySqlKind.aSpatialKey)
  	fw.write("SPATIAL ");
      }
      else if (s.startsWith("${notnull}", index)) {
        index += 10;
        if ((k == aMySqlKind.aColumn) && isMysqlNotNull())
  	fw.write(" NOT NULL");
      }
      else if (s.startsWith("${autoincr}", index)) {
        index += 11;
        if ((k == aMySqlKind.aColumn) && isMysqlAutoIncrement())
  	fw.write(" AUTO_INCREMENT");
      }
      else if (s.startsWith("${columns}", index)) {
        index += 10;
        if (k != aMySqlKind.aColumn) {
  	UmlAttribute[] columns = mysqlColumns();
  	
  	if (columns.length != 0) {
  	  int i = 0;
  	  
  	  columns[0].write();
  	  while (++i != columns.length) {
  	    fw.write(',');
  	    columns[i].write();
  	  }
  	}
        }
      }
      else if (s.charAt(index) == '\r')
        index += 1;
      else if (s.charAt(index) == '\n') {
        fw.write(' ');
        
        do
  	index += 1;
        while ((index != n) && Character.isWhitespace(s.charAt(index)));
      }
      else if (s.charAt(index) == '@')
        index = manage_alias(s, index);
      else
        writeq(s.charAt(index++));
    }
  }

  /**
   * produce the definition in Uml
   */
  private void gen_uml_decl() throws IOException {
    if (isClassMember())
      fw.write("static, ");
    write(visibility());
    writeq(name());
    fw.write(" : ");
    write(type());
    
    String s;
    
    s = defaultValue();
    if (s.length() != 0) {
      if (s.charAt(0) != '=')
        fw.write(" = ");
      writeq(s);
    }
    
    s = multiplicity();
    if (s.length() != 0) {
      fw.write(", multiplicity : ");
      writeq(s);
    }
  
    if (isDerived())
      fw.write((isDerivedUnion()) ? ", derived union" : ", derived");
      
    if (isReadOnly())
      fw.write(", read only");
      
    if (isOrdered())
      fw.write(", ordered");
      
    if (isUnique())
      fw.write(", unique");
  
  }

  public void key_columns() throws IOException {
    UmlAttribute[] columns = mysqlColumns();
    
    parent().write();
    fw.write(' ');
  
    if (columns.length != 0) {
      int i = 0;
      
      fw.write('(');
      columns[0].write();
      
      while (++i != columns.length) {
        fw.write(',');
        columns[i].write();
      }
      
      fw.write(')');
    }
    else
      fw.write("()");
  }

public static Vector attrs;

  static { attrs = new Vector(); }};
