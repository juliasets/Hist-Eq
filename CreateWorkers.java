// Create as many Workers as there are cores.
// Runs once per machine.



public class CreateWorkers {
    public void main () {
	int cores = Runtime.getRuntime().availableProcessors();
	for (int i = 0; i < cores; ++i) {
	    Worker new_worker = new Worker; // put in proper constructor here
	    new_Worker.start();
	}
    }
}
