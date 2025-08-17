package fr.bouml;


import java.io.*;
/**
 *  This class allows to manage diagram
 * 
 *  You can modify it as you want (except the constructor)
 */
abstract class UmlDiagram extends UmlBaseDiagram {
  public UmlDiagram(long id, String n){ super(id, n); }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
  
    define();
  
    String s = "fig" + String.valueOf(getIdentifier()) + ((svg) ? ".svg" : ".png");
    
    saveIn(directory + "/" + s);
    
    if (svg) {
      fw.write("<p align=\"center\"><embed src=\"");
      fw.write(s);
      fw.write('"');
      
      s = directory + s;
      
      BufferedReader in = null;
      
      try {
        in = new BufferedReader(new FileReader(s));
      
        in.readLine();	// <?xml version="1.0" ...
        in.readLine();	// <!-- Created with Bouml ...
        in.readLine();	// <!DOCTYPE svg PUBLIC ...
        s = in.readLine();	// <svg width="495" height="560" version=...
        
        int p = s.indexOf(" version=");
        
        if (p != -1)
  	fw.write(s.substring(4, p));
      }
      catch(FileNotFoundException e1) {
      }
      catch(Exception e2) {
      }
      
      if (in != null) {
        try {
  	in.close();
        }
        catch(IOException e3) {
        }
      }
      
      fw.write("/></p>\n");
    }
    else {
      fw.write("<p align=\"center\"><img src=\"");
      fw.write(s);
      fw.write("\" alt=\"\" /></p>\n");
    }
    
    fw.write(" <p align=\"center\"><b>");
    writeq(name());
    fw.write("</b></p><p><br /></p><p><br /></p>\n");
  
    write_description();
  
    write_properties();
  
    unload(false, false);
  
  }

};
