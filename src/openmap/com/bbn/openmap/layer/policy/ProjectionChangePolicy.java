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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/ProjectionChangePolicy.java,v $
// $RCSfile: ProjectionChangePolicy.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.policy;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A ProjectionChangePolicy is a policy object that determines how an
 * OMGraphicHandler layer reacts to a projectionChanged() method call.
 * The OMGraphicHandlerLayer has been written to consult this object
 * to control that activity. The reaction could include clearing out
 * the current OMGraphicList or keeping it, or launching a SwingWorker
 * with a doPrepare() call on the layer to have the layer's prepare()
 * method called.
 */
public interface ProjectionChangePolicy {

    /**
     * Set the OMGraphicHandlerLayer to work with.
     */
    public void setLayer(OMGraphicHandlerLayer layer);

    /**
     * Get the OMGraphicHandlerLayer to work with.
     */
    public OMGraphicHandlerLayer getLayer();

    /**
     * The method that is called when the projection changes. The
     * ProjectionChangePolicy should modify the OMGraphicList and do
     * other functions as dictated by the policy, like starting
     * threads to gather data and generating new OMGraphics for the
     * layer.
     */
    public void projectionChanged(ProjectionEvent pe);

    /**
     * The method that gets called when the SwingWorker thread
     * finishes. The OMGraphicList is what is getting returned from
     * the prepare() method on the layer, so it's most likely that
     * this list should be set on the layer.
     */
    public void workerComplete(OMGraphicList aList);
}