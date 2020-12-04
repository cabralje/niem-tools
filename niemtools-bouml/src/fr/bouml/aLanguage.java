package fr.bouml;


final class aLanguage {
  public static final int _umlLanguage = 0;
  public static final aLanguage umlLanguage = new aLanguage(_umlLanguage);
  public static final int _cppLanguage = 1;
  public static final aLanguage cppLanguage = new aLanguage(_cppLanguage);
  public static final int _javaLanguage = 2;
  public static final aLanguage javaLanguage = new aLanguage(_javaLanguage);
  public static final int _idlLanguage = 3;
  public static final aLanguage idlLanguage = new aLanguage(_idlLanguage);
  public static final int _phpLanguage = 4;
  public static final aLanguage phpLanguage = new aLanguage(_phpLanguage);
  public static final int _pythonLanguage = 5;
  public static final aLanguage pythonLanguage = new aLanguage(_pythonLanguage);
  public static final int _mysqlLanguage = 6;
  public static final aLanguage mysqlLanguage = new aLanguage(_mysqlLanguage);

  private int value;

  public int value() {
    return value;
  }

  public static aLanguage fromInt(int value) {
    switch (value) {
    case _umlLanguage: return umlLanguage;
    case _cppLanguage: return cppLanguage;
    case _javaLanguage: return javaLanguage;
    case _idlLanguage: return idlLanguage;
    case _phpLanguage: return phpLanguage;
    case _pythonLanguage: return pythonLanguage;
    case _mysqlLanguage: return mysqlLanguage;
    default: throw new Error();
    }
  }

  private aLanguage(int v) { value = v; }; 
}
