package fr.bouml;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Java doesn't define a simple Dialog just to ask to a question :-((
 * this one is defined here
 */
@SuppressWarnings("ThisEscapedInObjectConstruction")
class ConfirmBox extends Dialog implements ActionListener{
  @SuppressWarnings("deprecation")
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

  @Override
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
