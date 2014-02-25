
import java.io.*;
import java.util.concurrent.*;

public class Comrade {

    private static void usage () {
        System.out.println(
            "Usage: Comrade port [host port [host port [...]]]");
        System.exit(1);
    }

    public static void main (String[] argv) {
        if ((argv.length % 2) != 1) usage();
        int port = 0; // Make the compiler shut up.
        
        ExecutorService executor = 
			Executors.newFixedThreadPool(
			    Runtime.getRuntime().availableProcessors());
        
        try {
            port = Integer.parseInt(argv[0]);
        } catch (NumberFormatException e) { usage(); }
        try ( Protocol p = new Protocol(); )
        {
            for (int i = 1; i < argv.length; i += 2) {
                try {
                    p.addServer(argv[i], Integer.parseInt(argv[i + 1]));
                } catch (NumberFormatException e2) { usage(); }
            }
            p.setupServer(port);
            for (;;) {
                Communicator communicator = null;
                try {
                    communicator = p.serveOnce();
                    Worker worker = new Worker(communicator);
                    executor.execute(worker);
                } catch (IOException e) {
                    if (communicator != null)
                    {
                        try { communicator.close(); } 
                        catch (Exception e2) {}
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


