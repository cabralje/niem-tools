package fr.bouml;


/**
 *  this class manages subjects
 */
class UmlBaseSubject {
  /**
   *  return the name
   */
  public String name() {
    return _name;
  }

  private String _name;

  private int _x;

  private int _y;

  private int _w;

  private int _h;

  /**
   *  internal, don't call it
   */
  public void read_() {
    _name = UmlCom.read_string();
    _x = UmlCom.read_unsigned();
    _y = UmlCom.read_unsigned();
    _w = UmlCom.read_unsigned();
    _h = UmlCom.read_unsigned();
  }

  /**
   *  internal
   */
  public static UmlSubject get_container_(int x, int y, int w, int h, UmlSubject[] subjects)
  {
    int rank = subjects.length;
  
    while (rank-- != 0) {
      UmlBaseSubject s = subjects[rank];
  
      if ((x > s._x) && (y > s._y) &&
          ((x + w) < (s._x + s._w)) && ((y + h) < (s._y + s._h)))
        return subjects[rank];
    }
    return null;
  }

}
