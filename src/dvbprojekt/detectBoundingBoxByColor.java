package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 *
 * @author tina
 */
public class detectBoundingBoxByColor implements PlugIn {

    String path;
    ImagePlus imp;
    ImagePlus original;

    ArrayList<Roi> boundingBoxesCode = new ArrayList<Roi>();
    ArrayList<Roi> boundingBoxesSign = new ArrayList<Roi>();
    int grenzwert = 50;

    int minBoxHeight = 50;

    int tag = 127;
    int tagColor = (255 << 24) | (tag << 16) | (tag << 8) | tag;

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

                //if (!alreadyInBox(x, y)) {                  
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
                        //inspectRegion(x,y,ip);
                    }
                }
                if ((blue < red * 0.9 && blue < green * 0.9) || (blue > red * 1.1 && blue > green * 1.1)) {
                    if (blue > red && blue > green && blue > grenzwert) {
                        //inspectRegion(x,y,ip);
                        //IJ.log("Blue"+ip.get(x, y));
                        blueVals[i] = ip.get(x, y);
                        ip.setColor(Color.blue);
                        ip.drawDot(x, y);
                        // inspectRegion(x,y,ip);

                    }
                }
                //}
                i++;
            }
        }

        for (int y = r.y; y < (r.y + r.height); y++) {
            for (int x = r.x; x < (r.x + r.width); x++) {
                if (!alreadyInBox(x, y) && ip.getPixel(x, y) != tagColor) {
                    //IJ.log(ip.getColor(x, y)+"");
                    if (ip.getColor(x, y).equals(Color.RED)) {
                        IJ.log("true red");
                        inspectRegion(x, y, ip);
                    }
                    if (ip.getColor(x, y) == Color.BLUE) {
                        IJ.log("true blue");
                        inspectRegion(x, y, ip);
                    }
                }
            }
        }

//                            IJ.log(Arrays.toString( redVals));
//                    IJ.log(Arrays.toString( blueVals));
        new ImagePlus("Red&Blue", ip).show();
        original.show();
    }

    private boolean alreadyInBox(int x, int y) {
//    private boolean alreadyInBox(int[] Xs, int[] Ys) {
        for (Roi r : boundingBoxesCode) {
            if (r.contains(x, y)) {
                //IJ.log("already in List (Code)");
                return true;
            };
        }
        for (Roi r : boundingBoxesSign) {
            if (r.contains(x, y)) {
                //IJ.log("already in List (Sign)");
                return true;
            };
        }
        return false;
    }

    private void inspectRegion(int x, int y, ImageProcessor ip) {
        // if( (ip.getColor(x, y) == Color.red) || (ip.getColor(x, y) == Color.blue)){
        Wand wand = new Wand(ip);
        wand.autoOutline(x, y, 0.0, 4);//, 0.0 ,1);
        int tLX = getMin(wand.xpoints);
        int tLY = getMin(wand.ypoints);
        //IJ.log(tLX + ";" + tLY);

        int width = getMax(wand.xpoints) - tLX;
        int height = getMax(wand.ypoints) - tLY;

        Roi roi = new Roi(
                tLX, tLY,
                width, height
        );
        if (width > 1 && height > 1) {
            double sideRatio = width / height;
            double area = roi.getStatistics().area;
            if (roi.getStatistics().area >= minBoxHeight * minBoxHeight
                    && (area <= (original.getProcessor().getWidth() * original.getProcessor().getHeight()) * 0.8)) {
                if ((sideRatio >= 0.9) && (sideRatio <= 1.1)) {

                    ip.setColor(Color.green);
                    ip.draw(roi);
                    ip.setFont(new Font("SansSerif", Font.PLAIN, minBoxHeight / 3));
                    ip.drawString(boundingBoxesSign.size() + "", tLX, tLY);
//                        IJ.log("roi" + tLX + "," + tLY + ","
//                                + width + "," + height + ";");
                    boundingBoxesSign.add(roi);
                           
                    
                    //find the codebox
                    Roi roi2 = new Roi(
                            tLX, tLY+height*1.1,
                            width, height
                    );
                    ip.setColor(Color.magenta);
                    ip.draw(roi2);
                    ip.setFont(new Font("SansSerif", Font.PLAIN, minBoxHeight / 3));
                    ip.drawString(boundingBoxesCode.size() + "", tLX, (int)(tLY+height*1.1) );
//                        IJ.log("roi" + tLX + "," + tLY + ","
//                                + width + "," + height + ";");
                           boundingBoxesCode.add(roi2);

                } else {
                    excludeRegion(ip, x, y);
                }
            } else {
                 excludeRegion(ip, x, y);
            }
            //  }
        }
    }

    private int excludeRegion(ImageProcessor ip, int x, int y) {
        int[] histo = ip.getHistogram();
        int prevMarked = histo[tag];
        int pxInRegion = 0;
        FloodFiller ff = new FloodFiller(ip);
        ip.setColor(tagColor);
        ff.fill(x, y);
        pxInRegion = histo[tag] - prevMarked;
        return pxInRegion;
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
        int max = 0;// = Integer.MAX_VALUE;
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
