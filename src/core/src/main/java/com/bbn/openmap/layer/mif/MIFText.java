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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFText.java,v $
// $RCSfile: MIFText.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/11 20:39:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.mif;

import java.awt.Graphics;

import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;

/**
 * Extension of OMText to provide basic support to ensure that the
 * screen will not become cluttered This extension of OMText defines a
 * property visibleScale. visibleScale is the OpenMap maximum scale at
 * which the text will be rendered all scale values after the
 * visibleScale value will result in the text to be rendered.
 * <P>
 * 
 * If visible scale is set to -1 the text will be rendered all all
 * scale levels.
 * 
 * @author Simon Bowen
 */
public class MIFText extends OMText implements MIFGraphic, java.io.Serializable {
    /**
     * default visibleScale value is -1
     */
    private float visibleScale = -1;

    private boolean renderText = true;

    /**
     * @param lat the latitude to display the text string
     * @param lon the longitude to display the text string
     * @param txt the text string to display
     * @param justification from OMText e.g. OMText.JUSTIFY_CENTER
     * @param visibleScale if visible scale is set to -1 the text will
     *        be rendered all all scale levels.
     */
    public MIFText(float lat, float lon, String txt, int justification,
            float visibleScale) {

        super(lat, lon, txt, justification);
        this.setVisibleScale(visibleScale);
    }

    /**
     * Sets the scale at which the graphic becomes visible, if set to
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
     * Overriden from OMText so that we can handle a flag
     * <code>renderText</code> to signal to the render method if the
     * scale is appropriate to render the text or not.
     */
    public synchronized boolean generate(Projection proj) {
        if ((proj.getScale() <= getVisibleScale()) || (getVisibleScale() == -1)) {

            this.renderText = true;
        } else {
            this.renderText = false;
        }

        return super.generate(proj);
    }

    /**
     * Overriden from OMText so that we can handle a flag
     * <code>renderText</code> to determine if the text should be
     * rendered or not.
     */
    public synchronized void render(Graphics g) {
        if (renderText) {
            super.render(g);
        }
    }
}

/** Last line of file * */
