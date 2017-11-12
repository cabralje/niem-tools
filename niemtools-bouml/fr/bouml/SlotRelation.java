package fr.bouml;

class SlotRelation {
  public UmlRelation relation;

  public UmlClassInstance value;

  public  SlotRelation(UmlRelation r, UmlClassInstance v) {
    relation = r;
    value = v;
  }

}
