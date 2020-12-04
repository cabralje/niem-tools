package fr.bouml;


import java.util.*;
/**
 *  this class manages collaboration diagram definition
 */
class UmlBaseCollaborationDiagramDefinition {
  /**
   *  return the instances present in the diagram
   */
  public UmlClassInstanceReference[] instances() {
    return _instances;
  }

  /**
   *  return the messages present in the diagram,
   *  ordonned following their rank
   */
  public UmlCollaborationMessage[] messages() {
    return _messages;
  }

  private UmlClassInstanceReference[] _instances;

  private UmlCollaborationMessage[] _messages;

  /**
   *  internal, don't call it
   */
  @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
public void read_() {
    Hashtable instances;
    int n;
    int rank;
  
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
    _messages = new UmlCollaborationMessage[n];
    for (rank = 0; rank != n; rank += 1) {
      UmlCollaborationMessage m = new UmlCollaborationMessage();
  
      _messages[rank] = m;
      m.read_(instances);
    }
  }

}
