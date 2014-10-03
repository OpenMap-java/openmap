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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkRectangle.java,v $
// $RCSfile: LinkRectangle.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.util.Debug;

/**
 * Read and write the Link protocol for rectangles.
 */
public class LinkRectangle implements LinkGraphicConstants,
        LinkPropertiesConstants {

    /**
     * Create a lat/lon rectangle.
     * 
     * @param lt1 latitude of north edge, decimal degrees.
     * @param ln1 longitude of west edge, decimal degrees.
     * @param lt2 latitude of south edge, decimal degrees.
     * @param ln2 longitude of east edge, decimal degrees.
     * @param lType line type - see lineType.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt1, float ln1, float lt2, float ln2,
                             int lType, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        LinkRectangle.write(lt1, ln1, lt2, ln2, lType, -1, properties, dos);
    }

    /**
     * Create a lat/lon rectangle.
     * 
     * @param lt1 latitude of north edge, decimal degrees.
     * @param ln1 longitude of west edge, decimal degrees.
     * @param lt2 latitude of south edge, decimal degrees.
     * @param ln2 longitude of east edge, decimal degrees.
     * @param lType line type - see lineType.
     * @param nsegs number of segment points (only for LINETYPE_GREATCIRCLE or
     *        LINETYPE_RHUMB line types, and if &lt; 1, this value is generated
     *        internally)
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt1, float ln1, float lt2, float ln2,
                             int lType, int nsegs, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.RECTANGLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RECTANGLE);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(lType);
        dos.writeFloat(lt1);
        dos.writeFloat(ln1);
        dos.writeFloat(lt2);
        dos.writeFloat(ln2);

        dos.writeInt(nsegs);
        properties.write(dos);
    }

    /**
     * Construct an XY rectangle. It doesn't matter which corners of the
     * rectangle are used, as long as they are opposite from each other.
     * 
     * @param px1 x pixel position of the first corner relative to the window
     *        origin
     * @param py1 y pixel position of the first corner relative to the window
     *        origin
     * @param px2 x pixel position of the second corner relative to the window
     *        origin
     * @param py2 y pixel position of the second corner relative to the window
     *        origin
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int px1, int py1, int px2, int py2,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RECTANGLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RECTANGLE);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(px1);
        dos.writeInt(py1);
        dos.writeInt(px2);
        dos.writeInt(py2);
        properties.write(dos);
    }

    /**
     * Construct an XY rectangle relative to a lat/lon point
     * (RENDERTYPE_OFFSET). It doesn't matter which corners of the rectangle are
     * used, as long as they are opposite from each other.
     * 
     * @param lt1 latitude of the reference point, decimal degrees.
     * @param ln1 longitude of the reference point, decimal degrees.
     * @param px1 x pixel position of the first corner relative to the reference
     *        point
     * @param py1 y pixel position of the first corner relative to the reference
     *        point
     * @param px2 x pixel position of the second corner relative to the
     *        reference point
     * @param py2 y pixel position of the second corner relative to the
     *        reference point
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt1, float ln1, int px1, int py1, int px2,
                             int py2, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.RECTANGLE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RECTANGLE);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(lt1);
        dos.writeFloat(ln1);
        dos.writeInt(px1);
        dos.writeInt(py1);
        dos.writeInt(px2);
        dos.writeInt(py2);
        properties.write(dos);
    }

    /**
     * Write an OMRect to the link.
     */
    public static void write(OMRect rect, Link link, LinkProperties props)
            throws IOException {

        switch (rect.getRenderType()) {
        case OMRect.RENDERTYPE_LATLON:
            LinkRectangle.write((float) rect.getNorthLat(),
                    (float) rect.getWestLon(),
                    (float) rect.getSouthLat(),
                    (float) rect.getEastLon(),
                    rect.getLineType(),
                    props,
                    link.dos);
            break;
        case OMRect.RENDERTYPE_XY:
            LinkRectangle.write(rect.getLeft(),
                    rect.getTop(),
                    rect.getRight(),
                    rect.getBottom(),
                    props,
                    link.dos);
            break;
        case OMRect.RENDERTYPE_OFFSET:
            LinkRectangle.write((float) rect.getNorthLat(),
                    (float) rect.getWestLon(),
                    rect.getLeft(),
                    rect.getTop(),
                    rect.getRight(),
                    rect.getBottom(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkRectangle.write: rect rendertype unknown.");
        }
    }

    /**
     * Read the DataInputStream, and create an OMRect. Assumes that the
     * LinkRectangle header has been read from the link.
     * 
     * @param dis DataInputStream
     * @return OMRect
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMRect
     */
    public static OMRect read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the DataInputStream, and create an OMRect. Assumes that the
     * LinkRectangle header has been read from the link.
     * 
     * @param dis DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMRect being read.
     * @return OMRect
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMRect
     */
    public static OMRect read(DataInputStream dis,
                              LinkProperties propertiesBuffer)
            throws IOException {
        OMRect rect = null;
        int x1, y1, x2, y2;
        double lt1, ln1, lt2, ln2;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON:
            int lineType = dis.readByte();
            lt1 = dis.readFloat();
            ln1 = dis.readFloat();
            lt2 = dis.readFloat();
            ln2 = dis.readFloat();
            int nsegs = dis.readInt();

            rect = new OMRect(lt1, ln1, lt2, ln2, lineType, nsegs);
            break;
        case RENDERTYPE_XY:
            x1 = dis.readInt();
            y1 = dis.readInt();
            x2 = dis.readInt();
            y2 = dis.readInt();

            rect = new OMRect(x1, y1, x2, y2);
            break;
        case RENDERTYPE_OFFSET:
            lt1 = dis.readFloat();
            ln1 = dis.readFloat();

            x1 = dis.readInt();
            y1 = dis.readInt();
            x2 = dis.readInt();
            y2 = dis.readInt();

            rect = new OMRect(lt1, ln1, x1, y1, x2, y2);
            break;
        default:
        }

        if (rect != null) {
            LinkProperties.loadPropertiesIntoOMGraphic(dis,
                    rect,
                    propertiesBuffer);
        }

        return rect;
    }

}