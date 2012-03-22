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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkActionList.java,v $
// $RCSfile: LinkActionList.java,v $
// $Revision: 1.11 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.Color;
import java.awt.Image;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The LinkActionList carries information about actions that the client should
 * perform in response to a gesture query. The possible actions include URLs and
 * HTML text to display in a browser, information to display in a pop-up window
 * or status display, or changes to make to graphics.
 */
public class LinkActionList implements LinkActionConstants,
        LinkPropertiesConstants {

    /** Link used for the transmission/reception of actions. */
    protected Link link = null;
    /** The terminator of the gesture section when receiving actions. */
    protected String linkStatus = Link.END_TOTAL;
    /** */
    protected boolean reacted = false;
    /**
     * This flag tells whether properties have been received that note how to
     * update the map.
     */
    protected boolean mapUpdate = false;
    /** Use these properties to set the map */
    protected LinkProperties mapProperties;
    /**
     * Instead of allocating a new empty vector for each gesture response, even
     * if there isn't a graphic update, use this to return an empty vector.
     */
    protected static Vector emptyGraphicUpdates = new Vector();
    /** Graphics list received. */
    protected Vector updates = null;
    /** Version Number of request format. */
    protected static float version = Link.LINK_VERSION;
    /** The properties returned for this list. */
    protected LinkProperties properties;

    /**
     * Write a gesture response section to the link, from the server side.
     * 
     * @param link the Link to write to.
     * @throws IOException
     */
    public LinkActionList(Link link, LinkProperties properties)
            throws IOException {
        this.link = link;
        link.start(Link.ACTIONS_HEADER);
        link.dos.writeFloat(version);
        properties.write(link);
    }

    /**
     * Read the gesture section off the link, from the client.
     * 
     * @param link the link to read from.
     * @param layer the client layer.
     * @param proj the projection to use on graphic updates.
     * @param generator an OMGridGenerator that knows how to render grid
     *        objects.
     * @throws IOException
     * @throws EOFException
     */
    public LinkActionList(Link link, Layer layer, Projection proj,
            OMGridGenerator generator) throws IOException, EOFException {
        this.link = link;
        linkStatus = readGestureResponses(layer, proj, generator);
    }

    /**
     * After reading the gesture response, this returns the section ending
     * string terminating the gesture section, either Link.END_TOTAL or
     * Link.END_SECTION.
     * 
     * @return either Link.END_TOTAL or Link.END_SECTION.
     */
    public String getLinkStatus() {
        return linkStatus;
    }

    /**
     * Get the properties for the LinkActionList. Any information messages can
     * be picked up from within the properties - html, URL, messages, text and
     * information lines.
     * 
     * @return properties
     */
    public LinkProperties getProperties() {
        return properties;
    }

    /**
     * Get the properties for the map update.
     * 
     * @return mapProperties
     */
    public LinkProperties getMapProperties() {
        return mapProperties;
    }

    /**
     * Return all the graphic updates that came across the link, in the form of
     * a Vector of LinkActionList.GraphicUpdate objects.
     * 
     * @return Vector of GraphicUpdate objects.
     */
    public Vector getGraphicUpdates() {
        if (updates == null) {
            return emptyGraphicUpdates;
        } else {
            return updates;
        }
    }

    /**
     * The server method that needs to be called at the end of sending a gesture
     * response. This will tell the link what type of teminator to put on the
     * end of the gesture response section, and also tell the link to fluxh the
     * output stream..
     * 
     * @param endType use Link.END_SECTION if you want to add more types of
     *        responses. Use Link.END_TOTAL at the end of the total
     *        transmission.
     * @throws IOException
     */
    public void end(String endType) throws IOException {
        link.end(endType);
    }

    /**
     * If a ACTIONS_HEADER has been encountered coming off the link, then this
     * method should be called to read the string of gesture that follows. The
     * gestures are read and reacted to directly - the responses that involve
     * the Information Delegator are handled here, and all graphic updates are
     * stored in the graphicUpdates Vector. Assumes the ACTIONS_HEADER has been
     * read.
     * 
     * @param layer the layer that wants the gesture results..
     * @param proj If you want the gesture graphic to be projected as it comes
     *        off the wire, add a projection here. Otherwise, use null.
     * @param generator an OMGridGenerator that knows how to render grid
     *        objects.
     * @return Link.END_TOTAL or Link.END_SECTION, depending on how the section
     *         ends.
     * @throws IOException
     * @throws EOFException
     */
    protected String readGestureResponses(Layer layer, Projection proj,
                                          OMGridGenerator generator)
            throws IOException, EOFException {

        long startTime = System.currentTimeMillis();
        String header = null;
        int gestureType;

        float ver = link.dis.readFloat();

        if (ver != version) {
            if (ver == .1) {// Big difference....
                throw new IOException("LinkActionList: Versions do not match! DANGER!");
            } else {
                Debug.message("link", "LinkActionList: Versions do not match");
            }
        }

        if (properties != null) {
            properties.clear();
        }
        
        properties = LinkProperties.read(link.dis, properties);
        LinkProperties graphicProperties = new LinkProperties();

        Debug.message("link", "LinkActionList: reading actions:");

        while (true) {
            // Just consume the header, don't create a useless
            // string object.
            header = link.readDelimiter(false);

            if (header == Link.END_TOTAL || header == Link.END_SECTION) {

                long endTime = System.currentTimeMillis();
                if (Debug.debugging("link")) {
                    Debug.output("LinkActionList: received in "
                            + (float) (endTime - startTime) / 1000.0f
                            + " seconds");
                }

                return header;
            }

            gestureType = link.dis.readByte();

            switch (gestureType) {
            case ACTION_GRAPHICS:
                int graphicAction = link.dis.readInt();

                if (updates == null) {
                    updates = new Vector();
                }

                if (LinkUtil.isMask(graphicAction, UPDATE_ADD_GRAPHIC_MASK)
                        || LinkUtil.isMask(graphicAction, UPDATE_GRAPHIC_MASK)) {
                    updates.addElement(readGraphic(graphicAction, graphicProperties,
                            proj,
                            generator));
                } else {
                    graphicProperties = LinkProperties.read(link.dis, graphicProperties);
                    updates.addElement(new GraphicUpdate(graphicAction, graphicProperties.getProperty(LPC_GRAPHICID)));
                }
                reacted = true;
                break;
            case ACTION_GUI:
                break;
            case ACTION_MAP:
                mapUpdate = true;
                if (mapProperties != null) {
                    mapProperties.clear();
                }
                mapProperties = LinkProperties.read(link.dis, mapProperties);
                break;
            default:
                System.err.println("LinkActionList: received unknown gesture type.");
            }
        }
    }

    /**
     * Read a graphic's particulars, for upates and additions. Assumes that the
     * gesture type and graphic action has been read.
     * 
     * @param graphicAction the action to take on the graphic.
     * @param proj the projection to apply to the graphic.
     * @param generator an OMGridGenerator that knows how to render grid
     *        objects.
     * @throws IOException.
     */
    protected GraphicUpdate readGraphic(int graphicAction, LinkProperties graphicProperties, Projection proj,
                                        OMGridGenerator generator)
            throws IOException {

        OMGraphic graphic = null;
        String header = link.readDelimiter(false);

        // Sanity check
        if (header == Link.END_TOTAL || header == Link.END_SECTION) {
            return null;
        }

        int graphicType = link.dis.readByte();

        switch (graphicType) {
        case LinkGraphicList.GRAPHICTYPE_LINE:
            graphic = LinkLine.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_POLY:
            graphic = LinkPoly.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_RECTANGLE:
            graphic = LinkRectangle.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_POINT:
            graphic = LinkPoint.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_CIRCLE:
            graphic = LinkCircle.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_ELLIPSE:
            graphic = LinkEllipse.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_RASTER:
            graphic = LinkRaster.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_BITMAP:
            graphic = LinkBitmap.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_TEXT:
            graphic = LinkText.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_GRID:
            graphic = LinkGrid.read(link.dis, graphicProperties);
            break;
        case LinkGraphicList.GRAPHICTYPE_ARC:
            graphic = LinkArc.read(link.dis, graphicProperties);
            break;
        default:
            System.err.println("LinkActionList: received unknown graphic type.");
        }

        if (graphic != null && proj != null) {
            if (graphic instanceof OMGrid) {
                ((OMGrid) graphic).setGenerator(generator);
            }
            graphic.generate(proj);
        }

        return (new GraphicUpdate(graphicAction, graphic));
    }

    /** Returns true if the gesture was consumed by the server. */
    public boolean consumedGesture() {
        return reacted;
    }

    /** Returns true if a map update command was given. */
    public boolean getNeedMapUpdate() {
        return mapUpdate;
    }

    /**
     * Sets whether a set of MapUpdate parameters needs to be fetched. Should be
     * reset to false after the map projection has been updated.
     */
    public void setNeedMapUpdate(boolean value) {
        mapUpdate = value;
    }

    /**
     * The server method for writing a request to the client to deselect all the
     * graphics.
     * 
     * @throws IOException.
     */
    public void deselectGraphics() throws IOException {
        link.dos.write(Link.UPDATE_GRAPHICS.getBytes());
        link.dos.writeByte(ACTION_GRAPHICS);
        link.dos.writeInt(MODIFY_DESELECTALL_GRAPHIC_MASK);
        LinkProperties.EMPTY_PROPERTIES.write(link.dos); // Write
        // empty
    }

    /**
     * Server can use this method to modify a graphic with an action described
     * by the MODIFY mask values. Assumes that you don't want to update or add a
     * graphic, because that requires the graphic being added.
     * 
     * @param maskDescription an integer with the applicable MODIFY_MASKS set.
     * @param props property list containing the identifier used by the client
     *        to know which graphic to modify.
     * @throws IOException
     */
    public void modifyGraphic(int maskDescription, LinkProperties props)
            throws IOException {
        link.dos.write(Link.UPDATE_GRAPHICS.getBytes());
        link.dos.writeByte(ACTION_GRAPHICS);
        link.dos.writeInt(maskDescription);
        props.write(link.dos);
    }

    /**
     * Used by the graphic methods to write the correct mask and graphic header
     * when a graphic is updated. Should not be called directly unless you
     * understand the protocol.
     * 
     * @param graphicUpdateMask the masked integer to describe the action on the
     *        graphic.
     * @throws IOException
     */
    public void writeGraphicGestureHeader(int graphicUpdateMask)
            throws IOException {
        link.dos.write(Link.UPDATE_GRAPHICS.getBytes());
        link.dos.writeByte(ACTION_GRAPHICS);
        link.dos.writeInt(graphicUpdateMask);
    }

    /**
     * Update an arc with lat/lon placement.
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
    public void updateArc(float latPoint, float lonPoint, int w, int h,
                          float s, float e, LinkProperties properties,
                          int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Update an arc with x/y placement.
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
    public void updateArc(int x1, int y1, int w, int h, float s, float e,
                          LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
    public void updateArc(float latPoint, float lonPoint, int offset_x1,
                          int offset_y1, int w, int h, float s, float e,
                          LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Update an arc with a certain radius at a Lat/Lon location. Assumes the
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
    public void updateArc(float latPoint, float lonPoint, float radius,
                          float s, float e, LinkProperties properties,
                          int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Update an arc with a certain radius at a Lat/Lon location, and allows you
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
    public void updateArc(float latPoint, float lonPoint, float radius,
                          int units, float s, float e,
                          LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Update an arc with a certain radius at a Lat/Lon location, and allows you
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
    public void updateArc(float latPoint, float lonPoint, float radius,
                          int units, int nverts, float s, float e,
                          LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Update the bitmap.
     * 
     * @param lt latitude of placement of upper left corner of bitmap.
     * @param ln longitude of placement of upper left corner of bitmap.
     * @param w pixel width of bitmap.
     * @param h pixel height of bitmap.
     * @param bytes bitmap data.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void updateBitmap(float lt, float ln, int w, int h, byte[] bytes,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkBitmap.write(lt, ln, w, h, bytes, properties, link.dos);
    }

    /**
     * Update the bitmap.
     * 
     * @param x1 horizontal placement of upper left corner of bitmap.
     * @param y1 vertical placement of upper left corner of bitmap.
     * @param w pixel width of bitmap.
     * @param h pixel height of bitmap.
     * @param bytes bitmap data.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void updateBitmap(int x1, int y1, int w, int h, byte[] bytes,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkBitmap
     */
    public void updateBitmap(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, byte[] bytes,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(float latPoint, float lonPoint, int w, int h,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkCircle.write(latPoint, lonPoint, w, h, properties, link.dos);
    }

    /**
     * Write a circle in the response.
     * 
     * @param x1 horizontal pixel placement of center of circle..
     * @param y1 vertical pixel placement of center of circle..
     * @param w pixel width of circle.
     * @param h pixel height of circle.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(int x1, int y1, int w, int h,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(float latPoint, float lonPoint, int offset_x1,
                             int offset_y1, int w, int h,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(float latPoint, float lonPoint, float radius,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(float latPoint, float lonPoint, float radius,
                             int units, LinkProperties properties,
                             int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkCircle
     */
    public void updateCircle(float latPoint, float lonPoint, float radius,
                             int units, int nverts, LinkProperties properties,
                             int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkCircle.write(latPoint,
                lonPoint,
                radius,
                units,
                nverts,
                properties,
                link.dos);
    }

    /**
     * Write an ellipse in the response.
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
    public void updateEllipse(float latPoint, float lonPoint,
                              float majorAxisSpan, float minorAxisSpan,
                              int units, float rotationAngle,
                              LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Write an ellipse in the response.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param majorAxisSpan horizontal diameter of circle/ellipse, pixels
     * @param minorAxisSpan vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians *
     * @param properties the LinkProperties describing the attributes of the
     *        ellipse.
     */
    public void updateEllipse(int x1, int y1, int majorAxisSpan,
                              int minorAxisSpan, float rotateAngle,
                              LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkEllipse.write(x1,
                y1,
                majorAxisSpan,
                minorAxisSpan,
                rotateAngle,
                properties,
                link.dos);
    }

    /**
     * Write an ellipse in the response.
     * 
     * @param latPoint latitude of center of circle in decimal degrees
     * @param lonPoint longitude of center of circle in decimal degrees
     * @param w horizontal diameter of circle/ellipse, pixels
     * @param h vertical diameter of circle/ellipse, pixels
     * @param rotateAngle angle of rotation in Radians
     * @param properties the LinkProperties describing the attributes of the
     *        ellipse.
     */
    public void updateEllipse(float latPoint, float lonPoint, int w, int h,
                              float rotateAngle, LinkProperties properties,
                              int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkEllipse.write(latPoint,
                lonPoint,
                w,
                h,
                rotateAngle,
                properties,
                link.dos);
    }

    /**
     * Write an ellipse in the response.
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
    public void updateEllipse(float latPoint, float lonPoint, int offset_x1,
                              int offset_y1, int w, int h, float rotateAngle,
                              LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Lat/Lon placement grid.
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @see com.bbn.openmap.layer.link.LinkGrid
     * @throws IOException
     */
    public void updateGrid(float lt, float ln, int rows, int columns,
                           float orientation, float vResolution,
                           float hResolution, int major, int[] data,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * XY placement grid.
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @see com.bbn.openmap.layer.link.LinkGrid
     * @throws IOException
     */
    public void updateGrid(int x1, int y1, int rows, int columns,
                           float orientation, float vResolution,
                           float hResolution, int major, int[] data,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * Lat/lon placement with XY offset.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution pixels/point between rows of the grid.
     * @param hResolution pixels/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @see com.bbn.openmap.layer.link.LinkGrid
     * @throws IOException
     */
    public void updateGrid(float lt, float ln, int offset_x1, int offset_y1,
                           int rows, int columns, float orientation,
                           float vResolution, float hResolution, int major,
                           int[] data, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void updateLine(float lat_1, float lon_1, float lat_2, float lon_2,
                           int lineType, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void updateLine(float lat_1, float lon_1, float lat_2, float lon_2,
                           int lineType, int nsegs, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void updateLine(int x1, int y1, int x2, int y2,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkLine
     */
    public void updateLine(float lat_1, float lon_1, int x1, int y1, int x2,
                           int y2, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkLine.write(lat_1, lon_1, x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of raster.
     * @param ln longitude of placement of upper left corner of raster.
     * @param ii ImageIcon to place on the map..
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, ImageIcon ii,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException, InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRaster.write(lt, ln, ii, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param ii ImageIcon to place on the map..
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(int x1, int y1, ImageIcon ii,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException, InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int offset_x1, int offset_y1,
                             ImageIcon ii, LinkProperties properties,
                             int graphicUpdateMask) throws IOException,
            InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRaster.write(lt, ln, offset_x1, offset_y1, ii, properties, link.dos);
    }

    /**
     * Write a bitmap in the response.
     * 
     * @param lt latitude of placement of upper left corner of bitmap.
     * @param ln longitude of placement of upper left corner of bitmap.
     * @param image_width width of bitmap.
     * @param image_height height of bitmap.
     * @param image the java.awt.Image.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, Image image, int image_width,
                             int image_height, LinkProperties properties,
                             int graphicUpdateMask) throws IOException,
            InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(int x1, int y1, Image image, int image_width,
                             int image_height, LinkProperties properties,
                             int graphicUpdateMask) throws IOException,
            InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @throws InterruptedException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int offset_x1, int offset_y1,
                             Image image, int image_width, int image_height,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException, InterruptedException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int w, int h, int[] pix,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(int x1, int y1, int w, int h, int[] pix,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRaster.write(x1, y1, w, h, pix, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param lt latitude of placement of upper left corner of bitmap.
     * @param ln longitude of placement of upper left corner of bitmap.
     * @param offset_x1 horizontal pixel offset of upper left corner of raster.
     * @param offset_y1 vertical pixel offset of upper left corner of raster.
     * @param w width of raster.
     * @param h height of raster.
     * @param pix integer image pixel data.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, int[] pix,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, String url,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRaster.write(lt, ln, url, properties, link.dos);
    }

    /**
     * Write a raster in the response.
     * 
     * @param x1 horizontal pixel location of upper left corner of raster.
     * @param y1 vertical pixel location of upper left corner of raster.
     * @param url the url to download the image from.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(int x1, int y1, String url,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int offset_x1, int offset_y1,
                             String url, LinkProperties properties,
                             int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int w, int h, byte[] bytes,
                             Color[] colorTable, int trans,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(int x1, int y1, int w, int h, byte[] bytes,
                             Color[] colorTable, int trans,
                             LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRaster
     */
    public void updateRaster(float lt, float ln, int offset_x1, int offset_y1,
                             int w, int h, byte[] bytes, Color[] colorTable,
                             int trans, LinkProperties properties,
                             int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param lType the line type to use for the rectangle - straight, rhumb,
     *        great circle.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void updateRectangle(float lt1, float ln1, float lt2, float ln2,
                                int lType, LinkProperties properties,
                                int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void updateRectangle(float lt1, float ln1, float lt2, float ln2,
                                int lType, int nsegs,
                                LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param x1 Horizontal pixel offset of upper left corner of rectangle..
     * @param y1 Vertical pixel offset of upper left corner of rectangle.
     * @param x2 Horizontal pixel offset of lower right corner of rectangle..
     * @param y2 Vertical pixel offset of lower right corner of rectangle.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void updateRectangle(int x1, int y1, int x2, int y2,
                                LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRectangle.write(x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a rectangle in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of bitmap.
     * @param ln1 longitude of placement of upper left corner of bitmap.
     * @param x1 Horizontal pixel offset of upper left corner of rectangle..
     * @param y1 Vertical pixel offset of upper left corner of rectangle.
     * @param x2 Horizontal pixel offset of lower right corner of rectangle..
     * @param y2 Vertical pixel offset of lower right corner of rectangle.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkRectangle
     */
    public void updateRectangle(float lt1, float ln1, int x1, int y1, int x2,
                                int y2, LinkProperties properties,
                                int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkRectangle.write(lt1, ln1, x1, y1, x2, y2, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param lt1 latitude of point.
     * @param ln1 longitude of point.
     * @param radius the radius of the point graphic.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void updatePoint(float lt1, float ln1, int radius,
                            LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoint.write(lt1, ln1, radius, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param x1 the horizontal screen pixel location of the point.
     * @param y1 the vertical screen pixel location of the point.
     * @param radius the pixel radius size of the point.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void updatePoint(int x1, int y1, int radius,
                            LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoint.write(x1, y1, radius, properties, link.dos);
    }

    /**
     * Write a point in the response.
     * 
     * @param lt1 latitude of placement of upper left corner of bitmap.
     * @param ln1 longitude of placement of upper left corner of bitmap.
     * @param x1 Horizontal pixel offset of upper left corner of point..
     * @param y1 Vertical pixel offset of upper left corner of point.
     * @param radius the pixel size of the point.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoint
     */
    public void updatePoint(float lt1, float ln1, int x1, int y1, int radius,
                            LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoint.write(lt1, ln1, x1, y1, radius, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param llPoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(double[] llPoints, int units, int lType,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoly.write(llPoints, units, lType, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param llpoints alternating latitude and longitude points of poly.
     * @param units degrees or radians.
     * @param lType straight, rhumb, great circle.
     * @param nsegs number of segments to use to approximate curved poly lines..
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(double[] llpoints, int units, int lType, int nsegs,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoly.write(llpoints, units, lType, nsegs, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param xypoints alternating x and y pixel locations of poly.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(int[] xypoints, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
        LinkPoly.write(xypoints, properties, link.dos);
    }

    /**
     * Write a poly in the response.
     * 
     * @param xpoints horizontal pixel locations of poly.
     * @param ypoints vertical pixel locations of poly.
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(int[] xpoints, int[] ypoints,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(float latPoint, float lonPoint, int[] xypoints,
                           int cMode, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkPoly
     */
    public void updatePoly(float latPoint, float lonPoint, int[] xpoints,
                           int[] ypoints, int cMode, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(float latPoint, float lonPoint, String stuff,
                           int justify, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(int x, int y, String stuff, int justify,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(float latPoint, float lonPoint, int offset_x,
                           int offset_y, String stuff, int justify,
                           LinkProperties properties, int graphicUpdateMask)
            throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(float latPoint, float lonPoint, String stuff,
                           String font, int justify, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(int x, int y, String stuff, String font,
                           int justify, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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
     * @param properties description of drawing attributes.
     * @param graphicUpdateMask the mask describing the graphic update.
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateText(float latPoint, float lonPoint, int offset_x,
                           int offset_y, String stuff, String font,
                           int justify, LinkProperties properties,
                           int graphicUpdateMask) throws IOException {
        writeGraphicGestureHeader(graphicUpdateMask);
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

    /**
     * Write an OMGraphic to the response.
     * 
     * @throws IOException
     * @see com.bbn.openmap.layer.link.LinkText
     */
    public void updateGraphic(OMGraphic omGraphic, int graphicUpdateMask)
            throws IOException {

    }

}
