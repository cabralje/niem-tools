
/**
 *  Internal enum
 */
final class IdlSettingsCmd {
  public static final int _getIdlSettingsCmd = 0;
  public static final IdlSettingsCmd getIdlSettingsCmd = new IdlSettingsCmd(_getIdlSettingsCmd);
  public static final int _getIdlUseDefaultsCmd = 1;
  public static final IdlSettingsCmd getIdlUseDefaultsCmd = new IdlSettingsCmd(_getIdlUseDefaultsCmd);
  public static final int _setIdlUseDefaultsCmd = 2;
  public static final IdlSettingsCmd setIdlUseDefaultsCmd = new IdlSettingsCmd(_setIdlUseDefaultsCmd);
  public static final int _setIdlTypeCmd = 3;
  public static final IdlSettingsCmd setIdlTypeCmd = new IdlSettingsCmd(_setIdlTypeCmd);
  public static final int _setIdlRelationAttributeStereotypeCmd = 4;
  public static final IdlSettingsCmd setIdlRelationAttributeStereotypeCmd = new IdlSettingsCmd(_setIdlRelationAttributeStereotypeCmd);
  public static final int _setIdlClassStereotypeCmd = 5;
  public static final IdlSettingsCmd setIdlClassStereotypeCmd = new IdlSettingsCmd(_setIdlClassStereotypeCmd);
  public static final int _setIdlIncludeCmd = 6;
  public static final IdlSettingsCmd setIdlIncludeCmd = new IdlSettingsCmd(_setIdlIncludeCmd);
  public static final int _setIdlRootDirCmd = 7;
  public static final IdlSettingsCmd setIdlRootDirCmd = new IdlSettingsCmd(_setIdlRootDirCmd);
  public static final int _setIdlSourceContentCmd = 8;
  public static final IdlSettingsCmd setIdlSourceContentCmd = new IdlSettingsCmd(_setIdlSourceContentCmd);
  public static final int _setIdlSourceExtensionCmd = 9;
  public static final IdlSettingsCmd setIdlSourceExtensionCmd = new IdlSettingsCmd(_setIdlSourceExtensionCmd);
  public static final int _setIdlInterfaceDeclCmd = 10;
  public static final IdlSettingsCmd setIdlInterfaceDeclCmd = new IdlSettingsCmd(_setIdlInterfaceDeclCmd);
  public static final int _setIdlValuetypeDeclCmd = 11;
  public static final IdlSettingsCmd setIdlValuetypeDeclCmd = new IdlSettingsCmd(_setIdlValuetypeDeclCmd);
  public static final int _setIdlStructDeclCmd = 12;
  public static final IdlSettingsCmd setIdlStructDeclCmd = new IdlSettingsCmd(_setIdlStructDeclCmd);
  public static final int _setIdlTypedefDeclCmd = 13;
  public static final IdlSettingsCmd setIdlTypedefDeclCmd = new IdlSettingsCmd(_setIdlTypedefDeclCmd);
  public static final int _setIdlExceptionDeclCmd = 14;
  public static final IdlSettingsCmd setIdlExceptionDeclCmd = new IdlSettingsCmd(_setIdlExceptionDeclCmd);
  public static final int _setIdlUnionDeclCmd = 15;
  public static final IdlSettingsCmd setIdlUnionDeclCmd = new IdlSettingsCmd(_setIdlUnionDeclCmd);
  public static final int _setIdlEnumDeclCmd = 16;
  public static final IdlSettingsCmd setIdlEnumDeclCmd = new IdlSettingsCmd(_setIdlEnumDeclCmd);
  public static final int _setIdlExternalClassDeclCmd = 17;
  public static final IdlSettingsCmd setIdlExternalClassDeclCmd = new IdlSettingsCmd(_setIdlExternalClassDeclCmd);
  public static final int _setIdlAttributeDeclCmd = 18;
  public static final IdlSettingsCmd setIdlAttributeDeclCmd = new IdlSettingsCmd(_setIdlAttributeDeclCmd);
  public static final int _setIdlValuetypeAttributeDeclCmd = 19;
  public static final IdlSettingsCmd setIdlValuetypeAttributeDeclCmd = new IdlSettingsCmd(_setIdlValuetypeAttributeDeclCmd);
  public static final int _setIdlUnionItemDeclCmd = 20;
  public static final IdlSettingsCmd setIdlUnionItemDeclCmd = new IdlSettingsCmd(_setIdlUnionItemDeclCmd);
  public static final int _setIdlEnumItemDeclCmd = 21;
  public static final IdlSettingsCmd setIdlEnumItemDeclCmd = new IdlSettingsCmd(_setIdlEnumItemDeclCmd);
  public static final int _setIdlConstDeclCmd = 22;
  public static final IdlSettingsCmd setIdlConstDeclCmd = new IdlSettingsCmd(_setIdlConstDeclCmd);
  public static final int _setIdlRelationDeclCmd = 23;
  public static final IdlSettingsCmd setIdlRelationDeclCmd = new IdlSettingsCmd(_setIdlRelationDeclCmd);
  public static final int _setIdlValuetypeRelationDeclCmd = 24;
  public static final IdlSettingsCmd setIdlValuetypeRelationDeclCmd = new IdlSettingsCmd(_setIdlValuetypeRelationDeclCmd);
  public static final int _setIdlUnionRelationDeclCmd = 25;
  public static final IdlSettingsCmd setIdlUnionRelationDeclCmd = new IdlSettingsCmd(_setIdlUnionRelationDeclCmd);
  public static final int _setIdlOperationDeclCmd = 26;
  public static final IdlSettingsCmd setIdlOperationDeclCmd = new IdlSettingsCmd(_setIdlOperationDeclCmd);
  public static final int _setIdlGetNameCmd = 27;
  public static final IdlSettingsCmd setIdlGetNameCmd = new IdlSettingsCmd(_setIdlGetNameCmd);
  public static final int _setIdlSetNameCmd = 28;
  public static final IdlSettingsCmd setIdlSetNameCmd = new IdlSettingsCmd(_setIdlSetNameCmd);
  public static final int _setIdlIsSetOneWayCmd = 29;
  public static final IdlSettingsCmd setIdlIsSetOneWayCmd = new IdlSettingsCmd(_setIdlIsSetOneWayCmd);

  private int value;

  public int value() {
    return value;
  }

  public static IdlSettingsCmd fromInt(int value) {
    switch (value) {
    case _getIdlSettingsCmd: return getIdlSettingsCmd;
    case _getIdlUseDefaultsCmd: return getIdlUseDefaultsCmd;
    case _setIdlUseDefaultsCmd: return setIdlUseDefaultsCmd;
    case _setIdlTypeCmd: return setIdlTypeCmd;
    case _setIdlRelationAttributeStereotypeCmd: return setIdlRelationAttributeStereotypeCmd;
    case _setIdlClassStereotypeCmd: return setIdlClassStereotypeCmd;
    case _setIdlIncludeCmd: return setIdlIncludeCmd;
    case _setIdlRootDirCmd: return setIdlRootDirCmd;
    case _setIdlSourceContentCmd: return setIdlSourceContentCmd;
    case _setIdlSourceExtensionCmd: return setIdlSourceExtensionCmd;
    case _setIdlInterfaceDeclCmd: return setIdlInterfaceDeclCmd;
    case _setIdlValuetypeDeclCmd: return setIdlValuetypeDeclCmd;
    case _setIdlStructDeclCmd: return setIdlStructDeclCmd;
    case _setIdlTypedefDeclCmd: return setIdlTypedefDeclCmd;
    case _setIdlExceptionDeclCmd: return setIdlExceptionDeclCmd;
    case _setIdlUnionDeclCmd: return setIdlUnionDeclCmd;
    case _setIdlEnumDeclCmd: return setIdlEnumDeclCmd;
    case _setIdlExternalClassDeclCmd: return setIdlExternalClassDeclCmd;
    case _setIdlAttributeDeclCmd: return setIdlAttributeDeclCmd;
    case _setIdlValuetypeAttributeDeclCmd: return setIdlValuetypeAttributeDeclCmd;
    case _setIdlUnionItemDeclCmd: return setIdlUnionItemDeclCmd;
    case _setIdlEnumItemDeclCmd: return setIdlEnumItemDeclCmd;
    case _setIdlConstDeclCmd: return setIdlConstDeclCmd;
    case _setIdlRelationDeclCmd: return setIdlRelationDeclCmd;
    case _setIdlValuetypeRelationDeclCmd: return setIdlValuetypeRelationDeclCmd;
    case _setIdlUnionRelationDeclCmd: return setIdlUnionRelationDeclCmd;
    case _setIdlOperationDeclCmd: return setIdlOperationDeclCmd;
    case _setIdlGetNameCmd: return setIdlGetNameCmd;
    case _setIdlSetNameCmd: return setIdlSetNameCmd;
    case _setIdlIsSetOneWayCmd: return setIdlIsSetOneWayCmd;
    default: throw new Error();
    }
  }

  private IdlSettingsCmd(int v) { value = v; }; 
}
