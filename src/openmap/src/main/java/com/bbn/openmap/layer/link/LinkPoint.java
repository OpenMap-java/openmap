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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkPoint.java,v $
// $RCSfile: LinkPoint.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Read and write the Link protocol for points. The protocol for the point has
 * location information for the point, as well as a radius value indicating the
 * size associated with this point. OMPoints also support whether the point
 * should be rendered as a rectangle or an oval. That choice is specified as a
 * property for the LinkPoint, along with any other rendering or attribute
 * information that should be applied to the point.
 */
public class LinkPoint implements LinkGraphicConstants, LinkPropertiesConstants {

    /**
     * The property for the LinkPoint to specify if the OMPoint should be
     * rendered as an oval. The value should be true or false, depending if the
     * point should be rendered as an oval.
     */
    public final static String LPC_POINT_OVAL = "oval";

    /**
     * Create a lat/lon point.
     * 
     * @param lt latitude of north edge, decimal degrees.
     * @param ln longitude of west edge, decimal degrees.
     * @param radius pixel radius of the point.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int radius,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POINT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POINT);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(radius);
        properties.write(dos);
    }

    /**
     * Construct an XY point at a screen location..
     * 
     * @param px1 x pixel position of the first corner relative to the window
     *        origin
     * @param py1 y pixel position of the first corner relative to the window
     *        origin
     * @param radius pixel radius of the point.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int px1, int py1, int radius,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POINT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POINT);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(px1);
        dos.writeInt(py1);
        dos.writeInt(radius);
        properties.write(dos);
    }

    /**
     * Construct an XY point relative to a lat/lon point (RENDERTYPE_OFFSET). It
     * doesn't matter which corners of the point are used, as long as they are
     * opposite from each other.
     * 
     * @param lt latitude of the reference point, decimal degrees.
     * @param ln longitude of the reference point, decimal degrees.
     * @param px1 x pixel position of the first corner relative to the reference
     *        point
     * @param py1 y pixel position of the first corner relative to the reference
     *        point
     * @param radius a pixel radius of the point.
     * @param properties description of drawing attributes.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int px1, int py1, int radius,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.POINT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_POINT);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(px1);
        dos.writeInt(py1);
        dos.writeInt(radius);
        properties.write(dos);
    }

    /**
     * Write an OMPoint to the Link.
     */
    public static void write(OMPoint point, Link link, LinkProperties props)
            throws IOException {

        props.setProperty(LinkPoint.LPC_POINT_OVAL, point.isOval() ? "true"
                : "false");
        switch (point.getRenderType()) {
        case OMPoint.RENDERTYPE_LATLON:
            LinkPoint.write((float) point.getLat(),
                    (float) point.getLon(),
                    point.getRadius(),
                    props,
                    link.dos);
            break;
        case OMPoint.RENDERTYPE_XY:
            LinkPoint.write(point.getX(),
                    point.getY(),
                    point.getRadius(),
                    props,
                    link.dos);
            break;
        case OMPoint.RENDERTYPE_OFFSET:
            LinkPoint.write((float) point.getLat(),
                    (float) point.getLon(),
                    point.getX(),
                    point.getY(),
                    point.getRadius(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkPoint.write: point rendertype unknown.");
        }
    }

    /**
     * Read the DataInputStream, and create an OMPoint. Assumes that the
     * LinkPoint header has been read from the link.
     * 
     * @param dis DataInputStream
     * @return OMPoint
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMPoint
     */
    public static OMPoint read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the DataInputStream, and create an OMPoint. Assumes that the
     * LinkPoint header has been read from the link.
     * 
     * @param dis DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoint being read.
     * @return OMPoint
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMPoint
     */
    public static OMPoint read(DataInputStream dis,
                               LinkProperties propertiesBuffer)
            throws IOException {
        OMPoint point = null;
        int x1, y1, radius;
        float lt, ln;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON:
            lt = dis.readFloat();
            ln = dis.readFloat();
            radius = dis.readInt();

            point = new OMPoint(lt, ln, radius);
            break;
        case RENDERTYPE_XY:
            x1 = dis.readInt();
            y1 = dis.readInt();
            radius = dis.readInt();

            point = new OMPoint(x1, y1, radius);
            break;
        case RENDERTYPE_OFFSET:
            lt = dis.readFloat();
            ln = dis.readFloat();

            x1 = dis.readInt();
            y1 = dis.readInt();
            radius = dis.readInt();

            point = new OMPoint(lt, ln, x1, y1, radius);
            break;
        default:
        }

        if (point != null) {
            propertiesBuffer = LinkProperties.loadPropertiesIntoOMGraphic(dis,
                    point,
                    propertiesBuffer);

            if (propertiesBuffer != null) {
                point.setOval(PropUtils.booleanFromProperties(propertiesBuffer,
                        LPC_POINT_OVAL,
                        OMPoint.DEFAULT_ISOVAL));
            }
        }

        return point;
    }

}