
import java.util.*;
/**
 *  This class manages settings concerning IDL, configured through
 *  the 'Generation settings' dialog.
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static 
 *  members allowing to just write 'IdlSettings::member' rather than
 *  'IdlSettings::instance()->member' or other long sentence like this.
 */
final class IdlSettings extends UmlSettings {
  /**
   *  returns TRUE when the created Java objects are initialized
   *  with the default declaration/definition
   */
  public static boolean useDefaults()
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._getIdlUseDefaultsCmd);
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlUseDefaultsCmd, 
  		  (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
  }

  /**
   *  returns the IDL type corresponding to the 'UML' type given in
   *  argument, as it is configured in the first 'Generation settings'
   *  dialog's tab
   */
  public static String type(String s)
  {
    read_if_needed_();
    
    UmlBuiltin b = (UmlBuiltin) UmlSettings._map_builtins.get(s);
    
    return (b != null) ? b.idl : s;
  }

  /**
   *  set the IDL type corresponding to the 'UML' type given in
   *  argument, as it is configured in the first 'Generation settings'
   *  dialog's tab
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_Type(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlTypeCmd, s, v);
    UmlCom.check();
    
    UmlBuiltin b = (UmlBuiltin) UmlSettings._map_builtins.get(s);
    
    if (b == null)
      b = UmlSettings.add_type(s);
    b.idl = v;
  
  }

  /**
   *  reverse of the Type() operation, returns the 'UML' type corresponding
   *  to the IDL type given in argument
   */
  public static String umlType(String s)
  {
    read_if_needed_();
    
    int index = _builtins.length;
    
    while (index-- != 0)
      if (_builtins[index].idl.equals(s))
        return _builtins[index].uml;
    
    return null;
  
  }

  /**
   *  returns the IDL stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String relationAttributeStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_relation_attribute_stereotypes.get(s);
    
    return (b != null) ? b.idl : s;
  }

  /**
   *  set the IDL stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RelationAttributeStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
   UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlRelationAttributeStereotypeCmd, s, v);
   UmlCom.check();
   
   UmlStereotype st = (UmlStereotype) _map_relation_attribute_stereotypes.get(s);
   
   if (st == null)
     st = add_rel_attr_stereotype(s);
   st.idl = v;
  
  }

  /**
   *  reverse of the RelationAttributeStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the IDL one given in argument
   */
  public static String relationAttributeUmlStereotype(String s)
  {
  
    read_if_needed_();
    
    int index = _relation_attribute_stereotypes.length;
    
    while (index-- != 0)
      if (_relation_attribute_stereotypes[index].idl.equals(s))
        return _relation_attribute_stereotypes[index].uml;
    
    return null;
  }

  /**
   *  returns the IDL stereotype corresponding to the 'UML' stereotype given
   *  in argument
   */
  public static String classStereotype(String s)
  {
    read_if_needed_();
    
    UmlStereotype b = (UmlStereotype) UmlSettings._map_class_stereotypes.get(s);
    
    return (b != null) ? b.idl : s;
  }

  /**
   *  set the IDL stereotype corresponding to the 'UML' stereotype given
   *  in argument
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ClassStereotype(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlClassStereotypeCmd, s, v);
    UmlCom.check();
    
    UmlStereotype st = (UmlStereotype) _map_class_stereotypes.get(s);
  
    if (st == null)
      st = add_class_stereotype(s);
    st.idl = v;
  
  }

  /**
   *  reverse of the ClassStereotype() operation, returns the 'UML' 
   *  stereotype corresponding to the IDL one given in argument
   */
  public static String classUmlStereotype(String s)
  {
    read_if_needed_();
    
    int index = _class_stereotypes.length;
    
    while (index-- != 0)
      if (_class_stereotypes[index].idl.equals(s))
        return _class_stereotypes[index].uml;
    
    return null;
  
  }

  /**
   *  returns the #include or other form specified in the last 
   *  'Generation settings' tab for the Idl type given in argument.
   */
  public static String include(String s)
  {
    read_if_needed_();
    
    return (String) _map_includes.get(s);
  }

  /**
   *  set the #include or other form specified in the last 
   *  'Generation settings' tab for the Idl type given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_Include(String s, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlIncludeCmd, s, v);
    UmlCom.check();
    
    if ((v != null) && (v.length() != 0))
      _map_includes.put(s, v);
    else
      _map_includes.remove(s);
  }

  /**
   *  returns the 'root' directory 
   */
  public static String rootDir()
  {
    read_if_needed_();
    
    return _root;
  }

  /**
   *  set the 'root' directory 
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_RootDir(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlRootDirCmd, v);
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlSourceContentCmd, v);
    UmlCom.check();
    
    _src_content = v;
  
  }

  /**
   *  returns the extension of the files produced by the Idl code generator
   */
  public static String sourceExtension()
  {
    read_if_needed_();
    
    return _ext; 
  }

  /**
   *  set the extension of the files produced by the Idl code generator
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceExtension(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlSourceExtensionCmd, v);
    UmlCom.check();
    
    _ext = v;
  
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlInterfaceDeclCmd, v);
    UmlCom.check();
    
    _interface_decl = v;
  }

  /**
   *  returns the default definition of a valuetype
   */
  public static String valuetypeDecl()
  {
    read_if_needed_();
    
    return _valuetype_decl;
  }

  /**
   *  set the default definition of a valuetype
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ValuetypeDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlValuetypeDeclCmd, v);
    UmlCom.check();
    
    _valuetype_decl = v;
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlExternalClassDeclCmd, v);
    UmlCom.check();
    
    _external_class_decl = v;
  }

  /**
   *  returns the default definition of a struct
   */
  public static String structDecl()
  {
    read_if_needed_();
    
    return _struct_decl;
  }

  /**
   *  set the default definition of a struct
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_StructDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlStructDeclCmd, v);
    UmlCom.check();
    
    _struct_decl = v;
  
  }

  /**
   *  returns the default definition of a typedef
   */
  public static String typedefDecl()
  {
    read_if_needed_();
    
    return _typedef_decl;
  }

  /**
   *  set the default definition of a typedef
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_TypedefDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlTypedefDeclCmd, v);
    UmlCom.check();
    
    _typedef_decl = v;
  
  }

  /**
   *  returns the default definition of an exception
   */
  public static String exceptionDecl()
  {
    read_if_needed_();
    
    return _exception_decl;
  }

  /**
   *  set the default definition of an exception
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ExceptionDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlExceptionDeclCmd, v);
    UmlCom.check();
    
    _exception_decl = v;
  
  }

  /**
   *  returns the default definition of an union
   */
  public static String unionDecl()
  {
    read_if_needed_();
    
    return _union_decl;
  }

  /**
   *  set the default definition of an union
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UnionDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlUnionDeclCmd, v);
    UmlCom.check();
    
    _union_decl = v;
  
  }

  /**
   *  returns the default definition of an enum
   */
  public static String enumDecl()
  {
    read_if_needed_();
    
    return _enum_decl;
  }

  /**
   *  set the default definition of an enum
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_EnumDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlEnumDeclCmd, v);
    UmlCom.check();
    
    _enum_decl = v;
  
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlAttributeDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _attr_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  }

  /**
   *  returns the default definition of an attribute
   *  placed in a valuetype depending on the multiplicity
   */
  public static String valuetypeAttributeDecl(String multiplicity)
  {
    read_if_needed_();
  
    return _valuetype_attr_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of an attribute
   *  placed in a valuetype
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ValuetypeAttributeDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlValuetypeAttributeDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _valuetype_attr_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  }

  /**
   *  returns the default definition of an union item depending on the multiplicity
   */
  public static String unionItemDecl(String multiplicity)
  {
    read_if_needed_();
  
    return _union_item_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of an union item
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UnionItemDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlUnionItemDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _union_item_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlEnumItemDeclCmd, v);
    UmlCom.check();
    
    _enum_item_decl = v;
  
  }

  /**
   *  returns the default definition of a constant attribute depending on the multiplicity
   */
  public static String constDecl(String multiplicity)
  {
    read_if_needed_();
  
    return _const_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of a constant attribute
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ConstDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlConstDeclCmd, multiplicity, v);
    UmlCom.check();
  
    _const_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlRelationDeclCmd, multiplicity, v);
    UmlCom.check();
    
    _rel_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  
  }

  /**
   *  returns the default definition of a relation in a valuetype
   *  depending on the multiplicity given in argument.
   */
  public static String valuetypeRelationDecl(String multiplicity)
  {
    read_if_needed_();
    
    return _valuetype_rel_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of a relation in a valuetype
   *  depending on the multiplicity given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ValuetypeRelationDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlValuetypeRelationDeclCmd, multiplicity, v);
    UmlCom.check();
    
    _valuetype_rel_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  }

  /**
   *  returns the default definition of a relation in an union
   *  depending on the multiplicity given in argument.
   */
  public static String unionRelationDecl(String multiplicity)
  {
    read_if_needed_();
    
    return _union_rel_decl[UmlSettings.multiplicity_column(multiplicity)];
  }

  /**
   *  set the default definition of a relation in an union
   *  depending on the multiplicity given in argument.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UnionRelationDecl(String multiplicity, String v) throws RuntimeException
  {
    read_if_needed_();
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlUnionRelationDeclCmd, multiplicity, v);
    UmlCom.check();
    
    _union_rel_decl[UmlSettings.multiplicity_column(multiplicity)] = v;
  
  }

  /**
   *  returns the default declaration of an operation
   */
  public static String operationDecl()
  {
    read_if_needed_();
    
    return _oper_decl;
  }

  /**
   *  set the default declaration of an operation
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_OperationDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlOperationDeclCmd, v);
    UmlCom.check();
  
    _oper_decl = v;
  
  }

  /**
   *  returns the default name of a 'get' operation generated through
   *  the attribute and relation 'add get operation' menu
   */
  public static String getName()
  {
    read_if_needed_();
    
    return _get_name;
  }

  /**
   *  set the default name of a 'get' operation generated through
   *  the attribute and relation 'add get operation' menu
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_GetName(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlGetNameCmd, v);
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
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlSetNameCmd, v);
    UmlCom.check();
    
    _set_name = v;
  
  }

  /**
   *  returns if a 'set' operation generated through the attribute
   *  and relation 'add set operation' menu is oneway by default
   */
  public static boolean isSetOneway()
  {
    read_if_needed_();
    
    return _is_set_oneway;
  }

  /**
   *  set if a 'set' operation generated through the attribute
   *  and relation 'add set operation' menu is oneway by default
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_IsSetOneway(boolean v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._setIdlIsSetOneWayCmd,
  		  (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
    
    _is_set_oneway = v;
  
  }

  /**
   *  never called !
   */
  private IdlSettings(){
  }

  private static boolean _defined;
  private static String _root;
  private static String _interface_decl;
  private static String _valuetype_decl;
  private static String _struct_decl;
  private static String _typedef_decl;
  private static String _exception_decl;
  private static String _union_decl;
  private static String _enum_decl;
  private static String _external_class_decl;

  private static String _attr_decl[/*multiplicity*/];
  private static String _valuetype_attr_decl[/*multiplicity*/];
  private static String _union_item_decl[/*multiplicity*/];
  private static String _enum_item_decl;
  private static String _const_decl[/*multiplicity*/];
  private static String[] _rel_decl;
  private static String[] _valuetype_rel_decl;
  private static String[] _union_rel_decl;
  private static String _oper_decl;
  private static String _get_name;
  private static String _set_name;
  private static boolean _is_set_oneway;
  private static String _src_content;
  private static String _ext;
  private static Hashtable _map_includes;
  protected static void read_()
  {
    _root = UmlCom.read_string();
    
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1) {
      UmlSettings._builtins[index].idl = UmlCom.read_string();
    }
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._relation_attribute_stereotypes[index].idl = UmlCom.read_string();
    
    n = UmlCom.read_unsigned();
    
    for (index = 0; index != n; index += 1)
      UmlSettings._class_stereotypes[index].idl = UmlCom.read_string();
    
    n = UmlCom.read_unsigned();
    _map_includes = new Hashtable((n == 0) ? 1 : n);
    
    for (index = 0; index != n; index += 1) {
      String t = UmlCom.read_string();
      String i = UmlCom.read_string();
      
      _map_includes.put(t, i);
    }
    
    _src_content = UmlCom.read_string();
    _ext = UmlCom.read_string();
    
    _interface_decl = UmlCom.read_string();
    _valuetype_decl = UmlCom.read_string();
    _struct_decl = UmlCom.read_string();
    _typedef_decl = UmlCom.read_string();
    _exception_decl = UmlCom.read_string();
    _union_decl = UmlCom.read_string();
    _enum_decl = UmlCom.read_string();
    _external_class_decl = UmlCom.read_string();
    
    _enum_item_decl = UmlCom.read_string();
    _attr_decl = new String[3];
    _valuetype_attr_decl = new String[3];
    _union_item_decl = new String[3];
    _const_decl = new String[3];
    _rel_decl = new String[3];
    _valuetype_rel_decl = new String[3];
    _union_rel_decl = new String[3];
    for (index = 0; index != 3; index += 1) {
      _attr_decl[index] = UmlCom.read_string();
      _valuetype_attr_decl[index] = UmlCom.read_string();
      _union_item_decl[index] = UmlCom.read_string();
      _const_decl[index] = UmlCom.read_string();
      _rel_decl[index] = UmlCom.read_string();
      _valuetype_rel_decl[index] = UmlCom.read_string();
      _union_rel_decl[index] = UmlCom.read_string();
    }
    
    _oper_decl = UmlCom.read_string();
    _get_name = UmlCom.read_string();
    _set_name = UmlCom.read_string();
    _is_set_oneway = UmlCom.read_bool();
  }

  protected static void read_if_needed_()
  {
    UmlSettings.read_if_needed_();
    if (!_defined) {
      UmlCom.send_cmd(CmdFamily.idlSettingsCmd, IdlSettingsCmd._getIdlSettingsCmd);
      read_();
      _defined = true;
    }
  }

};
