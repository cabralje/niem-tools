package fr.bouml;

/**
 *  Manage the 'extra' class members to produce any code, for instance a C++ pre-processing form or a Java initialization
 */
abstract class UmlBaseExtraClassMember extends UmlClassItem {
  /**
   *  returns a new extra class member named 'name'  (the name may be empty)
   *  created under the class 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlExtraClassMember create(UmlClass parent, String name) throws RuntimeException
  {
    return (UmlExtraClassMember) parent.create_(anItemKind.anExtraClassMember, name);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anExtraClassMember;
  }

  /**
   *  returns TRUE if the extra member is managed as an inline operation in C++
   */
  public boolean isCppInline() {
    read_if_needed_();
      
    return _cpp_inline;
  }

  /**
   *  to set if the extra member is managed as an inline operation in C++
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isCppInline(boolean y) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setIsCppInlineCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _cpp_inline = y;
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
   *  to unload the object to free memory, it will be reloaded
   *  automatically if needed. args unused
   */
  public void unload(boolean rec, boolean del) {
    _cpp_def = null;
    super.unload(rec, del);
  }

  private boolean _cpp_inline;
  private String _cpp_def;
  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseExtraClassMember(long id, String n) { super(id, n); }

  protected void read_cpp_() {
    super.read_cpp_();
    _cpp_def = UmlCom.read_string();
    _cpp_inline = UmlCom.read_bool();
  }

}
