// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;

import java.awt.Point;

/**
 * A class used to draw the rectangle representing the area covered by
 * the source MapBean projection.  Used by the OverviewMapHandler.
 */
public class OverviewMapAreaLayer extends Layer 
    implements OverviewMapStatusListener {
    
    OMRect rectangle;
    float sourceScale;
    float overviewScale;
    LatLonPoint ul;
    LatLonPoint lr;

    /**
     * Listening to the overview MapBean.
     */
    public void projectionChanged(ProjectionEvent pEvent){
	Projection proj = pEvent.getProjection();
	
	// HACK for big world problem...
	if (rectangle == null){
	    rectangle = new OMRect();
	}
	if (ul != null || lr != null){
	    
	    if (proj instanceof Cylindrical) {
		Point ulp = proj.forward(ul);
		Point lrp = proj.forward(lr);
		rectangle.setLocation(ulp.x, ulp.y, lrp.x, lrp.y);
		rectangle.setLineType(OMGraphic.LINETYPE_STRAIGHT);
	    } else {
		//  HACK Would be nice if we didn't run into the
		//  big-world problem.
		rectangle.setLocation(ul.getLatitude(),
				      ul.getLongitude(),
				      lr.getLatitude(),
				      lr.getLongitude(),
				      OMGraphic.LINETYPE_RHUMB);
	    }
	    rectangle.generate(proj);
	}
	
	overviewScale = proj.getScale();
    }
    
    /**
     * Set with the projection of the source MapBean, before
     * changing the projection of the overview MapBean.  That way,
     * the rectangle coordinates are set before they get
     * generated(). 
     */
    public void setSourceMapProjection(Projection proj){
	ul = proj.getUpperLeft();
	lr = proj.getLowerRight();
	sourceScale = proj.getScale();
    }
    
    /** 
     * Get the area rectangle.
     */
    public OMRect getOverviewMapArea(){
	return rectangle;
    }
    
    public void paint(java.awt.Graphics g){
	if (rectangle != null &&
	    overviewScale > sourceScale) rectangle.render(g);
    }
}

