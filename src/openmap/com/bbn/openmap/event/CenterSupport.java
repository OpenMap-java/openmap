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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CenterSupport.java,v $
// $RCSfile: CenterSupport.java,v $
// $Revision: 1.3 $
// $Date: 2003/12/23 20:47:44 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling CenterListeners and firing CenterEvents  You can use
 * an instance of this class as a member field of your bean and
 * delegate work to it.
 * <p>
 * A center event is one that sets the center of a map by specifying
 * latitude and longitude.
 */
public class CenterSupport extends ListenerSupport {

    /**
     * @param sourceBean The bean to be given as the source for any events
     */
    public CenterSupport(Object sourceBean) {
	super(sourceBean);
    }

    /**
     * Add a CenterListener to the listener list.
     *
     * @param listener  The CenterListener to be added
     */
    public synchronized void addCenterListener(CenterListener listener) {
	addListener(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     *
     * @param listener  The CenterListener to be removed
     */
    public synchronized void removeCenterListener(CenterListener listener) {
	removeListener(listener);
    }

    /**
     * Send a center event to all registered listeners.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @see CenterEvent
     */
    public synchronized void fireCenter(float latitude, float longitude) {
	Iterator it = iterator();
	if (size() == 0) return;

	CenterEvent evt = new CenterEvent(source, latitude, longitude);

	while (it.hasNext()) {
	    ((CenterListener)it.next()).center(evt);
	}
    }
}

