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

/**
 * A component that can provide information about a time range it cares about.
 * Looks for TimeBoundsHandlers and adds them to itself. The TimeBoundsHandler
 * will also look for TimeBoundsProviders and add them to itself, two-way
 * connection, because they both have to initial communication at different
 * times.
 */
public interface TimeBoundsProvider {

    /**
     * A method called on the TimeBoundsProvider to retrieve the provider's time
     * bounds.
     */
    public TimeBounds getTimeBounds();

    /**
     * A method called on the TimeBoundsProvider to inform it of the overall
     * time bounds, in case some external time bounds values are being set by
     * another component and this provider wants to react to those bounds in
     * some way.
     */
    public void handleTimeBounds(TimeBounds tb);

    /**
     * A query that can be made to the TimeBoundsProvider asking it if it is
     * active, and if it's time bounds should be considered.
     */
    public boolean isActive();
    
    /**
     * An accessor that switches the TimeBoundsProvider on/off.
     */
    public void setActive(boolean active);

    /**
     * The TimeBoundsProvider keep track of the handlers it needs to notify when
     * the bounds or activeness changes. Called when a new handler needs to
     * know.
     */
    public void addTimeBoundsHandler(TimeBoundsHandler tbh);

    /**
     * The TimeBoundsProvider keep track of the handlers it needs to notify when
     * the bounds or activeness changes. Called when a handler doesn't need to
     * know.
     */
    public void removeTimeBoundsHandler(TimeBoundsHandler tbh);

}