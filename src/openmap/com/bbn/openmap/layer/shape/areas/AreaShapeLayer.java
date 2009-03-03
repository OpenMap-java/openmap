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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/areas/AreaShapeLayer.java,v $
// $RCSfile: AreaShapeLayer.java,v $
// $Revision: 1.4.2.5 $
// $Date: 2009/03/03 04:59:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape.areas;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * A Layer to use an AreaHandler to display geographic regions on OpenMap. See
 * the AreaHandler for more information on how to set this layer up.
 * 
 * @see com.bbn.openmap.layer.shape.areas.AreaHandler
 */
public class AreaShapeLayer extends ShapeLayer {

    protected AreaHandler areas;

    /**
     */
    public AreaShapeLayer() {
        super();
        setMouseModeIDsForEvents(new String[] { "Gestures" });
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for this
     *        layer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        areas = new AreaHandler(spatialIndex, drawingAttributes);
        areas.setProperties(prefix, props);
        areas.setCoordTransform(super.getCoordTransform());
    }
    
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        if (areas != null) {
            areas.getProperties(props);
        }
        return props;
    }
    
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        if (areas != null) {
            areas.getPropertyInfo(props);
        }
        return props;
    }

    /**
     * Set the AreaHandler.
     */
    public void setAreas(AreaHandler a) {
        areas = a;
    }

    /**
     * Get the AreaHandler.
     */
    public AreaHandler getAreas() {
        return areas;
    }

    /**
     * Gets the layer graphics.
     * 
     * @return OMGraphicList
     */
    public synchronized OMGraphicList prepare() {

        Projection projection = getProjection();
        Point2D ul = projection.getUpperLeft();
        Point2D lr = projection.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        OMGraphicList list = areas.getGraphics(ulLat,
                ulLon,
                lrLat,
                lrLon,
                getProjection());
        return list;
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        areas.setDrawingAttributes(da);
    }

    public DrawingAttributes getDrawingAttributes() {
        return areas.getDrawingAttributes();
    }

    /**
     * Find a PoliticalArea named by the abbreviation
     */
    public PoliticalArea findPoliticalArea(String area_abbrev) {
        return areas.findPoliticalArea(area_abbrev);
    }

    public boolean isHighlightable(OMGraphic omg) {
        return true;
    }
    
}
