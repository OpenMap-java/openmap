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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkMapRequest.java,v $
// $RCSfile: LinkMapRequest.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 18:08:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * A LinkMapRequest presents a view onto a map. This view can be used
 * to create a list of graphics that should be added to the map, or to
 * query a spatial database engine for other graphics.
 * <P>
 * 
 * This object can be used to write the query to the link, and to read
 * the query from the link.
 */
public class LinkMapRequest {

    /** The latitude/longitude of the center of the map. */
    protected LatLonPoint center;
    /** The scale of the map. The value is interpreted as 1:<scale> */
    protected float scale;
    /**
     * A series of LinkBoundingPoly objects making up polygons of
     * interest. NOTE: These polygons do not cover the poles, or cross
     * the dateline. For areas like that that need to be described,,
     * several LinkBoundingPolys must be used.
     */
    protected LinkBoundingPoly[] boundingPolys;
    /** Height of the map, in pixels. */
    protected int height;
    /** Width of the map, in pixels. */
    protected int width;
    /** Key value pairs of properties sent along with the map. */
    protected LinkProperties properties;
    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;

    /** The terminator of the graphics section when receiving graphics. */
    String linkStatus = Link.END_TOTAL;

    /**
     * The constructor to use when reading the LinkMapRequest off the
     * link.
     * 
     * @param link the Link to read from.
     * @throws IOException
     */
    public LinkMapRequest(Link link) throws IOException {
        linkStatus = read(link);
    }

    /**
     * Return the header for this object.
     * 
     * @return the String representing the header.
     */
    public String getType() {
        return Link.MAP_REQUEST_HEADER;
    }

    /**
     * After reading the gesture response, this returns the section
     * ending string terminating the gesture section, either
     * Link.END_TOTAL or Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION.
     */
    public String getLinkStatus() {
        return linkStatus;
    }

    /**
     * Write the request to the link.
     * 
     * @param centerLat center latitude, in decimal degrees.
     * @param centerLon center longitude, in decimal degrees.
     * @param scale scale of map.
     * @param height height of map in pixels.
     * @param width width of map in pixels.
     * @param boundingPolys An array of polygons of interest. Each
     *        bounding polygon is a series of floats, alternating
     *        latitude and longitude values.
     * @param props Properties object containing key-value attributes.
     * @param link link to write to.
     */
    public static void write(float centerLat, float centerLon, float scale,
                             int height, int width,
                             LinkBoundingPoly[] boundingPolys,
                             LinkProperties props, Link link)
            throws IOException {
        int i;

        link.start(Link.MAP_REQUEST_HEADER);
        link.dos.writeFloat(version);
        link.dos.writeFloat(centerLat);
        link.dos.writeFloat(centerLon);
        link.dos.writeFloat(scale);
        link.dos.writeInt(height);
        link.dos.writeInt(width);
        link.dos.writeInt(boundingPolys.length);
        for (i = 0; i < boundingPolys.length; i++) {
            boundingPolys[i].write(link.dos);
        }

        props.write(link);

        link.end(Link.END_TOTAL);

        if (Debug.debugging("link")) {
            System.out.println("LinkMapRequest wrote:");
            System.out.println(" version = " + version);
            System.out.println(" lat = " + centerLat);
            System.out.println(" lon = " + centerLon);
            System.out.println(" scale = " + scale);
            System.out.println(" height = " + height);
            System.out.println(" width = " + width);
            System.out.println(" bounding polys:");
            for (i = 0; i < boundingPolys.length; i++) {
                System.out.println(boundingPolys[i]);
            }
            System.out.println(" Args:");
            System.out.println(props);
        }
    }

    /**
     * Read the link to create the request object. Assumes the header
     * has already been read.
     * 
     * @param link the link to read.
     * @throws IOException.
     */
    public String read(Link link) throws IOException {

        Debug.message("link", "LinkMapRequest: read()");

        float ver = link.dis.readFloat();

        if (ver != version) {
            if (ver == .1) {// Big difference....
                throw new IOException("LinkMapRequest: Versions do not match! DANGER!");
            } else {
                Debug.message("link", "LinkMapRequest: Versions do not match");
            }
        }

        float lat = link.dis.readFloat();
        float lon = link.dis.readFloat();

        center = new LatLonPoint.Float(lat, lon);

        scale = link.dis.readFloat();
        height = link.dis.readInt();
        width = link.dis.readInt();
        int length = link.dis.readInt();

        boundingPolys = new LinkBoundingPoly[length];
        for (int i = 0; i < boundingPolys.length; i++) {
            boundingPolys[i] = new LinkBoundingPoly(link.dis);
        }

        properties = new LinkProperties(link);

        return link.readDelimiter(false);
    }

    /** Get the center of the map. */
    public LatLonPoint getCenter() {
        return center;
    }

    /** Get the scale of the map. */
    public float getScale() {
        return scale;
    }

    /** Get the height of map in pixels. */
    public int getHeight() {
        return height;
    }

    /** Get the width of the map in pixels. */
    public int getWidth() {
        return width;
    }

    /** Get an array of bounding polygons. */
    public LinkBoundingPoly[] getBoundingPolys() {
        return boundingPolys;
    }

    /** Get the key-value arguments for the request. */
    public LinkProperties getProperties() {
        return properties;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("LinkMapRequest:\n");
        s.append("LinkMapRequest wrote:\n");
        s.append(" version = ").append(version).append("\n");
        s.append(" center = ").append(center).append("\n");
        s.append(" scale = ").append(scale).append("\n");
        s.append(" height = ").append(height).append("\n");
        s.append(" width = ").append(width).append("\n");
        s.append(" ").append(boundingPolys.length).append(" bounding polys:");
        int i;
        for (i = 0; i < boundingPolys.length; i++) {
            s.append("\n").append(boundingPolys[i]);
        }
        return s.toString();
    }
}