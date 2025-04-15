package fr.bouml;


import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
class UmlSendObjectAction extends UmlBaseSendObjectAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "send object activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlSendObjectAction(long id, String s) {
    super(id, s);
  }

}
class UmlUnmarshallAction extends UmlBaseUnmarshallAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "unmarshall activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlUnmarshallAction(long id, String s) {
    super(id, s);
  }

}
class UmlSendSignalAction extends UmlBaseSendSignalAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "send signal activity action";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlSendSignalAction(long id, String s) {
    super(id, s);
  }

}
class UmlBroadcastSignalAction extends UmlBaseBroadcastSignalAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "broadcast signal activity action";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlBroadcastSignalAction(long id, String s) {
    super(id, s);
  }

}
class UmlValueSpecificationAction extends UmlBaseValueSpecificationAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "value specification activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    String s = value();
    String scpp = cppValue();
    String sjava = javaValue();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Value :</p><ul>");
    
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlValueSpecificationAction(long id, String s) {
    super(id, s);
  }

}
class UmlOpaqueAction extends UmlBaseOpaqueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "opaque activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    String s = behavior();
    String scpp = cppBehavior();
    String sjava = javaBehavior();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Behavior :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlOpaqueAction(long id, String s) {
    super(id, s);
  }

}
class UmlAcceptEventAction extends UmlBaseAcceptEventAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "accept event activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isUnmarshall()) {
      if (isTimeEvent())
        fw.write("<p>Unmarshall, event is a time event</p>");
      else
        fw.write("<p>Unmarshall</p>");
    }
    else if (isTimeEvent())
      fw.write("<p>Event is a time event</p>");
    
    String s = trigger ();
    String scpp = cppTrigger();
    String sjava = javaTrigger();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Trigger :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
  
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlAcceptEventAction(long id, String s) {
    super(id, s);
  }

}
class UmlCallOperationAction extends UmlBaseCallOperationAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "call operation activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isSynchronous())
      fw.write("<p>Is synchronous</p>");
      
    if (operation() != null){
      fw.write("<p>Operation : ");
      operation().write();
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlCallOperationAction(long id, String s) {
    super(id, s);
  }

}
class UmlCallBehaviorAction extends UmlBaseCallBehaviorAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "call behavior activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isSynchronous())
      fw.write("<p>Is synchronous</p>");
      
    if (behavior() != null){
      fw.write("<p>Behavior : ");
      behavior().write();
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlCallBehaviorAction(long id, String s) {
    super(id, s);
  }

}
class UmlClearVariableValueAction extends UmlBaseClearVariableValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "clear variable value activity action";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlClearVariableValueAction(long id, String s) {
    super(id, s);
  }

}
class UmlReadVariableValueAction extends UmlBaseReadVariableValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "read variable value activity action";
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReadVariableValueAction(long id, String s) {
    super(id, s);
  }

}
class UmlAddVariableValueAction extends UmlBaseAddVariableValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "add variable value activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html();
  
    if (isReplaceAll())
      fw.write("<p>Replace all</p>");
      
    if (variable() != null){
      fw.write("<p>Variable : ");
      variable().write();
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlAddVariableValueAction(long id, String s) {
    super(id, s);
  }

}
class UmlRemoveVariableValueAction extends UmlBaseRemoveVariableValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "remove variable value activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    html();
  
    if (isRemoveDuplicates())
      fw.write("<p>Remove duplicates</p><ul>");
      
    if (variable() != null){
      fw.write("<p>Variable : ");
      variable().write();
      fw.write("</p><ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRemoveVariableValueAction(long id, String s) {
    super(id, s);
  }

}
class UmlAcceptCallAction extends UmlBaseAcceptCallAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "accept call activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
    
    String s = trigger ();
    String scpp = cppTrigger();
    String sjava = javaTrigger();
  
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>Trigger :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
      
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
  
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlAcceptCallAction(long id, String s) {
    super(id, s);
  }

}
class UmlReplyAction extends UmlBaseReplyAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "reply activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    String s = replyToCall();
    String scpp = cppReplyToCall();
    String sjava = javaReplyToCall();
    
    if ((s.length() != 0) || (scpp.length() != 0) || (sjava.length() != 0)) {
      fw.write("<p>replyToCall :</p><ul>");
      
      if (s.length() != 0) {
        fw.write("<li>OCL : <pre>\n");
        writeq(s);
        fw.write("</pre></li>");
      }
  
      if (scpp.length() != 0) {
        fw.write("<li>C++ : <pre>\n");
        writeq(scpp);
        fw.write("</pre></li>");
      }
      
      if (sjava.length() != 0) {
        fw.write("<li>Java : <pre>\n");
        writeq(sjava);
        fw.write("</pre></li>");
      }
      
      fw.write("</ul>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReplyAction(long id, String s) {
    super(id, s);
  }

}
class UmlCreateObjectAction extends UmlBaseCreateObjectAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "create object activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (classifier().length() != 0){
      fw.write("<p>Classifier : ");
      writeq(classifier());
      fw.write("</p>");
    }
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlCreateObjectAction(long id, String s) {
    super(id, s);
  }

}
class UmlDestroyObjectAction extends UmlBaseDestroyObjectAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "destroy object activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isDestroyLinks()) {
      if (isDestroyOwnedObjects())
        fw.write("<p>Destroy links, destroy owned objects</p>");
      else
        fw.write("<p>Destroy links</p>");
    }
    else if (isDestroyOwnedObjects())
      fw.write("<p>Destroy owned objects</p>");
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlDestroyObjectAction(long id, String s) {
    super(id, s);
  }

}
class UmlTestIdentityAction extends UmlBaseTestIdentityAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "test identity activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlTestIdentityAction(long id, String s) {
    super(id, s);
  }

}
class UmlRaiseExceptionAction extends UmlBaseRaiseExceptionAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "raise exception activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRaiseExceptionAction(long id, String s) {
    super(id, s);
  }

}
class UmlReduceAction extends UmlBaseReduceAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "reduce activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isOrdered())
      fw.write("<p>Ordered</p>");
  
    if (reducer() != null){
      fw.write("<p>Reducer : ");
      reducer().write();
      fw.write("</p>");
    }
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReduceAction(long id, String s) {
    super(id, s);
  }

}
class UmlStartObjectBehaviorAction extends UmlBaseStartObjectBehaviorAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "start object behavior activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isSynchronous())
      fw.write("<p>Synchronous</p>");
  
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlStartObjectBehaviorAction(long id, String s) {
    super(id, s);
  }

}
class UmlReadSelfAction extends UmlBaseReadSelfAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "read self activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReadSelfAction(long id, String s) {
    super(id, s);
  }

}
class UmlReadExtentAction extends UmlBaseReadExtentAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "read extent activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (classifier().length() != 0) {
      fw.write("<p>Classifier : ");
      fw.write(classifier());
      fw.write("</p>");
    }
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReadExtentAction(long id, String s) {
    super(id, s);
  }

}
class UmlReclassifyObjectAction extends UmlBaseReclassifyObjectAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "reclassify object activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isReplaceAll())
      fw.write("<p>ReplaceAll</p>");
  
    if (oldClassifier().length() != 0) {
      fw.write("<p>Old Classifier : ");
      fw.write(oldClassifier());
      fw.write("</p>");
    }
  
    if (newClassifier().length() != 0) {
      fw.write("<p>New Classifier : ");
      fw.write(newClassifier());
      fw.write("</p>");
    }
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReclassifyObjectAction(long id, String s) {
    super(id, s);
  }

}
class UmlReadIsClassifiedObjectAction extends UmlBaseReadIsClassifiedObjectAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "read is classified object activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isDirect())
      fw.write("<p>Direct</p>");
  
    if (classifier().length() != 0) {
      fw.write("<p>Classifier : ");
      fw.write(classifier());
      fw.write("</p>");
    }
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReadIsClassifiedObjectAction(long id, String s) {
    super(id, s);
  }

}
class UmlStartClassifierBehaviorAction extends UmlBaseStartClassifierBehaviorAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "start classifier behavior activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlStartClassifierBehaviorAction(long id, String s) {
    super(id, s);
  }

}
class UmlReadStructuralFeatureAction extends UmlBaseReadStructuralFeatureAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "read structural feature activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlReadStructuralFeatureAction(long id, String s) {
    super(id, s);
  }

}
class UmlClearStructuralFeatureAction extends UmlBaseClearStructuralFeatureAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "clear structural feature activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlClearStructuralFeatureAction(long id, String s) {
    super(id, s);
  }

}
class UmlAddStructuralFeatureValueAction extends UmlBaseAddStructuralFeatureValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "add structural feature value activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isReplaceAll())
      fw.write("<p>Replace All</p>");
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlAddStructuralFeatureValueAction(long id, String s) {
    super(id, s);
  }

}
class UmlRemoveStructuralFeatureValueAction extends UmlBaseRemoveStructuralFeatureValueAction {
  /**
   * returns a string indicating the king of the element
   */
  public String sKind() {
    return "remove structural feature value activity action";
  }

  /**
   * entry to produce the html code receiving chapter number
   * path, rank in the mother and level in the browser tree
   */
  public void html(String pfix, int rank, int level) throws IOException {
    super.html();
  
    if (isRemoveDuplicates())
      fw.write("<p>Remove Duplicates</p>");
    
    write_children(pfix, rank, level);
  
    unload(false, false);
  }

  /**
   *   the constructor, do not call it yourself !!!!!!!!!!
   */
  public  UmlRemoveStructuralFeatureValueAction(long id, String s) {
    super(id, s);
  }

}
