package fr.bouml;

import java.util.*;
/**
 *  This class manages settings concerning JAVA, configured through
 *  the 'Generation settings' dialog.
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static 
 *  members allowing to just write 'JavaSettings::member' rather than
 *  'JavaSettings::instance()->member' or other long sentence like this.
 */
final class JavaSettings extends UmlSettings {
  /**
   *  returns TRUE when the created Java objects are initialized
   *  with the default declaration/definition
   */
  public static boolean useDefaults()
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._getJavaUseDefaultsCmd);
    return UmlCom.read_bool();
  }

  /**
   *  if y is TRUE the future created Java objects will be initialized
   *  with the default declaration/definition
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UseDefaults(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaUseDefaultsCmd,
  		  (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  }

  /**
   *  returns the JAVA type corresponding to the 'UML' type given in
   *  argument, as it is configured in the first 'Generation settings'
   *  dialog's tab
   */
  public static String type(String s)
  {
    read_if_needed_();
    
    UmlBuiltin b = (UmlBuiltin) UmlSettings._map_builtins.get(s);
    
    return (b != null) ? b.java : s;
  }

  /**
   *  set the JAVA type corresponding to the 'UML' type given in
   *  argument, as it is configured in the first 'Generation settings'
   *  dialog's tab
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_Type(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaTypeCmd, s, v);
    UmlCom.check();
  
    UmlBuiltin b = (UmlBuiltin) UmlSettings._map_builtins.get(s);
  
    if (b == null)
      b = UmlSettings.add_type(s);
    b.java = v;
  }

  /**
   *  reverse of the Type() operation, returns the 'UML' type corresponding
   *  to the JAVA type given in argument
   */
  public static String umlType(String s)
  {
    read_if_needed_();
    
    int index = _builtins.length;
    
    while (index-- != 0)
      if (_builtins[index].java.equals(s))
        return _builtins[index].uml;
    
    return null;
  }

  /**
   *  returns the JAVA stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String relationAttributeStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_relation_attribute_stereotypes.get(s);
    
    return (b != null) ? b.java : s;
  }

  /**
   *  set the JAVA stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationAttributeStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaRelationAttributeStereotypeCmd, s, v);
    UmlCom.check();
  
    UmlStereotype st = (UmlStereotype) UmlSettings._map_relation_attribute_stereotypes.get(s);
  
    if (st == null)
      st = UmlSettings.add_rel_attr_stereotype(s);
    st.java = v;
  }

  /**
   *  reverse of the RelationAttributeStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the JAVA one given in argument
   */
  public static String relationAttributeUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _relation_attribute_stereotypes.length;
    
    while (index-- != 0)
      if (_relation_attribute_stereotypes[index].java.equals(s))
        return _relation_attribute_stereotypes[index].uml;
    
    return null;
  }

  /**
   *  returns the JAVA stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String classStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
    
    return (b != null) ? b.java : s;
  }

  /**
   *  set the JAVA stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ClassStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaClassStereotypeCmd, s, v);
    UmlCom.check();
  
    UmlStereotype st = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
  
    if (st == null)
      st = UmlSettings.add_class_stereotype(s);
    st.java = v;
  }

  /**
   *  reverse of the ClassStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the JAVA one given in argument
   */
  public static String classUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _class_stereotypes.length;
    
    while (index-- != 0)
      if (_class_stereotypes[index].java.equals(s))
        return _class_stereotypes[index].uml;
    
    return null;
  }

  /**
   * returns the import or other form specified in the last
   * 'Generation settings' tab for the Java type given in argument.
   */
  public static String get_import(String s)
  {
    read_if_needed_();
    
    return (String) _map_imports.get(s);
  
  }

  /**
   *   set the import or other form specified in the last
   *   'Generation settings' tab for the Java type given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  @SuppressWarnings("unchecked")
public static void set_Import(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaImportCmd, s, v);
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaRootdirCmd, v);
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaSourceContentCmd, v);
    UmlCom.check();
    
    _src_content = v;
  
  }

  /**
   *  returns the extension of the file produced by the JAVA code generator
   */
  public static String sourceExtension()
  {
    read_if_needed_();
    
    return _ext; 
  }

  /**
   *  set the extension of the file produced by the JAVA code generator
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceExtension(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaSourceExtensionCmd, v);
    UmlCom.check();
    
    _ext = v;
  
  }

  /**
   *  return the regular expression used to bypass
   *  dir s on reverse/roundtrip
   */
  public static String reverseRoundtripDirRegExp()
  {
    read_if_needed_();
  
    return _dir_regexp;
  }

  /**
   *  return if the regular expression used to bypass
   *  dir s on reverse/roundtrip is case sensitive
   */
  public static boolean isReverseRoundtripDirRegExpCaseSensitive()
  {
    read_if_needed_();
  
    return _dir_regexp_case_sensitive;
  }

  /**
   *  set the regular expression used to bypass
   *  dir s on reverse/roundtrip
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ReverseRoundtripDirRegExp(String s, boolean cs) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaDirRevFilterCmd, s, cs);
    UmlCom.check();
    _dir_regexp = s;
    _dir_regexp_case_sensitive = cs;
  }

  /**
   *  return the regular expression used to bypass
   *  file s on reverse/roundtrip
   */
  public static String reverseRoundtripFileRegExp()
  {
    read_if_needed_();
  
    return _file_regexp;
  }

  /**
   *  return if the regular expression used to bypass
   *  file s on reverse/roundtrip is case sensitive
   */
  public static boolean isReverseRoundtripFileRegExpCaseSensitive()
  {
    read_if_needed_();
  
    return _file_regexp_case_sensitive;
  }

  /**
   *  set the regular expression used to bypass
   *  file s on reverse/roundtrip
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ReverseRoundtripFileRegExp(String s, boolean cs) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaFileRevFilterCmd, s, cs);
    UmlCom.check();
    _file_regexp = s;
    _file_regexp_case_sensitive = cs;
  }

  /**
   *  return if  generate Javadoc style comment
   */
  public static boolean isGenerateJavadocStyleComment()
  {
    read_if_needed_();
  
    return _is_generate_javadoc_comment;
  }

  /**
   *  set if  generate Javadoc style comment
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsGenerateJavadocStyleComment(boolean v)
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaJavadocStyleCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_generate_javadoc_comment = v;
  }

  /**
   *  return if the package prefix must be
   *  always generated before class's names
   */
  public static boolean isForcePackagePrefixGeneration()
  {
    read_if_needed_();
  
    return _is_force_package_gen;
  }

  /**
   *  set if the package prefix must be always generated before class's names
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsForcePackagePrefixGeneration(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaForcePackageGenCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_force_package_gen = v;
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaClassDeclCmd, v);
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaExternalClassDeclCmd, v);
    UmlCom.check();
    
    _external_class_decl = v;
  
  }

  /**
   *  returns the default definition of an enumeration implemented
   *  through an abstract class having constant attributes
   */
  public static String enumPatternDecl()
  {
    read_if_needed_();
    
    return _enum_pattern_decl;
  }

  /**
   *  set the default definition of an enumeration implemented
   *  through an abstract class having constant attributes
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumPatternDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaEnumPatternDeclCmd, v);
    UmlCom.check();
    
    _enum_pattern_decl = v;
  
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaEnumDeclCmd, v);
    UmlCom.check();
    
    _enum_decl = v;
  }

  /**
   *  returns the default definition of an interface
   */
  public static String interfaceDecl()
  {
    read_if_needed_();
    
    return _interface_decl;
  }

  /**
   *  set the default definition of an interface
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_InterfaceDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaInterfaceDeclCmd, v);
    UmlCom.check();
    
    _interface_decl = v;
  
  }

  /**
   *  returns the default definition of an attribute depending on the multiplicity
   */
  public static String attributeDecl(String multiplicity)
  {
    read_if_needed_();
  
    return _attr_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of an attribute
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_AttributeDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaAttributeDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _attr_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  }

  /**
   *  returns the default definition of an enumeration item
   */
  public static String enumPatternItemDecl()
  {
    read_if_needed_();
    
    return _enum_pattern_item_decl;
  }

  /**
   *  set the default definition of an enumeration item
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumPatternItemDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaEnumPatternItemDeclCmd, v);
    UmlCom.check();
      
    _enum_pattern_item_decl = v;
  
  }

  /**
   * Returns the 'case' form produced in the fromInt operation
   * for each enumeration item
   */
  public static String enumPatternItemCase()
  {
    read_if_needed_();
    
    return _enum_pattern_item_case;
  }

  /**
   *   set the 'case' form produced in the fromInt operation
   *   for each enumeration item
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumPatternItemCase(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaEnumPatternItemCaseCmd, v);
    UmlCom.check();
    
    _enum_pattern_item_case = v;
  
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaEnumItemDeclCmd, v);
    UmlCom.check();
      
    _enum_item_decl = v;
  }

  /**
   *  returns the default definition of a relation depending on the
   *  multiplicity given in argument.
   */
  public static String relationDecl(String multiplicity)
  {
    read_if_needed_();
    
    return _rel_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of a relation depending on the
   *  multiplicity given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaRelationDeclCmd, multiplicity, v);
    UmlCom.check();
    
    _rel_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaOperationDefCmd, v);
    UmlCom.check();
    
    _oper_def = v;
  
  }

  /**
   *  returns the default visibility of a 'get' operation generated
   *  through the attribute and relation 'add get operation'
   */
  public static aVisibility getVisibility()
  {
    read_if_needed_();
    
    return _get_visibility;
  }

  /**
   *  set the default visibility of a 'get' operation generated
   *  through the attribute and relation 'add get operation'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_GetVisibility(aVisibility v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaGetvisibilityCmd,
  		  (byte) v.value());
    UmlCom.check();
  
    _get_visibility = v;
  
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaGetNameCmd, v);
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
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaSetNameCmd, v);
    UmlCom.check();
  
    _set_name = v;
  }

  /**
   *   return if a 'get' operation generated through the attribute
   *   and relation 'add get operation' menu is final by default
   */
  public static boolean isGetFinal()
  {
    read_if_needed_();
    
    return _is_get_final;
  }

  /**
   *   set if a 'get' operation generated through the attribute
   *   and relation 'add get operation' menu is final by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsGetFinal(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaIsGetFinalCmd,
  		  (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_get_final = v;
  }

  /**
   *  returns if a 'set' operation generated through the attribute
   *  and relation 'add set operation' menu is final by default
   */
  public static boolean isSetFinal()
  {
    read_if_needed_();
    
    return _is_set_final;
  }

  /**
   *  set if a 'set' operation generated through the attribute
   *  and relation 'add set operation' menu is final by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsSetFinal(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaIsSetFinalCmd,
  		  (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_set_final = v;
  }

  /**
   *  returns the default visibility of a 'set' operation generated
   *  through the attribute and relation 'add set operation'
   */
  public static aVisibility setVisibility()
  {
    read_if_needed_();
    
    return _set_visibility;
  }

  /**
   *  set the default visibility of a 'set' operation generated
   *  through the attribute and relation 'add set operation'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SetVisibility(aVisibility v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaSetVisibilityCmd,
  		  (byte) v.value());
    UmlCom.check();
  
    _set_visibility = v;
  }

  /**
   *  returns if the parameters of a 'set' operation generated through the
   *  attribute and relation 'add set operation' menu are final by default
   */
  public static boolean isSetParamFinal()
  {
    read_if_needed_();
    
    return _is_set_param_final;
  }

  /**
   *  set if the parameters of a 'set' operation generated through the
   *  attribute and relation 'add set operation' menu are final by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsSetParamFinal(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._setJavaIsSetParamFinalCmd,
  		  (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_set_param_final = v;
  }

  /**
   *  never called !
   */
  private JavaSettings(){
  }

  private static boolean _defined;
  private static String _root;
  private static String _class_decl;
  private static String _external_class_decl;

  private static String _enum_pattern_decl;
  private static String _enum_decl;

  private static String _interface_decl;
  private static String _attr_decl[/*multiplicity*/];
  private static String _enum_pattern_item_decl;
  private static String _enum_pattern_item_case;
  private static String _enum_item_decl;

  private static String[] _rel_decl;
  private static String _oper_def;
  private static aVisibility _get_visibility;
  private static String _get_name;
  private static boolean _is_get_final;
  private static aVisibility _set_visibility;
  private static String _set_name;
  private static boolean _is_set_final;
  private static boolean _is_set_param_final;
  private static String _src_content;
  private static String _ext;
  private static String _dir_regexp;

  private static boolean _dir_regexp_case_sensitive;

  private static String _file_regexp;

  private static boolean _file_regexp_case_sensitive;

  private static boolean _is_generate_javadoc_comment;

  private static boolean _is_force_package_gen;

  @SuppressWarnings("rawtypes")
private static Hashtable _map_imports;
  @SuppressWarnings({ "rawtypes", "unchecked" })
protected static void read_()
  {
    _root = UmlCom.read_string();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1) {
      UmlSettings._builtins[index].java = UmlCom.read_string();
    }
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._relation_attribute_stereotypes[index].java = UmlCom.read_string();
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._class_stereotypes[index].java = UmlCom.read_string();
    
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
    _enum_pattern_decl = UmlCom.read_string();
    _interface_decl = UmlCom.read_string();
    _attr_decl = new String[3];
    for (index = 0; index != 3; index += 1)
      _attr_decl[index] = UmlCom.read_string();
    _enum_item_decl = UmlCom.read_string();
    _enum_pattern_item_decl = UmlCom.read_string();
    _enum_pattern_item_case = UmlCom.read_string();
    _rel_decl = new String[3];
    for (index = 0; index != 3; index += 1)
      _rel_decl[index] = UmlCom.read_string();
    _oper_def = UmlCom.read_string();
    _get_visibility = aVisibility.fromInt(UmlCom.read_char());
    _get_name = UmlCom.read_string();
    _is_get_final = UmlCom.read_bool();
    _set_visibility = aVisibility.fromInt(UmlCom.read_char());
    _set_name = UmlCom.read_string();
    _is_set_final = UmlCom.read_bool();
    _is_set_param_final = UmlCom.read_bool();
    _is_generate_javadoc_comment = UmlCom.read_bool();
    _is_force_package_gen = UmlCom.read_bool();
  
    _dir_regexp = UmlCom.read_string();
    _dir_regexp_case_sensitive = UmlCom.read_bool();
  
    _file_regexp = UmlCom.read_string();
    _file_regexp_case_sensitive = UmlCom.read_bool();
  }

  protected static void read_if_needed_()
  {
    UmlSettings.read_if_needed_();
    if (!_defined) {
      UmlCom.send_cmd(CmdFamily.javaSettingsCmd, JavaSettingsCmd._getJavaSettingsCmd);
      read_();
      _defined = true;
    }
  }

};
