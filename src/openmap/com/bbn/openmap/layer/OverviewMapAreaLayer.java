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
import java.util.ArrayList;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.OverviewMapStatusListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Cylindrical;
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
public class OverviewMapAreaLayer extends Layer implements
        OverviewMapStatusListener {

    protected float overviewScale;
    protected OMPoly poly;
    protected Projection sourceMapProjection;
    protected DrawingAttributes areaAttributes = DrawingAttributes.getDefaultClone();

    public void projectionChanged(ProjectionEvent pEvent) {
        // Sourge projection not yet set
        if (sourceMapProjection == null)
            return;

        Projection proj = pEvent.getProjection();

        // Save the scale for use in the
        overviewScale = proj.getScale();

        boolean cylindrical = sourceMapProjection instanceof Cylindrical;

        if (poly == null) {
            poly = new OMPoly();
            areaAttributes.setTo(poly);
            poly.setLineType(cylindrical ? OMGraphic.LINETYPE_STRAIGHT
                    : OMGraphic.LINETYPE_GREATCIRCLE);
        }

        // Would have used ArrayList<LatLonPoint> here but didn't for
        // backward compatibility.
        ArrayList l = new ArrayList();

        // Get the parameters needed for building the coverage polygon
        int width = sourceMapProjection.getWidth();
        int height = sourceMapProjection.getHeight();
        float xinc = ((float) width) / 10f;
        float yinc = ((float) height) / 10f;

        Point2D center = sourceMapProjection.getCenter(new LatLonPoint.Double());
        Point2D tmpllp;

        boolean northPoleVisible = isVisible(new LatLonPoint.Double(90, 0));
        boolean southPoleVisible = isVisible(new LatLonPoint.Double(-90, 0));

        // Walk the top edge of the source projection's screen bounds
        for (int i = 0; i <= 10; i++) {
            tmpllp = sourceMapProjection.inverse(xinc * i, 0, new LatLonPoint.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the right edge of the source projection's screen bounds
        for (int i = 0; i <= 10; i++) {
            tmpllp = sourceMapProjection.inverse(width, yinc * i, new LatLonPoint.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the south edge of the source projection's screen bounds
        for (int i = 10; i >= 0; i--) {
            tmpllp = sourceMapProjection.inverse(xinc * i, height, new LatLonPoint.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        // Walk the left edge of the source projection's screen bounds
        for (int i = 10; i >= 0; i--) {
            tmpllp = sourceMapProjection.inverse(0, yinc * i, new LatLonPoint.Double());
            if (!tmpllp.equals(center)) {
                l.add(tmpllp);
            }
        }

        if (false || northPoleVisible || southPoleVisible) {

        } else {
            // populate the coordinate array for the polygon
            double llarr[] = new double[l.size() * 2];
            for (int i = 0; i < l.size(); i++) {
                int pos = i * 2;
                LatLonPoint llp = ((LatLonPoint) l.get(i));
                llarr[pos] = (float) llp.getRadLat();
                llarr[pos + 1] = (float) llp.getRadLon();
            }

            // Set the poly coordinates
            poly.setLocation(llarr, OMPoly.RADIANS);

        }

        // And finally generate the poly
        poly.generate(proj);
    }

    protected boolean isVisible(LatLonPoint llp) {
        boolean ret = false;
        if (sourceMapProjection != null) {
            if (sourceMapProjection.isPlotable(llp)) {
                Point2D p = sourceMapProjection.forward(llp);
                double x = p.getX();
                double y = p.getY();
                if (x >= 0 && x <= sourceMapProjection.getWidth() && y >= 0
                        && y <= sourceMapProjection.getWidth()) {
                    ret = true;
                }
            }
        }
        return ret;
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
