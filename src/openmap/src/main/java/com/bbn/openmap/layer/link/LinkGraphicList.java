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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGraphicList.java,v $
// $RCSfile: LinkGraphicList.java,v $
// $Revision: 1.10 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.Color;
import java.awt.Image;
import java.io.EOFException;
import java.io.IOException;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The LinkGraphicList is an object that describes a list of graphics. This
 * object can be used to write the graphics to the link, and read the graphics
 * response section from the link.
 * <P>
 * 
 * To use it to write to the link, create the LinkGraphicList with the
 * constructor that takes a link as its only argument, and then use the write
 * methods to add graphics. When all the graphics are written to the link, close
 * the section by calling end() with the appropriate symbol.
 * <P>
 * 
 * To use it to read from a link, use the constructor that takes a link and a
 * LinkOMGraphicsList (and a projection, if you want to generate the graphics as
 * you read them). Call getGraphics() to get the updated list.
 */
public class LinkGraphicList implements LinkGraphicConstants {

    /** Link used for the transmission/reception of graphics. */
    protected Link link = null;
    /** Graphics list received. */
    protected LinkOMGraphicList graphics = null;
    /** The terminator of the graphics section when receiving graphics. */
    protected String linkStatus = Link.END_TOTAL;
    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;
    /** The properties returned for this list. */
    protected LinkProperties properties;

    /** Write a graphics section to the link. */
    public LinkGraphicList(Link link, LinkProperties properties)
            throws IOException {
        this.link = link;
        link.start(Link.GRAPHICS_HEADER);
        link.dos.writeFloat(version);
        properties.write(link);
    }

    /**
     * Read the graphics section off the link.
     * 
     * @param link the link to read the response from.
     * @param graphicList the list to add graphics to.
     * @throws IOException
     * @throws EOFException
     */
    public LinkGraphicList(Link link, LinkOMGraphicList graphicList)
            throws IOException, EOFException {
        this(link, graphicList, (Projection) null, (OMGridGenerator) null);
    }

    /**
     * Read the graphics section off the link, if you want the graphics to be
     * projected as they come off the link.
     * 
     * @param link the link to read graphics from.
     * @param graphicList the list to add graphics to.
     * @param proj the projection to use for generating graphics.
     * @param generator an OMGridGenerator that knows how to render grid
     *        objects.
     * @throws IOException
     * @throws EOFException
     */
    public LinkGraphicList(Link link, LinkOMGraphicList graphicList,
            Projection proj, OMGridGenerator generator) throws IOException,
            EOFException {
        this.link = link;
        graphics = graphicList;

        if (graphics == null) {
            graphics = new LinkOMGraphicList();
        }

        linkStatus = readGraphics(graphics, proj, generator);
    }

    /**
     * After a readAndParse() has been called on a link, this can be called to
     * retrieve graphics in an LinkOMGraphicList, if any graphics were sent.
     * 
     * @return LinkOMGraphicList containing the graphics read off the link. If
     *         no graphics were sent the list will be empty.
     */
    public LinkOMGraphicList getGraphics() {
        return graphics;
    }

    /**
     * After reading the graphics response, this returns the section ending
     * string terminating the graphics section, either Link.END_TOTAL or
     * Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION.
     */
    public String getLinkStatus() {
        return linkStatus;
    }

    /**
     * Get the properties for the LinkGraphicList. Any information messages can
     * be picked up from within the properties - html, URL, messages, text and
     * information lines.
     * 
     * @return properties
     */
    public LinkProperties getProperties() {
        if (properties != null) {
            return properties;
        } else {
            return LinkProperties.EMPTY_PROPERTIES;
        }
    }

    /**
     * The server method that needs to be called at the end of sending a
     * graphics response. This will tell the link what type of teminator to put
     * on the end of the graphics response section, and also tell the link to
     * fluxh the output stream..
     * 
     * @param endType use Link.END_SECTION if you want to add more types of
     *        response sections. Use Link.END_TOTAL at the end of the total
     *        transmission.
     * @throws IOException
     */
    public void end(String endType) throws IOException {
        link.end(endType);
    }

    /**
     * If a GRAPHICS_RESPONSE_HEADER has been encountered coming off the link,
     * then this method should be called to read the string of graphics that
     * follows. The graphics are read and added to the LinkOMGraphicList
     * provided.
     * 
     * @param graphics the LinkOMGraphicList to add the link graphics too. This
     *        method assumes that this is never null.
     * @param proj If you want the graphics to be projected as they come off the
     *        wire, add a projection here. Otherwise, use null.
     * @param generator an OMGridGenerator that knows how to render grid
     *        objects.
     * @throws IOException
     * @throws EOFException
     */
    protected String readGraphics(LinkOMGraphicList graphics, Projection proj,
                                  OMGridGenerator generator)
            throws IOException, EOFException {

        OMGraphic graphic;
        long startTime = System.currentTimeMillis();
        String header = null;
        int graphicType;

        // This is important, it's checked by the LinkLayer to see if
        // it needs to generate the LinkOMGraphicList to see if the
        // contents need to be generated.
        graphics.setNeedToRegenerate(proj == null);

        // doing nothing with the version number.
        float ver = link.dis.readFloat();

        if (ver != version) {
            if (ver == .1) {// Big difference....
                throw new IOException("LinkGraphicList: Versions do not match! DANGER!");
            } else {
                Debug.message("link", "LinkGraphicList: Versions do not match.");
            }
        }

        if (properties != null) {
            properties.clear();
        }

        properties = LinkProperties.read(link.dis, properties);

        Debug.message("link", "LinkGraphicList: reading graphics:");

        LinkProperties propertiesBuffer = new LinkProperties(properties);

        while (true) {
            graphic = null;
            // Just consume the header, don't create a useless
            // string object.
            header = link.readDelimiter(false);

            if (header == Link.END_TOTAL || header == Link.END_SECTION) {

                long endTime = System.currentTimeMillis();
                Debug.message("link", "LinkGraphicList: received "
                        + graphics.size() + " graphics in "
                        + (float) (endTime - startTime) / 1000.0f + " seconds");

                return header;
            }

            graphicType = link.dis.readByte();

            switch (graphicType) {
            case GRAPHICTYPE_LINE:
                graphic = LinkLine.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_POLY:
                graphic = LinkPoly.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_RECTANGLE:
                graphic = LinkRectangle.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_POINT:
                graphic = LinkPoint.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_CIRCLE:
                graphic = LinkCircle.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_ELLIPSE:
                graphic = LinkEllipse.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_RASTER:
                graphic = LinkRaster.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_BITMAP:
                graphic = LinkBitmap.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_TEXT:
                graphic = LinkText.read(link.dis, propertiesBuffer);
                break;
            case GRAPHICTYPE_GRID:
                graphic = LinkGrid.read(link.dis, propertiesBuffer);
                break;
            default:
                throw new IOException("LinkGraphicList: received unknown graphic type.");
            }

            if (graphic != null) {
                if (graphic instanceof OMGrid) {
                    ((OMGrid) graphic).setGenerator(generator);
                }
                if (proj != null) {
                    graphic.generate(proj);
                }
                graphics.add(graphic);
            }
        }
    }

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
     * @throws IOException
     */
    public void addArc(float latPoint, float lonPoint, int w, int h, float s,
                       float e, LinkProperties properties) throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                0,
                0,
                w,
                h,
                s,
                e,
                properties,
                link.dos);
    }

    /**
     * Write an arc with x/y placement.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param w horizontal diameter of arc, pixels
     * @param h vertical diameter of arc, pixels
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @throws IOException
     */
    public void addArc(int x1, int y1, int w, int h, float s, float e,
                       LinkProperties properties) throws IOException {
        LinkArc.write(x1, y1, w, h, s, e, properties, link.dos);
    }

    /**
     * Writing an arc at a x, y, offset to a Lat/Lon location.
     * 
     * @param latPoint latitude of center of arc.
     * @param lonPoint longitude of center of arc.
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of arc, pixels.
     * @param h vertical diameter of arc, pixels.
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @throws IOException
     */
    public void addArc(float latPoint, float lonPoint, int offset_x1,
                       int offset_y1, int w, int h, float s, float e,
                       LinkProperties properties) throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                offset_x1,
                offset_y1,
                w,
                h,
                s,
                e,
                properties,
                link.dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location. Assumes the
     * radius is in decimal degrees.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @throws IOException
     */
    public void addArc(float latPoint, float lonPoint, float radius, float s,
                       float e, LinkProperties properties) throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                radius,
                -1,
                -1,
                s,
                e,
                properties,
                link.dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location, and allows you
     * to specify units of the radius.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - KM, MILES, NMILES. If
     *        &lt; 0, assume decimal degrees.
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @throws IOException
     */
    public void addArc(float latPoint, float lonPoint, float radius, int units,
                       float s, float e, LinkProperties properties)
            throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                radius,
                units,
                -1,
                s,
                e,
                properties,
                link.dos);
    }

    /**
     * Write an arc with a certain radius at a Lat/Lon location, and allows you
     * to specify units of the radius, as well as the number of vertices to use
     * to approximate the arc.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units integer value for units for distance - OMArc.KM,
     *        OMArc.MILES, OMArc.NMILES. If &lt; 0, assume decimal degrees.
     * @param nverts number of vertices for the poly-arc (if &lt; 3, value is
     *        generated internally).
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     * @param properties attributes for the arc.
     * @throws IOException
     */
    public void addArc(float latPoint, float lonPoint, float radius, int units,
                       int nverts, float s, float e, LinkProperties properties)
            throws IOException {
        LinkArc.write(latPoint,
                lonPoint,
                radius,
                units,
                nverts,
                s,
                e,
                properties,
                link.dos);
    }

    /**
     * Write a bitmap in the response.
     * 
     * @param lt latitude of placement of upper left corner of bitmap.
     * @param ln longitude of placement of upper left corner of bitmap.
     * @param w pixel width of bitmap.
     * @param h pixel height of bitmap.
     * @param bytes bitmap data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void addBitmap(float lt, float ln, int w, int h, byte[] bytes,
                          LinkProperties properties) throws IOException {
        LinkBitmap.write(lt, ln, w, h, bytes, properties, link.dos);
    }

    /**
     * Write a bitmap in the response.
     * 
     * @param x1 horizontal placement of upper left corner of bitmap.
     * @param y1 vertical placement of upper left corner of bitmap.
     * @param w pixel width of bitmap.
     * @param h pixel height of bitmap.
     * @param bytes bitmap data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void addBitmap(int x1, int y1, int w, int h, byte[] bytes,
                          LinkProperties properties) throws IOException {
        LinkBitmap.write(x1, y1, w, h, bytes, properties, link.dos);
    }

    /**
     * Write a bitmap in the response.
     * 
     * @param lt latitude of placement of upper left corner of bitmap.
     * @param ln longitude of placement of upper left corner of bitmap.
     * @param offset_x1 horizontal offset of upper left corner of bitmap.
     * @param offset_y1 vertical offset of upper left corner of bitmap.
     * @param w pixel width of bitmap.
     * @param h pixel height of bitmap.
     * @param bytes bitmap data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void addBitmap(float lt, float ln, int offset_x1, int offset_y1,
                          int w, int h, byte[] bytes, LinkProperties properties)
            throws IOException {
        LinkBitmap.write(lt,
                ln,
                offset_x1,
                offset_y1,
                w,
                h,
                bytes,
                properties,
                link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param latPoint latitude of placement of center of circle.
     * @param lonPoint longitude of placement of center of circle.
     * @param w pixel width of circle.
     * @param h pixel height of circle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(float latPoint, float lonPoint, int w, int h,
                          LinkProperties properties) throws IOException {
        LinkCircle.write(latPoint, lonPoint, w, h, properties, link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param x1 horizontal pixel placement of center of circle..
     * @param y1 vertical pixel placement of center of circle..
     * @param w pixel width of circle.
     * @param h pixel height of circle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(int x1, int y1, int w, int h,
                          LinkProperties properties) throws IOException {
        LinkCircle.write(x1, y1, w, h, properties, link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param latPoint latitude of placement of center of circle.
     * @param lonPoint longitude of placement of center of circle.
     * @param offset_x1 horizontal pixel offset of center of circle..
     * @param offset_y1 vertical pixel offset of center of circle..
     * @param w width of circle.
     * @param h height of circle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(float latPoint, float lonPoint, int offset_x1,
                          int offset_y1, int w, int h, LinkProperties properties)
            throws IOException {
        LinkCircle.write(latPoint,
                lonPoint,
                offset_x1,
                offset_y1,
                w,
                h,
                properties,
                link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param latPoint latitude of placement of center of circle.
     * @param lonPoint longitude of placement of center of circle.
     * @param radius radius of circle, in decimal degrees..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(float latPoint, float lonPoint, float radius,
                          LinkProperties properties) throws IOException {
        LinkCircle.write(latPoint,
                lonPoint,
                radius,
                -1,
                -1,
                properties,
                link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param latPoint latitude of placement of center of circle.
     * @param lonPoint longitude of placement of center of circle.
     * @param radius radius of circle, in decimal degrees..
     * @param units units of the radius - km, miles, nmiles, degrees..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(float latPoint, float lonPoint, float radius,
                          int units, LinkProperties properties)
            throws IOException {
        LinkCircle.write(latPoint,
                lonPoint,
                radius,
                units,
                -1,
                properties,
                link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param latPoint latitude of placement of center of circle.
     * @param lonPoint longitude of placement of center of circle.
     * @param radius radius of circle, in decimal degrees..
     * @param units units of the radius - km, miles, nmiles, degrees..
     * @param nverts number of points to use to approximate the circle. If
     *        negative, the client algorithm will figure out what is best.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void addCircle(float latPoint, float lonPoint, float radius,
                          int units, int nverts, LinkProperties properties)
            throws IOException {
        LinkCircle.write(latPoint,
                lonPoint,
                radius,
                units,
                nverts,
                properties,
                link.dos);
    }

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
     * @throws IOException
     */
    public void addEllipse(float latPoint, float lonPoint, float majorAxisSpan,
                           float minorAxisSpan, int units, float rotationAngle,
                           LinkProperties properties) throws IOException {

        LinkEllipse.write(latPoint,
                lonPoint,
                majorAxisSpan,
                minorAxisSpan,
                units,
                rotationAngle,
                properties,
                link.dos);
    }

    /**
     * Create a OMEllipse, positioned with a x-y center with x-y axis.
     * Rendertype is RENDERTYPE_XY.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param majorAxisSpan horizontal diameter of circle/ellipse, pixels
     * @param minorAxisSpan vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians *
     * @param properties the LinkProperties describing the attributes of the
     *        ellipse.
     */
    public void addEllipse(int x1, int y1, int majorAxisSpan,
                           int minorAxisSpan, float rotateAngle,
                           LinkProperties properties) throws IOException {

        LinkEllipse.write(x1,
                y1,
                majorAxisSpan,
                minorAxisSpan,
                rotateAngle,
                properties,
                link.dos);
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
     * @param properties the LinkProperties describing the attributes of the
     *        ellipse.
     */
    public void addEllipse(float latPoint, float lonPoint, int w, int h,
                           float rotateAngle, LinkProperties properties)
            throws IOException {

        LinkEllipse.write(latPoint,
                lonPoint,
                w,
                h,
                rotateAngle,
                properties,
                link.dos);
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
     * @param rotateAngle the rotation of the ellipse around the center point,
     *        in radians.
     * @param properties the LinkProperties describing the attributes of the
     *        ellipse.
     */
    public void addEllipse(float latPoint, float lonPoint, int offset_x1,
                           int offset_y1, int w, int h, float rotateAngle,
                           LinkProperties properties) throws IOException {

        LinkEllipse.write(latPoint,
                lonPoint,
                offset_x1,
                offset_y1,
                w,
                h,
                rotateAngle,
                properties,
                link.dos);
    }

    /**
     * Add a Grid with Lat/Lon placement.
     * 
     * @param lt latitude of the top of the grid.
     * @param ln longitude of the left side of the grid.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution degrees/point between rows of the grid.
     * @param hResolution degrees/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties Properties containing attributes.
     * @throws IOException
     */
    public void addGrid(float lt, float ln, int rows, int columns,
                        float orientation, float vResolution,
                        float hResolution, int major, int[] data,
                        LinkProperties properties) throws IOException {
        LinkGrid.write(lt,
                ln,
                rows,
                columns,
                orientation,
                vResolution,
                hResolution,
                major,
                data,
                properties,
                link.dos);
    }

    /**
     * Add a Grid with XY placement.
     * 
     * @param x1 window location of the left side of the grid.
     * @param y1 window location of the top of the grid.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution pixels/point between rows of the grid.
     * @param hResolution pixels/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties Properties containing attributes.
     * @throws IOException
     */
    public void addGrid(int x1, int y1, int rows, int columns,
                        float orientation, float vResolution,
                        float hResolution, int major, int[] data,
                        LinkProperties properties) throws IOException {
        LinkGrid.write(x1,
                y1,
                rows,
                columns,
                orientation,
                vResolution,
                hResolution,
                major,
                data,
                properties,
                link.dos);
    }

    /**
     * Grid Lat/lon placement with XY offset.
     * 
     * @param lt latitude of the top of the grid, before the offset.
     * @param ln longitude of the left side of the grid, before the offset.
     * @param offset_x1 number of pixels to move grid to the right.
     * @param offset_y1 number of pixels to move grid down.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution pixels/point between rows of the grid.
     * @param hResolution pixels/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties Properties containing attributes.
     * @throws IOException
     */
    public void addGrid(float lt, float ln, int offset_x1, int offset_y1,
                        int rows, int columns, float orientation,
                        float vResolution, float hResolution, int major,
                        int[] data, LinkProperties properties)
            throws IOException {
        LinkGrid.write(lt,
                ln,
                offset_x1,
                offset_y1,
                rows,
                columns,
                orientation,
                vResolution,
                hResolution,
                major,
                data,
                properties,
                link.dos);
    }

    /**
     * Write a line in the response.
     * 
     * @param lat_1 latitude of placement of start of line.
     * @param lon_1 longitude of placement of start of line.
     * @param lat_2 latitude of placement of end of line.
     * @param lon_2 longitude of placement of end of line.
     * @param lineType type of line - straight, rhumb, great circle..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void addLine(float lat_1, float lon_1, float lat_2, float lon_2,
                        int lineType, LinkProperties properties)
            throws IOException {
        LinkLine.write(lat_1,
                lon_1,
                lat_2,
                lon_2,
                lineType,
                properties,
                link.dos);
    }

    /**
     * Write a line in the response.
     * 
     * @param lat_1 latitude of placement of start of line.
     * @param lon_1 longitude of placement of start of line.
     * @param lat_2 latitude of placement of end of line.
     * @param lon_2 longitude of placement of end of line.
     * @param lineType type of line - straight, rhumb, great circle..
     * @param nsegs number of points to use to approximate curved line..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void addLine(float lat_1, float lon_1, float lat_2, float lon_2,
                        int lineType, int nsegs, LinkProperties properties)
            throws IOException {
        LinkLine.write(lat_1,
                lon_1,
                lat_2,
                lon_2,
                lineType,
                nsegs,
                properties,
                link.dos);
    }

    /**
     * Write a line in the response.
     * 
     * @param x1 Horizontal pixel placement of start of line.
     * @param y1 Vertical pixel placement of start of line.
     * @param x2 Horizontal pixel placement of end of line.
     * @param y2 Vertical pixel placement of end of line.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void addLine(int x1, int y1, int x2, int y2,
                        LinkProperties properties) throws IOException {
        LinkLine.write(x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a line in the response.
     * 
     * @param lat_1 latitude of placement of line.
     * @param lon_1 longitude of placement of line.
     * @param x1 Horizontal pixel offset of start of line.
     * @param y1 Vertical pixel offset of start of line.
     * @param x2 Horizontal pixel offset of end of line.
     * @param y2 Vertical pixel offset of end of line.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void addLine(float lat_1, float lon_1, int x1, int y1, int x2,
                        int y2, LinkProperties properties) throws IOException {
        LinkLine.write(lat_1, lon_1, x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param ii ImageIcon to place on the map..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, ImageIcon ii,
                          LinkProperties properties) throws IOException,
            InterruptedException {
        LinkRaster.write(lt, ln, ii, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param ii ImageIcon to place on the map..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(int x1, int y1, ImageIcon ii,
                          LinkProperties properties) throws IOException,
            InterruptedException {
        LinkRaster.write(x1, y1, ii, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param ii ImageIcon to place on the map..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int offset_x1, int offset_y1,
                          ImageIcon ii, LinkProperties properties)
            throws IOException, InterruptedException {
        LinkRaster.write(lt, ln, offset_x1, offset_y1, ii, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param image_width width of raster.
     * @param image_height height of raster.
     * @param image the java.awt.Image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, Image image, int image_width,
                          int image_height, LinkProperties properties)
            throws IOException, InterruptedException {
        LinkRaster.write(lt,
                ln,
                image,
                image_width,
                image_height,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param image Image to place on map.
     * @param image_width width of image.
     * @param image_height height of image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(int x1, int y1, Image image, int image_width,
                          int image_height, LinkProperties properties)
            throws IOException, InterruptedException {
        LinkRaster.write(x1,
                y1,
                image,
                image_width,
                image_height,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param image Image to place on map.
     * @param image_width width of image.
     * @param image_height height of image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int offset_x1, int offset_y1,
                          Image image, int image_width, int image_height,
                          LinkProperties properties) throws IOException,
            InterruptedException {
        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                image,
                image_width,
                image_height,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param w width of raster.
     * @param h height of raster.
     * @param pix integer image pixel data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int w, int h, int[] pix,
                          LinkProperties properties) throws IOException {
        LinkRaster.write(lt, ln, w, h, pix, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param w width of raster.
     * @param h height of raster.
     * @param pix integer image pixel data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(int x1, int y1, int w, int h, int[] pix,
                          LinkProperties properties) throws IOException {
        LinkRaster.write(x1, y1, w, h, pix, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param w width of raster.
     * @param h height of raster.
     * @param pix integer image pixel data.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int offset_x1, int offset_y1,
                          int w, int h, int[] pix, LinkProperties properties)
            throws IOException {
        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                w,
                h,
                pix,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param url the url to download image from.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, String url,
                          LinkProperties properties) throws IOException {
        LinkRaster.write(lt, ln, url, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param url the url to download the image from.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(int x1, int y1, String url, LinkProperties properties)
            throws IOException {
        LinkRaster.write(x1, y1, url, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param url the url to download the image from.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int offset_x1, int offset_y1,
                          String url, LinkProperties properties)
            throws IOException {
        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                url,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param w width of image.
     * @param h height of image.
     * @param bytes the image data, indexes into the colortable.
     * @param colorTable RGB integers representing colortable of image.
     * @param trans the transparency of image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int w, int h, byte[] bytes,
                          Color[] colorTable, int trans,
                          LinkProperties properties) throws IOException {
        LinkRaster.write(lt,
                ln,
                w,
                h,
                bytes,
                colorTable,
                trans,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param w width of image.
     * @param h height of image.
     * @param bytes the image data, indexes into the colortable.
     * @param colorTable RGB integers representing colortable of image.
     * @param trans the transparency of image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(int x1, int y1, int w, int h, byte[] bytes,
                          Color[] colorTable, int trans,
                          LinkProperties properties) throws IOException {
        LinkRaster.write(x1,
                y1,
                w,
                h,
                bytes,
                colorTable,
                trans,
                properties,
                link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param w width of image.
     * @param h height of image.
     * @param bytes the image data, indexes into the colortable.
     * @param colorTable RGB integers representing colortable of image.
     * @param trans the transparency of image.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void addRaster(float lt, float ln, int offset_x1, int offset_y1,
                          int w, int h, byte[] bytes, Color[] colorTable,
                          int trans, LinkProperties properties)
            throws IOException {
        LinkRaster.write(lt,
                ln,
                offset_x1,
                offset_y1,
                w,
                h,
                bytes,
                colorTable,
                trans,
                properties,
                link.dos);
    }

    /**
     * Write a rectangle in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of rectangle.
     * @param ln1 longitude of placement of upper left corner of rectangle.
     * @param lt2 latitude of placement of lower right corner of rectangle.
     * @param ln2 longitude of placement of lower right corner of rectangle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void addRectangle(float lt1, float ln1, float lt2, float ln2,
                             int lType, LinkProperties properties)
            throws IOException {
        LinkRectangle.write(lt1, ln1, lt2, ln2, lType, properties, link.dos);
    }

    /**
     * Write a rectangle in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of rectangle.
     * @param ln1 longitude of placement of upper left corner of rectangle.
     * @param lt2 latitude of placement of lower right corner of rectangle.
     * @param ln2 longitude of placement of lower right corner of rectangle.
     * @param lType the line type to use for the rectangle - straight, rhumb,
     *        great circle.
     * @param nsegs number of segments to use to approximate curved rectangle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void addRectangle(float lt1, float ln1, float lt2, float ln2,
                             int lType, int nsegs, LinkProperties properties)
            throws IOException {
        LinkRectangle.write(lt1,
                ln1,
                lt2,
                ln2,
                lType,
                nsegs,
                properties,
                link.dos);
    }

    /**
     * Write a rectangle in the response.
     * 
     * @param x1 Horizontal pixel location of upper left corner of rectangle..
     * @param y1 Vertical pixel location of upper left corner of rectangle.
     * @param x2 Horizontal pixel location of lower right corner of rectangle..
     * @param y2 Vertical pixel location of lower right corner of rectangle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void addRectangle(int x1, int y1, int x2, int y2,
                             LinkProperties properties) throws IOException {
        LinkRectangle.write(x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a rectangle in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of rectangle.
     * @param ln1 longitude of placement of upper left corner of rectangle..
     * @param x1 Horizontal pixel offset of upper left corner of rectangle.
     * @param y1 Vertical pixel offset of upper left corner of rectangle.
     * @param x2 Horizontal pixel offset of lower right corner of rectangle.
     * @param y2 Vertical pixel offset of lower right corner of rectangle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void addRectangle(float lt1, float ln1, int x1, int y1, int x2,
                             int y2, LinkProperties properties)
            throws IOException {
        LinkRectangle.write(lt1, ln1, x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of point.
     * @param ln1 longitude of placement of upper left corner of point.
     * @param radius the pixel radius size of the point.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void addPoint(float lt1, float ln1, int radius,
                         LinkProperties properties) throws IOException {
        LinkPoint.write(lt1, ln1, radius, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param x1 Horizontal pixel location of upper left corner of point..
     * @param y1 Vertical pixel location of upper left corner of point.
     * @param radius Pixel radius of the point.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void addPoint(int x1, int y1, int radius, LinkProperties properties)
            throws IOException {
        LinkPoint.write(x1, y1, radius, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of point.
     * @param ln1 longitude of placement of upper left corner of point..
     * @param x1 Horizontal pixel offset of upper left corner of point.
     * @param y1 Vertical pixel offset of upper left corner of point.
     * @param radius Pixel radius of the point.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void addPoint(float lt1, float ln1, int x1, int y1, int radius,
                         LinkProperties properties) throws IOException {
        LinkPoint.write(lt1, ln1, x1, y1, radius, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param llPoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(double[] llPoints, int units, int lType,
                        LinkProperties properties) throws IOException {
        LinkPoly.write(llPoints, units, lType, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param llpoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param nsegs number of segments to use to approximate curved poly lines..
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(double[] llpoints, int units, int lType, int nsegs,
                        LinkProperties properties) throws IOException {
        LinkPoly.write(llpoints, units, lType, nsegs, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param xypoints alternating x and y pixel locations of poly.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(int[] xypoints, LinkProperties properties)
            throws IOException {
        LinkPoly.write(xypoints, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param xpoints horizontal pixel locations of poly.
     * @param ypoints vertical pixel locations of poly.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(int[] xpoints, int[] ypoints, LinkProperties properties)
            throws IOException {
        LinkPoly.write(xpoints, ypoints, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param latPoint the latitude anchor point of the poly.
     * @param lonPoint the longitude anchor point of the poly.
     * @param xypoints alternating x and y offset polygon points.
     * @param cMode Coordinate Mode (Origin or Previous) that indicate whether
     *        the x and y points are relative to the first point, or to the
     *        previous point. .
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(float latPoint, float lonPoint, int[] xypoints,
                        int cMode, LinkProperties properties)
            throws IOException {
        LinkPoly.write(latPoint,
                lonPoint,
                xypoints,
                cMode,
                properties,
                link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param latPoint the latitude anchor point of the poly.
     * @param lonPoint the longitude anchor point of the poly.
     * @param xpoints horizontal pixel offset polygon points.
     * @param ypoints vertical pixel offset polygon points.
     * @param cMode Coordinate Mode (Origin or Previous) that indicate whether
     *        the x and y points are relative to the first point, or to the
     *        previous point. .
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void addPoly(float latPoint, float lonPoint, int[] xpoints,
                        int[] ypoints, int cMode, LinkProperties properties)
            throws IOException {
        LinkPoly.write(latPoint,
                lonPoint,
                xpoints,
                ypoints,
                cMode,
                properties,
                link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param latPoint latitude of placement of text.
     * @param lonPoint longitude of placement of text.
     * @param stuff the text.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(float latPoint, float lonPoint, String stuff,
                        int justify, LinkProperties properties)
            throws IOException {
        LinkText.write(latPoint,
                lonPoint,
                stuff,
                LinkText.DEFAULT_FONT,
                justify,
                properties,
                link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param x Horizontal pixel location of text.
     * @param y Vertical pixel location of text.
     * @param stuff the text.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(int x, int y, String stuff, int justify,
                        LinkProperties properties) throws IOException {
        LinkText.write(x,
                y,
                stuff,
                LinkText.DEFAULT_FONT,
                justify,
                properties,
                link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param latPoint latitude of text placement.
     * @param lonPoint longitude of text placement.
     * @param offset_x Horizontal pixel offset of text.
     * @param offset_y Vertical pixel offset of text.
     * @param stuff the text.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(float latPoint, float lonPoint, int offset_x,
                        int offset_y, String stuff, int justify,
                        LinkProperties properties) throws IOException {
        LinkText.write(latPoint,
                lonPoint,
                offset_x,
                offset_y,
                stuff,
                LinkText.DEFAULT_FONT,
                justify,
                properties,
                link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param latPoint latitude of placement of text.
     * @param lonPoint longitude of placement of text.
     * @param stuff the text.
     * @param font a text representation of the font.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(float latPoint, float lonPoint, String stuff,
                        String font, int justify, LinkProperties properties)
            throws IOException {
        LinkText.write(latPoint,
                lonPoint,
                stuff,
                font,
                justify,
                properties,
                link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param x Horizontal pixel location of text.
     * @param y Vertical pixel location of text.
     * @param stuff the text.
     * @param font a text representation of the font.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(int x, int y, String stuff, String font, int justify,
                        LinkProperties properties) throws IOException {
        LinkText.write(x, y, stuff, font, justify, properties, link.dos);
    }

    /**
     * Write a text in the response.
     * 
     * @param latPoint latitude of text placement.
     * @param lonPoint longitude of text placement.
     * @param offset_x Horizontal pixel offset of text.
     * @param offset_y Vertical pixel offset of text.
     * @param stuff the text.
     * @param font a text representation of the font.
     * @param justify place the text left, right or centered on location.
     * @param properties Properties containing attributes.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void addText(float latPoint, float lonPoint, int offset_x,
                        int offset_y, String stuff, String font, int justify,
                        LinkProperties properties) throws IOException {
        LinkText.write(latPoint,
                lonPoint,
                offset_x,
                offset_y,
                stuff,
                font,
                justify,
                properties,
                link.dos);
    }

}
