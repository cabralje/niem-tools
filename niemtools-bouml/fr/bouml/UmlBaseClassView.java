package fr.bouml;

/**
 *  Manage the class views
 */
public abstract class UmlBaseClassView extends UmlView {
  /**
   *  returns a new class view named 'name' created under 'parent'
   * 
   *  In case it cannot be created (the name is already used or
   *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
   *  and produce a RuntimeException in Java
   */
  public static UmlClassView create(UmlPackage parent, String s) throws RuntimeException
  {
    return (UmlClassView) parent.create_(anItemKind.aClassView, s);
  }

  /**
   *  returns the kind of the item
   */
  public anItemKind kind() {
    return anItemKind.aClassView;
  }

  /**
   *  returns the optional associated deployment view
   */
  public UmlDeploymentView associatedDeploymentView() {
    read_if_needed_();
    
    return _assoc_view;
  }

  /**
   *  sets the associated deployment view, arg may be null to unset it
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void set_AssociatedDeploymentView(UmlDeploymentView v) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.setAssocViewCmd, v.identifier_());
    UmlCom.check();
    
    _assoc_view = v;
  
  }

  private UmlDeploymentView _assoc_view;
  protected void read_uml_() {
    _assoc_view = (UmlDeploymentView) UmlBaseItem.read_();
    super.read_uml_();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseClassView(long id, String n) { super(id, n); }

};
