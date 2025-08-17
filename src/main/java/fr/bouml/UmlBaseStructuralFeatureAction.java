package fr.bouml;


abstract class UmlBaseStructuralFeatureAction extends UmlActivityAction {
  /**
   *  return the structural feature
   */
  public UmlItem structuralFeature() {
    read_if_needed_();
    return _structural_feature;
  }

  /**
   *  set the structural feature
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_StructuralFeature(UmlItem v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setDefCmd, (v == null) ? (long) 0 : v.identifier_());
    UmlCom.check();
  
    _structural_feature = v;
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  protected  UmlBaseStructuralFeatureAction(long id, String s) {
    super(id, s);
  }

  protected void read_uml_() {
    super.read_uml_();
    _structural_feature = UmlBaseItem.read_();
  }

  private UmlItem _structural_feature;

}
