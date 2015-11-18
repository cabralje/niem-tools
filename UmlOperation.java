
import java.io.*;
import java.util.*;

/**
 *  This class manages 'operations', notes that the class 'UmlClassItem'
 *  is a mother class of all the class's children.
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlOperation extends UmlBaseOperation {
  public UmlOperation(long id, String n){ super(id, n); }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "operation";
  }

  public void memo_ref() {
    if (visibility() == aVisibility.PublicVisibility)
      opers.addElement(this);
    super.memo_ref();
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    define();
  
    fw.write("<table><tr><td><div class=\"element\">Operation <b>");
    writeq(name());
    fw.write("</b></div></td></tr></table>");
    
    String s = description();
  
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
      gen_python_decl(s, false);
      fw.write("</li>");
    }
  
    fw.write("</ul>");
    
    annotation_constraint();
    write_properties();
  
    unload(false, false);
  }

  public static void ref_index() throws IOException
  {
    if (!opers.isEmpty())
      fw.write("<a href=\"public_operations.html\" target = \"projectFrame\"><b> -Public Operations- </b></a>");
  }

  public static void generate_index() throws IOException
  {
    int n = opers.size();
    
    if (n != 0) {
      sort(opers);  
  
      start_file("public_operations", "Public Operations Index", true);
      
      fw.write("<table>\n");
      fw.write("<tr bgcolor=\"#f0f0f0\"><td align=\"center\"><b>Operation</b></td><td align=\"center\"><b>Class</b></td><td align=\"center\"><b>Description</b></td></tr>\n");
        
      for (int i = 0; i != n; i += 1) {
        UmlItem op = (UmlItem) opers.elementAt(i);
        
        fw.write("<tr bgcolor=\"#f0f0f0\"><td>");
        op.write();
        fw.write("</td><td>");
        op.parent().write();
        fw.write("</td><td>");
        writeq(op.description());
        fw.write("</td></tr>\n");
      }
      fw.write("</table>\n");
      
      end_file();
    }
  }

  /**
   * produce the definition in Uml
   */
  private void gen_uml_decl() throws IOException {
    if (isAbstract())
      fw.write("abstract, ");
    if (isClassMember())
      fw.write("static, ");
    write(visibility());
    writeq(name());
    
    UmlParameter[] pa = params();
    int rank;
    String sep = "(";
    String s;
    
    for (rank = 0; rank != pa.length; rank += 1) {
      UmlParameter p = pa[rank];
      
      fw.write(sep);
      sep = ", ";
      
      switch (p.dir.value()) {
      case aDirection._InputOutputDirection:
        fw.write("inout ");
        break;
      case aDirection._InputDirection:
        fw.write("in ");
        break;
      default:
        // aDirection.OutputDirection
        fw.write("out ");
      }
      writeq(p.name);
      fw.write(" : ");
      write(p.type);
      
      s = p.multiplicity;
      if (s.length() != 0) {
        fw.write(" [");
        writeq(s);
        fw.write("]");
      }
      
      s = p.default_value;
      if (s.length() != 0) {
        if (s.charAt(0) != '=')
  	fw.write(" = ");
        writeq(s);
      }
    }
    fw.write((rank == 0) ? "() : " : ") : ");
    write(returnType());
    
    s = multiplicity();
    if (s.length() != 0) {
      fw.write(" [");
      writeq(s);
      fw.write("]");
    }
    
    sep = ",  exceptions : ";
    
    UmlTypeSpec[] e = exceptions();
    
    for (int index2 = 0; index2 != e.length; index2 += 1) {
      fw.write(sep);
      sep = ", ";
      write(e[index2]);
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
    
    UmlParameter[] pa = params();
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${friend}", index)) {
        index += 9;
        if (isCppFriend())
  	fw.write("friend ");
      }
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("static ");
      }
      else if (s.startsWith("${inline}", index)) {
        index += 9;
        if (isCppInline())
  	fw.write("inline ");
      }
      else if (s.startsWith("${virtual}", index)) {
        index += 10;
        if (isCppVirtual())
  	fw.write("virtual ");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(returnType(), aLanguage.cppLanguage);
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(compute_name(cppNameSpec()));
      }
      else if (s.startsWith("${(}", index)) {
        index += 4;
        fw.write('(');
      }
      else if (s.startsWith("${)}", index)) {
        index += 4;
        fw.write(')');
      }
      else if (s.startsWith("${const}", index)) {
        index += 8;
        if (isCppConst())
  	fw.write(" const");
        if (isCppOverride())
  	fw.write(" override");
        if (isCppFinal())
  	fw.write(" final");
      }
      else if (s.startsWith("${volatile}", index)) {
        index += 11;
        if (isVolatile())
  	fw.write(" volatile");
      }
      else if (s.startsWith("${throw}", index)) {
        index += 8;
        
        if (isCppNoexcept())
  	fw.write(" noexcept");
        else {
  	String sep = " throw (";
  	UmlTypeSpec[] e = exceptions();
  	int index2;
  	
  	for (index2 = 0; index2 != e.length; index2 += 1) {
  	  fw.write(sep);
  	  sep = ", ";
  	  write(e[index2], aLanguage.cppLanguage);
  	}
  	if (index2 != 0)
  	  fw.write(')');
  	else if (CppSettings.operationForceNoexcept())
  	  fw.write(" noexcept");
  	else if (CppSettings.operationForceThrow())
  	  fw.write(" throw ()");
        }
      }
      else if (s.startsWith("${t", index)) {
        // must be placed after throw !
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        
        if (rank < pa.length) 
  	write(pa[rank].type, aLanguage.cppLanguage);
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${p", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) 
  	writeq(p[rank].name);
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${v", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank >= p.length) 
  	fw.write("???");
        else if (!p[rank].default_value.equals("")) {
  	fw.write(" = ");
  	writeq(p[rank].default_value);
        }
        index = index2 + 1;
      }
      else if (s.startsWith("${abstract}", index)) {
        if (isAbstract())
  	fw.write("= 0 ");
        else if (isCppDeleted())
  	fw.write(" = delete");
        else if (isCppDefaulted())
  	fw.write(" = default");
        break;
      }
      else if (s.startsWith("${stereotype}", index)) {
        index += 13;
        // get/set relation with multiplicity > 1
        UmlClassMember m = getOf();
        
        if ((m != null) || ((m = setOf()) != null))
  	writeq(CppSettings.relationAttributeStereotype(m.stereotype()));
      }
      else if (s.startsWith("${association}", index)) {
        index += 14;
        // get/set relation with multiplicity > 1
        UmlClassMember m = getOf();
        
        if (((m != null) || ((m = setOf()) != null)) &&
  	  (m.kind() == anItemKind.aRelation))
  	write(((UmlRelation) m).association(), aLanguage.cppLanguage);
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
      else if (s.startsWith("${final}", index)) {
        index += 8;
        if (isJavaFinal())
  	fw.write("final ");
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
      else if (s.startsWith("${abstract}", index)) {
        index += 11;
        if (isAbstract())
  	fw.write("abstract ");
      }
      else if (s.startsWith("${synchronized}", index)) {
        index += 15;
        if (isJavaSynchronized())
  	fw.write("synchronized ");
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        write(returnType(), aLanguage.javaLanguage);
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(compute_name(javaNameSpec()));
      }
      else if (s.startsWith("${(}", index)) {
        index += 4;
        fw.write('(');
      }
      else if (s.startsWith("${)}", index)) {
        index += 4;
        fw.write(')');
      }
      else if (s.startsWith("${throws}", index)) {
        index += 9;
        
        String sep = " throws ";
        UmlTypeSpec[] e = exceptions();
        
        for (int index2 = 0; index2 != e.length; index2 += 1) {
  	fw.write(sep);
  	sep = ", ";
  	write(e[index2], aLanguage.javaLanguage);
        }
      }
      else if (s.startsWith("${staticnl}", index))
        break;
      else if (s.startsWith("${t", index)) {
        // must be placed after throws !
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) 
  	write(p[rank].type, aLanguage.javaLanguage);
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${p", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) 
  	writeq(p[rank].name);
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${stereotype}", index)) {
        index += 13;
        // get/set relation with multiplicity > 1
        UmlClassMember m = getOf();
        
        if ((m != null) || ((m = setOf()) != null))
  	writeq(JavaSettings.relationAttributeStereotype(m.stereotype()));
      }
      else if (s.startsWith("${association}", index)) {
        index += 14;
        // get/set relation with multiplicity > 1
        UmlClassMember m = getOf();
        
        if (((m != null) || ((m = setOf()) != null)) &&
  	  (m.kind() == anItemKind.aRelation))
  	write(((UmlRelation) m).association(), aLanguage.javaLanguage);
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
    String cl_stereotype = 
       PhpSettings.classStereotype(parent().stereotype());
    int n = s.length();
    int index = bypass_comment(s);
    int afterparam = -1;
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${final}", index)) {
        index += 8;
        if (isPhpFinal())
  	fw.write("final ");
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
      else if (s.startsWith("${abstract}", index)) {
        index += 11;
        if (isAbstract() && !cl_stereotype.equals("interface"))
  	fw.write("abstract ");
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(compute_name(phpNameSpec()));
      }
      else if (s.startsWith("${(}", index)) {
        index += 4;
        fw.write('(');
      }
      else if (s.startsWith("${)}", index)) {
        index += 4;
        fw.write(')');
        afterparam = index;
        while ((afterparam != n) && (s.charAt(index) <= ' '))
  	afterparam += 1;
      }
      else if (s.startsWith("${staticnl}", index))
        break;
      else if (s.startsWith("${type}", index)) {
        index += 7;
        
        if ((index - 7) == afterparam) {
  	UmlTypeSpec t = returnType();
  	
  	if ((t.type != null) || !t.explicit_type.equals("")) {
  	  fw.write(": ");
  	  write(t, aLanguage.phpLanguage);
  	}
        }
        else
  	write(returnType(), aLanguage.phpLanguage);
      }
      else if (s.startsWith("${t", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) 
  	write(p[rank].type, aLanguage.phpLanguage);
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${p", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) {
  	fw.write('$');
  	writeq(p[rank].name);
        }
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${v", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank >= p.length)
  	fw.write("???");
        else if (!p[rank].default_value.equals("")) {
  	fw.write(" = ");
  	writeq(p[rank].default_value);
        }
        index = index2 + 1;
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
    String cl_stereotype = 
       PythonSettings.classStereotype(parent().stereotype());
    int n = s.length();
    int index = bypass_comment(s);
    boolean in_params = false;
    boolean unchanged = false;
  
    while (index != n) {
      if (s.startsWith("${comment}", index))
        index += 10;
      else if (s.startsWith("${description}", index))
        index += 14;
      else if (s.startsWith("${docstring}", index))
        index += 12;
      else if (s.startsWith("${static}", index)) {
        index += 9;
        if (isClassMember())
  	fw.write("@staticmethod<br />");
      }
      else if (s.startsWith("${abstract}", index)) {
        index += 11;
        if (isAbstract())
  	fw.write("@abstractmethod<br />");
      }
      else if (s.startsWith("${@}", index)) {
        index += 4;
        writeq(pythonDecorators());
      }
      else if (s.startsWith("${name}", index)) {
        index += 7;
        writeq(compute_name(pythonNameSpec()));
      }
      else if (s.startsWith("${class}", index)) {
        index += 8;
        parent().write();
      }
      else if (s.startsWith("${(}", index)) {
        index += 4;
        fw.write('(');
        in_params = true;
      }
      else if (s.startsWith("${)}", index)) {
        index += 4;
        fw.write(')');in_params = false;
      }
      else if (s.startsWith("${type}", index)) {
        index += 7;
        
        UmlTypeSpec t = returnType();
  	
        if (!t.toString().equals("")) {
  	fw.write(" -> ");
  	write(t, aLanguage.pythonLanguage);
        }
      }
      else if (s.startsWith("${t", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) {
  	UmlTypeSpec t = p[rank].type;
  	
  	if (!t.toString().equals("")) {
  	  if (in_params)
  	    fw.write(": ");
  	  write(t, aLanguage.pythonLanguage);
  	}
        }
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${p", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length) {
  	fw.write('$');
  	writeq(p[rank].name);
        }
        else
  	fw.write("???");
        index = index2 + 1;
      }
      else if (s.startsWith("${v", index)) {
        int index2 = s.indexOf('}', index + 3);
        
        if (index2 == -1)
  	break;
        
        int rank = Integer.valueOf(s.substring(index + 3, index2)).intValue();
        UmlParameter[] p = params();
        
        if (rank < p.length)
  	fw.write("???");
        else if (!p[rank].default_value.equals("")) {
  	fw.write(" = ");
  	writeq(p[rank].default_value);
        }
        index = index2 + 1;
      }
      else if (s.charAt(index) == ':') {
        if (descr)
  	fw.write(s.charAt(index++));
        else
  	break;
      }
      else if (s.charAt(index) == '\r')
        index += 1;
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

  public String compute_name(String s) {
     if (s.length() != 0) {
       UmlClassMember m = getOf();
       
       if ((m != null) || ((m = setOf()) != null)) {
         String n = (m.kind() == anItemKind.aRelation)
  	 ? ((UmlRelation) m).roleName()
  	 : m.name();
         int index;
       
         if ((index = s.indexOf("${name}")) != -1)
  	 return s.substring(0, index) + n + s.substring(index + 7);
         else if ((index = s.indexOf("${Name}")) != -1)
  	 return s.substring(0, index) +
  	   n.substring(0, 1).toUpperCase() +
  	     n.substring(1) + s.substring(index + 7);
         else if ((index = s.indexOf("${NAME}")) != -1)
  	 return s.substring(0, index) +
  	   n.toUpperCase() + s.substring(index + 7);
         else
  	 return s;
       }
    }
  
    return name();
  }

  protected static Vector opers;

  static { opers = new Vector(); }};
