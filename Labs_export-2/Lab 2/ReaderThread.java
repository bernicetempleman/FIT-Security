import java.io.*;
import java.net.*;
public class ReaderThread extends Thread{
	//Socket socket=null;
	byte[] buffer;
	DataInputStream in=null;
	Broadcaster broadcaster=null;

	public ReaderThread(Socket S, Broadcaster B){
		try {
		   System.out.println("Reader receiving Socket:" + S.toString());
			InputStream I=S.getInputStream();
			in=new DataInputStream (I);
		}catch (IOException e){
			System.err.println("ERROR:"+e.toString());
		}
		broadcaster=B;

	}
	public void run(){

		while(true){
			try{
				System.out.println("ReaderThread listening");
				buffer=new byte[in.readInt()];
				in.read(buffer);
				System.out.println("abc"+buffer);
				broadcaster.writeln(buffer);


			}catch (IOException e){
				System.err.println("ReaderThread: " + 
				     this.toString()+": "+e.toString()+" dying!");
				throw new ThreadDeath();
			}
	 	}
	}

}