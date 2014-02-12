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
            
            if (message == "processor")
            {
            	pal.put(socket);
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
            	ArrayList<String> names = new ArrayList<String>();
            	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
            	
            	for (int i = 0; i < numImages; i++)
            	{
            		names.add(ic.recvmsg());
            		ic.sendmsg("Confirm");
            		images.add(ic.recvimg());
            		ic.sendmsg("" + i);
            	}
            	
            	ArrayList<Socket> processors = new ArrayList<Socket>();
            	
            }
            socket.close();
        }catch (ImageReadException e) {} catch (IOException e) {
        e.printStackTrace();
        }
    }
}
