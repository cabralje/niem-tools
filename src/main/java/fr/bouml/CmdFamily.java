package fr.bouml;


/**
 *  Internal enum
 */
final class CmdFamily {
  public static final int _onInstanceCmd = 0;
  public static final CmdFamily onInstanceCmd = new CmdFamily(_onInstanceCmd);
  public static final int _classGlobalCmd = 1;
  public static final CmdFamily classGlobalCmd = new CmdFamily(_classGlobalCmd);
  public static final int _packageGlobalCmd = 2;
  public static final CmdFamily packageGlobalCmd = new CmdFamily(_packageGlobalCmd);
  public static final int _miscGlobalCmd = 3;
  public static final CmdFamily miscGlobalCmd = new CmdFamily(_miscGlobalCmd);
  public static final int _umlSettingsCmd = 4;
  public static final CmdFamily umlSettingsCmd = new CmdFamily(_umlSettingsCmd);
  public static final int _cppSettingsCmd = 5;
  public static final CmdFamily cppSettingsCmd = new CmdFamily(_cppSettingsCmd);
  public static final int _javaSettingsCmd = 6;
  public static final CmdFamily javaSettingsCmd = new CmdFamily(_javaSettingsCmd);
  public static final int _idlSettingsCmd = 7;
  public static final CmdFamily idlSettingsCmd = new CmdFamily(_idlSettingsCmd);
  public static final int _phpSettingsCmd = 8;
  public static final CmdFamily phpSettingsCmd = new CmdFamily(_phpSettingsCmd);
  public static final int _pythonSettingsCmd = 9;
  public static final CmdFamily pythonSettingsCmd = new CmdFamily(_pythonSettingsCmd);
  public static final int _mysqlSettingsCmd = 10;
  public static final CmdFamily mysqlSettingsCmd = new CmdFamily(_mysqlSettingsCmd);

  private int value;

  public int value() {
    return value;
  }

  public static CmdFamily fromInt(int value) {
    switch (value) {
    case _onInstanceCmd: return onInstanceCmd;
    case _classGlobalCmd: return classGlobalCmd;
    case _packageGlobalCmd: return packageGlobalCmd;
    case _miscGlobalCmd: return miscGlobalCmd;
    case _umlSettingsCmd: return umlSettingsCmd;
    case _cppSettingsCmd: return cppSettingsCmd;
    case _javaSettingsCmd: return javaSettingsCmd;
    case _idlSettingsCmd: return idlSettingsCmd;
    case _phpSettingsCmd: return phpSettingsCmd;
    case _pythonSettingsCmd: return pythonSettingsCmd;
    case _mysqlSettingsCmd: return mysqlSettingsCmd;
    default: throw new Error();
    }
  }

  private CmdFamily(int v) { value = v; }; 
}
