
class UmlInitialActivityNode extends UmlBaseInitialActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlInitialActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "initial activity node";
  }

}
class UmlFlowFinalActivityNode extends UmlBaseFlowFinalActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlFlowFinalActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "flow final";
  }

}
class UmlActivityFinalActivityNode extends UmlBaseActivityFinalActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlActivityFinalActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "activity final";
  }

}
class UmlDecisionActivityNode extends UmlBaseDecisionActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlDecisionActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "decision activity node";
  }

}
class UmlMergeActivityNode extends UmlBaseMergeActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlMergeActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "merge activity node";
  }

}
class UmlForkActivityNode extends UmlBaseForkActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlForkActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "fork activity node";
  }

}
class UmlJoinActivityNode extends UmlBaseJoinActivityNode {
  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlJoinActivityNode(long id, String s) {
    super(id, s);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "join activity node";
  }

}
