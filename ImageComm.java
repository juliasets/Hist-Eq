
import java.net.*;
import java.io.*;

import org.apache.commons.imaging.*;

public class ImageComm {

	private PrintWriter out;
	private BufferedReader in;

	public ImageComm (Socket socket) {
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
	}

	public void sendmsg (String msg) {
		out.println("message");
		out.println(msg);
	}

}