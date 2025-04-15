package fr.bouml;


/**
 *  The king of a relation, returned by UmlBaseRelation::RelationKind()
 */
public final class aRelationKind {
  public static final int _anAssociation = 0;
  public static final aRelationKind anAssociation = new aRelationKind(_anAssociation);
  public static final int _aDirectionalAssociation = 1;
  public static final aRelationKind aDirectionalAssociation = new aRelationKind(_aDirectionalAssociation);
  public static final int _aGeneralisation = 2;
  public static final aRelationKind aGeneralisation = new aRelationKind(_aGeneralisation);
  public static final int _anAggregation = 3;
  public static final aRelationKind anAggregation = new aRelationKind(_anAggregation);
  public static final int _anAggregationByValue = 4;
  public static final aRelationKind anAggregationByValue = new aRelationKind(_anAggregationByValue);
  public static final int _aDirectionalAggregation = 5;
  public static final aRelationKind aDirectionalAggregation = new aRelationKind(_aDirectionalAggregation);
  public static final int _aDirectionalAggregationByValue = 6;
  public static final aRelationKind aDirectionalAggregationByValue = new aRelationKind(_aDirectionalAggregationByValue);
  public static final int _aDependency = 7;
  public static final aRelationKind aDependency = new aRelationKind(_aDependency);
  public static final int _aRealization = 8;
  public static final aRelationKind aRealization = new aRelationKind(_aRealization);

  private int value;

  public int value() {
    return value;
  }

  public static aRelationKind fromInt(int value) {
    switch (value) {
    case _anAssociation: return anAssociation;
    case _aDirectionalAssociation: return aDirectionalAssociation;
    case _aGeneralisation: return aGeneralisation;
    case _anAggregation: return anAggregation;
    case _anAggregationByValue: return anAggregationByValue;
    case _aDirectionalAggregation: return aDirectionalAggregation;
    case _aDirectionalAggregationByValue: return aDirectionalAggregationByValue;
    case _aDependency: return aDependency;
    case _aRealization: return aRealization;
    default: throw new Error();
    }
  }

  private aRelationKind(int v) { value = v; }; 
}
