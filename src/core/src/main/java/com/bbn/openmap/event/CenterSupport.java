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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CenterSupport.java,v $
// $RCSfile: CenterSupport.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;


/**
 * This is a utility class that can be used by beans that need support
 * for handling CenterListeners and firing CenterEvents You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 * <p>
 * A center event is one that sets the center of a map by specifying
 * latitude and longitude.
 */
public class CenterSupport extends ListenerSupport<CenterListener> {

    /**
     * @param sourceBean The bean to be given as the source for any
     *        events
     */
    public CenterSupport(Object sourceBean) {
        super(sourceBean);
    }

    /**
     * Send a center event to all registered listeners.
     * 
     * @param latitude the latitude
     * @param longitude the longitude
     * @see CenterEvent
     */
    public synchronized void fireCenter(double latitude, double longitude) {
        if (size() == 0)
            return;

        CenterEvent evt = new CenterEvent(source, latitude, longitude);

        for (CenterListener listener : this) {
            listener.center(evt);
        }
    }
}

