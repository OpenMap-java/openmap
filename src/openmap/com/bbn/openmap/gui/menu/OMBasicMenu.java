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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/OMBasicMenu.java,v $
// $RCSfile: OMBasicMenu.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import com.bbn.openmap.gui.AbstractOpenMapMenu;

/**
 * This is version of AbstractOpenMapMenu that can be created and
 * configured with properties. No items are added by default.
 */
public class OMBasicMenu extends AbstractOpenMapMenu {

    public OMBasicMenu() {
        super();
    }

    public OMBasicMenu(String title) {
        super(title);
    }
}