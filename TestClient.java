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

public class TestClient {
    public static void main(String[] args) {
        try {
	        if (args.length != 3) {
	            System.err.println(
	                "Usage: java EchoClient <host name> <port number> <directory>");
	            System.exit(1);
	        }
	
	        String hostName = args[0];
	        int portNumber = Integer.parseInt(args[1]);
	        File dir = new File(args[2]);
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
	            //Create object with socket
	            //skt
	            ImageComm ic = new ImageComm(kkSocket);
	            for (int i = 0; i < inputFilenames.size(); i++)
	            {
			        File f = new File(args[2] + "/" + inputFilenames.get(i));
			        BufferedImage im = Imaging.getBufferedImage(f);

			        ic.sendimg(im);//send image
	
			        /*while ((fromServer = in.readLine()) != null) {
			            System.out.println("Server: " + fromServer);
			            if (fromServer.equals("Bye."))
			                break;
			            
			            fromUser = stdIn.readLine();
			            if (fromUser != null) {
			                System.out.println("Client: " + fromUser);
			                out.println(fromUser);
			            }
			            
			        }*/
	            }
	        } catch (UnknownHostException e) {
	        	e.printStackTrace();
	            System.err.println("Don't know about host " + hostName);
	            System.exit(1);
	        } catch (IOException e) {
	        	e.printStackTrace();
	            System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	            System.exit(1);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
