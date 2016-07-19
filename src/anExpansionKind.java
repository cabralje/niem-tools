
final class anExpansionKind {
  public static final int _parallelExecution = 0;
  public static final anExpansionKind parallelExecution = new anExpansionKind(_parallelExecution);
  public static final int _iterativeExecution = 1;
  public static final anExpansionKind iterativeExecution = new anExpansionKind(_iterativeExecution);
  public static final int _streamExecution = 2;
  public static final anExpansionKind streamExecution = new anExpansionKind(_streamExecution);

  private int value;

  public int value() {
    return value;
  }

  public static anExpansionKind fromInt(int value) {
    switch (value) {
    case _parallelExecution: return parallelExecution;
    case _iterativeExecution: return iterativeExecution;
    case _streamExecution: return streamExecution;
    default: throw new Error();
    }
  }

  private anExpansionKind(int v) { value = v; }; 
}
