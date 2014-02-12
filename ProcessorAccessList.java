
import java.io.*;
import java.net.*;
import java.util.*;

public class ProcessorAccessList{
	private ArrayList<Socket> processors;
	
	public ProcessorAccessList()
	{
		processors = new ArrayList<Socket>();
	}
	
	public synchronized Socket get()
	{
		while (processors.size() == 0){
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		
		Socket skt = processors.remove(0);
		
		return skt;
	}
	
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
