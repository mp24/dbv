package dvbprojekt;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Pack200;
import javafx.util.Pair;

public class QR_Plugin implements PlugIn {

    String path;
    ImagePlus original;
    
    int minBoxHeight = 100;
    int maxBoxHeight = 500;
    int minBoxWidth = minBoxHeight/2;
    int maxBoxWidth = maxBoxHeight;
    
    int scanColDist = 9;

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
        IJ.run(original, "Make Binary", "");
        IJ.run(original, "Invert", "");

        ImageConverter ic = new ImageConverter(original);
        ic.convertToRGB();

        ArrayList<double[]> scans = new ArrayList();
        HashMap<Integer, ColSegment> map = new HashMap();
        int col = 0;
        int noSegments = 0;
        for (int x = 0; x < (original.getWidth()); x = x + scanColDist) {
            original.setRoi(new Line(x, 0, x, original.getHeight()));
            ProfilePlot p = new ProfilePlot(original);
            double[] curProfile =p.getProfile();
            scans.add(curProfile);

            //aktuelles Zeilenprofil untersuchen
            int currSegmentHeight = 0;
            int startY = -1;
            for (int y = 0; y < curProfile.length; y++) {
                if (scans.get(col)[y] == 255) {
                    original.setColor(Color.magenta);
                    original.getProcessor().drawDot(x, y);
                    if (currSegmentHeight == 0) {
                        startY = y;
                        noSegments++;
                    }
                    currSegmentHeight++;
                } else {
                    currSegmentHeight = 0;
//                     original.setColor(Color.green);
//                    original.getProcessor().drawDot(x, y);
                }
                if (currSegmentHeight > minBoxHeight) {
                    map.put(noSegments, new ColSegment(x, startY, currSegmentHeight));
                    IJ.log("Column "+col+" Segment:"+noSegments+" höhe"+currSegmentHeight+" startY"+y);
                   // original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 20));
                   // original.getProcessor().drawString( "" +noSegments, x, y);                   
                }           
            }
            col++;
            //IJ.log(Arrays.toString(p.getProfile()));
        }
        IJ.log(map.toString());
        
//        for(Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry<String, String> entry = it.next();
//            if(entry.getKey().equals("test")) {
//              it.remove();
//        }

        //key = segmentNumber
        for(Map.Entry<Integer, ColSegment> segA : map.entrySet()){
            for(Map.Entry<Integer, ColSegment> segB : map.entrySet()){
//        for (int segA = 1; segA <= map.size(); segA++) {
//            for (int segB = 1; segB <= map.size(); segB++) {
//                if(map.get(segA).xstart != map.get(segB).xstart){ //UNgleiche Zeile?
//                    IJ.log(map.get(segA).compareTo(map.get(segB)) + "");
//                    if (map.get(segA).compareTo(map.get(segB)) == 0) {
//                        original.setColor(Color.green);
//                        original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 20));
//                        original.getProcessor().drawString(segA + "," + segB, map.get(segA).xstart, map.get(segA).ystart);
//                    }
//                }
                ColSegment a = segA.getValue();
                ColSegment b = segB.getValue();
                if(segA.getKey()!=segB.getKey() && a.xstart != b.xstart ){ //Ungleiches Segment && UNgleiche Spalte?
                    if(b.ystart >= a.ystart+minBoxWidth && b.ystart <= a.ystart+minBoxWidth){ //horizontale min & max distance?
                        original.setColor(Color.green);
                        original.getProcessor().setFont(new Font("SansSerif", Font.PLAIN, 20));
                        original.getProcessor().drawString(segA.getKey() + "," + segB.getKey(), a.xstart, b.ystart);
                    }
                       
                    
                }
                                                         
            }
        }

        // for(double[])
        original.show();
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
        Class<?> clazz = QR_Plugin.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        IJ.runPlugIn(clazz.getName(), "");
    }

}
