
import java.net.*;
import java.io.*;
import java.awt.*;
import java.applet.*;
import java.awt.image.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;


import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;


public class ChatApplet extends Frame implements Runnable {

  
  protected DataOutputStream Out;
  private   DataInputStream k;
 
  
  protected TextArea output;
  protected TextField input;
  protected TextField loginName, password;
  protected Panel loginPanel;
  protected Label loginLabel, passLabel;
    //protected static Thread listener;
  
  private String username, passwd;
  private byte[] keybyte;
    
  private javax.crypto.SecretKey DESede_key =null;
  private javax.crypto.Cipher encrypter;
  private javax.crypto.Cipher decrypter;
    
  SysListener myListener; 
  String host, port;
  Socket s;
  public ChatApplet (String address, String port) {

   try {
      host = address;
      
      this.port = port;
      if (port == null)
        port = "9830";
      //output.append("Connecting to " + host + ":" + port + "...");
      
      s = new Socket (host, Integer.parseInt (port));


   } catch (Exception e) { System.out.println("can not get connection");
   e.printStackTrace();
   }
      
   
    Provider sunJce = new com.sun.crypto.provider.SunJCE();
    Security.addProvider(sunJce);
   

    setLayout (new BorderLayout ());
    loginName = new TextField();
    loginName.setEditable(true);
    password = new TextField();
    password.setEditable(true);
    password.setEchoChar( '*' );
    loginLabel = new Label("Login Name:");
    passLabel = new Label("Password:");
    loginPanel = new Panel(new GridLayout(5,1));
    loginPanel.add(loginLabel);
    loginPanel.add (loginName);
    loginPanel.add(passLabel);
    loginPanel.add (password);
    add("North",loginPanel);
    output = new TextArea ();
    add ("Center",output);
    output.setEditable (false);
    input = new TextField ();
    add ("South", input);
    input.setEditable (false);
    myListener = new SysListener();
    
    loginName.addActionListener(myListener);
    password.addActionListener(myListener);
    input.addActionListener(myListener);
   

        
  }// end constructor

  //public void start () {
    //  listener = new Thread (this);
    // listener.start ();
 // }
  
 /* public void stop () {
    output.appendText ("Stopped.\n");
    if (listener != null)
      listener.stop ();
    listener = null;
    Out = null;
  } */

  public void run () {
        
    try {
      Thread.sleep (500);
    } catch (InterruptedException ex) {
    }

    try {
      
      Out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
      output.append(" connected.\n");
      //input.show ();
      validate ();
      
      //input.requestFocus ();
      if (login(s)) {
        
         try {
           SecretKeyFactory desedeFactory = 
                 SecretKeyFactory.getInstance("DESede");
           KeySpec spec = new DESedeKeySpec(keybyte);
           DESede_key = desedeFactory.generateSecret(spec);
        
        }catch (NoSuchAlgorithmException e) {
              System.out.println("No such algo exists!1 \n");   
        }catch (InvalidKeySpecException e) {
              System.out.println("Invalid KeySpec !1 \n");
        } catch (InvalidKeyException e) {
              System.out.println("Invalid Key!1\n");
        }
        
        
      input.setEditable (true); //only after receiving the key can you type
      
      try {
        //initiate encrypter since we get the key
         encrypter = javax.crypto.Cipher.getInstance("DESede/ECB/PKCS5Padding");
         encrypter.init(javax.crypto.Cipher.ENCRYPT_MODE, DESede_key);

      } catch (NoSuchAlgorithmException e) {
         System.out.println("No such algo!2 \n");
      } catch (NoSuchPaddingException e) {
         System.out.println("No such Padding!2 \n");
      } catch (InvalidKeyException e) {
         System.out.println("Invalid Key!2\n");
      }
      
      execute (s);
          //this.repaint();
      }// end if login
      else {
        try {
                s.close();
        } catch (Exception e) {}  
                
      }//end else
      
     } catch (IOException ex) {
      ByteArrayOutputStream out = new ByteArrayOutputStream ();
      ex.printStackTrace (new PrintStream (out));
      output.append("\n" + out);
     }
  }//end run

  

  public boolean login (Socket s) {
    try {
        DataInputStream i = new DataInputStream (new 
                BufferedInputStream (s.getInputStream ()));
        
        for (int m=0; m<5; m++) {
             String line = i.readUTF();
             
             if (line.equals("ok")) {
                 username = loginName.getText();
                 passwd   = password.getText();
                 password.setEditable(false);
                 loginName.setEditable(false);
                 password.setText("");
                 loginName.setText("");
                 password.removeActionListener(myListener);
                 loginName.removeActionListener(myListener);
                 output.append("Login Successfully!\n");
                 
                 input.requestFocus ();
                 
                 // receiving key 

                 //keybyte = new byte[i.readInt()];
                 //i.read(keybyte);
                 
                 byte[] encrypted_keybyte = new byte[i.readInt()];
                 i.read(encrypted_keybyte);
                 System.out.println("Receiving encrypted key :" + encrypted_keybyte
                     + "  length: " + encrypted_keybyte.length);
                 String c = new String(encrypted_keybyte);
                 System.out.println("  in string format: " + c + "\n");

                 
                 keybyte = PBE("d", passwd, encrypted_keybyte); 
                 
                 System.out.println("Receiving session key :" + keybyte
                     + "  length: " + keybyte.length);
                 String a = new String(keybyte);
                 System.out.println("  in string format: " + a + "\n");

                 return true;
             }
             else {
                loginName.setText("");
                password.setText("");
                output.setText("Invalid Login!");
                
             } 
       }
       return false;
   } catch (Exception e) { return false;}
 }
 
 
  public void execute (Socket s) {
      
    try {  
      decrypter = javax.crypto.Cipher.getInstance("DESede/ECB/PKCS5Padding");
      decrypter.init(javax.crypto.Cipher.DECRYPT_MODE, DESede_key);
                     
        DataInputStream i = new DataInputStream (
              new BufferedInputStream(s.getInputStream ())); 
                  
          while (true) {
            try{
           
             byte[] line = new byte[i.readInt()];
             i.read(line);
             byte[] decrypt_bytes = decrypter.doFinal(line);
             String input_text = new String(decrypt_bytes);
             //output.append("get one line \n");
             output.append(input_text + "\n");
           
             }catch (javax.crypto.IllegalBlockSizeException e) {
                System.out.println("IllegalBlockSizeException! Discard one line.\n");
             }catch (javax.crypto.BadPaddingException e) {
                System.out.println("BadPaddingException! Discard one line.\n");
             }
                
            
          }
         
    } catch (NoSuchAlgorithmException e) {
         System.out.println("No such algo!3 \n");
    } catch (NoSuchPaddingException e) {
         System.out.println("No such Padding!3 \n");
    } catch (InvalidKeyException e) {
         System.out.println("Invalid Key!3\n"); 
    } catch (IOException ex) {
          ByteArrayOutputStream out = new ByteArrayOutputStream ();
          ex.printStackTrace (new PrintStream (out));
          output.append(out.toString ());
    } finally {
           try {
               s.close ();
           } catch (IOException ex) {
                 ex.printStackTrace ();
           }
           }//end finally
  } // end execute

  


  public static byte[] makeDigest(String user, String password,
      long t1, double q1, String algo) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(algo);
    md.update(user.getBytes());
    md.update(password.getBytes());
    md.update(makeBytes(t1, q1));
    return md.digest();
  }
  
  public static byte[] makeBytes(long t, double q) {
    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);
      dataOut.writeLong(t);
      dataOut.writeDouble(q);
      return byteOut.toByteArray();
    }
    catch (IOException e) {
      return new byte[0];
    }
  }

   public static byte[] PBE(String options, String passphrase, byte[] input) 
       throws Exception {

        String d = new String(input);
        System.out.println("decrypting session key..." + 
            options + " " + passphrase + " " + d + "\n");               
        
        String algo="DES/ECB/PKCS5Padding";
        byte[] key = new byte[8];
        javax.crypto.SecretKey DES_key =null;
         
        boolean encrypting = (options.indexOf("e") != -1);
        
        

         //create a DES key from the passphrase
           MessageDigest md = MessageDigest.getInstance("MD5");
           md.update(passphrase.getBytes());
           byte[] digest = md.digest();
           System.arraycopy(digest, 0, key, 0, 8);
           SecretKeyFactory desFactory = 
              SecretKeyFactory.getInstance("DES");
           KeySpec spec = new DESKeySpec(key);
           DES_key = desFactory.generateSecret(spec);            

        
        //Encrypt or decrypt the input
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(algo);
        int mode = encrypting ? javax.crypto.Cipher.ENCRYPT_MODE : 
                                javax.crypto.Cipher.DECRYPT_MODE;
        cipher.init(mode, DES_key);
        byte[] output = cipher.doFinal(input);
        
                
        //return result
        System.out.println("Finished!\n");
        return output;
        
        
   }// end PBE
    
   
        
  class SysListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
             //output.append(e.getSource().toString());
             if ((e.getSource() == input) && (Out != null)) {
                
                try {   
                        String input_text =username + ":  " + (String) input.getText();
                        byte[] input_encrypted = encrypter.doFinal(input_text.getBytes());
                        Out.writeInt(input_encrypted.length);
                        Out.write(input_encrypted, 0, input_encrypted.length);
                        Out.flush ();
                        input.setText ("");
                }catch (javax.crypto.IllegalBlockSizeException ex) {
                       System.out.println
                           ("IllegalBlockSizeException! Line not encrypted.\n");
                }catch (javax.crypto.BadPaddingException ex) {
                       System.out.println
                           ("BadPaddingException! Line not encrypted.\n");
                }catch (IOException ex) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream ();
                        ex.printStackTrace (new PrintStream (out));
                        output.appendText (out.toString ());
                        /*if (listener != null)
			  listener.stop ();*/
                  }
             }
             else if ((e.getSource() == loginName) && (Out != null)) {
                 
             }
             else if ((e.getSource() == password) && (Out != null)) {
                try {
                   String user = (String) loginName.getText();
                   String pwd = (String) password.getText();
                  //create time stamp and random number
                  long t1 = (new Date()).getTime();
                  double q1 = Math.random();
                  //make digest of login info
                  byte[] protected1 = makeDigest(user, pwd, t1, q1, "SHA-1");
                  
                  //send out login infomation
                  Out.writeUTF(user);
                  Out.writeLong(t1);
                  Out.writeDouble(q1);
                  Out.writeInt(protected1.length);
                  Out.write(protected1, 0, protected1.length);
                  Out.flush();
                        
                  System.out.println("the password you typed:" + password);
                  input.setText ("");
                  
                } catch (IOException ex) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream ();
                        ex.printStackTrace (new PrintStream (out));
                        output.appendText (out.toString ());
			/* if (listener != null)
			   listener.stop ();*/
                } catch (NoSuchAlgorithmException ex) {
                        System.out.println("No such Message Digest Algorithm!\n");
                }
             }//end last else if
        }// end action performed
        
                
  } //end syslistener
  
  
//allow this applet to run like an application
    public static void main( String args[]) {
	
	ChatApplet app = new ChatApplet( args[0], args[1] );
	System.out.println(args[0]);
	System.out.println(args[1]);
	app.setSize( 300, 300);
     
	//app.addWindowListener(new CloseWindowAndExit() );
	Thread ab = new Thread(app);
	ab.start();
     
     
	app.setVisible( true );
     

     
     } 
     
   
}

























