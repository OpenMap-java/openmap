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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/InfoDisplayListener.java,v $
// $RCSfile: InfoDisplayListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.awt.event.MouseEvent;

/**
 * Listens for requests to display information.
 */
public interface InfoDisplayListener extends java.util.EventListener {

    /**
     * Request to have a URL displayed in a Browser.
     * @param event InfoDisplayEvent
     */
    public void requestURL(InfoDisplayEvent event);

    /** 
     * Request to have a message displayed in a dialog window.
     * @param event InfoDisplayEvent
     */
    public void requestMessage(InfoDisplayEvent event);
    
    /** 
     * Request to have an information line displayed in an
     * application status window.
     * @param event InfoDisplayEvent
     */
    public void requestInfoLine(InfoDisplayEvent event);

    /** 
     * Request that plain text or html text be displayed in a
     * browser.
     * @param event InfoDisplayEvent
     */
    public void requestBrowserContent(InfoDisplayEvent event);

    /**
     * Request that the MapBean cursor be set to a certain type.
     * @param Cursor java.awt.Cursor to set over the MapBean.
     */
    public void requestCursor(java.awt.Cursor cursor);

    /**
     * Request a tool tip be shown.
     *
     * @param me MouseEvent for where the tip should be placed.
     * @param event The InfoDisplayEvent containing the text and
     * requestor.  
     */
    public void requestShowToolTip(MouseEvent me, InfoDisplayEvent event);

    /**
     * Request a tool tip be hidden.
     *
     * @param me MouseEvent for where the tip was. May not be exact.
     */
    public void requestHideToolTip(MouseEvent me);

}
