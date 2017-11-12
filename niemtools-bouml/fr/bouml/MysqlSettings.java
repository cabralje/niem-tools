package fr.bouml;

/**
 *  This class manages settings concerning MYSQL, configured through
 *  the 'Generation settings' dialog.
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static
 *  members allowing to just write 'MysqlSettings::member' rather than
 *  'MysqlSettings::instance()->member' or other long sentence like this.
 */
class MysqlSettings extends UmlSettings {
  /**
   *  returns TRUE when the created MySql objects are initialized
   *  with the default definition
   */
  public static boolean useDefaults()
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._getMysqlUseDefaultsCmd);
    return UmlCom.read_bool();
  }

  /**
   *  if y is TRUE the future created MySql objects will be initialized
   *  with the default definition
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_UseDefaults(boolean y) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlUseDefaultsCmd,
  		  (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  }

  /**
   *  return the 'root' directory
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
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlRootDirCmd, v);
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
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlSourceContentCmd, v);
    UmlCom.check();
  
    _src_content = v;
  }

  /**
   *  returns the extension of the file produced by the MYSQL code generator
   */
  public static String sourceExtension()
  {
    read_if_needed_();
  
    return _ext;
  }

  /**
   *  set the extension of the file produced by the MYSQL code generator
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_SourceExtension(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlSourceExtensionCmd, v);
    UmlCom.check();
  
    _ext = v;
  }

  /**
   *  returns the default definition of a table
   */
  public static String tableDecl()
  {
    read_if_needed_();
  
    return _table_def;
  }

  /**
   *  set the default definition of a table
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_TableDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlTableDeclCmd, v);
    UmlCom.check();
  
    _table_def = v;
  }

  /**
   *  returns the default definition of a column
   */
  public static String columnDecl()
  {
    read_if_needed_();
  
    return _column_def;
  }

  /**
   *  set the default definition of a column
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_ColumnDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlColumnDeclCmd, v);
    UmlCom.check();
  
    _column_def = v;
  }

  /**
   *  returns the default definition of a key
   */
  public static String keyDecl()
  {
    read_if_needed_();
  
    return _key_def;
  }

  /**
   *  set the default definition of a key
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public static void set_KeyDecl(String v) throws RuntimeException
  {
    UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._setMysqlKeyDeclCmd, v);
    UmlCom.check();
  
    _key_def = v;
  }

  /**
   *  never called !
   */
  private MysqlSettings(){
  }

  private static boolean _defined;

  private static String _root;

  private static String _table_def;

  private static String _column_def;

  private static String _key_def;

  private static String _src_content;

  private static String _ext;

  protected static void read_()
  {
    _root = UmlCom.read_string();
    
    _src_content = UmlCom.read_string();
    _ext = UmlCom.read_string();
  
    _table_def = UmlCom.read_string();
    _column_def = UmlCom.read_string();
    _key_def = UmlCom.read_string();
  }

  protected static void read_if_needed_()
  {
    UmlSettings.read_if_needed_();
    if (!_defined) {
      UmlCom.send_cmd(CmdFamily.mysqlSettingsCmd, MysqlSettingsCmd._getMysqlSettingsCmd);
      read_();
      _defined = true;
    }
  }

}
