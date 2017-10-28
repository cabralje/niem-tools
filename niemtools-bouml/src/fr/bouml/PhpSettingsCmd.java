package fr.bouml;

final class PhpSettingsCmd {
  public static final int _getPhpSettingsCmd = 0;
  public static final PhpSettingsCmd getPhpSettingsCmd = new PhpSettingsCmd(_getPhpSettingsCmd);
  public static final int _getPhpUseDefaultsCmd = 1;
  public static final PhpSettingsCmd getPhpUseDefaultsCmd = new PhpSettingsCmd(_getPhpUseDefaultsCmd);
  public static final int _setPhpUseDefaultsCmd = 2;
  public static final PhpSettingsCmd setPhpUseDefaultsCmd = new PhpSettingsCmd(_setPhpUseDefaultsCmd);
  public static final int _setPhpClassStereotypeCmd = 3;
  public static final PhpSettingsCmd setPhpClassStereotypeCmd = new PhpSettingsCmd(_setPhpClassStereotypeCmd);
  public static final int _setPhpRootdirCmd = 4;
  public static final PhpSettingsCmd setPhpRootdirCmd = new PhpSettingsCmd(_setPhpRootdirCmd);
  public static final int _setPhpSourceContentCmd = 5;
  public static final PhpSettingsCmd setPhpSourceContentCmd = new PhpSettingsCmd(_setPhpSourceContentCmd);
  public static final int _setPhpSourceExtensionCmd = 6;
  public static final PhpSettingsCmd setPhpSourceExtensionCmd = new PhpSettingsCmd(_setPhpSourceExtensionCmd);
  public static final int _setPhpClassDeclCmd = 7;
  public static final PhpSettingsCmd setPhpClassDeclCmd = new PhpSettingsCmd(_setPhpClassDeclCmd);
  public static final int _setPhpEnumDeclCmd = 8;
  public static final PhpSettingsCmd setPhpEnumDeclCmd = new PhpSettingsCmd(_setPhpEnumDeclCmd);
  public static final int _setPhpExternalClassDeclCmd = 9;
  public static final PhpSettingsCmd setPhpExternalClassDeclCmd = new PhpSettingsCmd(_setPhpExternalClassDeclCmd);
  public static final int _setPhpInterfaceDeclCmd = 10;
  public static final PhpSettingsCmd setPhpInterfaceDeclCmd = new PhpSettingsCmd(_setPhpInterfaceDeclCmd);
  public static final int _setPhpAttributeDeclCmd = 11;
  public static final PhpSettingsCmd setPhpAttributeDeclCmd = new PhpSettingsCmd(_setPhpAttributeDeclCmd);
  public static final int _setPhpEnumItemDeclCmd = 12;
  public static final PhpSettingsCmd setPhpEnumItemDeclCmd = new PhpSettingsCmd(_setPhpEnumItemDeclCmd);
  public static final int _setPhpRelationDeclCmd = 13;
  public static final PhpSettingsCmd setPhpRelationDeclCmd = new PhpSettingsCmd(_setPhpRelationDeclCmd);
  public static final int _setPhpOperationDefCmd = 14;
  public static final PhpSettingsCmd setPhpOperationDefCmd = new PhpSettingsCmd(_setPhpOperationDefCmd);
  public static final int _setPhpGetNameCmd = 15;
  public static final PhpSettingsCmd setPhpGetNameCmd = new PhpSettingsCmd(_setPhpGetNameCmd);
  public static final int _setPhpSetNameCmd = 16;
  public static final PhpSettingsCmd setPhpSetNameCmd = new PhpSettingsCmd(_setPhpSetNameCmd);
  public static final int _setPhpIsGetFinalCmd = 17;
  public static final PhpSettingsCmd setPhpIsGetFinalCmd = new PhpSettingsCmd(_setPhpIsGetFinalCmd);
  public static final int _setPhpIsSetFinalCmd = 18;
  public static final PhpSettingsCmd setPhpIsSetFinalCmd = new PhpSettingsCmd(_setPhpIsSetFinalCmd);
  public static final int _setPhpIsSetParamFinalCmd = 19;
  public static final PhpSettingsCmd setPhpIsSetParamFinalCmd = new PhpSettingsCmd(_setPhpIsSetParamFinalCmd);
  public static final int _setPhpJavadocStyleCmd = 20;
  public static final PhpSettingsCmd setPhpJavadocStyleCmd = new PhpSettingsCmd(_setPhpJavadocStyleCmd);
  public static final int _setPhpRequireOnceWithPathCmd = 21;
  public static final PhpSettingsCmd setPhpRequireOnceWithPathCmd = new PhpSettingsCmd(_setPhpRequireOnceWithPathCmd);
  public static final int _setPhpRelativePathCmd = 22;
  public static final PhpSettingsCmd setPhpRelativePathCmd = new PhpSettingsCmd(_setPhpRelativePathCmd);
  public static final int _setPhpRootRelativePathCmd = 23;
  public static final PhpSettingsCmd setPhpRootRelativePathCmd = new PhpSettingsCmd(_setPhpRootRelativePathCmd);
  public static final int _setPhpDirRevFilterCmd = 24;
  public static final PhpSettingsCmd setPhpDirRevFilterCmd = new PhpSettingsCmd(_setPhpDirRevFilterCmd);
  public static final int _setPhpFileRevFilterCmd = 25;
  public static final PhpSettingsCmd setPhpFileRevFilterCmd = new PhpSettingsCmd(_setPhpFileRevFilterCmd);
  public static final int _setPhpForceNamespaceGenCmd = 26;
  public static final PhpSettingsCmd setPhpForceNamespaceGenCmd = new PhpSettingsCmd(_setPhpForceNamespaceGenCmd);
  public static final int _setPhpParametersTypedCmd = 27;
  public static final PhpSettingsCmd setPhpParametersTypedCmd = new PhpSettingsCmd(_setPhpParametersTypedCmd);
  public static final int _setPhpOutInoutParametersByReferenceCmd = 28;
  public static final PhpSettingsCmd setPhpOutInoutParametersByReferenceCmd = new PhpSettingsCmd(_setPhpOutInoutParametersByReferenceCmd);

  private int value;

  public int value() {
    return value;
  }

  public static PhpSettingsCmd fromInt(int value) {
    switch (value) {
    case _getPhpSettingsCmd: return getPhpSettingsCmd;
    case _getPhpUseDefaultsCmd: return getPhpUseDefaultsCmd;
    case _setPhpUseDefaultsCmd: return setPhpUseDefaultsCmd;
    case _setPhpClassStereotypeCmd: return setPhpClassStereotypeCmd;
    case _setPhpRootdirCmd: return setPhpRootdirCmd;
    case _setPhpSourceContentCmd: return setPhpSourceContentCmd;
    case _setPhpSourceExtensionCmd: return setPhpSourceExtensionCmd;
    case _setPhpClassDeclCmd: return setPhpClassDeclCmd;
    case _setPhpEnumDeclCmd: return setPhpEnumDeclCmd;
    case _setPhpExternalClassDeclCmd: return setPhpExternalClassDeclCmd;
    case _setPhpInterfaceDeclCmd: return setPhpInterfaceDeclCmd;
    case _setPhpAttributeDeclCmd: return setPhpAttributeDeclCmd;
    case _setPhpEnumItemDeclCmd: return setPhpEnumItemDeclCmd;
    case _setPhpRelationDeclCmd: return setPhpRelationDeclCmd;
    case _setPhpOperationDefCmd: return setPhpOperationDefCmd;
    case _setPhpGetNameCmd: return setPhpGetNameCmd;
    case _setPhpSetNameCmd: return setPhpSetNameCmd;
    case _setPhpIsGetFinalCmd: return setPhpIsGetFinalCmd;
    case _setPhpIsSetFinalCmd: return setPhpIsSetFinalCmd;
    case _setPhpIsSetParamFinalCmd: return setPhpIsSetParamFinalCmd;
    case _setPhpJavadocStyleCmd: return setPhpJavadocStyleCmd;
    case _setPhpRequireOnceWithPathCmd: return setPhpRequireOnceWithPathCmd;
    case _setPhpRelativePathCmd: return setPhpRelativePathCmd;
    case _setPhpRootRelativePathCmd: return setPhpRootRelativePathCmd;
    case _setPhpDirRevFilterCmd: return setPhpDirRevFilterCmd;
    case _setPhpFileRevFilterCmd: return setPhpFileRevFilterCmd;
    case _setPhpForceNamespaceGenCmd: return setPhpForceNamespaceGenCmd;
    case _setPhpParametersTypedCmd: return setPhpParametersTypedCmd;
    case _setPhpOutInoutParametersByReferenceCmd: return setPhpOutInoutParametersByReferenceCmd;
    default: throw new Error();
    }
  }

  private PhpSettingsCmd(int v) { value = v; }; 
}
