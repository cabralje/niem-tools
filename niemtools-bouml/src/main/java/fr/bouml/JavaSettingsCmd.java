package fr.bouml;


/**
 *  Internal enum
 */
final class JavaSettingsCmd {
  public static final int _getJavaSettingsCmd = 0;
  public static final JavaSettingsCmd getJavaSettingsCmd = new JavaSettingsCmd(_getJavaSettingsCmd);
  public static final int _getJavaUseDefaultsCmd = 1;
  public static final JavaSettingsCmd getJavaUseDefaultsCmd = new JavaSettingsCmd(_getJavaUseDefaultsCmd);
  public static final int _setJavaUseDefaultsCmd = 2;
  public static final JavaSettingsCmd setJavaUseDefaultsCmd = new JavaSettingsCmd(_setJavaUseDefaultsCmd);
  public static final int _setJavaTypeCmd = 3;
  public static final JavaSettingsCmd setJavaTypeCmd = new JavaSettingsCmd(_setJavaTypeCmd);
  public static final int _setJavaRelationAttributeStereotypeCmd = 4;
  public static final JavaSettingsCmd setJavaRelationAttributeStereotypeCmd = new JavaSettingsCmd(_setJavaRelationAttributeStereotypeCmd);
  public static final int _setJavaClassStereotypeCmd = 5;
  public static final JavaSettingsCmd setJavaClassStereotypeCmd = new JavaSettingsCmd(_setJavaClassStereotypeCmd);
  public static final int _setJavaImportCmd = 6;
  public static final JavaSettingsCmd setJavaImportCmd = new JavaSettingsCmd(_setJavaImportCmd);
  public static final int _setJavaRootdirCmd = 7;
  public static final JavaSettingsCmd setJavaRootdirCmd = new JavaSettingsCmd(_setJavaRootdirCmd);
  public static final int _setJavaSourceContentCmd = 8;
  public static final JavaSettingsCmd setJavaSourceContentCmd = new JavaSettingsCmd(_setJavaSourceContentCmd);
  public static final int _setJavaSourceExtensionCmd = 9;
  public static final JavaSettingsCmd setJavaSourceExtensionCmd = new JavaSettingsCmd(_setJavaSourceExtensionCmd);
  public static final int _setJavaClassDeclCmd = 10;
  public static final JavaSettingsCmd setJavaClassDeclCmd = new JavaSettingsCmd(_setJavaClassDeclCmd);
  public static final int _setJavaExternalClassDeclCmd = 11;
  public static final JavaSettingsCmd setJavaExternalClassDeclCmd = new JavaSettingsCmd(_setJavaExternalClassDeclCmd);
  public static final int _setJavaEnumPatternDeclCmd = 12;
  public static final JavaSettingsCmd setJavaEnumPatternDeclCmd = new JavaSettingsCmd(_setJavaEnumPatternDeclCmd);
  public static final int _setJavaInterfaceDeclCmd = 13;
  public static final JavaSettingsCmd setJavaInterfaceDeclCmd = new JavaSettingsCmd(_setJavaInterfaceDeclCmd);
  public static final int _setJavaAttributeDeclCmd = 14;
  public static final JavaSettingsCmd setJavaAttributeDeclCmd = new JavaSettingsCmd(_setJavaAttributeDeclCmd);
  public static final int _setJavaEnumPatternItemDeclCmd = 15;
  public static final JavaSettingsCmd setJavaEnumPatternItemDeclCmd = new JavaSettingsCmd(_setJavaEnumPatternItemDeclCmd);
  public static final int _setJavaEnumPatternItemCaseCmd = 16;
  public static final JavaSettingsCmd setJavaEnumPatternItemCaseCmd = new JavaSettingsCmd(_setJavaEnumPatternItemCaseCmd);
  public static final int _setJavaRelationDeclCmd = 17;
  public static final JavaSettingsCmd setJavaRelationDeclCmd = new JavaSettingsCmd(_setJavaRelationDeclCmd);
  public static final int _setJavaOperationDefCmd = 18;
  public static final JavaSettingsCmd setJavaOperationDefCmd = new JavaSettingsCmd(_setJavaOperationDefCmd);
  public static final int _setJavaGetvisibilityCmd = 19;
  public static final JavaSettingsCmd setJavaGetvisibilityCmd = new JavaSettingsCmd(_setJavaGetvisibilityCmd);
  public static final int _setJavaGetNameCmd = 20;
  public static final JavaSettingsCmd setJavaGetNameCmd = new JavaSettingsCmd(_setJavaGetNameCmd);
  public static final int _setJavaSetNameCmd = 21;
  public static final JavaSettingsCmd setJavaSetNameCmd = new JavaSettingsCmd(_setJavaSetNameCmd);
  public static final int _setJavaIsGetFinalCmd = 22;
  public static final JavaSettingsCmd setJavaIsGetFinalCmd = new JavaSettingsCmd(_setJavaIsGetFinalCmd);
  public static final int _setJavaIsSetFinalCmd = 23;
  public static final JavaSettingsCmd setJavaIsSetFinalCmd = new JavaSettingsCmd(_setJavaIsSetFinalCmd);
  public static final int _setJavaSetVisibilityCmd = 24;
  public static final JavaSettingsCmd setJavaSetVisibilityCmd = new JavaSettingsCmd(_setJavaSetVisibilityCmd);
  public static final int _setJavaIsSetParamFinalCmd = 25;
  public static final JavaSettingsCmd setJavaIsSetParamFinalCmd = new JavaSettingsCmd(_setJavaIsSetParamFinalCmd);
  public static final int _setJavaEnumDeclCmd = 26;
  public static final JavaSettingsCmd setJavaEnumDeclCmd = new JavaSettingsCmd(_setJavaEnumDeclCmd);
  public static final int _setJavaEnumItemDeclCmd = 27;
  public static final JavaSettingsCmd setJavaEnumItemDeclCmd = new JavaSettingsCmd(_setJavaEnumItemDeclCmd);
  public static final int _setJavaJavadocStyleCmd = 28;
  public static final JavaSettingsCmd setJavaJavadocStyleCmd = new JavaSettingsCmd(_setJavaJavadocStyleCmd);
  public static final int _setJavaForcePackageGenCmd = 29;
  public static final JavaSettingsCmd setJavaForcePackageGenCmd = new JavaSettingsCmd(_setJavaForcePackageGenCmd);
  public static final int _setJavaDirRevFilterCmd = 30;
  public static final JavaSettingsCmd setJavaDirRevFilterCmd = new JavaSettingsCmd(_setJavaDirRevFilterCmd);
  public static final int _setJavaFileRevFilterCmd = 31;
  public static final JavaSettingsCmd setJavaFileRevFilterCmd = new JavaSettingsCmd(_setJavaFileRevFilterCmd);

  private int value;

  public int value() {
    return value;
  }

  public static JavaSettingsCmd fromInt(int value) {
    switch (value) {
    case _getJavaSettingsCmd: return getJavaSettingsCmd;
    case _getJavaUseDefaultsCmd: return getJavaUseDefaultsCmd;
    case _setJavaUseDefaultsCmd: return setJavaUseDefaultsCmd;
    case _setJavaTypeCmd: return setJavaTypeCmd;
    case _setJavaRelationAttributeStereotypeCmd: return setJavaRelationAttributeStereotypeCmd;
    case _setJavaClassStereotypeCmd: return setJavaClassStereotypeCmd;
    case _setJavaImportCmd: return setJavaImportCmd;
    case _setJavaRootdirCmd: return setJavaRootdirCmd;
    case _setJavaSourceContentCmd: return setJavaSourceContentCmd;
    case _setJavaSourceExtensionCmd: return setJavaSourceExtensionCmd;
    case _setJavaClassDeclCmd: return setJavaClassDeclCmd;
    case _setJavaExternalClassDeclCmd: return setJavaExternalClassDeclCmd;
    case _setJavaEnumPatternDeclCmd: return setJavaEnumPatternDeclCmd;
    case _setJavaInterfaceDeclCmd: return setJavaInterfaceDeclCmd;
    case _setJavaAttributeDeclCmd: return setJavaAttributeDeclCmd;
    case _setJavaEnumPatternItemDeclCmd: return setJavaEnumPatternItemDeclCmd;
    case _setJavaEnumPatternItemCaseCmd: return setJavaEnumPatternItemCaseCmd;
    case _setJavaRelationDeclCmd: return setJavaRelationDeclCmd;
    case _setJavaOperationDefCmd: return setJavaOperationDefCmd;
    case _setJavaGetvisibilityCmd: return setJavaGetvisibilityCmd;
    case _setJavaGetNameCmd: return setJavaGetNameCmd;
    case _setJavaSetNameCmd: return setJavaSetNameCmd;
    case _setJavaIsGetFinalCmd: return setJavaIsGetFinalCmd;
    case _setJavaIsSetFinalCmd: return setJavaIsSetFinalCmd;
    case _setJavaSetVisibilityCmd: return setJavaSetVisibilityCmd;
    case _setJavaIsSetParamFinalCmd: return setJavaIsSetParamFinalCmd;
    case _setJavaEnumDeclCmd: return setJavaEnumDeclCmd;
    case _setJavaEnumItemDeclCmd: return setJavaEnumItemDeclCmd;
    case _setJavaJavadocStyleCmd: return setJavaJavadocStyleCmd;
    case _setJavaForcePackageGenCmd: return setJavaForcePackageGenCmd;
    case _setJavaDirRevFilterCmd: return setJavaDirRevFilterCmd;
    case _setJavaFileRevFilterCmd: return setJavaFileRevFilterCmd;
    default: throw new Error();
    }
  }

  private JavaSettingsCmd(int v) { value = v; }; 
}
