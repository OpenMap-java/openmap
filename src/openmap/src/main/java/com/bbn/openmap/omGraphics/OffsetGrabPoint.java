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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OffsetGrabPoint.java,v $
// $RCSfile: OffsetGrabPoint.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.util.Hashtable;

/**
 * An OffsetGrabPoint is one that manages other grab points. When it is moved,
 * the other GrabPoints on its internal list are moved the same amount in pixel
 * space.
 */
public class OffsetGrabPoint extends GrabPoint {
    /** The list of GrabPoints to move when this point moves. */
    protected Hashtable<GrabPoint, Offset> offsetPoints;

    /**
     * Create the OffsetGrabPoint at a certain window location.
     * 
     * @param x horizontal pixel location from left side of window.
     * @param y vertical pixel location from top side of window.
     */
    public OffsetGrabPoint(int x, int y) {
        this(x, y, DEFAULT_RADIUS);
    }

    /**
     * Create the OffsetGrabPoint at a certain window location.
     * 
     * @param x horizontal pixel location from left side of window.
     * @param y vertical pixel location from top side of window.
     * @param radius the pixel radius of the point.
     */
    public OffsetGrabPoint(int x, int y, int radius) {
        super(x, y, radius);
        offsetPoints = new Hashtable<GrabPoint, Offset>();
    }

    /**
     * Add a GrabPoint to the internal list.
     */
    public GrabPoint addGrabPoint(GrabPoint gp) {

        if (gp == null) {
            com.bbn.openmap.util.Debug.error("OffsetGrabPoint: adding null grab point!");
            return null;
        }

        if (offsetPoints == null) {
            offsetPoints = new Hashtable<GrabPoint, Offset>();
        }
        offsetPoints.put(gp, new Offset(gp));
        
        return gp;
    }

    /**
     * Remove a GrabPoint to the internal list.
     */
    public GrabPoint removeGrabPoint(GrabPoint rgp) {
        if (offsetPoints != null) {
            Offset offset = offsetPoints.remove(rgp);
            if (offset != null) {
                return offset.gp;
            }
        }
        return null;
    }

    /**
     * Called when the position of the OffsetGrabPoint has moved. Does not
     * adjust the offsets.
     */
    public void set(int x, int y) {
        super.set(x, y);
    }

    /**
     * Called when the X position of the OffsetGrabPoint has moved. Does not
     * adjust the offsets.
     */
    public void setX(int x) {
        super.setX(x);
    }

    /**
     * Called when the Y position of the OffsetGrabPoint has moved. Does not
     * adjust the offsets.
     */
    public void setY(int y) {
        super.setY(y);
    }

    /**
     * Called when the other grab points may have moved, and the offset
     * distances should be changed internally for the Offset objects.
     */
    public void set() {
        updateOffsets();
    }

    /**
     * Flag used as a lock to prevent StackOverflowErrors, in case this
     * OffetGrabPoint is unwittingly a child of itself.
     */
    protected boolean overflowLock = false;

    /**
     * Go through all the Offset elements and changes their position on the map.
     * Should be called when the OffsetGrabPoint has been moved and you want to
     * move all the GrabPoints in its list.
     */
    public synchronized void moveOffsets() {
        if (!overflowLock) {
            overflowLock = true;
            for (Offset offset : offsetPoints.values()) {
                offset.move();
            }
            overflowLock = false;
        }
    }

    /**
     * Go through all the Offset elements and update the relative position to
     * this grab point. Should be called when you set the position of the
     * OffsetGrabPoint and you want to set the offset distances of all the
     * GrabPoints in the internal list.
     */
    public synchronized void updateOffsets() {
        if (!overflowLock) {
            overflowLock = true;
            for (Offset offset : offsetPoints.values()) {
                offset.update();
            }
            overflowLock = false;
        }
    }

    public void clear() {
        offsetPoints.clear();
    }

    protected void finalize() {
        offsetPoints.clear();
    }

    /**
     * A wrapper class of the internal GrabPoints. Contains their pixel offset
     * distance from the OffsetGrabPoint.
     */
    public class Offset {
        public GrabPoint gp;
        public int offsetX;
        public int offsetY;

        public Offset(GrabPoint grabPoint) {
            gp = grabPoint;
            update();
        }

        /**
         * Update resets the pixel offsets from the OffsetGrabPoint, to the
         * current distances between the GrabPoint and the OffsetGrabPoint.
         */
        public void update() {
            offsetX = gp.getX() - getX();
            offsetY = gp.getY() - getY();
            if (gp instanceof OffsetGrabPoint) {
                ((OffsetGrabPoint) gp).updateOffsets();
            }
        }

        /**
         * Move relocates the GrabPoint to the current position of the
         * OffsetGrabPoint plus the offset distances.
         */
        public void move() {
            int newX = getX() + offsetX;
            int newY = getY() + offsetY;

            if (gp instanceof HorizontalGrabPoint) {
                ((HorizontalGrabPoint) gp).set(newX, newY, true);
            } else if (gp instanceof VerticalGrabPoint) {
                ((VerticalGrabPoint) gp).set(newX, newY, true);
            } else {
                gp.set(newX, newY);
            }

            if (gp instanceof OffsetGrabPoint) {
                ((OffsetGrabPoint) gp).moveOffsets();
            }
        }

    }
}