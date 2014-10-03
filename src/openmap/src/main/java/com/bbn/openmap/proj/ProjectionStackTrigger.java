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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStackTrigger.java,v $
// $RCSfile: ProjectionStackTrigger.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:23 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.event.ActionListener;

/**
 * Provides Projection Stack input by firing BackProkCmd and
 * ForwardProjCmd commands, which cause it to set a projection in the
 * MapBean. The commands used when firing an action event should be
 * the ones listed in the ProjectionStack
 */
public interface ProjectionStackTrigger {

    /**
     * Add an ActionListener for events that trigger events to shift
     * the Projection stack.
     */
    public void addActionListener(ActionListener al);

    /**
     * Remove an ActionListener that receives events that trigger
     * events to shift the Projection stack.
     */
    public void removeActionListener(ActionListener al);

    /**
     * To receive a status to let the trigger know if any projections
     * in the forward or backward stacks exist, possibly to disable
     * any gui widgets.
     * 
     * @param containsBackProjections there is at least one past
     *        projection in the back cache.
     * @param containsForwardProjections there is at least one future
     *        projection in the forward cache. Used when a past
     *        projection is being used.
     */
    public void updateProjectionStackStatus(boolean containsBackProjections,
                                            boolean containsForwardProjections);
}

