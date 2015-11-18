
final class aMySqlRefOption {
  public static final int _anUnspecifiedRefOption = 0;
  public static final aMySqlRefOption anUnspecifiedRefOption = new aMySqlRefOption(_anUnspecifiedRefOption);
  public static final int _doRestrict = 1;
  public static final aMySqlRefOption doRestrict = new aMySqlRefOption(_doRestrict);
  public static final int _doCascade = 2;
  public static final aMySqlRefOption doCascade = new aMySqlRefOption(_doCascade);
  public static final int _doSetNull = 3;
  public static final aMySqlRefOption doSetNull = new aMySqlRefOption(_doSetNull);
  public static final int _doNoAction = 4;
  public static final aMySqlRefOption doNoAction = new aMySqlRefOption(_doNoAction);

  private int value;

  public int value() {
    return value;
  }

  public static aMySqlRefOption fromInt(int value) {
    switch (value) {
    case _anUnspecifiedRefOption: return anUnspecifiedRefOption;
    case _doRestrict: return doRestrict;
    case _doCascade: return doCascade;
    case _doSetNull: return doSetNull;
    case _doNoAction: return doNoAction;
    default: throw new Error();
    }
  }

  private aMySqlRefOption(int v) { value = v; }; 
}
