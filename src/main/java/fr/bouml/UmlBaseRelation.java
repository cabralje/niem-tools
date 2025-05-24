package fr.bouml;


import java.util.*;

/**
 *  Manage the relations between classes
 */
@SuppressWarnings("unused")
abstract class UmlBaseRelation extends UmlClassMember {
  /**
   *  returns a new relation of the given 'kind' from 'start' to 'end'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlRelation create(aRelationKind kind, UmlClass start, UmlClass end) throws RuntimeException
  {
    UmlCom.send_cmd(start.identifier_(), OnInstanceCmd.createCmd, anItemKind.aRelation,
  		  kind, end.identifier_());
    UmlRelation result = (UmlRelation) UmlBaseItem.read_();
    
    if (result != null) {
      start.reread_children_if_needed_();
      
      switch (kind.value()) {
      case aRelationKind._anAssociation:
      case aRelationKind._anAggregation:
      case aRelationKind._anAggregationByValue:
        // bi-directional
        end.reread_children_if_needed_();
        break;
      case aRelationKind._aGeneralisation:
      case aRelationKind._aRealization:
        if (end.formals().length != 0)
  	// to update actuals & base_type
  	start.reread_if_needed_();
      default:
        break;
      }
    }
    else
      throw new RuntimeException("can't be created");
    
    return result;
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aRelation;
  }

  /**
   *  returns the kind of the relation
   */
  public aRelationKind relationKind() {
    read_if_needed_();
    
    return _rel_kind;
  }

  /**
   *  if 'first' is true returns the relation associated to the
   *  first role, else the relation associated to the second
   *  role or 0/null if the relation is uni directional
   */
  public UmlRelation side(boolean first) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.sideCmd, 
  		  (first) ? (byte) 1 : (byte) 0);
    
    return (UmlRelation) UmlBaseItem.read_();
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
   *  indicates if the relation is read only, returns TRUE if yes
   */
  public boolean isReadOnly() {
    read_if_needed_();
    
    return _read_only;
  }

  /**
   *  to set the 'read only' state of the relation
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isReadOnly(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsReadOnlyCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _read_only = y;
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
   *  returns the default relation value, may be an empty string
   */
  public String defaultValue() {
    read_if_needed_();
    
    return _default_value;
  }

  /**
   *  to set the default relation value ("" allowed)
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_DefaultValue(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefaultValueCmd, s);
    UmlCom.check();
  
    _default_value = s;
  }

  /**
   *  to set the stereotype
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   *  redefined in case the relation is bidirectional to set the stereotype
   *  of the relation corresponding to the other direction
   */
  public void set_Stereotype(String s) throws RuntimeException {
    super.set_Stereotype(s);
    
    UmlBaseRelation other = (UmlBaseRelation) UmlBaseItem.read_();
    
    if (other != null)
      other._stereotype = s;
  
  }

  /**
   *  returns the 'end' class (the 'start' class is the parent of the relation) no set !
   */
  public UmlClass roleType() {
    read_if_needed_();
    
    return _role_type;
  }

  /**
   *  return the associated class/type, may be an empty spec
   */
  public UmlTypeSpec association() {
    read_if_needed_();
    
    return _association;
  }

  /**
   *  set the associated class/type, may be an empty spec
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Association(UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setRelationAssocClassCmd, t);
    UmlCom.check();
    
    _association = t;
    UmlBaseRelation other = (UmlBaseRelation) UmlBaseItem.read_();
    
    if (other != null)
      other._association = t;
  }

  /**
   *  returns the name of the role
   */
  public String roleName() {
    read_if_needed_();
    
    return _role_name;
  }

  /**
   *  to set the name of the role
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_RoleName(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setRoleNameCmd, s);
    UmlCom.check();
  
    _role_name = s;
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
   *  returns the 'get' operation of the relation, or 0 if it does not exist
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
   *  returns the 'set' operation of the relation, or 0 if it does not exist
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
   *  indicates if the inheritance is virtual in C++, returns TRUE if yes
   */
  public boolean cppVirtualInheritance() {
    read_if_needed_();
    
    return _cpp_virtual_inheritance;
  }

  /**
   *  to set if the inheritance is virtual in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppVirtualInheritance(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppVirtualInheritanceCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_virtual_inheritance =  y;
  }

  /**
   *  Indicate if the relation is 'mutable'
   */
  public boolean isCppMutable() {
    read_if_needed_();
    
    return _cpp_mutable;
  }

  /**
   *  Set if the relation is 'mutable'
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppMutable(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppMutableCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_mutable = y;
  }

  /**
   *  Indicate if the relation is 'constexpr'
   */
  public boolean isCppConstExpr() {
    read_if_needed_();
  
    return _cpp_constexpr;
  }

  /**
   *  Set if the relation is 'constexpr'
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppConstExpr(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstExprCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_constexpr = y;
  }

  /**
   *  Indicate if the relation is 'thread_local'
   */
  public boolean isCppThreadLocal() {
    read_if_needed_();
  
    return _cpp_thread_local;
  }

  /**
   *  Set if the relation is 'thread_local'
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
   *  indicates if the relation is 'transient', returns TRUE if yes
   */
  public boolean isJavaTransient() {
    read_if_needed_();
    
    return _java_transient;
  }

  /**
   *  to set the 'transient' state of the relation
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaTransient(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsJavaTransientCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_transient = y;
  }

  /**
   *  indicates if the relation is 'truncatable', returns TRUE if yes
   *  have sense only for a valuetype inheritance
   */
  public boolean isIdlTruncatableInheritance() {
    read_if_needed_();
    
    return _idl_truncatable;
  }

  /**
   *  to set if the inheritance is 'truncatable'
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isIdlTruncatableInheritance(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsIdlTruncatableCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _idl_truncatable = y;
  }

  /**
   *  in case the relation is an IDL union's member returns the
   *  corresponding 'case', an empty string in case it is not specified
   */
  public String idlCase() {
    read_if_needed_();
    
    return (_idl_case != null) ? _idl_case.name() : _idl_explicit_case;
  }

  /**
   *  to set the 'case' even the relation is not (already) known as
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
   *  to set the 'case' even the relation is not (already) known as
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
   *  in case the relation is a foreign key part of a table returns
   *  the constraint, else the return value doesn't have meaning.
   */
  public String mysqlConstraint() {
    read_if_needed_();
  
    return _mysql_constraint;
  }

  /**
   *  to set the constraint of the foreing key part of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlConstraint(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlConstraintCmd, s);
    UmlCom.check();
  
    _mysql_constraint = s;
  }

  /**
   *  returns (in Java a copy of) the columns
   *  significant when the relation is a foreign key of a table
   */
  public UmlAttribute[] mysqlColumns() {
    read_if_needed_();
  
    return (UmlAttribute[]) _mysql_columns.clone();
  }

  /**
   *  to set the columns
   *  significant when the relation is a foreign key of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlColumns(UmlAttribute[] l) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlColumnsCmd, (UmlItem[]) l);
    UmlCom.check();
    _mysql_columns = (UmlAttribute[]) l.clone();
  }

  /**
   *  returns the key referenced by the foreing key or a null pointer
   *  significant when the relation is a foreign key of a table
   */
  public UmlAttribute mysqlReferencedKey() {
    read_if_needed_();
  
    return _mysql_ref_key;
  }

  /**
   *  set the key referenced by the foreing key or a null pointer
   *  significant when the relation is a foreign key of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlReferencedKey(UmlAttribute k) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlReferencedKeyCmd, (k != null) ? k.identifier_() : 0);
    UmlCom.check();
  
    _mysql_ref_key = k;
  }

  /**
   *  returns what is done on delete
   *  significant when the relation is a foreign key of a table
   */
  public aMySqlRefOption mysqlOnDelete() {
    read_if_needed_();
  
    return _mysql_on_delete;
  }

  /**
   *  set what is done on delete
   *  significant when the relation is a foreign key of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlOnDelete(aMySqlRefOption v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlOnDeleteCmd, (byte) v.value());
    UmlCom.check();
  
    _mysql_on_delete = v;
  }

  /**
   *  returns what is done on update
   *  significant when the relation is a foreign key of a table
   */
  public aMySqlRefOption mysqlOnUpdate() {
    read_if_needed_();
  
    return _mysql_on_update;
  }

  /**
   *  set what is done on update
   *  significant when the relation is a foreign key of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_MysqlOnUpdate(aMySqlRefOption v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMysqlOnUpdateCmd, (byte) v.value());
    UmlCom.check();
  
    _mysql_on_update = v;
  }

  /**
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _role_name = null;
    _multiplicity = null;
    _default_value = null;
    _idl_explicit_case = null;
    super.unload(rec, del);
  }

  private aRelationKind _rel_kind;
  private aMySqlRefOption _mysql_on_delete;

  private aMySqlRefOption _mysql_on_update;

  private boolean _read_only;
  private boolean _derived;

  private boolean _derived_union;

  private boolean _ordered;

  private boolean _unique;

  private boolean _cpp_virtual_inheritance;
  private boolean _cpp_mutable;

  private boolean _cpp_constexpr;

  private boolean _cpp_thread_local;

  private boolean _java_transient;

  private boolean _idl_truncatable;

  private String _default_value;
  private UmlClass _role_type;
  private String _role_name;
  private String _multiplicity;
  private UmlTypeSpec _association;

  private UmlOperation _get_oper;
  private UmlOperation _set_oper;
  /**
   *  exclusive with idl_explicit_case
   */
  private UmlAttribute _idl_case;
  private String _idl_explicit_case;
  private String _mysql_constraint;

  private UmlAttribute _mysql_ref_key;

  private UmlAttribute[] _mysql_columns;

  protected void read_uml_() {
    super.read_uml_();
    _rel_kind = aRelationKind.fromInt(UmlCom.read_char());
    _role_type = (UmlClass) UmlBaseItem.read_();
    _association = new UmlTypeSpec();
    _association.type = (UmlClass) UmlBaseItem.read_();
    if (_association.type == null)
      _association.explicit_type = UmlCom.read_string();
    _role_name = UmlCom.read_string();
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
    _cpp_virtual_inheritance = UmlCom.read_bool();
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
    _idl_truncatable = UmlCom.read_bool();
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
    _mysql_on_delete = aMySqlRefOption.fromInt(UmlCom.read_char());
    _mysql_on_update = aMySqlRefOption.fromInt(UmlCom.read_char());
    _mysql_ref_key = (UmlAttribute) UmlBaseItem.read_();
  
    int n;
    int index;
    
    n = UmlCom.read_unsigned();
    _mysql_columns = new UmlAttribute[n];
      
    for (index = 0; index != n; index += 1)
      _mysql_columns[index] = (UmlAttribute) UmlBaseItem.read_();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseRelation(long id, String n) { super(id, n); }

};
