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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/InfoDisplayListener.java,v $
// $RCSfile: InfoDisplayListener.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * Listens for requests to display information.
 */
public interface InfoDisplayListener extends java.util.EventListener {

    /**
     * Request to have a URL displayed in a Browser.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestURL(InfoDisplayEvent event);

    /**
     * Request to have a message displayed in a dialog window.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestMessage(InfoDisplayEvent event);

    /**
     * Request to have an information line displayed in an application
     * status window.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestInfoLine(InfoDisplayEvent event);

    /**
     * Request that plain text or html text be displayed in a browser.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestBrowserContent(InfoDisplayEvent event);

    /**
     * Request that the MapBean cursor be set to a certain type.
     * 
     * @param cursor java.awt.Cursor to set over the MapBean.
     */
    public void requestCursor(java.awt.Cursor cursor);

    /**
     * Request a tool tip be shown.
     * 
     * @param event The InfoDisplayEvent containing the text and
     *        requestor.
     */
    public void requestShowToolTip(InfoDisplayEvent event);

    /**
     * Request a tool tip be hidden.
     */
    public void requestHideToolTip();

}