
import java.net.*;
import java.io.*;

import javax.xml.bind.DatatypeConverter;

import java.awt.image.*;
import org.apache.commons.imaging.*;

public class ImageComm {

	private PrintWriter out;
	private BufferedReader in;
	private String temp;

	public ImageComm (Socket socket) throws IOException {
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
	}

	public void sendmsg (String msg) {
		out.println(msg);
	}

	public String recvmsg () throws IOException {
		return in.readLine();
	}

	public void sendimg (BufferedImage image) throws IOException, ImageWriteException {
		out.println(
				DatatypeConverter.printBase64Binary(
						Imaging.writeImageToBytes(image, ImageFormats.PNG, null)));
	}

	public BufferedImage recvimg () throws IOException, ImageReadException {
		temp = in.readLine();
		if (temp == null)
		{
			throw new ImageReadException("Nothing for recvimg to read");
		}
		return Imaging.getBufferedImage(
				DatatypeConverter.parseBase64Binary(temp));
	}

}
