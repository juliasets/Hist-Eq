import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

public class Worker{
	public static BufferedImage convert(Image img)
	{
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(img, 0, 0, null);
		bg.dispose();
		return bi;
	}

	public static BufferedImage toGrayScale(Image img)
	{
		// Convert image from type Image to BufferedImage
		BufferedImage bufImg = convert(img);

		// Scan through each row of the image
		for(int j=0; j < bufImg.getHeight(); j++)
		{
		    // Scan through each columns of the image
		    for(int i=0; i < bufImg.getWidth(); i++)
		    {
		        // Returns an integer pixel in the default RGB color model
		        int values=bufImg.getRGB(i,j);
		        // Convert the single integer pixel value to RGB color
		        Color oldColor = new Color(values);

		        int red = oldColor.getRed();        // get red value
		        int green = oldColor.getGreen();    // get green value
		        int blue = oldColor.getBlue();  // get blue value

		        // Convert RGB to gray scale using formula
		        // gray = 0.299 * R + 0.587 * G + 0.114 * B
		        double grayVal = 0.299*red + 0.587*green + 0.114*blue;

		        // Assign each channel of RGB with the same value
		        Color newColor = new Color((int)grayVal, (int)grayVal, (int)grayVal);

		        // Get back the integer representation of RGB color
		        // and assign it back to the original position
		        bufImg.setRGB(i, j, newColor.getRGB());
		    }
		}
		// return back the resulting image in BufferedImage type
		return bufImg;
	}

	public static BufferedImage histEqualization(Image img)
	{
		//Convert image to BufferedImage
		img = ImageProcessor.toGrayScale(img);
		BufferedImage bufImg = convert(img);


		//Getting information of each pixel;
		int[][] intensity = new int[bufImg.getWidth()][ bufImg.getHeight()];
		int[] counter = new int[256];
		for(int j=0; j < bufImg.getHeight();j++)
		    for(int i=0; i < bufImg.getWidth();i++)
		    {
		        int values=bufImg.getRGB(i,j);              
		        Color oldColor = new Color(values);
		        intensity[i][j] = oldColor.getBlue();
		        counter[intensity[i][j]]++;
		    }

		//BEGIN OF Histogram Equalization

		//find out how many rows the table have
		int row=0;

		for(int i=0;i<256;i++)
		    if(counter[i]!=0)
		        row++;

		//Find out the v column of the table
		//table[row][0] = v column
		//table[row][1] = c column
		int temp=0;
		int[][] table = new int[row][2];


		for(int i=0;i<256;i++)
		    if(counter[i]!=0)
		    {
		        table[temp][0] = i;
		        temp++;
		    }

		//Find out the c column of the table
		for(int i=0;i<row;i++)
		    table[i][1] = counter[table[i][0]];

		//C-> CS

		int sum = 0;

		for(int i=0;i<row;i++)
		{
		    sum += table[i][1];
		    table[i][1] = sum;
		}

		//CS->NCS
		int min = table[0][1], max = table[row-1][1];

		for(int i=0;i<row;i++)
		    table[i][1] = Math.round((table[i][1]-min)/(max-min));

		//Mapping
		for(int j=0;j<bufImg.getHeight();j++)
		    for(int i=0;i<bufImg.getWidth();i++)
		    {
		        for(int k=0;k<row;k++)
		            if(intensity[i][j]==table[k][0])
		                intensity[i][j] = table[k][1];

		        Color newColor = new Color(intensity[i][j], intensity[i][j], intensity[i][j]);

		        bufImg.setRGB(i, j, newColor.getRGB());
		    }


		return bufImg;
	}
}
