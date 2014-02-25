

import java.net.*;
import java.io.*;


public class Communicator implements AutoCloseable{

    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket socket;

    public Communicator (Socket socket) throws IOException {
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
        this.socket = socket;
    }
    
    

    public void send (byte[] b) throws IOException {
        dos.writeInt(b.length);
        dos.write(b, 0, b.length);
    }

    public byte[] recv () throws IOException {
        int length = dis.readInt();
        byte[] b = new byte[length];
        dis.readFully(b);
        return b;
    }

    public void close () throws IOException {
        System.out.println("Closing imComm");
        socket.close();
    }

}


