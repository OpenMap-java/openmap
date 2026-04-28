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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/Location.java,v $
// $RCSfile: Location.java,v $
// $Revision: 1.12 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.layer.DeclutterMatrix;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;

/**
 * A Location is a place. It can be thought of as a lat/lon place, with or
 * without an pixel offset, or a place on the screen. A location is basically
 * thought of as having a name, which get represented as a label, and some
 * graphical representation. It is abstract because it doesn't really know what
 * kind of markers or labels are being used or how they are being positioned
 * around the particular point. Therefore, it should be extended, and the
 * setGraphicLocations methods implemented to position the marker and text as
 * desired.
 * <P>
 */
public abstract class Location
        extends OMGraphicAdapter {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.location.Location");

    /**
     * The main latitude of object, in decimal degrees, for RENDERTYPE_LATLON
     * and RENDERTYPE_OFFSET locations.
     */
    public double lat = 0.0f;
    /**
     * The main longitude of object, in decimal degrees, for RENDERTYPE_LATLON
     * and RENDERTYPE_OFFSET locations.
     */
    public double lon = 0.0f;
    /**
     * The x pixel offset from the longitude, for RENDERTYPE_OFFSET locations.
     */
    public int xOffset = 0;
    /**
     * The y pixel offset from the latitude, for RENDERTYPE_OFFSET locations.
     */
    public int yOffset = 0;
    /** The x object location, in pixels, for RENDERTYPE_XY locations. */
    public int x = 0;
    /** The y object location, in pixels, for RENDERTYPE_XY locations. */
    public int y = 0;
    /** The name of the location. */
    public String name = null;
    /**
     * The LocationHandler that is handling the location. Need this to check for
     * more global settings for rendering.
     */
    public LocationHandler handler;

    public final static int DECLUTTER_LOCALLY = -1;
    public final static int DECLUTTER_ANYWHERE = -2;

    /** The Label of the object. */
    protected OMText label = null;
    /** The simple location marker of the object. */
    protected OMGraphic location = null;
    /** The URL to display when the object is gestured upon. */
    protected String details = "";
    /** The flag for displaying the location marker. */
    protected boolean showLocation = true;
    /** The flag for displaying the name label. */
    protected boolean showName = true;
    /**
     * The original offset/y location, kept for resetting the placement of the
     * label after decluttering and/or location placement.
     */
    public int origYLabelOffset = 0;
    /**
     * The original offset/x location, kept for resetting the placement of the
     * label after decluttering and/or location placement.
     */
    public int origXLabelOffset = 0;
    /**
     * the default distance away a label should be placed from a location
     * marker.
     */
    public final static int DEFAULT_SPACING = 6;
    /**
     * The pixel limit where the declutter matrix won't draw the name, if it
     * can't put the name at least this close to the original place.
     * DECLUTTER_LOCALLY keeps the limit to twice the height of the label.
     * DECLUTTER_ANYWHERE will place the thing anywhere it fits. Anything else
     * is the pixel limit.
     */
    protected int declutterLimit = DECLUTTER_LOCALLY;
    /** Set whether you want this location label decluttered. */
    protected boolean allowDecluttering = true;
    /**
     * The horizontal pixel distance you want to place the text away from the
     * actual location - to put space between the graphic.
     */
    protected int horizontalLabelBuffer = 0;

    /**
     * A plain constructor if you are planning on setting everything yourself.
     */
    public Location() {
    }

    /**
     * Create a location at a latitude/longitude. If the locationMarker is null,
     * a small rectangle (dot) will be created to mark the location.
     * 
     * @param latitude the latitude, in decimal degrees, of the location.
     * @param longitude the longitude, in decimal degrees, of the location.
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public Location(double latitude, double longitude, String name, OMGraphic locationMarker) {

        setLocation(latitude, longitude);
        this.name = name;

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Location Lat/Lon(" + latitude + ", " + longitude + ", " + name + ")");
        }

        if (locationMarker == null) {
            location = new OMPoint(lat, lon);
        } else {
            location = locationMarker;
        }

        // We can do the x offset off the location here, we'll do the
        // vertical offset later, when we can figure out the height of
        // the text and can line the middle of the text up with the
        // location.

        // If the caller has supplied a substitute graphic for
        // the location spot, it's up to them to horizontally
        // offset the label appropriately. They should do that
        // here, or in an extended class.
        label = new OMText(lat, lon, 0, 0, name, OMText.JUSTIFY_LEFT);
    }

    /**
     * Create a location at a map location. If the locationMarker is null, a
     * small rectangle (dot) will be created to mark the location.
     * 
     * @param x the pixel location of the object from the let of the map.
     * @param y the pixel location of the object from the top of the map
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public Location(int x, int y, String name, OMGraphic locationMarker) {

        setLocation(x, y);
        this.name = name;

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Location XY(" + x + ", " + y + ", " + name + ")");
        }

        if (locationMarker == null) {
            location = new OMPoint(x, y);
        } else {
            location = locationMarker;
        }

        // We can do the x offset off the location here, we'll do the
        // vertical offset later, when we can figure out the height of
        // the text and can line the middle of the text up with the
        // location.

        // If the caller has supplied a substitute graphic for
        // the location spot, it's up to them to horizontally
        // offset the label appropriately. They should do that
        // here, or in an extended class.
        label = new OMText(x, y, name, OMText.JUSTIFY_LEFT);
    }

    /**
     * Create a location at a pixel offset from a latitude/longitude. If the
     * locationMarker is null, a small rectangle (dot) will be created to mark
     * the location.
     * 
     * @param latitude the latitude, in decimal degrees, of the location.
     * @param longitude the longitude, in decimal degrees, of the location.
     * @param xOffset the pixel location of the object from the longitude.
     * @param yOffset the pixel location of the object from the latitude.
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public Location(double latitude, double longitude, int xOffset, int yOffset, String name, OMGraphic locationMarker) {
        setLocation(latitude, longitude, xOffset, yOffset);
        this.name = name;

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Location(" + latitude + ", " + longitude + ", offset " + x + ", " + y + ", " + name + ")");
        }

        if (locationMarker == null) {
            location = new OMPoint(lat, lon, xOffset, yOffset);
        } else {
            location = locationMarker;
        }

        // We can do the x offset off the location here, we'll do the
        // vertical offset later, when we can figure out the height of
        // the text and can line the middle of the text up with the
        // location.

        // If the caller has supplied a substitute graphic for
        // the location spot, it's up to them to horizontally
        // offset the label appropriately. They should do that
        // here, or in an extended class.
        label = new OMText(lat, lon, xOffset, yOffset, name, OMText.JUSTIFY_LEFT);
    }

    /** Set the placement of the location. */
    public void setLocation(double latitude, double longitude) {
        lat = latitude;
        lon = longitude;

        origYLabelOffset = 0;
        origXLabelOffset = DEFAULT_SPACING;

        setRenderType(RENDERTYPE_LATLON);
        if (location != null && label != null) {
            setGraphicLocations(latitude, longitude);
        }
    }

    /** Set the placement of the location. */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;

        origYLabelOffset = y;
        origXLabelOffset = x + DEFAULT_SPACING;

        setRenderType(RENDERTYPE_XY);
        if (location != null && label != null) {
            setGraphicLocations(x, y);
        }
    }

    /** Set the placement of the location. */
    public void setLocation(double latitude, double longitude, int xOffset, int yOffset) {
        lat = latitude;
        lon = longitude;
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        origYLabelOffset = yOffset;
        origXLabelOffset = xOffset + DEFAULT_SPACING;

        setRenderType(RENDERTYPE_OFFSET);
        if (location != null && label != null) {
            setGraphicLocations(latitude, longitude, xOffset, yOffset);
        }
    }

    /**
     * Convenience method that lets you provide a screen x, y and a projection
     * to the location, and let the location hash out how to place itself based
     * on it's rendertype.
     */
    public void setLocation(int x, int y, Projection proj) {
        int renderType = getRenderType();

        switch (renderType) {
            case RENDERTYPE_LATLON:
                if (proj != null) {
                    Point2D llp = proj.inverse(x, y);
                    setLocation((float) llp.getY(), (float) llp.getX());
                } else {
                    logger.fine("Location can't set lat/lon coordinates without a projection");
                }
                break;
            case RENDERTYPE_OFFSET:
                if (proj != null) {
                    Point2D llp = proj.inverse(x, y);
                    setLocation((float) llp.getY(), (float) llp.getX(), this.xOffset, this.yOffset);
                } else {
                    logger.fine("Location can't set lat/lon coordinates without a projection");
                }
                break;
            default:
                setLocation(x, y);
        }
    }

    public abstract void setGraphicLocations(double latitude, double longitude);

    public abstract void setGraphicLocations(int x, int y);

    public abstract void setGraphicLocations(double latitude, double longitude, int offsetX, int offsetY);

    /**
     * Set the location handler for the location.
     */
    public void setLocationHandler(LocationHandler lh) {
        handler = lh;
    }

    /**
     * Get the location handler for the location.
     */
    public LocationHandler getLocationHandler() {
        return handler;
    }

    /**
     * Set the edge java.awt.Paint for the marker graphic.
     */
    public void setLocationPaint(Paint locationPaint) {
        if (location != null) {
            location.setLinePaint(locationPaint);
        }
    }

    /**
     * Get the label for the location.
     */
    public OMText getLabel() {
        return label;
    }

    /**
     * Set the label for the location.
     */
    public void setLabel(OMText lable) {
        label = lable;
    }

    /**
     * Get the location marker for this location.
     */
    public OMGraphic getLocationMarker() {
        return location;
    }

    /**
     * Set the graphic for the location.
     */
    public void setLocationMarker(OMGraphic graphic) {
        location = graphic;
    }

    /**
     * Set whether this location should be shown on an individual basis.
     */
    public void setShowLocation(boolean showLocations) {
        showLocation = showLocations;
    }

    /** See of the location is displaying it's location. */
    public boolean isShowLocation() {
        return showLocation;
    }

    /** Set the location to display it's label. */
    public void setShowName(boolean showNames) {
        showName = showNames;
    }

    /** See if the location is displaying it's label. */
    public boolean isShowName() {
        return showName;
    }

    /** Get the name of the location. */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this location.
     */
    public void setName(String name) {
        this.name = name;
        if (label != null) {
            label.setData(name);
        }
    }

    /**
     * Set the details for the location. This should be the contents to be
     * displayed in a web browser.
     */
    public void setDetails(String det) {
        details = det;
    }

    /**
     * Get the details for the location.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Fire a browser to display the location details.
     */
    public void showDetails() {
        if (details != null) {
            try {
                getLocationHandler().getLayer().fireRequestBrowserContent(details);
            } catch (NullPointerException npe) {

            }
        }
    }

    /**
     * Set whether you want to allow the label for this location to be
     * decluttered.
     * 
     * @param allow if true, label will be decluttered if declutter matrix is
     *        available.
     */
    public void setAllowDecluttering(boolean allow) {
        allowDecluttering = allow;
    }

    /**
     * Get the decluttering allowance setting for this label.
     */
    public boolean isAllowDecluttering() {
        return allowDecluttering;
    }

    /**
     * Set the pixel distance that the label will be moved to the right, to
     * clear space for the graphic marking the location.
     */
    public void setHorizontalLabelBuffer(int buffer) {
        horizontalLabelBuffer = buffer;
    }

    /**
     * Get the pixel distance that the label will be moved to the right, to
     * clear space for the graphic marking the location.
     */
    public int getHorizontalLabelBuffer() {
        return horizontalLabelBuffer;
    }

    // //////////////////////////////////////////////////
    // ///////// OMGraphic methods ////////////////////
    // //////////////////////////////////////////////////

    /**
     * Generate the location, and use the declutter matrix to place the label is
     * a spot so that it doesn't interset with other labels.
     * 
     * @param proj projection of the map.
     * @param declutterMatrix DeclutterMatrix for the map.
     */
    public boolean generate(Projection proj, DeclutterMatrix declutterMatrix) {

        // Call generate(proj) first, to get the original position
        // set. Then, declutter the text.
        boolean ret = generate(proj);

        if (declutterMatrix != null && label != null && allowDecluttering) {
            declutterLabel(declutterMatrix, proj);
        }
        return ret;
    }

    /**
     * Set the pixel distance that us used by the declutter matrix in trying to
     * find a place for the label. If it can't find a place within this pixel
     * limit, it wouldn't draw it.
     */
    public void setDeclutterLimit(int value) {
        if (value < 0 && value != DECLUTTER_LOCALLY) {
            declutterLimit = DECLUTTER_ANYWHERE;
        } else {
            declutterLimit = value;
        }
    }

    /**
     * Get the declutter pixel distance limit.
     */
    public int getDeclutterLimit() {
        return declutterLimit;
    }

    protected int currentFontDescent = 0;

    /**
     * Prepare the graphic for rendering. This must be done before calling
     * <code>render()</code>! If a vector graphic has lat-lon components, then
     * we project these vertices into x-y space. For raster graphics we prepare
     * in a different fashion.
     * <p>
     * If the generate is unsuccessful, it's usually because of some oversight,
     * (for instance if <code>proj</code> is null), and if debugging is enabled,
     * a message may be output to the controlling terminal.
     * <p>
     * 
     * @param proj Projection
     * @return boolean true if successful, false if not.
     */
    public boolean generate(Projection proj) {
        if (label != null) {
            label.setY(origYLabelOffset);
            label.setX(origXLabelOffset);
        }

        java.awt.Graphics g = DeclutterMatrix.getGraphics();
        if (g != null && label != null) {
            g.setFont(label.getFont());
            // Now set the vertical offset to the original place based
            // off the height of the label, so that the location place
            // is halfway up the text. That way, it looks like a
            // label.
            int height = g.getFontMetrics().getAscent();
            currentFontDescent = g.getFontMetrics().getDescent();
            label.setX(label.getX() + horizontalLabelBuffer);
            label.setY(label.getY() + (height / 2) - 2);
        }

        if (label != null) {
            label.generate(proj);
            label.prepareForRender(g);
        }
        if (location != null)
            location.generate(proj);

        return true;
    }

    /**
     * Paint the graphic and the name of the location. This should only be used
     * if the locations are pretty spread out from each other. If you think you
     * need to declutter, you should render all the graphics, and then render
     * the names, so that the graphics don't cover up the names.
     * <P>
     * This paints the graphic into the Graphics context. This is similar to
     * <code>paint()</code> function of java.awt.Components. Note that if the
     * graphic has not been generated, it will not be rendered. This render will
     * take into account the layer showNames and showLocations settings.
     * 
     * @param g Graphics context to render into.
     */
    public void render(Graphics g) {
        renderLocation(g);
        renderName(g);
    }

    /**
     * Paint the graphic label (name) only. This paints the graphic into the
     * Graphics context. This is similar to <code>paint()</code> function of
     * java.awt.Components. Note that if the graphic has not been generated, it
     * will not be rendered. This render will take into account the layer
     * showNames and showLocations settings.
     * 
     * @param g Graphics context to render into.
     */
    public void renderName(Graphics g) {
        if (shouldRenderName()) {
            label.render(g);
        }
    }

    /**
     * Paint the graphic location graphic only. This paints the graphic into the
     * Graphics context. This is similar to <code>paint()</code> function of
     * java.awt.Components. Note that if the graphic has not been generated, it
     * will not be rendered. This render will take into account the layer
     * showNames and showLocations settings.
     * 
     * @param g Graphics context to render into.
     */
    public void renderLocation(Graphics g) {
        if (shouldRenderLocation()) {
            location.render(g);
        }
    }

    /**
     * Convenience method to see if handler/global settings dictate that the
     * location label should be rendered.
     * 
     * @return true if the name label should be rendered.
     */
    protected boolean shouldRenderName() {
        boolean globalShowNames = false;
        boolean forceGlobal = false;
        if (handler != null) {
            globalShowNames = handler.isShowNames();
            forceGlobal = handler.isForceGlobal();
        }

        return label != null && ((forceGlobal && globalShowNames) || (!forceGlobal && showName));
    }

    /**
     * Convenience method to see if handler/global settings dictate that the
     * location icon should be rendered.
     * 
     * @return true of the location marker should be rendered.
     */
    protected boolean shouldRenderLocation() {
        boolean globalShowLocations = false;
        boolean forceGlobal = false;
        if (handler != null) {
            globalShowLocations = handler.isShowLocations();
            forceGlobal = handler.isForceGlobal();
        }

        return location != null && ((forceGlobal && globalShowLocations) || (!forceGlobal && showLocation));
    }

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance from graphic to the point
     */
    public float distance(double x, double y) {
        float labelDist = Float.MAX_VALUE;
        float locationDist = Float.MAX_VALUE;

        if (shouldRenderLocation()) {
            locationDist = location.distance(x, y);
        }

        if (shouldRenderName()) {
            labelDist = label.distance(x, y);
        }

        return (locationDist > labelDist ? labelDist : locationDist);
    }

    /**
     * Given the label is this location has a height and width, find a clean
     * place on the map for it. Assumes label is not null.
     * 
     * @param declutter the DeclutterMatrix for the map.
     */
    protected void declutterLabel(DeclutterMatrix declutter, Projection proj) {

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("\nLocation::RepositionText => " + label.getData());
        }

        // Right now, I think this method takes some presumptuous
        // actions, assuming that you want the graphics to take up
        // space in the declutter matrix. We might want to delete
        // this showLocation code, and let people create their own
        // location subclasses that define how the graphic should be
        // handled in the declutter matrix.

        // I think I will. This stuff is commented out for the
        // reasons stated above.

        // if (isShowLocation()) {
        // Point lp;
        // // Take up space with the label
        // if (location instanceof OMRasterObject) {
        // lp = ((OMRasterObject)location).getMapLocation();
        // // This location is the upper left location of the
        // // declutter matrix. The declutter matrix works from
        // // lower left to upper right.
        // if (lp != null) {
        // int locHeight = ((OMRasterObject)location).getHeight();
        // int locWidth = ((OMRasterObject)location).getWidth();
        // // Need to get this right for the DeclutterMatrix
        // // space, but changing lp changes where the
        // // location will appear - fix this later.
        // lp.y += locHeight;
        // declutter.setTaken(lp, locWidth, locHeight);
        // // Reset it to the original projected location.
        // lp.y -= locHeight;
        // }
        // } else if (renderType != RENDERTYPE_XY) {
        // lp = proj.forward(lat,lon);
        // lp.x += xOffset-1;
        // lp.y += yOffset-1;
        // declutter.setTaken(lp, 3, 3);
        // } else {
        // lp = new Point(x-1, y-1);
        // declutter.setTaken(lp, 3, 3);
        // }
        // }

        if (isShowName() || (handler != null && handler.isShowNames())) {

            if (label == null || label.getPolyBounds() == null) {
                // Why bother going further??
                return;
            }

            Rectangle bounds = label.getPolyBounds().getBounds();
            int height = (int) ((float) (bounds.getHeight() - currentFontDescent / 2));
            int width = (int) bounds.getWidth();
            // Projected location of label on the screen
            Point2D p = label.getMapLocation();

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("old point X Y =>" + p.getX() + " " + p.getY() + "    height = " + height + " width = " + width);
            }

            int limit;
            if (declutterLimit == DECLUTTER_LOCALLY) {
                limit = height * 2;
            } else {
                limit = declutterLimit;
            }

            // newpoint is the new place on the map to put the label
            Point2D newpoint = declutter.setNextOpen(p, width, height, limit);

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("new point X Y =>" + newpoint.getX() + " " + newpoint.getY());
            }

            label.setMapLocation(newpoint);
        }
    }

    /**
     * A simple conversion method for the common String representation of
     * decimal degree coordinates, which is a letter denoting the globle
     * hemisphere (N or S for latitudes, W or E for longitudes, and then a
     * number string. For latitudes, the first two numbers represent the whole
     * degree value, and the rest of the numbers represent the fractional
     * protion. For longitudes, the first three numbers represent the whole
     * degree value. For instance N2443243 equals 24.43243 degrees North, and
     * S2443243 results in -24.43243 degrees. Likewise, w12423443 results in
     * -124.23443 degrees.
     * 
     * @param coord the coordinate string representing the decimal degree value,
     *        following the format [NSEW]XXXXXXXXX.
     * @return the decimal degrees for the string. There is no notation for you
     *         to know whether it's a latitude or longitude value.
     */
    public static float convertCoordinateString(String coord)
            throws NumberFormatException {

        float ret = 0f;
        String mantissa;
        char direction = coord.charAt(0);
        if (direction == 'N' || direction == 'S' || direction == 'n' || direction == 's') {
            float whole = new Float(coord.substring(1, 3)).floatValue();
            ret += whole;
            mantissa = coord.substring(3);
        } else if (direction == 'W' || direction == 'E' || direction == 'w' || direction == 'e') {
            ret += new Float(coord.substring(1, 4)).floatValue();
            mantissa = coord.substring(4);
        } else {
            // Don't know the format!!
            throw new NumberFormatException("Location.convertCoordinateString wants <[NSWE]XXXXXXXX>, not getting it.");
        }

        ret += new Float(mantissa).floatValue() / (float) (Math.pow(10, mantissa.length()));
        if (direction == 'W' || direction == 'S' || direction == 'w' || direction == 's') {
            ret *= -1f;
        }

        return ret;
    }

    /**
     * We're using the main function for Location to test the
     * convertCoordinateString function.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            logger.info("  usage: java com.bbn.openmap.layer.location.Location <[NSWE]XXXXXXXX>");
            return;
        }
        float ret = Location.convertCoordinateString(args[0]);
        System.out.println(ret);
    }

}