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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerStatusListener.java,v $
// $RCSfile: LayerStatusListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

/**
 * Listens for the computational status of layers.
 */
public interface LayerStatusListener extends java.util.EventListener {

    /**
     * Update the Layer status.
     * @param evt LayerStatusEvent
     */
    public void updateLayerStatus(LayerStatusEvent evt);
}
