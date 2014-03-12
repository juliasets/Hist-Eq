import java.util.*;
import java.io.*;
import java.net.*;
import org.apache.commons.imaging.*;
import java.util.Date;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.*;
import java.awt.*;
import java.util.concurrent.*;

import org.apache.commons.imaging.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Commissar {

    private static void usage () {
        System.out.println(
            "Usage: Commissar directory outputdir host port " + 
            "[host port [host port [...]]]");
        System.exit(1);
    }
    
    static final Logger logger = LogManager.getLogger(Commissar.class.getName());

    private static void log (String msg) {
    	logger.info(msg);
    }
    
    public static void main(String[] argv){
        if ((argv.length < 4) || ((argv.length%2) != 0)) usage();
        
        int MAX_HEIGHT = 300;
        int BUFFER = 2;
        
        String directory = argv[0];
		
		File dir = new File(directory);
        ArrayList<String> names = 
            new ArrayList<String>(Arrays.asList(dir.list()));
        ArrayList<String> inputFilenames = new ArrayList<String>();
        
        String outdirectory = argv[1];
        new File(outdirectory).mkdir();
		
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
            for (int i = 2; i < argv.length; i += 2) 
            {
                try {
                    p.addServer(argv[i], Integer.parseInt(argv[i + 1]));
                } catch (NumberFormatException e2) { usage(); }
            }
            
            System.out.println("sending images");
            Date date = new Date();
            long starttime = date.getTime();
            for (int i = 0; i < inputFilenames.size(); i++) 
            {
                System.out.println("splitting image " + i);
                File f = new File(directory + "/" + inputFilenames.get(i));
                BufferedImage im = Imaging.getBufferedImage(f);
                int h = im.getHeight();
                int w = im.getWidth();
                File outf = new File(
                    outdirectory + "/" + "processed-" + inputFilenames.get(i));
                Imaging.writeImage(im, outf, ImageFormats.PNG, null);
                if (h > MAX_HEIGHT)
                {
                    int num = Math.round(h/MAX_HEIGHT);
                    if (num == 1)
                    {
                        ImageCommunicator imgComm = 
                            new ImageCommunicator(p.communicate());
                        SingleJob job = new SingleJob(imgComm, im, outf, h, 0, 0);
                        executor.execute(job);
                    }
                    else
                    {
                        int smH = (int) Math.floor(h/num);
                        
                        ImageCommunicator imgComm = 
                            new ImageCommunicator(p.communicate());
                        SingleJob job = new SingleJob(imgComm, 
                            im.getSubimage(0, 0, w, smH + BUFFER), 
                            outf, smH, 0, 0);
                        executor.execute(job);
                        
                        for (int j = 1; j < num - 1; j++)
                        {
                            imgComm = new ImageCommunicator(p.communicate());
                            job = new SingleJob(imgComm, 
                                im.getSubimage(0, j*smH - BUFFER, 
                                    w, smH + 2*BUFFER), 
                                outf, smH, j*smH, BUFFER);
                            
                            executor.execute(job);
                        }
                        imgComm = new ImageCommunicator(p.communicate());
                        job = new SingleJob(imgComm, 
                            im.getSubimage(0, (num - 1)*smH  - BUFFER, 
                                w, h - (num - 1)*smH + BUFFER), 
                            outf, h - (num - 1)*smH, (num - 1)*smH, BUFFER);
                        
                        executor.execute(job);
                    }
                }
                else
                {
                    ImageCommunicator imgComm = 
                        new ImageCommunicator(p.communicate());
                    SingleJob job = new SingleJob(imgComm, im, outf, h, 0, 0);
                    executor.execute(job);
                }
                System.out.println("sent image " + i);
            }
            
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            
            Date date2 = new Date();
            String runtime = String.valueOf(date2.getTime() - starttime);
            log(runtime);
            
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
