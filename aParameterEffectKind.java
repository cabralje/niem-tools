
final class aParameterEffectKind {
  public static final int _noEffect = 0;
  public static final aParameterEffectKind noEffect = new aParameterEffectKind(_noEffect);
  public static final int _createEffect = 1;
  public static final aParameterEffectKind createEffect = new aParameterEffectKind(_createEffect);
  public static final int _readEffect = 2;
  public static final aParameterEffectKind readEffect = new aParameterEffectKind(_readEffect);
  public static final int _updateEffect = 3;
  public static final aParameterEffectKind updateEffect = new aParameterEffectKind(_updateEffect);
  public static final int _deleteEffect = 4;
  public static final aParameterEffectKind deleteEffect = new aParameterEffectKind(_deleteEffect);

  private int value;

  public int value() {
    return value;
  }

  public static aParameterEffectKind fromInt(int value) {
    switch (value) {
    case _noEffect: return noEffect;
    case _createEffect: return createEffect;
    case _readEffect: return readEffect;
    case _updateEffect: return updateEffect;
    case _deleteEffect: return deleteEffect;
    default: throw new Error();
    }
  }

  private aParameterEffectKind(int v) { value = v; }; 
}
