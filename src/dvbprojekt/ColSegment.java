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
        if (this.ystart != o.ystart) { //nicht die selbe spalte
            if (this.xstart <= o.xstart * 1.2 && this.xstart >= o.xstart * 0.7) {
                if (this.height <= o.height * 1.2 && this.height >= o.height * 0.7) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }

        if (this.ystart == o.ystart) {
            return 1;
        } else {
            return -1;
        }
    }
}
