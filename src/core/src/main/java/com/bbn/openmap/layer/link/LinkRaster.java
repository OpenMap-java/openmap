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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkRaster.java,v $
// $RCSfile: LinkRaster.java,v $
// $Revision: 1.7 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Read and write a Link protocol versions of a raster.
 */
public class LinkRaster implements LinkGraphicConstants,
        LinkPropertiesConstants {

    /**
     * Writes an image, Lat/Lon placement with a direct colormodel.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int w, int h, int[] pix,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(COLORMODEL_DIRECT);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(pix.length);

        for (int i = 0; i < pix.length; i++) {
            dos.writeInt(pix[i]);
        }
        properties.write(dos);
    }

    /**
     * Write an image, XY placement with a direct colormodel.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h, int[] pix,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeByte(COLORMODEL_DIRECT);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(pix.length);

        for (int i = 0; i < pix.length; i++) {
            dos.writeInt(pix[i]);
        }
        properties.write(dos);
    }

    /**
     * Write an image, Lat/lon placement with XY offset with a direct
     * colormodel.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, int[] pix,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeByte(COLORMODEL_DIRECT);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(pix.length);

        for (int i = 0; i < pix.length; i++) {
            dos.writeInt(pix[i]);
        }
        properties.write(dos);
    }

    /**
     * Write an image, Lat/Lon placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param image java.awt.Image to use for image.
     * @param image_width width of image in pixels.
     * @param image_height height of image in pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, Image image, int image_width,
                             int image_height, LinkProperties properties,
                             DataOutputStream dos) throws IOException,
            InterruptedException {

        int[] pixels = new int[image_width * image_height];

        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, image_width, image_height, pixels, 0, image_width);
        pixelgrabber.grabPixels();

        LinkRaster.write(lt,
                ln,
                image_width,
                image_height,
                pixels,
                properties,
                dos);
    }

    /**
     * Write an image, X/Y placement with an ImageIcon.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param image java.awt.Image to use for image.
     * @param image_width width of image in pixels.
     * @param image_height height of image in pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int x1, int y1, Image image, int image_width,
                             int image_height, LinkProperties properties,
                             DataOutputStream dos) throws IOException,
            InterruptedException {
        int[] pixels = new int[image_width * image_height];

        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, image_width, image_height, pixels, 0, image_width);
        pixelgrabber.grabPixels();

        LinkRaster.write(x1,
                y1,
                image_width,
                image_height,
                pixels,
                properties,
                dos);
    }

    /**
     * Write an image, Lat/Lon with X/Y placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param image java.awt.Image to use for image.
     * @param image_width width of image in pixels.
     * @param image_height height of image in pixels.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             Image image, int image_width, int image_height,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException, InterruptedException {
        int[] pixels = new int[image_width * image_height];

        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, image_width, image_height, pixels, 0, image_width);
        pixelgrabber.grabPixels();

        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                image_width,
                image_height,
                pixels,
                properties,
                dos);
    }

    /**
     * Write an image, Lat/Lon placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param ii ImageIcon to use for image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, ImageIcon ii,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException, InterruptedException {

        int image_width, image_height;
        Image image;

        image_width = ii.getIconWidth();
        image_height = ii.getIconHeight();
        image = ii.getImage();
        LinkRaster.write(lt,
                ln,
                image,
                image_width,
                image_height,
                properties,
                dos);
    }

    /**
     * Write an image, X/Y placement with an ImageIcon.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param ii ImageIcon to use for image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int x1, int y1, ImageIcon ii,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException, InterruptedException {

        int image_width, image_height;
        Image image;

        image_width = ii.getIconWidth();
        image_height = ii.getIconHeight();
        image = ii.getImage();
        LinkRaster.write(x1,
                y1,
                image,
                image_width,
                image_height,
                properties,
                dos);
    }

    /**
     * Write an image, Lat/Lon with X/Y placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param ii ImageIcon to use for image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             ImageIcon ii, LinkProperties properties,
                             DataOutputStream dos) throws IOException,
            InterruptedException {

        int image_width, image_height;
        Image image;

        image_width = ii.getIconWidth();
        image_height = ii.getIconHeight();
        image = ii.getImage();
        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                image,
                image_width,
                image_height,
                properties,
                dos);

    }

    // //////////////////////////////////// IMAGEICON LOADED FROM AN
    // URL

    /**
     * Write an image, Lat/Lon placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param url URL to download the image from.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, String url,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(COLORMODEL_URL);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        properties.setProperty(LPC_LINKRASTERIMAGEURL, url);
        properties.write(dos);
    }

    /**
     * Write an image, X/Y placement with an ImageIcon.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param url URL to download the image from.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int x1, int y1, String url,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeByte(COLORMODEL_URL);
        dos.writeInt(x1);
        dos.writeInt(y1);
        properties.setProperty(LPC_LINKRASTERIMAGEURL, url);
        properties.write(dos);
    }

    /**
     * Write an image, Lat/Lon with X/Y placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param url URL to download the image from.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             String url, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeByte(COLORMODEL_URL);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        properties.setProperty(LPC_LINKRASTERIMAGEURL, url);
        properties.write(dos);
    }

    // //////////////////////////////////// BYTE PIXELS with
    // COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a colortable
     * and a byte array to construct the int[] pixels.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int w, int h, byte[] bytes,
                             Color[] colorTable, int trans,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeByte(COLORMODEL_INDEXED);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);
        dos.writeInt(colorTable.length);

        int i;
        for (i = 0; i < colorTable.length; i++) {
            dos.writeInt(colorTable[i].getRGB());
        }
        dos.writeInt(trans);

        properties.write(dos);
    }

    /**
     * XY placement with a indexed colormodel, which is using a colortable and a
     * byte array to construct the int[] pixels.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h, byte[] bytes,
                             Color[] colorTable, int trans,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeByte(COLORMODEL_INDEXED);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);
        dos.writeInt(colorTable.length);

        int i;
        for (i = 0; i < colorTable.length; i++) {
            dos.writeInt(colorTable[i].getRGB());
        }
        dos.writeInt(trans);

        properties.write(dos);
    }

    /**
     * Lat/lon placement with XY offset with a indexed colormodel, which is
     * using a colortable and a byte array to construct the int[] pixels.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @param properties description of drawing attributes. Not used, but
     *        included to be consistent with the protocol graphics format.
     * @param dos DataOutputStream
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, byte[] bytes, Color[] colorTable,
                             int trans, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.RASTER_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_RASTER);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeByte(COLORMODEL_INDEXED);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);
        dos.writeInt(colorTable.length);

        int i;

        for (i = 0; i < colorTable.length; i++) {
            dos.writeInt(colorTable[i].getRGB());
        }
        dos.writeInt(trans);

        properties.write(dos);
    }

    /**
     * Write a raster to the link.
     */
    public static void write(OMRaster raster, Link link, LinkProperties props)
            throws IOException {

        switch (raster.getRenderType()) {
        case OMRaster.RENDERTYPE_LATLON:
        case OMRaster.RENDERTYPE_XY:
        case OMRaster.RENDERTYPE_OFFSET:
        default:
            Debug.error("LinkRaster.write: raster not implemented.");
        }
    }

    /**
     * Read the DataInputStream, and create an OMRaster. Assumes that the
     * LinkRaster header has been read from the link.
     * 
     * @param dis DataInputStream
     * @return OMRaster
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMRaster
     */
    public static OMRaster read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the DataInputStream, and create an OMRaster. Assumes that the
     * LinkRaster header has been read from the link.
     * 
     * @param dis DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMRaster
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMRaster
     */
    public static OMRaster read(DataInputStream dis,
                                LinkProperties propertiesBuffer)
            throws IOException {

        OMRaster raster = null;
        float lat = 0;
        float lon = 0;
        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;
        int length, i;
        String url;

        Debug.message("link", "LinkRaster | Reading Raster graphic");

        int renderType = dis.readByte();
        int colorModel = dis.readByte();

        if (Debug.debugging("link")) {
            System.out.println("LinkRaster | Rendertype = " + renderType
                    + ", colorModel = " + colorModel);
        }

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
            if (Debug.debugging("link")) {
                System.out.println("LinkRaster | Location: lat = " + lat
                        + ", lon = " + lon);
            }
        }

        // Now act differently depending on the colormodel
        if (colorModel != COLORMODEL_URL) {

            w = dis.readInt();
            h = dis.readInt();

            if (Debug.debugging("link")) {
                System.out.println("LinkRaster | Size: width = " + w
                        + ", height = " + h);
            }

            if (colorModel == COLORMODEL_INDEXED) {

                length = dis.readInt();

                byte[] bytes = new byte[length];

                if (Debug.debugging("link")) {
                    System.out.println("LinkRaster | Reading " + length
                            + " bytes.");
                }
                dis.readFully(bytes);

                if (Debug.debugging("link")) {
                    System.out.println("LinkRaster | read bytes.");
                }

                length = dis.readInt();

                if (Debug.debugging("link")) {
                    System.out.println("LinkRaster | " + length + " Colors.");
                }

                Color[] colorTable = new Color[length];
                for (i = 0; i < length; i++) {
                    int colorvalue = dis.readInt();
                    colorTable[i] = ColorFactory.createColor(colorvalue, true);
                    if (Debug.debugging("linkdetail")) {
                        System.out.println("LinkRaster | Color " + i + " =  "
                                + colorTable[i] + " from "
                                + Integer.toHexString(colorvalue));
                    }
                }

                int trans = dis.readInt();
                if (Debug.debugging("link")) {
                    System.out.println("LinkRaster | Transparency =  " + trans);
                }

                switch (renderType) {
                case RENDERTYPE_OFFSET:
                    raster = new OMRaster(lat, lon, x, y, w, h, bytes, colorTable, trans);
                    break;
                case RENDERTYPE_XY:
                    raster = new OMRaster(x, y, w, h, bytes, colorTable, trans);
                    break;
                case RENDERTYPE_LATLON:
                default:
                    raster = new OMRaster(lat, lon, w, h, bytes, colorTable, trans);
                }

            } else { // must be COLORMODEL_DIRECT
                length = dis.readInt();
                int[] pix = new int[length];
                if (Debug.debugging("link")) {
                    System.out.println("LinkRaster | Reading " + length
                            + " pixels.");
                }

                for (i = 0; i < length; i++) {
                    pix[i] = dis.readInt();
                }
                switch (renderType) {
                case RENDERTYPE_OFFSET:
                    raster = new OMRaster(lat, lon, x, y, w, h, pix);
                    break;
                case RENDERTYPE_XY:
                    raster = new OMRaster(x, y, w, h, pix);
                    break;
                case RENDERTYPE_LATLON:
                default:
                    raster = new OMRaster(lat, lon, w, h, pix);
                }
            }
        }

        LinkProperties properties = (LinkProperties) LinkProperties.read(dis, propertiesBuffer).clone();

        if (colorModel == COLORMODEL_URL) {
            url = properties.getProperty(LPC_LINKRASTERIMAGEURL);

            if (url != null) {
                switch (renderType) {
                case RENDERTYPE_OFFSET:
                    raster = new OMRaster(lat, lon, x, y, new ImageIcon(url));
                    break;
                case RENDERTYPE_XY:
                    raster = new OMRaster(x, y, new ImageIcon(url));
                    break;
                case RENDERTYPE_LATLON:
                default:
                    raster = new OMRaster(lat, lon, new ImageIcon(url));
                }
            }
        }

        if (raster != null) {
            properties.setProperties(raster);
            raster.setRotationAngle((double) ProjMath.degToRad(PropUtils.floatFromProperties(properties,
                    LPC_LINKROTATION,
                    0.0f)));
        }

        return raster;
    }
}