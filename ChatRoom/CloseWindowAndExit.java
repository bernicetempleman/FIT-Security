
import java.awt.event.* ;

public class CloseWindowAndExit extends WindowAdapter {

    public void windowClosing( WindowEvent e) {
        System.exit(0) ;
     }
     
}