package fr.bouml;


/**
 *  Internal enum
 */
final class PackageGlobalCmd {
  public static final int _findCppNamespaceCmd = 0;
  public static final PackageGlobalCmd findCppNamespaceCmd = new PackageGlobalCmd(_findCppNamespaceCmd);
  public static final int _findJavaPackageCmd = 1;
  public static final PackageGlobalCmd findJavaPackageCmd = new PackageGlobalCmd(_findJavaPackageCmd);
  public static final int _findIdlModuleCmd = 2;
  public static final PackageGlobalCmd findIdlModuleCmd = new PackageGlobalCmd(_findIdlModuleCmd);
  public static final int _getProjectCmd = 3;
  public static final PackageGlobalCmd getProjectCmd = new PackageGlobalCmd(_getProjectCmd);
  public static final int _isProjectModifiedCmd = 4;
  public static final PackageGlobalCmd isProjectModifiedCmd = new PackageGlobalCmd(_isProjectModifiedCmd);
  public static final int _saveProjectCmd = 5;
  public static final PackageGlobalCmd saveProjectCmd = new PackageGlobalCmd(_saveProjectCmd);
  public static final int _findPythonPackageCmd = 6;
  public static final PackageGlobalCmd findPythonPackageCmd = new PackageGlobalCmd(_findPythonPackageCmd);
  public static final int _updateProfileCmd = 7;
  public static final PackageGlobalCmd updateProfileCmd = new PackageGlobalCmd(_updateProfileCmd);
  public static final int _findStereotypeCmd = 8;
  public static final PackageGlobalCmd findStereotypeCmd = new PackageGlobalCmd(_findStereotypeCmd);
  public static final int _findPhpNamespaceCmd = 9;
  public static final PackageGlobalCmd findPhpNamespaceCmd = new PackageGlobalCmd(_findPhpNamespaceCmd);

  private int value;

  public int value() {
    return value;
  }

  public static PackageGlobalCmd fromInt(int value) {
    switch (value) {
    case _findCppNamespaceCmd: return findCppNamespaceCmd;
    case _findJavaPackageCmd: return findJavaPackageCmd;
    case _findIdlModuleCmd: return findIdlModuleCmd;
    case _getProjectCmd: return getProjectCmd;
    case _isProjectModifiedCmd: return isProjectModifiedCmd;
    case _saveProjectCmd: return saveProjectCmd;
    case _findPythonPackageCmd: return findPythonPackageCmd;
    case _updateProfileCmd: return updateProfileCmd;
    case _findStereotypeCmd: return findStereotypeCmd;
    case _findPhpNamespaceCmd: return findPhpNamespaceCmd;
    default: throw new Error();
    }
  }

  private PackageGlobalCmd(int v) { value = v; }; 
}
