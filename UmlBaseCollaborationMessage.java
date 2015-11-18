
import java.util.*;
/**
 *  this class manages messages in a collaboration diagram
 */
class UmlBaseCollaborationMessage extends UmlMessage {
  /**
   *  return the global rank of the message
   */
  public int rank() {
    return _rank;
  }

  /**
   *  return the hierarchical rank of the message
   */
  public String hrank() {
    return _hrank;
  }

  private int _rank;

  private String _hrank;

  /**
   *  internal, don't call it
   */
  public void read_(Hashtable instances) {
    super.read_(instances);
    _rank = UmlCom.read_unsigned();
    _hrank = UmlCom.read_string();
  }

}
