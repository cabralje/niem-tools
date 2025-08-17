package fr.bouml;


import java.io.*;

/**
 *  This class manages 'relations', notes that the class 'UmlClassItem'
 *  is a mother class of all the class's children.
 * 
 *  You can modify it as you want (except the constructor)
 */
public class UmlRelation extends UmlBaseRelation {
  public UmlRelation(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "relation";
  }

  @SuppressWarnings("unchecked")
public void memo_ref() {
    switch (relationKind().value()) {
    case aRelationKind._aGeneralisation:
    case aRelationKind._aRealization:
    case aRelationKind._aDependency:
      return;
    default:
      if (visibility() == aVisibility.PublicVisibility) {
        String s = parent().stereotype();
  
        if (!s.equals("table"))
          UmlAttribute.attrs.addElement(this);
      }
      super.memo_ref();
    }
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    boolean extension;
    
    switch (relationKind().value()) {
    case aRelationKind._aGeneralisation:
    case aRelationKind._aRealization:
    case aRelationKind._aDependency:
      return;
    case aRelationKind._aDirectionalAssociation:
      extension = (parent().stereotype().equals("stereotype")) &&
        (roleType().stereotype().equals("metaclass"));
      break;
    default:
      extension = false;
    }
  
    define();
  
    if (extension)
      fw.write("<table><tr><td><div class=\"element\">Extension</div></td></tr></table>");
    else {
      fw.write("<table><tr><td><div class=\"element\">Relation <b>");
      writeq(name());
      fw.write("</b></div></td></tr></table>");
    }
    
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
  
    if (extension) {
      fw.write("<p>Extend ");
      roleType().write();
      fw.write("</p>");
    }
    else {
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
    }
  
    annotation_constraint();
    write_properties();
  
    unload(false, false);
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
        roleType().write();
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(roleName());
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
      else if (s.startsWith("${association}", index)) {
        index += 14;
        write(association(), aLanguage.cppLanguage);
      }
      else if (s.startsWith("${association}", index)) {
        index += 14;
        write(association(), aLanguage.cppLanguage);
      }
      else if (s.startsWith("${value}", index) ||
  	     s.startsWith("${h_value}", index)) {
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
        roleType().write();
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(roleName());
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
   * produce the definition in Php
   */
  private void gen_php_decl(String s, boolean descr) throws IOException {
    int n = s.length();
    int index = bypass_comment(s);
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
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("static ");
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        if (!isReadOnly())
  	fw.write("$");
        writeq(roleName());
      }
      else if (s.startsWith("${var}")) {
        index += 6;
        if (!isReadOnly() &&
  	  !isClassMember() &&
  	  (visibility() == aVisibility.PackageVisibility))
  	fw.write("var ");
      }
      else if (s.startsWith("${value}", index)) {
        index += 8;
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
        writeq(roleName());
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
        roleType().write();
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
   * produce the definition in Python
   */
  private void gen_mysql_decl(String s) throws IOException {
    aRelationKind k = relationKind();
    
    if ((k != aRelationKind.aDirectionalAssociation) &&
        (k != aRelationKind.aDirectionalAggregation) &&
        (k != aRelationKind.aDirectionalAggregationByValue))
      return;
    
    if ((side(true) == this) && roleType().stereotype().equals("table")) {
      int n = s.length();
      int index = 0;
  
      while ((index != n) && ((s.charAt(index) == ' ') || (s.charAt(index) == '\t')))
        index += 1;
    
      while (index != n) {
        if (s.startsWith("${comment}", index))
  	index += 10;
        else if (s.startsWith("${description}", index))
  	index += 14;
        else if (s.startsWith("${constraint}", index)) {
  	index += 13;
  
  	if (!mysqlConstraint().equals("")) {
  	  fw.write("CONSTRAINT ");
  	  writeq(mysqlConstraint());
  	  fw.write(' ');
  	}
        }
        else if (s.startsWith("${modifier}", index)) {
  	index += 11;
  	fw.write("FOREIGN ");
        }
        else if (s.startsWith("${name}", index)) {
  	index += 7;
  	fw.write(' ');
  	writeq(roleName());
        }
        else if (s.startsWith("${type}", index)) {
  	// not for FK
  	index += 7;
        }
        else if (s.startsWith("${columns}", index)) {
  	index += 10;
  
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
        else if (s.startsWith("${ref}", index)) {
  	index += 6;
  	
  	fw.write(" REFERENCES ");
  	
  	UmlAttribute ref = mysqlReferencedKey();
  	
  	if (ref == null)
  	  fw.write("???");
  	else
  	  ref.key_columns();
  	
  	String st = stereotype();
  	
  	if (!st.equals("")) {
  	  fw.write(" MATCH ");
  	  fw.write(st.toUpperCase());
  	}
  	
  	write_opt(mysqlOnDelete(), " ON DELETE ");
  	write_opt(mysqlOnUpdate(), " ON UPDATE ");
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
  }

  /**
   * produce the definition in Uml
   */
  private void gen_uml_decl() throws IOException {
    if (isClassMember())
      fw.write("static, ");
    write(visibility());
    writeq(roleName());
    fw.write(" : ");
    roleType().write();
  
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

  public String pretty_name() {
    return roleName();
  }

  private void write_opt(aMySqlRefOption opt, String pfix) throws IOException {
    if (opt == aMySqlRefOption.doRestrict) {
      fw.write(pfix);
      fw.write("RESTRICT");
    }
    else if (opt == aMySqlRefOption.doCascade) {
      fw.write(pfix);
      fw.write("CASCADE");
    }
    else if (opt == aMySqlRefOption.doSetNull) {
      fw.write(pfix);
      fw.write("SET NULL");
    }
    else if (opt == aMySqlRefOption.doNoAction) {
      fw.write(pfix);
      fw.write("NO ACTION");
    }
  }

};
