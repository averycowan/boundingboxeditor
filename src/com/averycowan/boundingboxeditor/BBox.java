package com.averycowan.boundingboxeditor;

public class BBox {
    public final int xmin;
    public final int ymin;
    public final int xmax;
    public final int ymax;

    public final String label;

    public BBox(int xmin, int ymin, int xmax, int ymax, String label) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.label = label;
    }

    public String toString() {
        return xmin + "," + ymin + "," + xmax + "," + ymax + "," + label;
    }

}
