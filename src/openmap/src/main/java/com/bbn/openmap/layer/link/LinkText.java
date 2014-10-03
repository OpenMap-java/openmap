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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkText.java,v $
// $RCSfile: LinkText.java,v $
// $Revision: 1.10 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class LinkText implements LinkGraphicConstants, LinkPropertiesConstants {

    public static String DEFAULT_FONT = "-*-SansSerif-normal-o-normal--12-*-*-*-*-*-*";

    /**
     * Creates a text object, with Lat/Lon placement.
     * 
     * @param latPoint latitude of the string, in decimal degrees.
     * @param lonPoint longitude of the string, in decimal degrees.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string.
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(float latPoint, float lonPoint, String stuff,
                             String font, int just, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.TEXT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_TEXT);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeByte(just);

        properties.setProperty(LPC_LINKTEXTSTRING, stuff);
        properties.setProperty(LPC_LINKTEXTFONT, font);
        properties.write(dos);
    }

    /**
     * Creates a text object, with XY placement, and default SansSerif font.
     * 
     * @param x1 horizontal window pixel location of the string.
     * @param y1 vertical window pixel location of the string.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(int x1, int y1, String stuff, String font,
                             int just, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.TEXT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_TEXT);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeByte(just);

        properties.setProperty(LPC_LINKTEXTSTRING, stuff);
        properties.setProperty(LPC_LINKTEXTFONT, font);
        properties.write(dos);
    }

    /**
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center of text/ellipse.
     * @param lonPoint longitude of center of text/ellipse.
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string.
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(float latPoint, float lonPoint, int offset_x1,
                             int offset_y1, String stuff, String font,
                             int just, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.TEXT_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_TEXT);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeByte(just);

        properties.setProperty(LPC_LINKTEXTSTRING, stuff);
        properties.setProperty(LPC_LINKTEXTFONT, font);
        properties.write(dos);
    }

    /**
     * Write a text to the link.
     */
    public static void write(OMText text, Link link, LinkProperties props)
            throws IOException {

        switch (text.getRenderType()) {
        case OMText.RENDERTYPE_LATLON:
            write((float) text.getLat(),
                    (float) text.getLon(),
                    text.getData(),
                    OMText.fontToXFont(text.getFont()),
                    text.getJustify(),
                    props,
                    link.dos);
            break;
        case OMText.RENDERTYPE_XY:
            write(text.getX(),
                    text.getY(),
                    text.getData(),
                    OMText.fontToXFont(text.getFont()),
                    text.getJustify(),
                    props,
                    link.dos);
            break;
        case OMText.RENDERTYPE_OFFSET:
            write((float) text.getLat(),
                    (float) text.getLon(),
                    text.getX(),
                    text.getY(),
                    text.getData(),
                    OMText.fontToXFont(text.getFont()),
                    text.getJustify(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkText.write: text rendertype unknown.");
        }
    }

    /**
     * Read the DataInputStream to create a OMText. Assumes the LinkText header
     * has already been read.
     * 
     * @param dis DataInputStream
     * @return OMText
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMText
     */
    public static OMText read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the DataInputStream to create a OMText. Assumes the LinkText header
     * has already been read.
     * 
     * @param dis DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMText being read.
     * @return OMText
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMText
     */
    public static OMText read(DataInputStream dis,
                              LinkProperties propertiesBuffer)
            throws IOException {

        OMText text = null;
        float lat = 0;
        float lon = 0;
        int x = 0;
        int y = 0;
        int just = 0;
        String string, font;

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

        just = dis.readByte();

        LinkProperties properties = (LinkProperties) LinkProperties.read(dis,
                propertiesBuffer).clone();

        string = properties.getProperty(LPC_LINKTEXTSTRING);
        font = properties.getProperty(LPC_LINKTEXTFONT);

        if (string == null)
            string = "";
        if (font == null)
            font = DEFAULT_FONT;

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            text = new OMText(lat, lon, x, y, string, OMText.rebuildFont(font), just);
            break;
        case RENDERTYPE_XY:
            text = new OMText(x, y, string, OMText.rebuildFont(font), just);
            break;
        case RENDERTYPE_LATLON:
        default:
            text = new OMText(lat, lon, string, OMText.rebuildFont(font), just);
        }

        if (text != null) {
            properties.setProperties(text);
            text.setBaseline(PropUtils.intFromProperties(properties,
                    LPC_LINKTEXTBASELINE,
                    BASELINE_BOTTOM));
            text.setRotationAngle((double) ProjMath.degToRad(PropUtils.floatFromProperties(properties,
                    LPC_LINKROTATION,
                    0.0f)));
        }

        return text;
    }

}