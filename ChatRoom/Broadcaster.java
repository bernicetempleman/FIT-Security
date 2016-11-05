import java.net.*;
import java.util.*;

public class Broadcaster extends Thread{
    /* the max number of connections available */
    public final static int MAXCONS=100;
    /* should we be dead ???? */
    public static boolean die;
    /* a vector to hold writerThreads */
    Vector writers;
    /* a one line buffer */
    byte[] br_buffer;
    /* Object used to notify us when
     * data is ready
     */
    private Object dataReady;

    public Broadcaster(){
    	dataReady=new Object(); // object to sychronize with
        writers=new Vector(MAXCONS); // maxconnections
        die=false; // I should hope so, we're seconds old!
    }

    public void run(){

        WriterThread writer;

        synchronized(dataReady){
        	String data="ready";
            while (die==false){

                try{

                	/* wait to be notified there is a new
                 	* line in the buffer...*/
                	dataReady.wait();

	                /* broadcast the line to all waiting
	                 * writerThreads*/

	                for (Enumeration E = writers.elements() ; E.hasMoreElements() ;) {
	                    writer=(WriterThread)E.nextElement();

	                    /* if writeln returns false (error),
	                     * remove writerThread from vector */
	                     
	                    if (!writer.writeln(br_buffer)){
	                    	System.err.println("removing a WriterThread:"+writer.toString());
	                        writers.removeElement(writer);
	                    }
	                    writer.writeln(br_buffer);
	                }
	                dataReady.notify();
				}catch (InterruptedException e){
					System.err.println("InterruptedException "+e.toString());
				}

        	}
    	}
	}//run

        /* write a line into buffer
         * and notify dataReady */
     public void writeln(byte[] S){
         synchronized(dataReady){
         br_buffer=S;
         System.out.println(S);
         dataReady.notify();

         //dataReady.wait(); //wait until data consumed
         }
      }

      /* add a new Writer Thread to the Vector  */
      public void add(WriterThread T){
      	System.out.println("adding WriterThread:"+T);
      	writers.addElement((Object) T);
      	T.start();
      }
 }





