
class StateBehavior {
  public String on_entry;

  public String on_exit;

  public String do_activity;

  public void read() {
    on_entry = UmlCom.read_string();
    on_exit = UmlCom.read_string();
    do_activity = UmlCom.read_string();
  }

}
