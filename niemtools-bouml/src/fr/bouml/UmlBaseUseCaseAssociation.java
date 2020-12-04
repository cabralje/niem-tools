package fr.bouml;


import java.util.*;
/**
 *  this class manages association between use case and actor
 */
class UmlBaseUseCaseAssociation {
  /**
   *  return the name
   */
  public String name() {
    return _name;
  }

  /**
   *  return the stereotype
   */
  public String stereotype() {
    return _stereotype;
  }

  /**
   *  return the use case
   */
  public UmlUseCaseReference useCase() {
    return _use_case;
  }

  /**
   *  return the actor
   */
  public UmlClass actor() {
    return _actor;
  }

  /**
   *  return true if the actor is a primary actor for the use case 
   */
  public boolean primary() {
    return _primary;
  }

  private UmlUseCaseReference _use_case;

  private UmlClass _actor;

  private String _name;

  private String _stereotype;

  private boolean _primary;

  /**
   *  internal, don't call it
   */
  @SuppressWarnings("deprecation")
public void read_(@SuppressWarnings("rawtypes") Hashtable useCases) {
    _use_case = (UmlUseCaseReference) useCases.get(new Integer(UmlCom.read_unsigned()));
    _actor = (UmlClass) UmlBaseItem.read_();
    _primary = UmlCom.read_bool();
    _name = UmlCom.read_string();
    _stereotype = UmlCom.read_string();
  }

}
