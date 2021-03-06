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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/SelectionListener.java,v $
// $RCSfile: SelectionListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

/**
 * An interface defining an object that is interested in receiving
 * SelectionEvents.
 */
public interface SelectionListener {

    /**
     * The method that catches SelectionEvents.
     */
    public void selectionNotification(SelectionEvent se);

}