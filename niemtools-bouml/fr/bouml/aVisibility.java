package fr.bouml;

/**
 *  The visibility of a class member
 * 
 *  The 'default visibility' is used to configure the Drawing Settings and
 *  for the C++ cases (see UmlBaseClassMember::CppVisibility())
 */
public final class aVisibility {
  public static final int _PublicVisibility = 0;
  public static final aVisibility PublicVisibility = new aVisibility(_PublicVisibility);
  public static final int _ProtectedVisibility = 1;
  public static final aVisibility ProtectedVisibility = new aVisibility(_ProtectedVisibility);
  public static final int _PrivateVisibility = 2;
  public static final aVisibility PrivateVisibility = new aVisibility(_PrivateVisibility);
  public static final int _PackageVisibility = 3;
  public static final aVisibility PackageVisibility = new aVisibility(_PackageVisibility);
  public static final int _DefaultVisibility = 4;
  public static final aVisibility DefaultVisibility = new aVisibility(_DefaultVisibility);

  private int value;

  public int value() {
    return value;
  }

  public static aVisibility fromInt(int value) {
    switch (value) {
    case _PublicVisibility: return PublicVisibility;
    case _ProtectedVisibility: return ProtectedVisibility;
    case _PrivateVisibility: return PrivateVisibility;
    case _PackageVisibility: return PackageVisibility;
    case _DefaultVisibility: return DefaultVisibility;
    default: throw new Error();
    }
  }

  private aVisibility(int v) { value = v; }; 
}
