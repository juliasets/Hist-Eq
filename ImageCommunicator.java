import java.nio.file.*;
import java.io.*;
import java.awt.image.*;
import org.apache.commons.imaging.*;

public class ImageCommunicator{
    private Communicator comm;

    public ImageCommunicator(Communicator comm)
    {
        this.comm = comm;
    }
    
    public void sendImg(String filename) throws IOException 
    {
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);
        comm.send(data);
    }
    
    public void sendImg(BufferedImage image) 
        throws IOException, ImageWriteException
    {
        byte[] data = Imaging.writeImageToBytes(image, ImageFormats.PNG, null);
        comm.send(data); 
    }
    
    public BufferedImage revcImg() throws IOException, ImageReadException
    {
        byte[] data = comm.recv();
        return Imaging.getBufferedImage(data);
    }
}
