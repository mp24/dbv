package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.Histogram;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.FloodFiller;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author tina
 */
public class detectBoundingBoxByColor implements PlugIn {

    String path;
    ImagePlus imp;
    ImagePlus original;

    ArrayList<Roi> boundingBoxes = new ArrayList<Roi>();
    int grenzwert = 70;

    @Override
    public void run(String string) {
        //original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
        //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:47:20.png");
        original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:48:32.png");
        //original = IJ.openImage("/home/tina/Desktop/2.jpg");

        int[] redVals = new int[original.getProcessor().getPixelCount()];
        int[] blueVals = new int[original.getProcessor().getPixelCount()];
        int i = 0;

//         IJ.run(original, "Enhance Contrast...", "saturated=1.2 equalize");
        //IJ.run(original, "Subtract Background...", "rolling=50 light separate");
        IJ.run(original, "Enhance Contrast...", "saturated=1.2 equalize");
        ColorProcessor ip = new ColorProcessor(original.getImage());

        //grenzwert = (int)original.getStatistics().mean - (int)original.getStatistics().stdDev ;
        Rectangle r = ip.getRoi();
        for (int y = r.y; y < (r.y + r.height); y++) {
            for (int x = r.x; x < (r.x + r.width); x++) {
                Color c = ip.getColor(x, y);
                ///IJ.log(c+"");
                int red = c.getRed();
                int blue = c.getBlue();
                int green = c.getGreen();

                if ((red < blue * 0.99 && red < green * 0.99) || (red > blue * 1.1 && red > green * 1.1)) {
                    if (red > green && red > blue && red > grenzwert) {
                        //inspectRegion(x,y,ip);
                        redVals[i] = ip.get(x, y);
                        //IJ.log("RED"+ip.get(x, y));
                        ip.setColor(Color.red);
                        ip.drawDot(x, y);
                    }
                }
                if ((blue < red * 0.9 && blue < green * 0.9) || (blue > red * 1.1 && blue > green * 1.1)) {
                    if (blue > red && blue > green && blue > grenzwert) {
                        //inspectRegion(x,y,ip);
                        //IJ.log("Blue"+ip.get(x, y));
                        blueVals[i] = ip.get(x, y);
                        ip.setColor(Color.blue);
                        ip.drawDot(x, y);

                    }
                }

                i++;
            }
        }

//                            IJ.log(Arrays.toString( redVals));
//                    IJ.log(Arrays.toString( blueVals));
        new ImagePlus("Red&Blue", ip).show();
        original.show();
    }

    private void inspectRegion(int x, int y, ImageProcessor ip) {
        ip.setColor(Color.magenta);
        ip.drawDot(x, y);
    }

    private int getMin(int[] array) {
        //int min = -16777215;//array[0];// = Integer.MAX_VALUE;
        // int min = array[0];// = Integer.MAX_VALUE;
        int min = array[0];// = Integer.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) {
                i++;
            } else if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    private int getMax(int[] array) {
        //int max = 0;// = Integer.MAX_VALUE;
        int max = array[0];// = Integer.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) {
                i++;
                //IJ.log(array[i]+" ZERO");
            } else if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Main method for debugging.
     *
     * For debugging, it is convenient to have a method that starts ImageJ,
     * loads an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = detectBoundingBoxByColor.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        IJ.runPlugIn(clazz.getName(), "");
    }

}
