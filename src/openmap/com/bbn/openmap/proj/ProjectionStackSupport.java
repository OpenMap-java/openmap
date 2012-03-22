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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStackSupport.java,v $
// $RCSfile: ProjectionStackSupport.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.util.ArrayList;
import java.util.Iterator;

import com.bbn.openmap.util.Debug;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProjectionListeners and firing ProjectionEvents. You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class ProjectionStackSupport implements java.io.Serializable {

    transient private ArrayList<ProjectionStackTrigger> triggers;

    /**
     * Construct a ProjectionStackSupport.
     */
    public ProjectionStackSupport() {}

    /**
     * Add a ProjectionStackTrigger.
     * 
     * @param pt ProjectionStackTrigger
     */
    public synchronized void add(ProjectionStackTrigger pt) {
        if (triggers == null) {
            triggers = new ArrayList<ProjectionStackTrigger>();
        }

        if (!triggers.contains(pt)) {
            triggers.add(pt);
        }
    }

    /**
     * Remove a ProjectionStackTrigger.
     * 
     * @param pt ProjectionStackTrigger
     */
    public synchronized void remove(ProjectionStackTrigger pt) {
        if (triggers == null) {
            return;
        }
        triggers.remove(pt);
    }

    /**
     * Return a cloned list of Triggers.
     * 
     * @return Vector of triggers, null if none have been added.
     */
    public synchronized ArrayList<ProjectionStackTrigger> getTriggers() {
        if (triggers == null) {
            return null;
        }

        return (ArrayList<ProjectionStackTrigger>) triggers.clone();
    }

    public int size() {
        return triggers.size();
    }

    /**
     * Send a status to all registered triggers.
     * 
     * @param enableBackProjections there is at least one past
     *        projection in the back cache.
     * @param enableForwardProjections there is at least one future
     *        projection in the forward cache. Used when a past
     *        projection is being used.
     */
    public void fireStackStatus(boolean enableBackProjections,
                                boolean enableForwardProjections) {

        ArrayList<ProjectionStackTrigger> targets = getTriggers();

        if (triggers == null) {
            return;
        }

        Iterator<ProjectionStackTrigger> iterator = targets.iterator();

        while (iterator.hasNext()) {
            ProjectionStackTrigger target = iterator.next();
            if (Debug.debugging("projectionstack")) {
                Debug.output("ProjectionStackSupport.fireStackStatus(): target is: "
                        + target);
            }

            target.updateProjectionStackStatus(enableBackProjections,
                    enableForwardProjections);
        }
    }
}