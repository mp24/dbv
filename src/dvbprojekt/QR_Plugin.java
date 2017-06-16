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

    int minBoxHeight = 100;
    int maxBoxHeight = 500;
    int minBoxWidth = minBoxHeight / 2;
    int maxBoxWidth = maxBoxHeight;
    int maxAngle = 20;

    int scanColDist = 7;
    
    ArrayList<Roi> boundingBoxes = new ArrayList<Roi>();

    @Override
    public void run(String string) {

        if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) {
            String args = Macro.getOptions().trim();
            path = args;
            original = IJ.openImage(path);
        } else {
            //original = IJ.getImage();

            //original = IJ.openImage("/home/tina/Desktop/IMG_20170530_102445.jpg");
            original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:47:20.png");
            //original = IJ.openImage("/home/tina/Desktop/Screenshot from 2017-06-13 14:48:32.png");
            //original = IJ.openImage("/home/tina/Desktop/2.jpg");
        }
        IJ.run(original, "Enhance Contrast...", "saturated=0.3 equalize");
        //IJ.run(original, "Subtract Background...", "rolling=50 light separate");
        ImagePlus bin = new ImagePlus("bin", original.getProcessor());
        //IJ.run(bin, "Enhance Contrast...", "saturated=1.2 equalize");
        IJ.run(bin, "Make Binary", "");
        IJ.run(bin, "Invert", "");
        ImageConverter ic = new ImageConverter(bin);
        ic.convertToRGB();

//        ImagePlus copie=new ImagePlus("copie",bin.getProcessor());
//        ImageConverter ic2 = new ImageConverter(copie);
//        ic2.convertToRGB();
        //ArrayList<double[]> scans = new ArrayList();
        HashMap<Integer, Line> segmentMap = new HashMap();
      //int [] columnsXvals = new int[(bin.getWidth()/scanColDist)+1];
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
                   // columnsXvals[col]=x;
                    //IJ.log("Column "+col+" Segment:"+noSegments+" höhe"+currSegmentHeight+" startY"+t);
                    original.setColor(Color.magenta);
                    original.getProcessor().drawLine(x, startY, x, startY + currSegmentHeight);
                }
            }
            col++;

            //IJ.log(Arrays.toString(p.getProfile()));
        }
        IJ.log(segmentMap.toString());
        //IJ.log(Arrays.toString(columnsXvals));
        int preavCol=0;
        
        for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Line> entry = it.next();       
            Line lineSeg = entry.getValue();
              preavCol = lineSeg.x1; 
            //IJ.log(entry.getKey() + "");
            if (lineSeg.y2 > (lineSeg.y1) + maxBoxHeight) { //entferne zu hohe linien  
                original.setColor(Color.gray);
                original.getProcessor().draw(lineSeg);
                it.remove();
            }
//            else if(preavCol == (lineSeg.x1)-1){// segment in Zeile davor && dannach? 
//                original.setColor(Color.yellow);
//                original.getProcessor().draw(lineSeg);
//                it.remove();
//            }
//            
        }

        IJ.log(segmentMap.toString());

        //key = segmentNumber
        for (Map.Entry<Integer, Line> segA : segmentMap.entrySet()) {
            for (Map.Entry<Integer, Line> segB : segmentMap.entrySet()) {
              //for (Iterator<Map.Entry<Integer, Line>> it = segmentMap.entrySet().iterator(); it.hasNext();) {
                Line lA = segA.getValue();
                Line lB = segB.getValue();
                if (segA.getKey() != segB.getKey() && lA.x1 != lB.x1 ) { //Ungleiches Segment && UNgleiche Spalte?
                    //  if(lB.y1 >= lA.y1+minBoxWidth && lB.y1 <= lA.y1+minBoxWidth){ //horizontale min & max distance?
                    if (lB.x1 >= lA.x1 + minBoxWidth && lB.x1 <= lA.x1 + maxBoxWidth) { //horizontale min & max distance?

                        //sind segmente verbundne?
                        int scanlineOffset = scanColDist / 2;
                        //oben
                        boolean topConnected = false;
                        Line topHline = new Line(lA.x1, lA.y1, lB.x1, lB.y1);
                        bin.setRoi( new Line(lA.x1, lA.y1+scanlineOffset, lB.x1, lB.y1+scanlineOffset));
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
                        bin.setRoi(new Line(lA.x2, lA.y2-scanlineOffset, lB.x2, lB.y2-scanlineOffset));
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

                        if (topConnected && bottomConnected ){//&& !alreadyInBox(lA, lB)) {

                            boundingBoxes.add(new Roi(lA.x1, lA.y1, topHline.getLength(), lA.getLength()));
                            original.setColor(Color.green);
                            // original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 20));
                            // original.getProcessor().drawString(segA.getKey() + "," + segB.getKey(), lA.x1, lB.y1);
                            
                            
//                            original.getProcessor().draw(lA);
//                            original.getProcessor().draw(lB);
//
                            original.setColor(Color.red);
                            original.getProcessor().draw(topHline);

                            original.setColor(Color.blue);
                            original.getProcessor().draw(bottomHline);
                            
                        }

                    }
                }
            }
        }

        original.show();
        bin.show();
    }
    
            private boolean inBetween(Line left, Line right, Line between) {
//    private boolean alreadyInBox(int[] Xs, int[] Ys) { 

            if (between.x1>left.x1 && right.x1>between.x1)               
            {
                //IJ.log("already in List (Code)");
                return true;
            }else{    
                return false;
            }

    }
        private boolean alreadyInBox(int x, int y) {
//    private boolean alreadyInBox(int[] Xs, int[] Ys) {
        for (Roi r : boundingBoxes) {
            if (r.contains(x, y)) {
                //IJ.log("already in List (Code)");
                return true;
            };
        }
        return false;
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
