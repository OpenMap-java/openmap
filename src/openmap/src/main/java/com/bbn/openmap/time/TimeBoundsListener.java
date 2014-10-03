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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/TimeBoundsListener.java,v $
// $RCSfile: TimeBoundsListener.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.beans.PropertyChangeListener;

/**
 * A component that is interested in knowing about time bounds, i.e. a window of
 * time. A TimeBoundsHandler will notify the listener when the time bounds
 * changes.
 */
public interface TimeBoundsListener extends PropertyChangeListener {

    /**
     * This method will be called on the TimeBoundsListener to let it know that
     * the time bounds have changed.
     * 
     * @param tbe TimeBoundsEvent with the source of the change along with the
     *        old and new values.
     */
    void updateTimeBounds(TimeBoundsEvent tbe);
}