

import java.io.*;
import java.net.*;
import org.apache.commons.imaging.*;
import java.util.*;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import org.apache.commons.imaging.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KnockKnockClient implements Runnable {
	private String hostName;
	private int portNumber;
	private String directory;
	
	public KnockKnockClient(String hostName, int portNumber, String directory)
	{
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.directory = directory;
	}

    public void run(){
	
        File dir = new File(directory);
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(dir.list()));
        
        ArrayList<String> inputFilenames = new ArrayList<String>();
        
        for (int i = 0; i < names.size(); i++)
        {
        	if (names.get(i).endsWith(".png") ||
        	    names.get(i).endsWith(".jpg") ||
        	    names.get(i).endsWith(".JPG") ||
        		names.get(i).endsWith(".PNG"))
        	{
        		inputFilenames.add(names.get(i));
        	}
        }
        
        try (
            Socket kkSocket = new Socket(hostName, portNumber);
        ) {
        	System.out.println("Connection established");
            ImageComm ic = new ImageComm(kkSocket);
            String message;
            int count;
            System.out.println("Sending num files");
            ic.sendmsg("" + inputFilenames.size());
            message = ic.recvmsg();
            
            if (message.equals("Close"))
            {
                System.out.println("Request denied");
                return;
            }
            
            if (!message.equals("Confirm"))
            {
            	//problem with protocol
            	System.out.println("Protocol problem c " + message + " " + inputFilenames.size());
            }
            
            File f;
            BufferedImage im;
            System.out.println("Sending images");
            for (int i = 0; i < inputFilenames.size(); i++)
            {//send images to serverThread
		        f = new File(directory + "/" + inputFilenames.get(i));
		        System.out.println(inputFilenames.get(i));
		        im = Imaging.getBufferedImage(f);
		        
		        ic.sendmsg(inputFilenames.get(i));
		        
		        message = ic.recvmsg();
		        
		        if (!message.equals("Confirm"))
		        {
		        	//problem with protocol
		        	System.out.println("Protocol problem d");
		        }
		        
		        ic.sendimg(im);//send image
		        
		        count = Integer.parseInt(ic.recvmsg());
		        
		        if (count != i)
		        {
		        	//problem with serverThread
		        	System.out.println("Protocol problem e");
		        }
            }
            System.out.println("Images sent");
            message = ic.recvmsg();
            while (!message.equals("Ready"))
            {
            	message = ic.recvmsg();
            }
            
            ic.sendmsg("Confirm");
            System.out.println("Receiving images");
            int j;
            String outFilename;
            for (int i = 0; i < inputFilenames.size(); i++)
            {//receive images from server
            	message = ic.recvmsg();
            	/*for (j = 0; (j<inputFilenames.size()) && 
            		(message!=inputFilenames.get(j)); j++){}*/
            	outFilename = directory + "/" + "processed-" + message;
            	if (!(outFilename.endsWith(".png") ||
            	    outFilename.endsWith(".PNG")))
            	{
            	    outFilename = outFilename + ".png";
            	}
            	f = new File(outFilename);
            	
            	ic.sendmsg("Confirm");
            	
		        im = ic.recvimg();
		        
		        ic.sendmsg("Confirm");
		        
		        //write image
		        Imaging.writeImage(im, f, ImageFormats.PNG, null);
            }
            System.out.println("Done");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            e.printStackTrace();
            System.exit(1);
        } catch (ImageReadException e) {
        	System.err.println("ImageReadException");
        	System.exit(1);
        } catch (ImageWriteException e) {
        	System.err.println("ImageWriteException");
        	System.exit(1);
        }
    }
}
