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
           int col =0;
            for (int x = 0; x < (original.getWidth()); x= x+scanColDist) {
		original.setRoi(new Line(x,0,x,original.getHeight()));
		ProfilePlot p = new ProfilePlot(original);
               scans.add(p.getProfile());

                for (int y = 0; y < p.getProfile().length; y++) {
                    if(scans.get(col)[y]==255){
                        original.setColor(Color.magenta);
                        original.getProcessor().drawDot(x, y);
                    }
                    
                }
                col++;
                //IJ.log(Arrays.toString(p.getProfile()));
	}
            
            
            
           //HashMap<Pair<Integer,Integer>,Integer> map = new HashMap();
           HashMap<Integer,ColSegment> map = new HashMap();
            int minSize=0;
            int maxSize=0;
            //for(double[] d : scans){
            for(int c=0; c<scans.size(); c++){
                int noSegments=0;
                int currSegmentHeight=0;
                int startX=-1;
                for(int i =0; i<scans.get(c).length;i++){                   
                    if(scans.get(c)[i]==255){
                        if(currSegmentHeight==0){
                            startX=i;
                            noSegments++;
                        }
                        currSegmentHeight++;
                    }else{
                        currSegmentHeight=0;
                    }
                    
                    if(currSegmentHeight>minBoxHeight){
                        //map.put(new Pair(startX, (c*scanColDist)), currSegmentHeight);
                        map.put(noSegments,new ColSegment(startX, (c*scanColDist), currSegmentHeight));
                        //IJ.log("Line "+c+" Segment:"+noSegments+" höhe"+currSegmentHeight+" startX"+startX);
                    }
                    
                }
            }
            IJ.log(map.toString());
            
            
            for(int m=1; m<=map.size();m++){
                for(int n=2; n<=map.size();n++){
                    IJ.log(map.get(m).compareTo(map.get(n))+"");   
                    if(map.get(m).compareTo(map.get(n))==0){
                          original.getProcessor().setFont(new Font("SansSerif",Font.PLAIN,20));
                        original.getProcessor().drawString(m+","+n, map.get(m).xstart, map.get(m).ystart);
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
