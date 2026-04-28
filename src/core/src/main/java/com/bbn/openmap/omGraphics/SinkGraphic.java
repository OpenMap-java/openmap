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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/SinkGraphic.java,v $
// $RCSfile: SinkGraphic.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;

import com.bbn.openmap.proj.Projection;

/**
 * This graphic is a Sink. You can use it as a placeholder, say in an
 * OMGraphicList. It is never visible, and you can use the single
 * shared instance in as many places as you want. This shared instance
 * is multithreaded safe.
 * 
 * @see OMGraphic
 */
public class SinkGraphic extends OMGraphicAdapter implements OMGraphic {

    // the shared instance
    private static transient SinkGraphic sharedInstance;

    // cannot construct
    private SinkGraphic() {}

    /**
     * Get a shared instance of the SinkGraphic.
     * 
     * @return SinkGraphic shared instance
     */
    public final static SinkGraphic getSharedInstance() {
        if (sharedInstance == null)
            sharedInstance = new SinkGraphic();
        return sharedInstance;
    }

    /**
     * This graphic is not visible.
     * 
     * @param visible IGNORED
     */
    public void setVisible(boolean visible) {}

    /**
     * This graphic is not visible.
     * 
     * @return false
     */
    public boolean isVisible() {
        return false;
    }

    /**
     * This graphic generates nothing, successfully.
     * 
     * @param proj IGNORED
     * @return true
     */
    public boolean generate(Projection proj) {
        return true;
    }

    /**
     * This graphic does not render.
     * 
     * @param g IGNORED
     */
    public void render(Graphics g) {}

    /**
     * This graphic is at Float.POSITIVE_INFINITY.
     * 
     * @param x IGNORED
     * @param y IGNORED
     * @return Float.POSITIVE_INFINITY
     */
    public float distance(double x, double y) {
        return Float.POSITIVE_INFINITY;
    }
}