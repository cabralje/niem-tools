
import java.util.*;
/**
 *  this class manages messages indenpendently of the diagram
 */
class UmlBaseMessage {
  /**
   *  return the instance sending the message
   */
  public UmlClassInstanceReference from() {
    return _from;
  }

  /**
   *  return the instance receiving the message
   */
  public UmlClassInstanceReference to() {
    return _to;
  }

  /**
   *  return the operation or 0/null,
   *  exclusive with form()
   */
  public UmlOperation operation() {
    return _operation;
  }

  /**
   *  return message as a string, may be empty/null,
   *  exclusive with operation()
   */
  public String form() {
    return _form;
  }

  private UmlClassInstanceReference _from;

  private UmlClassInstanceReference _to;

  private UmlOperation _operation;

  private String _form;

  /**
   *  internal, don't call it
   */
  public void read_(Hashtable instances) {
    _operation = (UmlOperation) UmlBaseItem.read_();
    if (_operation == null) _form = UmlCom.read_string();
    _from = (UmlClassInstanceReference) instances.get(new Integer(UmlCom.read_unsigned()));
    _to = (UmlClassInstanceReference) instances.get(new Integer(UmlCom.read_unsigned()));
  }

}
