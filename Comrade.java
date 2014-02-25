
import java.io.*;
import java.util.concurrent.*;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Comrade {

    private static Log LOG = LogFactory.getLog(Comrade.class);

    private void log (String msg) {
        SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz");
        LOG.info(
                 fmt.format(new Date()).toString() + ": " + msg);
    }

    private static void usage () {
        System.out.println(
                           "Usage: Comrade port [host port [host port [...]]]");
        System.exit(1);
    }

    public static void main (String[] argv) {
        if ((argv.length % 2) != 1) usage();
        int port = 0; // Make the compiler shut up.
        
        Date date = new Date();
        log("Comrade started.");

        ExecutorService executor = 
            Executors.newFixedThreadPool(
                                         Runtime.getRuntime().availableProcessors());
        
        try {
            port = Integer.parseInt(argv[0]);
        } catch (NumberFormatException e) { 
            log("Input Error: port number not an integer.");
            usage();
        }
        try ( Protocol p = new Protocol(); )
                {
                    for (int i = 1; i < argv.length; i += 2) {
                        try {
                            p.addServer(argv[i], Integer.parseInt(argv[i + 1]));
                            log("added server at port " argv[i+1]);
                        } catch (NumberFormatException e2) { 
                            log("Input Error: port number not an integer.");
                            usage();
                        }
                    }
                    p.setupServer(port);
                    for (;;) {
                        Communicator communicator = null;
                        try {
                            communicator = p.serveOnce();
                            Worker worker = new Worker(communicator);
                            log("Worker created.");
                            executor.execute(worker);
                        } catch (IOException e) {
                            if (communicator != null)
                                {
                                    log("Opening ImageCommunicator.");
                                    try { 
                                        communicator.close();
                                    } 
                                    catch (Exception e2) {log("Closing ImageCommunicator.");}
                                }
                        }
                    }
                } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


