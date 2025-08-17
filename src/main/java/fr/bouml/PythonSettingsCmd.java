package fr.bouml;


final class PythonSettingsCmd {
  public static final int _getPythonSettingsCmd = 0;
  public static final PythonSettingsCmd getPythonSettingsCmd = new PythonSettingsCmd(_getPythonSettingsCmd);
  public static final int _getPythonUseDefaultsCmd = 1;
  public static final PythonSettingsCmd getPythonUseDefaultsCmd = new PythonSettingsCmd(_getPythonUseDefaultsCmd);
  public static final int _setPythonUseDefaultsCmd = 2;
  public static final PythonSettingsCmd setPythonUseDefaultsCmd = new PythonSettingsCmd(_setPythonUseDefaultsCmd);
  public static final int _setPython22Cmd = 3;
  public static final PythonSettingsCmd setPython22Cmd = new PythonSettingsCmd(_setPython22Cmd);
  public static final int _setPythonIndentStepCmd = 4;
  public static final PythonSettingsCmd setPythonIndentStepCmd = new PythonSettingsCmd(_setPythonIndentStepCmd);
  public static final int _setPythonRelationAttributeStereotypeCmd = 5;
  public static final PythonSettingsCmd setPythonRelationAttributeStereotypeCmd = new PythonSettingsCmd(_setPythonRelationAttributeStereotypeCmd);
  public static final int _setPythonClassStereotypeCmd = 6;
  public static final PythonSettingsCmd setPythonClassStereotypeCmd = new PythonSettingsCmd(_setPythonClassStereotypeCmd);
  public static final int _setPythonImportCmd = 7;
  public static final PythonSettingsCmd setPythonImportCmd = new PythonSettingsCmd(_setPythonImportCmd);
  public static final int _setPythonRootdirCmd = 8;
  public static final PythonSettingsCmd setPythonRootdirCmd = new PythonSettingsCmd(_setPythonRootdirCmd);
  public static final int _setPythonSourceContentCmd = 9;
  public static final PythonSettingsCmd setPythonSourceContentCmd = new PythonSettingsCmd(_setPythonSourceContentCmd);
  public static final int _setPythonSourceExtensionCmd = 10;
  public static final PythonSettingsCmd setPythonSourceExtensionCmd = new PythonSettingsCmd(_setPythonSourceExtensionCmd);
  public static final int _setPythonClassDeclCmd = 11;
  public static final PythonSettingsCmd setPythonClassDeclCmd = new PythonSettingsCmd(_setPythonClassDeclCmd);
  public static final int _setPythonEnumDeclCmd = 12;
  public static final PythonSettingsCmd setPythonEnumDeclCmd = new PythonSettingsCmd(_setPythonEnumDeclCmd);
  public static final int _setPythonExternalClassDeclCmd = 13;
  public static final PythonSettingsCmd setPythonExternalClassDeclCmd = new PythonSettingsCmd(_setPythonExternalClassDeclCmd);
  public static final int _setPythonInterfaceDeclCmd = 14;
  public static final PythonSettingsCmd setPythonInterfaceDeclCmd = new PythonSettingsCmd(_setPythonInterfaceDeclCmd);
  public static final int _setPythonAttributeDeclCmd = 15;
  public static final PythonSettingsCmd setPythonAttributeDeclCmd = new PythonSettingsCmd(_setPythonAttributeDeclCmd);
  public static final int _setPythonEnumItemDeclCmd = 16;
  public static final PythonSettingsCmd setPythonEnumItemDeclCmd = new PythonSettingsCmd(_setPythonEnumItemDeclCmd);
  public static final int _setPythonRelationDeclCmd = 17;
  public static final PythonSettingsCmd setPythonRelationDeclCmd = new PythonSettingsCmd(_setPythonRelationDeclCmd);
  public static final int _setPythonOperationDefCmd = 18;
  public static final PythonSettingsCmd setPythonOperationDefCmd = new PythonSettingsCmd(_setPythonOperationDefCmd);
  public static final int _setPythonGetNameCmd = 19;
  public static final PythonSettingsCmd setPythonGetNameCmd = new PythonSettingsCmd(_setPythonGetNameCmd);
  public static final int _setPythonSetNameCmd = 20;
  public static final PythonSettingsCmd setPythonSetNameCmd = new PythonSettingsCmd(_setPythonSetNameCmd);
  public static final int _setPythonInitOperationDefCmd = 21;
  public static final PythonSettingsCmd setPythonInitOperationDefCmd = new PythonSettingsCmd(_setPythonInitOperationDefCmd);
  public static final int _setPython3OperationCmd = 22;
  public static final PythonSettingsCmd setPython3OperationCmd = new PythonSettingsCmd(_setPython3OperationCmd);

  private int value;

  public int value() {
    return value;
  }

  public static PythonSettingsCmd fromInt(int value) {
    switch (value) {
    case _getPythonSettingsCmd: return getPythonSettingsCmd;
    case _getPythonUseDefaultsCmd: return getPythonUseDefaultsCmd;
    case _setPythonUseDefaultsCmd: return setPythonUseDefaultsCmd;
    case _setPython22Cmd: return setPython22Cmd;
    case _setPythonIndentStepCmd: return setPythonIndentStepCmd;
    case _setPythonRelationAttributeStereotypeCmd: return setPythonRelationAttributeStereotypeCmd;
    case _setPythonClassStereotypeCmd: return setPythonClassStereotypeCmd;
    case _setPythonImportCmd: return setPythonImportCmd;
    case _setPythonRootdirCmd: return setPythonRootdirCmd;
    case _setPythonSourceContentCmd: return setPythonSourceContentCmd;
    case _setPythonSourceExtensionCmd: return setPythonSourceExtensionCmd;
    case _setPythonClassDeclCmd: return setPythonClassDeclCmd;
    case _setPythonEnumDeclCmd: return setPythonEnumDeclCmd;
    case _setPythonExternalClassDeclCmd: return setPythonExternalClassDeclCmd;
    case _setPythonInterfaceDeclCmd: return setPythonInterfaceDeclCmd;
    case _setPythonAttributeDeclCmd: return setPythonAttributeDeclCmd;
    case _setPythonEnumItemDeclCmd: return setPythonEnumItemDeclCmd;
    case _setPythonRelationDeclCmd: return setPythonRelationDeclCmd;
    case _setPythonOperationDefCmd: return setPythonOperationDefCmd;
    case _setPythonGetNameCmd: return setPythonGetNameCmd;
    case _setPythonSetNameCmd: return setPythonSetNameCmd;
    case _setPythonInitOperationDefCmd: return setPythonInitOperationDefCmd;
    case _setPython3OperationCmd: return setPython3OperationCmd;
    default: throw new Error();
    }
  }

  private PythonSettingsCmd(int v) { value = v; }; 
}
