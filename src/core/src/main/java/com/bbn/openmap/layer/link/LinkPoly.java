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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkPoly.java,v $
// $RCSfile: LinkPoly.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.util.Debug;

/**
 * Read and write a Link protocol polyline/polygon.
 */
public class LinkPoly implements LinkGraphicConstants, LinkPropertiesConstants {

    /**
     * Write a poly, with an array of alternating lat/lon points. Lat/lons in
     * decimal degrees.
     * 
     * @param llPoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(double[] llPoints, int units, int lType,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        LinkPoly.write(llPoints, units, lType, -1, properties, dos);
    }

    /**
     * Write a poly.
     * 
     * @param llpoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param nsegs number of segments to use to approximate curved poly lines..
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(double[] llpoints, int units, int lType, int nsegs,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(lType);
        dos.writeInt(llpoints.length);

        for (int i = 0; i < llpoints.length; i++) {
            dos.writeFloat((float) llpoints[i]);
        }

        dos.writeByte(units);
        dos.writeInt(nsegs);

        properties.write(dos);
    }

    /**
     * Write a poly.
     * 
     * @param latpoints latitude points of poly.
     * @param lonpoints longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param nsegs number of segments to use to approximate curved poly lines..
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float[] latpoints, float[] lonpoints, int units,
                             int lType, int nsegs, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(lType);

        int length = latpoints.length;
        // We only want to write out the points that have equal
        // pairings.
        if (lonpoints.length < latpoints.length) {
            length = lonpoints.length;
        }

        dos.writeInt(length);

        for (int i = 0; i < length; i++) {
            dos.writeFloat(latpoints[i]);
            dos.writeFloat(lonpoints[i]);
        }

        dos.writeByte(units);
        dos.writeInt(nsegs);

        properties.write(dos);
    }

    /**
     * Write a poly.
     * 
     * @param xypoints alternating x and y pixel locations of poly.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int[] xypoints, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(xypoints.length);

        for (int i = 0; i < xypoints.length; i++) {
            dos.writeInt(xypoints[i]);
        }
        properties.write(dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param xpoints horizontal pixel locations of poly.
     * @param ypoints vertical pixel locations of poly.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int[] xpoints, int[] ypoints,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_XY);
        int numPoints = xpoints.length + ypoints.length;
        dos.writeInt(numPoints);

        for (int i = 0; i < numPoints / 2; i++) {
            dos.writeInt(xpoints[i]);
            dos.writeInt(ypoints[i]);
        }

        properties.write(dos);
    }

    /**
     * Write a poly.
     * 
     * @param latPoint the latitude anchor point of the poly.
     * @param lonPoint the longitude anchor point of the poly.
     * @param xypoints alternating x and y offset polygon points.
     * @param cMode Coordinate Mode (Origin or Previous) that indicate whether
     *        the x and y points are relative to the first point, or to the
     *        previous point. .
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int[] xypoints,
                             int cMode, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeInt(xypoints.length);

        for (int i = 0; i < xypoints.length; i++) {
            dos.writeInt(xypoints[i]);
        }

        dos.writeByte(cMode);
        properties.write(dos);
    }

    /**
     * Write a poly.
     * 
     * @param latPoint the latitude anchor point of the poly.
     * @param lonPoint the longitude anchor point of the poly.
     * @param xpoints horizontal pixel offset polygon points.
     * @param ypoints vertical pixel offset polygon points.
     * @param cMode Coordinate Mode (Origin or Previous) that indicate whether
     *        the x and y points are relative to the first point, or to the
     *        previous point. .
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint, int[] xpoints,
                             int[] ypoints, int cMode,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POLY_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POLY);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        int numPoints = xpoints.length + ypoints.length;
        dos.writeInt(numPoints);

        for (int i = 0; i < numPoints / 2; i++) {
            dos.writeInt(xpoints[i]);
            dos.writeInt(ypoints[i]);
        }
        dos.writeByte(cMode);
        properties.write(dos);
    }

    /**
     * Write a poly to the link.
     */
    public static void write(OMPoly poly, Link link, LinkProperties props)
            throws IOException {

        switch (poly.getRenderType()) {
        case OMPoly.RENDERTYPE_LATLON:
            write(poly.getLatLonArray(),
                    OMPoly.RADIANS,
                    poly.getLineType(),
                    poly.getNumSegs(),
                    props,
                    link.dos);
            break;
        case OMPoly.RENDERTYPE_XY:
            write(poly.getXs(), poly.getYs(), props, link.dos);
            break;
        case OMPoly.RENDERTYPE_OFFSET:
            write((float)poly.getLat(),
                    (float)poly.getLon(),
                    poly.getXs(),
                    poly.getYs(),
                    poly.getCoordMode(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkPoly.write: poly rendertype unknown.");
        }
    }

    /**
     * Read the DataInputStream to create a OMPoly. Assumes the LinkPoly header
     * has already been read.
     * 
     * @param dis DataInputStream
     * @return OMPoly
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMPoly
     */
    public static OMPoly read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the DataInputStream to create a OMPoly. Assumes the LinkPoly header
     * has already been read.
     * 
     * @param dis DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMPoly
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMPoly
     */
    public static OMPoly read(DataInputStream dis,
                              LinkProperties propertiesBuffer)
            throws IOException {

        OMPoly poly = null;
        int numPoints;
        int[] xpoints, ypoints;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON:
            int lineType = dis.readByte();
            numPoints = dis.readInt();

            double[] llpoints = new double[numPoints];
            for (int i = 0; i < numPoints; i++) {
                llpoints[i] = dis.readFloat();
            }
            int units = dis.readByte();
            int nsegs = dis.readInt();

            if (Debug.debugging("linkdetail")) {
                System.out.println("  Lat/Lon LinkPoly:");
                System.out.println("  linetype = " + lineType);
                System.out.println("  number of points = " + numPoints / 2);
                // for (int i = 0; i < numPoints; i+=2) {
                // System.out.println(" Lat = " + llpoints[i] +
                // ", Lon = " + llpoints[i+1]);
                // }
                System.out.println("  units = " + units);
                System.out.println("  nsegs = " + nsegs);
            }

            poly = new OMPoly(llpoints, units, lineType, nsegs);
            break;
        case RENDERTYPE_XY:
            numPoints = dis.readInt();
            xpoints = new int[numPoints / 2];
            ypoints = new int[numPoints / 2];

            for (int i = 0; i < numPoints / 2; i += 1) {
                xpoints[i] = dis.readInt();
                ypoints[i] = dis.readInt();
            }

            if (Debug.debugging("linkdetail")) {
                System.out.println("  X/Y LinkPoly:");
                System.out.println("  number of points = " + numPoints / 2);
                // for (i = 0; i < numPoints; i++) {
                // System.out.println(" X = " + xpoints[i] +
                // ", Y = " + ypoints[i]);
                // }
            }

            poly = new OMPoly(xpoints, ypoints);
            break;
        case RENDERTYPE_OFFSET:
            float lat_1 = dis.readFloat();
            float lon_1 = dis.readFloat();
            numPoints = dis.readInt();

            xpoints = new int[numPoints / 2];
            ypoints = new int[numPoints / 2];

            for (int i = 0; i < numPoints / 2; i += 1) {
                xpoints[i] = dis.readInt();
                ypoints[i] = dis.readInt();
            }
            int cMode = dis.readByte();

            if (Debug.debugging("linkdetail")) {
                System.out.println("  Offset LinkPoly:");
                System.out.println("  lat = " + lat_1);
                System.out.println("  lon = " + lon_1);
                System.out.println("  number of points = " + numPoints / 2);
                // for (i = 0; i < numPoints; i+=2) {
                // System.out.println(" Lat = " + llpoints[i] +
                // ", Lon = " + llpoints[i+1]);
                // }
                System.out.println("  cMode = " + cMode);
            }

            poly = new OMPoly(lat_1, lon_1, xpoints, ypoints, cMode);
            break;
        default:
        }

        if (poly != null) {
            LinkProperties.loadPropertiesIntoOMGraphic(dis, poly, propertiesBuffer);
        }

        return poly;
    }
}