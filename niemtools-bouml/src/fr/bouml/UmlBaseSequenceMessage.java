package fr.bouml;

import java.util.*;
/**
 *  this class manages messages in a sequence diagram
 */
@SuppressWarnings("rawtypes")
class UmlBaseSequenceMessage extends UmlMessage implements java.lang.Comparable {
  /**
   *  return kind of the message
   */
  public aMessageKind kind() {
    return _kind;
  }

  /**
   *  return the stereotype of the message
   */
  public String stereotype() {
    return _stereotype;
  }

  /**
   *  return the fragment compartment containing the message
   */
  public UmlFragmentCompartment fragment() {
    return _fragment;
  }

  /**
   *  return when the message is sent (arbitrary unit)
   */
  public int sentAt() {
    return _send_at;
  }

  /**
   *  return when the message is received (arbitrary unit)
   */
  public int receivedAt() {
    return _received_at;
  }

  /**
   *  return the arguments of the operation, may be empty
   */
  public String arguments() {
    return _args;
  }

  public final int compareTo(Object other) {
    return _send_at - ((UmlBaseSequenceMessage) other)._send_at;
  }

  private aMessageKind _kind;

  private UmlFragmentCompartment _fragment;

  private int _x;

  private int _send_at;

  private int _received_at;

  private String _args;

  private String _stereotype;

  /**
   *  internal, don't call it
   */
  public void read_(Hashtable instances, UmlFragment[] fragments) {
    super.read_(instances);
    _kind = aMessageKind.fromInt((int) UmlCom.read_char());
    _args = UmlCom.read_string();
    _stereotype = UmlCom.read_string();
    _x = UmlCom.read_unsigned();
    _send_at = UmlCom.read_unsigned();
    _received_at = UmlCom.read_unsigned();
    _fragment = UmlBaseFragment.get_container_(_x, _send_at, 1, 1, fragments);
  }

}
