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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/OverviewMapAreaLayer.java,v $
// $RCSfile: OverviewMapAreaLayer.java,v $
// $Revision: 1.6 $
// $Date: 2006/02/16 16:22:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.OverviewMapStatusListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.Projection;

/**
 * A class used to draw the rectangle representing the area covered by the
 * source MapBean projection. Used by the OverviewMapHandler. The layer responds
 * to DrawingAttributes properties being set, which are forwarded on to the
 * coverage box rectangle.
 */
public class OverviewMapAreaLayer extends Layer implements
        OverviewMapStatusListener {

    OMRect rectangle;
    float sourceScale;
    float overviewScale;
    Point2D ul;
    Point2D lr;

    DrawingAttributes boxAttributes = DrawingAttributes.getDefaultClone();

    /**
     * Listening to the overview MapBean.
     */
    public void projectionChanged(ProjectionEvent pEvent) {
        Projection proj = pEvent.getProjection();

        // HACK for big world problem...
        if (rectangle == null) {
            rectangle = new OMRect();
            boxAttributes.setTo(rectangle);
        }
        if (ul != null || lr != null) {

            if (proj instanceof Cylindrical) {
                Point ulp = (Point) proj.forward(ul, new Point());
                Point lrp = (Point) proj.forward(lr, new Point());
                rectangle.setLocation(ulp.x, ulp.y, lrp.x, lrp.y);
                rectangle.setLineType(OMGraphic.LINETYPE_STRAIGHT);
            } else {
                // HACK Would be nice if we didn't run into the
                // big-world problem.
                rectangle.setLocation((float) ul.getY(),
                        (float) ul.getX(),
                        (float) lr.getY(),
                        (float) lr.getX(),
                        OMGraphic.LINETYPE_RHUMB);
            }
            rectangle.generate(proj);
        }

        overviewScale = proj.getScale();
    }

    /**
     * Set with the projection of the source MapBean, before changing the
     * projection of the overview MapBean. That way, the rectangle coordinates
     * are set before they get generated().
     */
    public void setSourceMapProjection(Projection proj) {
        ul = proj.getUpperLeft();
        lr = proj.getLowerRight();
        sourceScale = proj.getScale();
    }

    /**
     * Get the area rectangle.
     */
    public OMRect getOverviewMapArea() {
        return rectangle;
    }

    public void paint(Graphics g) {
        if (rectangle != null && overviewScale > sourceScale)
            rectangle.render(g);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        boxAttributes.setProperties(prefix, props);
        // Cause a rebuild if this is called after
        // the first projection change.
        rectangle = null;
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        boxAttributes.getProperties(props);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        boxAttributes.getPropertyInfo(props);
        return props;
    }

}
