

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;


public class ChatServer{


	public static void main(String Args[]) {
	
	  Provider sunJce = new com.sun.crypto.provider.SunJCE();
          Security.addProvider(sunJce);
          
          
		int port;
		byte[] DES_key = null;
		//JLabel a = new JLabel();
	        try {
		   javax.crypto.KeyGenerator kg = 
		     javax.crypto.KeyGenerator.getInstance("DESede");
		   kg.init(new SecureRandom());
		   javax.crypto.SecretKey Key = kg.generateKey();
		   
		   SecretKeyFactory desedeFactory = 
		          SecretKeyFactory.getInstance("DESede");
		   DESedeKeySpec spec = (DESedeKeySpec)desedeFactory.
		             getKeySpec(Key, DESedeKeySpec.class);
		   byte[] keybyte = spec.getKey();
		   DES_key = keybyte;
		   String k = new String(DES_key);
		   System.out.println("The key length is:  " + keybyte.length + " bytes.\n");
                   System.out.println("The key is: " + k + "\n");
              	
             	}catch (NoSuchAlgorithmException e) {
                   System.out.println("No such algo exists! \n");
  		
  		}catch (InvalidKeySpecException e) {
  	           System.out.println("Invalid KeySpec ! \n");
		}
		
		try {
			port=Integer.parseInt(Args[0]);
		}catch (Exception e){
			System.out.println("No port specified. Using default(1984).");
			port=1984;
		}


		
		Broadcaster broadcaster=new Broadcaster();
		broadcaster.start();
		
		

		ServerSocket me=null;
		Socket them=null;

			try{

				me=new ServerSocket(port);


			}catch (IOException e){

					System.err.println("Server Socket Error!");
			}
		while (true){
			System.out.println("Listening:"+me.toString());
			try{
				them= me.accept();
				System.out.println(them+" accepted.");
			}catch (IOException e){}
			
			new CheckValidity(broadcaster, them, DES_key).start();
			
			//broadcaster.add(new WriterThread(them));
			//new ReaderThread(them,broadcaster).start();

		}
	}
}

