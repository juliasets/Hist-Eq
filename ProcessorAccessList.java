
import java.io.*;
import java.net.*;
import java.util.*;

public class ProcessorAccessList{
	private ArrayList<Socket> processors;
	
	public ProcessorAccessList()
	{
		processors = new ArrayList<Socket>();
	}
	
	public synchronized Socket get(int maxWait)
	{
		while (processors.size() == 0){
			try {
			    if (maxWait == -1)
			    {
			        wait();
			    }
			    else
			    {
				    wait(maxWait);
				    if (processors.size() == 0)
				    {
				        return null;
				    }
				}
			} catch (InterruptedException e) {}
		}
		
		Socket skt = processors.remove(0);
		
		return skt;
	}
	
	/*public synchronized Socket get()
	{
	    return get(-1);
	}*/
	
	public synchronized void put(Socket skt)
	{
		if (processors.size() > 10000)
		{
			//tell processor to wait
			return;
		}
		processors.add(skt);
		
		notifyAll();
	}
}
