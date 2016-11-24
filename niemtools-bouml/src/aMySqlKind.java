
final class aMySqlKind {
  public static final int _aColumn = 0;
  public static final aMySqlKind aColumn = new aMySqlKind(_aColumn);
  public static final int _aPrimaryKey = 1;
  public static final aMySqlKind aPrimaryKey = new aMySqlKind(_aPrimaryKey);
  public static final int _anUniqueKey = 2;
  public static final aMySqlKind anUniqueKey = new aMySqlKind(_anUniqueKey);
  public static final int _aKey = 3;
  public static final aMySqlKind aKey = new aMySqlKind(_aKey);
  public static final int _aFulltextKey = 4;
  public static final aMySqlKind aFulltextKey = new aMySqlKind(_aFulltextKey);
  public static final int _aSpatialKey = 5;
  public static final aMySqlKind aSpatialKey = new aMySqlKind(_aSpatialKey);

  private int value;

  public int value() {
    return value;
  }

  public static aMySqlKind fromInt(int value) {
    switch (value) {
    case _aColumn: return aColumn;
    case _aPrimaryKey: return aPrimaryKey;
    case _anUniqueKey: return anUniqueKey;
    case _aKey: return aKey;
    case _aFulltextKey: return aFulltextKey;
    case _aSpatialKey: return aSpatialKey;
    default: throw new Error();
    }
  }

  private aMySqlKind(int v) { value = v; }; 
}
