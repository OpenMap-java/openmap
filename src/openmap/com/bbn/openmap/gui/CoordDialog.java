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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/CoordDialog.java,v $
// $RCSfile: CoordDialog.java,v $
// $Revision: 1.6 $
// $Date: 2004/01/26 18:18:07 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import com.bbn.openmap.*;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.util.Debug;

import javax.swing.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.io.Serializable;

/**
 * A Dialog box wrapper for a CombinedCoordPanel.
 */
public class CoordDialog extends JDialog 
    implements Serializable, ActionListener, LightMapHandlerChild {

    protected transient CombinedCoordPanel ccp;

    public final static String DEFAULT_TITLE = "Go To Coordinates";

    /** 
     * Creates a Dialog Box with a CombinedCoordPanel.
     */
    public CoordDialog() {
        super();
        setTitle(DEFAULT_TITLE);
        setup();
    }

    /** 
     * Creates a Dialog Box with a CombinedCoordPanel
     * with a specified title and comment.
     */
    public CoordDialog(String title, String comment) {
        super();
        setTitle(DEFAULT_TITLE);
        setup(comment);
    }

    /**
     *  Creates a CoordPanel (which has latitude and longitude entry boxes)
     *  and Apply and Close buttons
     */
    protected void setup() {
        setup(CombinedCoordPanel.DEFAULT_COMMENT);
    }

    protected void setup(String comment) {
        ccp = new CombinedCoordPanel(comment, this);
        getContentPane().add(ccp);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand() == CombinedCoordPanel.CloseCmd) {
            setVisible(false);
        }
    }

    /**
     *  @return the LatLonPoint represented by contents of the 
     *  entry boxes in the CoordPanel
     */
    public LatLonPoint getLatLon() {
        return ccp.getLatLon();
    }

    /**
     * Sets the contents of the latitude and longitude entry 
     * boxes in CoordPanel
     * @param llpoint the object containt the coordinates that should
     * go in the boxes
     */
    public void setLatLon(LatLonPoint llpoint) {
        ccp.setLatLon(llpoint);
    }

    /**
     * Add a CenterListener to the CombinedCoordPanel to receive
     * events when the apply button is hit.
     *
     * @param listener  The CenterListener to be added
     */
    public void addCenterListener(CenterListener listener) {
        ccp.addCenterListener(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     *
     * @param listener  The CenterListener to be removed
     */
    public void removeCenterListener(CenterListener listener) {
        ccp.removeCenterListener(listener);
    }

    /**
     * LightMapHandlerChild method.  The CoordDialog passes all
     * objects to the CombinedCoordPanel.
     */
    public void findAndInit(Object someObj) {
        ccp.findAndInit(someObj);
    }

    /**
     * LightMapHandlerChild method.  The CoordDialog passes all
     * objects to the CombinedCoordPanel.
     */
   public void findAndUndo(Object someObj) {
       ccp.findAndUndo(someObj);
    }

}
