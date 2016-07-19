
class FlowBehavior {
  public String weight;

  public String guard;

  public String selection;

  public String transformation;

  public void read() {
    weight = UmlCom.read_string();
    guard = UmlCom.read_string();
    selection = UmlCom.read_string();
    transformation = UmlCom.read_string();
  }

}
