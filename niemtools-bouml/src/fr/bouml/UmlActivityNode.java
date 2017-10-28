package fr.bouml;

abstract class UmlActivityNode extends UmlBaseActivityNode implements UmlActivityItem {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlActivityNode(long id, String s) {
    super(id, s);
  }

}
