
import java.util.*;
/**
 *  this class manages sequence diagram definition
 */
class UmlBaseSequenceDiagramDefinition {
  /**
   *  return the instances present in the diagram
   */
  public UmlClassInstanceReference[] instances() {
    return _instances;
  }

  /**
   *  return the messages present in the diagram,
   *  ordonned following the sending time
   */
  public UmlSequenceMessage[] messages() {
    return _messages;
  }

  /**
   *  return the fragments present in the diagram
   */
  public UmlFragment[] fragments() {
    return _fragments;
  }

  private UmlClassInstanceReference[] _instances;

  private UmlSequenceMessage[] _messages;

  private UmlFragment[] _fragments;

  /**
   *  internal, don't call it
   */
  public void read_() {
    Hashtable instances;
    int n;
    int rank;
  
    n = UmlCom.read_unsigned();
    _fragments = new UmlFragment[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlFragment f = new UmlFragment();
  
      _fragments[rank] = f;
      f.read_();
    }
    UmlBaseFragment.compute_container_(_fragments);
  
    n = UmlCom.read_unsigned();
    _instances = new UmlClassInstanceReference[n];
    instances = new Hashtable((n == 0) ? 1 : n);
    for (rank = 0; rank != n; rank += 1) {
      UmlClassInstanceReference i = new UmlClassInstanceReference();
  
      _instances[rank] = i;
      instances.put(new Integer(UmlCom.read_unsigned()), i);
      i.read_();
    }
  
    n = UmlCom.read_unsigned();
    _messages = new UmlSequenceMessage[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlSequenceMessage m = new UmlSequenceMessage();
  
      _messages[rank] = m;
      m.read_(instances, _fragments);
    }
    Arrays.sort(_messages);
  
    n = _fragments.length;
    for (rank = 0; rank != n; rank += 1)
      _fragments[rank].read_covered_(instances);
  
    while (UmlCom.read_bool()) {
      String s = UmlCom.read_string();
      int x = (int) UmlCom.read_unsigned();
      int y = (int) UmlCom.read_unsigned();
      int w = (int) UmlCom.read_unsigned();
      int h = (int) UmlCom.read_unsigned();
      UmlFragmentCompartment cp = UmlBaseFragment.get_container_(x, y, w, h, _fragments);
  
      if (cp != null) cp.add_text_(s);
    }
  
    while (UmlCom.read_bool()) {
      String s = UmlCom.read_string();
      int x = (int) UmlCom.read_unsigned();
      int y = (int) UmlCom.read_unsigned();
      int w = (int) UmlCom.read_unsigned();
      int h = (int) UmlCom.read_unsigned();
      UmlFragmentCompartment cp = UmlBaseFragment.get_container_(x, y, w, h, _fragments);
  
      if (cp != null) cp.add_cont_(s, y + h/2);
    }
  }

}
