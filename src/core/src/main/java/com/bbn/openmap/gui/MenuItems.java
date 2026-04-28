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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MenuItems.java,v $
// $RCSfile: MenuItems.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.util.Iterator;

/**
 * This interface should be used for objects representing a group of
 * MenuItem(s) that should be added to a Menu. If a Menu comes in
 * contact with a MenuItems object, it will use the iterator to go
 * through all of the JMenuItem objects that are contained within the
 * MenuItems.
 */
public interface MenuItems {

    /**
     * Get the java.util.Iterator used to retrieve all of the
     * JMenuItems held inside the MenuItems object.
     */
    public Iterator iterator();
}