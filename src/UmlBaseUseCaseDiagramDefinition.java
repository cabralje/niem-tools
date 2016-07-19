
import java.util.*;
/**
 *  this class manages use case diagram definition
 */
class UmlBaseUseCaseDiagramDefinition {
  /**
   *  return the use cases present in the diagram
   */
  public UmlUseCaseReference[] useCases() {
    return _use_cases;
  }

  /**
   *  return the actors present in the diagram
   */
  public UmlClass[] actors() {
    return _actors;
  }

  /**
   *  return the associations between actor and use case present in the diagram
   */
  public UmlUseCaseAssociation[] associations() {
    return _rels;
  }

  /**
   *  return the fragments present in the diagram
   */
  public UmlFragment[] fragments() {
    return _fragments;
  }

  /**
   *  return the subjects present in the diagram
   */
  public UmlSubject[] subjects() {
    return _subjects;
  }

  private UmlUseCaseReference[] _use_cases;

  private UmlClass[] _actors;

  private UmlUseCaseAssociation[] _rels;

  private UmlFragment[] _fragments;

  private UmlSubject[] _subjects;

  /**
   *  internal, don't call it
   */
  public void read_() {
    Hashtable ucrefs;
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
    _subjects = new UmlSubject[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlSubject sb = new UmlSubject();
  
      _subjects[rank] = sb;
      sb.read_();
    }
  
    n = UmlCom.read_unsigned();
    _use_cases = new UmlUseCaseReference[n];
    ucrefs = new Hashtable((n == 0) ? 1 : n);
    for (rank = 0; rank != n; rank += 1) {
      UmlUseCaseReference uc = new UmlUseCaseReference();
  
      _use_cases[rank] = uc;
      ucrefs.put(new Integer(UmlCom.read_unsigned()), uc);
      uc.read_(_fragments, _subjects);
    }
  
    n = UmlCom.read_unsigned();
    _actors = new UmlClass[n];
    for (rank = 0; rank != n; rank += 1)
      _actors[rank] = (UmlClass) UmlBaseItem.read_();
  
    n = UmlCom.read_unsigned();
    _rels = new UmlUseCaseAssociation[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlUseCaseAssociation r = new UmlUseCaseAssociation();
  
      _rels[rank] = r;
      r.read_(ucrefs);
    }
  }

}
