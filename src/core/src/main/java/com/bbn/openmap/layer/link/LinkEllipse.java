// **********************************************************************
// 
// <copyright>
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkEllipse.java,v $
// $RCSfile: LinkEllipse.java,v $
// $Revision: 1.4 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMEllipse;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Reading and writing a Link protocol version of a circle.
 */
public class LinkEllipse implements LinkGraphicConstants,
        LinkPropertiesConstants {

    /**
     * Write an ellipse with Lat/Lon placement with axis defined in terms of
     * distance.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param majorAxisSpan x axis value, units
     * @param minorAxisSpan y axis value, units
     * @param units integer value for units for distance - OMCircle.KM,
     *        OMCircle.MILES, OMCircle.NMILES. If &lt; 0, assume decimal
     *        degrees.
     * @param rotationAngle The angle by which the circle/ellipse is to be
     *        rotated, in radians
     * 
     * @param properties attributes for the circle.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float latPoint, float lonPoint,
                             float majorAxisSpan, float minorAxisSpan,
                             int units, float rotationAngle,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.ELLIPSE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ELLIPSE);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeFloat(majorAxisSpan);
        dos.writeFloat(minorAxisSpan);
        dos.writeByte(units);
        dos.writeFloat(rotationAngle);
        properties.write(dos);
    }

    /**
     * Create a OMEllipse, positioned with a x-y center with x-y axis.
     * Rendertype is RENDERTYPE_XY.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param majorAxisSpan horizontal diameter of circle/ellipse, pixels
     * @param minorAxisSpan vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians
     */
    public static void write(int x1, int y1, int majorAxisSpan,
                             int minorAxisSpan, float rotateAngle,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        dos.write(Link.ELLIPSE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ELLIPSE);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);
        dos.writeInt(majorAxisSpan);
        dos.writeInt(minorAxisSpan);
        dos.writeFloat(rotateAngle);
        properties.write(dos);
    }

    /**
     * Create a OMEllipse, positioned with a lat-lon center and x-y axis.
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians
     */
    public static void write(float latPoint, float lonPoint, int w, int h,
                             float rotateAngle, LinkProperties properties,
                             DataOutputStream dos) throws IOException {
        write(latPoint, lonPoint, 0, 0, w, h, rotateAngle, properties, dos);
    }

    /**
     * Create a OMEllipse, positioned at a Lat-lon location, x-y offset, x-y
     * axis. Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of circle/ellipse, pixels.
     * @param h vertical diameter of circle/ellipse, pixels.
     */
    public static void write(float latPoint, float lonPoint, int offset_x1,
                             int offset_y1, int w, int h, float rotateAngle,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {
        dos.write(Link.ELLIPSE_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_ELLIPSE);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(latPoint);
        dos.writeFloat(lonPoint);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);
        dos.writeInt(w);
        dos.writeInt(h);
        dos.writeFloat(rotateAngle);
        properties.write(dos);
    }

    public static void write(OMEllipse ellipse, Link link, LinkProperties props)
            throws IOException {

        LatLonPoint llp;
        switch (ellipse.getRenderType()) {
        case OMEllipse.RENDERTYPE_LATLON:
            llp = ellipse.getLatLon();
            LinkEllipse.write(llp.getLatitude(),
                    llp.getLongitude(),
                    (float) Length.KM.fromRadians(ellipse.getMajorAxis()),
                    (float) Length.KM.fromRadians(ellipse.getMinorAxis()),
                    0, /* Length.KM */
                    (float) ellipse.getRotationAngle(),
                    props,
                    link.dos);
            break;
        case LinkEllipse.RENDERTYPE_XY:
            LinkEllipse.write(ellipse.getX(),
                    ellipse.getY(),
                    (int) ellipse.getMajorAxis(),
                    (int) ellipse.getMinorAxis(),
                    (float) ellipse.getRotationAngle(),
                    props,
                    link.dos);
            break;
        case LinkEllipse.RENDERTYPE_OFFSET:
            llp = ellipse.getLatLon();
            LinkEllipse.write(llp.getLatitude(),
                    llp.getLongitude(),
                    ellipse.getOffX(),
                    ellipse.getOffY(),
                    (int) ellipse.getMajorAxis(),
                    (int) ellipse.getMinorAxis(),
                    (float) ellipse.getRotationAngle(),
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkCircle.write: circle rendertype unknown.");
        }
    }

    /**
     * Read the ellipse protocol off the data input, and return an OMEllipse.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @return OMEllipse
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMEllipse
     */
    public static OMEllipse read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read the ellipse protocol off the data input, and return an OMEllipse.
     * Assumes the header for the graphic has already been read.
     * 
     * @param dis the DataInputStream
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMEllipse
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMEllipse
     */
    public static OMEllipse read(DataInputStream dis,
                                 LinkProperties propertiesBuffer)
            throws IOException {
        OMEllipse ellipse = null;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_LATLON: {
            float lat = dis.readFloat();
            float lon = dis.readFloat();
            double majorAxisSpan = dis.readFloat();
            double minorAxisSpan = dis.readFloat();
            int units = dis.readByte();
            double rotationAngle = dis.readFloat();

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
                break;
            }

            ellipse = new OMEllipse(new LatLonPoint.Double(lat, lon), majorAxisSpan, minorAxisSpan, unit, rotationAngle);
            break;
        }
        case RENDERTYPE_XY: {
            int x = dis.readInt();
            int y = dis.readInt();
            int majorAxisSpan = dis.readInt();
            int minorAxisSpan = dis.readInt();
            double rotationAngle = dis.readFloat();

            ellipse = new OMEllipse(x, y, majorAxisSpan, minorAxisSpan, rotationAngle);
            break;
        }
        case RENDERTYPE_OFFSET: {
            float lat = dis.readFloat();
            float lon = dis.readFloat();
            int offsetX = dis.readInt();
            int offsetY = dis.readInt();
            int w = dis.readInt();
            int h = dis.readInt();
            double rotationAngle = dis.readFloat();

            ellipse = new OMEllipse(new LatLonPoint.Double(lat, lon), offsetX, offsetY, w, h, rotationAngle);
            break;
        }
        default: {
            Debug.error("LinkEllipse.read: ellipse rendertype unknown.");
            break;
        }
        }

        if (ellipse != null) {
            LinkProperties.loadPropertiesIntoOMGraphic(dis,
                    ellipse,
                    propertiesBuffer);
        }

        return ellipse;
    }

}