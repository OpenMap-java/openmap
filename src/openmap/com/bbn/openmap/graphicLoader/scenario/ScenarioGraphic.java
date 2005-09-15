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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/ScenarioGraphic.java,v $
// $RCSfile: ScenarioGraphic.java,v $
// $Revision: 1.3 $
// $Date: 2005/09/15 14:39:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.scenario;

import com.bbn.openmap.proj.Projection;

/**
 * An Interface that describes a map object used in a scenario, with
 * TimeStamp objects.
 */
public interface ScenarioGraphic {

    /**
     * Query the ScenarioGraphic according to it's name. The string
     * should be interned.
     */
    public boolean thisIsYou(String name);

    /**
     * Add a location at a time.
     */
    public void addTimeStamp(TimeStamp timeStamp);

    /**
     * Remove a location at a certain time.
     */
    public boolean removeTimeStamp(TimeStamp timeStamp);

    /**
     * Clear all time stamps.
     */
    public void clearTimeStamps();

    /**
     * Prepare the ScenarioPoint to be rendered in its position at a
     * certain time. include a flag that says whether the entire path
     * of the graphic should be displayed.
     */
    public void generate(Projection p, long time, boolean showTotalScenario);

    /**
     * Prepare the ScenarioPoint to display its overall scenario
     * movements.
     */
    public void generateTotalScenario(Projection p);

}