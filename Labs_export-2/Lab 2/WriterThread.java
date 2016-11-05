import java.io.*;
import java.net.*;
public class WriterThread extends Thread{
	Socket socket=null;
	byte[] buffer;
	DataOutputStream out=null;
	boolean status=true;
	OutputStream O;
	
	public WriterThread(Socket S){
		this.socket=S;
		try {
			O=S.getOutputStream();
			out=new DataOutputStream (O);
		}catch (IOException e){
		System.err.println("WriterThread ERROR: " + 
	                this.toString()+": "+e.toString());
		}

	}
	public void run(){
		synchronized(this){while(true){
			try{
				wait();
				out.writeInt(buffer.length);
				out.write(buffer, 0, buffer.length);
				out.flush();
			}catch (Exception e){
				 /* there's been an error! */
				status=false;
			}
			

	 	}}
	}

	public synchronized boolean  writeln(byte[] S){
		buffer=S;
		notify();
		return status;
	}
}