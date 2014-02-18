// Create as many Workers as there are cores.
// Runs once per machine.

import java.util.concurrent.*;

public class CreateWorkers {
    public static void main (String[] args) {
    
    if (args.length != 2) {
        System.err.println(
            "Usage: java CreateWorkers <host name> <port number>");
        System.exit(1);
    }

    String hostName = args[0];
    int portNumber = Integer.parseInt(args[1]);
    
	int cores = Runtime.getRuntime().availableProcessors();
	
	ExecutorService executor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	for (int i = 0; i < cores; ++i) {
	    Worker new_worker = new Worker(hostName, portNumber); // put in proper constructor here
	    System.out.println("i " + i + " cores " + cores);
	    executor.execute(new_worker);
	}
    }
}
