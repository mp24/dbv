package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
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

            //original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
   //         original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:47:20.png");
     original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:48:32.png");
            //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:43:01.png");
        }

        ImagePlus bin = new ImagePlus("bin", original.getProcessor());
        IJ.run(original, "Enhance Contrast...", "saturated=1.2 equalize");

        IJ.run(bin, "8-bit", "");
        IJ.setAutoThreshold(bin, "Otsu dark");
        //IJ.setAutoThreshold(bin, "Minimum");
        // IJ.setRawThreshold(bin, 100, 255, null);
        IJ.run(bin, "Convert to Mask", "");

        HashMap<Integer, Line> segmentMap = new HashMap();
        int col = 0;
        int noSegments = 0;
        for (int x = 0; x < (bin.getWidth()); x = x + scanColDist) {
            bin.setRoi(new Line(x, 0, x, bin.getHeight()));
            ProfilePlot p = new ProfilePlot(bin);
            double[] curProfile = p.getProfile();
            // scans.add(curProfile);

            //aktuelles Zeilenprofil untersuchen
            int currSegmentHeight = 0;
            int startY = -1;
            for (int y = 0; y < curProfile.length; y++) {
                if (curProfile[y] == 255) {
//                    original.setColor(Color.magenta);
//                    original.getProcessor().drawDot(x, t);
                    if (currSegmentHeight == 0) {
                        startY = y;
                        noSegments++;
                    }
                    currSegmentHeight++;
                } else {
                    currSegmentHeight = 0;
//                     original.setColor(Color.green);
//                    original.getProcessor().drawDot(x, t);
                }
                if (currSegmentHeight > minBoxHeight) {
                    segmentMap.put(noSegments, new Line(x, startY, x, startY + currSegmentHeight));
                    //IJ.log("Column "+col+" Segment:"+noSegments+" höhe"+currSegmentHeight+" startY"+t);
//                    original.setColor(Color.magenta);
//                    original.getProcessor().drawLine(x, startY, x, startY + currSegmentHeight);
                }
            }
            col++;

            //IJ.log(Arrays.toString(p.getProfile()));
        }
        IJ.log(segmentMap.toString());
        //IJ.log(Arrays.toString(columnsXvals));
        int scanlineVOffset = scanColDist + 1;//(int) (scanColDist * 1.8);
        for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Line> entry = it.next();
            Line lineSeg = entry.getValue();
            //IJ.log(entry.getKey() + "");

            boolean overMaxHeight = lineSeg.y2 > (lineSeg.y1) + maxBoxHeight;
            if (overMaxHeight) { //entferne zu hohe linien  
                original.setColor(Color.darkGray);
                original.getProcessor().draw(lineSeg);
                it.remove();
            } else {
                //preavCol = lineSeg.x1;
//                original.setColor(Color.yellow);
//                original.getProcessor().draw(lineSeg);

                boolean leftEdge = false;
                Line leftVline = new Line(lineSeg.x1 + scanlineVOffset, lineSeg.y1 + scanlineVOffset, lineSeg.x2 + scanlineVOffset, lineSeg.y2 - scanlineVOffset);
                bin.setRoi(leftVline);
                double[] leftProfile = new ProfilePlot(bin).getProfile();
//                original.setColor(Color.red);
//                original.getProcessor().draw(leftVline);
                for (int b = 0; b < leftProfile.length; b++) {
                    if (b > leftProfile.length * 0.1 && b < leftProfile.length * 0.9) {
                        if (leftProfile[b] == 0) {
//                            original.setColor(Color.magenta);
//                            original.getProcessor().drawDot(leftVline.x1, leftVline.y1 + b);
                            leftEdge = true;
                        } else {
                            leftEdge = false;
//                            original.setColor(Color.lightGray);
//                            original.getProcessor().drawDot(leftVline.x1, leftVline.y1 + b);
                        }
                    }
                }

                boolean rightEdge = false;
                Line rightVline = new Line(lineSeg.x1 - scanlineVOffset, lineSeg.y1 + scanlineVOffset, lineSeg.x2 - scanlineVOffset, lineSeg.y2 - scanlineVOffset);
                bin.setRoi(rightVline);
                double[] rightProfile = new ProfilePlot(bin).getProfile();
//                original.setColor(Color.green);
//                original.getProcessor().draw(rightVline);
                for (int b = 0; b < rightProfile.length; b++) {
                    if (b > rightProfile.length * 0.1 && b < rightProfile.length * 0.9) {
                        if (rightProfile[b] == 0) {
                            rightEdge = true;
                            //IJ.log(rightProfile[b] + "");
//                            original.setColor(Color.cyan);
//                            original.getProcessor().drawDot(rightVline.x1, rightVline.y1 + b);

                        } else {
                            rightEdge = false;
//                            original.setColor(Color.lightGray);
//                            original.getProcessor().drawDot(rightVline.x1, rightVline.y1 + b);
                        }
                    }
                }

                if (!leftEdge) {
                    if (!rightEdge) {
                        original.setColor(Color.gray);
                        original.getProcessor().draw(lineSeg);
                        it.remove();
                    }
                } else if (rightEdge) {
                    original.setColor(Color.gray);
                    original.getProcessor().draw(lineSeg);
                    it.remove();
                }
            }

        }

        IJ.log(segmentMap.toString());

        //key = segmentNumber
        for (Map.Entry<Integer, Line> segA : segmentMap.entrySet()) {
            for (Map.Entry<Integer, Line> segB : segmentMap.entrySet()) {
                //for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
                Line lA = segA.getValue();
                Line lB = segB.getValue();
                if (segA.getKey() != segB.getKey() && lA.x1 != lB.x1) { //Ungleiches Segment && UNgleiche Spalte?
                    if (lB.x1 >= lA.x1 + minBoxWidth && lB.x1 <= lA.x1 + maxBoxWidth) { //horizontale min & max distance?

                        //sind segmente verbundne?
                        int scanlineHOffset = scanColDist / 2;
                        //oben
                        boolean topConnected = false;
                        Line topHline = new Line(lA.x1, lA.y1, lB.x1, lB.y1);
                        bin.setRoi(new Line(lA.x1, lA.y1 + scanlineHOffset, lB.x1, lB.y1 + scanlineHOffset));
                        if (topHline.getLength() <= lA.getLength()) {// && topHline.getAngle() > (-maxAngle) && topHline.getAngle() < maxAngle) {
                            double[] topProfile = new ProfilePlot(bin).getProfile();
                            //IJ.log(Arrays.toString(topProfile));
                            for (int t = 0; t < topProfile.length; t++) {
                                if (topProfile[t] == 255) {
                                    topConnected = true;
                                } else {
                                    topConnected = false;
                                }
                            }
//                            original.setColor(Color.magenta);
//                            original.getProcessor().draw(topHline);
                            // IJ.log(topHline.getAngle()+"");
                        }
                        //unten
                        boolean bottomConnected = false;
                        Line bottomHline = new Line(lA.x2, lA.y2, lB.x2, lB.y2);
                        bin.setRoi(new Line(lA.x2, lA.y2 - scanlineHOffset, lB.x2, lB.y2 - scanlineHOffset));
                        if (bottomHline.getLength() <= lA.getLength()) {//  && bottomHline.getAngle() > (-maxAngle) && bottomHline.getAngle() < maxAngle) {
                            double[] bottomProfile = new ProfilePlot(bin).getProfile();

                            for (int b = 0; b < bottomProfile.length; b++) {
                                if (bottomProfile[b] == 255) {
                                    bottomConnected = true;
                                } else {
                                    bottomConnected = false;
                                }
                            }
//                            original.setColor(Color.blue);
//                            original.getProcessor().draw(bottomHline);
                            // IJ.log(bottomHline.getAngle()+"");
                        }

                        if (topConnected && bottomConnected) {
                            double angleTop = topHline.getAngle();
                            double angleBottom = bottomHline.getAngle();
                            if ( angleBottom > (-maxAngle) && angleBottom < maxAngle) {
                                if (angleTop > (-maxAngle) && angleTop < maxAngle) {
                                    IJ.log(angleTop+" , "+angleBottom);
                                    //Winkel und/oder Länge von Horizontalen grenzen damit entzerrt quadratisch?
                                    //if (Math.abs(angleTop) <= Math.abs(angleBottom) * 1.2 && Math.abs(angleTop) >= Math.abs(angleBottom) * 0.8) {
                                        original.setColor(Color.cyan);
////
                                        boundingBoxes.add(new Rectangle((int) lA.x1, (int) lA.y1, (int) topHline.getLength(), (int) lA.getLength()));

                                        original.setColor(Color.magenta);
                                         original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 10));
                                         original.getProcessor().drawString(angleTop+" , "+angleBottom, lA.x1, lB.y1);

                                        original.getProcessor().draw(lA);
                                        original.getProcessor().draw(lB);
//
                                        original.setColor(Color.red);
                                        original.getProcessor().draw(topHline);

                                        original.setColor(Color.blue);
                                        original.getProcessor().draw(bottomHline);
                                //    }
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

//    private boolean conectedH(int x1, int y1, int x2, int y2){
//    
//    }
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
