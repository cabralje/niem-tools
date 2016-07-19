
import java.io.*;
import java.util.*;

abstract class UmlActivityRegion extends UmlBaseActivityRegion implements UmlActivityItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlActivityRegion(long id, String s) {
    super(id, s);
  }

  public boolean chapterp() {
    return true;
  }

}
