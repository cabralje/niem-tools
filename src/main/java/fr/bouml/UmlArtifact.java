package fr.bouml;


import java.io.*;

/**
 *  This class manages 'artifacts'
 * 
 *  You can modify it as you want (except the constructor)
 */
class UmlArtifact extends UmlBaseArtifact {
  public UmlArtifact(long id, String n){ super(id, n); }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    UmlCom.message(name());
  
    html("Artifact", associatedDiagram());
    
    UmlItem[] l;
    
    if (stereotype().equals("source")) {
      fw.write("<p>Artifact <i>source</i>");
      l = associatedElements();
    }
    else if (stereotype().equals("database")) {
      fw.write("<p>Artifact <i>database</i>");
      l = associatedElements();
    }
    else {
      fw.write("<p><i>");
      writeq(stereotype());
      fw.write("</i>");
      l = associatedArtifacts();
    }
  
    String sep = " associated with : ";
    
    for (int i = 0; i != l.length; i += 1) {
      fw.write(sep);
      l[i].write();
      sep = ", ";
    }
  
    fw.write("</p>\n");
    
    UmlItem[] ch = children();
    
    if (ch.length != 0) {
      String spfix = (rank == 0)
        ? ""
        : (pfix + String.valueOf(rank) + ".");
      
      level += 1;
      rank = 1;
      fw.write("<div class=\"sub\">\n");    
      for (int i = 0; i != ch.length; i += 1) {
        if (ch[i].kind() == anItemKind.anExtraArtifactDefinition) {
  	ch[i].html(spfix, rank, level);
  	if (ch[i].chapterp())
  	  rank += 1;
        }
      }
      fw.write("</div>\n");
    }
  
    unload(false, false);
  }

  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "artifact";
  }

};
