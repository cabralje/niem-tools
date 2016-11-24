import java.awt.*;
import java.awt.event.*;


/**
 * Java doesn't define a simple Dialog just to ask to a question :-((
 * this one is defined here
 */
@SuppressWarnings({ "serial" , "deprecation"})
class ConfirmBox extends Dialog implements ActionListener{
  public ConfirmBox(String msg) {
    super(new Frame(), "Html generator", true);
    setLayout(new BorderLayout());
    add("Center", new Label(msg));
    
    Panel p = new Panel();
    
    p.setLayout(new FlowLayout());
    p.add(yes = new Button("Yes"));
    yes.addActionListener(this); 
    p.add(no = new Button("No"));
    no.addActionListener(this); 
    add("South",p);
    pack();
  
    Dimension d = getToolkit().getScreenSize();
    setLocation(d.width/2,d.height/2);
    
    setModal(true);
    show();
  }

  public boolean ok() {
    return choice;
  }

  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource() == yes) {
      choice = true;
      dispose();
    }
    else if(ae.getSource() == no) {
      choice = false;
      dispose();
    }
  }

  protected boolean choice;

  protected Button yes;

  protected Button no;

}
