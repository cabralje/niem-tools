package fr.bouml;


/**
 *  this class manages fragments compartments,
 *  a fragment without separator contains one compartment
 */
class UmlBaseFragmentCompartment {
  /**
   *  return the fragment owning the compartment
   */
  public UmlFragment fragment() {
    return _fragment;
  }

  /**
   *  the rank of the compartment in the fragment (0..)
   */
  public int rank() {
    return _rank;
  }

  /**
   *  the texts placed in the compartment
   */
  public String[] texts() {
    return _texts;
  }

  /**
   *  return the fragments contained in the compartment,
   *  may be none
   */
  public UmlFragment[] contained() {
    return _contained;
  }

  /**
   *  return the continuation ('label' case), or an empty string/null
   */
  public String startContinuation() {
    return _start_continuation;
  }

  /**
   *  return the continuation ('goto' case), or an empty string/null
   */
  public String endContinuation() {
    return _end_continuation;
  }

  private UmlFragment _fragment;

  private UmlFragment[] _contained;

  private int _rank;

  private String[] _texts;

  private int _y;

  private String _start_continuation;

  private String _end_continuation;

  public void add_contained_(UmlFragment x) {
    UmlFragment[] v = _contained;
    int n = _contained.length;
  
    _contained = new UmlFragment[n + 1];
    System.arraycopy(v, 0, _contained, 0, n);
    _contained[n] = x;
  }

  public void add_text_(String x) {
    String[] v = _texts;
    int n = _texts.length;
  
    _texts = new String[n + 1];
    System.arraycopy(v, 0, _texts, 0, n);
    _texts[n] = x;
  }

  /**
   * internal, do NOT use it
   */
  public void add_cont_(String s, int cy) {
    if (cy < _fragment.vcenter_(_rank))
      _start_continuation = s;
    else
      _end_continuation = s;
  }

  public int b() {
    return _y;
  }

  public boolean smaller(UmlBaseFragmentCompartment x) {
    return ((_fragment.w() < x._fragment.w()) &&
            (_fragment.h() < x._fragment.h()));
  }

  /**
   *  internal, don't call it
   */
  public void read_(UmlBaseFragment fragment, int rank) {
    _fragment = (UmlFragment) fragment;
    _rank = rank;
    _y = UmlCom.read_unsigned();
    _contained = new UmlFragment[0];
    _texts = new String[0];
  }

}
