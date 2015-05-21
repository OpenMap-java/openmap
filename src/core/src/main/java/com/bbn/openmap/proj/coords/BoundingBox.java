package com.bbn.openmap.proj.coords;

/**
 * A Bounding Box.
 */
public class BoundingBox {

    private double minx;

    private double miny;

    private double maxx;

    private double maxy;

    public BoundingBox(double minx, double miny, double maxx, double maxy) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public double getMinX() {
        return minx;
    }

    public double getMinY() {
        return miny;
    }

    public double getMaxX() {
        return maxx;
    }

    public double getMaxY() {
        return maxy;
    }

}
