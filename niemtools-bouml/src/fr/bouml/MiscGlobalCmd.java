package fr.bouml;

/**
 *  Internal enum
 */
final class MiscGlobalCmd {
  public static final int _byeCmd = 0;
  public static final MiscGlobalCmd byeCmd = new MiscGlobalCmd(_byeCmd);
  public static final int _traceCmd = 1;
  public static final MiscGlobalCmd traceCmd = new MiscGlobalCmd(_traceCmd);
  public static final int _messageCmd = 2;
  public static final MiscGlobalCmd messageCmd = new MiscGlobalCmd(_messageCmd);
  public static final int _toolRunningCmd = 3;
  public static final MiscGlobalCmd toolRunningCmd = new MiscGlobalCmd(_toolRunningCmd);
  public static final int _targetCmd = 4;
  public static final MiscGlobalCmd targetCmd = new MiscGlobalCmd(_targetCmd);
  public static final int _allMarkedCmd = 5;
  public static final MiscGlobalCmd allMarkedCmd = new MiscGlobalCmd(_allMarkedCmd);
  public static final int _loadCmd = 6;
  public static final MiscGlobalCmd loadCmd = new MiscGlobalCmd(_loadCmd);
  public static final int _showTraceCmd = 7;
  public static final MiscGlobalCmd showTraceCmd = new MiscGlobalCmd(_showTraceCmd);
  public static final int _traceAutoRaiseCmd = 8;
  public static final MiscGlobalCmd traceAutoRaiseCmd = new MiscGlobalCmd(_traceAutoRaiseCmd);

  private int value;

  public int value() {
    return value;
  }

  public static MiscGlobalCmd fromInt(int value) {
    switch (value) {
    case _byeCmd: return byeCmd;
    case _traceCmd: return traceCmd;
    case _messageCmd: return messageCmd;
    case _toolRunningCmd: return toolRunningCmd;
    case _targetCmd: return targetCmd;
    case _allMarkedCmd: return allMarkedCmd;
    case _loadCmd: return loadCmd;
    case _showTraceCmd: return showTraceCmd;
    case _traceAutoRaiseCmd: return traceAutoRaiseCmd;
    default: throw new Error();
    }
  }

  private MiscGlobalCmd(int v) { value = v; }; 
}
