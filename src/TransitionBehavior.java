
class TransitionBehavior {
  public String trigger;

  public String guard;

  public String activity;

  public void read() {
    trigger = UmlCom.read_string();
    guard = UmlCom.read_string();
    activity = UmlCom.read_string();
  }

}
