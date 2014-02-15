import java.util.concurrent.*;
import java.util.*;

class MassClient{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
            System.err.println(
                "Usage: java MassClient <host name> <port number>");
            System.exit(1);
		}
		
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		
		String[] directories = new String[3];
		directories[0] = "Photos1";
		directories[1] = "Photos2";
		directories[2] = "Photos3";
		
		ExecutorService executor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for (int i = 0; i < directories.length; i++)
		{
			Runnable client = new 
				KnockKnockClient(hostName, portNumber, directories[i]);
			executor.execute(client);
		}
		
		executor.shutdown();
        while (!executor.isTerminated()) {}
        
        System.out.println("Finished all threads");
	}
}
