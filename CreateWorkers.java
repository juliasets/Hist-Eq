// Create as many Workers as there are cores.
// Runs once per machine.



public class CreateWorkers {
    public void main (String[] args) {
    
    if (args.length != 2) {
        System.err.println(
            "Usage: java CreateWorkers <host name> <port number>");
        System.exit(1);
    }

    String hostName = args[0];
    int portNumber = Integer.parseInt(args[1]);
    
	int cores = Runtime.getRuntime().availableProcessors();
	for (int i = 0; i < cores; ++i) {
	    Worker new_worker = new Worker(hostName, portNumber); // put in proper constructor here
	    new_worker.run();
	}
    }
}
