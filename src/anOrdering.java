
final class anOrdering {
  public static final int _unordered = 0;
  public static final anOrdering unordered = new anOrdering(_unordered);
  public static final int _ordered = 1;
  public static final anOrdering ordered = new anOrdering(_ordered);
  public static final int _lifo = 2;
  public static final anOrdering lifo = new anOrdering(_lifo);
  public static final int _fifo = 3;
  public static final anOrdering fifo = new anOrdering(_fifo);

  private int value;

  public int value() {
    return value;
  }

  public static anOrdering fromInt(int value) {
    switch (value) {
    case _unordered: return unordered;
    case _ordered: return ordered;
    case _lifo: return lifo;
    case _fifo: return fifo;
    default: throw new Error();
    }
  }

  private anOrdering(int v) { value = v; }; 
}
