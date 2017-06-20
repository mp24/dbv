import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.*;
import ij.plugin.filter.PlugInFilter;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.process.*;
import ij.gui.Roi;


	public class QR_Verkehrszeichen implements PlugInFilter{

		@Override
		public int setup(String arg, ImagePlus imp) {
			
			//DOES_8G Das Plugin akzeptiert nur B-Bit Grauwertbilder
			//NO_CHANGES Das Plugin modifiziert die Pixeldaten des uebergeben Bildes nicht 
			return DOES_8G;
		}
		@Override
		public void run(ImageProcessor ip) {
			//ImagePlus img = new ImagePlus("binär", ip);	
			//IJ.makeRectangle(202, 202, 764, 764);
			Rectangle r = ip.getRoi();
			
			int min_y = Integer.MAX_VALUE;
			int max_y = Integer.MIN_VALUE;
			int min_x = Integer.MAX_VALUE;
			int max_x = Integer.MIN_VALUE;
			
			// Durch jeden Pixel durchgehen x und y
			for ( int y=r.y; y<(r.y+r.height); y++)
				for (int x=r.x; x<(r.x+r.width); x++){
					if ( ip.getPixel(x, y) == 255){
							
							if (y > max_y){
								max_y=y;
						 
							if (y < min_y){
								min_y=y;}
							
								}
							
							if (x > max_x){
								max_x=x;
						 
							if (x < min_x){
								min_x=x;}
							
								}
							}
				}
			//System.out.println("Max_y: " +max_y);
			//IJ.makeLine(min_x, min_y, max_x, max_y);
			
			// Rechteck erzeugen -> Region 
			Roi roi = new Roi(
					min_x, min_y,
					max_x-min_x, max_y-min_y); 
			ip.setRoi(roi);
			
			ImageProcessor ip_neu = ip.crop();	
			createChessboard(ip_neu);
			ImagePlus img = new ImagePlus("binär", ip_neu);
			img.show();
			
			}
		private void createChessboard(ImageProcessor ip) {
	        int i = 0;
	        
			int height=ip.getHeight();
			int width=ip.getWidth();
			int elemWidth=width/5;
			for (int y = 0; y < height; y = y + elemWidth) {
				for (int x = 0; x < width; x = x + elemWidth) {
					ip.setColor(Color.blue);
	                Roi roi = new Roi(x, y, elemWidth, elemWidth);
					ip.draw(roi);
					
	                i++;
	            }
	             i++;
	        }
		/*	 selectColor();
	    }
		private void selectColor(ImageProcessor ip){
			Roi roi = img.getRoi();*/
			
			
		}

}


	


