package fr.bouml;


/**
 *  This class manages settings concerning PHP, configured through
 *  the 'Generation settings' dialog.
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static 
 *  members allowing to just write 'PhpSettings::member' rather than
 *  'PhpSettings::instance()->member' or other long sentence like this.
 */
class PhpSettings extends UmlSettings {
  /**
   *  returns TRUE when the created Php objects are initialized
   *  with the default declaration/definition
   */
  public static boolean useDefaults()
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._getPhpUseDefaultsCmd);
    return UmlCom.read_bool();
  }

  /**
   *  if y is TRUE the future created Php objects will be initialized
   *  with the default declaration/definition
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UseDefaults(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpUseDefaultsCmd,
  		  (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  }

  /**
   *  returns the PHP stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String classStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
    
    return (b != null) ? b.php : s;
  }

  /**
   *  set the PHP stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ClassStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpClassStereotypeCmd, s, v);
    UmlCom.check();
  
    UmlStereotype st = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
  
    if (st == null)
      st = UmlSettings.add_class_stereotype(s);
    st.php = v;
  }

  /**
   *  reverse of the ClassStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the PHP one given in argument
   */
  public static String classUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _class_stereotypes.length;
    
    while (index-- != 0)
      if (_class_stereotypes[index].php.equals(s))
        return _class_stereotypes[index].uml;
    
    return null;
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpRootdirCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpSourceContentCmd, v);
    UmlCom.check();
    
    _src_content = v;
  
  }

  /**
   *  returns the extension of the file produced by the PHP code generator
   */
  public static String sourceExtension()
  {
    read_if_needed_();
    
    return _ext; 
  }

  /**
   *  set the extension of the file produced by the PHP code generator
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceExtension(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpSourceExtensionCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpDirRevFilterCmd, s, cs);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpFileRevFilterCmd, s, cs);
    UmlCom.check();
    _file_regexp = s;
    _file_regexp_case_sensitive = cs;
  }

  /**
   *  indicates to the code generator if the require_once may specify
   *  the path of just the file's name
   */
  public static boolean requireOnceWithPath()
  {
    read_if_needed_();
  
    return _req_with_path;
  }

  /**
   *  to indicates to the code generator if the require_once may specify
   *  the path of just the file's name
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RequireOnceWithPath(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpRequireOnceWithPathCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _req_with_path = v;
  }

  /**
   *  return if a relative path must be used when the path
   *  must be generated in the produced require_once
   */
  public static boolean isRelativePath()
  {
    read_if_needed_();
  
    return _is_relative_path;
  }

  /**
   *  set if a relative path must be used when the path
   *  must be generated in the produced require_once
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsRelativePath(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpRelativePathCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_relative_path = v;
    if (v) _is_root_relative_path = false;
  }

  /**
   *  return if a path relative to the project root must be used
   *  when the path must be generated in the produced require_once
   */
  public static boolean isRootRelativePath()
  {
    read_if_needed_();
  
    return _is_root_relative_path;
  }

  /**
   *  set if a relative to the project root path must be used
   *  when the path must be generated in the produced require_once
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsRootRelativePath(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpRootRelativePathCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_root_relative_path = v;
    if (v) _is_relative_path = false;
  }

  /**
   *  return if the namespace prefix must be
   *  always generated before class's names
   */
  public static boolean isForceNamespacePrefixGeneration()
  {
    read_if_needed_();
  
    return _is_force_namespace_gen;
  }

  /**
   *  set if the namespace prefix must be always generated before class's names
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsForceNamespacePrefixGeneration(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpForceNamespaceGenCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_force_namespace_gen = v;
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpJavadocStyleCmd,
  		   (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    _is_generate_javadoc_comment = v;
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpClassDeclCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpExternalClassDeclCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpEnumDeclCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpInterfaceDeclCmd, v);
    UmlCom.check();
    
    _interface_decl = v;
  
  }

  /**
   *  returns the default definition of a trait
   */
  public static String traitDecl()
  {
    read_if_needed_();
  
    return _trait_decl;
  }

  /**
   *  set the default definition of a trait
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_TraitDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpTraitDeclCmd, v);
    UmlCom.check();
  
    _trait_decl = v;
  }

  /**
   *  returns the default definition of an attribute
   */
  public static String attributeDecl()
  {
    read_if_needed_();
  
    return _attr_decl;
  }

  /**
   *  set the default definition of an attribute
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_AttributeDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpAttributeDeclCmd, v);
    UmlCom.check();
  
    _attr_decl = v;
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpEnumItemDeclCmd, v);
    UmlCom.check();
      
    _enum_item_decl = v;
  }

  /**
   *  returns the default definition of an relation
   */
  public static String relationDecl()
  {
    read_if_needed_();
  
    return _rel_decl;
  }

  /**
   *  set the default definition of an relation
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpRelationDeclCmd, v);
    UmlCom.check();
  
    _rel_decl = v;
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpOperationDefCmd, v);
    UmlCom.check();
    
    _oper_def = v;
  
  }

  /**
   *  returns TRUE if the parameters are typed by default
   */
  public static boolean isParametersTyped()
  {
    read_if_needed_();
  
    return _is_param_typed;
  }

  /**
   *  to set if parameters are typed by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsParametersTyped(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpParametersTypedCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_param_typed = y;
  }

  /**
   *  returns TRUE if out/inout parameters are given by reference
   */
  public static boolean isOutInoutParametersByReference()
  {
    read_if_needed_();
  
    return _is_out_inout_by_ref;
  }

  /**
   *  to set if out/inout parameters are given by reference
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsOutInoutParametersByReference(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpOutInoutParametersByReferenceCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_out_inout_by_ref = y;
  }

  /**
   *  returns the default visibility of a 'get' operation generated
   *  through the attribute and relation 'add get operation'
   * 
   *  note : visibility shared with Java
   */
  public static aVisibility getVisibility()
  {
    return JavaSettings.getVisibility();
  }

  /**
   *  set the default visibility of a 'get' operation generated
   *  through the attribute and relation 'add get operation'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   * 
   *  note : visibility shared with Java
   */
  public static void set_GetVisibility(aVisibility v) throws RuntimeException
  {
    JavaSettings.set_GetVisibility(v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpGetNameCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpSetNameCmd, v);
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpIsGetFinalCmd,
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
    UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._setPhpIsSetFinalCmd,
  		  (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _is_set_final = v;
  }

  /**
   *  returns the default visibility of a 'set' operation generated
   *  through the attribute and relation 'add set operation'
   * 
   *    note : visibility shared with Java
   */
  public static aVisibility setVisibility()
  {
    return JavaSettings.setVisibility();
  }

  /**
   *  set the default visibility of a 'set' operation generated
   *  through the attribute and relation 'add set operation'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   * 
   *    note : visibility shared with Java
   */
  public static void set_SetVisibility(aVisibility v) throws RuntimeException
  {
    JavaSettings.set_SetVisibility(v);
  }

  /**
   *  never called !
   */
  private PhpSettings(){
  }

  private static boolean _defined;
  private static String _root;
  private static String _class_decl;
  private static String _external_class_decl;

  private static String _enum_decl;

  private static String _interface_decl;
  private static String _trait_decl;

  private static String _attr_decl;

  private static String _enum_item_decl;

  private static String _rel_decl;

  private static String _oper_def;
  private static String _get_name;
  private static boolean _is_get_final;
  private static String _set_name;
  private static boolean _is_set_final;
  private static String _src_content;
  private static String _ext;
  private static String _dir_regexp;

  private static boolean _dir_regexp_case_sensitive;

  private static String _file_regexp;

  private static boolean _file_regexp_case_sensitive;

  private static boolean _is_generate_javadoc_comment;

  private static boolean _req_with_path;

  private static boolean _is_relative_path;

  private static boolean _is_root_relative_path;

  private static boolean _is_force_namespace_gen;

  private static boolean _is_param_typed;

  private static boolean _is_out_inout_by_ref;

  protected static void read_()
  {
    _root = UmlCom.read_string();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._class_stereotypes[index].php = UmlCom.read_string();
    
    _src_content = UmlCom.read_string();
    _ext = UmlCom.read_string();
  
    _class_decl = UmlCom.read_string();
    _external_class_decl = UmlCom.read_string();
    _enum_decl = UmlCom.read_string();
    _interface_decl = UmlCom.read_string();
    _attr_decl = UmlCom.read_string();
    _enum_item_decl = UmlCom.read_string();
    _rel_decl = UmlCom.read_string();
    _oper_def = UmlCom.read_string();
    UmlCom.read_char(); // getter visibility
    _get_name = UmlCom.read_string();
    _is_get_final = UmlCom.read_bool();
    UmlCom.read_char(); // setter visibility
    _set_name = UmlCom.read_string();
    _is_set_final = UmlCom.read_bool();
    _is_generate_javadoc_comment = UmlCom.read_bool();
    _req_with_path = UmlCom.read_bool();
    _is_relative_path = UmlCom.read_bool();
    _is_root_relative_path = UmlCom.read_bool();
  
    _dir_regexp = UmlCom.read_string();
    _dir_regexp_case_sensitive = UmlCom.read_bool();
  
    _file_regexp = UmlCom.read_string();
    _file_regexp_case_sensitive = UmlCom.read_bool();
  
    _is_force_namespace_gen = UmlCom.read_bool();
  
    _is_param_typed = UmlCom.read_bool();
    _is_out_inout_by_ref = UmlCom.read_bool();
  
    _trait_decl = UmlCom.read_string();
  }

  protected static void read_if_needed_()
  {
    UmlSettings.read_if_needed_();
    if (!_defined) {
      UmlCom.send_cmd(CmdFamily.phpSettingsCmd, PhpSettingsCmd._getPhpSettingsCmd);
      read_();
      _defined = true;
    }
  }

}
