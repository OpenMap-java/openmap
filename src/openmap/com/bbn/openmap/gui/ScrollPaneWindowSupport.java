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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ScrollPaneWindowSupport.java,v $
// $RCSfile: ScrollPaneWindowSupport.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Component;

import javax.swing.JScrollPane;

/**
 * The ScrollPaneWindowSupport class does the same thing as
 * WindowSupport, it just wraps content in a JScrollPane.
 */
public class ScrollPaneWindowSupport extends WindowSupport {

    /**
     * Create the window support.
     * 
     * @param content the content to display in the window.
     * @param windowTitle the title of the window.
     */
    public ScrollPaneWindowSupport(Component content, String windowTitle) {
        super(content, windowTitle);
    }

    /**
     * Wrap content in a JScrollPane.
     */
    public Component modifyContent(Component comp) {
        return new JScrollPane(comp);
    }
}