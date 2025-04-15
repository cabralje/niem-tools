package fr.bouml;


/**
 *  Internal enum
 */
final class ClassGlobalCmd {
  public static final int _findClassCmd = 0;
  public static final ClassGlobalCmd findClassCmd = new ClassGlobalCmd(_findClassCmd);

  private int value;

  public int value() {
    return value;
  }

  public static ClassGlobalCmd fromInt(int value) {
    switch (value) {
    case _findClassCmd: return findClassCmd;
    default: throw new Error();
    }
  }

  private ClassGlobalCmd(int v) { value = v; }; 
}
