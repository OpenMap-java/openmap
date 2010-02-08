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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/GridBagToolBar.java,v $
// $RCSfile: GridBagToolBar.java,v $
// $Revision: 1.5 $
// $Date: 2006/03/06 15:41:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

import com.bbn.openmap.util.Debug;

/**
 * JToolBar with a GridBagLayout to have a more compressed look.
 */
public class GridBagToolBar extends javax.swing.JToolBar {
    GridBagLayout gridbag;
    GridBagConstraints c;

    public GridBagToolBar() {
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        setOrientation(SwingConstants.HORIZONTAL);
        setLayout(gridbag);
        setFloatable(false);
        if (Debug.debugging("layout")) {
            setBorder(BorderFactory.createLineBorder(Color.blue));
        }
    }

    /**
     */
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);

        // We need to remove and re-add the current components, they don't get
        // their constraints redone to match the current orientation.
        Component[] currentComps = getComponents();
        removeAll();

        if (c == null) {
            c = new GridBagConstraints();
        }
        
        if (getOrientation() == SwingConstants.HORIZONTAL) {
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 1.0f;
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.WEST;
        } else {
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTH;
            c.weightx = 1.0f;
            c.weighty = 0f;
            c.fill = GridBagConstraints.HORIZONTAL;
        }

        for (int i = 0; i < currentComps.length; i++) {
            add(currentComps[i]);
        }

    }

    public GridBagConstraints getConstraints() {
        return c;
    }
    
    public Component add(Component comp) {
        gridbag.setConstraints(comp, c);
        return super.add(comp);
    }
}