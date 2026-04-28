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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PanSupport.java,v $
// $RCSfile: PanSupport.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;


/**
 * This is a utility class that can be used by beans that need support
 * for handling PanListeners and firing PanEvents. You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class PanSupport extends ListenerSupport<PanListener> {

    /**
     * Construct a PanSupport.
     * 
     * @param sourceBean The bean to be given as the source for any
     *        events.
     */
    public PanSupport(Object sourceBean) {
        super(sourceBean);
    }

    /**
     * Send a pan event to all registered listeners.
     * 
     * @param direction PanEvent.NORTH ... PanEvent.NORTH_WEST
     * @see PanEvent
     * @deprecated use firePan(azimuth)
     */
    public void firePan(int direction) {
        firePan(direction, 1f);
    }

    /**
     * Send a pan event to all registered listeners.
     * 
     * @param direction PanEvent.NORTH ... PanEvent.NORTH_WEST
     * @param amount (0.0 &lt;= amount) in decimal degrees.
     * @see PanEvent
     * @deprecated use firePan(azimuth, arc)
     */
    public void firePan(int direction, float amount) {

        if (direction < PanEvent.PAN_FIRST || direction > PanEvent.PAN_LAST) {
            throw new IllegalArgumentException("Bad value, " + direction
                    + " for direction in " + "PanSupport.firePan()");
        }

        float az = PanEvent.dir2Az(direction);
        firePan(az);
    }

    public void firePan(float Az) {
        firePan(Az, Float.NaN);
    }

    /**
     * Fire a pan event.
     * 
     * @param az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees.
     */
    public synchronized void firePan(float az, float c) {

        if (isEmpty())
            return;

        PanEvent evt = new PanEvent(source, az, c);

        for (PanListener listener : this) {
            listener.pan(evt);
        }
    }
}

