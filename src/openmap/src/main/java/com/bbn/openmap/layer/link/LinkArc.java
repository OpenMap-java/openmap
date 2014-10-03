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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkArc.java,v $
// $RCSfile: LinkArc.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMArc;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Reading and writing a Link protocol version of a circle.
 */
public class LinkArc implements LinkGraphicConstants, LinkPropertiesConstants {

    /**
     * Write an arc with lat/lon placement.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param w horizontal diameter of arc, pixels
     * @param h vertical diameter of arc, pixels
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int w, int h,
                             float s, float e, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        LinkArc.write(latPoint, lonPoint, 0, 0, w, h, s, e, properties, dos);
    }

    /**
     * Write an arc with x/y placement.
     * 
     * @param x1 window position of center point from left of window,
     *        in pixels
     * @param y1 window position of center point from top of window,
     *        in pixels
     * @param w horizontal diameter of arc, pixels
     * @param h vertical diameter of arc, pixels
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h, float s, float e,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.ARC_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ARC);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeFloat(s);
        dos.writeFloat(e);
        properties.write(dos);
    }

    /**
     * Writing an arc at a x, y, offset to a Lat/Lon location.
     * 
     * @param latPoint latitude of center of arc.
     * @param lonPoint longitude of center of arc.
     * @param offset_x1 # pixels to the right the center will be moved
     *        from lonPoint.
     * @param offset_y1 # pixels down that the center will be moved
     *        from latPoint.
     * @param w horizontal diameter of arc, pixels.
     * @param h vertical diameter of arc, pixels.
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int offset_x1,
                             int offset_y1, int w, int h, float s, float e,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.ARC_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ARC);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeFloat(s);
        dos.writeFloat(e);
        properties.write(dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location.
     * Assumes the radius is in decimal degrees.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             float s, float e, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        LinkArc.write(latPoint, lonPoint, radius, -1, -1, s, e, properties, dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location, and
     * allows you to specify units of the radius.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - KM, MILES,
     *        NMILES. If &lt; 0, assume decimal degrees.
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             int units, float s, float e,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                radius,
                units,
                -1,
                s,
                e,
                properties,
                dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location, and
     * allows you to specify units of the radius, as well as the
     * number of vertices to use to approximate the arc.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - OMArc.KM,
     *        OMArc.MILES, OMArc.NMILES. If &lt; 0, assume decimal
     *        degrees.
     * @param nverts number of vertices for the poly-arc (if &lt; 3,
     *        value is generated internally).
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, float radius,
                             int units, int nverts, float s, float e,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        // Write this out...
        dos.write(Link.ARC_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ARC);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeFloat(radius);
        dos.writeByte(units);
        dos.writeInt(nverts);
        dos.writeFloat(s);
        dos.writeFloat(e);
        properties.write(dos);
    }

    public static void write(OMArc arc, Link link, LinkProperties props)
            throws IOException {

        LatLonPoint llp;
        switch (arc.getRenderType()) {
        case OMArc.RENDERTYPE_LATLON:
            llp = arc.getLatLon();
            LinkArc.write((float) llp.getLatitude(),
                    (float) llp.getLongitude(),
                    (float) arc.getRadius(),
                    (float) arc.getStartAngle(),
                    (float) arc.getExtentAngle(),
                    props,
                    link.dos);
            break;
        case OMArc.RENDERTYPE_XY:
            LinkArc.write(arc.getX(),
                    arc.getY(),
                    arc.getWidth(),
                    arc.getHeight(),
                    (float) arc.getStartAngle(),
                    (float) arc.getExtentAngle(),
                    props,
                    link.dos);
            break;
        case OMArc.RENDERTYPE_OFFSET:
            llp = arc.getLatLon();
            LinkArc.write((float) llp.getLatitude(),
                    (float) llp.getLongitude(),
                    arc.getOffX(),
                    arc.getOffY(),
                    arc.getWidth(),
                    arc.getHeight(),
                    (float) arc.getStartAngle(),
                    (float) arc.getExtentAngle(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkArc.write: arc rendertype unknown.");
        }
    }

    /**
     * Read the arc protocol off the data input, and return an OMArc.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @return OMArc
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMLine
     */
    public static OMArc read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }
    
    /**
     * Read the arc protocol off the data input, and return an OMArc.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMArc being read.
     * @return OMArc
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMArc
     */
    public static OMArc read(DataInputStream dis, LinkProperties propertiesBuffer) throws IOException {

        OMArc arc = null;
        float lat, lon, radius, start, extent;
        int x, y, w, h;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON:
            lat = dis.readFloat();
            lon = dis.readFloat();
            radius = dis.readFloat();
            start = dis.readFloat();
            extent = dis.readFloat();
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

            arc = new OMArc(new LatLonPoint.Double(lat, lon), radius, unit, nverts, start, extent);
            break;
        case RENDERTYPE_XY:
            x = dis.readInt();
            y = dis.readInt();
            w = dis.readInt();
            h = dis.readInt();
            start = dis.readFloat();
            extent = dis.readFloat();

            arc = new OMArc(x, y, w, h, start, extent);
            break;
        case RENDERTYPE_OFFSET:
            lat = dis.readFloat();
            lon = dis.readFloat();
            x = dis.readInt();
            y = dis.readInt();
            w = dis.readInt();
            h = dis.readInt();
            start = dis.readFloat();
            extent = dis.readFloat();
            arc = new OMArc(lat, lon, x, y, w, h, start, extent);
            break;
        default:
        }

        if (arc != null) {
            LinkProperties.loadPropertiesIntoOMGraphic(dis,
                    arc,
                    propertiesBuffer);
        }

        return arc;
    }

}