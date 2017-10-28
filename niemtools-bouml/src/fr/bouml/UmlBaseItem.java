package fr.bouml;

import java.util.*;
/**
 *  Base class of all the classes representing browser's objects.
 * 
 *  In the API all for each 'system' class exists a 'user' class
 *  under your responsability which inherits the 'system' class.
 * 
 *  You do not have to change the 'system' classes, place your
 *  attributes and operations in the corresponding 'user' classes :
 *  all the instances made by the 'system' classes are instance of
 *  the corresponding 'user' classes.
 * 
 *  You must never create or delete yourself a class of the API,
 *  use the Create operation defined in the 'system' classes.
 * 
 *  To not allow you to access to the 'system' attributes or to
 *  call 'system' internal operations even by error, the 'system' internal
 *  parts are declared 'private' rather than 'public' or 'protected'.
 */
@SuppressWarnings("rawtypes")
abstract class UmlBaseItem {
  /**
   *  return a constant identifier, it is unique within a given
   *  kind of element (two classes can't have the same identifiers, but
   *  a class and a component may have the same identifier) 
   *  except for the diagrams (a class diagram can't have the identifier
   *  of a component diagram)
   */
  public int getIdentifier() {
    read_if_needed_();
  
    return _modeler_id;
  }

  /**
   *  returns the kind of the item
   */
  public abstract anItemKind kind() ;

  /**
   *  returns the name
   */
  public String name() { return _name; }

  /**
   *  to set the name
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Name(String s) throws RuntimeException {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.setNameCmd, s);
      UmlCom.check();
      _name = s;
  }

  /**
   *  returns the stereotype
   */
  public String stereotype() {
    read_if_needed_();
    
    return _stereotype;
  }

  /**
   *  to set the stereotype
   *   On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Stereotype(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setStereotypeCmd, s);
    UmlCom.check();
    
    if (_defined) _stereotype = s;
  
  }

  /**
   *  If the current stereotype is part of a profile add needed properties.
   *  In all cases remove extra properties whose keys contain two ':'.
   * 
   *  If the element is read-only, return FALSE in C++, produce a RuntimeException in Java
   */
  public void applyStereotype() throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.applyStereotypeCmd);
    UmlCom.check();
    unload(false, false);
  }

  /**
   *  returns the description
   */
  public String description() {
    read_if_needed_();
    
    return _description;
  }

  /**
   *  to set the description
   *   On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_Description(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDescriptionCmd, s);
    UmlCom.check();
    
    if (_defined) _description = s;
  
  }

  /**
   *  returns the parent (0/null if it is applied on the project itself),
   *  to go all over the browser tree
   */
  public UmlItem parent() {
    if (_parent == null) {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.parentCmd);
      
      _parent = UmlBaseItem.read_();
    }
    
    return _parent;
  }

  /**
   *  returns (in Java : a copy of) the children list, to go all over the browser tree
   */
  public UmlItem[] children() {
    if (_children == null)
      read_children_();
    
    return (UmlItem[]) _children.clone();
  }

  /**
   *  return TRUE if the children exist and are visible (independently
   *  of the scroll bar) in the browser, else FALSE
   */
  public boolean childrenVisible() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.isOpenCmd);
    return UmlCom.read_bool();
  }

  /**
   *  open the item in the browser (its children are visible) if y is TRUE,
   *  else close it (its children are not visible). May be applied on items
   *  without children. The item is also selected in the browser, so 
   *  set_ChildrenVisible(childrenVisible()) is the right way to select the item
   *  in the browser.
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_childrenVisible(boolean y) throws RuntimeException {
    UmlCom.send_cmd(_identifier, OnInstanceCmd.setOpenCmd, (y) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  }

  /**
   *  In C++, in case the key specified by 'k' exist for the current
   *  object, the string 'v' is modified which the associated value
   *  and TRUE is returned, else returns FALSE.
   * 
   *  In Java return the value associated with the key specified by 'k'
   *  for the current object, null in case the property doesn't
   *  exist.
   * 
   *  These properties may be edited through the object dialog last tab
   *  named 'user'
   */
  public String propertyValue(String k) {
    read_if_needed_();
    
    if (_dict == null)
      return null;
    
    return (String) _dict.get(k);
  }

  /**
   *  to set (may be insert a new) the value 'v' associated to the key 'k'
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
@SuppressWarnings("unchecked")
public void set_PropertyValue(String k, String v) throws RuntimeException {
    read_if_needed_();
    
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setCoupleValueCmd, k, v);
    UmlCom.check();
    
    if (_dict == null)
      _dict = new Hashtable();
      
    _dict.put(k, v);
  
  }

  /**
   *  returns all the properties of the object through (in Java a copy of) a dictionnary
   */
public Hashtable properties() {
    read_if_needed_();
  
    return (_dict == null)
      ? new Hashtable()
      : (Hashtable) _dict.clone();
  }

  /**
   *  If the parameter is null, move the current item to be
   *  the first child of its parent. Else the current item and
   *  the parameter must have the same parent, the current item
   *  is moved to be just after the parameter.
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void moveAfter(UmlItem x) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.moveAfterCmd, 
                    (x != null) ? x.identifier_() : 0);
    UmlCom.check();
    parent().reread_children_if_needed_();
  }

  /**
   *   Returns the absolute path name of the file memoring the Item.
   *   In general an item is saved in a file associated to its package,
   *   the exceptions are :
   * 
   *   - the body of the operations of a class which definition contains the
   *      keyword '{$body}'. This path name is returned when you apply
   *      supportFile on any operation of the class.
   * 
   *   - the drawing of the diagrams, this path name is returned when
   *      you apply supportFile on the diagram.
   * 
   *   - the configuration of the tools edited throw the entry 'Tools Settings' of
   *      the menu Tools is saved in the file 'tools'
   * 
   *   - the configuration of the '#include' and 'using' forms associated to the
   *     external type and edited through the last C++ tab of the
   *     'Generation Settings' dialog is saved in the file 'cpp_includes'
   * 
   *   - the configuration of the 'imports' forms associated to the external
   *     types and edited through the last Java tab of the 'Generation Settings'
   *     dialog is saved in the file 'java_imports'
   * 
   *   - the configuration of the '#include' forms associated to the
   *     external type and edited through the last Idl tab of the
   *     'Generation Settings' dialog is saved in the file 'idl_includes'
   *  
   */
  public String supportFile() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.supportFileCmd); 
    return UmlCom.read_string();
  }

  /**
   *  return TRUE in case the item may be modified, i.e. it is not
   *  an API base 'plug-out' class and the corresponding file(s) are not
   *  read-only.
   */
  public boolean isWritable() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.isWritableCmd); 
    return UmlCom.read_bool();
  }

  /**
   *   Apply asynchronously the tool on the item, returns an identifier to call isToolDone()
   */
  public int apply(String cmd) {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.applyCmd, cmd); 
    return UmlCom.read_unsigned();
  }

  /**
   *  return TRUE in case the item is marked
   */
  public boolean isMarked() {
    read_if_needed_();
    return _marked;
  }

  /**
   *  to mark/unmark the current item. The project cannot be marked
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_isMarked(boolean v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setMarkedCmd, (v) ? (byte) 1 : (byte) 0);
    UmlCom.check();
  
    _marked = v;
  }

  /**
   *   Returns the items referencing the current one.
   *   The result may contains UmlAttribute, UmlRelations, UmlNcRelations,
   *   UmlOperation (their bodies are not taken into account) , UmlClass
   *   and UmlComponents.
   */
  public UmlItem[] referencedBy() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.childrenCmd);
    return UmlCom.read_item_list();
  }

  /**
   *  to unload the object to free memory, it will be reloaded automatically
   *  if needed. Recursively done for the sub items if 'rec' is TRUE. 
   * 
   *  if 'del' is true the sub items are deleted in C++, and removed from the
   *  internal dictionnary in C++ and Java (to allow it to be garbaged),
   *  you will have to call Children() to re-access to them
   */
  public void unload(boolean rec, boolean del) {
    _defined = false;
    _stereotype = null;
    _dict = null;
    _description = null;
  
    if (_children != null) {
      if (rec)
        for (int chindex = 0; chindex != _children.length; chindex += 1) {
  	_children[chindex].unload(true, del);
  	if (del)
  	  _all.remove(new Long(_children[chindex].identifier_()));
      }
  
      _children = null;
    }
  }

  /**
   *  remove the element from the model, use it carefully because
   *  after that the element can't be used anymore by the plug-out
   * 
   *  On error : return FALSE in C++, produce a RuntimeException in Java
   */
  public void deleteIt() throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.deleteCmd);
    UmlCom.check();
    parent().unload(true, false);
  }

  /**
   *   'id' is an identifier returned by apply(), indicates if the tool is still running
   */
  public static boolean isToolRunning(int id)
  {
    UmlCom.send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._toolRunningCmd, id, "");
    return UmlCom.read_bool();
  }

  public static UmlItem[] markedItems()
  {
    UmlCom.send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._allMarkedCmd);
    return UmlCom.read_item_list();
  }


/**
 * Do NOT use the next definitions
 */
  private boolean _defined;
  private boolean _marked;
  private long _identifier;
  private int _modeler_id;

  private String _name;
  /**
   *   Note : protected in Java for internal reason, but do NOT
   *   access to this field yourself !
   */
  protected String _stereotype;
  private String _description;
  private UmlItem _parent;
  private UmlItem[] _children;
private Hashtable _dict;
private static Hashtable _all;
static { _all = new Hashtable(997); }  protected final void read_if_needed_() {
    if (!_defined) {
      UmlCom.send_cmd(identifier_(), OnInstanceCmd.getDefCmd);
      read_uml_();
      read_cpp_();
      read_java_();
      read_php_();
      read_python_();
      read_idl_();
      read_mysql_();
      
      _defined = true;
    }
  }

  protected final UmlItem create_(anItemKind k, String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.createCmd, k, s);
  
    UmlItem result = UmlBaseItem.read_();
    
    if (result != null) {
      if (_children != null) {
        int n = _children.length;
        UmlItem[] ch = new UmlItem[n + 1];
        int index;
  
        for (index = 0; index != n; index += 1)
  	ch[index] = _children[index];
        
        ch[index] = result;
        _children = ch;
      }
      ((UmlBaseItem) result)._parent = (UmlItem) this;
    }
    else
      throw new RuntimeException("can't be created");
    
    return result;
  }

  @SuppressWarnings("unchecked")
protected void read_uml_() {
    _stereotype = UmlCom.read_string();
  
    int n = UmlCom.read_unsigned();
    
    if (n != 0) {
      _dict = new Hashtable(n);
      
      do {
        String k = UmlCom.read_string();
        
        _dict.put(k, UmlCom.read_string());
      } while (--n != 0);
    }
    else
      _dict = null;
    
    _description = UmlCom.read_string();
    
    _marked = UmlCom.read_bool();
    _modeler_id = UmlCom.read_unsigned();
  }

  protected void read_cpp_() {
  }

  protected void read_java_() {
  }

  protected void read_php_() {
  }

  /**
   * internal, do NOT use it
   */
  protected void read_python_() {
  }

  protected void read_idl_() {
  }

  /**
   * internal, do NOT use it
   */
  protected void read_mysql_() {
  }

  protected final void read_children_() {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.childrenCmd);
    _children = UmlCom.read_item_list();
    
    int n = _children.length;
    
    while (n-- != 0)
      ((UmlBaseItem) _children[n])._parent = (UmlItem) this;
  }

  protected final void reread_children_if_needed_() {
    if (_children != null)
      read_children_();
  }

  protected static UmlItem read_()
  {
    long id = UmlCom.read_id();
    
    if (id == 0)
      return null;
    
    int kind = (int) UmlCom.read_char();
    String name = UmlCom.read_string();
    
    UmlItem result = (UmlItem) _all.get(new Long(id));
    
    if (result == null) {
      switch (kind) {
      case anItemKind._aRelation:
        return new UmlRelation(id, name);
      case anItemKind._anAttribute:
        return new UmlAttribute(id, name);
      case anItemKind._anOperation:
        return new UmlOperation(id, name);
      case anItemKind._anExtraClassMember:
        return new UmlExtraClassMember(id, name);
      case anItemKind._aClass:
        return new UmlClass(id, name);
      case anItemKind._anUseCase:
        return new UmlUseCase(id, name);
      case anItemKind._aComponent:
        return new UmlComponent(id, name);
      case anItemKind._anArtifact:
        return new UmlArtifact(id, name);
      case anItemKind._aNode:
        return new UmlNode(id, name);
      case anItemKind._aNcRelation:
        return new UmlNcRelation(id, name);
      case anItemKind._aClassDiagram:
        return new UmlClassDiagram(id, name);
      case anItemKind._anUseCaseDiagram:
        return new UmlUseCaseDiagram(id, name);
      case anItemKind._aSequenceDiagram:
        return new UmlSequenceDiagram(id, name);
      case anItemKind._aCollaborationDiagram:
        return new UmlCollaborationDiagram(id, name);
      case anItemKind._aComponentDiagram:
        return new UmlComponentDiagram(id, name);
      case anItemKind._aDeploymentDiagram:
        return new UmlDeploymentDiagram(id, name);
      case anItemKind._aClassView:
        return new UmlClassView(id, name);
      case anItemKind._anUseCaseView:
        return new UmlUseCaseView(id, name);
      case anItemKind._aComponentView:
        return new UmlComponentView(id, name);
      case anItemKind._aDeploymentView:
        return new UmlDeploymentView(id, name);
      case anItemKind._aPackage:
        return new UmlPackage(id, name);
      case anItemKind._aState:
        return new UmlState(id, name);
      case anItemKind._aTransition:
        return new UmlTransition(id, name);
      case anItemKind._aRegion:
        return new UmlRegion(id, name);
      case anItemKind._aStateDiagram:
        return new UmlStateDiagram(id, name);
      case anItemKind._aStateAction:
        return new UmlStateAction(id);
      case anItemKind._anInitialPseudoState:
        return new UmlInitialPseudoState(id);
      case anItemKind._anEntryPointPseudoState:
        return new UmlEntryPointPseudoState(id, name);
      case anItemKind._aFinalState:
        return new UmlFinalState(id);
      case anItemKind._aTerminatePseudoState:
        return new UmlTerminatePseudoState(id);
      case anItemKind._anExitPointPseudoState:
        return new UmlExitPointPseudoState(id, name);
      case anItemKind._aDeepHistoryPseudoState:
        return new UmlDeepHistoryPseudoState(id);
      case anItemKind._aShallowHistoryPseudoState:
        return new UmlShallowHistoryPseudoState(id);
      case anItemKind._aJunctionPseudoState:
        return new UmlJunctionPseudoState(id);
      case anItemKind._aChoicePseudoState:
        return new UmlChoicePseudoState(id);
      case anItemKind._aForkPseudoState:
        return new UmlForkPseudoState(id);
      case anItemKind._aJoinPseudoState:
        return new UmlJoinPseudoState(id);
      case anItemKind._anObjectDiagram:
        return new UmlObjectDiagram(id, name);
      case anItemKind._anActivityDiagram:
        return new UmlActivityDiagram(id, name);
          case anItemKind._anActivity:
        return new UmlActivity(id, name);
      case anItemKind._aFlow:
        return new UmlFlow(id, name);
      case anItemKind._anActivityParameter:
        return new UmlActivityParameter(id, name);
      case anItemKind._aParameterSet:
        return new UmlParameterSet(id, name);
      case anItemKind._anExpansionRegion:
        return new UmlExpansionRegion(id, name);
      case anItemKind._anInterruptibleActivityRegion:
        return new UmlInterruptibleActivityRegion(id, name);
      case anItemKind._anOpaqueAction:
        return new UmlOpaqueAction(id, name);
      case anItemKind._anAcceptEventAction:
        return new UmlAcceptEventAction(id, name);
      case anItemKind._aReadVariableValueAction:
        return new UmlReadVariableValueAction(id, name);
      case anItemKind._aClearVariableValueAction:
        return new UmlClearVariableValueAction(id, name);
      case anItemKind._aWriteVariableValueAction:
        return new UmlWriteVariableValueAction(id, name);
      case anItemKind._anAddVariableValueAction:
        return new UmlAddVariableValueAction(id, name);
      case anItemKind._aRemoveVariableValueAction:
        return new UmlRemoveVariableValueAction(id, name);
      case anItemKind._aCallBehaviorAction:
        return new UmlCallBehaviorAction(id, name);
      case anItemKind._aCallOperationAction:
        return new UmlCallOperationAction(id, name);
      case anItemKind._aSendObjectAction:
        return new UmlSendObjectAction(id, name);
      case anItemKind._aSendSignalAction:
        return new UmlSendSignalAction(id, name);
      case anItemKind._aBroadcastSignalAction:
        return new UmlBroadcastSignalAction(id, name);
      case anItemKind._anUnmarshallAction:
        return new UmlUnmarshallAction(id, name);
      case anItemKind._aValueSpecificationAction:
        return new UmlValueSpecificationAction(id, name);
      case anItemKind._anActivityObject:
        return new UmlActivityObject(id, name);
      case anItemKind._anExpansionNode:
        return new UmlExpansionNode(id, name);
      case anItemKind._anActivityPin:
        return new UmlActivityPin(id, name);
      case anItemKind._anInitialActivityNode:
        return new UmlInitialActivityNode(id, name);
      case anItemKind._aFlowFinalActivityNode:
        return new UmlFlowFinalActivityNode(id, name);
      case anItemKind._anActivityFinalActivityNode:
        return new UmlActivityFinalActivityNode(id, name);
      case anItemKind._aDecisionActivityNode:
        return new UmlDecisionActivityNode(id, name);
      case anItemKind._aMergeActivityNode:
        return new UmlMergeActivityNode(id, name);
      case anItemKind._aForkActivityNode:
        return new UmlForkActivityNode(id, name);
      case anItemKind._aJoinActivityNode:
        return new UmlJoinActivityNode(id, name);
      case anItemKind._aPartition:
        return new UmlActivityPartition(id, name);
      case anItemKind._aClassInstance:
        return new UmlClassInstance(id, name);
      case anItemKind._anAcceptCallAction:
        return new UmlAcceptCallAction(id, name);
      case anItemKind._aReplyAction:
        return new UmlReplyAction(id, name);
      case anItemKind._aCreateObjectAction:
        return new UmlCreateObjectAction(id, name);
      case anItemKind._aDestroyObjectAction:
        return new UmlDestroyObjectAction(id, name);
      case anItemKind._aTestIdentityAction:
        return new UmlTestIdentityAction(id, name);
      case anItemKind._aRaiseExceptionAction:
        return new UmlRaiseExceptionAction(id, name);
      case anItemKind._aReduceAction:
        return new UmlReduceAction(id, name);
      default:
        UmlCom.bye(-1);
        UmlCom.fatal_error(new String("unknown item type ") + String.valueOf(kind));
      }
    }
  
    return result;
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  @SuppressWarnings("unchecked")
public UmlBaseItem(long id, String n) {
    _defined = false;
    _identifier = id;
    _name = n;
    
    _all.put(new Long(id), this);
  
  }

  public final long identifier_() { return _identifier; }

  public boolean defined_() { return _defined; }

};
