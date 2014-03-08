import java.awt.Color;
import java.net.Socket;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.apache.commons.imaging.*;

public class EdgeWorker implements Runnable{

    private Communicator comm;
    private ConvolveOp convolver;
    private static float[] LaplacianData = {1, 4, 1, 4, -20, 4, 1, 4, 1};
    
    public EdgeWorker (Communicator comm){
        this.comm = comm;
        Kernel k = new Kernel(3, 3, LaplacianData);
        convolver = new ConvolveOp(k);
    }
    
    public void run(){
        try {
        ImageCommunicator imComm = new ImageCommunicator(comm);
        BufferedImage original;
        BufferedImage processed;
        
        original = imComm.recvImg();
        processed = detectEdges(original);
        imComm.sendImg(processed);
        comm.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ImageReadException e) {
            System.err.println("ImageReadException");
            e.printStackTrace();
            System.exit(1);
        } catch (ImageWriteException e) {
            System.err.println("ImageWriteException");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private BufferedImage detectEdges(BufferedImage orig)
    {
        BufferedImage detected = convolver.createCompatibleDestImage(
            orig, orig.getColorModel());
        convolver.filter(orig, detected);
        return detected;
    }
}
