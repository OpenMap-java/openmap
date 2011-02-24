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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/Link.java,v $
// $RCSfile: Link.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.logging.Level;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;

/**
 * A Link is a relationship between Locations, represented as a line between
 * them. If you want to draw the endpoints, use a LocationHandler for them.
 * <P>
 * 
 * Although this class extends from Location, it doesn't handle displaying a
 * name for the link that well. The name will appear as the first node's marker
 * name, although it should declutter if needed.
 */
public class Link
        extends Location {

    /*
     * The other endpoints of the link, in decimal degrees. The first endpoints
     * are contained in the Location superclass.
     */
    /** The lat of point 2. */
    public double lat2 = 0.0f;
    /** The lon of point 2. */
    public double lon2 = 0.0f;
    /** The x of point 2. */
    public int x2 = 0;
    /** The y of point 2. */
    public int y2 = 0;

    // /** The x offset of point 2, never used. */
    // public int xOffset2 = 0;
    // /** The y offset of point 2, never used. */
    // public int yOffset2 = 0;

    /** the default color is black */
    public static Color DEFAULT_COLOR = Color.black;
    /** the default dash style - not dashed */
    public static boolean DEFAULT_DASHED = false;
    /** the default line type - straight */
    public static int DEFAULT_LINETYPE = OMGraphic.LINETYPE_STRAIGHT;
    /** the default line thickness - 1 */
    public static float DEFAULT_THICKNESS = 1.0f;

    /**
     * A plain constructor if you are planning on setting everything yourself.
     */
    public Link() {
    }

    /**
     * Construct a Link with the given attributes
     * 
     * @param lat1 latitude of start-point
     * @param lon1 longitude of start-point
     * @param lat2 latitude of end-point
     * @param lon2 longitude of endpoint
     * @param details A string that gives information about this link
     * @param paint the link's displayed edge java.awt.Paint (Color).
     * @param dashed Is it a dashed line?
     * @param thickness The line thickness.
     * @param linetype LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE, LINETYPE_RHUMB
     */
    public Link(double lat1, double lon1, double lat2, double lon2, String details, Paint paint, boolean dashed, float thickness,
                int linetype) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Link(" + lat1 + ", " + lon1 + ", " + lat2 + ", " + lon2 + ", " + details + ", " + paint + ", " + dashed
                    + ", " + thickness + ", " + linetype + ")");
        }

        this.lat = lat1;
        this.lon = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;

        if (details != null) {
            this.details = details;
        } else {
            this.details = "";
        }

        OMLine link = new OMLine(lat1, lon1, lat2, lon2, linetype);
        setLinkDrawingParameters(link, paint, thickness, dashed);
        setLocationMarker(link);
    }

    /**
     * Construct a Link with the given attribute
     * 
     * @param x1 Starting x point of Link
     * @param y1 Starting y point of Link
     * @param x2 End x point of Link
     * @param y2 End y point of Link
     * @param details A string that gives information about this link
     * @param paint the link's displayed edge java.awt.Paint (Color).
     * @param dashed Is it a dashed line?
     * @param thickness The line thickness.
     */
    public Link(int x1, int y1, int x2, int y2, String details, Paint paint, boolean dashed, float thickness) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Link(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")");
        }

        this.x = x1;
        this.y = y1;
        this.x2 = x2;
        this.y2 = y2;

        if (details != null) {
            this.details = details;
        } else {
            this.details = "";
        }

        OMLine link = new OMLine(x1, y1, x2, y2);
        setLinkDrawingParameters(link, paint, thickness, dashed);
        setLocationMarker(link);
    }

    /**
     * Set the drawing attributes of the link
     * 
     * @param link the line used for the link
     * @param paint the line color
     * @param thickness the thickness of the line
     * @param dashed true if the line should be dashed
     */
    public void setLinkDrawingParameters(OMLine link, Paint paint, float thickness, boolean dashed) {
        Stroke stroke;
        if (dashed) {
            // create a basic default dash
            float[] dash = {
                8.0f,
                8.0f
            };
            stroke = new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        } else {
            stroke = new BasicStroke(thickness);
        }

        link.setStroke(stroke);
        link.setLinePaint(paint);
    }

    /**
     * Set the location for the link
     * 
     * @param lat1 the latitude of the first location
     * @param lon1 the longitude of the first location
     * @param lat2 the latitude of the second location
     * @param lon2 the longitude of the second location
     */
    public void setLocation(double lat1, double lon1, double lat2, double lon2) {

        this.lat = lat1;
        this.lon = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;

        OMLine line = (OMLine) getLocationMarker();
        double[] locs = {
            lat1,
            lon1,
            lat2,
            lon2
        };
        line.setLL(locs);
    }

    /**
     * Set new coordinates for this link.
     * 
     * @param x1 the x coordinate of the first location
     * @param y1 the y coordinate of the first location
     * @param x2 the x coordinate of the second location
     * @param y2 the y coordinate of the second location
     */
    public void setLocation(int x1, int y1, int x2, int y2) {

        this.x = x1;
        this.y = y1;
        this.x2 = x2;
        this.y2 = y2;

        int xy[] = new int[4];
        xy[0] = this.x = x1;
        xy[1] = this.y = y1;
        xy[2] = this.x2 = x2;
        xy[3] = this.y2 = y2;
        OMLine link = getLink();
        link.setPts(xy);
        link.setRenderType(RENDERTYPE_XY);
    }

    /** Does nothing - marker handled in setLocation methods. */
    public void setGraphicLocations(double latitude, double longitude) {
    }

    /** Does nothing - marker handled in setLocation methods. */
    public void setGraphicLocations(int x, int y) {
    }

    /** Does nothing - marker handled in setLocation methods. */
    public void setGraphicLocations(double latitude, double longitude, int offsetX, int offsetY) {
    }

    public void setLinkColor(Paint linkPaint) {
        // location is actually the link graphic. getLink() does the
        // proper casting.
        if (location != null) {
            getLink().setLinePaint(linkPaint);
        }
    }

    public void setShowLink(boolean showLinks) {
        showLocation = showLinks;
    }

    public boolean isShowLink() {
        return showLocation;
    }

    public OMLine getLink() {
        return (OMLine) getLocationMarker();
    }
}