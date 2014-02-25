import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.commons.imaging.*;
import java.util.*;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.imaging.*;

public class Commissar {

    private static Log LOG = LogFactory.getLog(Commissar.class);
    private void log (String msg) {
        SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz");
        LOG.info(
                 fmt.format(new Date()).toString() + ": " + msg);
    }

    private static void usage () {
        System.out.println(
                           "Usage: Commissar directory host port " + 
                           "[host port [host port [...]]]");
        System.exit(1);
    }

    public static void main(String[] argv){
        if ((argv.length < 3) || ((argv.length%2) != 1)) usage();
        
        log("Commissar started.");

        String directory = argv[0];
        
        File dir = new File(directory);
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(dir.list()));
        ArrayList<String> inputFilenames = new ArrayList<String>();
        
        for (int i = 0; i < names.size(); i++)
            {
                if (names.get(i).endsWith(".png") ||
                    names.get(i).endsWith(".PNG"))
                    {
                        inputFilenames.add(names.get(i));
                        log("Image added: " + names.get(i));
                    }
                else {log("Image not added: " + names.get(i));}
            }
        
        if (inputFilenames.size() < 1) usage();
        
        try ( Protocol p = new Protocol(); )
                {
                    for (int i = 1; i < argv.length; i += 2) {
                        try {
                            p.addServer(argv[i], Integer.parseInt(argv[i + 1]));
                            log("Server added at" + argv[i + 1]);
                        } catch (NumberFormatException e2) { usage(); }
                    }
                    for (int i = 0; i < inputFilenames.size(); i++) {
                        log("Job created for " + inputFilenames.get(i));
                        ImageCommunicator imgComm = 
                            new ImageCommunicator(p.communicate());
                        imgComm.sendImg(directory + "/" + inputFilenames.get(i));
                        BufferedImage result = imgComm.recvImg();
                        log("Job finished for " + inputFilenames.get(i));
                        File f = new File(
                                          directory + "/" + "processed-" + inputFilenames.get(i));
                        Imaging.writeImage(result, f, ImageFormats.PNG, null);
                        log("File saved for " + inputFilenames.get(i) + " in file processed-" + inputFilenames.get(i));
                    }
                    p.close();
                } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageReadException e) {
            System.err.println("ImageReadException");
            log("ImageReadException");
            e.printStackTrace();
            System.exit(1);
        } catch (ImageWriteException e) {
            System.err.println("ImageWriteException");
            log("ImageReadException");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
