
abstract class UmlBasePackage extends UmlItem {
	
	public UmlBasePackage() {
	}

	  public UmlBasePackage(UmlItem p, anItemKind apackage, String n) {
			super(p,anItemKind.aPackage,n);
	}

	public static UmlPackage getProject()
	  {
	   // UmlCom.send_cmd(CmdFamily.packageGlobalCmd, PackageGlobalCmd._getProjectCmd);
	    
	   // return (UmlPackage) UmlBaseItem.read_();  
	   return null;
	  }

		public static UmlPackage create(UmlPackage parent, String prefix)
		{
			return (UmlPackage)parent.create_(anItemKind.aPackage, prefix);
		}
	  
}
