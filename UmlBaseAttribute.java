
import java.util.*;

/**
 *   Manage the class's attributs
 */
abstract class UmlBaseAttribute extends UmlClassMember {
  /**
   *  returns a new attribute named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlAttribute create(UmlClass parent, String s) throws RuntimeException
  {
    return (UmlAttribute) parent.create_(anItemKind.anAttribute, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anAttribute;
  }

  /**
   *  Set if the member is a 'class member' (static)
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isClassMember(boolean y) throws RuntimeException {
    if (!y)
      _cpp_thread_local = false;
    super.set_isClassMember(y);
  }

  /**
   *  indicates if the attribute is read only, returns TRUE if yes
   */
  public boolean isReadOnly() {
    read_if_needed_();
    
    return _read_only;
  }

  /**
   *  to set the 'read only' state of the attribute
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isReadOnly(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsReadOnlyCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _read_only =  y;
  }

  /**
   *  return the property 'derived'
   */
  public boolean isDerived() {
    read_if_needed_();
    return _derived;
  }

  /**
   *  return the property 'derived union'
   */
  public boolean isDerivedUnion() {
    read_if_needed_();
    return _derived_union;
  }

  /**
   *  Set the properties 'derived' and 'union'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isDerived(boolean is_derived, boolean is_union) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDerivedCmd,
                    (byte) (((is_derived) ? 1 : 0) + ((is_union) ? 2 : 0)));
    UmlCom.check();
  
    _derived = is_derived;
    _derived_union = is_union;
  }

  /**
   *  return the property 'ordered'
   */
  public boolean isOrdered() {
    read_if_needed_();
    return _ordered;
  }

  /**
   *  set the property 'ordered'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isOrdered(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setOrderingCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _ordered = v;
  }

  /**
   *  return the property 'unique'
   */
  public boolean isUnique() {
    read_if_needed_();
    return _unique;
  }

  /**
   *  set the property 'unique'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isUnique(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setUniqueCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _unique = v;
  }

  /**
   *  returns the default attribute value, may be an empty string
   */
  public String defaultValue() {
    read_if_needed_();
    
    return _default_value;
  }

  /**
   *  to set the default attribute value ("" allowed)
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_DefaultValue(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefaultValueCmd, s);
    UmlCom.check();
  
    _default_value = s;
  }

  /**
   *  returns the attribute UML type
   */
  public UmlTypeSpec type() {
    read_if_needed_();
    
    return _type;
  }

  /**
   *  to set the attribute UML type
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Type(UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setTypeCmd, t);
    UmlCom.check();
  
    _type = t;
  }

  /**
   *  returns the multiplicity (may be an empty string)
   */
  public String multiplicity() {
    read_if_needed_();
    
    return _multiplicity;
  }

  /**
   *  to set the multiplicity
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Multiplicity(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMultiplicityCmd, s);
    UmlCom.check();
  
    _multiplicity = s;
  }

  /**
   *  returns the 'get' operation of the attribute, or 0 if it does not exist
   */
  public UmlOperation getOperation() {
    read_if_needed_();
    
    return _get_oper;
  }

  /**
   *  to generate an associated 'get' operation
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addGetOperation() throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addGetOperCmd);
    UmlCom.check();
    
    reread_children_if_needed_();
  
  }

  /**
   *  returns the 'set' operation of the attribute, or 0 if it does not exist
   */
  public UmlOperation setOperation() {
    read_if_needed_();
    
    return _set_oper;
  }

  /**
   *  to generate an associated 'set' operation
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addSetOperation() throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addSetOperCmd);
    UmlCom.check();
    
    reread_children_if_needed_();
  
  }

  /**
   *  Indicate if the attribute is 'mutable'
   */
  public boolean isCppMutable() {
    read_if_needed_();
    
    return _cpp_mutable;
  }

  /**
   *  Set if the attribute is 'mutable'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppMutable(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppMutableCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_mutable = y;
  }

  /**
   *  Indicate if the attribute is 'constexpr'
   */
  public boolean isCppConstExpr() {
    read_if_needed_();
  
    return _cpp_constexpr;
  }

  /**
   *  Set if the attribute is 'constexpr'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppConstExpr(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstExprCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_constexpr = y;
  }

  /**
   *  Indicate if the attribute is 'thread_local'
   */
  public boolean isCppThreadLocal() {
    read_if_needed_();
  
    return _cpp_thread_local;
  }

  /**
   *  Set if the attribute is 'thread_local'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppThreadLocal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppThreadLocalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_thread_local = y;
    if (y)
      set_isClassMember(true);
  }

  /**
   *  indicates if the attribute is 'transient', returns TRUE if yes
   */
  public boolean isJavaTransient() {
    read_if_needed_();
    
    return _java_transient;
  }

  /**
   *  to set the 'transient' state of the attribute
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaTransient(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsJavaTransientCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_transient = y;
  }

  /**
   *  in case the attribute is an IDL union's member returns the
   *  corresponding 'case', an empty string in case it is not specified
   */
  public String idlCase() {
    read_if_needed_();
    
    return (_idl_case != null) ? _idl_case.name() : _idl_explicit_case;
  }

  /**
   *  to set the 'case' even the attribute is not (already) known as
   *  an IDL union's member
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlCase(UmlAttribute a) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlCaseCmd, a.identifier_(), "");
    UmlCom.check();
    
    _idl_case = a;
    _idl_explicit_case = null;
  }

  /**
   *  to set the 'case' even the attribute is not (already) known as
   *  an IDL union's member
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlCase(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlCaseCmd, (long) 0, s);
    UmlCom.check();
    
    _idl_case = null;
    _idl_explicit_case = s;
  
  }

  /**
   *  in case the attribute is part of a table returns what this attribute
   *  represents, else the return value doesn't have meaning.
   */
  public aMySqlKind mysqlKind() {
    read_if_needed_();
  
    return _mysql_kind;
  }

  /**
   *  to set the meaning of the attribute part of a table.
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlKind(aMySqlKind v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlKindCmd, (byte) v.value());
    UmlCom.check();
  
    _mysql_kind = v;
  }

  /**
   *  in case the attribute is a columns part of a table indicates
   *  if it must be not null, else the return value doesn't have meaning
   */
  public boolean isMysqlNotNull() {
    read_if_needed_();
  
    return _mysql_notnull_hash;
  }

  /**
   *  to set if the columns part of a table must be not null
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMysqlNotNull(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsMysqlNotNullHashCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _mysql_notnull_hash = y;
  }

  /**
   *  in case the attribute is a columns part of a table indicates
   *  if auto increment is true, else the return value doesn't have meaning.
   */
  public boolean isMysqlAutoIncrement() {
    read_if_needed_();
  
    return _mysql_autoincr_btree;
  }

  /**
   *  to set if the columns part of a table must be in auto increment mode
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMysqlAutoIncrement(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsMysqlAutoIncrBtreeCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _mysql_autoincr_btree = y;
  }

  /**
   *  in case the attribute is a key part of a table returns the constraint,
   *  else the return value doesn't have meaning
   */
  public String mysqlConstraint() {
    read_if_needed_();
  
    return _mysql_constraint;
  }

  /**
   *  to set the constraint of the key part of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlConstraint(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlConstraintCmd, s);
    UmlCom.check();
  
    _mysql_constraint = s;
  }

  /**
   *  in case the attribute is a key part of a table indicates if it
   *  uses btree, else the return value doesn't have meaning.
   */
  public boolean isMysqlUsingBtree() {
    read_if_needed_();
  
    return _mysql_autoincr_btree;
  }

  /**
   *  to set if the key part of a table uses btree
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMysqlUsingBtree(boolean y) throws RuntimeException {
    set_isMysqlAutoIncrement(y);
  }

  /**
   *  in case the attribute is a key part of a table indicates if it
   *  uses hash, else the return value doesn't have meaning.
   */
  public boolean isMysqlUsingHash() {
    read_if_needed_();
  
    return _mysql_notnull_hash;
  }

  /**
   *  to set if the key part of a table uses hash
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMysqlUsingHash(boolean y) throws RuntimeException {
    set_isMysqlNotNull(y);
  }

  /**
   *  returns (in Java a copy of) the columns
   *  significant when the attribute is a key of a table
   */
  public UmlAttribute[] mysqlColumns() {
    read_if_needed_();
  
    return (UmlAttribute[]) _mysql_columns.clone();
  }

  /**
   *  to set the columns
   *  significant when the attribute is a key of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlColumns(UmlAttribute[] l) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlColumnsCmd, (UmlItem[]) l);
    UmlCom.check();
    _mysql_columns = (UmlAttribute[]) l.clone();
  }

  /**
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _type = null;
    _default_value = null;
    _idl_explicit_case = null;
    super.unload(rec, del);
    _multiplicity = null;
  }

  private boolean _read_only;
  private boolean _derived;

  private boolean _derived_union;

  private boolean _ordered;

  private boolean _unique;

  private boolean _cpp_mutable;

  private boolean _cpp_constexpr;

  private boolean _cpp_thread_local;

  private boolean _java_transient;

  private aMySqlKind _mysql_kind;

  private boolean _mysql_autoincr_btree;

  private boolean _mysql_notnull_hash;

  private String _multiplicity;
  private String _default_value;
  private UmlTypeSpec _type;
  private UmlOperation _get_oper;
  private UmlOperation _set_oper;
  /**
   *  exclusive with idl_explicit_case
   */
  private UmlAttribute _idl_case;
  private String _idl_explicit_case;
  private String _mysql_constraint;

  private UmlAttribute[] _mysql_columns;

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  public UmlBaseAttribute(long id, String n) { super(id, n); }

  protected void read_uml_() {
    super.read_uml_();
    _type = new UmlTypeSpec();
    _type.type = (UmlClass) UmlBaseItem.read_();
    if (_type.type == null)
      _type.explicit_type = UmlCom.read_string();
    _multiplicity = UmlCom.read_string();
    _default_value = UmlCom.read_string();
    _read_only = UmlCom.read_bool();
    _derived = UmlCom.read_bool();
    _derived_union = UmlCom.read_bool();
    _ordered = UmlCom.read_bool();
    _unique = UmlCom.read_bool();
    _get_oper = (UmlOperation) UmlBaseItem.read_();
    _set_oper = (UmlOperation) UmlBaseItem.read_();
  }

  protected void read_cpp_() {
    super.read_cpp_();
    _cpp_mutable = UmlCom.read_bool();
    _cpp_constexpr = UmlCom.read_bool();
    _cpp_thread_local = UmlCom.read_bool();
  }

  protected void read_java_() {
    super.read_java_();
    _java_transient = UmlCom.read_bool();
  }

  protected void read_php_() {
    super.read_php_();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
    super.read_python_();
  }

  protected void read_idl_() {
    super.read_idl_();
    _idl_case = (UmlAttribute) UmlBaseItem.read_();
    if (_idl_case == null)
      _idl_explicit_case = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_mysql_() {
    super.read_mysql_();
    
    _mysql_constraint = UmlCom.read_string();
    _mysql_kind = aMySqlKind.fromInt(UmlCom.read_char());
    _mysql_autoincr_btree = UmlCom.read_bool();
    _mysql_notnull_hash = UmlCom.read_bool();
  
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    _mysql_columns = new UmlAttribute[n];
      
    for (index = 0; index != n; index += 1)
      _mysql_columns[index] = (UmlAttribute) UmlBaseItem.read_();
  }

};
