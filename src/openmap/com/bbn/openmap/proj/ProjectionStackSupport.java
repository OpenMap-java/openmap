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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStackSupport.java,v $
// $RCSfile: ProjectionStackSupport.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import com.bbn.openmap.util.Debug;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProjectionListeners and firing ProjectionEvents.  You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class ProjectionStackSupport implements java.io.Serializable {

    transient private ArrayList triggers;

    /**
     * Construct a ProjectionStackSupport.
     */
    public ProjectionStackSupport () { }

    /**
     * Add a ProjectionStackTrigger.
     * @param pt ProjectionStackTrigger
     */
    public synchronized void add(ProjectionStackTrigger pt) {
        if (triggers == null) {
            triggers = new ArrayList();
        }

        if (!triggers.contains(pt)) {
            triggers.add(pt);
        }
    }


    /**
     * Remove a ProjectionStackTrigger.
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
     * @return Vector of triggers, null if none have been added.
     */
    public synchronized ArrayList getTriggers(){
        if (triggers == null){
            return null;
        }

        return (ArrayList) triggers.clone();
    }

    public int size() {
        return triggers.size();
    }


    /**
     * Send a status to all registered triggers.
     * @param enableBackProjections there is at least one past
     * projection in the back cache.  
     * @param enableForwardProjections there is at least one future
     * projection in the forward cache.  Used when a past projection
     * is being used. 
     */
    public void fireStackStatus(boolean enableBackProjections,
                                boolean enableForwardProjections) {

        ProjectionStackTrigger target;
        ArrayList targets = getTriggers();

        if (triggers == null) {
            return;
        }

        Iterator iterator = targets.iterator();

        while (iterator.hasNext()) {
            target = (ProjectionStackTrigger)iterator.next();
            if (Debug.debugging("projectionstack")) {
                Debug.output("ProjectionStackSupport.fireStackStatus(): target is: " +
                             target);
            }

            target.updateProjectionStackStatus(enableBackProjections,
                                               enableForwardProjections);
        }
    }
}
