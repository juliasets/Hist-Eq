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
import java.util.concurrent.*;

import org.apache.commons.imaging.*;

public class Commissar {

    private static void usage () {
        System.out.println(
            "Usage: Commissar directory outputdir host port " + 
            "[host port [host port [...]]]");
        System.exit(1);
    }

    public static void main(String[] argv){
        if ((argv.length < 4) || ((argv.length%2) != 0)) usage();
        
        int MAX_HEIGHT = 100;
        int BUFFER = 1;
        
        String directory = argv[0];
        String outdirectory = argv[1];
		
		File dir = new File(directory);
        ArrayList<String> names = 
            new ArrayList<String>(Arrays.asList(dir.list()));
        ArrayList<String> inputFilenames = new ArrayList<String>();
        
        for (int i = 0; i < names.size(); i++)
        {
        	if (names.get(i).endsWith(".png") ||
        		names.get(i).endsWith(".PNG"))
        	{
        		inputFilenames.add(names.get(i));
        	}
        }
        
        if (inputFilenames.size() < 1) usage();
        
        try ( Protocol p = new Protocol(); )
        {
            ExecutorService executor = 
			    Executors.newFixedThreadPool(
			    Runtime.getRuntime().availableProcessors());
            ArrayList<BufferedImage> outIms = new ArrayList<BufferedImage>();
            for (int i = 2; i < argv.length; i += 2) 
            {
                try {
                    p.addServer(argv[i], Integer.parseInt(argv[i + 1]));
                } catch (NumberFormatException e2) { usage(); }
            }
            
            System.out.println("sending images");
            for (int i = 0; i < inputFilenames.size(); i++) 
            {
                File f = new File(directory + "/" + inputFilenames.get(i));
                BufferedImage im = Imaging.getBufferedImage(f);
                int h = im.getHeight();
                int w = im.getWidth();
                BufferedImage result = new BufferedImage( w, h, im.getType());
                Graphics g = result.getGraphics();
                outIms.add(result);
                System.out.println("splitting image " + i);
                if (h > MAX_HEIGHT)
                {
                    int num = Math.round(h/MAX_HEIGHT);
                    if (num == 1)
                    {
                        ImageCommunicator imgComm = 
                            new ImageCommunicator(p.communicate());
                        SingleJob job = new SingleJob(imgComm, im, g, h, 0, 0);
                        executor.execute(job);
                    }
                    else
                    {
                        int smH = (int) Math.floor(h/num);
                        
                        ImageCommunicator imgComm = 
                            new ImageCommunicator(p.communicate());
                        SingleJob job = new SingleJob(imgComm, 
                            im.getSubimage(0, 0, w, smH + BUFFER), 
                            g, smH, 0, 0);
                        executor.execute(job);
                        
                        for (int j = 1; j < num - 1; j++)
                        {
                            imgComm = new ImageCommunicator(p.communicate());
                            job = new SingleJob(imgComm, 
                                im.getSubimage(0, j*smH - BUFFER, 
                                    w, smH + 2*BUFFER), 
                                g, smH, j*smH + BUFFER, BUFFER);
                            
                            executor.execute(job);
                        }
                        imgComm = new ImageCommunicator(p.communicate());
                        job = new SingleJob(imgComm, 
                            im.getSubimage(0, (num - 1)*smH  - BUFFER, 
                                w, h - (num - 1)*smH + BUFFER), 
                            g, h - (num - 1)*smH, (num - 1)*smH, BUFFER);
                        
                        executor.execute(job);
                    }
                }
                else
                {
                    ImageCommunicator imgComm = 
                        new ImageCommunicator(p.communicate());
                    SingleJob job = new SingleJob(imgComm, im, g, h, 0, 0);
                    executor.execute(job);
                }
            }
            
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            
            for (int i = 0; i < inputFilenames.size(); i++) 
            {
                File f = new File(
                    outdirectory + "/" + "processed-" + inputFilenames.get(i));
                Imaging.writeImage(outIms.get(i), f, ImageFormats.PNG, null);
            }
            
            p.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageReadException e) {
            System.err.println("ImageReadException");
            e.printStackTrace();
            System.exit(1);
        } catch (ImageWriteException e) {
            System.err.println("ImageWriteException");
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
