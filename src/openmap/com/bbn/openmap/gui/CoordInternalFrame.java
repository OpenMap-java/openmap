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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/CoordInternalFrame.java,v $
// $RCSfile: CoordInternalFrame.java,v $
// $Revision: 1.8 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.Environment;

/**
 * An Internal Frame wrapper for a CombinedCoordPanel.
 */
public class CoordInternalFrame extends JInternalFrame implements Serializable,
        ActionListener, LightMapHandlerChild {

    protected CombinedCoordPanel ccp;

    /**
     * Creates the internal frame with a CoordPanel and Apply and
     * Close buttons. You MUST call addCenterListener() for the Apply
     * button to do anything.
     */
    public CoordInternalFrame() {
        super(Environment.getI18n().get(CoordDialog.class,
                "defaultTitle",
                "Go To Coordinates"), true, //resizable
              false, //closable - weird bug, won't close the second
                     // time
              false, //maximizable
              true //iconifiable
        );
        setup();
    }

    /**
     * Creates a CoordPanel (which has latitude and longitude entry
     * boxes) and Apply and Close buttons
     */
    protected void setup() {
        ccp = new CombinedCoordPanel(this);
        getContentPane().add(ccp);
        setOpaque(true);
    }

    /**
     * Add a CenterListener to the CombinedCoordPanel to receive
     * events when the apply button is hit.
     * 
     * @param listener The CenterListener to be added
     */
    public void addCenterListener(CenterListener listener) {
        ccp.addCenterListener(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     * 
     * @param listener The CenterListener to be removed
     */
    public void removeCenterListener(CenterListener listener) {
        ccp.removeCenterListener(listener);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand() == CombinedCoordPanel.CloseCmd) {
            setVisible(false);
        }
    }

    /**
     * LightMapHandlerChild method. The CoordInternalFrame passes all
     * objects to the CombinedCoordPanel.
     */
    public void findAndInit(Object someObj) {
        ccp.findAndInit(someObj);
    }

    /**
     * LightMapHandlerChild method. The CoordInternalFrame passes all
     * objects to the CombinedCoordPanel.
     */
    public void findAndUndo(Object someObj) {
        ccp.findAndUndo(someObj);
    }
}