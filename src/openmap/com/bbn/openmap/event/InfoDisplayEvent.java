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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/InfoDisplayEvent.java,v $
// $RCSfile: InfoDisplayEvent.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.Layer;

/**
 * An event to request that information be displayed.
 */
public class InfoDisplayEvent extends java.util.EventObject {

    /**
     * The requester may send information along with the event if the
     * event represents an information display request from the layer,
     * this variable contains the information needed to process the
     * event.
     */
    protected String information = null;

    /**
     * A preferred location index for which info line, if there is
     * more than one, should display the requested information. The
     * default is 0.
     */
    protected int preferredLocation = 0;

    /**
     * Construct an InfoDisplayEvent.
     * 
     * @param source Object
     */
    public InfoDisplayEvent(Object source) {
        this(source, null);
    }

    /**
     * Construct an InfoDisplayEvent.
     * 
     * @param source Object
     * @param info String information
     */
    public InfoDisplayEvent(Object source, String info) {
        super(source);
        information = info;
    }

    /**
     * Construct an InfoDisplayEvent.
     * 
     * @param source Object
     * @param info String information
     * @param loc the location index for which info line should
     *        display the information.
     */
    public InfoDisplayEvent(Object source, String info, int loc) {
        super(source);
        information = info;
        preferredLocation = loc;
    }

    /**
     * Get the associated Layer or null. Returns a Layer, if the Layer
     * is the source of the event, otherwise null.
     * 
     * @return Layer or null
     */
    public Layer getLayer() {
        Object obj = getSource();
        return (obj instanceof Layer) ? (Layer) obj : null;
    }

    /**
     * Get the information.
     * 
     * @return String information
     */
    public String getInformation() {
        return information;
    }

    /**
     * Set the information.
     * 
     * @param info String
     */
    public void setInformation(String info) {
        information = info;
    }

    /**
     * Get the preferred location index where the information should
     * be displayed.
     */
    public int getPreferredLocation() {
        return preferredLocation;
    }

    /**
     * Set the preferred location index where the information should
     * be displayed.
     */
    public void setPreferredLocation(int pl) {
        preferredLocation = pl;
    }
}