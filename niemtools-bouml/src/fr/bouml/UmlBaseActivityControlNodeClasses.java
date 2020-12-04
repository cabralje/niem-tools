package fr.bouml;


abstract class UmlBaseInitialActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new initial activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlInitialActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlInitialActivityNode) parent.create_(anItemKind.anInitialActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anInitialActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseInitialActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseFlowFinalActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new flow final activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlFlowFinalActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlFlowFinalActivityNode) parent.create_(anItemKind.aFlowFinalActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aFlowFinalActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseFlowFinalActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseActivityFinalActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new activity final activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlActivityFinalActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlActivityFinalActivityNode) parent.create_(anItemKind.anActivityFinalActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.anActivityFinalActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseActivityFinalActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseDecisionActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new decision activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlDecisionActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlDecisionActivityNode) parent.create_(anItemKind.aDecisionActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aDecisionActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseDecisionActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseMergeActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new merge activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlMergeActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlMergeActivityNode) parent.create_(anItemKind.aMergeActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aMergeActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseMergeActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseForkActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new fork activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlForkActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlForkActivityNode) parent.create_(anItemKind.aForkActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aForkActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseForkActivityNode(long id, String s) {
    super(id, s);
  }

}
abstract class UmlBaseJoinActivityNode extends UmlActivityControlNode {
  /**
   *   returns a new join activity control node named 's' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlJoinActivityNode create(UmlItem parent, String s) throws RuntimeException
  {
    return (UmlJoinActivityNode) parent.create_(anItemKind.aJoinActivityNode, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aJoinActivityNode;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseJoinActivityNode(long id, String s) {
    super(id, s);
  }

}
