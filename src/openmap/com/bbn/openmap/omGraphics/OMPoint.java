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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMPoint.java,v $
// $RCSfile: OMPoint.java,v $
// $Revision: 1.9 $
// $Date: 2005/01/10 16:58:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A OMPoint is used to mark a specific point. You can set this point
 * as a lat/lon position, a screen X/Y position, or a lat/lon position
 * with a screen X/Y offset. The position can be marked with a
 * rectangle or circle with an adjusted radius. The radius is the
 * pixel distance from the center of the location to each edge of the
 * marking rectangle or circle.
 */
public class OMPoint extends OMGraphic implements Serializable {

    public final static int DEFAULT_RADIUS = 2;
    public final static boolean DEFAULT_ISOVAL = false;
    /**
     * The number of pixels in the radius for the point
     * representation.
     */
    protected int radius = DEFAULT_RADIUS;
    /**
     * Horizontal window position of point, in pixels from left side
     * of window.
     */
    protected int x = 0;
    /**
     * Vertical window position of point, in pixels from the top of
     * the window.
     */
    protected int y = 0;
    /** Latitude of point, decimal degrees. */
    protected float lat1 = 0.0f;
    /** Longitude of point, decimal degrees. */
    protected float lon1 = 0.0f;

    /** Set to true if you want little circles marking the point. */
    protected boolean oval = DEFAULT_ISOVAL;

    /** Default constructor, waiting to be filled. */
    public OMPoint() {
        super();
    }

    /**
     * Create an OMPoint at a lat/lon position, with the default
     * radius.
     */
    public OMPoint(float lat, float lon) {
        this(lat, lon, DEFAULT_RADIUS);
    }

    /**
     * Create an OMPoint at a lat/lon position, with the specified
     * radius.
     */
    public OMPoint(float lat, float lon, int radius) {
        setRenderType(RENDERTYPE_LATLON);
        set(lat, lon);
        this.radius = radius;
    }

    /**
     * Create an OMPoint at a lat/lon position with a screen X/Y pixel
     * offset, with the default radius.
     */
    public OMPoint(float lat, float lon, int offsetx, int offsety) {
        this(lat, lon, offsetx, offsety, DEFAULT_RADIUS);
    }

    /**
     * Create an OMPoint at a lat/lon position with a screen X/Y pixel
     * offset, with the specified radius.
     */
    public OMPoint(float lat, float lon, int offsetx, int offsety, int radius) {
        setRenderType(RENDERTYPE_OFFSET);
        set(lat, lon, offsetx, offsety);
        this.radius = radius;
    }

    /**
     * Put the point at a screen location, marked with a rectangle
     * with edge size DEFAULT_RADIUS * 2 + 1.
     */
    public OMPoint(int x, int y) {
        this(x, y, DEFAULT_RADIUS);
    }

    /**
     * Put the point at a screen location, marked with a rectangle
     * with edge size radius * 2 + 1.
     */
    public OMPoint(int x, int y, int radius) {
        setRenderType(RENDERTYPE_XY);
        set(x, y);
        this.radius = radius;
    }

    /** For lat/lon rendertype points, to move the point location. */
    public void set(float lat, float lon) {
        setLat(lat);
        setLon(lon);
    }

    /** For offset rendertype points, to move the point location. */
    public void set(float lat, float lon, int offsetx, int offsety) {
        setLat(lat);
        setLon(lon);
        ;
        set(offsetx, offsety);
    }

    /**
     * For screen x/y rendertype points, to move the point location.
     * This method does not call setX() and setY().
     */
    public void set(int x, int y) {
        // You have to set these directly, or you can mess up the grab
        // points by using set methods - VerticalGrabPoints and
        // HorizontalGrabPoints disable some methods. This method is
        // used to override them, for initialization purposes.
        this.x = x;
        this.y = y;
        setNeedToRegenerate(true);
    }

    /** Set the latitude of the point, in decimal degrees. */
    public void setLat(float lat) {
        this.lat1 = lat;
        setNeedToRegenerate(true);
    }

    /** Get the latitude of the point, in decimal degrees. */
    public float getLat() {
        return lat1;
    }

    /** Set the longitude of the point, in decimal degrees. */
    public void setLon(float lon) {
        this.lon1 = lon;
        setNeedToRegenerate(true);
    }

    /** Get the longitude of the point, in decimal degrees. */
    public float getLon() {
        return lon1;
    }

    /** For screen x/y rendertype points. */
    public void setX(int x) {
        this.x = x;
        setNeedToRegenerate(true);
    }

    /** For screen x/y rendertype points. */
    public int getX() {
        return x;
    }

    /** For screen x/y rendertype points. */
    public void setY(int y) {
        this.y = y;
        setNeedToRegenerate(true);
    }

    /** For screen x/y rendertype points. */
    public int getY() {
        return y;
    }

    /**
     * Set the radius of the marking rectangle. The edge size of the
     * marking rectangle will be radius * 2 + 1.
     */
    public void setRadius(int radius) {
        this.radius = radius;
        setNeedToRegenerate(true);
    }

    /**
     * Get the radius for the point.
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Set whether little circles should be marking the point.
     * 
     * @param set true for circles.
     */
    public void setOval(boolean set) {
        if (oval != set) {
            setNeedToRegenerate(true);
            oval = set;
        }
    }

    /**
     * Get whether little circles should be marking the point.
     */
    public boolean isOval() {
        return oval;
    }

    /**
     * Prepare the rectangle for rendering.
     * 
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
        setShape(null);
        if (proj == null) {
            Debug.message("omgraphic", "OMPoint: null projection in generate!");
            return false;
        }
        
        // reset the internals
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;

        switch (renderType) {
        case RENDERTYPE_XY:
            x1 = x - radius;
            y1 = y - radius;
            x2 = x + radius;
            y2 = y + radius;
            
            break;
        case RENDERTYPE_OFFSET:
        case RENDERTYPE_LATLON:
            if (!proj.isPlotable(lat1, lon1)) {
                setNeedToRegenerate(true);//HMMM not the best flag
                return false;
            }
            Point p1 = proj.forward(lat1, lon1);

            x1 = p1.x + x - radius;
            y1 = p1.y + y - radius;
            x2 = p1.x + x + radius;
            y2 = p1.y + y + radius;
            break;
        case RENDERTYPE_UNKNOWN:
            System.err.println("OMPoint.generate(): invalid RenderType");
            return false;
        }

        if (oval) {
            shape = new GeneralPath(new Ellipse2D.Float((float) Math.min(x2, x1), (float) Math.min(y2,
                    y1), (float) Math.abs(x2 - x1), (float) Math.abs(y2 - y1)));
        } else {
            shape = createBoxShape((int) Math.min(x2, x1), (int) Math.min(y2,
                    y1), (int) Math.abs(x2 - x1), (int) Math.abs(y2 - y1));
        }

        initLabelingDuringGenerate();
        setLabelLocation(new Point(x2, y1));
        
        setNeedToRegenerate(false);
        return true;
    }

    protected boolean hasLineTypeChoice() {
        return false;
    }

}

