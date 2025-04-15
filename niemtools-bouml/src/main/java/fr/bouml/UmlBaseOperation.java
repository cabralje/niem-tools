package fr.bouml;


/**
 *   Manage the class's operations
 */
abstract class UmlBaseOperation extends UmlClassMember {
  /**
   *  returns a new operation named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlOperation create(UmlClass parent, String s) throws RuntimeException
  {
    return (UmlOperation) parent.create_(anItemKind.anOperation, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anOperation;
  }

  /**
   *  indicates if the body is generated even if preserve body is set, returns TRUE if yes
   */
  public boolean isBodyGenerationForced() {
    read_if_needed_();
    return _force_body_generation;
  }

  /**
   *  to set if the body is generated even if preserve body is set
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isBodyGenerationForced(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsForceBodyGenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _force_body_generation = v;
  }

  /**
   *  indicates if the operation is abstract, returns TRUE if yes
   */
  public boolean isAbstract() {
    read_if_needed_();
      
    return _abstract;
  }

  /**
   *  to set the 'abstract' flag
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isAbstract(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsAbstractCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _abstract =  y;
    if (y) {
      ((UmlBaseClass) parent()).set_isAbstract(y);
      _cpp_deleted = _cpp_defaulted = false;
    }
  }

  /**
   *  returns the operation value type
   */
  public UmlTypeSpec returnType() {
    read_if_needed_();
      
    return _return_type;
  }

  /**
   *  to set the operation value type
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_ReturnType(UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setReturnTypeCmd, t);
    UmlCom.check();
  
    _return_type = t;
  }

  /**
   *  return the return type multiplicity
   */
  public String multiplicity() {
    read_if_needed_();
    return _multiplicity;
  }

  /**
   *  set the return type multiplicity
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Multiplicity(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMultiplicityCmd, v);
    UmlCom.check();
  
    _multiplicity = v;
  }

  /**
   *  returns (in java a copy of) the parameters list
   */
  public UmlParameter[] params() {
    read_if_needed_();
      
    return (UmlParameter[]) _params.clone();
  }

  /**
   *  adds a parameter at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addParameter(int rank, UmlParameter p) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addParameterCmd, rank, (byte) p.dir.value(), p.name, 
  		   p.default_value, p.type);
    UmlCom.check();
    
    if (defined_()) {
      // compatible with old Java versions
      int n = _params.length;
      UmlParameter[] params = new UmlParameter[n + 1];
      int index;
      
      for (index = 0; index != rank; index += 1)
        params[index] = _params[index];
      params[index] = p.clone_it();
      while (index++ != n)
        params[index] = _params[index - 1];
      _params = params;
    }
  }

  /**
   *  remove the parameter of the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void removeParameter(int rank) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeParameterCmd, rank);
    UmlCom.check();
    
    if (defined_()) {
      int n = _params.length;
      UmlParameter[] params = new UmlParameter[n - 1];
      int index;
      
      for (index = 0; index != rank; index += 1)
        params[index] = _params[index];
      
      while (++index != n)
        params[index - 1] = _params[index];
      
      _params = params;
    }
  }

  /**
   *  replace the parameter at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void replaceParameter(int rank, UmlParameter p) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceParameterCmd, rank,
  		  (byte) p.dir.value(), p.name, p.default_value, p.type);
    UmlCom.check();
    
    if (defined_())
      _params[rank] = p.clone_it();
  }

  /**
   *  returns the exceptions
   */
  public UmlTypeSpec[] exceptions() {
    read_if_needed_();
      
    return (UmlTypeSpec[]) _exceptions.clone();
  }

  /**
   *  adds the exception at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void addException(int rank, UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addExceptionCmd, rank, t);
    UmlCom.check();
    
    if (defined_()) {
      // compatible with old Java versions
      int n = _exceptions.length;
      int index;
      
      UmlTypeSpec[] exceptions = new UmlTypeSpec[n + 1];
      for (index = 0; index != rank; index += 1)
        exceptions[index] = _exceptions[index];
      exceptions[index] = t.clone_it();
      while (index++ != n)
        exceptions[index] = _exceptions[index - 1];
      _exceptions = exceptions;
    }
  
  }

  /**
   *  remove the exception of the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void removeException(int rank) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeExceptionCmd, rank);
    UmlCom.check();
    
    if (defined_()) {
      int n = _exceptions.length;
      UmlTypeSpec[] exceptions = new UmlTypeSpec[n - 1];
      int index;
      
      for (index = 0; index != rank; index += 1)
        exceptions[index] = _exceptions[index];
      
      while (++index != n)
        exceptions[index - 1] = _exceptions[index];
      
      _exceptions = exceptions;
    }
  
  }

  /**
   *  replaces the exception at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void replaceException(int rank, UmlTypeSpec t) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceExceptionCmd, rank, t);
    UmlCom.check();
    
    if (defined_())
      _exceptions[rank] = t.clone_it();
  
  }

  /**
   *  return the behaviors (state and activities) implementing the operation
   */
  public UmlItem[] methods() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.sideCmd);
    return UmlCom.read_item_list();
  }

  /**
   *  in case the operation is a 'get' operation, returns the associated
   *  attribute or relation
   */
  public UmlClassMember getOf() {
    read_if_needed_();
      
    return _get_of;
  }

  /**
   *  in case the operation is a 'set' operation, returns the associated
   *  attribute or relation
   */
  public UmlClassMember setOf() {
    read_if_needed_();
      
    return _set_of;
  }

  /**
   *  returns (a copy of) the formals list
   */
  public UmlFormalParameter[] formals() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.formalsCmd);
    
    int n = UmlCom.read_unsigned();
    UmlFormalParameter[] formals = new UmlFormalParameter[n];
    
    for (int i = 0; i != n; i += 1) {
      UmlFormalParameter f = new UmlFormalParameter();
      
      f.read_();
      formals[i] = f;
    }
    
    return formals;
  }

  /**
   *  remove the formal of the given rank (0...), returns 0 on error
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void removeFormal(int rank) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.removeFormalCmd, rank);
    UmlCom.check();
  }

  /**
   *  adds a formal at the given rank (0...), returns 0 on error
   * 
   * On error return FALSE in C++, produce a RuntimeException in Java,
   * does not check that the class is (already) a typedef
   */
  public void addFormal(int rank, UmlFormalParameter formal) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.addFormalCmd, rank, formal._name, 
  		   formal._type, formal._default_value, formal._extends);
    UmlCom.check();
  }

  /**
   *  replace the formal at the given rank (0...)
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java,
   *  does not check that the class is (already) a typedef
   */
  public void replaceFormal(int rank, UmlFormalParameter formal) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.replaceFormalCmd, rank, formal._name, 
  		   formal._type, formal._default_value, formal._extends);
    UmlCom.check();
  }

  /**
   *  returns TRUE if the operation is declared const in C++
   */
  public boolean isCppConst() {
    read_if_needed_();
      
    return _cpp_const;
  }

  /**
   *  to set if the operation is declared const in C++
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppConst(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_const =  y;
  }

  /**
   *  return if the operation value is declared volatile in C++
   */
  public boolean isCppVolatileValue() {
    read_if_needed_();
    return _cpp_volatilevalue;
  }

  /**
   * if the operation value is declared volatile in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppVolatileValue(boolean v) throws RuntimeException {
    byte vv = (byte) (((v) ? 1 : 0) | 4);
    
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstExprCmd, vv);
    UmlCom.check();
    
    _cpp_volatilevalue = v;
  }

  /**
   *  return if the operation value is declared const in C++
   */
  public boolean isCppConstValue() {
    read_if_needed_();
    return _cpp_constvalue;
  }

  /**
   * if the operation value is declared const in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppConstValue(boolean v) throws RuntimeException {
    byte vv = (byte) ((v) ? 1 : 0);
    
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstExprCmd, vv);
    UmlCom.check();
    
    _cpp_constvalue = v;
  }

  /**
   *  return if the operation value is declared const in C++
   */
  public boolean isCppConstExprValue() {
    read_if_needed_();
    return _cpp_constexprvalue;
  }

  /**
   * if the operation value is declared const in C++
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppConstExprValue(boolean v) throws RuntimeException {
    byte vv = (byte) (((v) ? 1 : 0) | 2);
    
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppConstExprCmd, vv);
    UmlCom.check();
    
    _cpp_constexprvalue = v;
  }

  /**
   *  returns TRUE if the operation is a friend in C++
   */
  public boolean isCppFriend() {
    read_if_needed_();
      
    return _cpp_friend;
  }

  /**
   *  to set if the operation is a friend in C++
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppFriend(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppFriendCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_friend =  y;
    if (y)
      _cpp_deleted = _cpp_defaulted = false;
  }

  /**
   *  returns TRUE if the operation is declared virtual in C++
   */
  public boolean isCppVirtual() {
    read_if_needed_();
      
    return _cpp_virtual;
  }

  /**
   *  to set if the operation is declared virtual in C++
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppVirtual(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppVirtualCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_virtual =  y;
  }

  /**
   *  Indicate if the operation is declared override in C++
   */
  public boolean isCppOverride() {
    read_if_needed_();
  
    return _cpp_override;
  }

  /**
   *  to set if the operation is declared override in C++
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppOverride(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppOverrideCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_override = y;
  }

  /**
   *  Indicate if the operation is declared final in C++
   */
  public boolean isCppFinal() {
    read_if_needed_();
  
    return _cpp_final;
  }

  /**
   *  to set if the operation is declared final in C++
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppFinal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppFinalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_final = y;
  }

  /**
   *  Indicate if the operation is declared noexcept in C++
   */
  public boolean isCppNoexcept() {
    read_if_needed_();
  
    return _cpp_noexcept;
  }

  /**
   *  to set if the operation is declared noexcept in C++
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppNoexcept(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppNoexceptCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_noexcept = y;
  }

  /**
   *  Indicate if the operation is declared deleted in C++
   */
  public boolean isCppDeleted() {
    read_if_needed_();
  
    return _cpp_deleted;
  }

  /**
   *  to set if the operation is declared deleted in C++
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppDeleted(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppDeletedCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_deleted = y;
      if (y)
        _abstract = _cpp_friend = _cpp_defaulted = false;
  }

  /**
   *  Indicate if the operation is declared defaulted in C++
   */
  public boolean isCppDefaulted() {
    read_if_needed_();
  
    return _cpp_defaulted;
  }

  /**
   *  to set if the operation is declared defaulted in C++
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppDefaulted(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppDefaultedCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_defaulted = y;
      if (y)
        _abstract = _cpp_friend = _cpp_deleted = false;
  }

  /**
   *  returns TRUE if the operation is declared inline in C++
   */
  public boolean isCppInline() {
    read_if_needed_();
      
    return _cpp_inline;
  }

  /**
   *  to set if the operation is declared inline in C++
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppInline(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppInlineCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_inline =  y;
  }

  /**
   *  returns the operation's definition in C++, notes that the declaration
   *  is returned by the inherited ClassItemBase::CppDecl() operation
   */
  public String cppDef() {
    read_if_needed_();
      
    return _cpp_def;
  }

  /**
   *  sets the operation's definition in C++, notes that the declaration
   *  is set through the inherited ClassItemBase::set_CppDecl() operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppDef(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppDefCmd, s);
    UmlCom.check();
  
    _cpp_def = s;
  }

  /**
   *  returns the operation's body in C++, useless if the def does not
   *  contains ${body}. Note that the body is get each time from BOUML
   *  for memory size reason
   */
  public String cppBody() {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.cppBodyCmd);
    return UmlCom.read_string();
  }

  /**
   *  sets the operation's body in C++, useless if the def does not 
   *  contains ${body}
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppBody(String s) throws RuntimeException {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppBodyCmd, s);
    UmlCom.check();
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's C++ name must be generated
   */
  public String cppNameSpec() {
    read_if_needed_();
      
    return _cpp_name_spec;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's C++ name must be generated
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppNameSpec(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppNameSpecCmd, s);
    UmlCom.check();
  
    _cpp_name_spec = s;
  }

  /**
   *  return the if the C++ definition is frozen, only for getter/setter operation
   */
  public boolean cppGetSetFrozen() {
    read_if_needed_();
    return _cpp_get_set_frozen;
  }

  /**
   *  set the if the C++ definition is frozen, only for getter/setter operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppGetSetFrozen(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppFrozenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _cpp_get_set_frozen = v;
  }

  /**
   *  indicate if the indent of the C++ body is contextual or absolute
   */
  public boolean cppContextualBodyIndent() {
    read_if_needed_();
    return _cpp_contextual_body_indent;
  }

  /**
   *  set if the indent of the C++ body is contextual or absolute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_CppContextualBodyIndent(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCppContextualBodyIndentCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _cpp_contextual_body_indent = v;
  }

  /**
   *  returns TRUE if the operation is declared final in JAVA
   */
  public boolean isJavaFinal() {
    read_if_needed_();
      
    return _java_final;
  }

  /**
   *  to set if the operation is declared final in JAVA
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaFinal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaFinalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_final =  y;
  }

  /**
   *  returns TRUE if the operation is declared synchronized in JAVA
   */
  public boolean isJavaSynchronized() {
    read_if_needed_();
      
    return _java_synchronized;
  }

  /**
   *  to set if the operation is declared synchronized in JAVA
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaSynchronized(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaSynchronizedCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_synchronized =  y;
  }

  /**
   *  returns TRUE if the operation is declared default in JAVA
   *  relevant in an interface
   */
  public boolean isJavaDefault() {
    read_if_needed_();
  
    return _java_default && !isClassMember();
  }

  /**
   *  to set if the operation is declared default in JAVA
   *  relevant in an interface
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isJavaDefault(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaDefaultOperCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _java_default = y && !isClassMember();
  }

  /**
   *  returns the operation's definition in Java, notes that it is
   *  already made by the inherited JavaDecl operation
   */
  public String javaDef() {
    return javaDecl();
  }

  /**
   *  sets the operation's definition in Java, notes that it is
   *  already made by the inherited set_JavaDecl operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaDef(String s) throws RuntimeException {
   set_JavaDecl(s);
  }

  /**
   *  returns the operation's body in Java++, useless if the def does
   *  not contains ${body} Note that the body is get each time from BOUML
   *  for memory size reason
   */
  public String javaBody() {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.javaBodyCmd);
    return UmlCom.read_string();
  }

  /**
   *  sets the operation's body in Java, useless if the def does not 
   *  contains ${body}
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaBody(String s) throws RuntimeException {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaBodyCmd, s);
    UmlCom.check();
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's JAVA name must be generated
   */
  public String javaNameSpec() {
    read_if_needed_();
      
    return _java_name_spec;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's JAVA name must be generated
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaNameSpec(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaNameSpecCmd, s);
    UmlCom.check();
  
    _java_name_spec = s;
  }

  /**
   *  return the if the Java definition is frozen, only for getter/setter operation
   */
  public boolean javaGetSetFrozen() {
    read_if_needed_();
    return _java_get_set_frozen;
  }

  /**
   *  set the if the Java definition is frozen, only for getter/setter operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaGetSetFrozen(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaFrozenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _java_get_set_frozen = v;
  }

  /**
   *  indicate if the indent of the Java body is contextual or absolute
   */
  public boolean javaContextualBodyIndent() {
    read_if_needed_();
    return _java_contextual_body_indent;
  }

  /**
   *  set if the indent of the Java body is contextual or absolute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_JavaContextualBodyIndent(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setJavaContextualBodyIndentCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _java_contextual_body_indent = v;
  }

  /**
   *  returns TRUE if the operation is declared final in PHP
   */
  public boolean isPhpFinal() {
    read_if_needed_();
      
    return _php_final;
  }

  /**
   *  to set if the operation is declared final in PHP
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isPhpFinal(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpFinalCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _php_final =  y;
  }

  /**
   *  returns the operation's definition in Php, notes that it is
   *  already made by the inherited PhpDecl operation
   */
  public String phpDef() {
    return phpDecl();
  }

  /**
   *  sets the operation's definition in Php, notes that it is
   *  already made by the inherited set_PhpDecl operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpDef(String s) throws RuntimeException {
   set_PhpDecl(s);
  }

  /**
   *  returns the operation's body in Php++, useless if the def does
   *  not contains ${body} Note that the body is get each time from BOUML
   *  for memory size reason
   */
  public String phpBody() {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.phpBodyCmd);
    return UmlCom.read_string();
  }

  /**
   *  sets the operation's body in Php, useless if the def does not 
   *  contains ${body}
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpBody(String s) throws RuntimeException {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpBodyCmd, s);
    UmlCom.check();
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's PHP name must be generated
   */
  public String phpNameSpec() {
    read_if_needed_();
      
    return _php_name_spec;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's PHP name must be generated
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpNameSpec(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpNameSpecCmd, s);
    UmlCom.check();
  
    _php_name_spec = s;
  }

  /**
   *  return the if the Php definition is frozen, only for getter/setter operation
   */
  public boolean phpGetSetFrozen() {
    read_if_needed_();
    return _php_get_set_frozen;
  }

  /**
   *  set the if the Php definition is frozen, only for getter/setter operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpGetSetFrozen(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpFrozenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _php_get_set_frozen = v;
  }

  /**
   *  indicate if the indent of the PHP body is contextual or absolute
   */
  public boolean phpContextualBodyIndent() {
    read_if_needed_();
    return _php_contextual_body_indent;
  }

  /**
   *  set if the indent of the PHP body is contextual or absolute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PhpContextualBodyIndent(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPhpContextualBodyIndentCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _php_contextual_body_indent = v;
  }

  /**
   *  returns the operation's definition in Python, notes that it is
   *  already made by the inherited PythonDecl operation
   */
  public String pythonDef() {
    return pythonDecl();
  }

  /**
   *  sets the operation's definition in Python, notes that it is
   *  already made by the inherited set_PythonDecl operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonDef(String s) throws RuntimeException {
   set_PythonDecl(s);
  }

  /**
   *  returns the operation's body in Python++, useless if the def does
   *  not contains ${body} Note that the body is get each time from BOUML
   *  for memory size reason
   */
  public String pythonBody() {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.pythonBodyCmd);
    return UmlCom.read_string();
  }

  /**
   *  sets the operation's body in Python, useless if the def does not 
   *  contains ${body}
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonBody(String s) throws RuntimeException {
    // not memorized in the instance for memory size reason
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonBodyCmd, s);
    UmlCom.check();
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's PYTHON name must be generated
   */
  public String pythonNameSpec() {
    read_if_needed_();
      
    return _python_name_spec;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's PYTHON name must be generated
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonNameSpec(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonNameSpecCmd, s);
    UmlCom.check();
  
    _python_name_spec = s;
  }

  /**
   *  return the if the Python definition is frozen, only for getter/setter operation
   */
  public boolean pythonGetSetFrozen() {
    read_if_needed_();
    return _python_get_set_frozen;
  }

  /**
   *  set the if the Python definition is frozen, only for getter/setter operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonGetSetFrozen(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonFrozenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _python_get_set_frozen = v;
  }

  /**
   *  indicate if the indent of the Python body is contextual or absolute
   */
  public boolean pythonContextualBodyIndent() {
    read_if_needed_();
    return _python_contextual_body_indent;
  }

  /**
   *  set if the indent of the Python body is contextual or absolute
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonContextualBodyIndent(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonContextualBodyIndentCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _python_contextual_body_indent = v;
  }

  /**
   *  return the  decorators
   */
  public String pythonDecorators() {
    read_if_needed_();
    return _python_decorators;
  }

  /**
   *  set the  decorators
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_PythonDecorators(String v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setPythonDecoratorsCmd, v);
    UmlCom.check();
  
    _python_decorators = v;
  }

  /**
   *  returns TRUE if the operation is declared oneway in IDL
   */
  public boolean isIdlOneway() {
    read_if_needed_();
      
    return _idl_oneway;
  }

  /**
   *  to set if the operation is declared oneway in IDL
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isIdlOneway(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsIdlOnewayCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _idl_oneway = y;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's IDL name must be generated
   */
  public String idlNameSpec() {
    read_if_needed_();
      
    return _idl_name_spec;
  }

  /**
   *  in case the operation is a 'get' or 'set' operation, returns how
   *  the operation's IDL name must be generated
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlNameSpec(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlNameSpecCmd, s);
    UmlCom.check();
  
    _idl_name_spec = s;
  }

  /**
   *  return the if the IDL definition is frozen, only for getter/setter operation
   */
  public boolean idlGetSetFrozen() {
    read_if_needed_();
    return _idl_get_set_frozen;
  }

  /**
   *  set the if the IDL definition is frozen, only for getter/setter operation
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_IdlGetSetFrozen(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIdlFrozenCmd, (v) ? 1 : 0);
    UmlCom.check();
  
    _idl_get_set_frozen = v;
  }

  /**
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _return_type = null;
    _params = null;
    _exceptions = null;
    _cpp_def = null;
    _cpp_name_spec = null;
    _java_name_spec = null;
    _php_name_spec = null;
    _python_name_spec = null;
    _python_decorators = null;
    _idl_name_spec = null;
    super.unload(rec, del);
  }

  private boolean _force_body_generation;

  private boolean _abstract;
  private boolean _cpp_const;
  private boolean _cpp_volatilevalue;

  private boolean _cpp_constvalue;

  private boolean _cpp_constexprvalue;

  private boolean _cpp_friend;

  private boolean _cpp_virtual;
  private boolean _cpp_override;

  private boolean _cpp_final;

  private boolean _cpp_noexcept;

  private boolean _cpp_deleted;

  private boolean _cpp_defaulted;

  private boolean _cpp_inline;
  private boolean _java_final;
  private boolean _java_default;

  private boolean _java_synchronized;

  private boolean _php_final;
  private boolean _idl_oneway;
  private boolean _cpp_get_set_frozen;

  private boolean _java_get_set_frozen;

  private boolean _php_get_set_frozen;

  private boolean _python_get_set_frozen;

  private boolean _idl_get_set_frozen;

  private boolean _cpp_contextual_body_indent;

  private boolean _java_contextual_body_indent;

  private boolean _php_contextual_body_indent;

  private boolean _python_contextual_body_indent;

  private UmlTypeSpec _return_type;
  private String _multiplicity;

  private UmlParameter[] _params;

  private UmlTypeSpec[] _exceptions;

  private String _cpp_def;
  private String _cpp_name_spec;
  private String _java_name_spec;
  private String _php_name_spec;
  private String _python_name_spec;

  private String _python_decorators;

  private String _idl_name_spec;
  /**
   *  exclusive with set_of
   */
  private UmlClassMember _get_of;

  /**
   *  exclusive with get_of
   */
  private UmlClassMember _set_of;

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseOperation(long id, String n) { super(id, n); }

  protected void read_uml_() {
    super.read_uml_();
    _return_type = new UmlTypeSpec();
    _return_type.type = (UmlClass) UmlBaseItem.read_();
    if (_return_type.type == null)
      _return_type.explicit_type = UmlCom.read_string();
    _multiplicity = UmlCom.read_string();
    _abstract = UmlCom.read_bool();
    
    _force_body_generation = UmlCom.read_bool();
    
    int i;
    int n;
    
    n = UmlCom.read_unsigned();
    _params = new UmlParameter[n];
  
    for (i = 0; i != n; i += 1) {
      UmlParameter param = new UmlParameter();
      
      param.dir = aDirection.fromInt(UmlCom.read_char());
      param.type.type = (UmlClass) UmlBaseItem.read_();
      if (param.type.type == null)
        param.type.explicit_type = UmlCom.read_string();
      param.name = UmlCom.read_string();
      param.default_value = UmlCom.read_string();
      param.multiplicity = UmlCom.read_string();
      _params[i] = param;
    }
    
    n = UmlCom.read_unsigned();
    _exceptions = new UmlTypeSpec[n];
    
    for (i = 0; i != n; i += 1) {
      UmlTypeSpec exception = new UmlTypeSpec();
      
      exception.type = (UmlClass) UmlBaseItem.read_();
      if (exception.type == null)
        exception.explicit_type = UmlCom.read_string();
      _exceptions[i] = exception;
    }
  
    _get_of = (UmlClassMember) UmlBaseItem.read_();
    _set_of = (UmlClassMember) UmlBaseItem.read_();
  }

  protected void read_cpp_() {
    super.read_cpp_();
    _cpp_const = UmlCom.read_bool();
    _cpp_volatilevalue = UmlCom.read_bool();
    _cpp_constvalue = UmlCom.read_bool();
    _cpp_constexprvalue = UmlCom.read_bool();
    _cpp_friend = UmlCom.read_bool();
    _cpp_virtual = UmlCom.read_bool();
    _cpp_override = UmlCom.read_bool();
    _cpp_final = UmlCom.read_bool();
    _cpp_noexcept = UmlCom.read_bool();
    _cpp_deleted = UmlCom.read_bool();
    _cpp_defaulted = UmlCom.read_bool();
    _cpp_inline = UmlCom.read_bool();
    _cpp_def = UmlCom.read_string();
    _cpp_name_spec = UmlCom.read_string();
    _cpp_get_set_frozen = UmlCom.read_bool();
    _cpp_contextual_body_indent = UmlCom.read_bool();
  }

  protected void read_java_() {
    super.read_java_();
    _java_final = UmlCom.read_bool();
    _java_synchronized = UmlCom.read_bool();
    _java_name_spec = UmlCom.read_string();
    _java_get_set_frozen = UmlCom.read_bool();
    _java_contextual_body_indent = UmlCom.read_bool();
  
    _java_default = UmlCom.read_bool();
  }

  protected void read_php_() {
    super.read_php_();
    _php_final = UmlCom.read_bool();
    _php_name_spec = UmlCom.read_string();
    _php_get_set_frozen = UmlCom.read_bool();
    _php_contextual_body_indent = UmlCom.read_bool();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
    super.read_python_();
    _python_decorators = UmlCom.read_string();
    _python_name_spec = UmlCom.read_string();
    _python_get_set_frozen = UmlCom.read_bool();
    _python_contextual_body_indent = UmlCom.read_bool();
  }

  protected void read_idl_() {
    super.read_idl_();
    _idl_oneway = UmlCom.read_bool();
    _idl_name_spec = UmlCom.read_string();
    _idl_get_set_frozen = UmlCom.read_bool();
  }

  /**
   * internal, do NOT use it
   */
  protected void read_mysql_() {
  }

};
