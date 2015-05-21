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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFPoint.java,v $
// $RCSfile: MIFPoint.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/11 20:39:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.mif;

import java.awt.Graphics;

import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Projection;

/**
 * Extension of OMPoint to provide basic support to ensure that the
 * screen will not become cluttered This extension of OMPoint defines
 * a property visibleScale. visibleScale is the OpenMap maximum scale
 * at which the point will be rendered all scale values after the
 * visibleScale value will result in the point to be rendered.
 * <P>
 * 
 * if visible scale is set to -1 the point will be rendered all all
 * scale levels.
 * 
 * @author Simon Bowen
 */
public class MIFPoint extends OMPoint implements MIFGraphic,
        java.io.Serializable {

    /**
     * default visibleScale value is -1
     */
    private float visibleScale = -1;

    private boolean renderPoint = true;

    /**
     * @param lat the latitude to display the text string
     * @param lon the longitude to display the text string
     * @param visibleScale if visible scale is set to -1 the text will
     *        be rendered all all scale levels.
     */
    public MIFPoint(float lat, float lon, float visibleScale) {
        super(lat, lon);
        this.visibleScale = visibleScale;
    }

    /**
     * sets the scale at which the graphic becomes visible, if set to
     * -1 the graphic is visible at all scale levels.
     * 
     * @param visibleScale
     */
    public void setVisibleScale(float visibleScale) {
        this.visibleScale = visibleScale;
    }

    public float getVisibleScale() {
        return this.visibleScale;
    }

    /**
     * Overriden from OMPoint so that we can handle a flag
     * <code>renderPoint</code> to signal to the render method if
     * the scale is appropriate to render the point or not.
     */
    public synchronized boolean generate(Projection proj) {
        if ((proj.getScale() <= getVisibleScale()) || (getVisibleScale() == -1)) {
            this.renderPoint = true;
        } else {
            this.renderPoint = false;
        }

        return super.generate(proj);
    }

    /**
     * Overriden from OMPoint so that we can handle a flag
     * <code>renderPoint</code> to determine if the point should be
     * rendered or not.
     */
    public synchronized void render(Graphics g) {
        if (renderPoint) {
            super.render(g);
        }
    }
}
/** Last line of file * */
