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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ZoomSupport.java,v $
// $RCSfile: ZoomSupport.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;


/**
 * This is a utility class that can be used by beans that need support
 * for handling ZoomListeners and firing ZoomEvents. You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class ZoomSupport extends ListenerSupport<ZoomListener> {

    /**
     * Construct a ZoomSupport.
     * 
     * @param sourceBean The bean to be given as the source for any
     *        events.
     */
    public ZoomSupport(Object sourceBean) {
        super(sourceBean);
    }

    /**
     * Send a zoom event to all registered listeners.
     * 
     * @param zoomType Either ZoomEvent.RELATIVE or ZoomEvent.ABSOLUTE
     * @param amount The new scale if ABSOLUTE, the multiplier if
     *        RELATIVE
     */
    public void fireZoom(int zoomType, float amount) {

        if (!((zoomType == ZoomEvent.RELATIVE) || (zoomType == ZoomEvent.ABSOLUTE))) {
            throw new IllegalArgumentException("Bad value, " + zoomType
                    + " for zoomType in " + "ZoomSupport.fireZoom()");
        }

        if (isEmpty())
            return;

        ZoomEvent evt = new ZoomEvent(source, zoomType, amount);

        for (ZoomListener listener : this) {
            listener.zoom(evt);
        }
    }

}