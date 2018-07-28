package fr.bouml;

import java.util.*;
/**
 *  this class manages fragments
 */
class UmlBaseFragment {
  /**
   *  return the name
   */
  public String name() {
    return _name;
  }

  /**
   *  return the compartments, at least one compartment exists
   */
  public UmlFragmentCompartment[] compartments() {
    return _compartments;
  }

  /**
   *  return the fragment compartment containing the fragment,
   *  or 0/null
   */
  public UmlFragmentCompartment container() {
    return _container;
  }

  /**
   *  return the diagram optionally referenced by the fragment,
   *  generally associated to an interaction use
   */
  public UmlDiagram refer() {
    return _refer;
  }

  /**
   *  return the form corresponding to the arguments
   *  and return value of the interaction use
   */
  public String arguments() {
    return _arguments;
  }

  /**
   *  return the the list of covered instances (life lines)
   */
  public UmlClassInstanceReference[] covered() {
    return _covered.clone();
  }

  private UmlFragmentCompartment _container;

  private UmlFragmentCompartment[] _compartments;

  private String _name;

  private int _x;

  private int _y;

  private int _w;

  private int _h;

  private UmlDiagram _refer;

  private String _arguments;

  private UmlClassInstanceReference[] _covered;

  /**
   *  internal, don't call it
   */
  public void read_() {
    _name = UmlCom.read_string();
    _x = UmlCom.read_unsigned();
    _y = UmlCom.read_unsigned();
    _w = UmlCom.read_unsigned();
    _h = UmlCom.read_unsigned();
  
    int n = UmlCom.read_unsigned();
    int rank;
  
    _compartments = new UmlFragmentCompartment[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlFragmentCompartment fc = new UmlFragmentCompartment();
  
      _compartments[rank] = fc;
      fc.read_(this, rank);
    }
    _refer = (UmlDiagram) UmlBaseItem.read_();
    _arguments = UmlCom.read_string();
  }

  /**
   * internal, do NOT use it
   */
  public int vcenter_(int rank) {
    int t = (rank == 0) ? _y : _compartments[rank - 1].b();
  
    return (t + _compartments[rank].b())/2;
  }

  /**
   * internal, do NOT use it
   */
  @SuppressWarnings({ "rawtypes", "deprecation" })
public void read_covered_(Hashtable instances) {
    int n = UmlCom.read_unsigned();
    int rank;
  
    _covered = new UmlClassInstanceReference[n];
    for (rank = 0; rank != n; rank += 1)
      _covered[rank] = (UmlClassInstanceReference) instances.get(new Integer(UmlCom.read_unsigned()));
  }

  /**
   *  internal
   */
  public static UmlFragmentCompartment get_container_(int x, int y, int w, int h, UmlFragment[] fragments)
  {
    UmlFragmentCompartment r = null;
    int nf = fragments.length;
    int frank;
  
    for (frank = 0; frank != nf; frank += 1) {
      UmlBaseFragment f = fragments[frank];
  
      if ((x > f._x) && (y > f._y) &&
          ((x + w) < (f._x + f._w)) && ((y + h) < (f._y + f._h))) {
        int y0 = f._y;
        int nfc = f._compartments.length;
        int fcrank;
  
        for (fcrank = 0; fcrank != nfc; fcrank += 1) {
          UmlBaseFragmentCompartment fc = f._compartments[fcrank];
  
          if ((y > y0) &&
              ((y + h) < fc.b()) &&
              ((r == null) || fc.smaller(r))) {
            r = f._compartments[fcrank];
            break;
          }
          y0 = fc.b();
        }
      }
    }
    return r;
  }

  /**
   *  internal
   */
  public static void compute_container_(UmlFragment[] fragments)
  {
    int rank = fragments.length;
  
    while (rank-- != 0) {
      UmlBaseFragment f = fragments[rank];
      UmlFragmentCompartment fc = get_container_(f._x, f._y, f._w, f._h, fragments);
  
      if (fc != null) {
        f._container = fc;
        fc.add_contained_((UmlFragment) f);
      }
    }
  }

  public int w() {
    return _w;
  }

  public int h() {
    return _h;
  }

}
