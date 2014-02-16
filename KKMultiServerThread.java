import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import org.apache.commons.imaging.*;


public class KKMultiServerThread extends Thread {
    private Socket socket = null;
    private ProcessorAccessList pal;

    public KKMultiServerThread(Socket socket, ProcessorAccessList pal) {
        super("KKMultiServerThread");
        this.socket = socket;
        this.pal = pal;
    }
    
    public void run() {

        try{
            String message;
            BufferedImage im;
            
            ImageComm ic = new ImageComm(socket);
            
            message = ic.recvmsg();
            
            if (message.equals("processor"))
            {
            	pal.put(socket);
            	System.out.println("Processor connected");
            }
            else
            {
            	int numImages = Integer.parseInt(message);
            	if (numImages <= 0)
            	{
            		ic.sendmsg("Close");
            		socket.close();
            		return;
            	}
            	ic.sendmsg("Confirm");
            	System.out.println("Received num images");
            	ArrayList<String> names = new ArrayList<String>();
            	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
            	
            	System.out.println("Receiving images");
            	for (int i = 0; i < numImages; i++)
            	{//receiving images from client
            		names.add(ic.recvmsg());
            		ic.sendmsg("Confirm");
            		images.add(ic.recvimg());
            		ic.sendmsg("" + i);
            	}
            	System.out.println("Received images");
            	ArrayList<Socket> processors = new ArrayList<Socket>();
            	Socket skt;
            	ImageComm icTmp;
            	
            	System.out.println("Sending to worker");
            	for (int i = 0; i < numImages; i++)
            	{//sending images to workers
            		skt = pal.get();
            		while (skt.isClosed())
            		{
            			skt = pal.get();
            		}
            		icTmp = new ImageComm(skt);
            		icTmp.sendmsg("Job");
            		message = icTmp.recvmsg();
            		while (!message.equals("Ready"))
            		{
            			icTmp.sendmsg("Close");
            			skt.close();
            			skt = pal.get();
            			while (skt.isClosed())
		        		{
		        			skt = pal.get();
		        		}
		        		icTmp = new ImageComm(skt);
		        		icTmp.sendmsg("Job");
		        		message = icTmp.recvmsg();
            		}
            		icTmp.sendimg(images.remove(0));
            		processors.add(skt);
            	}
            	System.out.println("Sent to worker");
            	
            	while (images.size() > 0)
            	{
            		//Somehow missed one. Shouldn't happen, but to prevent more
            		//obscure errors
            		images.remove(0);
            	}
            	
            	System.out.println("Receiving from workers");
            	for (int i = 0; i < numImages; i++)
            	{//getting images from workers
            		skt = processors.get(0);
            		icTmp = new ImageComm(skt);
            		message = icTmp.recvmsg();
            		if (!message.equals("Ready"))
            		{
            			//problem following protocol
            			System.out.println("Protocol problem a");
            		}
            		icTmp.sendmsg("Confirm");
            		images.add(icTmp.recvimg());
            		icTmp.sendmsg("Close");
            		skt.close();
            	}
            	System.out.println("Received from workers");
            	
            	ic.sendmsg("Ready");
            	message = ic.recvmsg();
            	if (!message.equals("Confirm"))
            	{
            		//problem following protocol
            		System.out.println("Protocol problem b");
            	}
            	System.out.println("Sending to Client");
            	for (int i = 0; i < numImages; i++)
            	{
            		ic.sendmsg(names.get(i));
            		message = ic.recvmsg();
            		while (!message.equals("Confirm"))
            		{
            			ic.sendmsg(names.get(i));
            			message = ic.recvmsg();
            		}
            		ic.sendimg(images.get(i));
            		message = ic.recvmsg();
            		while (!message.equals("Confirm"))
            		{
            			ic.sendimg(images.get(i));
            			message = ic.recvmsg();
            		}
            	}
            	System.out.println("Sent to client");
		        ic.sendmsg("Close");
		        socket.close();
            }
        }catch (ImageReadException e) {} catch (ImageWriteException e) {} 
        catch (IOException e) {
        e.printStackTrace();
        }
    }
}
