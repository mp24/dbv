package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Pack200;
import javafx.util.Pair;

public class QR_Plugin implements PlugIn {

    String path;
    ImagePlus original;

    int minBoxHeight = 70;
    int maxBoxHeight = 350;// 500;
    int minBoxWidth = minBoxHeight / 2;
    int maxBoxWidth = maxBoxHeight;
    int maxAngle = 20;

    int scanColDist = 7;

    ArrayList<Rectangle> boundingBoxes = new ArrayList<Rectangle>();

    @Override
    public void run(String string) {

        if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) {
            String args = Macro.getOptions().trim();
            path = args;
            original = IJ.openImage(path);
        } else {
            //original = IJ.getImage();

            //original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
            //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:47:20.png");
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
        int preavCol = 0;

        for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Line> entry = it.next();
            Line lineSeg = entry.getValue();
            preavCol = lineSeg.x1;
            //IJ.log(entry.getKey() + "");
            if (lineSeg.y2 > (lineSeg.y1) + maxBoxHeight) { //entferne zu hohe linien  
//                original.setColor(Color.gray);
//                original.getProcessor().draw(lineSeg);
                it.remove();
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
                        if (topHline.getLength() <= lA.getLength()
                                && topHline.getAngle() > (-maxAngle) && topHline.getAngle() < maxAngle) {
                            ProfilePlot topP = new ProfilePlot(bin);
                            double[] topProfile = topP.getProfile();
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
                        if (bottomHline.getLength() <= lA.getLength()
                                && bottomHline.getAngle() > (-maxAngle) && bottomHline.getAngle() < maxAngle) {
                            ProfilePlot bottomP = new ProfilePlot(bin);
                            double[] bottomProfile = bottomP.getProfile();

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

                        if (topConnected && bottomConnected) {//&& !alreadyInBox(lA, lB)) {

                            int scanlineVOffset = (int) (scanColDist * 1.8);

                            boolean blackLeft = false;
                            Line leftVline = new Line(lA.x1 + scanlineVOffset, lA.y1 + scanlineVOffset, lA.x2 + scanlineVOffset, lA.y2 - scanlineVOffset);
                            bin.setRoi(leftVline);

                            ProfilePlot leftP = new ProfilePlot(bin);
                            double[] leftProfile = leftP.getProfile();
                            for (int b = 0; b < leftProfile.length; b++) {
                                //if () {
                                    if (leftProfile[b] == 0) {
                                        original.setColor(Color.red);
                                        original.getProcessor().draw(leftVline);
                                        blackLeft = true;
                                    } else {
                                        blackLeft = false;
                                    }

                               // }

                            }

                            boolean blackRight = false;
                            Line rightVline = new Line(lB.x1 - scanlineVOffset, lB.y1 + scanlineVOffset, lB.x2 - scanlineVOffset, lB.y2 - scanlineVOffset);
                            bin.setRoi(rightVline);

                            ProfilePlot rightP = new ProfilePlot(bin);
                            double[] rightProfile = rightP.getProfile();
                            for (int b = 0; b < rightProfile.length; b++) {
                                if (rightProfile[b] == 0) {
                                    blackRight = true;
                                    original.setColor(Color.green);
                                    original.getProcessor().draw(rightVline);
                                } else {
                                    blackRight = false;
                                }
                            }
                            if (blackLeft && blackRight) {
                                boundingBoxes.add(new Rectangle((int) lA.x1, (int) lA.y1, (int) topHline.getLength(), (int) lA.getLength()));
                            }
//                                original.setColor(Color.green);
//                                // original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 20));
//                                // original.getProcessor().drawString(segA.getKey() + "," + segB.getKey(), lA.x1, lB.y1);
//
//                                original.getProcessor().draw(lA);
//                                original.getProcessor().draw(lB);
////
//                                original.setColor(Color.red);
//                                original.getProcessor().draw(topHline);
//
//                                original.setColor(Color.blue);
//                                original.getProcessor().draw(bottomHline);

                        }

                    }

                }
            }
        }

//         List<Rectangle> deleteCandidates = new ArrayList<>();
//        for(Rectangle r1 :boundingBoxes){
//           for(Rectangle r2 :boundingBoxes){
//               if(!r1.equals(r2)){
//                   if(r1.contains(r2)){
//                      // boundingBoxes.remove(r2);
//                       deleteCandidates.add(r2);
//                   }else if(r2.contains(r1)){
//                       //boundingBoxes.remove(r1);
//                        deleteCandidates.add(r1);
//                   }
//                   
//               }
//            } 
//        }
//        for (Rectangle deleteCandidate : deleteCandidates) {
//            boundingBoxes.remove(deleteCandidate);
//         }
        for (Rectangle r1 : boundingBoxes) {
            original.setColor(Color.magenta);
            original.getProcessor().draw(new Roi(r1));
        }

        original.show();
        bin.show();
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
