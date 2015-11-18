
/**
 *  indicate the kind of a message
 */
final class aMessageKind {
  public static final int _aSynchronousCall = 0;
  public static final aMessageKind aSynchronousCall = new aMessageKind(_aSynchronousCall);
  public static final int _anAsynchronousCall = 1;
  public static final aMessageKind anAsynchronousCall = new aMessageKind(_anAsynchronousCall);
  public static final int _anExplicitReturn = 2;
  public static final aMessageKind anExplicitReturn = new aMessageKind(_anExplicitReturn);
  /**
   *   message added to indicate the end of a duration bar
   */
  public static final int _anImplicitReturn = 3;
  public static final aMessageKind anImplicitReturn = new aMessageKind(_anImplicitReturn);
  public static final int _aDestruction = 4;
  public static final aMessageKind aDestruction = new aMessageKind(_aDestruction);
  public static final int _anInteractionUse = 5;
  public static final aMessageKind anInteractionUse = new aMessageKind(_anInteractionUse);

  private int value;

  public int value() {
    return value;
  }

  public static aMessageKind fromInt(int value) {
    switch (value) {
    case _aSynchronousCall: return aSynchronousCall;
    case _anAsynchronousCall: return anAsynchronousCall;
    case _anExplicitReturn: return anExplicitReturn;
    case _anImplicitReturn: return anImplicitReturn;
    case _aDestruction: return aDestruction;
    case _anInteractionUse: return anInteractionUse;
    default: throw new Error();
    }
  }

  private aMessageKind(int v) { value = v; }; 
}
