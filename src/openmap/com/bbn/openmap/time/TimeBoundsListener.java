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
 * A component that is interested in knowing about
 * TimeBoundsProviders. Once it is given access to
 * TimeBoundsProviders, it will make queries on them.
 */
public interface TimeBoundsListener extends PropertyChangeListener {

    public void addTimeBoundsProvider(TimeBoundsProvider tbp);

    public void removeTimeBoundsProvider(TimeBoundsProvider tbp);

    public void clearTimeBoundsProviders();
}