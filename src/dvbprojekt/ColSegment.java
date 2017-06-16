/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dvbprojekt;

/**
 *
 * @author tina
 */
public class ColSegment implements Comparable<ColSegment> {

    int xstart;
    int ystart;
    int height;

    public ColSegment(int x, int y, int l) {
        this.xstart = x;
        this.ystart = y;
        this.height = l;
    }

    @Override
    public int compareTo(ColSegment o) {
        if (this.height <= o.height * 1.2 && this.height >= o.height * 0.7) {
            return 0;
        } else {
            return -1;
        }

    }

}
