import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;


public class CheckValidity extends Thread{
    Broadcaster bc;
    Socket s;
    byte[] DESede_key, encrypted_key;
    String returnvalue, passwd;
    DataInputStream in=null;
    DataOutputStream out=null;
    OutputStream O;
    boolean die = false;
    
    public CheckValidity(Broadcaster bc, Socket s, byte[] key){
    	this.bc = bc;
    	this.s = s;
    	DESede_key = key ; 
    	
    	try {
			
	     InputStream I=s.getInputStream();
	     in=new DataInputStream (I);
	     O=s.getOutputStream();
	     out=new DataOutputStream (O);
	}catch (IOException e){
	     System.err.println("ERROR:"+e.toString());
	}
    }

    public void run(){
	while(!die) {

    	      try {
 
    	      	
    	      	if (authenticate()) { 
    	      	    returnvalue = "ok";
    	      	    System.out.println("ok");
    	      	    encrypted_key = PBE("e", passwd, DESede_key);
    	      	    System.out.println("encrypted key is: " + encrypted_key +
		             "   length: " + encrypted_key.length);
		    String b = new String(encrypted_key);
		    System.out.println(" in string format: " + b + "\n");
    	      	    
    	      	}
    	      	else returnvalue = "fail";
    	      	
    	      	out.writeUTF(returnvalue);
		out.flush();
		
		
		if (returnvalue.equals("ok")) {

	            
		    // encrypting session key and sending it out

		    out.writeInt(encrypted_key.length);
		    out.write(encrypted_key, 0, encrypted_key.length); 
		    
		    //out.writeInt(DESede_key.length);
		    //out.write(DESede_key, 0, DESede_key.length); 
		    out.flush();
		    
		    System.out.println("sending session key: " + DESede_key +
		             "   length: " + DESede_key.length);
		    String b = new String(DESede_key);
		    System.out.println(" in string format: " + b + "\n");
		    
		    //adding a new Broadcaster thread and a new ReaderThread
		    bc.add(new WriterThread(s));
	            new ReaderThread(s,bc).start();
	            die = true;
		    
	        }
	      } catch (Exception e) { die = true;}
      }
    }//run
    
  public boolean authenticate()  throws IOException, NoSuchAlgorithmException {

    String user = in.readUTF();
    long t1 = in.readLong();
    double q1 = in.readDouble();
    int length = in.readInt();
    byte[] protected1 = new byte[length];
    in.readFully(protected1);

    String password = lookupPassword(user);
    if(password.equals("no such user")) return false;
    byte[] local = makeDigest(user, password, t1, q1, "SHA-1");
    
    if(MessageDigest.isEqual(protected1, local)) {
    	passwd = password;
    	return true;
    }
    else return false;
    
   }//end authenticate

  protected String lookupPassword(String user) { 
  	if(user.equals("lei")) return "shen";
  	else if(user.equals("tian")) return "yu";
  	else if(user.equals("yingli")) return "wang";
  	else return "no such user";
  	 }
  
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
      	System.out.println("encrypting session key..." + 
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

 }
