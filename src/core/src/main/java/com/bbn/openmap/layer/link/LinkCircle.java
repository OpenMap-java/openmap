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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkCircle.java,v $
// $RCSfile: LinkCircle.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Reading and writing a Link protocol version of a circle.
 */
public class LinkCircle implements LinkGraphicConstants,
        LinkPropertiesConstants {

    /**
     * Write a circle with lat/lon placement.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int w, int h,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        LinkCircle.write(latPoint, lonPoint, 0, 0, w, h, properties, dos);
    }

    /**
     * Write a circle with x/y placement.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.CIRCLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_CIRCLE);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(w);
        dos.writeInt(h);
        properties.write(dos);
    }

    /**
     * Writing a circle at a x, y, offset to a Lat/Lon location.
     * 
     * @param latPoint latitude of center of circle/ellipse.
     * @param lonPoint longitude of center of circle/ellipse.
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of circle/ellipse, pixels.
     * @param h vertical diameter of circle/ellipse, pixels.
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int offset_x1,
                             int offset_y1, int w, int h,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.CIRCLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_CIRCLE);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);
        properties.write(dos);
    }

    /**
     * Write a circle with a certain radius at a Lat/Lon location. Assumes the
     * radius is in decimal degrees.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        LinkCircle.write(latPoint, lonPoint, radius, -1, -1, properties, dos);
    }

    /**
     * Write a circle with a certain radius at a Lat/Lon location, and allows
     * you to specify units of the radius.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - KM, MILES, NMILES. If
     *        &lt; 0, assume decimal degrees.
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             int units, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        LinkCircle.write(latPoint, lonPoint, radius, units, -1, properties, dos);
    }

    /**
     * Write a circle with a certain radius at a Lat/Lon location, and allows
     * you to specify units of the radius, as well as the number of vertices to
     * use to approximate the circle.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - OMCircle.KM,
     *        OMCircle.MILES, OMCircle.NMILES. If &lt; 0, assume decimal
     *        degrees.
     * @param nverts number of vertices for the poly-circle (if &lt; 3, value is
     *        generated internally).
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             int units, int nverts, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        // Write this out...
        dos.write(Link.CIRCLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_CIRCLE);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeFloat(radius);
        dos.writeByte(units);
        dos.writeInt(nverts);
        properties.write(dos);
    }

    public static void write(OMCircle circle, Link link, LinkProperties props)
            throws IOException {

        LatLonPoint llp;
        switch (circle.getRenderType()) {
        case OMCircle.RENDERTYPE_LATLON:
            llp = circle.getLatLon();
            LinkCircle.write((float) llp.getLatitude(),
                    (float) llp.getLongitude(),
                    (float) circle.getRadius(),
                    props,
                    link.dos);
            break;
        case OMCircle.RENDERTYPE_XY:
            LinkCircle.write(circle.getX(),
                    circle.getY(),
                    circle.getWidth(),
                    circle.getHeight(),
                    props,
                    link.dos);
            break;
        case OMCircle.RENDERTYPE_OFFSET:
            llp = circle.getLatLon();
            LinkCircle.write(llp.getLatitude(),
                    llp.getLongitude(),
                    circle.getOffX(),
                    circle.getOffY(),
                    circle.getWidth(),
                    circle.getHeight(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkCircle.write: circle rendertype unknown.");
        }
    }

    /**
     * Read the circle protocol off the data input, and return an OMCircle.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @return OMCircle
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMCircle
     */
    public static OMCircle read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the circle protocol off the data input, and return an OMCircle.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMCircle
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMCircle
     */
    public static OMCircle read(DataInputStream dis,
                                LinkProperties propertiesBuffer)
            throws IOException {

        OMCircle circle = null;
        float lat, lon, radius;
        int x, y, w, h;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON:
            lat = dis.readFloat();
            lon = dis.readFloat();
            radius = dis.readFloat();
            int units = dis.readByte();
            int nverts = dis.readInt();

            Length unit = Length.DECIMAL_DEGREE;

            switch (units) {
            case 0:
                unit = Length.KM;
                break;
            case 1:
                unit = Length.MILE;
                break;
            case 2:
                unit = Length.NM;
                break;
            default:
            }

            circle = new OMCircle(new LatLonPoint.Double(lat, lon), radius, unit, nverts);
            break;
        case RENDERTYPE_XY:
            x = dis.readInt();
            y = dis.readInt();
            w = dis.readInt();
            h = dis.readInt();

            circle = new OMCircle(x, y, w, h);
            break;
        case RENDERTYPE_OFFSET:
            lat = dis.readFloat();
            lon = dis.readFloat();
            x = dis.readInt();
            y = dis.readInt();
            w = dis.readInt();
            h = dis.readInt();

            circle = new OMCircle(lat, lon, x, y, w, h);
            break;
        default:
        }

        if (circle != null) {
            LinkProperties.loadPropertiesIntoOMGraphic(dis, circle, propertiesBuffer);
        }

        return circle;
    }

}