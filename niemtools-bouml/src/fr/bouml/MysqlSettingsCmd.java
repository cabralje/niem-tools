package fr.bouml;


final class MysqlSettingsCmd {
  public static final int _getMysqlSettingsCmd = 0;
  public static final MysqlSettingsCmd getMysqlSettingsCmd = new MysqlSettingsCmd(_getMysqlSettingsCmd);
  public static final int _getMysqlUseDefaultsCmd = 1;
  public static final MysqlSettingsCmd getMysqlUseDefaultsCmd = new MysqlSettingsCmd(_getMysqlUseDefaultsCmd);
  public static final int _setMysqlUseDefaultsCmd = 2;
  public static final MysqlSettingsCmd setMysqlUseDefaultsCmd = new MysqlSettingsCmd(_setMysqlUseDefaultsCmd);
  public static final int _setMysqlRootDirCmd = 3;
  public static final MysqlSettingsCmd setMysqlRootDirCmd = new MysqlSettingsCmd(_setMysqlRootDirCmd);
  public static final int _setMysqlSourceContentCmd = 4;
  public static final MysqlSettingsCmd setMysqlSourceContentCmd = new MysqlSettingsCmd(_setMysqlSourceContentCmd);
  public static final int _setMysqlSourceExtensionCmd = 5;
  public static final MysqlSettingsCmd setMysqlSourceExtensionCmd = new MysqlSettingsCmd(_setMysqlSourceExtensionCmd);
  public static final int _setMysqlTableDeclCmd = 6;
  public static final MysqlSettingsCmd setMysqlTableDeclCmd = new MysqlSettingsCmd(_setMysqlTableDeclCmd);
  public static final int _setMysqlColumnDeclCmd = 7;
  public static final MysqlSettingsCmd setMysqlColumnDeclCmd = new MysqlSettingsCmd(_setMysqlColumnDeclCmd);
  public static final int _setMysqlKeyDeclCmd = 8;
  public static final MysqlSettingsCmd setMysqlKeyDeclCmd = new MysqlSettingsCmd(_setMysqlKeyDeclCmd);

  private int value;

  public int value() {
    return value;
  }

  public static MysqlSettingsCmd fromInt(int value) {
    switch (value) {
    case _getMysqlSettingsCmd: return getMysqlSettingsCmd;
    case _getMysqlUseDefaultsCmd: return getMysqlUseDefaultsCmd;
    case _setMysqlUseDefaultsCmd: return setMysqlUseDefaultsCmd;
    case _setMysqlRootDirCmd: return setMysqlRootDirCmd;
    case _setMysqlSourceContentCmd: return setMysqlSourceContentCmd;
    case _setMysqlSourceExtensionCmd: return setMysqlSourceExtensionCmd;
    case _setMysqlTableDeclCmd: return setMysqlTableDeclCmd;
    case _setMysqlColumnDeclCmd: return setMysqlColumnDeclCmd;
    case _setMysqlKeyDeclCmd: return setMysqlKeyDeclCmd;
    default: throw new Error();
    }
  }

  private MysqlSettingsCmd(int v) { value = v; }; 
}
