
import java.util.*;
/**
 *  This class manages settings concerning PYTHON, configured through
 *  the 'Generation settings' dialog.
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static
 *  members allowing to just write 'PythonSettings::member' rather than
 *  'PythonSettings::instance()->member' or other long sentence like this.
 */
class PythonSettings extends UmlSettings {
  /**
   *  return if classes follow Python 2.2 by default
   */
  public static boolean isPython_2_2()
  {
    read_if_needed_();
    return _2_2;
  }

  /**
   *  set if classes follow Python 2.2 by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsPython_2_2(boolean y)
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPython22Cmd,
  		   (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _2_2 = y;
  }

  /**
   *  returns if operations follow Python 3 (pep-3107)
   */
  public static boolean isPython_3_operation()
  {
    read_if_needed_();
  
    return _operation_3;
  }

  /**
   *  set if operations follow Python 3 (pep-3107)
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsPython_3_operation(boolean y)
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPython3OperationCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    
    _operation_3 = y;
  }

  /**
   *  return the  default indent step
   */
  public static String indentStep()
  {
    read_if_needed_();
    return _indent_step;
  }

  /**
   *  set default indent step
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IndentStep(String v)
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonIndentStepCmd, v);
    UmlCom.check();
    _indent_step = v;
  }

  /**
   *  returns TRUE when the created Python objects are initialized
   *  with the default declaration/definition
   */
  public static boolean useDefaults()
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._getPythonUseDefaultsCmd);
    return UmlCom.read_bool();
  }

  /**
   *  if y is TRUE the future created Python objects will be initialized
   *  with the default declaration/definition
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UseDefaults(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonUseDefaultsCmd,
  		  (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  }

  /**
   *  returns the PYTHON stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String relationAttributeStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_relation_attribute_stereotypes.get(s);
    
    return (b != null) ? b.python : s;
  }

  /**
   *  set the PYTHON stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationAttributeStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonRelationAttributeStereotypeCmd, s, v);
    UmlCom.check();
  
    UmlStereotype st = (UmlStereotype) UmlSettings._map_relation_attribute_stereotypes.get(s);
  
    if (st == null)
      st = UmlSettings.add_rel_attr_stereotype(s);
    st.python = v;
  }

  /**
   *  reverse of the RelationAttributeStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the PYTHON one given in argument
   */
  public static String relationAttributeUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _relation_attribute_stereotypes.length;
    
    while (index-- != 0)
      if (_relation_attribute_stereotypes[index].python.equals(s))
        return _relation_attribute_stereotypes[index].uml;
    
    return null;
  }

  /**
   *  returns the PYTHON stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String classStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
    
    return (b != null) ? b.python : s;
  }

  /**
   *  set the PYTHON stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ClassStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonClassStereotypeCmd, s, v);
    UmlCom.check();
  
    UmlStereotype st = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
  
    if (st == null)
      st = UmlSettings.add_class_stereotype(s);
    st.python = v;
  }

  /**
   *  reverse of the ClassStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the PYTHON one given in argument
   */
  public static String classUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _class_stereotypes.length;
    
    while (index-- != 0)
      if (_class_stereotypes[index].python.equals(s))
        return _class_stereotypes[index].uml;
    
    return null;
  }

  /**
   * returns the import or other form specified in the last
   * 'Generation settings' tab for the Python type given in argument.
   */
  public static String get_import(String s)
  {
    read_if_needed_();
    
    return (String) _map_imports.get(s);
  
  }

  /**
   *   set the import or other form specified in the last
   *   'Generation settings' tab for the Python type given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_Import(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonImportCmd, s, v);
    UmlCom.check();
      
    if ((v != null) && (v.length() != 0))
      _map_imports.put(s, v);
    else
      _map_imports.remove(s);
  }

  /**
   *   return the 'root' directory
   */
  public static String rootDir()
  {
    read_if_needed_();
    
    return _root;
  }

  /**
   *   set the 'root' directory
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RootDir(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonRootdirCmd, v);
    UmlCom.check();
  
    _root = v;
  }

  /**
   *  returns the default source file content
   */
  public static String sourceContent()
  {
    read_if_needed_();
    
    return _src_content;
  }

  /**
   *  set the default source file content
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceContent(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonSourceContentCmd, v);
    UmlCom.check();
    
    _src_content = v;
  
  }

  /**
   *  returns the extension of the file produced by the PYTHON code generator
   */
  public static String sourceExtension()
  {
    read_if_needed_();
    
    return _ext; 
  }

  /**
   *  set the extension of the file produced by the PYTHON code generator
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceExtension(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonSourceExtensionCmd, v);
    UmlCom.check();
    
    _ext = v;
  
  }

  /**
   *  returns the default definition of a class
   */
  public static String classDecl()
  {
    read_if_needed_();
    
    return _class_decl;
  }

  /**
   *  set the default definition of a class
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ClassDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonClassDeclCmd, v);
    UmlCom.check();
    
    _class_decl = v;
  
  }

  /**
   *  returns the default specification for an 'external' class
   */
  public static String externalClassDecl()
  {
    read_if_needed_();
    
    return _external_class_decl;
  }

  /**
   *  set the default specification for an 'external' class
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ExternalClassDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonExternalClassDeclCmd, v);
    UmlCom.check();
    
    _external_class_decl = v;
  
  }

  /**
   *  returns the default definition of an enumeration
   */
  public static String enumDecl()
  {
    read_if_needed_();
    
    return _enum_decl;
  }

  /**
   *  set the default definition of an enumeration
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonEnumDeclCmd, v);
    UmlCom.check();
    
    _enum_decl = v;
  }

  /**
   *  returns the default definition of an attribute depending on the multiplicity
   */
  public static String attributeDecl(String multiplicity)
  {
    read_if_needed_();
  
    return _attr_decl[mult_column(multiplicity)];
  }

  /**
   *  set the default definition of an attribute
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_AttributeDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonAttributeDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _attr_decl[mult_column(multiplicity)] = v;
  }

  /**
   *  returns the default definition of an enumeration item
   */
  public static String enumItemDecl()
  {
    read_if_needed_();
    
    return _enum_item_decl;
  }

  /**
   *  set the default definition of an enumeration item
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumItemDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonEnumItemDeclCmd, v);
    UmlCom.check();
      
    _enum_item_decl = v;
  }

  /**
   *  returns the default definition of a relation depending on it is an
   *  aggregation by value or not and the multiplicity, given in argument.
   */
  public static String relationDecl(boolean by_value, String multiplicity)
  {
    read_if_needed_();
    
    return _rel_decl[(by_value) ? 1 : 0][mult_column(multiplicity)];
  }

  /**
   *  set the default definition of a relation depending on it is an
   *  aggregation by value or not and the multiplicity, given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationDecl(boolean by_value, String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonRelationDeclCmd, by_value, multiplicity, v);
    UmlCom.check();
    
    _rel_decl[(by_value) ? 1 : 0][mult_column(multiplicity)] = v;
  
  }

  /**
   *  returns the default definition of an operation
   */
  public static String operationDef()
  {
    read_if_needed_();
    
    return _oper_def;
  }

  /**
   *  set the default definition of an operation
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_OperationDef(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonOperationDefCmd, v);
    UmlCom.check();
    
    _oper_def = v;
  
  }

  /**
   *  returns the default definition of __init__
   */
  public static String initOperationDef()
  {
    read_if_needed_();
  
    return _initoper_def;
  }

  /**
   *  set the default definition of __init__
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_InitOperationDef(String v)
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonInitOperationDefCmd, v);
    UmlCom.check();
    
    _initoper_def = v;
  }

  /**
   *  returns the default name of a 'get' operation generated 
   *  through the attribute and relation 'add get operation' menu
   */
  public static String getName()
  {
    read_if_needed_();
    
    return _get_name;
  }

  /**
   *  set the default name of a 'get' operation generated 
   *  through the attribute and relation 'add get operation' menu
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_GetName(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonGetNameCmd, v);
    UmlCom.check();
    
    _get_name = v;
  
  }

  /**
   *  returns the default name of a 'set' operation generated 
   *  through the attribute and relation 'add set operation' menu
   */
  public static String setName()
  {
    read_if_needed_();
    
    return _set_name;
  }

  /**
   *  set the default name of a 'set' operation generated 
   *  through the attribute and relation 'add set operation' menu
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SetName(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._setPythonSetNameCmd, v);
    UmlCom.check();
  
    _set_name = v;
  }

  /**
   *  never called !
   */
  private PythonSettings(){
  }

  private static int mult_column(String mult)
  {
    return ((mult == null) || (mult.length() == 0) || mult.equals("1")) ? 0 : 1;
  }

  private static boolean _defined;
  private static boolean _2_2;

  private static boolean _operation_3;

  private static String _indent_step;

  private static String _root;
  private static String _class_decl;
  private static String _external_class_decl;

  private static String _enum_decl;
  private static String _attr_decl[/*multiplicity*/];
  private static String _enum_item_decl;
  private static String[][] _rel_decl;
  private static String _oper_def;
  private static String _initoper_def;
  private static String _get_name;
  private static String _set_name;
  private static String _src_content;
  private static String _ext;
  private static Hashtable _map_imports;
  protected static void read_()
  {
    _2_2 = UmlCom.read_bool();
    
    _indent_step = UmlCom.read_string();
    
    _root = UmlCom.read_string();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._relation_attribute_stereotypes[index].python = UmlCom.read_string();
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._class_stereotypes[index].python = UmlCom.read_string();
    
    n = UmlCom.read_unsigned();
    _map_imports = new Hashtable((n == 0) ? 1 : n);
    
    for (index = 0; index != n; index += 1) {
      String t = UmlCom.read_string();
      String i = UmlCom.read_string();
      
      _map_imports.put(t, i);
    }
    
    _src_content = UmlCom.read_string();
    _ext = UmlCom.read_string();
  
    _class_decl = UmlCom.read_string();
    _external_class_decl = UmlCom.read_string();
    _enum_decl = UmlCom.read_string();
    _attr_decl = new String[2];
    _attr_decl[0] = UmlCom.read_string();
    _attr_decl[0] = UmlCom.read_string();
    _enum_item_decl = UmlCom.read_string();
    _rel_decl = new String[2][2];
    _rel_decl[0][0] = UmlCom.read_string();
    _rel_decl[0][1] = UmlCom.read_string();
    _rel_decl[1][0] = UmlCom.read_string();
    _rel_decl[1][1] = UmlCom.read_string();
    _oper_def = UmlCom.read_string();
    _get_name = UmlCom.read_string();
    _set_name = UmlCom.read_string();
    _initoper_def = UmlCom.read_string();
    _operation_3 = UmlCom.read_bool();
  }

  protected static void read_if_needed_()
  {
    UmlSettings.read_if_needed_();
    if (!_defined) {
      UmlCom.send_cmd(CmdFamily.pythonSettingsCmd, PythonSettingsCmd._getPythonSettingsCmd);
      read_();
      _defined = true;
    }
  }

}
