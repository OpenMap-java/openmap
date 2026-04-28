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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkBitmap.java,v $
// $RCSfile: LinkBitmap.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMBitmap;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Reading and writing the Link protocol version of a bitmap..
 */
public class LinkBitmap implements LinkGraphicConstants,
        LinkPropertiesConstants {

    /**
     * Lat/Lon placement.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float lt, float ln, int w, int h, byte[] bytes,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.BITMAP_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_BITMAP);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(w);
        dos.writeInt(h);

        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);

        properties.write(dos);
    }

    /**
     * XY placement.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h, byte[] bytes,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.BITMAP_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_BITMAP);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(w);
        dos.writeInt(h);

        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);

        properties.write(dos);
    }

    /**
     * Lat/lon placement with XY offset.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, byte[] bytes,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.BITMAP_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_BITMAP);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);

        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);

        properties.write(dos);
    }

    /**
     * Write a bitmap to the link.
     */
    public static void write(OMBitmap bitmap, Link link, LinkProperties props)
            throws IOException {

        switch (bitmap.getRenderType()) {
        case OMBitmap.RENDERTYPE_LATLON:
            LinkBitmap.write((float) bitmap.getLat(),
                    (float) bitmap.getLon(),
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    bitmap.getBits(),
                    props,
                    link.dos);
            break;
        case OMBitmap.RENDERTYPE_XY:
            LinkBitmap.write(bitmap.getX(),
                    bitmap.getY(),
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    bitmap.getBits(),
                    props,
                    link.dos);
            break;
        case OMBitmap.RENDERTYPE_OFFSET:
            LinkBitmap.write((float) bitmap.getLat(),
                    (float) bitmap.getLon(),
                    bitmap.getX(),
                    bitmap.getY(),
                    bitmap.getBits(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkBitmap.write: bitmap rendertype not handled.");
        }
    }

    /**
     * Read a Bitmap off a DataInputStream. Assumes the Bitmap header has
     * already been read.
     * 
     * @param dis DataInputStream to read from.
     * @return OMBitmap
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMBitmap
     */
    public static OMBitmap read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read a Bitmap off a DataInputStream. Assumes the Bitmap header has
     * already been read.
     * 
     * @param dis DataInputStream to read from.
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMBitmap
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMBitmap
     */
    public static OMBitmap read(DataInputStream dis,
                                LinkProperties propertiesBuffer)
            throws IOException {

        OMBitmap bitmap = null;
        float lat = 0;
        float lon = 0;
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        int length;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            lat = dis.readFloat();
            lon = dis.readFloat();
            // Fall through...
        case RENDERTYPE_XY:
            x = dis.readInt();
            y = dis.readInt();
            break;
        case RENDERTYPE_LATLON:
        default:
            lat = dis.readFloat();
            lon = dis.readFloat();
        }

        w = dis.readInt();
        h = dis.readInt();
        length = dis.readInt();

        byte[] bytes = new byte[length];
        dis.readFully(bytes);

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            bitmap = new OMBitmap(lat, lon, x, y, w, h, bytes);
            break;
        case RENDERTYPE_XY:
            bitmap = new OMBitmap(x, y, w, h, bytes);
            break;
        case RENDERTYPE_LATLON:
        default:
            bitmap = new OMBitmap(lat, lon, w, h, bytes);
        }

        if (bitmap != null) {
            LinkProperties properties = LinkProperties.loadPropertiesIntoOMGraphic(dis, bitmap, propertiesBuffer);
            bitmap.setRotationAngle((double) ProjMath.degToRad(PropUtils.floatFromProperties(properties,
                    LPC_LINKROTATION,
                    0.0f)));
        }

        return bitmap;
    }
}