// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkBoundingPoly.java,v $
// $RCSfile: LinkBoundingPoly.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * LinkBoundingPoly objects are used to describe simple polygons that
 * cover a certain area. If the area described is in coordinates, the
 * polygon should not be sued for areas covering a pole or straddling
 * the dateline. More than one LinkBoundingPolys should be used for
 * areas like that.
 */
public class LinkBoundingPoly {

    public float maxX;
    public float maxY;
    public float minX;
    public float minY;
    protected float[] points;

    /**
     * The constructor to use when reading the bounding polygon off an
     * input stream.
     * 
     * @param dis DataInputStream to read from.
     */
    public LinkBoundingPoly(DataInput dis) throws IOException {
        read(dis);
    }

    /**
     * The constructor to use to create a LinkBoundingPoly to write to
     * an output stream.
     * 
     * @param poly a series of alternating x, y points describing a
     *        polygon.
     */
    public LinkBoundingPoly(float[] poly) {
        points = poly;
    }

    /**
     * Create a LinkBoundingPoly out of minimum and max x, y, values.
     * 
     * @param minX minimum X value.
     * @param minY minimum Y value.
     * @param maxX maximum X value.
     * @param maxY maximum Y value.
     */
    public LinkBoundingPoly(float minX, float minY, float maxX, float maxY) {
        points = new float[10];

        Debug.message("link",
                "LinkBoundingPoly: Creating link bounding poly with " + minX
                        + ", " + minY + ", " + maxX + ", " + maxY);

        points[0] = minY;
        points[1] = minX;
        points[2] = maxY;
        points[3] = minX;
        points[4] = maxY;
        points[5] = maxX;
        points[6] = minY;
        points[7] = maxX;
        points[8] = minY;
        points[9] = minX;
    }

    /**
     * Write the polygon on the output stream. If the number of points
     * is an odd number, the last number will be left off.
     * 
     * @param dos the DataOutput to write to.
     */
    public void write(DataOutput dos) throws IOException {
        // round down to a multiple of two.
        int length = (points.length / 2) * 2;

        dos.writeInt(length);
        for (int i = 0; i < length; i++) {
            dos.writeFloat(points[i]);
        }
    }

    /**
     * Read the bounding polygon off the input stream.
     * 
     * @param dis DataInputStream to read from.
     */
    public void read(DataInput dis) throws IOException {

        Debug.message("link", "LinkBoundingPoly: read()");

        int polyLength = dis.readInt();
        points = new float[polyLength];
        float x, y;

        for (int i = 0; i < points.length; i += 2) {
            y = dis.readFloat();
            x = dis.readFloat();

            if (i == 0) {
                minX = x;
                minY = y;
                maxX = x;
                maxY = y;
            }

            points[i] = y;
            points[i + 1] = x;

            if (x < minX)
                minX = x;
            if (x > maxX)
                maxX = x;
            if (y < minY)
                minY = y;
            if (y > maxY)
                maxY = y;
        }
    }

    /**
     * Return the polygon points, as a series of alternating x and y
     * values.
     * 
     * @return float[] of alternating x, y, points.
     */
    public float[] getPoints() {
        return points;
    }

    /**
     * Convert the points to Lat/Lon points. There is no guarantee
     * that the points really translate into latitude and longitude
     * points.
     * 
     * @return LatLonPoints
     */
    public LatLonPoint[] getLatLonPoints() {
        LatLonPoint[] boundingPoly = new LatLonPoint[points.length / 2];
        float lat, lon;
        for (int i = 0; i < points.length; i += 2) {
            lat = points[i];
            lon = points[i + 1];
            boundingPoly[i] = new LatLonPoint.Double(lat, lon);
        }
        return boundingPoly;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("  LinkBoundingPoly has ")
                .append(points.length / 2).append(" points.");
        for (int i = 0; i < points.length; i += 2) {
            s.append("\n    |Lat = ").append(points[i])
                    .append(", Lon = ").append(points[i + 1]).append("|");
        }
        return s.toString();
    }
}