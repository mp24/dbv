package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.PlugIn;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QR_Plugin implements PlugIn {

    String path;
    ImagePlus original;
    ImageRoi overlay;

    int minBoxHeight = 70;
    int maxBoxHeight = 350;// 500;
    int minBoxWidth = minBoxHeight / 2;
    int maxBoxWidth = maxBoxHeight;
    int maxAngle = 20;

    int scanColDist = 7;
   
    ArrayList<Rectangle> boundingBoxes = new ArrayList<Rectangle>();
    ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    @Override
    public void run(String string) {

        if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) {
            String args = Macro.getOptions().trim();
            path = args;
            original = IJ.openImage(path);
        } else {
            //original = IJ.getImage();

    //        original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
           original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:47:20.png");
    //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:48:32.png");
            //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:43:01.png");
    //        original = IJ.openImage("/home/tina/Desktop/2.jpg");
        }

        ImagePlus bin = new ImagePlus("bin", original.getProcessor());
        IJ.run(original, "Enhance Contrast...", "saturated=1.2 equalize");

        IJ.run(bin, "8-bit", "");
        IJ.setAutoThreshold(bin, "Otsu dark");
        IJ.run(bin, "Convert to Mask", "");

        HashMap<Integer, Line> segmentMap = new HashMap();
        int noSegments = 0;
        for (int x = 0; x < (bin.getWidth()); x = x + scanColDist) {
            bin.setRoi(new Line(x, 0, x, bin.getHeight()));
            ProfilePlot p = new ProfilePlot(bin);
            double[] curProfile = p.getProfile();

            //aktuelles Zeilenprofil untersuchen
            int currSegmentHeight = 0;
            int startY = -1;
            for (int y = 0; y < curProfile.length; y++) {
                if (curProfile[y] == 255) {
//                  original.setColor(Color.magenta);
//                  original.getProcessor().drawDot(x, t);
                    if (currSegmentHeight == 0) {
                        startY = y;
                        noSegments++;
                    }
                    currSegmentHeight++;
                } else {
                    currSegmentHeight = 0;
//                  original.setColor(Color.green);
//                  original.getProcessor().drawDot(x, t);
                }
                if (currSegmentHeight > minBoxHeight) {
                    segmentMap.put(noSegments, new Line(x, startY, x, startY + currSegmentHeight));
//                  original.setColor(Color.magenta);
//                  original.getProcessor().drawLine(x, startY, x, startY + currSegmentHeight);
                }
            }        
            //IJ.log(Arrays.toString(p.getProfile()));
        }
//        IJ.log(segmentMap.toString());

        int scanlineVOffset = scanColDist + 1;//(int) (scanColDist * 1.8);
        for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Line> entry = it.next();
            Line lineSeg = entry.getValue();

            boolean overMaxHeight = lineSeg.y2 > (lineSeg.y1) + maxBoxHeight;
            if (overMaxHeight) { //entferne zu hohe linien  
//              original.setColor(Color.darkGray);
//              original.getProcessor().draw(lineSeg);
                it.remove();
            } else { //entferne linien die keine Kante sind
                boolean leftEdge = true;
                Line leftVline = new Line(lineSeg.x1 + scanlineVOffset, lineSeg.y1 + scanlineVOffset, lineSeg.x2 + scanlineVOffset, lineSeg.y2 - scanlineVOffset);
                bin.setRoi(leftVline);
                double[] leftProfile = new ProfilePlot(bin).getProfile();
                for (int b = 0; b < leftProfile.length; b++) {
                    if (b > leftProfile.length * 0.1 && b < leftProfile.length * 0.9 && leftEdge) {
                        if (leftProfile[b] == 0) {
                            leftEdge = true;
//                          original.setColor(Color.magenta);
//                          original.getProcessor().drawDot(leftVline.x1, leftVline.y1 + b);
                        } else {
                            leftEdge = false;
//                          original.setColor(Color.lightGray);
//                          original.getProcessor().drawDot(leftVline.x1, leftVline.y1 + b);
                        }
                    }
                }

                boolean rightEdge = true;
                Line rightVline = new Line(lineSeg.x1 - scanlineVOffset, lineSeg.y1 + scanlineVOffset, lineSeg.x2 - scanlineVOffset, lineSeg.y2 - scanlineVOffset);
                bin.setRoi(rightVline);
                double[] rightProfile = new ProfilePlot(bin).getProfile();
                for (int b = 0; b < rightProfile.length; b++) {
                    if (b > rightProfile.length * 0.1 && b < rightProfile.length * 0.9 && rightEdge) {
                        if (rightProfile[b] == 0) {
                            rightEdge = true;
//                          original.setColor(Color.cyan);
//                          original.getProcessor().drawDot(rightVline.x1, rightVline.y1 + b);
                        } else {
                            rightEdge = false;
//                          original.setColor(Color.lightGray);
//                          original.getProcessor().drawDot(rightVline.x1, rightVline.y1 + b);
                        }
                    }
                }

                if (!leftEdge) {
                    if (!rightEdge) {
//                      original.setColor(Color.gray);
//                      original.getProcessor().draw(lineSeg);
                        it.remove();
                    }
                } else if (rightEdge) {
//                  original.setColor(Color.gray);
//                  original.getProcessor().draw(lineSeg);
                    it.remove();
                }
            }

        }
        //IJ.log(segmentMap.toString());

        //key = segmentNumber
        for (Map.Entry<Integer, Line> segA : segmentMap.entrySet()) {
            for (Map.Entry<Integer, Line> segB : segmentMap.entrySet()) {
                Line lA = segA.getValue();
                Line lB = segB.getValue();
                
                if (segA.getKey() != segB.getKey() && lA.x1 != lB.x1) { //Ungleiches Segment && UNgleiche Spalte?
                    if (lB.x1 >= lA.x1 + minBoxWidth && lB.x1 <= lA.x1 + maxBoxWidth) { //horizontale min & max distance?
                        
                        Line topHline = new Line(lA.x1, lA.y1, lB.x1, lB.y1);
                        Line bottomHline = new Line(lA.x2, lA.y2, lB.x2, lB.y2);
                        if(topHline.getLength() <= lA.getLength()){
                            
                            //sind segmente verbundne?
                            int scanlineHOffset = scanColDist / 2;

                            boolean topConnected = true;
                            bin.setRoi(new Line(lA.x1, lA.y1 + scanlineHOffset, lB.x1, lB.y1 + scanlineHOffset));
                            if (topHline.getLength() <= lA.getLength()) {// && topHline.getAngle() > (-maxAngle) && topHline.getAngle() < maxAngle) {
                                double[] topProfile = new ProfilePlot(bin).getProfile();
                                //IJ.log(Arrays.toString(topProfile));
                                for (int t = 0; t < topProfile.length; t++) {
                                    if (topProfile[t] == 255 && topConnected==true) {
                                        topConnected = true;

                                    } else {
                                        topConnected = false;
                                    }
                                }
//                              original.setColor(Color.magenta);
//                              original.getProcessor().draw(new Line(lA.x1, lA.y1 + scanlineHOffset, lB.x1, lB.y1 + scanlineHOffset));
                            }
                           
                            boolean bottomConnected = true;
                            bin.setRoi(new Line(lA.x2, lA.y2 - scanlineHOffset, lB.x2, lB.y2 - scanlineHOffset));
                            if (bottomHline.getLength() <= lA.getLength()) {//  && bottomHline.getAngle() > (-maxAngle) && bottomHline.getAngle() < maxAngle) {
                                double[] bottomProfile = new ProfilePlot(bin).getProfile();

                                for (int b = 0; b < bottomProfile.length; b++) {
                                    if (bottomProfile[b] == 255 && bottomConnected==true) {
                                        bottomConnected = true;

                                    } else {
                                        bottomConnected = false;
                                    }                                                                         
//                                  original.setColor(Color.cyan);
//                                  original.getProcessor().draw(new Line(lA.x2, lA.y2 - scanlineHOffset, lB.x2, lB.y2 - scanlineHOffset));
                                }
                            }
                            
                            //                                        Wand wand = new Wand(bin.getProcessor());
//                                        wand.autoOutline(lA.x1, lA.y1, 0.0, 4);
//                                        int[] Xs = wand.xpoints;
//                                        int[] Ys = wand.ypoints;
//                                                            for (int i = 0; i < Xs.length; i++) {
//                        //  ip.drawString("("+Xs[i]+","+Ys[i]+")", Xs[i]+10, Ys[i]+fontSize);
//                        original.getProcessor().drawDot(Xs[i], Ys[i]);
//                    }
//                                        
//                                        //suche äßere Box
//                                        int min_y = getMin(Ys);
//                                        int max_y = getMax(Ys);
//                                        int min_x = getMin(Xs);
//                                        int max_x = getMax(Xs);                                   
//                                         boundingBoxes.add(new Rectangle((int) min_x, min_y, min_x-max_x, min_y-max_y));

                            if (topConnected && bottomConnected) {
                                double angleTop = topHline.getAngle();
                                double angleBottom = bottomHline.getAngle();
                                if ( angleBottom > (-maxAngle) && angleBottom < maxAngle) {
                                    if (angleTop > (-maxAngle) && angleTop < maxAngle) {                                      
                   
                                        int y =(int) lA.y1 - scanlineHOffset;
                                        if(lB.y1 < y){
                                            y =lB.y1-scanlineHOffset;
                                        }
                                        int height = (int)lA.getLength();
                                        if(lB.y2 > lA.y2){
                                            height = (int)lA.getLength() + (lB.y2 - lA.y2);
                                        }
                                        int x= (int) lA.x1 - scanlineVOffset;
                                        int width= (int) topHline.getLength() + (2*scanlineVOffset);                                        
                                        
                                         boundingBoxes.add(
                                                 new Rectangle(x, y, width, height)
                                         );

                                        original.setColor(Color.magenta);
                                        original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 10));
                                        original.getProcessor().drawString(angleTop+" , "+angleBottom, lA.x1, lB.y1);

                                        original.getProcessor().draw(lA);
                                        original.getProcessor().draw(lB);

                                        original.setColor(Color.red);
                                        original.getProcessor().draw(topHline);

                                        original.setColor(Color.blue);
                                        original.getProcessor().draw(bottomHline);
                                    }
                                }
                            }                          
                        }
        
                    }
                }
                
            }
        }

        for (Rectangle r1 : boundingBoxes) {
            original.setColor(Color.yellow);
            original.getProcessor().draw(new Roi(r1));
        }

        bin.show();
        original.show();
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
        int max = 0;// = Integer.MIN_VALUE;
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
     * For debugging, it is convenient to have lA method that starts ImageJ,
     * loads an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = QR_Plugin.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        IJ.runPlugIn(clazz.getName(), "");
    }

}
