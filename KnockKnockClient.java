/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

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
        //ArrayList<String> outputFilenames = new ArrayList<String>();
        
        for (int i = 0; i < names.size(); i++)
        {
        	if (names.get(i).endsWith(".jpg") ||
        		names.get(i).endsWith(".JPG") ||
        		names.get(i).endsWith(".png") ||
        		names.get(i).endsWith(".PNG"))
        	{
        		inputFilenames.add(names.get(i));
        		//outputFilenames.add("processed-" + names.get(i));
        	}
        }
        
        try (
            Socket kkSocket = new Socket(hostName, portNumber);
            //PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            //BufferedReader in = new BufferedReader(
            //    new InputStreamReader(kkSocket.getInputStream()));
        ) {
        	System.out.println("Connection established");
            ImageComm ic = new ImageComm(kkSocket);
            String message;
            int count;
            System.out.println("Sending num files");
            ic.sendmsg("" + inputFilenames.size());
            message = ic.recvmsg();
            
            if (!message.equals("Confirm"))
            {
            	//problem with protocol
            	System.out.println("Protocol problem c");
            }
            
            ArrayList<ImageFormat> imgfs = new ArrayList<ImageFormat>();
            File f;
            BufferedImage im;
            System.out.println("Sending images");
            for (int i = 0; i < inputFilenames.size(); i++)
            {//send images to serverThread
		        f = new File(directory + "/" + inputFilenames.get(i));
		        im = Imaging.getBufferedImage(f);
		        imgfs.add(Imaging.guessFormat(f));
		        
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
            ImageFormat imgf;
            for (int i = 0; i < inputFilenames.size(); i++)
            {//receive images from server
            	message = ic.recvmsg();
            	for (j = 0; (j<inputFilenames.size()) && 
            		(message!=inputFilenames.get(j)); j++){}
            	f = new File(directory + "/" + "processed-" + message);
            	imgf = imgfs.get(j);
            	
            	ic.sendmsg("Confirm");
            	
		        im = ic.recvimg();
		        
		        ic.sendmsg("Confirm");
		        
		        //write image
		        Imaging.writeImage(im, f, imgf, null);
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
