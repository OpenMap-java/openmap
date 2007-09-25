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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/TimeBoundsProvider.java,v $
// $RCSfile: TimeBoundsProvider.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.beans.PropertyChangeListener;

/**
 * A component that can provide information about a time range it
 * cares about.
 */
public interface TimeBoundsProvider {

    public final static String ACTIVE_PROPERTY = "ACTIVE_PROPERTY";
    public final static String ACTIVE = "ACTIVE";
    public final static String INACTIVE = "INACTIVE";

    /**
     * A method called on the TimeBoundsProvider to retrieve the
     * provider's time bounds.
     */
    public TimeBounds getTimeBounds();

    /**
     * A method called on the TimeBoundsProvider to set the provider's
     * time bounds.
     */
    public void setTimeBounds(TimeBounds tb);

    /**
     * A method called on the TimeBoundsProvider to inform it of some
     * external time bounds, in case it wants to react to it in some
     * way.
     */
    public void handleTimeBounds(TimeBounds tb);

    /**
     * A query that can be made to the TimeBoundsProvider asking it if
     * it is active, and if it's time bounds should be considered.
     */
    public boolean isActive();

    /**
     * TimeBoundsListeners expect that ACTIVE notifications will come
     * as PropertyChangeEvents. This method is for the listener adding
     * itself to the provider as a listener. For purposes of the
     * TimeBoundsProvider, the ACTIVE_PROPERTY should be the string
     * passed in here.
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener pcl);

    /**
     * TimeBoundsListeners expect that ACTIVE notifications will come
     * as PropertyChangeEvents. This method is for the listener
     * removing itself from the provider as a listener. For purposes
     * of the TimeBoundsProvider, the ACTIVE_PROPERTY should be the
     * string passed in here.
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener pcl);

}