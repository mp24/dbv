package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.PlugIn;
import ij.process.FloodFiller;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author tina
 */
public class detectBoundingBox implements PlugIn {

    String path;
    ImagePlus imp;
    ImagePlus original;
    
    
        int tag = 127;
        int tagColor = (255 << 24) | (tag << 16) | (tag << 8) | tag;
        
            int minBoxHeight = 50;
        ArrayList<Roi> boundingBoxes = new ArrayList<Roi>();

    @Override
    public void run(String string) {
        original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
        //original = IJ.openImage("/home/tina/Desktop/2.jpg");
        imp = new ImagePlus("Bin", original.getProcessor());
        imp.getType();

        ImageConverter ic = new ImageConverter(imp);
        if (imp.getType() != imp.GRAY8) {
            ic.convertToGray8();
        }

        IJ.run(imp, "Find Edges", "");
        IJ.run(imp, "Make Binary", "");
        //IJ.run(imp, "Invert", "");
        imp.show();

        //   showDialog();
//       ic.convertToRGB();
//       imp.draw(Xs[0], 0, 0, 0);
        ic.convertToRGB();

        ImageProcessor ip = getTheBox(imp.getProcessor());

        ImageRoi imageRoi = new ImageRoi(imp.getWidth(), imp.getHeight(), ip);
        Overlay overlay = new Overlay(imageRoi);
        imp.setOverlay(overlay);
        imp.setRoi(imageRoi);
        imp.show();

    }

    private ImageProcessor getTheBox(ImageProcessor ip) {
        //ImageProcessor ip = imp.getProcessor();   

        int target = 0;
        int targetColor = (255 << 24) | (target << 16) | (target << 8) | target;

        int markedPXs = 0;
        int regionCount = 0;
        int found = 0;

        //while (markedPXs < ip.getPixelCount()/2) {
       // while (found <= 2) {
        while (boundingBoxes.size()<7) {
            Wand wand = new Wand(ip);
            FloodFiller ff = new FloodFiller(ip);
            Random rand = new Random();
            int xRand = rand.nextInt(ip.getWidth()) + 0;//0 - width //2354;//
            int yRand = rand.nextInt(ip.getHeight()) + 0;//0 - height//1747;//
            //IJ.log("" + ip.getPixel(xRand, yRand));

            if (ip.getPixel(xRand, yRand) != tagColor) {
                if (ip.getPixel(xRand, yRand) == targetColor) {
                    wand.autoOutline(xRand, yRand, 0.0, 4);//, 0.0 ,1);
                    //IJ.log(Arrays.toString(wand.xpoints));
                    //IJ.log(Arrays.toString(wand.ypoints));

                    int[] Xs = wand.xpoints;
                    int[] Ys = wand.ypoints;

                    ip.setColor(Color.magenta);

//                    for (int i = 0; i < Xs.length; i++) {
//                        //  ip.drawString("("+Xs[i]+","+Ys[i]+")", Xs[i]+10, Ys[i]+fontSize);
//                        ip.drawDot(Xs[i], Ys[i]);
//                    }
                    int tLX = getMin(Xs);
                    int tLY = getMin(Ys);
                    //IJ.log(tLX + ";" + tLY);

                    int width = getMax(Xs) - tLX;
                    int height = getMax(Ys) - tLY;

                    Roi r = new Roi(
                            tLX, tLY,
                            width, height
                    );
//                    IJ.log("roi" + tLX + "," + tLY + ","
//                            + width + "," + height);

                    //int minBoxHeight = 50;
                    double sideRatio = width / height;
                     double area = r.getStatistics().area;
                    if (r.getStatistics().area >= minBoxHeight * minBoxHeight && (area <= (original.getProcessor().getWidth() * original.getProcessor().getHeight()) * 0.8)) {
                        if ((sideRatio >= 0.9) && (sideRatio <= 1.1)) {
//                            IJ.log("w/h" + width + ";" + height);
//                            IJ.log("a" + width * height + "");
//                            ip.fill(r);
//                            found++;

                        ip.draw(r);
                        ip.setFont(new Font("SansSerif", Font.PLAIN, minBoxHeight / 3));
                        ip.drawString(boundingBoxes.size() + "", tLX, tLY);
//                        IJ.log("roi" + tLX + "," + tLY + ","
//                                + width + "," + height + ";");
//                            boundingBoxes.add(r);

                        } else {
                            markedPXs =markedPXs+excludeRegion(ip, xRand, yRand);
//                            ip.setColor(tagColor);
//                            ff.fill(xRand, yRand);
//                            regionCount++;
//                            int[] histo = ip.getHistogram();
//                            markedPXs = histo[tag];
                        }
                    } else {
                        markedPXs =markedPXs+excludeRegion(ip, xRand, yRand);
//                        ip.setColor(tagColor);
//                        ff.fill(xRand, yRand);
//                        regionCount++;
//                        int[] histo = ip.getHistogram();
//                        markedPXs = histo[tag];
                    }
                }else {
                    markedPXs =markedPXs+excludeRegion(ip, xRand, yRand);
//                    ip.setColor(tagColor);
//                    ff.fill(xRand, yRand);
//                     regionCount++;
//                     int[] histo = ip.getHistogram();
//                     markedPXs = histo[tag];
                    
                }
                   // IJ.log(regionCount + "");
                    //int[] histo = ip.getHistogram();
                    //markedPXs = histo[tag];
                }
            }
            //Rectangle r = new Rectangle();
            return ip;
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
        int max = 0;// = Integer.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) {
                i++;
                //IJ.log(array[i]+" ZERO");
            }
            if (array[i] > max) {
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
        Class<?> clazz = detectBoundingBox.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        IJ.runPlugIn(clazz.getName(), "");
    }

}
