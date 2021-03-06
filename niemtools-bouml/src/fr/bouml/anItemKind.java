package fr.bouml;


/**
 *  the king of any item, returned by UmlBaseItem::Kind()
 */
public final class anItemKind {
  public static final int _aRelation = 0;
  public static final anItemKind aRelation = new anItemKind(_aRelation);
  public static final int _anAttribute = 1;
  public static final anItemKind anAttribute = new anItemKind(_anAttribute);
  public static final int _anOperation = 2;
  public static final anItemKind anOperation = new anItemKind(_anOperation);
  public static final int _anExtraClassMember = 3;
  public static final anItemKind anExtraClassMember = new anItemKind(_anExtraClassMember);
  public static final int _aClass = 4;
  public static final anItemKind aClass = new anItemKind(_aClass);
  public static final int _anUseCase = 5;
  public static final anItemKind anUseCase = new anItemKind(_anUseCase);
  public static final int _aComponent = 6;
  public static final anItemKind aComponent = new anItemKind(_aComponent);
  public static final int _aNode = 7;
  public static final anItemKind aNode = new anItemKind(_aNode);
  public static final int _anArtifact = 8;
  public static final anItemKind anArtifact = new anItemKind(_anArtifact);
  public static final int _aNcRelation = 9;
  public static final anItemKind aNcRelation = new anItemKind(_aNcRelation);
  public static final int _aClassDiagram = 10;
  public static final anItemKind aClassDiagram = new anItemKind(_aClassDiagram);
  public static final int _anUseCaseDiagram = 11;
  public static final anItemKind anUseCaseDiagram = new anItemKind(_anUseCaseDiagram);
  public static final int _aSequenceDiagram = 12;
  public static final anItemKind aSequenceDiagram = new anItemKind(_aSequenceDiagram);
  public static final int _aCollaborationDiagram = 13;
  public static final anItemKind aCollaborationDiagram = new anItemKind(_aCollaborationDiagram);
  public static final int _aComponentDiagram = 14;
  public static final anItemKind aComponentDiagram = new anItemKind(_aComponentDiagram);
  public static final int _aDeploymentDiagram = 15;
  public static final anItemKind aDeploymentDiagram = new anItemKind(_aDeploymentDiagram);
  public static final int _anObjectDiagram = 16;
  public static final anItemKind anObjectDiagram = new anItemKind(_anObjectDiagram);
  public static final int _anActivityDiagram = 17;
  public static final anItemKind anActivityDiagram = new anItemKind(_anActivityDiagram);
  public static final int _aClassView = 18;
  public static final anItemKind aClassView = new anItemKind(_aClassView);
  public static final int _anUseCaseView = 19;
  public static final anItemKind anUseCaseView = new anItemKind(_anUseCaseView);
  public static final int _aComponentView = 20;
  public static final anItemKind aComponentView = new anItemKind(_aComponentView);
  public static final int _aDeploymentView = 21;
  public static final anItemKind aDeploymentView = new anItemKind(_aDeploymentView);
  public static final int _aPackage = 22;
  public static final anItemKind aPackage = new anItemKind(_aPackage);
  public static final int _aState = 23;
  public static final anItemKind aState = new anItemKind(_aState);
  public static final int _aTransition = 24;
  public static final anItemKind aTransition = new anItemKind(_aTransition);
  public static final int _aRegion = 25;
  public static final anItemKind aRegion = new anItemKind(_aRegion);
  public static final int _aStateDiagram = 26;
  public static final anItemKind aStateDiagram = new anItemKind(_aStateDiagram);
  public static final int _aStateAction = 27;
  public static final anItemKind aStateAction = new anItemKind(_aStateAction);
  public static final int _anInitialPseudoState = 28;
  public static final anItemKind anInitialPseudoState = new anItemKind(_anInitialPseudoState);
  public static final int _anEntryPointPseudoState = 29;
  public static final anItemKind anEntryPointPseudoState = new anItemKind(_anEntryPointPseudoState);
  public static final int _aFinalState = 30;
  public static final anItemKind aFinalState = new anItemKind(_aFinalState);
  public static final int _aTerminatePseudoState = 31;
  public static final anItemKind aTerminatePseudoState = new anItemKind(_aTerminatePseudoState);
  public static final int _anExitPointPseudoState = 32;
  public static final anItemKind anExitPointPseudoState = new anItemKind(_anExitPointPseudoState);
  public static final int _aDeepHistoryPseudoState = 33;
  public static final anItemKind aDeepHistoryPseudoState = new anItemKind(_aDeepHistoryPseudoState);
  public static final int _aShallowHistoryPseudoState = 34;
  public static final anItemKind aShallowHistoryPseudoState = new anItemKind(_aShallowHistoryPseudoState);
  public static final int _aJunctionPseudoState = 35;
  public static final anItemKind aJunctionPseudoState = new anItemKind(_aJunctionPseudoState);
  public static final int _aChoicePseudoState = 36;
  public static final anItemKind aChoicePseudoState = new anItemKind(_aChoicePseudoState);
  public static final int _aForkPseudoState = 37;
  public static final anItemKind aForkPseudoState = new anItemKind(_aForkPseudoState);
  public static final int _aJoinPseudoState = 38;
  public static final anItemKind aJoinPseudoState = new anItemKind(_aJoinPseudoState);
  public static final int _anActivity = 39;
  public static final anItemKind anActivity = new anItemKind(_anActivity);
  public static final int _aFlow = 40;
  public static final anItemKind aFlow = new anItemKind(_aFlow);
  public static final int _anActivityParameter = 41;
  public static final anItemKind anActivityParameter = new anItemKind(_anActivityParameter);
  public static final int _aParameterSet = 42;
  public static final anItemKind aParameterSet = new anItemKind(_aParameterSet);
  public static final int _aPartition = 43;
  public static final anItemKind aPartition = new anItemKind(_aPartition);
  public static final int _anExpansionRegion = 44;
  public static final anItemKind anExpansionRegion = new anItemKind(_anExpansionRegion);
  public static final int _anInterruptibleActivityRegion = 45;
  public static final anItemKind anInterruptibleActivityRegion = new anItemKind(_anInterruptibleActivityRegion);
  public static final int _anOpaqueAction = 46;
  public static final anItemKind anOpaqueAction = new anItemKind(_anOpaqueAction);
  public static final int _anAcceptEventAction = 47;
  public static final anItemKind anAcceptEventAction = new anItemKind(_anAcceptEventAction);
  public static final int _aReadStructuralFeatureAction = 48;
  public static final anItemKind aReadStructuralFeatureAction = new anItemKind(_aReadStructuralFeatureAction);
  public static final int _aClearStructuralFeatureAction = 49;
  public static final anItemKind aClearStructuralFeatureAction = new anItemKind(_aClearStructuralFeatureAction);
  public static final int _aWriteStructuralFeatureAction = 50;
  public static final anItemKind aWriteStructuralFeatureAction = new anItemKind(_aWriteStructuralFeatureAction);
  public static final int _anAddStructuralFeatureValueAction = 51;
  public static final anItemKind anAddStructuralFeatureValueAction = new anItemKind(_anAddStructuralFeatureValueAction);
  public static final int _aRemoveStructuralFeatureValueAction = 52;
  public static final anItemKind aRemoveStructuralFeatureValueAction = new anItemKind(_aRemoveStructuralFeatureValueAction);
  public static final int _aCallBehaviorAction = 53;
  public static final anItemKind aCallBehaviorAction = new anItemKind(_aCallBehaviorAction);
  public static final int _aCallOperationAction = 54;
  public static final anItemKind aCallOperationAction = new anItemKind(_aCallOperationAction);
  public static final int _aSendObjectAction = 55;
  public static final anItemKind aSendObjectAction = new anItemKind(_aSendObjectAction);
  public static final int _aSendSignalAction = 56;
  public static final anItemKind aSendSignalAction = new anItemKind(_aSendSignalAction);
  public static final int _aBroadcastSignalAction = 57;
  public static final anItemKind aBroadcastSignalAction = new anItemKind(_aBroadcastSignalAction);
  public static final int _anUnmarshallAction = 58;
  public static final anItemKind anUnmarshallAction = new anItemKind(_anUnmarshallAction);
  public static final int _aValueSpecificationAction = 59;
  public static final anItemKind aValueSpecificationAction = new anItemKind(_aValueSpecificationAction);
  public static final int _anAcceptCallAction = 60;
  public static final anItemKind anAcceptCallAction = new anItemKind(_anAcceptCallAction);
  public static final int _aReplyAction = 61;
  public static final anItemKind aReplyAction = new anItemKind(_aReplyAction);
  public static final int _aCreateObjectAction = 62;
  public static final anItemKind aCreateObjectAction = new anItemKind(_aCreateObjectAction);
  public static final int _aDestroyObjectAction = 63;
  public static final anItemKind aDestroyObjectAction = new anItemKind(_aDestroyObjectAction);
  public static final int _aTestIdentityAction = 64;
  public static final anItemKind aTestIdentityAction = new anItemKind(_aTestIdentityAction);
  public static final int _aRaiseExceptionAction = 65;
  public static final anItemKind aRaiseExceptionAction = new anItemKind(_aRaiseExceptionAction);
  public static final int _aReduceAction = 66;
  public static final anItemKind aReduceAction = new anItemKind(_aReduceAction);
  public static final int _aStartObjectBehaviorAction = 67;
  public static final anItemKind aStartObjectBehaviorAction = new anItemKind(_aStartObjectBehaviorAction);
  public static final int _aReadSelfAction = 68;
  public static final anItemKind aReadSelfAction = new anItemKind(_aReadSelfAction);
  public static final int _aReadExtentAction = 69;
  public static final anItemKind aReadExtentAction = new anItemKind(_aReadExtentAction);
  public static final int _aReclassifyObjectAction = 70;
  public static final anItemKind aReclassifyObjectAction = new anItemKind(_aReclassifyObjectAction);
  public static final int _aReadIsClassifiedObjectAction = 71;
  public static final anItemKind aReadIsClassifiedObjectAction = new anItemKind(_aReadIsClassifiedObjectAction);
  public static final int _aStartClassifierBehaviorAction = 72;
  public static final anItemKind aStartClassifierBehaviorAction = new anItemKind(_aStartClassifierBehaviorAction);
  public static final int _aReadVariableValueAction = 73;
  public static final anItemKind aReadVariableValueAction = new anItemKind(_aReadVariableValueAction);
  public static final int _aClearVariableValueAction = 74;
  public static final anItemKind aClearVariableValueAction = new anItemKind(_aClearVariableValueAction);
  public static final int _anAddVariableValueAction = 75;
  public static final anItemKind anAddVariableValueAction = new anItemKind(_anAddVariableValueAction);
  public static final int _aRemoveVariableValueAction = 76;
  public static final anItemKind aRemoveVariableValueAction = new anItemKind(_aRemoveVariableValueAction);
  public static final int _anActivityObject = 77;
  public static final anItemKind anActivityObject = new anItemKind(_anActivityObject);
  public static final int _anExpansionNode = 78;
  public static final anItemKind anExpansionNode = new anItemKind(_anExpansionNode);
  public static final int _anActivityPin = 79;
  public static final anItemKind anActivityPin = new anItemKind(_anActivityPin);
  public static final int _anInitialActivityNode = 80;
  public static final anItemKind anInitialActivityNode = new anItemKind(_anInitialActivityNode);
  public static final int _aFlowFinalActivityNode = 81;
  public static final anItemKind aFlowFinalActivityNode = new anItemKind(_aFlowFinalActivityNode);
  public static final int _anActivityFinalActivityNode = 82;
  public static final anItemKind anActivityFinalActivityNode = new anItemKind(_anActivityFinalActivityNode);
  public static final int _aDecisionActivityNode = 83;
  public static final anItemKind aDecisionActivityNode = new anItemKind(_aDecisionActivityNode);
  public static final int _aMergeActivityNode = 84;
  public static final anItemKind aMergeActivityNode = new anItemKind(_aMergeActivityNode);
  public static final int _aForkActivityNode = 85;
  public static final anItemKind aForkActivityNode = new anItemKind(_aForkActivityNode);
  public static final int _aJoinActivityNode = 86;
  public static final anItemKind aJoinActivityNode = new anItemKind(_aJoinActivityNode);
  public static final int _aClassInstance = 87;
  public static final anItemKind aClassInstance = new anItemKind(_aClassInstance);
  public static final int _anExtraArtifactDefinition = 88;
  public static final anItemKind anExtraArtifactDefinition = new anItemKind(_anExtraArtifactDefinition);
  public static final int _aPort = 89;
  public static final anItemKind aPort = new anItemKind(_aPort);
  public static final int _aPortRef = 90;
  public static final anItemKind aPortRef = new anItemKind(_aPortRef);
  public static final int _aClassCompositeDiagram = 91;
  public static final anItemKind aClassCompositeDiagram = new anItemKind(_aClassCompositeDiagram);
  public static final int _anObjectCompositeDiagram = 92;
  public static final anItemKind anObjectCompositeDiagram = new anItemKind(_anObjectCompositeDiagram);
  public static final int _aRolePart = 93;
  public static final anItemKind aRolePart = new anItemKind(_aRolePart);
  public static final int _aRolePartInstance = 94;
  public static final anItemKind aRolePartInstance = new anItemKind(_aRolePartInstance);
  public static final int _aConnector = 95;
  public static final anItemKind aConnector = new anItemKind(_aConnector);
  public static final int _aVariable = 96;
  public static final anItemKind aVariable = new anItemKind(_aVariable);

  private int value;

  public int value() {
    return value;
  }

  public static anItemKind fromInt(int value) {
    switch (value) {
    case _aRelation: return aRelation;
    case _anAttribute: return anAttribute;
    case _anOperation: return anOperation;
    case _anExtraClassMember: return anExtraClassMember;
    case _aClass: return aClass;
    case _anUseCase: return anUseCase;
    case _aComponent: return aComponent;
    case _aNode: return aNode;
    case _anArtifact: return anArtifact;
    case _aNcRelation: return aNcRelation;
    case _aClassDiagram: return aClassDiagram;
    case _anUseCaseDiagram: return anUseCaseDiagram;
    case _aSequenceDiagram: return aSequenceDiagram;
    case _aCollaborationDiagram: return aCollaborationDiagram;
    case _aComponentDiagram: return aComponentDiagram;
    case _aDeploymentDiagram: return aDeploymentDiagram;
    case _anObjectDiagram: return anObjectDiagram;
    case _anActivityDiagram: return anActivityDiagram;
    case _aClassView: return aClassView;
    case _anUseCaseView: return anUseCaseView;
    case _aComponentView: return aComponentView;
    case _aDeploymentView: return aDeploymentView;
    case _aPackage: return aPackage;
    case _aState: return aState;
    case _aTransition: return aTransition;
    case _aRegion: return aRegion;
    case _aStateDiagram: return aStateDiagram;
    case _aStateAction: return aStateAction;
    case _anInitialPseudoState: return anInitialPseudoState;
    case _anEntryPointPseudoState: return anEntryPointPseudoState;
    case _aFinalState: return aFinalState;
    case _aTerminatePseudoState: return aTerminatePseudoState;
    case _anExitPointPseudoState: return anExitPointPseudoState;
    case _aDeepHistoryPseudoState: return aDeepHistoryPseudoState;
    case _aShallowHistoryPseudoState: return aShallowHistoryPseudoState;
    case _aJunctionPseudoState: return aJunctionPseudoState;
    case _aChoicePseudoState: return aChoicePseudoState;
    case _aForkPseudoState: return aForkPseudoState;
    case _aJoinPseudoState: return aJoinPseudoState;
    case _anActivity: return anActivity;
    case _aFlow: return aFlow;
    case _anActivityParameter: return anActivityParameter;
    case _aParameterSet: return aParameterSet;
    case _aPartition: return aPartition;
    case _anExpansionRegion: return anExpansionRegion;
    case _anInterruptibleActivityRegion: return anInterruptibleActivityRegion;
    case _anOpaqueAction: return anOpaqueAction;
    case _anAcceptEventAction: return anAcceptEventAction;
    case _aReadStructuralFeatureAction: return aReadStructuralFeatureAction;
    case _aClearStructuralFeatureAction: return aClearStructuralFeatureAction;
    case _aWriteStructuralFeatureAction: return aWriteStructuralFeatureAction;
    case _anAddStructuralFeatureValueAction: return anAddStructuralFeatureValueAction;
    case _aRemoveStructuralFeatureValueAction: return aRemoveStructuralFeatureValueAction;
    case _aCallBehaviorAction: return aCallBehaviorAction;
    case _aCallOperationAction: return aCallOperationAction;
    case _aSendObjectAction: return aSendObjectAction;
    case _aSendSignalAction: return aSendSignalAction;
    case _aBroadcastSignalAction: return aBroadcastSignalAction;
    case _anUnmarshallAction: return anUnmarshallAction;
    case _aValueSpecificationAction: return aValueSpecificationAction;
    case _anAcceptCallAction: return anAcceptCallAction;
    case _aReplyAction: return aReplyAction;
    case _aCreateObjectAction: return aCreateObjectAction;
    case _aDestroyObjectAction: return aDestroyObjectAction;
    case _aTestIdentityAction: return aTestIdentityAction;
    case _aRaiseExceptionAction: return aRaiseExceptionAction;
    case _aReduceAction: return aReduceAction;
    case _aStartObjectBehaviorAction: return aStartObjectBehaviorAction;
    case _aReadSelfAction: return aReadSelfAction;
    case _aReadExtentAction: return aReadExtentAction;
    case _aReclassifyObjectAction: return aReclassifyObjectAction;
    case _aReadIsClassifiedObjectAction: return aReadIsClassifiedObjectAction;
    case _aStartClassifierBehaviorAction: return aStartClassifierBehaviorAction;
    case _aReadVariableValueAction: return aReadVariableValueAction;
    case _aClearVariableValueAction: return aClearVariableValueAction;
    case _anAddVariableValueAction: return anAddVariableValueAction;
    case _aRemoveVariableValueAction: return aRemoveVariableValueAction;
    case _anActivityObject: return anActivityObject;
    case _anExpansionNode: return anExpansionNode;
    case _anActivityPin: return anActivityPin;
    case _anInitialActivityNode: return anInitialActivityNode;
    case _aFlowFinalActivityNode: return aFlowFinalActivityNode;
    case _anActivityFinalActivityNode: return anActivityFinalActivityNode;
    case _aDecisionActivityNode: return aDecisionActivityNode;
    case _aMergeActivityNode: return aMergeActivityNode;
    case _aForkActivityNode: return aForkActivityNode;
    case _aJoinActivityNode: return aJoinActivityNode;
    case _aClassInstance: return aClassInstance;
    case _anExtraArtifactDefinition: return anExtraArtifactDefinition;
    case _aPort: return aPort;
    case _aPortRef: return aPortRef;
    case _aClassCompositeDiagram: return aClassCompositeDiagram;
    case _anObjectCompositeDiagram: return anObjectCompositeDiagram;
    case _aRolePart: return aRolePart;
    case _aRolePartInstance: return aRolePartInstance;
    case _aConnector: return aConnector;
    case _aVariable: return aVariable;
    default: throw new Error();
    }
  }

  private anItemKind(int v) { value = v; }; 
}
