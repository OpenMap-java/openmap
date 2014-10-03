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
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.OverviewMapStatusListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * A class used to draw a polygon representing the area covered by the source
 * MapBean projection. Used by the OverviewMapHandler. The layer responds to
 * DrawingAttributes properties being set, which are forwarded on to the
 * coverage box rectangle.
 * <p>
 * This layer uses the source map's projection to construct a polygon to depict
 * the coverage area. A coverage area in one projection does not necessarily
 * translate into a rectangle on another projection. This layer uses the source
 * map projection to create a polygon that approximates the extents of the
 * projection by walking the perimeter of the projection's screen coordinates
 * and translating those screen coordinates into a lat/lon array for the
 * polygon.
 */
public class OverviewMapAreaLayer extends Layer implements OverviewMapStatusListener {

    protected float overviewScale;
    protected OMGraphic poly;
    protected Projection sourceMapProjection;
    protected DrawingAttributes areaAttributes = DrawingAttributes.getDefaultClone();

    public void projectionChanged(ProjectionEvent pEvent) {

        if (sourceMapProjection == null)
            return;

        Projection proj = pEvent.getProjection();

        // Save the scale for use in the
        overviewScale = proj.getScale();

        boolean cylindrical = sourceMapProjection instanceof Cylindrical;

        double[] llarr = ProjMath.getProjectionScreenOutlineCoords(sourceMapProjection);

        if (llarr != null) {

            boolean northPoleVisible = ProjMath.isVisible(sourceMapProjection, new LatLonPoint.Double(90, 0));
            boolean southPoleVisible = ProjMath.isVisible(sourceMapProjection, new LatLonPoint.Double(-90, 0));

            if (northPoleVisible || southPoleVisible) {
                Point2D center = sourceMapProjection.getCenter();
                Point2D ul = sourceMapProjection.getUpperLeft();
                double dist = Geo.distance(center.getY(),  center.getX(), ul.getY(), ul.getX());
                poly = new OMCircle(center.getY(), center.getX(), dist, Length.RADIAN);
            } else {
                poly = new OMPoly(llarr, OMPoly.DECIMAL_DEGREES, cylindrical ? OMGraphic.LINETYPE_STRAIGHT
                        : OMGraphic.LINETYPE_GREATCIRCLE);
            }
            areaAttributes.setTo(poly);

            // And finally generate the poly
            poly.generate(proj);
        }
    }

    /**
     * Set with the projection of the source MapBean, before changing the
     * projection of the overview MapBean. That way, the rectangle coordinates
     * are set before they get generated().
     */
    public void setSourceMapProjection(Projection proj) {
        sourceMapProjection = proj;
    }

    public void paint(Graphics g) {
        if (poly != null && overviewScale > sourceMapProjection.getScale())
            poly.render(g);
    }

    public DrawingAttributes getAreaAttributes() {
        return areaAttributes;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        areaAttributes.setProperties(prefix, props);
        // Cause a rebuild if this is called after
        // the first projection change.
        poly = null;
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        areaAttributes.getProperties(props);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        areaAttributes.getPropertyInfo(props);
        return props;
    }

}
