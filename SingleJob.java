import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.commons.imaging.*;
import java.util.*;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.*;
import java.awt.*;

public class SingleJob implements Runnable
{
    private BufferedImage toSend;
    private File toInsert;
    private int offset;
    private int interiorOffset;
    private int height;
    private ImageCommunicator imComm;
    
    SingleJob( ImageCommunicator imComm, BufferedImage toSend, File toInsert, 
        int height, int offset, int interiorOffset)
    {
        this.toSend = toSend;
        this.toInsert = toInsert;
        this.height = height;
        this.offset = offset;
        this.interiorOffset = interiorOffset;
        this.imComm = imComm;
    }
    
    public void run()
    {
        try
        {
            imComm.sendImg(toSend);
            
            BufferedImage received = imComm.recvImg();
            
            synchronized(toInsert)
            {
                BufferedImage im = Imaging.getBufferedImage(toInsert);
                Graphics g = im.getGraphics();
                g.drawImage( received.getSubimage(0, interiorOffset, 
                    received.getWidth(), height), 0, offset, null );
                Imaging.writeImage(im, toInsert, ImageFormats.PNG, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (ImageReadException e) {
            System.err.println("ImageReadException");
            e.printStackTrace();
            System.exit(1);
        } catch (ImageWriteException e) {
            System.err.println("ImageWriteException");
            e.printStackTrace();
            System.exit(1);
        }
    }
};
