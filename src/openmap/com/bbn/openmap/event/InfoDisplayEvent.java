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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/InfoDisplayEvent.java,v $
// $RCSfile: InfoDisplayEvent.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
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
     * Construct an InfoDisplayEvent.
     * @param source Object
     */
    public InfoDisplayEvent(Object source) {
	this (source, null);
    }

    /**
     * Construct an InfoDisplayEvent.
     * @param source Object
     * @param info String information
     */
    public InfoDisplayEvent(Object source, String info) {
	super(source);
	information = info;
    }

    /**
     * Get the associated Layer or null.
     * Returns a Layer, if the Layer is the source of the event,
     * otherwise null.
     * @return Layer or null
     */
    public Layer getLayer() {
	Object obj = getSource();
	return (obj instanceof Layer)
	    ? (Layer)obj
	    : null;
    }

    /**
     * Get the information.
     * @return String information
     */
    public String getInformation() {
	return information;
    }

    /**
     * Set the information.
     * @param info String
     */
    public void setInformation(String info) {
	information = info;
    }
}
