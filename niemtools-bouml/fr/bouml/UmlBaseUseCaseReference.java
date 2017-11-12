package fr.bouml;

// import java.util.*;
/**
 *  this class manages use case references
 */
class UmlBaseUseCaseReference {
  /**
   *  return the use case
   */
  public UmlUseCase useCase() {
    return _use_case;
  }

  /**
   *  return the fragment compartment containing the
   *  use case, or 0/null
   */
  public UmlFragmentCompartment fragment() {
    return _fragment;
  }

  /**
   *  return the subject containing the use case, or 0/null
   */
  public UmlSubject subject() {
    return _subject;
  }

  private UmlUseCase _use_case;

  private UmlFragmentCompartment _fragment;

  private UmlSubject _subject;

  private int _x;

  private int _y;

  private int _w;

  private int _h;

  /**
   *  internal, don't call it
   */
  public void read_(UmlFragment[] fragments, UmlSubject[] subjects) {
    _use_case = (UmlUseCase) UmlBaseItem.read_();
    _x = UmlCom.read_unsigned();
    _y = UmlCom.read_unsigned();
    _w = UmlCom.read_unsigned();
    _h = UmlCom.read_unsigned();
    _fragment = UmlBaseFragment.get_container_(_x, _y, _w, _h, fragments);
    _subject = UmlBaseSubject.get_container_(_x, _y, _w, _h, subjects);
  }

}
