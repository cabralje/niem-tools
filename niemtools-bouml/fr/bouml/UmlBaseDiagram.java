package fr.bouml;

/**
 *   Mother class of all the classes managing the diagrams
 */
abstract class UmlBaseDiagram extends UmlItem {
  /**
   *  saves the diagram picture in the file whose pathname is given
   *  in argument, the produced file is a PNG file.
   *  The aspect of the diagram is :
   *  - its visible part in case it is open
   *  - the aspect specified by its prefered size and scale in case
   *    these last are specified (see menu on a diagram view)
   *  - else the default diagram aspect, which is probably not as you want
   * 
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public void saveIn(String s) throws RuntimeException {
    UmlCom.send_cmd(identifier_(), OnInstanceCmd.saveInCmd, s);
    UmlCom.check();
  }

  /**
   *  the constructor, do not call it yourself !!!!!!!!!!
   */
  protected UmlBaseDiagram(long id, String n) { super(id, n); }

};
