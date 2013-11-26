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
// $Revision: 1.13 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.Serializable;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A OMPoint is used to mark a specific point. You can set this point as a
 * lat/lon position, a screen X/Y position, or a lat/lon position with a screen
 * X/Y offset. The position can be marked with a rectangle or circle with an
 * adjusted radius. The radius is the pixel distance from the center of the
 * location to each edge of the marking rectangle or circle.
 */
public class OMPoint
        extends OMGraphicAdapter
        implements OMGraphic, Serializable {

    public final static int DEFAULT_RADIUS = 2;
    public final static boolean DEFAULT_ISOVAL = false;
    /**
     * The number of pixels in the radius for the point representation.
     */
    protected int radius = DEFAULT_RADIUS;
    /**
     * Horizontal window position of point, in pixels from left side of window.
     */
    protected int x = 0;
    /**
     * Vertical window position of point, in pixels from the top of the window.
     */
    protected int y = 0;
    /** Latitude of point, decimal degrees. */
    protected double lat1 = 0.0f;
    /** Longitude of point, decimal degrees. */
    protected double lon1 = 0.0f;

    /** Set to true if you want little circles marking the point. */
    protected boolean oval = DEFAULT_ISOVAL;

    /** Default constructor, waiting to be filled. */
    public OMPoint() {
        super();
    }

    /**
     * Create an OMPoint at a lat/lon position, with the default radius.
     */
    public OMPoint(double lat, double lon) {
        this(lat, lon, DEFAULT_RADIUS);
    }

    /**
     * Create an OMPoint at a lat/lon position, with the specified radius.
     */
    public OMPoint(double lat, double lon, int radius) {
        setRenderType(RENDERTYPE_LATLON);
        set(lat, lon);
        this.radius = radius;
    }

    /**
     * Create an OMPoint at a lat/lon position with a screen X/Y pixel offset,
     * with the default radius.
     */
    public OMPoint(double lat, double lon, int offsetx, int offsety) {
        this(lat, lon, offsetx, offsety, DEFAULT_RADIUS);
    }

    /**
     * Create an OMPoint at a lat/lon position with a screen X/Y pixel offset,
     * with the specified radius.
     */
    public OMPoint(double lat, double lon, int offsetx, int offsety, int radius) {
        setRenderType(RENDERTYPE_OFFSET);
        set(lat, lon, offsetx, offsety);
        this.radius = radius;
    }

    /**
     * Put the point at a screen location, marked with a rectangle with edge
     * size DEFAULT_RADIUS * 2 + 1.
     */
    public OMPoint(int x, int y) {
        this(x, y, DEFAULT_RADIUS);
    }

    /**
     * Put the point at a screen location, marked with a rectangle with edge
     * size radius * 2 + 1.
     */
    public OMPoint(int x, int y, int radius) {
        setRenderType(RENDERTYPE_XY);
        set(x, y);
        this.radius = radius;
    }

    /** For lat/lon rendertype points, to move the point location. */
    public void set(double lat, double lon) {
        setLat(lat);
        setLon(lon);
    }

    /** For offset rendertype points, to move the point location. */
    public void set(double lat, double lon, int offsetx, int offsety) {
        setLat(lat);
        setLon(lon);
        set(offsetx, offsety);
    }

    /**
     * For screen x/y rendertype points, to move the point location. This method
     * does not call setX() and setY().
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
    public void setLat(double lat) {
        this.lat1 = lat;
        setNeedToRegenerate(true);
    }

    /** Get the latitude of the point, in decimal degrees. */
    public double getLat() {
        return lat1;
    }

    /** Set the longitude of the point, in decimal degrees. */
    public void setLon(double lon) {
        this.lon1 = lon;
        setNeedToRegenerate(true);
    }

    /** Get the longitude of the point, in decimal degrees. */
    public double getLon() {
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
     * Set the radius of the marking rectangle. The edge size of the marking
     * rectangle will be radius * 2 + 1.
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
     * Prepare the point for rendering.
     * 
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {

        setNeedToRegenerate(true);

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
                    setNeedToRegenerate(true);// HMMM not the best flag
                    return false;
                }
                Point p1 = (Point) proj.forward(lat1, lon1, new Point());

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
            setShape(new GeneralPath(new Ellipse2D.Float((float) Math.min(x2, x1), (float) Math.min(y2, y1), (float) Math.abs(x2
                    - x1), (float) Math.abs(y2 - y1))));
        } else {
            setShape(createBoxShape((int) Math.min(x2, x1), (int) Math.min(y2, y1), (int) Math.abs(x2 - x1),
                                    (int) Math.abs(y2 - y1)));
        }

        initLabelingDuringGenerate();
        setLabelLocation(new Point(x2, y1));

        setNeedToRegenerate(false);
        return true;
    }

    public boolean hasLineTypeChoice() {
        return false;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMPoint) {
            OMPoint point = (OMPoint) source;
            this.radius = point.radius;
            this.x = point.x;
            this.y = point.y;
            this.lat1 = point.lat1;
            this.lon1 = point.lon1;
            this.oval = point.oval;
        }
    }

    public static class Image
            extends OMPoint {
        protected java.awt.Image image;
        protected boolean useImage = true;
        protected int imageX = 0;
        protected int imageY = 0;

        /**
         * Create an OMPoint at a lat/lon position, with the default radius.
         */
        public Image(double lat, double lon) {
            super(lat, lon);
        }

        /**
         * Create an OMPoint at a lat/lon position, with the specified radius.
         */
        public Image(double lat, double lon, int radius) {
            super(lat, lon, radius);
        }

        /**
         * Create an OMPoint at a lat/lon position with a screen X/Y pixel
         * offset, with the default radius.
         */
        public Image(double lat, double lon, int offsetx, int offsety) {
            this(lat, lon, offsetx, offsety, DEFAULT_RADIUS);
        }

        /**
         * Create an OMPoint at a lat/lon position with a screen X/Y pixel
         * offset, with the specified radius.
         */
        public Image(double lat, double lon, int offsetx, int offsety, int radius) {
            super(lat, lon, offsetx, offsety, radius);
        }

        /**
         * Put the point at a screen location, marked with a rectangle with edge
         * size DEFAULT_RADIUS * 2 + 1.
         */
        public Image(int x, int y) {
            this(x, y, DEFAULT_RADIUS);
        }

        /**
         * Put the point at a screen location, marked with a rectangle with edge
         * size radius * 2 + 1.
         */
        public Image(int x, int y, int radius) {
            super(x, y, radius);
        }

        public java.awt.Image getImage() {
            return image;
        }

        public void setImage(java.awt.Image image) {
            this.image = image;
        }

        public boolean isUseImage() {
            return useImage;
        }

        public void setUseImage(boolean useImage) {
            setNeedToRegenerate(this.useImage != useImage);
            this.useImage = useImage;
        }

        /**
         * Prepare the point image for rendering.
         * 
         * @param proj Projection
         * @return true if generate was successful
         */
        public boolean generate(Projection proj) {
            if (!isUseImage() || image == null) {
                return super.generate(proj);
            }

            if (proj == null) {
                Debug.message("omgraphic", "OMPoint: null projection in generate!");
                setNeedToRegenerate(true);
                return false;
            }

            // reset the internals
            int imageHeight = image.getHeight(null);
            int imageWidth = image.getWidth(null);
            int imageOffsetX = imageWidth / 2;
            int imageOffsetY = imageHeight / 2;
            switch (renderType) {
                case RENDERTYPE_XY:
                    imageX = x - imageOffsetX;
                    imageY = y - imageOffsetY;

                    break;
                case RENDERTYPE_OFFSET:
                case RENDERTYPE_LATLON:
                    if (!proj.isPlotable(lat1, lon1)) {
                        setNeedToRegenerate(true);// HMMM not the best flag
                        return false;
                    }
                    Point2D p1 = proj.forward(lat1, lon1);

                    imageX = (int) p1.getX() + x - imageOffsetX;
                    imageY = (int) p1.getY() + y - imageOffsetY;
                    break;
                case RENDERTYPE_UNKNOWN:
                    System.err.println("OMPoint.Image.generate(): invalid RenderType");
                    return false;
            }

            setShape(createBoxShape(imageX, imageY, imageWidth, imageHeight));

            initLabelingDuringGenerate();
            setLabelLocation(new Point(imageX + imageWidth, imageY + imageOffsetY));

            setNeedToRegenerate(false);
            return true;
        }

        /**
         * Render the image when useImage is true, or calls super implementation
         * otherwise
         * 
         * @see com.bbn.openmap.omGraphics.OMGraphic#render(java.awt.Graphics)
         */
        public void render(Graphics g) {
            if (!isRenderable(getShape()))
                return;

            if (isUseImage()) {
                g.drawImage(image, imageX, imageY, null);
                renderLabel(g);
            } else
                super.render(g);
        }

        /**
         * Overriding this method will get mouse events to work over any part of
         * the image.
         */
        public boolean shouldRenderFill() {
            return isUseImage() || super.shouldRenderFill();
        }

        public void restore(OMGeometry source) {
            super.restore(source);
            if (source instanceof OMPoint.Image) {
                OMPoint.Image pntImage = (OMPoint.Image) source;
                this.image = pntImage.image;
                this.useImage = pntImage.useImage;
                this.imageX = pntImage.imageX;
                this.imageY = pntImage.imageY;
            }
        }
    }

}
