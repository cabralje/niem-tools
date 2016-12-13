
abstract class UmlBaseRelation extends UmlItem {
	/**
	 *  returns a new relation of the given 'kind' from 'start' to 'end'
	 * 
	 *  In case it cannot be created (the name is already used or
	 *  invalid, 'parent' cannot contain it etc ...) return 0 in C++
	 *  and produce a RuntimeException in Java
	 */
	public static UmlRelation create(aRelationKind kind, UmlClass start, UmlClass end) throws RuntimeException
	{

		UmlRelation r = (UmlRelation)start.create_(anItemKind.aRelation, "Generalisation");
		if (r != null)
			r._role_type = end;

		return r;
	}

	public UmlClass roleType() {

		return _role_type;
	}

	protected UmlBaseRelation(long id, String n) { super(id, n); }

	protected UmlBaseRelation()
	{
		super();
	}

	protected UmlClass _role_type;

	// protected UmlClass baseType;
}
