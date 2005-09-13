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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/FullProjectionRenderPolicy.java,v $
// $RCSfile: FullProjectionRenderPolicy.java,v $
// $Revision: 1.3 $
// $Date: 2005/09/13 14:33:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.proj.Projection;
import java.awt.Graphics;

/**
 * The FullProjectionRenderPolicy is a StandardRenderPolicy that sets
 * the clip of the java.awt.Graphics passed into the paint method with
 * the clipping area of the projection. You'll need to use this render
 * policy if you want to paint into a buffer that is bigger than the
 * layer size (and MapBean size), because Java sets the max clipping
 * size to the size of the component.
 */
public class FullProjectionRenderPolicy extends StandardRenderPolicy {

    public FullProjectionRenderPolicy() {}

    /**
     * Don't pass in a null layer.
     */
    public FullProjectionRenderPolicy(OMGraphicHandlerLayer layer) {
        super(layer);
    }

    public void paint(Graphics g) {
        if (layer != null && layer.isProjectionOK(layer.getProjection())) {
            Projection proj = layer.getProjection();
            // The proj shouldn't be null, because isProjectionOK
            // checks for that, but that method may be overridden
            if (proj != null) {
                g.setClip(0, 0, proj.getWidth(), proj.getHeight());
            }
        }
        super.paint(g);
    }
}