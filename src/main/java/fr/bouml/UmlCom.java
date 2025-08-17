package fr.bouml;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *  This class manages the communications
 * 
 *  This class may be defined as a 'singleton', but I prefer to use static 
 *  members allowing to just write 'UmlCom::member' rather than
 *  'UmlCom::instance()->member' or other long sentence like this.
 * 
 *  The operation you can use yourself are :
 * 
 *  - connect()
 * 
 *  - targetItem()
 * 
 *  - trace()
 * 
 *  - showTrace()
 * 
 *  - traceAutoRaise()
 * 
 *  - message()
 * 
 *  - bye()
 * 
 *  - close()
 *  
 *  you must NOT call the others
 */

 @SuppressWarnings("all")
public class UmlCom {
  /**
   *  does the connexion
   *  
   *  On error return FALSE in C++, produce a RuntimeException in Java
   */
  public static void connect(int port) throws RuntimeException
  {
    try {
      sock = new Socket("127.0.0.1", port);
      is = new DataInputStream(sock.getInputStream());
      os = new DataOutputStream(sock.getOutputStream());
    
      buffer_in_size = 1024;
      buffer_in = new byte[buffer_in_size];
      p_buffer_in = 0;
    
      buffer_out_size = 1024;
      buffer_out = new byte[buffer_out_size];
      p_buffer_out = 4/*bytes for length*/;
  
      // send API version
      write_unsigned(71);
      flush();
    }
    catch (Exception e) {
    throw new RuntimeException("cannot establish connexion");
    }
  }

  /**
   *   returns the item on which the tool is applied
   */
  public static UmlItem targetItem()
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._targetCmd);
    return UmlBaseItem.read_();
  }

  /**
   *  to write messages in the tool window,
   *  use Qt rich text : allows HTML like formatting
   */
  public static void trace(String s)
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._traceCmd, s);
  }

  /**
   * to show the trace window
   */
  public static void showTrace()
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._showTraceCmd);
  }

  /**
   * to automatically raise the trace window
   * each time trace() is called
   */
  public static void traceAutoRaise(boolean y)
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._traceAutoRaiseCmd, (y) ? (byte) 1 : (byte) 0);
  }

  /**
   *  to write a message in the status bar line
   *  does not use Qt rich text
   */
  public static void message(String s)
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._messageCmd, s);
  }

  /**
   *  must be called just before the disconnexion
   */
  public static void bye(int v)
  {
    send_cmd(CmdFamily.miscGlobalCmd, MiscGlobalCmd._byeCmd, v, "");
  }

  /**
   *  disconnexion
   */
  public static void close()
  {
    try {
      os.close();
      is.close();
      sock.close();
      os = null;
      is = null;
      sock = null;
    }
    catch(Exception e) {
    }
  
  }

  private static Socket sock;

  private static DataInputStream is;

  private static DataOutputStream os;

  private static byte[] buffer_in;

  private static int buffer_in_size;

  private static int p_buffer_in;

  private static int buffer_in_end;

  private static byte[] buffer_out;

  private static int p_buffer_out;

  private static int buffer_out_size;

  private static byte id_size;

  private static String empty_string;

  static { empty_string = new String(); }  protected static void check_size_out(int len)
  {
    if ((p_buffer_out + len) >= buffer_out_size) {
      buffer_out_size = p_buffer_out + len + 1024;
      
      byte[] newbuff = new byte[buffer_out_size];
      
      int index;
  
      for (index = 0; index != p_buffer_out; index += 1)
        newbuff[index] = buffer_out[index];
      
      buffer_out = newbuff;
    }
  }

  protected static void read_if_needed()
  {
    if (p_buffer_in == buffer_in_end) {
      read_buffer(4);
      //System.out.print("plug-out read ");System.out.println(((((int) buffer_in[0]) & 255) << 24) + ((((int) buffer_in[1]) & 255) <<16) + ((((int) buffer_in[2]) & 255) << 8) + (((int) buffer_in[3]) & 255));
      read_buffer(((((int) buffer_in[0]) & 255) << 24) +
  		((((int) buffer_in[1]) & 255) << 16) +
  		((((int) buffer_in[2]) & 255) << 8) +
  		(((int) buffer_in[3]) & 255));
    }
  }

  protected static void read_buffer(int len)
  {
    //cout << "enter UmlCom.read_buffer(" << len << ")\n";
    
    if (buffer_in_size < len) {
      buffer_in_size = len + 256;
      buffer_in = new byte[buffer_in_size];
    }
    
    int remainder = len;
    int offset = 0;
    int nread;
    
    for (;;) {
      try {
        if ((nread = is.read(buffer_in, offset, remainder)) == -1)
    	nread = 0;
      }
      catch (Exception e) {
        throw new RuntimeException("communication closed");
      }
      
      //cout << "UmlCom a lu " << nread << '\n';
      if ((remainder -= nread) == 0)
        break;
      offset += nread;
      //...wait some time
    }
    //cout << "exit UmlCom.read_buffer()\n";
    
    p_buffer_in = 0;
    buffer_in_end = len;
  }

  protected static void write_bool(boolean b)
  {
    check_size_out(1);
  
    buffer_out[p_buffer_out++] = (b) ? (byte) 1 : (byte) 0;
  }

  protected static void write_char(byte c)
  {
    check_size_out(1);
    
    buffer_out[p_buffer_out++] = c;
  }

  protected static void write_unsigned(int u)
  {
    check_size_out(4);
    
    buffer_out[p_buffer_out] = (byte) (u >> 24);
    buffer_out[p_buffer_out+1] = (byte) (u >> 16);
    buffer_out[p_buffer_out+2] = (byte) (u >> 8);
    buffer_out[p_buffer_out+3] = (byte) u;
    
    p_buffer_out += 4;
  }

  protected static void write_id(long id)
  {
    check_size_out((int) id_size);
    
    // id_size is supposed to be 8 or 4 !
    if (id_size == (byte) 8) {
      buffer_out[p_buffer_out] = (byte) (id >> 56);
      buffer_out[p_buffer_out+1] = (byte) (id >> 48);
      buffer_out[p_buffer_out+2] = (byte) (id >> 40);
      buffer_out[p_buffer_out+3] = (byte) (id >> 32);
  
      p_buffer_out += 4;
    }
    buffer_out[p_buffer_out] = (byte) (id >> 24);
    buffer_out[p_buffer_out+1] = (byte) (id >> 16);
    buffer_out[p_buffer_out+2] = (byte) (id >> 8);
    buffer_out[p_buffer_out+3] = (byte) id;
  
    p_buffer_out += 4;
  }

  protected static void write_string(String p)
  {
    if (p == null) {
      check_size_out(1);
      buffer_out[p_buffer_out++] = 0;
    }
    else {
      byte[] pb = p.getBytes();
      int index;
    
      check_size_out(pb.length + 1);
      for (index = 0; index != pb.length; index += 1)
        buffer_out[p_buffer_out + index] = pb[index];
      buffer_out[p_buffer_out + index] = 0;
      p_buffer_out += index + 1;
    }
  
    
  }

  /**
   *  do NOT call the followings yourself !!!!
   */
  public static void send_cmd(CmdFamily f, int cmd)
  {
    //System.out.print("UmlCom.send_cmd((CmdFamily) " ); System.out.print(f.value()); System.out.print(", "); System.out.print(cmd); System.out.println(")");
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    flush();
  }

  /**
   * internal, do NOT use it
   */
  public static void send_cmd(CmdFamily f, int cmd, String s, boolean b)
  {
    //cout << "UmlCom::send_cmd((CmdFamily) " << f << ", " << cmd << ", " << ((s) ? s : "") << b << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_string(s);
    write_bool(b);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, byte arg)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << arg << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_char(arg);
    flush();
  }

  /**
   * internal, do NOT use it
   */
  public static void send_cmd(CmdFamily f, int cmd, int arg, String dummy)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << arg << ", dummy)\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_unsigned(arg);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, long id)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << id << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_id(id);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, String s)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << s << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_string(s);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, long id, String n)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << id << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_id(id);
    write_string(n);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, String s, String v)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << s << ", " << v << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_string(s);
    write_string(v);
    flush();
  }

  public static void send_cmd(CmdFamily f, int cmd, boolean b, String s, String v)
  {
    //cout << "UmlCom.send_cmd((CmdFamily) " << f << ", " << cmd << ", " << b << ", " << s << ", " << v << ")\n";
    
    write_char((byte) f.value());
    write_char((byte) cmd);
    write_bool(b);
    write_string(s);
    write_string(v);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd)
  {
    //System.out.print("UmlCom.send_cmd(id, "); System.out.print(cmd.value()); System.out.println(")");
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, String arg)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_string(arg);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, byte arg)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << ((int) arg) << '\n';
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_char(arg);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, int arg)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg << '\n';
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_unsigned(arg);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, UmlTypeSpec arg)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", UmlTypeSpec)\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    if (arg.type != null) {
      write_id(arg.type.identifier_());
      write_string(null);
    }
    else {
      write_id(0);
      write_string(arg.explicit_type);
    }
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, String arg1, String arg2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg1 << ", " << arg2 << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_string(arg1);
    write_string(arg2);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, anItemKind arg1, String arg2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg1 << ", " << arg2 << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_char((byte) arg1.value());
    write_string(arg2);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, anItemKind arg1, aRelationKind arg2, long id2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg1 << ", " << arg2 << ", " << id2 << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_char((byte) arg1.value());
    write_char((byte) arg2.value());
    write_id(id2);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, long id1)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", id1)\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_id(id1);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, long id1, String arg2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", id1, " << arg2 << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_id(id1);
    write_string(arg2);
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, int arg1, UmlTypeSpec arg2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg1 << ", UmlTypeSpec)\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_unsigned(arg1);
    if (arg2.type != null) {
      write_id(arg2.type.identifier_());
      write_string(null);
    }
    else {
      write_id(0);
      write_string(arg2.explicit_type);
    }
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, int arg1, String arg2, String arg3, UmlTypeSpec arg4, UmlTypeSpec arg5)
  {
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_unsigned(arg1);
    write_string(arg2);
    write_string(arg3);
    if (arg4.type != null) {
      write_id(arg4.type.identifier_());
      write_string(null);
    }
    else {
      write_id(0);
      write_string(arg4.explicit_type);
    }
    if (arg5.type != null) {
      write_id(arg5.type.identifier_());
      write_string(null);
    }
    else {
      write_id(0);
      write_string(arg5.explicit_type);
    }
    flush();
  }
  public static void send_cmd(long id, OnInstanceCmd cmd, int arg1, byte arg2, String arg3, String arg4, UmlTypeSpec arg5)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg1 << ", " << arg2 << ", " << arg3 << ", " << arg4 << ", UmlTypeSpec)\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_unsigned(arg1);
    write_char(arg2);
    write_string(arg3);
    write_string(arg4);
    if (arg5.type != null) {
      write_id(arg5.type.identifier_());
      write_string(null);
    }
    else {
      write_id(0);
      write_string(arg5.explicit_type);
    }
    flush();
  }

  public static void send_cmd(long id, OnInstanceCmd cmd, UmlItem[] l)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", const Q3PtrVector<UmlItem> & l)\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    
    int n = l.length;
    
    write_unsigned(n);
    
    for (int i = 0; i != n; i += 1)
      write_id(l[i].identifier_());
    
    flush();
  }

  /**
   * internal, do NOT use it
   */
  public static void send_cmd(long id, OnInstanceCmd cmd, anItemKind arg, long id2)
  {
    //cout << "UmlCom.send_cmd(id, " << cmd << ", " << arg << ", " << id2 << ")\n";
    
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    write_char((byte) arg.value());
    write_id(id2);
    flush();
  }

  /**
   * internal, do NOT use it
   */
  public static void send_cmd(long id, OnInstanceCmd cmd, UmlClass[] l1, UmlClass[] l2, UmlClass[] l3)
  {
    write_char((byte) CmdFamily._onInstanceCmd);
    write_id(id);
    write_char((byte) cmd.value());
    
    int n;
    
    n = l1.length;
    write_unsigned(n);
    
    for (int i = 0; i != n; i += 1)
      write_id(l1[i].identifier_());
    
    n = l2.length;
    write_unsigned(n);
    
    for (int i = 0; i != n; i += 1)
      write_id(l2[i].identifier_());
    
    n = l3.length;
    write_unsigned(n);
    
    for (int i = 0; i != n; i += 1)
      write_id(l3[i].identifier_());
    
    flush();
  }
  public static long read_id()
  {
    read_if_needed();
  
    id_size = buffer_in[p_buffer_in++];
  
    long id;
  
    // id_size is supposed to be 8 or 4 !
    if (id_size == (byte) 8) {
      id = ((((long) buffer_in[p_buffer_in]) & ((long) 255)) << 56)
         + ((((long) buffer_in[p_buffer_in+1]) & ((long) 255)) << 48)
         + ((((long) buffer_in[p_buffer_in+2]) & ((long) 255)) << 40)
         + ((((long) buffer_in[p_buffer_in+3]) & ((long) 255)) << 32);
  
      p_buffer_in += 4;
    }
    else
      id = 0;
  
    id += ((((long) buffer_in[p_buffer_in]) & ((long) 255)) << 24)
       + ((((long) buffer_in[p_buffer_in+1]) & ((long) 255)) << 16)
       + ((((long) buffer_in[p_buffer_in+2]) & ((long) 255)) << 8)
       + (((long) buffer_in[p_buffer_in+3]) & ((long) 255));
  
    p_buffer_in += 4;
  
    return id;
  }

  public static String read_string()
  {
    read_if_needed();
    
    //System.out.print("read_string offset ");System.out.println(p_buffer_in);  
    int start = p_buffer_in;
    
    while (buffer_in[p_buffer_in++] != 0)
      ;
  
    if (p_buffer_in == (start + 1))
      return empty_string;
      
    return new String(buffer_in, start, p_buffer_in - start - 1);
  
  }

  public static boolean read_bool()
  {
    read_if_needed();
    //System.out.print("read_bool offset ");System.out.println(p_buffer_in);  
    
    return buffer_in[p_buffer_in++] != 0;
  }

  public static int read_char()
  {
    read_if_needed();
    //System.out.print("read_char offset ");System.out.println(p_buffer_in);  
    
    return ((int) buffer_in[p_buffer_in++]) & 255;
  }

  public static int read_unsigned()
  {
    read_if_needed();
    //System.out.print("read_unsigned offset ");System.out.println(p_buffer_in);  
    
    p_buffer_in += 4;
    
    return ((((int) buffer_in[p_buffer_in-4]) & 255) << 24)
       + ((((int) buffer_in[p_buffer_in-3]) & 255) << 16)
       + ((((int) buffer_in[p_buffer_in-2]) & 255) << 8)
       + (((int) buffer_in[p_buffer_in-1]) & 255);
  }

  /**
   *  reads the selected items when the tool is called
   *  you MUST call it only one time after the connexion
   */
  public static UmlItem[] read_item_list()
  {
    int n = read_unsigned();
    UmlItem[] v = new UmlItem[n];
    
    //System.out.print("UmlCom.read_item_list ");System.out.print(n);System.out.println(" items");
    
    for (int index = 0; index != n; index += 1)
      v[index] = UmlBaseItem.read_();
  
    return v;
  }

  public static void fatal_error(String msg)
  {
    System.out.println(msg);
  
    throw new RuntimeException(msg);
  }

  public static void flush()
  {
    if (sock != null) {
      int len = p_buffer_out - 4;
      /* the four first bytes of buffer_out are free to contains the length */
      buffer_out[0] = (byte) (len >> 24);
      buffer_out[1] = (byte) (len >> 16);
      buffer_out[2] = (byte) (len >> 8);
      buffer_out[3] = (byte) len;
  
      try {
        //System.out.print("plug-out send "); System.out.println(p_buffer_out);
        os.write(buffer_out, 0, p_buffer_out);
        os.flush();
        p_buffer_out = 4/*bytes for length*/;
      }
      catch (IOException e) {
        close();	// to not try to send "bye" !
        fatal_error("send error");
      }      
    }
  }

  public static void check()
  {
    if (! read_bool())
      throw new RuntimeException("Cannot be done");
  }

};
