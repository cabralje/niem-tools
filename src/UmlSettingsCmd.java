
/**
 *  Internal enum
 */
final class UmlSettingsCmd {
  public static final int _getUmlSettingsCmd = 0;
  public static final UmlSettingsCmd getUmlSettingsCmd = new UmlSettingsCmd(_getUmlSettingsCmd);
  public static final int _setDefaultArtifactDescriptionCmd = 50;
  public static final UmlSettingsCmd setDefaultArtifactDescriptionCmd = new UmlSettingsCmd(_setDefaultArtifactDescriptionCmd);
  public static final int _setDefaultClassDescriptionCmd = 51;
  public static final UmlSettingsCmd setDefaultClassDescriptionCmd = new UmlSettingsCmd(_setDefaultClassDescriptionCmd);
  public static final int _setDefaultOperationDescriptionCmd = 52;
  public static final UmlSettingsCmd setDefaultOperationDescriptionCmd = new UmlSettingsCmd(_setDefaultOperationDescriptionCmd);
  public static final int _setDefaultAttributeDescriptionCmd = 53;
  public static final UmlSettingsCmd setDefaultAttributeDescriptionCmd = new UmlSettingsCmd(_setDefaultAttributeDescriptionCmd);
  public static final int _setDefaultRelationDescriptionCmd = 54;
  public static final UmlSettingsCmd setDefaultRelationDescriptionCmd = new UmlSettingsCmd(_setDefaultRelationDescriptionCmd);
  public static final int _setUmlDefaultGetNameCmd = 55;
  public static final UmlSettingsCmd setUmlDefaultGetNameCmd = new UmlSettingsCmd(_setUmlDefaultGetNameCmd);
  public static final int _setUmlDefaultSetNameCmd = 56;
  public static final UmlSettingsCmd setUmlDefaultSetNameCmd = new UmlSettingsCmd(_setUmlDefaultSetNameCmd);

  private int value;

  public int value() {
    return value;
  }

  public static UmlSettingsCmd fromInt(int value) {
    switch (value) {
    case _getUmlSettingsCmd: return getUmlSettingsCmd;
    case _setDefaultArtifactDescriptionCmd: return setDefaultArtifactDescriptionCmd;
    case _setDefaultClassDescriptionCmd: return setDefaultClassDescriptionCmd;
    case _setDefaultOperationDescriptionCmd: return setDefaultOperationDescriptionCmd;
    case _setDefaultAttributeDescriptionCmd: return setDefaultAttributeDescriptionCmd;
    case _setDefaultRelationDescriptionCmd: return setDefaultRelationDescriptionCmd;
    case _setUmlDefaultGetNameCmd: return setUmlDefaultGetNameCmd;
    case _setUmlDefaultSetNameCmd: return setUmlDefaultSetNameCmd;
    default: throw new Error();
    }
  }

  private UmlSettingsCmd(int v) { value = v; }; 
}
