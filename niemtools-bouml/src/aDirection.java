
/**
 *  The direction of an operation's parameter, see UmlParameter
 */
public final class aDirection {
  public static final int _InputOutputDirection = 0;
  public static final aDirection InputOutputDirection = new aDirection(_InputOutputDirection);
  public static final int _InputDirection = 1;
  public static final aDirection InputDirection = new aDirection(_InputDirection);
  public static final int _OutputDirection = 2;
  public static final aDirection OutputDirection = new aDirection(_OutputDirection);
  public static final int _ReturnDirection = 3;
  public static final aDirection ReturnDirection = new aDirection(_ReturnDirection);

  private int value;

  public int value() {
    return value;
  }

  public static aDirection fromInt(int value) {
    switch (value) {
    case _InputOutputDirection: return InputOutputDirection;
    case _InputDirection: return InputDirection;
    case _OutputDirection: return OutputDirection;
    case _ReturnDirection: return ReturnDirection;
    default: throw new Error();
    }
  }

  private aDirection(int v) { value = v; }; 
}
