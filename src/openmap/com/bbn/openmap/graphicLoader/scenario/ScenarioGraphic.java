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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/ScenarioGraphic.java,v $
// $RCSfile: ScenarioGraphic.java,v $
// $Revision: 1.1 $
// $Date: 2003/06/25 20:38:09 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.graphicLoader.scenario;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;

/**
 * An Interface that describes a map object used in a scenario, with
 * TimeStamp objects.
 */
public interface ScenarioGraphic {

    /**
     * Query the ScenarioGraphic according to it's name.  The string
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
     * certain time.
     */
    public void generateSnapshot(Projection p, long time);

    /**
     * Prepare the ScenarioPoint to display its overall scenario movements.
     */
    public void generateTotalScenario(Projection p);

}
