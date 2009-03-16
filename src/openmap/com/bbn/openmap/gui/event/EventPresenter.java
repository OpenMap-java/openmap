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
// $Source: /cvs/darwars/ambush/aar/src/com/bbn/hotwash/gui/EventPresenter.java,v $
// $RCSfile: EventPresenter.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.Component;

/**
 * An EventPresenter is a empty interface that marks a component to be picked up
 * by the EventPanel.
 */
public interface EventPresenter extends FilterPresenter {

    /**
     * @return the main event display.
     */
    public Component getComponent();

}