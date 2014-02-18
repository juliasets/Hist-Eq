import java.util.concurrent.*;
import java.util.*;
import java.io.*;

class MassClient{
	public static void main(String[] args)
	{
		if (args.length != 3)
		{
            System.err.println(
                "Usage: java MassClient <host name> <port number> <directory>");
            System.exit(1);
		}
		
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		String directory = args[2];
		
		File dir = new File(directory);
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(dir.list()));
        
        ArrayList<String> directories = new ArrayList<String>();
        
        for (int i = 0; i < names.size(); i++)
        {
        	if (names.get(i).startsWith("Photos"))
        	{
        		directories.add(names.get(i));
        	}
        }
		
		ExecutorService executor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for (int i = 0; i < directories.size(); i++)
		{
			Runnable client = new 
				KnockKnockClient(hostName, portNumber, 
				    directory + "/" + directories.get(i));
			executor.execute(client);
		}
		
		executor.shutdown();
        while (!executor.isTerminated()) {}
        
        System.out.println("Finished all threads");
	}
}
